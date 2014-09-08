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
package edu.jhu.pha.vospace.jobs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.meta.MetaStore;
import edu.caltech.vao.vospace.meta.MetaStoreFactory;
import edu.caltech.vao.vospace.protocol.ProtocolHandler;
import edu.caltech.vao.vospace.storage.StorageManager;
import edu.caltech.vao.vospace.storage.StorageManagerFactory;
import edu.caltech.vao.vospace.xml.ContainerNode;
import edu.caltech.vao.vospace.xml.IdHelpers;
import edu.caltech.vao.vospace.xml.Node;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.ConflictException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.oauth.UserHelper;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.rest.JobDescription.DIRECTION;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

public class TransferThread extends Thread {

	private static final Logger logger = Logger.getLogger(TransferThread.class);
	private static Configuration conf = SettingsServlet.getConfig();
	
	private final JobDescription job;
	private final JobsProcessor proc;

	public TransferThread(JobDescription job, JobsProcessor proc) {
		this.job = job;
		this.proc = proc;
	}

	/**
	 * Executes the RequestMethod
	 */
	@Override
	public void run() {
		JobsProcessor.modifyJobState(job, STATE.RUN);
		logger.debug("Started the job "+job.getId());

		try {
			validateTransfer(job);

			ProtocolHandler handler = null;

			if(job.getDirection().equals(DIRECTION.LOCAL)){ // local job
				logger.debug("Processing local job.");
				if(job.isKeepBytes()){
					copyNode(job);
				} else {
					moveNode(job);
				}
				JobsProcessor.modifyJobState(job, STATE.COMPLETED);
			} else {
				for(String protocolKey: job.getProtocols().keySet()) {
					if(null != proc.getProtocolHandler(protocolKey, this)){
						handler = proc.getProtocolHandler(protocolKey, this);
					}
				}

				if(null == handler) {
					JobsProcessor.modifyJobState(job, STATE.ERROR, "The service supports none of the requested Protocols");
				} else {
					handler.invoke(job);
					JobsProcessor.modifyJobState(job, STATE.COMPLETED);
				}
			}

		} catch(WebApplicationException ex) {
			ex.printStackTrace();
			JobsProcessor.modifyJobState(job, STATE.ERROR, ex.getResponse().getEntity().toString());
			logger.error("Error executing job "+job.getId()+": "+ex.getResponse().getEntity().toString());
		} catch(Exception ex) {
			ex.printStackTrace();
			JobsProcessor.modifyJobState(job, STATE.ERROR, ex.toString());
			logger.error("Error executing job "+job.getId()+": "+ex.toString());
		}

		synchronized(JobsProcessor.class) {
			JobsProcessor.class.notify();
		}
	}


	/**
	 * Move from the specified target to the direction
	 */
	private static void moveNode(JobDescription transfer) {
		logger.debug("Moving data");
		logger.debug(transfer.getJobXmlDescription());
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();
		StorageManager manager = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(transfer.getUsername()));

		// Request details
		String target = transfer.getTarget();
		String direction = transfer.getDirectionTarget();
		// Get node
		Node node = store.getNode(target, transfer.getUsername());

		// Check whether endpoint is reserved URI
		if (direction.endsWith(".null")) {
			manager.removeBytes(target);
		}
		if (direction.endsWith(".auto")) direction = generateUri(direction, ".auto"); 
		
		if(!store.isStored(target, transfer.getUsername()))
			throw new NotFoundException("NodeNotFound");
		
