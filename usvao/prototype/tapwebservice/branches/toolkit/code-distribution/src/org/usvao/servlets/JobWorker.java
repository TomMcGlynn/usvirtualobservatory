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
package org.usvao.servlets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.usvao.descriptors.QueryDescription;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.exceptions.InternalServerErrorException;
import org.usvao.helpers.AsyncQExecuter;
import org.usvao.helpers.ResourceHelper;


/**
 *
 * @author deoyani nandrekar-heinis
 */
public class JobWorker extends HttpServlet implements Runnable{
    
    private ExecutorService service;
    private Thread jobsThread = null;
    //private static Vector<Future> jobsQueue;
    public  static HashMap<String,Future> jobsMap;
    private Configuration conf;    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    private static Logger logger = Logger.getLogger(JobWorker.class);        
           
    @Override
    public void init() {
    
        ServletContext context = this.getServletContext();
        conf = (Configuration)context.getAttribute("configuration");
	dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        service = Executors.newCachedThreadPool();
        //jobsQueue = new Vector<Future>();
        jobsMap = new HashMap<String, Future>();
        jobsThread = new Thread(this);
	jobsThread.setDaemon(true);
        jobsThread.start();
   }

   @Override
   public void destroy() {
          
	jobsThread.interrupt();
   }
   
    @Override
    public void run() {
            Connection conn = null;
            Channel chan = null;            
            String jobId = null;
            // Initialize the queue connection
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(conf.getString("rabbitmq.host"));
           
            
            try {
		conn = factory.newConnection();
		chan = conn.createChannel();
                
                chan.queueDeclare(conf.getString("rabbitmq.queue.submitted"), false, false, false, null);
                System.out.println(" [*] Waiting for Jobs. ");
                QueueingConsumer consumer = new QueueingConsumer(chan);
                
                chan.basicConsume(conf.getString("rabbitmq.queue.submitted"), true, consumer);
                while (!jobsThread.isInterrupted()) {                                   
                    
                for(Iterator<String> it = jobsMap.keySet().iterator(); it.hasNext();){                    
                    String jobinQueue = it.next();                        
                    if(jobsMap.get(jobinQueue).isDone()){
                        it.remove();
                        jobsMap.remove(jobinQueue);                        
                        logger.debug("Job: "+jobinQueue+" is removed from the workers.");
                        System.out.println("***************Job: "+jobinQueue+" is removed from the Map."+":: size::"+jobsMap.size());
                    }
                 }                

                 if(jobsMap.size() >= conf.getInt("async.jobs")) {
                     System.out.println("Waiting for a jobs pool, size: "+jobsMap.size());
                    logger.debug("Waiting for a jobs pool, size: "+jobsMap.size());
                        synchronized(AsyncQExecuter.class) {
                                AsyncQExecuter.class.wait();      }
                    logger.debug("End waiting for a jobs pool, size: "+jobsMap.size());
                    System.out.println("End waiting for a jobs pool, size: "+jobsMap.size());
                 } else {
                    
                    logger.debug("Waiting for a job");           
                    
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                                    
                    byte[] messagesBytes = delivery.getBody();
                    String[] messages = (String[]) toObject(messagesBytes);
                    
                    //String iQuery = messages[0];
                    jobId = messages[0];
                    //String rFormat = messages[2];
                    //int maxrec =Integer.parseInt(messages[3]);
                  
                    QueryDescription qDesc = this.getJobData(jobId);
                    qDesc.setDuration(conf.getInt("query.maxexecution"));
                    qDesc.setResultsDir(conf.getString("results.datadir"));
                    if(!qDesc.getRequest().equals("DONT PROCESS")){
                        
                    AsyncQExecuter asyncExecuter = new AsyncQExecuter(qDesc);
//                    AsyncQExecuter asyncExecuter = new AsyncQExecuter(jobId,iQuery,rFormat,
//                            dirPath,conf.getInt("query.maxexecution"), maxrec);                    
                    Thread  thread = new Thread(asyncExecuter);
                    thread.setName(jobId);
                    Future future = service.submit(thread);                      
                    jobsMap.put(jobId, future);     
                    }
                    //queryExecution(Query, jobId, rFormat);                                     
                 }                   
               }
            }catch(java.lang.InterruptedException iExp){
                System.out.println("Exception in JobWorker because of problem in delivery :"+iExp.getMessage());                
            }catch(java.io.IOException ioExp){
                System.out.println("Exception in JobWorker because of connection to RabitMQ service :"+ioExp.getMessage());                
            }finally{
                try{ chan.close();}catch(Exception e){}
                try{ conn.close();}catch(Exception e){}
            }
   }       

