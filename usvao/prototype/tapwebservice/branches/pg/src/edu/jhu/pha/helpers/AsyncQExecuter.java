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

import edu.jhu.pha.descriptors.QueryDescription;
import edu.jhu.pha.servlets.LoadProperties;
import edu.jhu.pha.writers.CSVWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.DriverManager;
import edu.jhu.pha.writers.VotableWriter;
import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.util.Calendar;
import org.apache.log4j.Logger;
import edu.jhu.pha.descriptors.StaticMessages;

/**
 * This is written to handle asynchronous queries
 * This class handles the job from queue and execute query, generate results in votable/csv format.
 * @author deoyani nandrekar-heinis
 */
public  class AsyncQExecuter implements Runnable {
    
    private static Logger logger = Logger.getLogger(AsyncQExecuter.class);    
    private String jobId;
    private String iQuery;
    private String resultFormat;
    private String resultsDir;
    private long   duration;
    private int    maxrec;
    private int    servicemax;
    private String uploadparam;
    
    public AsyncQExecuter(QueryDescription qDesc){
        this.jobId        = qDesc.getJobId();
        this.iQuery       = qDesc.getQuery();
        this.resultFormat = qDesc.getFormat();
        this.resultsDir   = qDesc.getResultsDir();
        this.duration     = qDesc.getDuration();
        this.maxrec       = qDesc.getMaxrec();
        this.servicemax   = qDesc.getServiceMax();
        this.uploadparam  = qDesc.getUploadparam();  
    }
    

    public void run(){
        this.updateJobsDb(StaticMessages.msgExec, jobId,"", true);
        boolean success = executeSQLQuery();
        if(success)this.updateJobsDb(StaticMessages.msgComplete, jobId,"", false);        
        synchronized(AsyncQExecuter.class) {
            AsyncQExecuter.class.notify();
        }
    }
    /**
     * Executes actual SQL query
     * @return true or false depending on successful query execution
     */
    private boolean executeSQLQuery() {
        //System.out.println("Query:"+iQuery+"\tjobid:"+jobId+"\tformat:"+resultFormat+"\tResultDir:"+resultsDir+"\tduration:"+duration);
        java.sql.Connection connectDbdata = null;
        ResultSet rs = null;
        Statement stmt = null;
        try{
            
//            if(iQuery.contains(StaticMessages.tapSchema)){
//                iQuery = iQuery.replace(StaticMessages.tapSchema, LoadProperties.propMain.getProperty("jobs.database")+"."+StaticMessages.tapSchema); 
// //               connectDbdata  = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
//            }
//            if(iQuery.contains(StaticMessages.uploadSchema)){
//                iQuery = iQuery.replace(StaticMessages.uploadSchema, LoadProperties.propMain.getProperty("upload.database")+"."+StaticMessages.uploadSchema); 
// //               connectDbdata  = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolUpload");
//            }            
//            //else
            connectDbdata = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");
            stmt = connectDbdata.createStatement();
            
            stmt.setQueryTimeout((int)duration);
            stmt.setMaxRows(servicemax);
            
//            if(uploadparam != null || !uploadparam.isEmpty() || !uploadparam.equals(" ") )
//            {
//                System.out.println("Before calling tableupload:"+uploadparam);
//                UploadHelper up = new UploadHelper();
//                up.uploadData(uploadparam, jobId, true);
//            }
                
            if(!LoadProperties.propMain.getProperty("database.archive").equals("SDSS"))
            rs = stmt.executeQuery(iQuery);
            else                      
            rs = usingSpExec(iQuery);       //Execute using safe stored procedure 
            if(rs == null) return false;
            
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            String Path = this.resultsDir+System.getProperty("file.separator")+
                          jobId+System.getProperty("file.separator")+"Results";
                         
            File file = null ;
            // Create multiple directories           
            boolean success = (new File(Path)).mkdirs();
            
            if(resultFormat.equalsIgnoreCase(StaticMessages.outputVotable)){
                logger.debug("Time before VOtablewrite:"+System.currentTimeMillis());    
                file = new File(Path+"/Result");
                FileOutputStream foStream = new FileOutputStream(file);
                VotableWriter votable = new VotableWriter(rs,maxrec);
                votable.generateFinalVOTable(foStream);                
                logger.debug("Time After VOtablewrite:"+System.currentTimeMillis());    
            }else if(resultFormat.equalsIgnoreCase(StaticMessages.outputCSV)){
                logger.debug("Time before CSVWrite:"+System.currentTimeMillis());    
                file = new File(Path+"/Result");
                FileOutputStream foStream = new FileOutputStream(file);
                PrintStream prnStream = new PrintStream(foStream, true) ;            
                CSVWriter csv = new CSVWriter(prnStream,rs,maxrec,",");                                
                logger.debug("Time After CSVWrite:"+System.currentTimeMillis());    
            } else 
                return false;
            
            return true;
        }catch(SQLException sexp){
            logger.error("SQLException in JobExecuter executequery:"+sexp.getMessage());
            this.updateJobsDb(StaticMessages.msgAborted, jobId,"SQLException in Worker executequery:"+sexp.getMessage(),false);
            return false;
        }catch(IOException iexp){
            logger.error("IOException in JobExecuter executequery:"+iexp.getMessage());
            this.updateJobsDb(StaticMessages.msgAborted, jobId,"IOException in Worker executequery:"+iexp.getMessage(),false);
            return false;
        }catch(Exception exp){
            logger.error("Exception in JobExecuter executequery:"+exp.getMessage());
            this.updateJobsDb(StaticMessages.msgAborted, jobId,"Exception in Worker executequery:"+exp.getMessage(),false);
            return false;
        }finally{
            try{  rs.close();}catch(Exception e){}
            try{  stmt.close();}catch(Exception e){}
            try{  connectDbdata.close();}catch(Exception e){}
        }        
    }
    
