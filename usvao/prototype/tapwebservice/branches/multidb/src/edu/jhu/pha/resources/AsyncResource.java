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
package edu.jhu.pha.resources;

import edu.jhu.pha.descriptors.EnumDescriptors.uwsJobElements;
import edu.jhu.pha.descriptors.QueryDescription;
import edu.jhu.pha.descriptors.StaticMessages;
import edu.jhu.pha.descriptors.uwsdesc.UWSJobDescription;
import edu.jhu.pha.exceptions.InternalServerErrorException;
import edu.jhu.pha.exceptions.NotFoundException;
import edu.jhu.pha.exceptions.PermissionDeniedException;
import edu.jhu.pha.exceptions.TapException;
import edu.jhu.pha.helpers.AsyncHandler;
import edu.jhu.pha.helpers.JobDestroyer;
import edu.jhu.pha.helpers.QueryHelper;
import edu.jhu.pha.helpers.RequestValidator;
import edu.jhu.pha.helpers.ResourceHelper;
import edu.jhu.pha.helpers.UploadHelper;
import edu.jhu.pha.helpers.VospaceHelper;
import edu.jhu.pha.helpers.VospaceTransferHelper;
import edu.jhu.pha.helpers.resourcehelper.UWSResources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * TAP REST-Web Service : Async resource and sub resources (UWS)
 * @author deoyani nandrekar-heinis
 */

@Path("async")
public class AsyncResource {
    
    private static  Logger logger = Logger.getLogger(AsyncResource.class); 
    private @Context ServletContext context;
    private @Context HttpServletResponse response;   
    private @Context HttpServletRequest hrequest;   
    //private ResourceHelper rHelper;
    
   
    /** Creates a new instance of AsyncResource */
    public AsyncResource() {               
                 
    }

    /**
     * resource to get the result file
     * @param jobId
     * @return 
     */
    @GET 
    @Path("{jobid}/results/result")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    //@Produces("application/x-votable+xml") 
    public InputStream getJobsResult(@PathParam("jobid") String jobId)
    {
        logger.info("GET /async/"+jobId+"/results/result");
        try{
            Configuration conf = (Configuration)context.getAttribute("configuration");
            File file = new File(conf.getString("results.datadir")+"/"+jobId+"/Results/Result");
            FileInputStream fis = new FileInputStream(file);
            //System.out.println(file.exists() + "!!");
            response.setHeader("Content-Disposition", "attachment; filename=result");
            response.setHeader("content-type", "application/x-votable+xml");
            response.setContentLength((int)file.length());
            return fis;
        }catch(Exception exp){     
            logger.error(exp.getMessage());
            throw new NotFoundException(ResourceHelper.getVotableError(exp.getMessage()));            
        }        
    }  
    
