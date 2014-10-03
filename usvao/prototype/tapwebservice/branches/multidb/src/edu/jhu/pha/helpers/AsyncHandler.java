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
package edu.jhu.pha.helpers;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import edu.jhu.pha.descriptors.QueryDescription;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import edu.jhu.pha.descriptors.EnumDescriptors.uwsJobElements;
import edu.jhu.pha.descriptors.StaticMessages;
import edu.jhu.pha.exceptions.InternalServerErrorException;
import edu.jhu.pha.helpers.resourcehelper.UWSResources;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import edu.jhu.pha.descriptors.uwsdesc.UWSJobDescription;
import edu.jhu.pha.exceptions.PermissionDeniedException;
import java.io.IOException;


/**
 * This handles the valid query from user and submits in queue to be taken care in RabbitQ.
 * It also updates tapjobstable in database.
 * @author deoyani nandrekar-heinis
 */
public class AsyncHandler {

    private static Logger logger = Logger.getLogger(AsyncHandler.class);    

    public AsyncHandler(){}  
   
    /**
     * 
     * @param jobid
     * @param conf 
     */
    public void queueJob(String jobid,Configuration conf)
    {       
        if(!this.isPending(jobid))
            throw new PermissionDeniedException(ResourceHelper.getVotableError("This job is not in pending state. Check Job status"));
        Connection conn = null;
        Channel chan  = null;
        try{
        
            ConnectionFactory factory = new ConnectionFactory();        
            factory.setHost(conf.getString("rabbitmq.host"));
            conn = factory.newConnection();        
            chan = conn.createChannel();        
            chan.queueDeclare(conf.getString("rabbitmq.queue.submitted"), false, false, false, null);        
            String[] messages = new String[1];
            messages[0] = jobid;            
            this.updateJobsdb(jobid, StaticMessages.msgQueue);     
            chan.basicPublish("",conf.getString("rabbitmq.queue.submitted"), null, getBytes(messages));
        }
        catch(IOException iexp){
            this.updateJobsdb(jobid,StaticMessages.msgPend);     
            System.out.println("Exception in the submitJob ::"+iexp.getMessage());            
            logger.error(iexp.getMessage());
            throw new InternalServerErrorException(ResourceHelper.getVotableError("Job can not be submitted in queue."+iexp.getMessage()));            
        }
        
        finally{            
            try{
                chan.close();
                conn.close();
            } catch (Exception exp) {
                System.out.println("Problem closing connections:" + exp.getMessage());
            }
        }
    }
    
 
    /**
     * 
     * @param obj
     * @return
     * @throws java.io.IOException 
     */
    private static byte[] getBytes(Object obj) throws java.io.IOException{
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
      oos.flush();
      oos.close();
      bos.close();
      byte [] data = bos.toByteArray();
      return data;
   }

    /**
     * Inserts the job related information in tapjobstable
     */
    public void submitJob(QueryDescription qdesc, Configuration conf){
      java.sql.Connection jobsConnection = null;
      PreparedStatement pstmt = null;
       try{           
           jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");   
           //For setting date format
           String DatePattern = "yyyy-MM-dd HH:mm:ss.SSS";            
           java.text.DateFormat df = new java.text.SimpleDateFormat(DatePattern);           
           java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());   
        
           Calendar cal = Calendar.getInstance();
           cal.setTime(sqlDate);
           cal.add(Calendar.DAY_OF_MONTH, 30);
           java.sql.Timestamp  sqlDateDestruction =null;
        
           System.out.println("destruction:"+qdesc.getDestruction());
           System.out.println("cal destruction:"+cal.getTime());
           
           if(qdesc.getDestruction() != null && !qdesc.getDestruction().equals(""))
            sqlDateDestruction = new java.sql.Timestamp(df.parse(qdesc.getDestruction()).getTime());            
           else    
            sqlDateDestruction = new java.sql.Timestamp(cal.getTime().getTime());
        
            pstmt = jobsConnection.prepareStatement("insert into" 
                   + " tapjobstable ( jobid,jobstatus,starttime,duration,destruction,lang,query,adql,resultFormat,request,maxrec)"
                   + " values(?,?,?,?,?,?,?,?,?,?,?)");
            pstmt.setString(1,qdesc.getJobId());
            pstmt.setString(2, StaticMessages.msgPend);
            pstmt.setLong(3, 0); //sqlDate.getTime());
            pstmt.setLong(4, conf.getInt("query.maxexecution"));
            pstmt.setLong(5, sqlDateDestruction.getTime());
            pstmt.setString(6, qdesc.getLang()) ;
            pstmt.setString(7, qdesc.getQuery());       
            pstmt.setString(8, qdesc.getAdqlQuery());
            pstmt.setString(9, qdesc.getFormat());
            pstmt.setString(10,qdesc.getRequest());
            pstmt.setInt(11, qdesc.getMaxrec());
            pstmt.executeUpdate();            
            
        }catch (SQLException sexp){            
            throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in insertDatabase function:"+sexp.getMessage()));            
        }catch(Exception exp){            
            throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in insertDatabase function:"+exp.getMessage()));
        }finally{
           try{ pstmt.close();}catch(Exception e){}
           try{ jobsConnection.close();}catch(Exception exp){}              
       }
    }    
    
    /**
     * 
     * @param jobid
     * @param jobstatus 
     */
    private void updateJobsdb(String jobid, String jobstatus){
        
      java.sql.Connection jobsConnection = null;
      PreparedStatement pstmt = null;
       try{           
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");              
            pstmt = jobsConnection.prepareStatement("update tapjobstable set jobstatus = ? where jobid = ?");                   
            pstmt.setString(1, jobstatus);
            pstmt.setString(2,jobid);            
            pstmt.executeUpdate();                        
        }catch (SQLException sexp){            
            throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in insertDatabase function:"+sexp.getMessage()));            
        }catch(Exception exp){            
             throw new InternalServerErrorException(ResourceHelper.getVotableError("Exception in insertDatabase function:"+exp.getMessage()));
        }finally{
           try{ pstmt.close();}catch(Exception e){}
           try{ jobsConnection.close();}catch(Exception exp){}
              
       }
    } 
    
    public String killJob(String jobid){
        UWSResources uws = new UWSResources();            
        UWSJobDescription uDesc = uws.getJobData(uwsJobElements.phase, jobid);
        
        if(!uDesc.getPhase().equalsIgnoreCase(StaticMessages.msgComplete)&& !uDesc.getPhase().equalsIgnoreCase(StaticMessages.msgExec)){
            uws.updateJobData(jobid, uwsJobElements.phase, StaticMessages.msgAborted);
            return StaticMessages.msgAborted;
        }else
            return uDesc.getPhase();            
    }
    
    public boolean isPending(String jobid){
        
        UWSResources uws = new UWSResources();        
        UWSJobDescription jDesc = uws.getJobData(uwsJobElements.phase, jobid);    
         System.out.println(":"+jDesc.getPhase()+":"+jDesc.getUploaadParams());
        if(jDesc.getPhase().equalsIgnoreCase(StaticMessages.msgPend))
            return true;        
        return false;
    }
    
    //    private String getJobStatus(String jobid){