    /**
     * To update the taps jobs table depending on state of query/execution
     * @param jobstatus
     * @param jobid
     * @param Error
     * @param execute 
     */
    private void updateJobsDb(String jobstatus, String jobid, String Error, boolean execute) {
      java.sql.Connection jobsConnection = null;
      PreparedStatement pstmt = null;
      try{        
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
     
            String DatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";            
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(DatePattern); 
            java.util.Date dt1 = Calendar.getInstance().getTime();
            df.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            String formated = df.format(dt1);
            System.out.println("test here:"+formated);
            java.sql.Timestamp curTime = new java.sql.Timestamp(df.parse(formated).getTime());
            
            String prepStat = "UPDATE tapjobstable SET jobstatus = ?,";
            
                   if(execute)   prepStat  += " starttime= ?";
                   else          prepStat  += " endtime= ?";
                   
                   prepStat += ", error=?   WHERE jobid = ?";
            
            pstmt =  jobsConnection.prepareStatement(prepStat);

            pstmt.setString(1,jobstatus);
            pstmt.setLong(2,curTime.getTime());            
            pstmt.setString(3, Error);
            pstmt.setString(4, jobid);
            
            pstmt.executeUpdate();
            pstmt.close();            
                    
      }catch(SQLException sexp){
          logger.error("Exception jobs data datbase update."+sexp.getMessage());
      }catch(Exception exp){
          logger.error("Error updating the job details in worker"+exp.getMessage());
      }
      finally{
          try{ pstmt.close();} catch(Exception exp){}
          try{jobsConnection.close();}catch(Exception exp){}
      }
    }    
    
     private ResultSet usingSpExec(String query) throws SQLException{
        
        java.sql.Connection connectDb = null;
        ResultSet rs = null;
//        try{            
        connectDb = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");            
        java.sql.CallableStatement cal = connectDb.prepareCall("{call spExecuteSQL(?,?,?,?,?,?,?,?)}");
        cal.setString(1,query);
        cal.setInt(2, Integer.parseInt(LoadProperties.propMain.getProperty("results.servicemaxrecasync")));
        cal.setString(3,LoadProperties.propMain.getProperty("application.webserver"));
        cal.setString(4,LoadProperties.propMain.getProperty("application.servername"));
        cal.setString(5,"");
        cal.setString(6,"");
        cal.setInt(7,0);
        cal.setInt(8,10);
        rs = cal.executeQuery();
        return rs;
//        }
//        catch(SQLException sexp){
//            System.out.println("SQL Exception in usingSpExex::"+sexp.getMessage());
//            return null;
//        }
//        catch(Exception exp){
//            System.out.println("Exception in usingSpExec::"+exp.getMessage());
//            return null;
//        }

    }
}
