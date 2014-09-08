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
import org.jdom.JDOMException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import edu.caltech.vao.vospace.protocol.ProtocolHandler;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.rest.JobDescription.DIRECTION;

/**
 * The JobsProcessor class creates a separate thread, waiting for new jobs to appear in the queue.
 * If a new client-initialized job appears, the process sets the needed fiends of the job and sets the finished status.
 * @author Dmitry Mishin
 */
public class ExecutorImpl extends JobsProcessor implements HttpConnectionPoolProvider {

	private static final long serialVersionUID = -9221962022096928143L;
	private static Logger logger = Logger.getLogger(ExecutorImpl.class);
    private static ThreadSafeClientConnManager cm;
    private ExecutorService service;
	private Thread jobsThread = null;
	private static Vector<Future> workers;
    
	public ExecutorImpl() {
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
		
		ConnectionFactory factory = new ConnectionFactory();
		Connection conn = null;
		Channel channel = null;

		factory.setHost(conf.getString("rabbitmq.host"));
		factory.setVirtualHost(conf.getString("rabbitmq.vhost"));

		try {
			conn = factory.newConnection();
			channel = conn.createChannel();

			channel.exchangeDeclare(conf.getString("transfers.exchange.submited"), "topic", true);

			channel.queueDeclare(conf.getString("transfers.queue.submited.server_initialised"), true, false, false, null);
			
			channel.queueBind(conf.getString("transfers.queue.submited.server_initialised"), conf.getString("transfers.exchange.submited"), "direction."+JobDescription.DIRECTION.PUSHFROMVOSPACE);
			channel.queueBind(conf.getString("transfers.queue.submited.server_initialised"), conf.getString("transfers.exchange.submited"), "direction."+JobDescription.DIRECTION.PULLTOVOSPACE);
			channel.queueBind(conf.getString("transfers.queue.submited.server_initialised"), conf.getString("transfers.exchange.submited"), "direction."+JobDescription.DIRECTION.LOCAL);

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(conf.getString("transfers.queue.submited.server_initialised"), false, consumer);

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
			    	logger.debug("Waiting for a job");
			    	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			    	
			    	// Job JSON notation
				    JobDescription job = (new ObjectMapper()).readValue(delivery.getBody(), 0, delivery.getBody().length, JobDescription.class);
				    
			    	logger.debug("There's a submited job! "+job.getId());

	    			TransferThread thread = new TransferThread(job, this);
	    			thread.setName(job.getId());
	    			
	    			Future future = service.submit(thread);
	    			
	    			workers.add(future);
	    			channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			}
	    } catch(IOException ex) {
    		logger.error("Error retrieving the task from the queue: "+ex.getMessage());
    		ex.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e);
		} finally {
			try {
				if(null != channel)
					channel.close();
				if(null != conn)
					conn.close();
				service.awaitTermination(3, TimeUnit.SECONDS);
				logger.debug("Exiting the jobs process.");
			} catch (IOException e) {
				logger.error(e);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
	}

	public static void submitJob(String login, JobDescription job) {
		try {
			JobsProcessor.submitJob(login, job);
			if(job.getDirection() == DIRECTION.PUSHFROMVOSPACE || job.getDirection() == DIRECTION.PULLTOVOSPACE || job.getDirection() == DIRECTION.LOCAL) {
				ConnectionFactory factory = new ConnectionFactory();
				factory.setVirtualHost(conf.getString("rabbitmq.vhost"));
				factory.setHost(conf.getString("rabbitmq.host"));
				Connection conn = factory.newConnection();
				
				Channel channel = conn.createChannel();
				channel.exchangeDeclare(conf.getString("transfers.exchange.submited"), "topic", true);
		
				byte[] jobSer = (new ObjectMapper()).writeValueAsBytes(job);
				channel.basicPublish(conf.getString("transfers.exchange.submited"), "direction."+job.getDirection(), null, jobSer);
				
				channel.close();
				conn.close();
			}
		} catch (IOException e) {
			logger.error(e);
			throw new InternalServerErrorException(e);
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
