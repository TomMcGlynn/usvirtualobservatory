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

import com.sun.jersey.multipart.FormDataParam;
import edu.jhu.pha.descriptors.QueryDescription;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.util.Random;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import edu.jhu.pha.helpers.QueryHelper;
import edu.jhu.pha.helpers.RequestValidator;
import edu.jhu.pha.helpers.SyncQExecuter;
import edu.jhu.pha.servlets.LoadProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 * synchronous queries are handled and returned results in VOTable format
 * @author deoyani nandrekar-heinis
 */

@Path("sync")
public class SyncResource {
    
    private static Logger logger = Logger.getLogger(SyncResource.class); 
    private @Context ServletContext context;
    private @Context HttpServletRequest hrequest;
    /** Creates a new instance of SyncResource */
    public SyncResource() { 
        
    }   
    /**
     * Synchronous query request
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
    public Response postRquest(@FormParam("REQUEST") String request,
                               @FormParam("LANG")    String lang,
                               @FormParam("QUERY")   String query,
                               @FormParam("VERSION") String version,
                               @FormParam("FORMAT")  String format,
                               @FormParam("MAXREC")  String maxrec
            ) throws URISyntaxException {
         
         Configuration conf = (Configuration)context.getAttribute("configuration");
         int maxrecords = 0;
         int serviceMax = conf.getInt("results.servicemaxrecsync");
//        System.out.println("webserver"+hrequest.getRequestURL().toString());
//        LoadProperties.propMain.setProperty("webserver", hrequest.getRequestURL().toString());
        //if(maxrec !=null && maxrec.equals("0")) return Response.status(Response.Status.SEE_OTHER).location(new URI("/tables")).build();
        if(maxrec != null) {
            try{ 
                maxrecords = Integer.parseInt(maxrec) ;
            }catch(NumberFormatException exp){
                maxrecords = -1;
            }
        }
        else maxrecords = -1;
      
        QueryDescription qdesc = new QueryDescription(request,query,lang,version,
                                                      format,maxrecords,null,null,
                                                      query,serviceMax);
        RequestValidator reqValidator = new RequestValidator();     
        reqValidator.validateRequest(qdesc);
        
        if(request.equals("doQuery"))
        {             
            ByteArrayOutputStream os  = null;
            Random randomid= new Random();
            QueryHelper qHelper = new QueryHelper(qdesc, conf.getString("adql.styleSheet"));
            SyncQExecuter qExec = new SyncQExecuter();
            os = qExec.executeQuery(qdesc);
            if(os != null && qdesc.getFormat().equalsIgnoreCase("VOTABLE"))
                return Response.ok(os.toByteArray(),"application/x-votable+xml")
                        .header("Content-Disposition","inline; filename=Result"
                        +randomid.nextInt()).build();
            else if(os != null && qdesc.getFormat().equalsIgnoreCase("CSV"))
                return Response.ok(os.toByteArray(),MediaType.TEXT_PLAIN).
                       header("Content-Disposition","inline; filename=Result"
                       +randomid.nextInt()).build();
         }    
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Unable to run the query").build();        
    }  

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
    @GET    
    //@Produces(MediaType.APPLICATION_OCTET_STREAM)
    //@Produces("application/x-votable+xml") 
    @Produces(MediaType.TEXT_XML)
    public Response getXml(@QueryParam("REQUEST")  String request,
                           @QueryParam("LANG")     String lang,
                           @QueryParam("QUERY")    String query,
                           @QueryParam("Version")  String version,
                           @QueryParam("FORMAT")   String format,
                           @QueryParam("MAXREC")   String maxrec) throws URISyntaxException {
//        System.out.println("In sync:"+query+"REQUEST:"+request);
//        System.out.println("webserver"+hrequest.getRequestURL().toString());
//        LoadProperties.propMain.setProperty("webserver", hrequest.getRequestURL().toString());
        
        Configuration conf = (Configuration)context.getAttribute("configuration");
        int maxrecords = 0;
        int serviceMax = conf.getInt("results.servicemaxrecsync");
        //if(maxrec !=null && maxrec.equals("0")) return Response.status(Response.Status.SEE_OTHER).location(new URI("/tables")).build();
       if(maxrec != null) {
            try{ 
                maxrecords = Integer.parseInt(maxrec);
            }catch(NumberFormatException exp){
                maxrecords = -1;
            }
        }
        else maxrecords = -1;
        
        QueryDescription qdesc = new QueryDescription(request,query,lang,version,
                                                      format,maxrecords,null,null,query,serviceMax);
        RequestValidator reqValidator = new RequestValidator();     
        reqValidator.validateRequest(qdesc);
        
        if(request.equals("doQuery"))
        {             
            ByteArrayOutputStream os  = null;
            Random randomid= new Random();
            QueryHelper qHelper = new QueryHelper(qdesc, conf.getString("adql.styleSheet"));
            SyncQExecuter qExec = new SyncQExecuter();
            os = qExec.executeQuery(qdesc);
            if(os != null && qdesc.getFormat().equalsIgnoreCase("VOTABLE"))
                return Response.ok(os.toByteArray(),"application/x-votable+xml").header("Content-Disposition","inline; filename=Result"+randomid.nextInt()).build();                       
            else if(os != null && qdesc.getFormat().equalsIgnoreCase("CSV"))
                return Response.ok(os.toByteArray(),MediaType.TEXT_PLAIN).header("Content-Disposition","inline; filename=Result"+randomid.nextInt()).build();                       

        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Unable to run the query").build();    

//        Configuration conf = (Configuration)context.getAttribute("configuration");
//        int serviceMaxrec  = Integer.parseInt(conf.getString("results.servicemaxrec"));
//        QueryDescription qdesc = new QueryDescription(request,query,lang,version,
//                                          format,maxrec,serviceMaxrec,null,null,query);
//        RequestValidator reqValidator = new RequestValidator();     
//        reqValidator.validateRequest(qdesc);
//        
//        if(request.equals("doQuery"))
//        {             
//            ByteArrayOutputStream os  = null;
//            Random randomid= new Random();
//            QueryHelper qHelper = new QueryHelper(qdesc, conf.getString("adql.styleSheet"));            
//            
//            SyncQExecuter qExec = new SyncQExecuter();
//            os = qExec.executeQuery(qdesc);
//            if(os != null)
//               return Response.ok(os.toByteArray(), MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition","inline; filename=Result"+randomid.nextInt()).build();           
//        }    
//        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Unable to run the query").build();        
    }  
    
        
//    @POST   
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    public Response postRquest(@FormParam("REQUEST") String request,
//                               @FormParam("LANG")    String lang,
//                               @FormParam("QUERY")   String query,
//                               @FormParam("Version") String version,
//                               @FormParam("FORMAT")  String format,
//                               @FormParam("MAXREC")  String maxrec) {
//
//        Configuration conf = (Configuration)context.getAttribute("configuration");
//        int serviceMaxrec = Integer.parseInt(conf.getString("results.servicemaxrec"));
//        QueryDescription qdesc = new QueryDescription(request,query,lang,version,
//                                                      format,maxrec,serviceMaxrec,null,null);
//        RequestValidator reqValidator = new RequestValidator();     
//        reqValidator.validateRequest(qdesc);
//        
//        if(request.equals("doQuery"))
//        {             
//            ByteArrayOutputStream os  = null;
//            Random randomid= new Random();
//            QueryHelper qHelper = new QueryHelper(qdesc, conf.getString("adql.styleSheet"));            
//  
//            
//             QueryExecuter qExec = new QueryExecuter();
//             os = qExec.executeQuery(qdesc);
//             if(os != null)
//               return Response.ok(os.toByteArray(), MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition","inline; filename=Result"+randomid.nextInt()).build();           
//            
//        }    
//        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Unable to run the query").build();        
//    }  
    
//    @Path("/upload")
//    @POST
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response upload(@FormDataParam("FILE") InputStream is) {             
//        try{
////            System.out.println("Checking file:"+input.available());
////            File f=new File("H:\\testdir\\outFile");            
////            OutputStream out=new FileOutputStream("H:\\testdir\\outFile");  
////            byte buf[]=new byte[1024];
////            int len;
////            while((len=input.read(buf))>0){
////                  out.write(buf,0,len);
////                  
////            }      
////            out.close();
////            input.close();
////            System.out.println("\nFile is created....................");
//            if (is != null) {
//            java.io.StringWriter writer = new java.io.StringWriter();
// 
//            char[] buffer = new char[1024];
//            try {
//                java.io.Reader reader = new java.io.BufferedReader(
//                        new java.io.InputStreamReader(is, "UTF-8"));
//                int n;
//                while ((n = reader.read(buffer)) != -1) {
//                    writer.write(buffer, 0, n);
//                }
//            } finally {
//                is.close();
//            }
//            
//            System.out.println("data** ::"+writer.toString());
//            }
//            return Response.ok("UPLOADED").build();
//        }catch (IOException e){ 
//        return Response.status(Response.Status.BAD_REQUEST).entity("UPLOADED Error:"+e.getMessage()).build();}
//    }

}

