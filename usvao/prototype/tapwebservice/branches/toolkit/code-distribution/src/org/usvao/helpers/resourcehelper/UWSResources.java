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
package org.usvao.helpers.resourcehelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.usvao.descriptors.EnumDescriptors.uwsJobElements;
import org.usvao.descriptors.QueryDescription;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.descriptors.uwsdesc.JobListDescription;
import org.usvao.descriptors.uwsdesc.UWSJobDescription;
import org.usvao.descriptors.uwsdesc.UploadDescriptors;
import org.usvao.descriptors.uwsdesc.UploadListDescriptor;
import org.usvao.exceptions.BadRequestException;
import org.usvao.exceptions.InternalServerErrorException;
import org.usvao.exceptions.NotFoundException;
import org.usvao.exceptions.TapException;
import org.usvao.helpers.QueryHelper;
import org.usvao.helpers.ResourceHelper;
import org.usvao.servlets.LoadProperties;

/**
 * Queries tapjobstable to get job related information 
 * @author deoyani nandrekar-heinis
 */
public class UWSResources {

    private static Logger logger = Logger.getLogger(UWSResources.class);
    public UWSResources(){  }  
    
     /**
     * Returns the list of all jobs in tapjobstable
     * @return JobListDescription
     */
    public UploadListDescriptor getUploadsList(){
        
        String selectString ="Select tabledbname,uploadsuccess, tableaccess FROM ";        
                selectString += LoadProperties.taptableName+ "tapusersdata ";
                
        ResultSet rsStatus = null;
        Connection jobsConnection = null;
        UploadListDescriptor uplDesc = new UploadListDescriptor();
        try{
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            rsStatus = jobsConnection.createStatement().executeQuery(selectString);
            
            List<UploadDescriptors> upList = new ArrayList<UploadDescriptors>();
            while(rsStatus.next()){
                UploadDescriptors uwsUp = new UploadDescriptors();
                uwsUp.setUploadTable(rsStatus.getString("tabledbname"));
                uwsUp.setUploadStatus(rsStatus.getString("uploadsuccess"));
                upList.add(uwsUp);
            }
           
            uplDesc.setUpsList(upList);
            return uplDesc;
            
        }catch(SQLException sexp){
            logger.error("SQLException in Query getJobList for resources :"+sexp.getMessage());
            throw new InternalServerErrorException(ResourceHelper.getVotableError(sexp.getMessage()));
        }catch(Exception exp){
            logger.error("Exception in Query getJobList for resources :"+exp.getMessage());
            throw new InternalServerErrorException(ResourceHelper.getVotableError(exp.getMessage()));
        }finally{
            try{ rsStatus.close();}catch(Exception ex){}            
            try{jobsConnection.close();}catch(Exception ex){}
        }
    }
    
    /**
     * Returns the list of all jobs in tapjobstable
     * @return JobListDescription
     */
    public JobListDescription getJobsList(){
        //// this was for MSSQL        
        ////String selectString ="Select jobid, jobstatus FROM dbo.tapjobstable where jobid "+StaticMessages.notVospace;
       //// this for mysql 
        
        
        String selectString = "Select jobid, jobstatus from  "+StaticDescriptors.tapSchema+"."+"tapjobstable where jobid "+StaticDescriptors.notVospace;
        ResultSet rsStatus = null;
        Connection jobsConnection = null;
        JobListDescription JListDesc = new JobListDescription();
        try{
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            rsStatus = jobsConnection.createStatement().executeQuery(selectString);
            
            List<UWSJobDescription> jobList = new ArrayList<UWSJobDescription>();
            while(rsStatus.next()){
                UWSJobDescription uwsJob = new UWSJobDescription();
                uwsJob.setJobId(rsStatus.getString("jobid"));
                uwsJob.setPhase(rsStatus.getString("jobstatus"));
                jobList.add(uwsJob);
            }
           
            JListDesc.setJobsList(jobList);
            return JListDesc;
            
        }catch(SQLException sexp){
            logger.error("SQLException in Query getJobList for resources :"+sexp.getMessage());
            throw new InternalServerErrorException(ResourceHelper.getVotableError(sexp.getMessage()));
        }catch(Exception exp){
            logger.error("Exception in Query getJobList for resources :"+exp.getMessage());
            throw new InternalServerErrorException(ResourceHelper.getVotableError(exp.getMessage()));
        }finally{
            try{ rsStatus.close();}catch(Exception ex){}            
            try{jobsConnection.close();}catch(Exception ex){}
        }
    }

