
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
import java.io.StringWriter;
import java.util.HashMap;
import org.jdom.Document;
import org.jdom.ProcessingInstruction;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import edu.jhu.pha.writers.XMLWriter;
import edu.jhu.pha.helpers.resourcehelper.UWSResources;
import edu.jhu.pha.descriptors.EnumDescriptors.uwsJobElements;
import edu.jhu.pha.descriptors.uwsdesc.JobListDescription;
import edu.jhu.pha.descriptors.uwsdesc.UWSJobDescription;
import edu.jhu.pha.descriptors.uwsdesc.UploadListDescriptor;
import edu.jhu.pha.exceptions.BadRequestException;
import edu.jhu.pha.exceptions.NotFoundException;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * It helps different resources of TAP service
 * @author deoyani nandrekar-heinis
 */
public class ResourceHelper {
    
    private static Logger logger = Logger.getLogger(ResourceHelper.class);    
//    private UWSResources uws ;
//    private UWSJobDescription uDesc;
    private String urlEndpoint;
  
    public ResourceHelper(){
        
    } 
//    public ResourceHelper(uwsJobElements var,String jobId){
//      //   uws = new UWSResources();
//      //   uDesc = uws.getJobData(var, jobId);
//    }
    /**
     * 
     * @param var
     * @param jobId
     * @return 
     */
    public void setUrlEndpoint(String value){
        urlEndpoint = value;
    }
    
    /**
     * For /results resource
     * @param url
     * @return 
     */
    public String getResultXML(String url){
        XMLWriter writer = null;
        Document doc = null;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter sw = new StringWriter();        
       try{ 
            writer =new XMLWriter();
            doc = writer.xmlUWSJobResult(url);
            out.output(doc,sw);
            return sw.toString();
        } catch (Exception exp){
              throw new BadRequestException(getVotableError(exp.getMessage()));  
        }        
    }
    
    /**
     * get job related details
     * @param var
     * @param jobId
     * @return 
     */
    public String getJobDataXML(uwsJobElements var, String jobId){
        XMLWriter writer = null;
        Document doc = null;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter sw = new StringWriter();
        UWSResources uws = new UWSResources();
        
        try{
        switch(var){
            case uploadlist:{
                
                            UploadListDescriptor uplDesc = uws.getUploadsList();                
                            writer = new XMLWriter(uplDesc);
                            doc = writer.getXMLDoc(var);
                            doc = addXSL(var,doc); 
                        }
                            break;
                        
            case list : {
                            JobListDescription jlDesc = uws.getJobsList();                
                            writer = new XMLWriter(jlDesc);
                            doc = writer.getXMLDoc(var);
                            doc = addXSL(var,doc); 
                        }   break;
            case newjob:{
                            writer =new XMLWriter();
                            doc = writer.getNewJobXMLDoc(jobId);
                            addXSL(var,doc);
                        }   break;  
            
                       
            default:    {                            
                            UWSJobDescription uwsDesc = uws.getJobData(var, jobId);                            
                            uwsDesc.setResultLink(urlEndpoint);
                            writer = new XMLWriter(uwsDesc);
                            doc = writer.getXMLDoc(var);
                            if(var.equals(uwsJobElements.jobid) 
                               || var.equals(uwsJobElements.destruction)
                               || var.equals(uwsJobElements.duration)
                               || var.equals(uwsJobElements.phase))doc = addXSL(var,doc);
                        }   break;
                
         }
         
            out.output(doc,sw);
            return sw.toString();
        } catch (Exception exp){
              throw new BadRequestException(getVotableError(exp.getMessage()));  
        }
        
    }
    
    public long showJobDuration(String jobid){  
        UWSResources uws = new UWSResources();
        UWSJobDescription uDesc = uws.getJobData( uwsJobElements.duration, jobid);
        if(uDesc == null ) throw new BadRequestException(getVotableError("No Data available for this job:"+jobid));
        return uDesc.getDuration();         
    }
    public java.sql.Timestamp showJobDestruction(String jobid){      
        UWSResources uws = new UWSResources();
        UWSJobDescription uDesc = uws.getJobData( uwsJobElements.destruction, jobid);
        if(uDesc == null ) throw new BadRequestException(getVotableError("No Data available for this job:"+jobid));
        return new java.sql.Timestamp(uDesc.getDestruction());         
    }    
    public String showJobPhase(String jobid){         
        UWSResources uws = new UWSResources();
        UWSJobDescription uDesc = uws.getJobData( uwsJobElements.phase, jobid);
        if(uDesc == null ) throw new BadRequestException(getVotableError("No Data available for this job:"+jobid));
        return uDesc.getPhase();
    }
    public java.sql.Timestamp showJobStarttime(String jobid){
        UWSResources uws = new UWSResources();
        UWSJobDescription uDesc = uws.getJobData( uwsJobElements.starttime, jobid);
        if(uDesc == null ) throw new BadRequestException(getVotableError("No Data available for this job:"+jobid));
        return new java.sql.Timestamp(uDesc.getStarttime());
    }
    public java.sql.Timestamp showJobEndtime(String jobid){
        UWSResources uws = new UWSResources();
        UWSJobDescription uDesc = uws.getJobData( uwsJobElements.endtime, jobid);
        if(uDesc == null ) throw new BadRequestException(getVotableError("No Data available for this job:"+jobid));
        return new java.sql.Timestamp(uDesc.getEndtime());
    }
    
