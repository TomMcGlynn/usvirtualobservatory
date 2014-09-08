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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import com.generationjava.io.xml.SimpleXmlWriter;
import edu.jhu.pha.vospace.jobs.JobsProcessor;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.rest.JobDescription.DIRECTION;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

/**
 * Provides the REST service for /transfers/ path: the functions for manipulating the transfer jobs
 * @author Dmitry Mishin
 */
@Path("/transfers/")
public class TransfersController {
	
	private static Logger logger = Logger.getLogger(TransfersController.class);
	private static Configuration conf = SettingsServlet.getConfig();;
	private @Context HttpServletRequest request;
	
	/**
	 * Manages the transfers methods:
	 * sending data to a service (pushToVoSpace)
	 * importing data into a service (pullToVoSpace)
	 * reading data from a service (pullFromVoSpace)
	 * sending data from a service (pushFromVoSpace)
	 * @param fullPath the path of the node
	 * @param fileDataInp the XML representation of the job
	 * @return HTTP result
	 */
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_XML)
    public Response transferNodePost(String xmlNode) {
		logger.debug("Got new job");
		String username = (String)request.getAttribute("username");
		
		StringReader strRead = new StringReader(xmlNode);
		UUID jobUID = UUID.randomUUID();
		try {
			
			JobDescription job = new JobDescription();
			job.setJobXmlDescription(xmlNode);
			job.setId(jobUID.toString());
			job.setUsername(username);
			job.setStartTime(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
			job.setState(JobDescription.STATE.PENDING);

			SAXBuilder xmlBuilder = new SAXBuilder();
			Element nodeElm = xmlBuilder.build(strRead).getRootElement();
			//List<Element> paramNodes = nodeElm.getChild("jobInfo",Namespace.getNamespace(conf.getString("namespace.uws"))).getChild("transfer",Namespace.getNamespace(conf.getString("namespace.vos"))).getChildren();
			List<Element> paramNodes = nodeElm.getChildren();
			for(Iterator<Element> it = paramNodes.iterator(); it.hasNext();){
				Element param = it.next();
				if(param.getName().equals("target")){
					job.setTarget(param.getValue());
				} else if(param.getName().equals("direction")){
					if(param.getValue().equals(conf.getString("transfers.direction.pull_from_vospace"))) {
						job.setDirection(JobDescription.DIRECTION.PULLFROMVOSPACE);
						job.addProtocol(conf.getString("transfers.protocol.httpget"), conf.getString("application.url")+"/rest/data/"+job.getId());
					} else if(param.getValue().equals(conf.getString("transfers.direction.pull_to_vospace"))) {
						job.setDirection(JobDescription.DIRECTION.PULLTOVOSPACE);
					} else if(param.getValue().equals(conf.getString("transfers.direction.push_from_vospace"))) {
						job.setDirection(JobDescription.DIRECTION.PUSHFROMVOSPACE);
					} else if(param.getValue().equals(conf.getString("transfers.direction.push_to_vospace"))) {
						job.setDirection(JobDescription.DIRECTION.PUSHTOVOSPACE);
						job.addProtocol(conf.getString("transfers.protocol.httpput"), conf.getString("application.url")+"/rest/data/"+job.getId());
					} else {
						job.setDirection(DIRECTION.LOCAL);
						job.setDirectionTarget(param.getValue());
					}
				} else if(param.getName().equals("view")){
					job.addView(param.getValue());
				} else if(param.getName().equals("keepBytes")){
					job.setKeepBytes(Boolean.parseBoolean(param.getValue()));
				} else if(param.getName().equals("protocol")){
					String protocol = param.getAttributeValue("uri");
					String protocolEndpoint = param.getChildText("protocolEndpoint", Namespace.getNamespace(conf.getString("namespace.vos")));
					
					if(job.getDirection().equals(DIRECTION.PULLFROMVOSPACE) || job.getDirection().equals(DIRECTION.PUSHTOVOSPACE)){
						protocolEndpoint = conf.getString("application.url")+"/rest/data/"+job.getId();
					}
					
					if(null != protocol && null != protocolEndpoint)
						job.addProtocol(protocol, protocolEndpoint);
					else
						throw new BadRequestException("InvalidArgument");
				}
			}

			logger.debug(job.toString());
			
			Method submitJobMethod = Class.forName(conf.getString("jobsprocessor.class")).getMethod("submitJob", String.class, JobDescription.class);
			submitJobMethod.invoke(null, username, job);
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			logger.error(e);
			throw new InternalServerErrorException(e);
		} catch (SecurityException e) {
			throw new InternalServerErrorException(e);
		} catch (NoSuchMethodException e) {
			throw new InternalServerErrorException(e);
		} catch (ClassNotFoundException e) {
			throw new InternalServerErrorException(e);
		} catch (IllegalArgumentException e) {
			throw new InternalServerErrorException(e);
		} catch (IllegalAccessException e) {
			throw new InternalServerErrorException(e);
		} catch (InvocationTargetException e) {
			throw new InternalServerErrorException(e);
		} finally {
			strRead.close();
		}

		// Makes DOJO to automatically make the GET request which fails because of the lack of autentification. Replaced with OK + link.
		//try {
			//String redirectUri = conf.getString("application.url")+"/rest/transfers/"+jobUID.toString();
			//Response resp = Response.seeOther(new URI(redirectUri)).build();
		
			//return resp;
		//} catch (URISyntaxException e) {
		//	throw new InternalServerErrorException(e);
		//}

		//return Response.ok(conf.getString("application.url")+"/rest/transfers/"+jobUID.toString()).build();
		return Response.ok(getJob(jobUID.toString())).build();
	}
	
	/**
	 * Returns the transfer representation
	 * @param jobId Job identifier
	 * @return transfer representation
	 */
	@GET @Path("{jobId}")
	@Produces(MediaType.TEXT_XML)
	public String getTransferDetails(@PathParam("jobId") String jobId) {
		return getJob(jobId);
	}

	
	private String getJob(String jobId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		StringWriter writ = new StringWriter();
		SimpleXmlWriter xw = new SimpleXmlWriter(writ);
		try {
			JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));
			
			xw.writeEntity("uws:job").
				writeAttribute("xmlns:uws", conf.getString("namespace.uws")).
				writeAttribute("xmlns:vos", conf.getString("namespace.vos")).
				writeAttribute("xmlns:xlink", conf.getString("namespace.xlink")).
				writeAttribute("xmlns:xsi", conf.getString("namespace.xsi")).
				writeAttribute("xmlns:schemaLocation", conf.getString("schema_location"));

		  
			xw.writeEntity("uws:jobId").writeText(jobId).endEntity();
			
			xw.writeEntity("uws:ownerId").writeAttribute("xsi:nil", "true").endEntity();

			if(null == job) {
				xw.writeEntity("uws:phase").writeText(STATE.ERROR).endEntity();
				xw.writeEntity("uws:errorSummary").writeText("Internal Fault").endEntity();
				xw.writeEntity("result").writeAttribute("id", "transferDetails").writeAttribute("xlink:href",jobId+"/error").endEntity();
			} else {
					xw.writeEntity("uws:phase").writeText(job.getState().toString()).endEntity();
				
				xw.writeEntity("uws:startTime");
				if(null != job.getStartTime())
					xw.writeText(dateFormat.format(job.getStartTime()));
				else
					xw.writeAttribute("xsi:nil", "true");
				xw.endEntity();
	
				xw.writeEntity("uws:endTime");
				if(null != job.getEndTime())
					xw.writeText(dateFormat.format(job.getEndTime()));
				else
					xw.writeAttribute("xsi:nil", "true");
				xw.endEntity();
				
				xw.writeEntity("uws:executionDuration").writeText("0").endEntity();
				xw.writeEntity("uws:destruction").writeAttribute("xsi:nil", "true").endEntity();
				
				xw.writeEntity("uws:jobInfo");

					xw.writeEntity("vos:transfer");

						switch(job.getDirection()){
							case PULLFROMVOSPACE:
								xw.writeEntity("vos:direction").writeText(conf.getString("transfers.direction.pull_from_vospace")).endEntity();
								break;
							case PULLTOVOSPACE:
								xw.writeEntity("vos:direction").writeText(conf.getString("transfers.direction.pull_to_vospace")).endEntity();
								break;
							case PUSHFROMVOSPACE:
								xw.writeEntity("vos:direction").writeText(conf.getString("transfers.direction.push_from_vospace")).endEntity();
								break;
							case PUSHTOVOSPACE:
								xw.writeEntity("vos:direction").writeText(conf.getString("transfers.direction.push_to_vospace")).endEntity();
								break;
						}

						for(Iterator<String> it = job.getProtocols().keySet().iterator(); it.hasNext();){
							String protocol = it.next();
							String protocolEndpoint = job.getProtocols().get(protocol);
							xw.writeEntity("vos:protocol").writeAttribute("uri", protocol);
							if(null != protocolEndpoint && !protocolEndpoint.isEmpty()){
								xw.writeEntity("vos:protocolEndpoint").writeText(protocolEndpoint).endEntity();
							}
							xw.endEntity();
						}
						xw.writeEntity("vos:target").writeText(job.getTarget()).endEntity();
						for(String view: job.getViews())
							xw.writeEntity("vos:view").writeText(view).endEntity();

					xw.endEntity();

				xw.endEntity();
		
				xw.writeEntity("uws:results");
				xw.writeEntity("result").writeAttribute("id", "transferDetails").writeAttribute("xlink:href",conf.getString("application.url")+"/rest/transfers/"+job.getId()+"/results/details").endEntity();
				xw.endEntity();
			}
	
			
			xw.endEntity();
			xw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		} catch(IllegalArgumentException ex) {
			throw new BadRequestException(ex);
		}
		return writ.toString();
	}
	
	@POST @Path("{jobid}/phase")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response changeJobPhase(@PathParam("jobid") String jobId, String phase) {
		logger.debug("Got new phase "+phase+" for job "+jobId);
		/**TODO make something here */
		return Response.ok().build();
	}
	
	/**
	 * Returns the transfer error representation
	 * @param jobId Job identifier
	 * @return transfer representation
	 */
	@GET @Path("{jobid}/error")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTransferErrorDetails(@PathParam("jobid") String jobId) {
		JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));

		StringBuffer errorDescr = new StringBuffer();
		if(null == job)
			errorDescr.append("The job "+jobId+" is not found.\n");
		else
			errorDescr.append(job.getNote());
		return errorDescr.toString();
	}

	/**
	 * Returns the transfer result
	 * @param jobId Job identifier
	 * @return transfer result
	 */
	@GET @Path("{jobid}/results")
	@Produces(MediaType.TEXT_XML)
	public String getTransferResults(@PathParam("jobid") String jobId) {
		JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));

		if(null == job)
			throw new NotFoundException("The job "+jobId+" is not found.");

		//if(!job.getState().equals(JobDescription.STATE.COMPLETED))
		//	return "";
		
		StringWriter writ = new StringWriter();
		SimpleXmlWriter xw = new SimpleXmlWriter(writ);
		try {
			xw.writeEntity("ns0:results").writeAttribute("xmlns:ns0", conf.getString("namespace.uws"));
				xw.writeEntity("ns0:result").
					writeAttribute("xmlns:ns1", conf.getString("namespace.xlink")).
					writeAttribute("id","transferDetails").
					writeAttribute("ns1:href",conf.getString("application.url")+"/rest/transfers/"+jobId+"/results/details").endEntity();
			xw.endEntity();
			xw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}
		return writ.toString();
	}

	/**
	 * Returns the transfer results details
	 * @param jobId Job identifier
	 * @return transfer results details
	 */
	@GET @Path("{jobid}/results/details")
	@Produces(MediaType.TEXT_XML)
	public String getTransferResultsDetails(@PathParam("jobid") String jobId) {
		JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));
		
		if(null == job)
			throw new NotFoundException("The job "+jobId+" is not found.");

		StringWriter writ = new StringWriter();
		SimpleXmlWriter xw = new SimpleXmlWriter(writ);
		try {
			xw.writeEntity("transfer");

			xw.writeEntity("target").writeText(job.getTarget()).endEntity();
			switch(job.getDirection()){
				case PULLFROMVOSPACE:
					xw.writeEntity("direction").writeText(conf.getString("transfers.direction.pull_from_vospace")).endEntity();
					break;
				case PULLTOVOSPACE:
					xw.writeEntity("direction").writeText(conf.getString("transfers.direction.pull_to_vospace")).endEntity();
					break;
				case PUSHFROMVOSPACE:
					xw.writeEntity("direction").writeText(conf.getString("transfers.direction.push_from_vospace")).endEntity();
					break;
				case PUSHTOVOSPACE:
					xw.writeEntity("direction").writeText(conf.getString("transfers.direction.push_to_vospace")).endEntity();
					break;
			}
			for(String view: job.getViews())
				xw.writeEntity("view").writeText(view).endEntity();

			for(Iterator<String> it = job.getProtocols().keySet().iterator(); it.hasNext();){
				String protocol = it.next();
				String protocolEndpoint = job.getProtocols().get(protocol);
				xw.writeEntity("protocol").writeAttribute("uri", protocol);
				if(null != protocolEndpoint && !protocolEndpoint.isEmpty()){
					xw.writeEntity("endpoint").writeText(protocolEndpoint).endEntity();
				}
				xw.endEntity();
			}

			xw.endEntity();
			xw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}
		return writ.toString();
	}

	/**
	 * Returns the transfer error representation
	 * @param jobId Job identifier
	 * @return transfer representation
	 */
	@GET @Path("{jobid}/phase")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTransferPhase(@PathParam("jobid") String jobId) {
		JobDescription job = JobsProcessor.getJob(UUID.fromString(jobId));
		
		if(null == job){
			logger.error("Job "+jobId+" isn't found.");
			throw new NotFoundException("The job is not found.");
		}

		logger.debug("Job "+jobId+" state: "+job.getState());
		
		return job.getState().toString();
	}

	/**
	 * Returns the transfers queue
	 * @return transfer representation
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getTransfersQueue() {
		StringBuffer resultBuf = new StringBuffer();
		resultBuf.append("id, state, direction, starttime, endtime, path\n");
		
		Statement stmt = null;
		java.sql.Connection con = null;
		ResultSet resSet = null;
		
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			stmt = con.createStatement();

			String whereClause = "";
			if(null != request.getAttribute("username"))
				whereClause = " where login = '"+request.getAttribute("username")+"'";
				
			resSet = stmt.executeQuery("select id, state, direction, starttime, endtime, target from jobs "+whereClause+";");
			
			while(resSet.next()) {
				resultBuf.append(resSet.getString(1)+", ");
				resultBuf.append(resSet.getString(2)+", ");
				resultBuf.append(resSet.getString(3)+", ");
				resultBuf.append((null != resSet.getTimestamp(4)?resSet.getTimestamp(4):"")+", ");
				resultBuf.append((null != resSet.getTimestamp(5)?resSet.getTimestamp(5):"")+", ");
				resultBuf.append(resSet.getString(6));
				resultBuf.append("\n");
	    	}
		} catch(Exception ex) {
			logger.debug("Error adding a new job to the database");
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} finally {
			try { resSet.close(); } catch(Exception e) { }
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		return resultBuf.toString();
	}
	
	
}