    /**
     * Resource to get results/
     * @param jobId
     * @return 
     */
    @GET 
    @Path("{jobid}/results")    
    @Produces(MediaType.TEXT_XML) 
    public Response getJobsResults(@PathParam("jobid") String jobId)
    {
        logger.info("GET /async/"+jobId+"/results");
        try{
            System.out.println("ResultURL:"+hrequest.getRequestURL()+"/result"); 
            ResourceHelper rHelper = new ResourceHelper(); 
            rHelper.setUrlEndpoint(hrequest.getRequestURL()+"/result");
            return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getResultXML(hrequest.getRequestURL()+"/result")).build();
        }catch(Exception exp){     
            logger.error(exp.getMessage());
            throw new NotFoundException(ResourceHelper.getVotableError(exp.getMessage()));            
        }        
    }  
    
    
    /**
     * Resource to show error
     * @param jobId
     * @return 
     */
    @GET
    @Path("{jobid}/error")
    @Produces(MediaType.TEXT_XML)
    public Response getError(@PathParam("jobid") String jobId)
    {
        logger.info("GET /async/"+jobId+"/error");
        ResourceHelper rHelper = rHelper = new ResourceHelper(); 
        //rHelper.getErrorXML(jobId);
        return  Response.status(javax.ws.rs.core.Response.Status.OK).entity(ResourceHelper.getVotableError(rHelper.showJobError(jobId))).build();        
    }
    
    /**
     * Phase 
     * @param jobId
     * @return 
     */
    @GET
    @Path("{jobid}/phase")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPhase(@PathParam("jobid") String jobId)
    {        
        logger.info("GET /async/"+jobId+"/phase");
        //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.phase, jobId)).build();
        ResourceHelper rHelper = new ResourceHelper();
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.showJobPhase(jobId)).build();
    }
    
    /**
     * 
     * @param jobId
     * @param phase
     * @return 
     */
    @POST
    @Path("{jobid}/phase")
    @Produces(MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public synchronized Response changePhase(@PathParam("jobid") String jobId,@FormParam("PHASE") String phase)
    {   
        logger.info("POST /async/"+jobId+"/phase PHASE="+phase);
        if(phase.equalsIgnoreCase("ABORT") || phase.equalsIgnoreCase("ABORTED")){
            AsyncHandler ahandler = new AsyncHandler();
            logger.info("Result from ABORT request: " + ahandler.killJob(jobId));
        } else if(phase.equalsIgnoreCase("RUN")){
            Configuration conf = (Configuration)context.getAttribute("configuration");
            AsyncHandler ahandler = new AsyncHandler();
            ahandler.queueJob(jobId, conf);
            
        }else {
            logger.debug("test here if response comes as exception");            
            return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity(ResourceHelper.getVotableError("BAD PHASE REQUEST")).build();      

        }
        try{
                return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async/"+jobId)).build();
        }catch(Exception ex){
                logger.error("Here error redirecting");
                throw new InternalServerErrorException(ResourceHelper.getVotableError(ex.getMessage()));
        }
    }

    ///// POST Methods
    
    /**
     * 
     * @param request
     * @param lang
     * @param query
     * @param version
     * @param format
     * @param maxrec
     * @return 
     */
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_XML)
    public Response postRequest(@FormParam("REQUEST") String request,
                                @FormParam("LANG")    String lang,
                                @FormParam("QUERY")   String query,
                                @FormParam("VERSION") String version,
                                @FormParam("FORMAT")  String format,
                                @FormParam("MAXREC")  String maxrec,
                                @FormParam("UPLOAD")  String upload) 
    {
        logger.info("POST /async REQUEST="+request+" LANG="+lang+" VERSION="+version+
                    " MAXREC="+maxrec+" FORMAT="+format+" UPLOAD="+upload+
                    "\nQUERY="+query);
        
         //Generating Random id for jobs submitted        
         UUID randomid =  UUID.randomUUID();
         String randomIdNum = randomid.toString();
         
         //reading conf parameters
         Configuration conf = (Configuration)context.getAttribute("configuration");
         int maxrecords = 0;
         int serviceMax = conf.getInt("results.servicemaxrecasync");
         if(maxrec != null) {
            try{
                maxrecords = Integer.parseInt(maxrec) ;
            }catch(NumberFormatException exp){
                maxrecords = -1;
            }
         }
         else maxrecords = -1;
                  
         //Storing description of requested query
         QueryDescription qdesc = new QueryDescription(request,query,lang,version, 
                                                       format,maxrecords,randomIdNum,
                                                       null,query,serviceMax);
        
         ResourceHelper rHelper  = new ResourceHelper(); 
         if(qdesc.isAllNull())
             return Response.status(javax.ws.rs.core.Response.Status.OK).
                    entity(rHelper.getJobDataXML(uwsJobElements.list,"")).build();

         RequestValidator reqValidator = new RequestValidator();     
         reqValidator.validateRequest(qdesc);
        
         //Checks syntax and parses query depending on lang
         QueryHelper qHelper = new QueryHelper(qdesc, conf.getString("adql.styleSheet"));      
         //Validated query is sent to asynchandler
         AsyncHandler handle = new AsyncHandler();
         handle.submitJob(qdesc, conf);      
         if(upload!=null){
             
            String username = "testuser"; 
            if(upload.contains("vospace")){
                username = hrequest.getAttribute("username").toString();
                if(username.equals("") || username == null) 
                    throw new PermissionDeniedException("You need to be registered "
                    + "VAO user to upload data in TAP from vospace, Query is Submitted "
                    + "Successfully, you can use jobid resource to upload table before "
                    + "running query. You can accesss the job from following link \n"
                    + ""+hrequest.getRequestURI()+"/"+randomIdNum);                  
            }
            try{
               UploadHelper uh = new UploadHelper(username, true,"public");                  
               uh.submitURL(upload, randomIdNum);
               
            }catch(TapException tap){
                  return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST)
                         .entity("Exception while upload submission check there "
                          + "might be duplication in table names").build();
            }
            
         }        
         try{
            return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async/"+randomIdNum)).build();
         }catch(Exception ex){
           logger.error("Here error redirecting");
           throw new InternalServerErrorException(ResourceHelper.getVotableError(ex.getMessage()));
         }
    }
       
    
    @GET
    @Path("{jobid}")
    @Produces (MediaType.TEXT_XML)
    public Response getJob(@PathParam("jobid") String jobId)
    {
        logger.info("GET /async/"+jobId);
        try{
        //System.out.println("ResultURL:"+hrequest.getRequestURL()+"/results/result");
        ResourceHelper rHelper = rHelper = new ResourceHelper();      
        rHelper.setUrlEndpoint(hrequest.getRequestURL()+"/results/result");
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.jobid,jobId)).build();
        }catch(Exception exp){
            logger.error("Exception in Job:"+exp.getMessage());
            throw new NotFoundException(ResourceHelper.getVotableError("Exp"+exp.getMessage()));
        }

    }
    
    
    @POST
    @Path("{jobid}")
    @Produces (MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitJob(@PathParam("jobid") String jobid, 
                              @FormParam("ACTION")String action, 
                              @FormParam("UPLOAD")String uploadParam, 
                              @FormParam("TABLEACCESS")String upaccess,
                              @FormParam("RESULTSTORE")String resultStore,
                              @FormParam("VOCONTAINER")String vospaceContainer,
                              @FormParam("VODATANODE")String vospaceNode)
    {
        logger.info("POST /async/"+jobid+" ACTION="+action+" UPLOAD="+uploadParam+
                    " TABLEACCESS="+upaccess+" RESULTSTORE="+resultStore+
                    " VOCONTAINER="+vospaceContainer+" VODATANODE="+vospaceNode);
        try{
             Configuration conf = (Configuration)context.getAttribute("configuration");
             if(jobid != null){
             if(action != null && action.equalsIgnoreCase("DELETE")){
                JobDestroyer jd = new JobDestroyer();
                jd.deleteJob(jobid);
                return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async/"+jobid)).build();
             }else if(uploadParam != null){
                  // get authenticated user name
                  String username ="testuser";
                  if(uploadParam.contains("vos:")) {
                    username = hrequest.getAttribute("username").toString();
                    if(username.equals("") || username == null) throw new PermissionDeniedException("You need to be registered VAO user to use this functionality");                  
                    System.out.println("User name :"+username);
                  }
                  ///This is for direct upload
                  //UploadHelper uh = new UploadHelper(username);                  
                  //uh.uploadData(uploadParam,jobid);
                  //Here for async upload
                  try{
                  UploadHelper uh = new UploadHelper(username, true,"public");                  
                  uh.submitURL(uploadParam, jobid);
                  }catch(TapException tap){
                      return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Exception while job submission check there might be duplication in table names").build();
                  }
                  return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async/"+jobid)).build();
             } else if(resultStore != null){
                 if(resultStore.equals("VOSPACE")){
                     String username = hrequest.getAttribute("username").toString();
                     VospaceHelper vhelp = new VospaceHelper(jobid,username);
                     //String resulturl,String container, String datanode
                     //test "http://tempsdss.pha.jhu.edu:8080/sdss/tap/async/6d83a928-e3c5-4248-b2d1-005cc731c1f1/results/result"
                     //System.out.println("RESULT url:"+hrequest.getRequestURL()+"/results/result");
                     //System.out.println("RESULT url:"+hrequest.getServerName()+"/results/result");
                     
                     String resulturl = "http://"+hrequest.getServerName()+":"+conf.getString("http.port") +hrequest.getRequestURI()+"/results/result";
                     System.out.println("RESULT url:"+resulturl);
                     vhelp.pushinToVospace(resulturl,vospaceContainer,vospaceNode);
                 }
             }                
            }
            try{
                return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async")).build();
            }catch(Exception ex){
               logger.error("Here error redirecting the job to joblist");
               throw new InternalServerErrorException(ResourceHelper.getVotableError(ex.getMessage()));
            }
        }catch(Exception exp){
            logger.error("Exception in job action:"+exp.getMessage());           
            throw new TapException("Exception in job action:"+exp.getMessage());
        }
    }
    
    
    
    /**
     * 
     * @param jobId
     * @return 
     */
    
    @GET
    @Path("{jobid}/parameters")
    @Produces (MediaType.TEXT_XML)
    public Response getParameters(@PathParam("jobid") String jobId){
        logger.info("GET /async/"+jobId+"/parameters");
         ResourceHelper rHelper = rHelper = new ResourceHelper(); 
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.parameters, jobId)).build();
    }
    
    @POST
    @Path("{jobid}/parameters")
    @Produces (MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response posttParameters(@PathParam("jobid") String jobId, @FormParam("RUNID") String runId, @FormParam("QUERY") String queryParam)
    {
        logger.info("POST /async/"+jobId+"/parameters RUNID="+runId+
                    "\nQUERY="+queryParam);
        
        ResourceHelper rHelper  = new ResourceHelper();
        if(runId != null && !runId.isEmpty()){                 
                UWSResources uws = new UWSResources();
                uws.updateJobData(jobId, uwsJobElements.runid,runId);
                //String resp = rHelper.getJobDataXML(uwsJobElements.job, jobId);
                //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(resp).build();
        }else if(queryParam != null && !queryParam.isEmpty()){
            //NEED to complete this
            UWSResources uws = new UWSResources();
            UWSJobDescription uwsdesc = uws.getJobData(uwsJobElements.phase, jobId);
            if(!uwsdesc.getPhase().equals(StaticMessages.msgPend)) 
                return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity(
                       ResourceHelper.getVotableError("Job is not in pending state "
                       + "can not change query value")).build() ;
            
            Configuration conf = (Configuration)context.getAttribute("configuration");            
            int serviceMax = conf.getInt("results.servicemaxrecasync");                    
            //Storing description of requested query
            QueryDescription qdesc = new QueryDescription();
            qdesc.setQuery(queryParam);
            qdesc.setADQLquery(queryParam);
            qdesc.setServiceMax(serviceMax);
            qdesc.setJobId(jobId);
            qdesc.setQuery(queryParam);
            
                
            uws.updateJobQuery(qdesc, conf.getString("adql.styleSheet"));
            
        }        
        //return Response.ok(uws.getResourceData(uwsJobElements.parameters, jobId)).build();
         
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.parameters, jobId)).build();
    }
    
    
    @GET
    @Path("{jobid}/destruction")
    @Produces (MediaType.TEXT_PLAIN)
    public Response getDestruction(@PathParam("jobid") String jobId){
        logger.info("GET /async/"+jobId+"/destruction");
         //ResourceHelper rHelper = new ResourceHelper(uwsJobElements.destruction, jobId);
        ResourceHelper rHelper = new ResourceHelper();
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.showJobDestruction(jobId).toString()).build();
        //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.destruction, jobId)).build();
    }
    
    @POST
    @Path("{jobid}/destruction")
    @Produces (MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response changeDestruction(@PathParam("jobid") String jobId, @FormParam("DESTRUCTION") String destruction){
        logger.info("POST /async/"+jobId+"/destruction DESTRUCTIO="+destruction);
        UWSResources uws = new UWSResources();
        ResourceHelper rHelper  = new ResourceHelper(); 
        uws.updateJobData(jobId, uwsJobElements.destruction, destruction);
        String resp = rHelper.getJobDataXML(uwsJobElements.destruction, jobId);
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(resp).build();
    }

    /**
     * Not supported
     * @param jobId
     * @return 
     */
    @GET
    @Path("{jobid}/quote")
    @Produces (MediaType.TEXT_PLAIN)
    public Response getQuote(@PathParam("jobid") String jobId){
        //return Response.ok(uws.getResourceData(uwsJobElements.duration, jobId)).build();
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity("NOT SUPPORTED").build();
    }

    /**
     * 
     * @param jobId
     * @return 
     */
    @GET
    @Path("{jobid}/executionduration")      
    @Produces (MediaType.TEXT_PLAIN)
    public Response getDuration(@PathParam("jobid") String jobId)
    {        
        logger.info("GET /async/"+jobId+"/executionduration");
        //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.duration, jobId)).build();
        //ResourceHelper rHelper = new ResourceHelper(uwsJobElements.duration, jobId);
        ResourceHelper rHelper = new ResourceHelper();
        //System.out.println("Execution duration:"+rHelper.getJobDuration());
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(String.valueOf(rHelper.showJobDuration(jobId))).build();
    }
    
    @POST
    @Path("{jobid}/executionduration")
    @Produces (MediaType.TEXT_XML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response changeDuration(@PathParam("jobid") String jobId,@FormParam("TERMINATION") String termination)
    {
        logger.info("POST /async/"+jobId+"/executionduration TERMINATION="+termination);
        UWSResources uws = new UWSResources();
        ResourceHelper rHelper = new ResourceHelper(); 
        uws.updateJobData(jobId, uwsJobElements.duration, termination);        
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.duration, jobId)).build();
    }
    

    @GET
    @Path("{jobid}/info")
    @Produces (MediaType.TEXT_XML)
    public Response getJobInfo(@PathParam("jobid") String jobId)
    {
        logger.info("GET /async/"+jobId+"/info");
        //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.jobid,jobId)).build();
        try{
            return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async/"+jobId)).build();
        }catch(Exception ex){
           logger.error("Here error redirecting");
           throw new InternalServerErrorException(ResourceHelper.getVotableError(ex.getMessage()));
        }
    }
    @GET
    @Path("{jobid}/starttime")
    @Produces (MediaType.TEXT_PLAIN)
    public Response getJobStarttime(@PathParam("jobid") String jobId)
    {
        logger.info("GET /async/"+jobId+"/starttime");
         //ResourceHelper rHelper = new ResourceHelper(uwsJobElements.starttime, jobId);
         ResourceHelper rHelper = new ResourceHelper();
         return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.showJobStarttime(jobId).toString()).build();

    }
    @GET
    @Path("{jobid}/endtime")
    @Produces (MediaType.TEXT_PLAIN)
    public Response getJobEndtime(@PathParam("jobid") String jobId)
    {
        //ResourceHelper rHelper = new ResourceHelper(uwsJobElements.endtime, jobId);
        ResourceHelper rHelper = new ResourceHelper();
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.showJobEndtime(jobId).toString()).build();
    }
        
    @GET
    @Path("/newjob")
    @Produces ("application/xml")
    public Response getNew(){                 
        logger.info("GET /async/newjob");
        ResourceHelper rHelper  = new ResourceHelper(); 
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.newJobXML()).build();     
    }
    
    @GET
    @Produces ("application/xml")
    public Response getList(){
        logger.info("GET /async");
         ResourceHelper rHelper = new ResourceHelper(); 
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.list,"")).build();
    }
    
    
    @GET
    @Path("authentication")
    @Produces ("text/plain")
    public Response getOauth(){                 
        logger.info("GET /async/authentication");
        //AuthenticationHelper oauthHelp = new AuthenticationHelper();
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(hrequest.getAttribute("requestToken")).build();     
        
    }
    
    @POST
    @Path("vospaceupload")
    @Produces("text/plain")  
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response vospaceData(@FormParam("UPLOAD")String uploadParam ){
        logger.info("POST /async/vospaceupload UPLOAD="+uploadParam);
        
        try{
              //Generating Random id for jobs submitted        
              UUID randomid =  UUID.randomUUID();
              String upjobid = "vospace_"+randomid.toString();                
              VospaceTransferHelper uh = new VospaceTransferHelper();                                
              uh.submitJob(upjobid);
              uh.submitURL(uploadParam);
        }catch(TapException tap){
              return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity("Exception while job submission check there might be duplication in table names").build();
        }
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity("TEST").build();     
    }
    
    @GET
    @Path("uptables")
    @Produces (MediaType.TEXT_XML)
    public Response getUploads()
    {
       logger.info("GET /async/uptables");
       ResourceHelper rHelper = new ResourceHelper(); 
       //return Response.status(javax.ws.rs.core.Response.Status.OK).entity("here test").build();
       return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.uploadlist, null)).build();
    }
    //***//
    // added this extra resource to check status of uploaded tables
    //***//
    @GET
    @Path("{jobid}/uploads")
    @Produces (MediaType.TEXT_XML)
    public Response getUploadStatus(@PathParam("jobid") String jobId)
    {
       logger.info("GET /async/"+jobId+"/uptables");
       ResourceHelper rHelper = new ResourceHelper(); 
       return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.uploadparam, jobId)).build();
    }
    