    /**
     * Accepts resultset of the query for job and returns the data for specified job resource
     * @param rs resultset
     * @param sResource jobresource
     * @return UWSJobdescription 
     */
    private UWSJobDescription getuwsJob(ResultSet rs, uwsJobElements sResource){
        UWSJobDescription uwsJob = null;
    try{
            
            String[] parameters = new String[4];
            
            while(rs.next()){
                uwsJob = new UWSJobDescription();
             
                uwsJob.setJobId(rs.getString("jobid"));
                switch(sResource){
                

                case jobid      :{                                   
                                    parameters[0] = "doQuery";
                                    parameters[1] = rs.getString("lang");
                                    parameters[2] = rs.getString("adql");
                                    parameters[3] = rs.getString("uploadparam");
                                    uwsJob = new UWSJobDescription( rs.getString("jobid"),rs.getString("jobstatus"),
                                             rs.getLong("starttime"),rs.getLong("endtime"),rs.getLong("destruction"),
                                             rs.getLong("duration"),parameters,rs.getString("query"), rs.getString("owner"),
                                             true, rs.getString("error"), rs.getString("adql"));
                                 }
                case error      :{ uwsJob.setError(rs.getString("error")); break; }    
                case phase      :{ uwsJob.setPhase(rs.getString("jobstatus")); break;}
                case starttime  :{ uwsJob.setStarttime(rs.getLong("starttime"));break;}
                case endtime    :{ uwsJob.setEndtime(rs.getLong("endtime")); break;}
                case duration   :{ uwsJob.setDuration(rs.getLong("duration")); break;}
                case destruction:{ uwsJob.setDestruction(rs.getLong("destruction"));break;}
                case owner      :{ uwsJob.setOwner(rs.getString("owner"));break;}
                case parameters : {
                                    parameters[0] = "doQuery";
                                    parameters[1] = rs.getString("lang");
                                    parameters[2] = rs.getString("adql");
                                    if(rs.getString("uploadparam")!=null)
                                    parameters[3] = rs.getString("uploadparam");
                                    else
                                        parameters[3] ="";
                                    uwsJob.setParameters(parameters);
                                    break;}
                
                case uploadparam : {
                                        //uwsJob.setUploadedtables(rs.getString("uploadedtables"));
                                        uwsJob.setUploadParams(rs.getString("uploadparam"));
                                        uwsJob.setPhase(rs.getString("jobstatus"));
                                   }  break;
                    
                case maxrec:   {
                                    uwsJob.setJobId(rs.getString("jobid"));
                                    uwsJob.setMaxrec(rs.getInt("maxrec"));
                               } 
                }

            }
            //if(!hasResults) throw new NotFoundException(ResourceHelper.getVotableError("There is no data associated with this jobid"));
            
            return uwsJob;

        }catch(SQLException sexp){
            logger.error("Exception in resultset access getuwsJob :"+sexp.getMessage());            
            throw new InternalServerErrorException(ResourceHelper.getVotableError(sexp.getMessage()));
           
        }
//        catch(Exception exp){
//            logger.error("Exception in  getuwsJob :"+exp.getMessage());            
//            throw new InternalServerErrorException(ResourceHelper.getVotableError(exp.getMessage()));
//        }
    }

     
     /**
      * Returns given job related data as per resource queried 
      * @param sResource resource queried
      * @param jobid id of job submitted
      * @return UWSJobDescription
      */
     public  UWSJobDescription getJobData(uwsJobElements sResource, String jobid){
        UWSJobDescription uwsJobDesc = new UWSJobDescription();
        ResultSet rsStatus = null;
        Connection jobsConnection = null;
        try{
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            rsStatus = jobsConnection.createStatement().executeQuery(selectString(sResource,jobid));
            
            uwsJobDesc = this.getuwsJob(rsStatus, sResource);            
            if(uwsJobDesc == null) throw new BadRequestException(ResourceHelper.getVotableError("No job data available with job: "+jobid));
            return uwsJobDesc;
        }catch(SQLException sexp){
            logger.error("Query:"+uwsJobDesc.getQuery());
            logger.error("Exception in Query getJobData for resources :"+sexp.getMessage());
            throw new NotFoundException(ResourceHelper.getVotableError(sexp.getMessage()));
        }
//        catch(Exception exp){
//            logger.error("Exception in getJobData :"+exp.getMessage());
//            throw new InternalServerErrorException(ResourceHelper.getVotableError(exp.getMessage()));
//        }
        finally{
            try{if(rsStatus != null) rsStatus.close();}catch(Exception ex){ }
            try{if(jobsConnection !=  null) jobsConnection.close();}catch(Exception ex){}
        }
         
     }     
     
//     /**
//      * Returns error message in standard votable format 
//      * @param jobId job id 
//      * @return String error associated with job
//      */
//     public String getErrorString(String jobId){                  
//         ResultSet rs = null;
//         Connection jobsConnection = null;
//         Statement stmt = null;
//         try{            
//         
//            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");            
//            stmt = jobsConnection.createStatement();
//            rs = stmt.executeQuery("select error from dbo.tapjobstable where jobid = '"+jobId+"'");
//            String error = "";
//            while(rs.next()){
//                error = rs.getString("error");
//            }
//            //if(error.isEmpty()||error.equals("")) error = "No error with this job";
//            //  throw new NotFoundException("Job :"+jobId+" is not available"); 
//                
//             return error;
//         }catch(SQLException sexp){
//              logger.error("SQLException in getJobError String:"+sexp.getMessage());
//              return "SQLException while getJobError String:"+sexp.getMessage();
//        }catch(Exception exp){
//          logger.error("Exception getJobError String: "+exp.getMessage());
//          return "Exception getJobError String: "+exp.getMessage();
//        }finally{
//          try{ stmt.close();} catch(Exception exp){}
//          try{jobsConnection.close();}catch(Exception exp){}
//        }
//     }
     