//      java.sql.Connection jobsConnection = null;
//      Statement stmt = null;
//      java.sql.ResultSet rs=null;
//      String jobstatus ="";
//       try{           
//           jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");   
//           stmt = jobsConnection.createStatement();
//           rs = stmt.executeQuery("Select jobstatus from tapjobstable where jobid = '"+jobid+"'");
//          
//           while(rs.next()){
//              jobstatus = rs.getString("jobstatus");
//           }
//          return jobstatus;
//            
//        }catch (SQLException sexp){            
//            throw new InternalServerErrorException("Exception in insertDatabase function:"+sexp.getMessage());            
//        }catch(Exception exp){            
//             throw new InternalServerErrorException("Exception in insertDatabase function:"+exp.getMessage());
//        }finally{
//           try{ stmt.close();}catch(Exception e){}
//           try{ jobsConnection.close();}catch(Exception exp){}
//       }
//    }
       /**
     * submits job in the queue
     */
//    public void submitJob()
//    {
//        Connection conn = null;
//        Channel chan  = null;
//        try{
//        
//            ConnectionFactory factory = new ConnectionFactory();        
//            factory.setHost(conf.getString("rabbitmq.host"));
//            conn = factory.newConnection();        
//            chan = conn.createChannel();        
//            chan.queueDeclare(conf.getString("rabbitmq.queue.submitted"), false, false, false, null);        
//            String[] messages = new String[4];
//            messages[0] = query;
//            messages[1] = jobid;
//            messages[2] = format;
//            messages[3] = String.valueOf(maxrec);
//            insertDatabase();        
//            chan.basicPublish("",conf.getString("rabbitmq.queue.submitted"), null, getBytes(messages));
//        }
//        catch(Exception exp){            
//            System.out.println("Exception in the submitJob ::"+exp.getMessage());            
//        }
//        finally{            
//            try{
//                chan.close();
//                conn.close();
//            } catch (Exception exp) {
//                System.out.println("Problem closing connections:" + exp.getMessage());
//            }
//        }
//    }
    
     /**
     * Constructor to get the query details 
     * @param qdesc
     * @param conf 
//     */
//    public AsyncHandler(QueryDescription qdesc, Configuration conf){
////        this.query  = qdesc.getQuery();
////        this.jobid  = qdesc.getJobId();
////        this.format = qdesc.getFormat();
////        this.lang   = qdesc.getLang();       
////        this.adql   = qdesc.getAdqlQuery(); 
////        this.destruction =qdesc.getDestruction();        
////        this.conf   = conf;
////        this.maxrec = qdesc.getMaxrec();
//        
//    }
    
    //    private Configuration conf;
//    private String format;
//    private String query;
//    private String jobid;    
//    private String lang;    
//    private String destruction;
//    private String adql;
//    private int maxrec;
}
