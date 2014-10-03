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

import edu.jhu.pha.servlets.LoadProperties;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.apache.log4j.Logger;

/**
 *
 * @author deoyani nandrekar-heinis
 */
public class JobDestroyer {
    
    private Logger logger = Logger.getLogger(JobDestroyer.class);
    
    public void deleteJob(String jobid){
        dealwithJob(jobid);
        deleteFiles(jobid);
    }
    
    
    /**
     * 
     * @param jobid 
     */
    private void dealwithJob(String jobid){
        
        java.sql.Connection jobsConnection = null;
        PreparedStatement pstmt = null;
        String updateString = "";
        try{
            jobsConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");            
            updateString = " delete from tapjobstable where jobid = '"+ jobid+"'";
            pstmt = jobsConnection.prepareStatement(updateString);
            pstmt.executeUpdate();   
            
        }catch(Exception exp){
            //throw new InternalServerErrorException("Exception in dealwithJob:"+exp.getMessage()); 
            logger.error("Exception in dealwithJob:"+exp.getMessage());
        }finally{
           try{ pstmt.close();}catch(Exception e){}
           try{ jobsConnection.close();}catch(Exception exp){}
        }
    }
    /**
     * 
     * @param jobid  name of the directory to be deleted
     */
    private void deleteFiles(String jobid){
        try{            
            System.out.println("Direc::"+LoadProperties.propMain.getProperty("results.datadir"));
            File f = new File(LoadProperties.propMain.getProperty("results.datadir") +jobid);
            if (!f.exists()) throw new IllegalArgumentException("Delete: no such file or directory: " + jobid);
            if (!f.canWrite())throw new IllegalArgumentException("Delete: write protected: "+jobid);
            delete(f);            
            System.out.println("Here in delete files");
        }catch(Exception exp){
            logger.error("Exception:"+exp.getMessage());
        }
    }
    
    /**
     * Checks whether directory is empty and deletes files accordingly
     * @param file Directory to be deleted
     * @throws IOException 
     */
    private void delete(File file)throws IOException{
 
        
    	if(file.isDirectory()){
     		//directory is empty, then delete it
    		if(file.list().length==0){ 
    		   file.delete();
    		   System.out.println("Directory is deleted : "+ file.getAbsolutePath()); 
    		}else{ 
    		   //list all the directory contents
        	   String files[] = file.list(); 
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(file, temp); 
        	      //recursive delete
        	      delete(fileDelete);
        	   } 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	     System.out.println("Directory is deleted : "+ file.getAbsolutePath());
        	   }
    		} 
    	}else{
    		//if file, then delete it
    		file.delete();
    		System.out.println("File is deleted : " + file.getAbsolutePath());
    	}
    }          
}