     /**
      * Update the job information
      * @param qdesc
      * @param adqlstylesheet 
      */
       public void updateJobQuery(QueryDescription qdesc,String adqlstylesheet ){
           
        UWSJobDescription uwsdesc = this.getJobData(uwsJobElements.maxrec, qdesc.getJobId()); 
        qdesc.setMaxrec(uwsdesc.getMaxrec());
        QueryHelper qHelper = new QueryHelper(qdesc, adqlstylesheet);                    
           
        String updateString = " update  "+StaticDescriptors.tapSchema+"."+"tapjobstable set ";            
        updateString += " adql = ? ," ;              
        updateString += " query = ?" ;              
        updateString += " where jobid = ?;" ;       
        
        java.sql.Connection jobsConnection = null;
        PreparedStatement pstmt = null;
        try{        
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");            
            pstmt =  jobsConnection.prepareStatement(updateString);            
            pstmt.setString(1, qdesc.getAdqlQuery());
            pstmt.setString(2, qdesc.getQuery());
            pstmt.setString(3, qdesc.getJobId());
            pstmt.executeUpdate();
            pstmt.close();                                
        }catch(SQLException sexp){
           logger.error("Exception jobs data datbase update."+sexp.getMessage());              
           throw new TapException("SQLException jobs data datbase update."+sexp.getMessage());
        }catch(Exception exp){
           logger.error("Error updating the job details "+exp.getMessage());
           throw new TapException("Exception jobs data datbase update."+exp.getMessage());
        }finally{
           try{ pstmt.close();} catch(Exception exp){}
           try{jobsConnection.close();}catch(Exception exp){}
        }
     }    
       
     /**
      *  For updating resources by post request.
      * 
      * @param jobid
      * @param sResource
      * @param updateValue 
      */
     public void updateJobData(String jobid,uwsJobElements sResource,String updateValue ){
        String updateString = " update  "+StaticDescriptors.tapSchema+"."+"tapjobstable set ";
        //System.out.println("Check before query here:"+sResource);
        switch(sResource){
            
            case phase      : updateString += " jobstatus = ?"  ;break;            
            case duration   : updateString += " duration = ? "  ;break;
            case destruction: updateString += " destruction = ?";break;
            case runid      : updateString += " runid = ?"; break;
                
        }
        updateString  += " where jobid = ?" ;       
        
        java.sql.Connection jobsConnection = null;
        PreparedStatement pstmt = null;
        try{
        
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");            
            pstmt =  jobsConnection.prepareStatement(updateString);
            
            switch(sResource){
            
                case phase      :  pstmt.setString(1,updateValue); break;            
                case duration   :  pstmt.setLong(1,Long.parseLong(updateValue));   ;break;
                case destruction:  {java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");  
                                   java.sql.Timestamp  sqlDateDestruction = new java.sql.Timestamp(df.parse(updateValue).getTime()); 
                                   pstmt.setLong(1,sqlDateDestruction.getTime());
                                   break;}
                case runid      :  pstmt.setString(1, updateValue); break;    
            }
            pstmt.setString(2, jobid);
            pstmt.executeUpdate();
            pstmt.close();            
                    
        }catch(SQLException sexp){
           logger.error("Exception jobs data datbase update."+sexp.getMessage());              
           throw new TapException("SQLException jobs data datbase update."+sexp.getMessage());
        }catch(Exception exp){
           logger.error("Error updating the job details "+exp.getMessage());
           throw new TapException("Exception jobs data datbase update."+exp.getMessage());
        }finally{
          try{ pstmt.close();} catch(Exception exp){}
          try{jobsConnection.close();}catch(Exception exp){}
        }
     }    
     
     /**
      * Forms select string to query database depending on resource queried for given job
      * @param sResource resource
      * @param jobid jobid
      * @return String select query
      */
     private String selectString(uwsJobElements sResource, String jobid){
         String selectString = "select ";
        //System.out.println("Check before query here:"+sResource);
        switch(sResource){
            case jobid          : selectString += " * " ;break;
            case phase          : selectString += " jobid,jobstatus "  ;break;
            case starttime      : selectString += " jobid,starttime "  ;break;
            case endtime        : selectString += " jobid,endtime "    ;break;
            case duration       : selectString += " jobid,duration "   ;break;
            case destruction    : selectString += " jobid,destruction ";break;
            case owner          : selectString += " jobid,owner  "      ;break;
            case parameters     : selectString += " jobid,lang,query,adql,uploadparam,runid " ;break;
            case uploadparam    : selectString += " jobid,jobstatus,uploadparam " ;break;
            case error          : selectString += " jobid,jobstatus,error " ;break;   
            case maxrec         : selectString += " jobid, maxrec " ;break;       
            default             : selectString += " * " ; break;   
        }
        selectString  += " from  "+StaticDescriptors.tapSchema+"."+"tapjobstable where jobid ='"+jobid+"'" ;
        logger.debug("Check query here:"+selectString);
        return selectString;
     }
}

