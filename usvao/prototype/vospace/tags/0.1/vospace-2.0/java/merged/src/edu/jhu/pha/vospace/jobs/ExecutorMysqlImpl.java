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
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import edu.caltech.vao.vospace.protocol.ProtocolHandler;
import edu.jhu.pha.vospace.rest.JobDescription;

/**
 * The JobsProcessor class creates a separate thread, waiting for new jobs to appear in the queue.
 * If a new client-initialized job appears, the process sets the needed fiends of the job and sets the finished status.
 * @author Dmitry Mishin
 */
public class ExecutorMysqlImpl extends JobsProcessor implements HttpConnectionPoolProvider {

	private static final long serialVersionUID = -7347829485565922282L;
	private static Logger logger = Logger.getLogger(ExecutorMysqlImpl.class);
    private static ThreadSafeClientConnManager cm;
    private ExecutorService service;
	private Thread jobsThread = null;
	private static Vector<Future> workers;
    
	public ExecutorMysqlImpl() {
		cm = new ThreadSafeClientConnManager();
        cm.setMaxTotal(100);
        
        service = Executors.newCachedThreadPool();
        workers = new Vector<Future>();
        
		jobsThread = new Thread(this);
		jobsThread.setDaemon(true);
		jobsThread.start();
	}
	
	public void destroy() {
		logger.debug("INTERRUPTING!");
		jobsThread.interrupt();
	}
	
	/* (non-Javadoc)
	 * @see edu.jhu.pha.vospace.JobsProcessor#getProtocolHandler(java.lang.String, edu.jhu.pha.vospace.TransferThread)
	 */
	@Override
	public ProtocolHandler getProtocolHandler(String protocolUri, TransferThread thread) {
		if(null != protocolHandlers.get(protocolUri)){
			try {
				ProtocolHandler handlerInst = (ProtocolHandler)protocolHandlers.get(protocolUri).getConstructor(HttpConnectionPoolProvider.class).newInstance(this);
				return handlerInst;
			} catch(InvocationTargetException e) {
				logger.error(e);
			} catch (IllegalArgumentException e) {
				logger.error(e);
			} catch (SecurityException e) {
				logger.error(e);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			} catch (NoSuchMethodException e) {
				logger.error(e);
			}
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			// The main cycle to process the jobs
			while (!jobsThread.isInterrupted()) {
				
				for(Iterator<Future> it = workers.iterator(); it.hasNext();){
					Future next = it.next();
					if(next.isDone()){
						it.remove();
						logger.debug("Job "+next+" is removed from the workers.");
					}
				}
				
				if(workers.size() >= jobsPoolSize) {
					logger.debug("Waiting for a jobs pool, size: "+workers.size());
					synchronized(JobsProcessor.class) {
						JobsProcessor.class.wait();
					}
					logger.debug("End waiting for a jobs pool, size: "+workers.size());
				} else {
			    	//logger.debug("Looking for a job");

			    	JobDescription job = null;
			    	
					Connection con = null;
					PreparedStatement stmt = null;
					ResultSet resSet = null;
					
			    	try {
			    	
						con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
						stmt = con.prepareStatement("select id, json_notation from jobs where state = '"+JobDescription.STATE.PENDING.toString()+"' and (DIRECTION != 'PULLFROMVOSPACE' and DIRECTION != 'PUSHTOVOSPACE') order by starttime limit 1;");
						resSet = stmt.executeQuery();
	
						if(resSet.next()){
						    if(con.createStatement().executeUpdate("update jobs set state = '"+JobDescription.STATE.RUN.toString()+"' where id = '"+resSet.getString("id")+"' and state = '"+JobDescription.STATE.PENDING.toString()+"';") > 0) {
						    	logger.debug("Updated the job to Running, running the job.");
								String jobString = resSet.getString("json_notation");
							    job = (new ObjectMapper()).readValue(jobString.getBytes(), 0, jobString.length(), JobDescription.class);
						    } else {
						    	logger.debug("Update returned 0");
						    }
						}
			    	} catch(SQLException ex){
			    		logger.error("Error retrieving the task from the db: "+ex.getMessage());
			    		ex.printStackTrace();
			    	} finally {
						try { resSet.close();} catch(Exception e) { }
						try { stmt.close(); } catch(Exception e) { }
						try { con.close(); } catch(Exception e) { }
			    	}
			    	
			    	if(null != job){
				    	logger.debug("There's a submited job! "+job.getId());
				    	
		    			TransferThread thread = new TransferThread(job, this);
		    			thread.setName(job.getId());
		    			
		    			Future future = service.submit(thread);
		    			
		    			workers.add(future);
			    	} else {
						Thread.sleep(1000*10);
					}
				}
			}
	    } catch(IOException ex) {
    		logger.error("Error retrieving the task from the queue: "+ex.getMessage());
    		ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				service.awaitTermination(3, TimeUnit.SECONDS);
				logger.debug("Exiting the jobs process.");
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
	}


	
	/* (non-Javadoc)
	 * @see edu.jhu.pha.vospace.JobsProcessor#getClientConnManager()
	 */
	@Override
	public ClientConnectionManager getClientConnManager() {
		return cm;
	}
	
}