    public String showJobError(String jobid){
        UWSResources uws = new UWSResources();
        UWSJobDescription uDesc = uws.getJobData( uwsJobElements.error, jobid);
        if(uDesc == null ) throw new BadRequestException(getVotableError("No Data available for this job:"+jobid));
        return uDesc.getError();
    }
    
    /**
     * Used by vosi resources availability and capability
     * @param baseURL
     * @param resource
     * @return 
     */
    public String getVOSIXML(String baseURL, String resource, boolean available){
        XMLWriter writer = null;
        Document doc = null;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter sw = new StringWriter();
    try{
            writer = new XMLWriter();
            if(resource.equalsIgnoreCase("capabilities"))
            doc = writer.getCapability(baseURL);
            else if (resource.equalsIgnoreCase("availability")){
                String textAvailable ="Service is not available";                
                if(available)textAvailable = "Service is available now";
                doc = writer.getAvailability(""+available+"",textAvailable,"","","");
            }
            out.output(doc, sw);        
            return sw.toString();
        }catch(Exception exp){
           throw new BadRequestException(getVotableError(exp.getMessage()));  
        }
        
    }
    
//     public String getXMLErmessage(String message){
//        XMLWriter writer = null;
//        Document doc = null;
//        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
//        StringWriter sw = new StringWriter();
//        try{        
//         UWSResources uws = new UWSResources();   
//         writer = new XMLWriter();
//         sw = new StringWriter();
//         out = new XMLOutputter(Format.getPrettyFormat());         
//         doc  =  writer.getVotableError(message, "ERROR"); 
//         out.output(doc, sw);
//         return sw.toString(); 
//        }catch(Exception Exp){
//            return "Exception in writing error !!";
//        }        
//    }   
//    /**
//     * 
//     * @param jobid
//     * @return 
//     */
//     public String getErrorXML(String jobid) {
//        XMLWriter writer = null;
//        Document doc = null;
//        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
//        StringWriter sw = new StringWriter();        
//        UWSResources uws = new UWSResources();   
//        writer = new XMLWriter();
//        sw = new StringWriter();
//        try{
//        out = new XMLOutputter(Format.getPrettyFormat());
//        if(jobid.equals("") || jobid == null)
//            doc  =  writer.getVotableError("No Such Job Available", "ERROR");             
//        else{
//            String errorString = uws.getErrorString(jobid);
//            String errorStatus = "ERROR";
//            if(errorString.equals(""))errorStatus = "OK";
//                doc = writer.getVotableError(errorString,errorStatus);
//        }            
//        out.output(doc, sw);
//        return sw.toString();            
//        }catch(IOException iexp){
//            throw new NotFoundException("Can not find Error resource for this job");
//        }
//    }   
    
     public static String getVotableError(String errorMsg){
        XMLWriter writer = null;
        Document doc = null;
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        StringWriter sw = new StringWriter();
        try{        
         
         writer = new XMLWriter();
         sw = new StringWriter();
         out = new XMLOutputter(Format.getPrettyFormat());         
         doc = writer.getVotableError(errorMsg,"ERROR");                     
         out.output(doc, sw);
         return sw.toString(); 
        }catch(Exception Exp){
            return "Exception in writing error !!";
        }        
    }    
   /**
     * Depending on the user selection
     * @param var
     * @param doc
     * @return 
     */
    private Document addXSL(uwsJobElements var,Document doc){        
           
       HashMap piMap = new HashMap( 2 );
       piMap.put( "type", "text/xsl" );
       switch(var){
           case jobid:      piMap.put( "href", "../../styles/job-display.xsl" ); break;
           case list:       piMap.put( "href", "../styles/joblist-display.xsl" ); break;
           case newjob:     piMap.put( "href", "../styles/newjob-display.xsl" );break;   
           case destruction:piMap.put( "href", "../../../styles/change-destruct.xsl" );break;      
           case duration:   piMap.put( "href", "../../../styles/change-duration.xsl" );break;      
           case phase:      piMap.put( "href", "../../../styles/change-phase.xsl" );break;          
           case uploadlist: piMap.put( "href", "../../styles/uplist-display.xsl" ); break;    
       }       
       ProcessingInstruction pi = new ProcessingInstruction( "xml-stylesheet",piMap );
       doc.getContent().add( 0, pi );                     
       return doc;
    }
    
    /**
     * 
     * @return 
     */
    public String newJobXML(){
        Document document = null;
        XMLWriter xWriter =new XMLWriter();        
        XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
        StringWriter swrite = new StringWriter();
        try{            
            document = addXSL(uwsJobElements.newjob, xWriter.xmlUWSnewJob());           
            xout.output(document,swrite);
            return swrite.toString();
        }catch (Exception exp){              
             throw new BadRequestException(getVotableError(exp.getMessage()));           
        }   
    }
}


