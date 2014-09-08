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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import edu.caltech.vao.vospace.protocol.ProtocolHandler;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

public abstract class JobsProcessor implements Runnable  {

	private static final long serialVersionUID = 4011217154603941869L;
	private static Logger logger = Logger.getLogger(JobsProcessor.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	static Configuration conf = SettingsServlet.getConfig();;
	Hashtable<String, Class> protocolHandlers;
	int jobsPoolSize;

	public JobsProcessor() {
        jobsPoolSize = conf.getInt("maxtransfers");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
		initProtocolHandlers();
	}
		
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
	
	public abstract void destroy();
	
	public abstract ProtocolHandler getProtocolHandler(String protocolUri, TransferThread thread);

	public abstract ClientConnectionManager getClientConnManager();

	public static void modifyJobState(JobDescription job, STATE state) {
		modifyJobState(job, state, null);
	}

	public static void modifyJobState(JobDescription job, STATE state, String note) {

		if(null == job.getEndTime() && (state == STATE.COMPLETED || state == STATE.ERROR)){
			job.setEndTime(new Date());
		}

		job.setState(state);
		
		// Update the database record for the job
		PreparedStatement stmt = null;
		java.sql.Connection con = null;
		
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			stmt = con.prepareStatement("update jobs set endtime = ?, state = ?, json_notation = ?, note = ? where id = ?;");

			stmt.setString(1, (null == job.getEndTime())?null:dateFormat.format(job.getEndTime().getTime()));
			stmt.setString(2, job.getState().toString());
			stmt.setBytes(3, (new ObjectMapper()).writeValueAsBytes(job));
			stmt.setString(4, (null == note)?"":note);
			stmt.setString(5, job.getId());
			
			stmt.execute();
		} catch(SQLException ex) {
			ex.printStackTrace();
			logger.error("Error modifying the job record in database.");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error serializing the job.");
			job.setState(STATE.ERROR);
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		logger.debug("Job "+job.getId()+" is modified. "+job.getState());
	}

	/**
	 * Returns the JobDescription object serialized from the database record
	 * @param jobId The identifier of a job
	 * @return The job java object
	 */
	public static JobDescription getJob(UUID jobId) {
		JobDescription returnJob = null;
		
		PreparedStatement stmt = null;
		java.sql.Connection con = null;;
		ResultSet resSet = null;
		
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			stmt = con.prepareStatement("select json_notation, note from jobs where id = ?;");

			stmt.setString(1, jobId.toString());
			
			resSet = stmt.executeQuery();
			
			if(resSet.next()) {
				byte[] jobJsonNotation = resSet.getBytes(1);
				returnJob = (new ObjectMapper()).readValue(jobJsonNotation, 0, jobJsonNotation.length, JobDescription.class);
				returnJob.setNote(resSet.getString("note"));
			}
		} catch(Exception ex) {
			logger.error(ex);
			throw new InternalServerErrorException(ex);
		} finally {
			try { resSet.close(); } catch(Exception e) { }
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		return returnJob;
	}

	/**
	 * Adds the new job to the SQL database
	 * @param job The job description object
	 */
	public static void submitJob(String login, JobDescription job) {
		java.sql.Connection con = null;
		PreparedStatement stmt = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			byte[] jobSer = (new ObjectMapper()).writeValueAsBytes(job);
			
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			
			stmt = con.prepareStatement("insert into jobs (id,login,starttime,state,direction,target,json_notation) values (?,?,?,?,?,?,?);");

			stmt.setString(1, job.getId());
			stmt.setString(2, login);
			stmt.setString(3, dateFormat.format(job.getStartTime().getTime()));
			stmt.setString(4, job.getState().toString());
			stmt.setString(5, job.getDirection().toString());
			stmt.setString(6, job.getTarget());
			stmt.setBytes(7, jobSer);
			
			stmt.execute();
			
		} catch(Exception ex) {
			logger.debug("Error adding a new job to the database");
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
	}

}
