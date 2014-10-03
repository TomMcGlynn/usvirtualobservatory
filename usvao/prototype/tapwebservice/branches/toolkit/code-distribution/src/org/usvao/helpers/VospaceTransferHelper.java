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
package org.usvao.helpers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;
import org.apache.log4j.Logger;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.exceptions.InternalServerErrorException;
import org.usvao.exceptions.TapException;
import org.usvao.servlets.LoadProperties;
/**
 * this has been written for direct trafser to and from jhu vospace
 * @author deoyani nandrekar-heinis
 */
public class VospaceTransferHelper {
    private Logger logger = Logger.getLogger(UploadHelper.class);    
    private String username = "vospaceuser";
    private String uniqueid; 
    public VospaceTransferHelper(){
        
    }
    
    public VospaceTransferHelper(String uniqueid){
        this.uniqueid = uniqueid; 
    }
    /**
     * 
     * @param upload
     * @param jobid 
     */
    public void submitURL(String upload) {                         
        extractTablenames(upload);
        updateUsersdata();
    }
    
    private String[][] upl ;
    private int uplno =0;
    private Random generator = new Random();
    /**
     * 
     * @param upload 
     */
    private void extractTablenames(String upload){
       
        
        String tableNames ="";
               
        String[] touploadTables = new String[upload.split(";").length];
        touploadTables = upload.split(";");     
        upl = new String[touploadTables.length][4]; 
        uplno = touploadTables.length;
        for(int j=0 ;j<touploadTables.length;j++ ){
               String[] uploadValues = new String[2];
               uploadValues = touploadTables[j].split(",");
               //if(tableNames.matches(uploadValues[0])) throw new TapException("Table name duplication. Give unique table names with each URL.");
               tableNames += "|"+uploadValues[0]+"|";
               upl[j][0] = uploadValues[0];
               upl[j][1] = uploadValues[1];
               upl[j][2] = uploadValues[0]+"_vospace_"+generator.nextInt();
               upl[j][3] = "public";
        } 
//             }                    
//        }
        checkTableNames(tableNames);
    }
   /**
     * 
     * @param tables 
     */
    private void checkTableNames(String tables){
        
        java.sql.Connection con = null;
        java.sql.Statement stmt  = null;
        java.sql.ResultSet rs = null;
        try{         
            con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            stmt= con.createStatement();
            rs  = stmt.executeQuery("select tableusername from tapusersdata");
            //"where username= '"+this.username+"' and submittedjobs='"+this.jobid+"'");
            
            while(rs.next()){
               if(rs.getString("tableusername")!= null){
                //java.util.regex.Pattern p = java.util.regex.Pattern.compile(rs.getString("tableusername"));
                //java.util.regex.Matcher m = p.matcher(tables);
                //if(m.matches()){ throw new TapException("Table name duplication. Give unique table names with each URL.");}
                if(tables.contains("|"+rs.getString("tableusername")+"|")){
                    tables.replace("|"+rs.getString("tableusername")+"|", "|"+rs.getString("tableusername")+ this.uniqueid+"|") ;
                    //throw new TapException("Table name duplication. Give unique table names with each URL.");
                    return;
                }
               }
            }          
        }catch(SQLException sexp){
            logger.error("Exception in checkTablenames:"+sexp.getMessage());
            throw new TapException("Exception in checktablenames:"+sexp.getMessage());
        }finally{
            try{con.close();}catch(Exception e){}
            try{stmt.close();}catch(Exception e){}
        }        
    }    
    
