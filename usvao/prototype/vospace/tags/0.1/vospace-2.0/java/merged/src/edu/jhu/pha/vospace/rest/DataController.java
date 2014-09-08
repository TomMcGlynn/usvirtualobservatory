/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.vospace.rest;

import java.io.InputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.caltech.vao.vospace.meta.MetaStore;
import edu.caltech.vao.vospace.meta.MetaStoreFactory;
import edu.caltech.vao.vospace.storage.StorageManager;
import edu.caltech.vao.vospace.storage.StorageManagerFactory;
import edu.caltech.vao.vospace.xml.IdHelpers;
import edu.caltech.vao.vospace.xml.Node;
import edu.caltech.vao.vospace.xml.NodeFactory;
import edu.jhu.pha.vospace.jobs.JobsProcessor;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.PathSeparator;
import edu.jhu.pha.vospace.api.PathSeparator.NodePath;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;
import edu.jhu.pha.vospace.oauth.UserHelper;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

/**
 * Provides the REST service for /data/ path: the functions for manipulating the nodes data content
 * @author Dmitry Mishin
 */
@Path("/data/")
public class DataController {
	
	private static Logger logger = Logger.getLogger(DataController.class);
	private @Context HttpServletResponse response;
	private @Context HttpServletRequest request;
	private static Configuration conf = SettingsServlet.getConfig();;
	
	/**
	 * Returns the data of a transfer
	 * @param jobId Job identifier
	 * @return transfer representation
	 */
	@GET @Path("{jobid}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream getTransferData(@PathParam("jobid") String jobId) {
		JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));
		if(null == job)
			throw new NotFoundException("The job "+jobId+" is not found.");

		if(!IdHelpers.validId(job.getTarget())) {
			JobsProcessor.modifyJobState(job, STATE.ERROR, "The requested URI "+job.getTarget()+" is invalid.");
			throw new BadRequestException("The requested URI "+job.getTarget()+" is invalid.");
		}
		
		if(job.getDirection().equals(JobDescription.DIRECTION.PULLFROMVOSPACE)){
			
			JobsProcessor.modifyJobState(job, STATE.RUN);
			
			logger.debug("Downloading node "+job.getTarget());
			
			StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(job.getUsername()));
			
			try {
				InputStream dataInp = backend.getBytes(job.getTarget());
				response.setHeader("Content-Disposition", "attachment; filename="+job.getTarget().substring(job.getTarget().lastIndexOf("/")+1));
				response.setHeader("Content-Length", backend.getNodeSize(job.getTarget()));
				
				JobsProcessor.modifyJobState(job, STATE.COMPLETED);
				return dataInp;
			} catch(InternalServerErrorException ex) {
				JobsProcessor.modifyJobState(job, STATE.ERROR);
				throw ex;
			} catch(NotFoundException ex) {
				JobsProcessor.modifyJobState(job, STATE.ERROR);
				throw ex;
			} catch(PermissionDeniedException ex) {
				JobsProcessor.modifyJobState(job, STATE.ERROR);
				throw ex;
			}
		}
		
		throw new InternalServerErrorException("The job "+job.getDirection()+" is unsupported in this path.");
	}
	
	/**
	 * Method supporting data upload (push to VOSpace)
	 * @param fullPath The node path
	 * @param fileNameInp Node fileName
	 * @param fileDataInp Node data
	 * @return HTTP response
	 */
	@PUT @Path("{jobid}") 
    public Response uploadNodePut(@PathParam("jobid") String jobId, InputStream fileDataInp) {
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();
		JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));
		if(null == job)
			throw new NotFoundException("The job "+jobId+" is not found.");

		if(!IdHelpers.validId(job.getTarget())) {
			JobsProcessor.modifyJobState(job, STATE.ERROR, "The requested URI "+job.getTarget()+" is invalid.");
			throw new BadRequestException("The requested URI "+job.getTarget()+" is invalid.");
		}
		
		JobsProcessor.modifyJobState(job, STATE.RUN);
		
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(job.getUsername()));

		if(job.getDirection().equals(JobDescription.DIRECTION.PUSHTOVOSPACE)){
			
			String id = job.getTarget();
			if(id.endsWith(".auto")) {
				id = id.substring(0, id.length() - ".auto".length()) + UUID.randomUUID().toString();
			}

			if(!store.isStored(id, (String)job.getUsername())){
				Node newNode = NodeFactory.getInstance().getDefaultNode();
				newNode.setUri(id);
				IdHelpers.create(newNode, job.getUsername(), false);
			}
			
			logger.debug("Uploading node "+id);
			
			try {
				backend.putBytes(id, fileDataInp);
			} catch(InternalServerErrorException ex) {
				JobsProcessor.modifyJobState(job, STATE.ERROR);
				throw ex;
			}
			
			JobsProcessor.modifyJobState(job, STATE.COMPLETED);
			return Response.ok().build();
		}
		
		throw new InternalServerErrorException("The job "+job.getDirection()+" is unsupported in this path.");
    }
}
