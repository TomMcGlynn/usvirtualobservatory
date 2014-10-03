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

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.usvao.descriptors.QueryDescription;
import org.usvao.exceptions.InternalServerErrorException;
import org.usvao.helpers.ResourceHelper;
import org.usvao.helpers.uploadhelper.UploadTable;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * This is a servlet where thread keeps on runing to check submission of 
 * the upload table.
 * @author deoyani nandrekar-heinis
 */
public class UploadWorker extends HttpServlet implements Runnable{
    
    private ExecutorService service;
    private Thread uploadThread = null;
    //private static Vector<Future> jobsQueue;
    public  static HashMap<String,Future> uploadMap;
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
        uploadMap = new HashMap<String, Future>();
        uploadThread = new Thread(this);
	uploadThread.setDaemon(true);
        uploadThread.start();
   }

   @Override
   public void destroy() {
          
	uploadThread.interrupt();
   }
   
    @Override
    public void run() {
            Connection conn = null;
            Channel chan = null;            
            String uploadid = null;
            // Initialize the queue connection
	    ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(conf.getString("rabbitmq.host"));
           
            try {
		conn = factory.newConnection();
		chan = conn.createChannel();
                
                chan.queueDeclare(conf.getString("rabbitmq.queue.table.submitted"), false, false, false, null);
                System.out.println(" [*] Waiting for upload job. ");
                QueueingConsumer consumer = new QueueingConsumer(chan);
                
                chan.basicConsume(conf.getString("rabbitmq.queue.table.submitted"), true, consumer);
                while (!uploadThread.isInterrupted()) {                                   
                    
                for(Iterator<String> it = uploadMap.keySet().iterator(); it.hasNext();){                    
                    String jobinQueue = it.next();                        
                    if(uploadMap.get(jobinQueue).isDone()){
                        it.remove();
                        uploadMap.remove(jobinQueue);                        
                        logger.debug("Job: "+jobinQueue+" is removed from the worker.");
                        System.out.println("***************Job: "+jobinQueue+" is removed from the Map."+":: size::"+uploadMap.size());
                    }
                 }                

                 if(uploadMap.size() >= conf.getInt("async.jobs")) {
                    System.out.println("Waiting for a upload pool, size: "+uploadMap.size());
                    logger.debug("Waiting for a upload pool, size: "+uploadMap.size());
                    synchronized(UploadTable.class) {
                        UploadTable.class.wait();
		    }
                    logger.debug("End waiting for a upload pool, size: "+uploadMap.size());
                    System.out.println("End waiting for a upload pool, size: "+uploadMap.size());
                 } else {
                    
                    logger.debug("Waiting for a upload job");           
                    
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                                    
                    byte[] messagesBytes = delivery.getBody();
                    String[] messages = (String[]) toObject(messagesBytes);                    
                    System.out.println("1:"+messages[0]+":2:"+messages[1]+":3:"+messages[2]+":4:"+messages[3]);
                   
                    UploadTable up = new UploadTable(messages[0],messages[1], messages[2], messages[3], messages[4]);
                    uploadid = messages[4];
                    Thread  thread = new Thread(up);
                    thread.setName(uploadid);
                    Future future = service.submit(thread);                      
                    uploadMap.put(uploadid, future);     
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
    
//   private QueryDescription getJobData(String jobid){
//      java.sql.Connection jobsConnection = null;
//      Statement stmt = null;
//      java.sql.ResultSet rs=null;
//       QueryDescription qDesc = new QueryDescription();
//       try{           
//           jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");   
//           stmt = jobsConnection.createStatement();
//           rs = stmt.executeQuery("Select * from tapjobstable where jobid = '"+jobid+"'");
//          
//           while(rs.next()){
//            qDesc.setJobId(rs.getString("jobid"));            
//            qDesc.setUploadparam(rs.getString("uploadparam"));
//            System.out.println("check upload param :"+rs.getString("uploadparam"));            
//           }
//           if(qDesc.getUploadparam().equals("") && qDesc.getUploadparam() == null)
//               qDesc.setRequest("DONT PROCESS");
//           return qDesc;
//            
//        }catch (SQLException sexp){                        
//            throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in get job function:"+sexp.getMessage()));            
//        }catch(Exception exp){            
//             throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in insertDatabase function:"+exp.getMessage()));
//        }finally{
//           try{ stmt.close();}catch(Exception e){}
//           try{ jobsConnection.close();}catch(Exception exp){}
//       }
//    }
}
