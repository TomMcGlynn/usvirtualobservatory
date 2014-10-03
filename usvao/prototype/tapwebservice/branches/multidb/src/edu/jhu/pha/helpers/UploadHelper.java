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

import edu.jhu.pha.descriptors.EnumDescriptors.uwsJobElements;
import edu.jhu.pha.descriptors.uwsdesc.UWSJobDescription;
import edu.jhu.pha.exceptions.TapException;
import edu.jhu.pha.helpers.resourcehelper.UWSResources;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import edu.jhu.pha.servlets.LoadProperties;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import edu.jhu.pha.descriptors.StaticMessages;

/**
 * This is written to upload the data in database
 * @author deoyani nandrekar-heinis
 */
public class UploadHelper {
    
    private String jobid = null;      
    private String username = "";
    private Logger logger = Logger.getLogger(UploadHelper.class);    
    private UWSJobDescription jDesc ;
    private String uploadparam = "";
    private String tableaccess = "";
    
    /**
     * 
     */
    public UploadHelper(){
        jDesc = new UWSJobDescription();
    }
    /**
     * 
     * @param username
     * @param async
     * @param tableaccess 
     */
    public UploadHelper(String username, boolean async, String tableaccess){        
        this.username = username;
        this.tableaccess = tableaccess;
    }
    
    /**
     * 
     * @param userid 
     */
    public UploadHelper(String userid){
        username   = userid;            
        jDesc = new UWSJobDescription();
    }
    
   
    /**
     * 
     * @param upload
     * @param jobid 
     */
    public void submitURL(String upload, String jobid) {
        this.jobid = jobid;
        
        if(isPending()){       
            
            this.uploadparam = jDesc.getUploaadParams();            
            if(this.uploadparam == null || this.uploadparam.isEmpty() || this.uploadparam.equals(" ") ) 
                this.uploadparam = upload;            
            else 
                this.uploadparam =  this.uploadparam+upload;

            extractTablenames(upload);
            this.updateJobstable();
            this.updateUsersdata();
            
            //System.out.println("here is what it is");            
           
        }else{
            throw new TapException("Job is not in PENDING state, you can not upload data");
        }
    }
   /**
     * 
     */
    private String[][] upl ;
    private int uplno =0;
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
                       upl[j][2] = uploadValues[0]+"_"+this.jobid;
                       upl[j][3] = this.tableaccess;
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
                  if(tables.contains("|"+rs.getString("tableusername")+"|"))throw new TapException("Table name duplication. Give unique table names with each URL.");
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
    private void updateJobstable(){
        java.sql.Connection con = null;
        java.sql.PreparedStatement pstmt  = null;
        try{         
            con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            pstmt = con.prepareStatement(" update tapjobstable set uploadparam = ? where jobid = ? ");
            pstmt.setString(1, this.uploadparam);                        
            pstmt.setString(2, jobid);
            pstmt.executeUpdate();
        }catch(SQLException sexp){
            logger.error("Exception in updateJobsTable:"+sexp.getMessage());
            throw new TapException("Exception in updateJobsTable:"+sexp.getMessage());
        }catch(Exception exp){
            logger.error("Exception in updateJobsTable:"+exp.getMessage());
            throw new TapException("Exception in updateJobstable:"+exp.getMessage());
        }finally{
            try{con.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
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
            pstmt = con.prepareStatement(" insert into tapusersdata (username, submittedjobs, "
                    + " tableusername, tableurl , tableuploadid  , tableaccess, uploadsuccess)"
                    + " values(?,?,?,?,?,?,?) ");
            pstmt.setString(1, this.username);
            pstmt.setString(2, this.jobid);
            pstmt.setString(3, upl[i][0]);
            pstmt.setString(4, upl[i][1]);
            pstmt.setString(5, upl[i][2]);
            pstmt.setString(6, upl[i][3]);
            pstmt.setString(7, StaticMessages.msgQueue);
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
     
    /**
     * 
     * @return 
     */ 
    public boolean isPending(){
        
        UWSResources uws = new UWSResources();        
        jDesc = uws.getJobData(uwsJobElements.uploadparam, jobid);    
         System.out.println(":"+jDesc.getPhase()+":"+jDesc.getUploaadParams());
        if(jDesc.getPhase().equalsIgnoreCase(StaticMessages.msgPend))
            return true;        
        return false;
    }
    
    
    /**
     * 
     */
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
            messages[0] = this.jobid;            
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
    
}