    private static Object toObject(byte[] bytes){
        Object object = null;
        try{
            object = new java.io.ObjectInputStream(new
            java.io.ByteArrayInputStream(bytes)).readObject();
            return object;
        }catch(java.io.IOException ioe){
             return null;
        }catch(java.lang.ClassNotFoundException cnfe){
            System.out.println("Exception here:"+cnfe.getMessage());
            return null;
        }        
    }
    
   private QueryDescription getJobData(String jobid){
      java.sql.Connection jobsConnection = null;
      Statement stmt = null;
      java.sql.ResultSet rs=null;
       QueryDescription qDesc = new QueryDescription();
       try{           
           jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");   
           stmt = jobsConnection.createStatement();
           rs = stmt.executeQuery("Select * from "+StaticDescriptors.tapSchema+"."+"tapjobstable where jobid = '"+jobid+"'");
          
           while(rs.next()){
            qDesc.setJobId(rs.getString("jobid"));
            qDesc.setADQLquery(rs.getString("adql"));
            qDesc.setDestruction(new java.sql.Timestamp(rs.getLong("destruction")).toString());
            qDesc.setFormat(rs.getString("resultFormat"));
            qDesc.setLang(rs.getString("lang"));
            qDesc.setRequest(rs.getString("request"));
            qDesc.setQuery(rs.getString("query"));
            qDesc.setMaxrec(rs.getInt("maxrec")); 
            qDesc.setUploadparam(rs.getString("uploadparam"));
            System.out.println("check upload param :"+rs.getString("uploadparam"));
            
           }
           if(qDesc.getJobId().equals("") && qDesc.getJobId() == null)
               qDesc.setRequest("DONT PROCESS");
           return qDesc;
            
        }catch (SQLException sexp){            
            
            throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in get job function:"+sexp.getMessage()));            
        }catch(Exception exp){            
             throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in insertDatabase function:"+exp.getMessage()));
        }finally{
           try{ stmt.close();}catch(Exception e){}
           try{ jobsConnection.close();}catch(Exception exp){}
       }
    }
    
    
//      private void queryExecution(String iQuery, String jobId, String resultFormat){
//      java.sql.Connection jobsConnection = null;
//      Statement stmt = null;
//        try{
//            //System.out.println("Check here:"+"select jobstatus,duration from tapjobstable where jobid ='"+jobId+"'");
//            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
//            stmt = jobsConnection.createStatement();
//            ResultSet rs = stmt.executeQuery("select jobstatus,duration from tapjobstable where jobid ='"+jobId+"'");
//            rs.next();
//            if(rs.getString("jobstatus").equalsIgnoreCase("PENDING")){                   
//                 String dirPath = conf.getString("results.datadir");
//                 JobsExecuter jobExecuter = new JobsExecuter(jobId,iQuery,resultFormat,dirPath,rs.getLong("duration"), conf.getInt("results.servicemaxrec"));                    
//                 Thread  thread = new Thread(jobExecuter);
//                 thread.setName(jobId);
//                 Future future = service.submit(thread);                      
//                 jobsMap.put(jobId, future);              
//            }           
//        }catch(SQLException sexp){
//              System.out.println("Exception jobs data datbase update."+sexp.getMessage());              
//        }catch(Exception exp){
//          System.out.println("Error updating the job details in worker"+exp.getMessage());          
//        }
//        finally{
//          try{ stmt.close();} catch(Exception exp){}
//          try{ jobsConnection.close();}catch(Exception exp){}
//        }
//    }
}
