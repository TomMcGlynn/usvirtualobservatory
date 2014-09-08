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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import edu.jhu.pha.vospace.DbPoolServlet;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.DbPoolServlet.SqlWorker;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.node.Node;
import edu.jhu.pha.vospace.protocol.ProtocolHandler;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

public abstract class JobsProcessor implements Runnable  {

	private static final long serialVersionUID = 4011217154603941869L;
	private static final Logger logger = Logger.getLogger(JobsProcessor.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static Configuration conf = SettingsServlet.getConfig();
	Hashtable<String, Class> protocolHandlers;
	int jobsPoolSize;

	public JobsProcessor() {
        jobsPoolSize = conf.getInt("maxtransfers");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
		initProtocolHandlers();
	}
		
	public static Class getImplClass() {
		Class jobsClass;
		try {
			jobsClass = Class.forName(conf.getString("jobsprocessor.class"));
			return jobsClass;
		} catch (ClassNotFoundException e) {
			logger.error("Error in configuration: can't find the jobs processor class");
			return null;
		}
	}
	
	/**
	 * Returns the JobDescription object serialized from the database record
	 * @param jobId The identifier of a job
	 * @return The job java object
	 */
	public static JobDescription getJob(final UUID jobId) {
        return DbPoolServlet.goSql("GetJob request",
        		"select json_notation, note from jobs where id = ?",
                new SqlWorker<JobDescription>() {
                    @Override
                    public JobDescription go(Connection conn, PreparedStatement stmt) throws SQLException {
                		JobDescription returnJob = null;
                        stmt.setString(1, jobId.toString());
                        ResultSet rs = stmt.executeQuery();
            			if(rs.next()) {
            				byte[] jobJsonNotation = rs.getBytes(1);
            				try {
	            				returnJob = (new ObjectMapper()).readValue(jobJsonNotation, 0, jobJsonNotation.length, JobDescription.class);
	            				returnJob.setNote(rs.getString("note"));
            				} catch(JsonMappingException ex) { // Shouldn't happen
            					throw new InternalServerErrorException(ex.getMessage());
            				} catch (JsonParseException ex) {
            					throw new InternalServerErrorException(ex.getMessage());
							} catch (IOException ex) {
            					throw new InternalServerErrorException(ex.getMessage());
							}
            			}
            			return returnJob;
                    }
                }
        );
	}
	
	public static void modifyJobState(JobDescription job, STATE state) {
		modifyJobState(job, state, null);
	}

	public static void modifyJobState(final JobDescription job, final STATE state, final String note) {

		if(null == job.getEndTime() && (state == STATE.COMPLETED || state == STATE.ERROR)){
			job.setEndTime(new Date());
		}

		job.setState(state);
		final byte[] jobBytes;
		try {
			jobBytes = (new ObjectMapper()).writeValueAsBytes(job);
		} catch(Exception ex) {
			throw new InternalServerErrorException(ex.getMessage());
		}
		
        DbPoolServlet.goSql("Modify job",
        		"update jobs set endtime = ?, state = ?, json_notation = ?, note = ? where id = ?",
                new SqlWorker<Integer>() {
                    @Override
                    public Integer go(Connection conn, PreparedStatement stmt) throws SQLException {
            			stmt.setString(1, (null == job.getEndTime())?null:dateFormat.format(job.getEndTime().getTime()));
            			stmt.setString(2, job.getState().toString());
            			stmt.setBytes(3, jobBytes);
            			stmt.setString(4, (null == note)?"":note);
            			stmt.setString(5, job.getId());
            			return stmt.executeUpdate();
                    }
                }
        );
		logger.debug("Job "+job.getId()+" is modified. "+job.getState());
	}
	
	/**
	 * Adds the new job to the SQL database
	 * @param job The job description object
	 */
	public static void submitJob(final String login, final JobDescription job) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		final byte[] jobSer;
		try {
			jobSer = (new ObjectMapper()).writeValueAsBytes(job);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex.getMessage());
		}
		
        DbPoolServlet.goSql("Submit job",
        		"insert into jobs (id,user_id,starttime,state,direction,target,json_notation) select ?, user_id, ?,?,?,?,? from user_identities WHERE identity = ?",
                new SqlWorker<Integer>() {
                    @Override
                    public Integer go(Connection conn, PreparedStatement stmt) throws SQLException {
            			stmt.setString(1, job.getId());
            			stmt.setString(2, dateFormat.format(job.getStartTime().getTime()));
            			stmt.setString(3, job.getState().toString());
            			stmt.setString(4, job.getDirection().toString());
            			stmt.setString(5, job.getTarget().toString());
            			stmt.setBytes(6, jobSer);
            			stmt.setString(7, login);
            			return stmt.executeUpdate();
                    }
                }
        );
	}

	public abstract void destroy();

	public abstract ProtocolHandler getProtocolHandler(String protocolUri, TransferThread thread);

	
	private void initProtocolHandlers(){
		String confProtocolsPrefix = "transfers.protocol.handler";
		protocolHandlers = new Hashtable<String, Class>();
		for(Iterator<String> it = conf.getKeys(confProtocolsPrefix); it.hasNext();){
			String protocolHandlerKey = it.next();
			String protocolName = protocolHandlerKey.substring(confProtocolsPrefix.length()+1);
			try {
				Class handlerClass = Class.forName(conf.getString(protocolHandlerKey));
				protocolHandlers.put(conf.getString("transfers.protocol."+protocolName), handlerClass);
			} catch(ClassNotFoundException ex) {
				logger.error("Unable to initialise the protocol handler "+protocolName+": Class "+conf.getString(protocolHandlerKey)+" not found.");
			}
		}
	}

	/**
	 * Start the jobs queue processing
	 */
	public abstract void start();

}