		// Check whether endpoint is a container
		if (store.getType(target, transfer.getUsername()).equals(NodeType.CONTAINER_NODE)) 
			direction += target.substring(target.lastIndexOf("/"));
		// Change identifier
		node.setUri(direction);
		// Move bytes
		manager.moveBytes(target, direction);
		// Store update node
		store.updateData(target, direction, transfer.getUsername(), node.toString());
		// Check if target is a container
		if (node instanceof ContainerNode) {
			// Move directory
			//manager.moveBytes(target, direction);
			// Update metadata
			for (String child: store.getAllChildren(target, transfer.getUsername())) {
				// Update uri
				Node childNode = store.getNode(child, transfer.getUsername());
				node.setUri(child.replace(target, direction));
				// Store moved node
				store.updateData(child, childNode.getUri(), transfer.getUsername(), childNode.toString());
			}
		}
	}

	/**
	 * Copy from the specified target to the direction
	 */
	private static void copyNode(JobDescription transfer) {
		logger.debug("Copying data");
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();
		StorageManager manager = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(transfer.getUsername()));

		// Request details
		String target = transfer.getTarget();
		String direction = transfer.getDirectionTarget();
		// Get node
		Node node = store.getNode(target, transfer.getUsername());
		// Check whether endpoint is reserved URI
		if (direction.endsWith(".null")) {
			manager.removeBytes(target);
		}
		if (direction.endsWith(".auto")) direction = generateUri(direction, ".auto");
		
		// Check whether endpoint is a container
		if (store.isStored(direction, transfer.getUsername()) &&
				store.getType(direction, transfer.getUsername()).equals(NodeType.CONTAINER_NODE)) 
			direction += target.substring(target.lastIndexOf("/"));
		// Change identifier
		node.setUri(direction);
		// Copy bytes
		manager.copyBytes(target, direction);
		// Store new node
		store.storeData(direction, store.getType(target, transfer.getUsername()), transfer.getUsername(), node.toString());
		// Check if target is a container
		if (node instanceof ContainerNode) {
			// Update metadata
			for (String child: store.getAllChildren(target, transfer.getUsername())) {
				// Update uri
				Node childNode = store.getNode(child, transfer.getUsername());
				node.setUri(child.replace(target, direction));
				NodeType type = store.getType(child, transfer.getUsername());
				// Store copy node
				store.storeData(child, type, transfer.getUsername(), childNode.toString());
			}
		}
	}
	
	private void validateTransfer(JobDescription transfer) {
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();

		// Check transfer details
		String target = transfer.getTarget();
		String direction = transfer.getDirectionTarget();
		boolean external = !transfer.getDirection().equals(DIRECTION.LOCAL);
		// Syntactically valid target and direction (move, copy)
		if (!IdHelpers.validId(target)) throw new BadRequestException("The requested URI is invalid.");
		
		if (!external && !IdHelpers.validId(direction)) throw new BadRequestException("The requested URI is invalid.");
		// Parent node
		if (!external && !IdHelpers.validParent(direction, transfer.getUsername())) throw new BadRequestException("The parent node is not valid.");
		// Existence
		if (store.isStored(target, transfer.getUsername())) {
			if (transfer.getDirection().equals(DIRECTION.PUSHTOVOSPACE) || transfer.getDirection().equals(DIRECTION.PULLTOVOSPACE)) {
				// Container
				if (store.getType(target, transfer.getUsername()).equals(NodeType.CONTAINER_NODE)) 
					throw new BadRequestException("Data cannot be uploaded to a container."); 
			}
		} else {
			if (!external) throw new ConflictException("A Node does not exist with the requested URI"); 
		}
		if (!external && store.isStored(direction, transfer.getUsername()) && !store.getType(direction, transfer.getUsername()).equals(NodeType.CONTAINER_NODE)) { 
			throw new ConflictException("A Node already exists with the requested URI");		
		}
		if (external) {
			// Views
			if (transfer.getViews().isEmpty()) throw new BadRequestException("A View must be specified.");
			//if (!view.equals(Views.get(Views.View.DEFAULT)) && !manager.SPACE_ACCEPTS_IMAGE.contains(view) && !manager.SPACE_ACCEPTS_TABLE.contains(view) && !manager.SPACE_ACCEPTS_ARCHIVE.contains(view)) throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "The service does not support the requested view");
		}

	}

	
	/**
	 * Generate a URI replacing the specified part
	 * @param uri The static part of the URI
	 * @param remove The part of the URI to replace with an autogenerated part
	 * @return the new URI
	 */
	private static String generateUri(String uri, String remove) {
		String newUri = uri.substring(0, uri.length() - remove.length()) + UUID.randomUUID().toString();
		return newUri;
	}

}