//    @GET
//    @Path("{jobid}")
//    @Produces (MediaType.TEXT_XML)
//    public Response getJob(@PathParam("jobid") String jobId)
//    {
//        //return Response.ok(uws.getResourceData(uwsJobElements.jobid, jobId)).build();
//         return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.jobid,jobId)).build();
//
//    }
//    @GET
//    @Path("{jobid}/executionduration")       
//    public int getDuration(@PathParam("jobid") String jobId)
//    {        
//        //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.duration, jobId)).build();
//         System.out.println("Execution duration:"+rHelper.getJobDuration(uwsJobElements.duration, jobId));
//         return (int) rHelper.getJobDuration(uwsJobElements.duration, jobId);
//         //return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDuration(uwsJobElements.duration, jobId)).build();
//    }
//    @GET
//    @Path("{jobid}/owner")
//    @Produces (MediaType.TEXT_XML)
//    public Response getOwner(@PathParam("jobid") String jobId)
//    {
//        //return Response.ok(uws.getResourceData(uwsJobElements.owner, jobId)).build();
//        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.owner, jobId)).build();
//    }
//   /**
//     * 
//     * @param request
//     * @param lang
//     * @param query
//     * @param version
//     * @param format
//     * @param maxrec
//     * @param newjob
//     * @param newjobId
//     * @param destruction
//     * @return 
//     */
//    @GET
//    @Produces("application/xml")
//    public Response getXml( @QueryParam("REQUEST") String request,
//                            @QueryParam("LANG") String lang,
//                            @QueryParam("QUERY") String query,
//                            @QueryParam("Version") String version,
//                            @QueryParam("FORMAT") String format,
//                            @QueryParam("MAXREC") String maxrec,
//                            @QueryParam("newjob") String newjob,
//                            @QueryParam("newJobId") String newjobId,
//                            @QueryParam("Destruction")String destruction) {
//
//
//         //Generating Random id for jobs submitted
//         UUID randomid =  UUID.randomUUID();
//         String randomIdNum = randomid.toString();
//         
//         //reading conf parameters
//         Configuration conf = (Configuration)context.getAttribute("configuration");
//         int serviceMaxrec = Integer.parseInt(conf.getString("results.servicemaxrec"));
//         
//         //if new query submitted by user using user interface
//         if(newjobId != null){ if(!newjobId.equals(""))randomIdNum = newjobId;}
//          
//         
//         //Storing description of requested query
//         QueryDescription qdesc = new QueryDescription(request,query,lang,version, 
//                                  format,maxrec,serviceMaxrec,randomIdNum,destruction,query);
//
//        // supports new queries submitted by user interface
//        if(newjob != null) return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.newjob,randomIdNum)).build();
//        
//        // For initial set up it is directed to default tables metadata (special query for any table)
//        if(maxrec !=null && maxrec.equals("0")){ try {
//            return Response.seeOther(new URI("../tables")).build();} catch (URISyntaxException ex) {
//           
//            throw new NotFoundException("Exception in Asyncresource while redirecting tables resource. uri exp:"+ex.getMessage());}
//         }
//        
//        if(qdesc.isAllNull())return Response.status(javax.ws.rs.core.Response.Status.OK).entity(rHelper.getJobDataXML(uwsJobElements.list,"")).build();
//
//        RequestValidator reqValidator = new RequestValidator();     
//        reqValidator.validateRequest(qdesc);
//        
//        //Checks syntax and parses query depending on lang
//        QueryHelper qHelper = new QueryHelper(qdesc,conf.getString("adql.styleSheet"));
//      
//        //Validated query is sent to asynchandler
//         AsyncHandler handle = new AsyncHandler(qdesc, conf);
//         handle.submitJob();
//             
////          try{
////               //return Response.status(javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT).entity(new URI("/async/"+randomIdNum)).build();
////              return Response.temporaryRedirect(new URI("/async/"+randomIdNum)).build();
////          }catch(Exception exp){
////                    throw new NotFoundException("Error Creating URI with :"+randomIdNum);            
////          }          
//          try{
//            return Response.status(javax.ws.rs.core.Response.Status.SEE_OTHER).location(new URI("/async/"+randomIdNum)).build();
//          }catch(Exception ex){
//            logger.error("Here error redirecting");
//           throw new InternalServerErrorException(ex.getMessage());
//          }
//             
//    }
}