    /**
     * 
     */
    private void updateUsersdata(){
        java.sql.Connection con = null;
        java.sql.PreparedStatement pstmt  = null;
        try{         
            con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            this.connectQueue();
            for(int i=0;i<this.uplno;i++){
            pstmt = con.prepareStatement(" insert  into  "+StaticDescriptors.tapSchema+"."+"tapusersdata (username, submittedjobs, "
                    + " tableusername, tableurl , tableuploadid  , tableaccess, uploadsuccess, tabledbname)"
                    + " values(?,?,?,?,?,?,?,?) ");
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.uniqueid);
            pstmt.setString(3, upl[i][0]);
            pstmt.setString(4, upl[i][1]);
            pstmt.setString(5, upl[i][2]);
            pstmt.setString(6, upl[i][3]);
            pstmt.setString(7, "QUEUED");
            pstmt.setString(8, StaticDescriptors.uploadSchema+"."+upl[i][0]);
            pstmt.executeUpdate();
            queueJob(upl[i][0],upl[i][1], upl[i][2]);
           }
        }catch(SQLException sexp){
            logger.error("Exception in updateUsersdata:"+sexp.getMessage());
            throw new TapException("Exception in updateUsersdata:"+sexp.getMessage());
        }catch(Exception exp){
            logger.error("Exception in updateUsersdata:"+exp.getMessage());
            throw new TapException("Exception in updateUsersdata:"+exp.getMessage());
        }finally{
            this.disconnectQueue();
            try{con.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
        }
    }
    
    private Connection conn;
    private Channel chan;

    /**
     * 
     */
    private void connectQueue(){
        
        try{
                      
            ConnectionFactory factory = new ConnectionFactory();        
            factory.setHost(LoadProperties.propMain.getProperty("rabbitmq.host"));
            conn = factory.newConnection();        
            chan = conn.createChannel();        
            chan.queueDeclare(LoadProperties.propMain.getProperty("rabbitmq.queue.table.submitted"), false, false, false, null);  
        } catch(Exception exp){            
            System.out.println("Exception in the connectQueue ::"+exp.getMessage());
            throw new TapException("Exception in the connectQueue ::"+exp.getMessage());
        }
            
    }    
    /**
     * 
     */
    private void disconnectQueue(){
       try{
             chan.close();
             conn.close();
       } catch (Exception exp) {
              System.out.println("Problem closing connections:" + exp.getMessage());
       }
    }
    /**
     * 
     * @param uploadtablename
     * @param uploadtableurl
     * @param uploadtableid 
     */
    private void queueJob(String uploadtablename, String uploadtableurl, String uploadtableid){       
      
        try{
            String[] messages = new String[5];
            messages[0] = this.uniqueid;            
            messages[1] = this.username;            
            messages[2] = uploadtablename;
            messages[3] = uploadtableurl;
            messages[4] = uploadtableid;             
            chan.basicPublish("",LoadProperties.propMain.getProperty("rabbitmq.queue.table.submitted"), null, getBytes(messages));
        }
        catch(Exception exp){            
            System.out.println("Exception in the submitJob ::"+exp.getMessage());            
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
    public void submitJob(String upjobid){
      this.uniqueid=upjobid;
      java.sql.Connection jobsConnection = null;
      java.sql.PreparedStatement pstmt = null;
       try{           
           jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");   
           //For setting date format
           String DatePattern = "yyyy-MM-dd HH:mm:ss.SSS";            
           java.text.DateFormat df = new java.text.SimpleDateFormat(DatePattern);           
           java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());   
        
           java.util.Calendar cal = java.util.Calendar.getInstance();
           cal.setTime(sqlDate);
           cal.add(java.util.Calendar.DAY_OF_MONTH, 30);
           java.sql.Timestamp  sqlDateDestruction =null;          
           sqlDateDestruction = new java.sql.Timestamp(cal.getTime().getTime());
        
           pstmt = jobsConnection.prepareStatement(" insert into " + StaticDescriptors.tapSchema+"."+"tapjobstable "
                   +"( jobid,jobstatus,starttime,duration,destruction,lang,query,adql,resultFormat,request,maxrec)"
                   + " values(?,?,?,?,?,?,?,?,?,?,?)");
            pstmt.setString(1,upjobid);
            pstmt.setString(2, "PENDING");
            pstmt.setLong(3, 0); //sqlDate.getTime());
            pstmt.setLong(4, 0);
            pstmt.setLong(5, sqlDateDestruction.getTime());
            pstmt.setString(6, "") ;
            pstmt.setString(7, "");       
            pstmt.setString(8, "");
            pstmt.setString(9, "");
            pstmt.setString(10,"");
            pstmt.setInt(11, 0);
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
}
