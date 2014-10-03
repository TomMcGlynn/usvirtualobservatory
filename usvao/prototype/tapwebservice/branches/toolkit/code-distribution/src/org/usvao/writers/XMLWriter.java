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
package org.usvao.writers;

/**
 * writes xml document for all UWS resources 
 * @author deoyani nandrekar-heinis
 */
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Attribute;
import org.usvao.descriptors.uwsdesc.JobListDescription;
import org.usvao.descriptors.uwsdesc.UWSJobDescription;
import org.usvao.descriptors.EnumDescriptors.uwsJobElements;
import org.usvao.descriptors.EnumDescriptors.uwsJobParametersEle;
import org.usvao.descriptors.uwsdesc.UploadDescriptors;
import org.usvao.descriptors.uwsdesc.UploadListDescriptor;
import org.usvao.servlets.CheckJobs;
import org.usvao.servlets.LoadProperties;
import java.io.IOException;
import java.util.Calendar;
        


public class XMLWriter
{
    private static Logger log = Logger.getLogger(XMLWriter.class);
    private JobListDescription jobList;
    private UploadListDescriptor upList;
    private UWSJobDescription uwsJob;
      
    public XMLWriter() {  }
    /**
     * Constructor to create the XML doc for all jobs which are submitted
     * @param jobSchema 
     */
    public XMLWriter(JobListDescription jobSchema)
    {
        this.jobList = jobSchema;
    }
    
    public XMLWriter(UploadListDescriptor upldes)
    {
        this.upList = upldes;
    }
    
    /*
     * Constructor called to generate doc on a single job related query/resource
     * according to UWS specifications
     */
    public XMLWriter(UWSJobDescription uwsJob)
    {
        this.uwsJob = uwsJob;
    }

    
    /**
     * This is a common method serves a purpose of returning xml document depending 
     * on resource requirement.
     * @param resource type of queried resource from the enum
     * @return xml document
     */
    public Document getXMLDoc(uwsJobElements resource){
        
        Document document = new Document();
        
        if(resource == uwsJobElements.uploadlist)
            document.addContent(this.xmlUWSuploadList());
        else if(resource == uwsJobElements.list)
            document.addContent(this.xmlUWSJobList());
        else 
            document.addContent(xmlUWSJob(resource));
               
        return document;
    }
    /**
     * 
     * @param eleUWSJob
     */
    private void addForJob(Element eleUWSJob)
    {
        Namespace xlink = Namespace.getNamespace("xlink", LoadProperties.propMain.getProperty("uri.xlink"));
        addChild(eleUWSJob, "runId", uwsJob.getRunId());
        addChild(eleUWSJob, "phase", uwsJob.getPhase());
        if(uwsJob.getStarttime()!= 0)
        addChild(eleUWSJob, "startTime",new java.sql.Timestamp( uwsJob.getStarttime()).toString());
        if(uwsJob.getEndtime()!=0)
        addChild(eleUWSJob, "endTime", new java.sql.Timestamp(uwsJob.getEndtime()).toString());
        addChild(eleUWSJob, "executionDuration",Long.toString(uwsJob.getDuration()));
        addChild(eleUWSJob, "destruction", new java.sql.Timestamp(uwsJob.getDestruction()).toString());
        Element elePara = addChild(eleUWSJob, "parameters", "");
        for(int i=0; i<uwsJob.getParameters().length; i++){
              uwsJobParametersEle e = uwsJobParametersEle.values()[i];
              Element childPara = addChild(elePara, "parameter",uwsJob.getParameters()[i]);
              childPara.setAttribute("id",e.name());
        }
        Element childR = addChild(eleUWSJob,"results","");
                addChild(childR,"result","RESULT") .setAttribute("href",uwsJob.getResultLink(), xlink);
        addChild(eleUWSJob, "owner", uwsJob.getOwner());
    }

    /**
     * 
     * @param resource
     * @returns
     */
    private Element xmlUWSJob(uwsJobElements resource)
    {
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));
        Namespace schemaLocation = Namespace.getNamespace("schemaLocation", LoadProperties.propMain.getProperty("uri.uwsloc"));
        Namespace xlink = Namespace.getNamespace("xlink", LoadProperties.propMain.getProperty("uri.xlink"));

        Element eleUWSJob = new Element("job",uws.getPrefix(), uws.getURI());
        eleUWSJob.addNamespaceDeclaration(xsi);
        eleUWSJob.addNamespaceDeclaration(schemaLocation);
        eleUWSJob.addNamespaceDeclaration(xlink);
        addChild(eleUWSJob, "jobId", uwsJob.getJobId());


        switch(resource){
            case jobid:{
                addForJob(eleUWSJob);
                break;
            }
            case runid:{
                addChild(eleUWSJob, "runId", uwsJob.getRunId());
                break;}
            case phase:{
                addChild(eleUWSJob, "phase", uwsJob.getPhase());
                break;}
            case starttime:{
                if(uwsJob.getStarttime()!= 0)
                addChild(eleUWSJob, "startTime",new java.sql.Timestamp( uwsJob.getStarttime()).toString());
                break;}
            case endtime:{
                if(uwsJob.getEndtime()!= 0)
                addChild(eleUWSJob, "endTime", new java.sql.Timestamp(uwsJob.getEndtime()).toString());
                break;}
            case duration:{                
                addChild(eleUWSJob, "executionDuration",Long.toString(uwsJob.getDuration()));
                break;}
            case destruction:{
                addChild(eleUWSJob, "destruction", new java.sql.Timestamp(uwsJob.getDestruction()).toString());
                break;}
            case parameters:{
                Element elePara = addChild(eleUWSJob, "parameters", "");
                for(int i=0; i<uwsJob.getParameters().length; i++){
                    uwsJobParametersEle e = uwsJobParametersEle.values()[i];
                    Element childPara = addChild(elePara, "parameter",uwsJob.getParameters()[i]);
                    childPara.setAttribute("id",e.name());
                }
                break;}
            case result:{
                Element childR = addChild(eleUWSJob,"results","");
                addChild(childR,"result","RESULT") .setAttribute("href",uwsJob.getResultLink(), xlink);
                break;}
            case owner:{
                addChild(eleUWSJob, "owner", uwsJob.getOwner());
                break;}
            case quote:{
                addChild(eleUWSJob, "quote", uwsJob.getQuery());
                break;}

            }

        eleUWSJob.addNamespaceDeclaration(xsi);
        eleUWSJob.addNamespaceDeclaration(schemaLocation);

        return eleUWSJob;
    }
//    /**
//     * This is to write job related subresources for async resource
//     * @return
//     */
//    private Element xmlUWSJob(uwsJobElements resource)
//    {
//        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
//        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));
//        Namespace schemaLocation = Namespace.getNamespace("schemaLocation", LoadProperties.propMain.getProperty("uri.uwsloc"));
//        Namespace xlink = Namespace.getNamespace("xlink", LoadProperties.propMain.getProperty("uri.xlink"));
//
//        Element eleUWSJob = new Element("job",uws.getPrefix(), uws.getURI());
//        eleUWSJob.addNamespaceDeclaration(xsi);
//        eleUWSJob.addNamespaceDeclaration(schemaLocation);
//        eleUWSJob.addNamespaceDeclaration(xlink);
//        addChild(eleUWSJob, "jobId", uwsJob.getJobId());
//
//        if(uwsJob.getaccessError()){
//             addChild(eleUWSJob, "error", uwsJob.getError());
//             eleUWSJob.addNamespaceDeclaration(xsi);
//             eleUWSJob.addNamespaceDeclaration(schemaLocation);
//             return eleUWSJob;
//        }
//
//        if(resource == uwsJobElements.runid || resource == uwsJobElements.runid)
//            if(uwsJob.getPhase() != null && uwsJob.getJobId() != null)
//            addChild(eleUWSJob, "runId", uwsJob.getRunId());
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.phase)
//            if(uwsJob.getPhase() != null && uwsJob.getJobId() != null)
//            addChild(eleUWSJob, "phase", uwsJob.getPhase());
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.starttime)
//            if(uwsJob.getStarttime() != 0 && uwsJob.getJobId() != null)
//            addChild(eleUWSJob, "startTime",new java.sql.Timestamp( uwsJob.getStarttime()).toString());
//
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.endtime)
//            if(uwsJob.getEndtime() != 0 && uwsJob.getJobId() != null)
//            addChild(eleUWSJob, "endTime", new java.sql.Timestamp(uwsJob.getEndtime()).toString());
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.duration)
//            if(uwsJob.getDuration() != 0 && uwsJob.getJobId() != null)
//            addChild(eleUWSJob, "executionDuration",Long.toString(uwsJob.getDuration()));
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.destruction)
//            if(uwsJob.getDestruction() != 0 && uwsJob.getJobId() != null)
//            addChild(eleUWSJob, "destruction", new java.sql.Timestamp(uwsJob.getDestruction()).toString());
//
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.parameters)
//            if(uwsJob.getParameters() != null && uwsJob.getJobId() != null)
//            {
//                Element elePara = addChild(eleUWSJob, "parameters", "");
//                for(int i=0; i<uwsJob.getParameters().length; i++){
//                    uwsJobParametersEle e = uwsJobParametersEle.values()[i];
//                    Element childPara = addChild(elePara, "parameter",uwsJob.getParameters()[i]);
//                    childPara.setAttribute("id",e.name());
//                }
//            }
//        if(resource == uwsJobElements.jobid || resource == uwsJobElements.result)
//            if( uwsJob.getResults() && uwsJob.getJobId() != null){
//            Element childR = addChild(eleUWSJob,"results","");
//                    addChild(childR,"result","RESULT") .setAttribute("href",uwsJob.getResultLink(), xlink);
//
//            }
//
//        eleUWSJob.addNamespaceDeclaration(xsi);
//        eleUWSJob.addNamespaceDeclaration(schemaLocation);
//
//        return eleUWSJob;
//    }
    
    
    
    public Document xmlUWSJobResult(String url)
    {
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));
        Namespace schemaLocation = Namespace.getNamespace("schemaLocation", LoadProperties.propMain.getProperty("uri.uwsloc"));
        Namespace xlink = Namespace.getNamespace("xlink", LoadProperties.propMain.getProperty("uri.xlink"));

        Element eleUWSJob = new Element("results",uws.getPrefix(), uws.getURI());
        eleUWSJob.addNamespaceDeclaration(xsi);
        eleUWSJob.addNamespaceDeclaration(schemaLocation);
        eleUWSJob.addNamespaceDeclaration(xlink);                
        addChild(eleUWSJob,"result","").setAttribute("href", url, xlink);        
        
        Document document = new Document();
        document.addContent(eleUWSJob);
        return document;
    }

    /**
     * Add child to the Element of UWS job
     * @param eleParent
     * @param chdName
     * @param chdText
     * @return 
     */
    private Element addChild(Element eleParent, String chdName, String chdText)
    {
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));
        Element ele = new Element(chdName, uws.getPrefix(),uws.getURI());
        if (chdText != null && !chdText.equals("")) {
            ele.setText(chdText);
        }
        eleParent.addContent(ele);
        return ele;
    } 
    
    
    /**
     * List of the jobs submitted
     * @return 
     */
    private Element xmlUWSJobList()
    {
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));

        Element eleList = new Element("jobs",uws.getPrefix(),uws.getURI());  
        for(UWSJobDescription uwsjob : this.jobList.getJobsList()){
            Element eleJobRef = addChild(eleList, "jobref", "");
            eleJobRef.setAttribute("id",uwsjob.getJobId());
            addChild(eleJobRef, "phase", uwsjob.getPhase());
        }
        eleList.addNamespaceDeclaration(xsi);
        eleList.setAttribute( new Attribute("schemaLocation",LoadProperties.propMain.getProperty("uri.uwsloc"), uws));
        return eleList;
    }
    
    
     /**
     * List of the jobs submitted
     * @return 
     */
    private Element xmlUWSuploadList()
    {
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));

        Element eleList = new Element("uploads",uws.getPrefix(),uws.getURI());  
        for(UploadDescriptors uwsUps : this.upList.getUpsList()){
            Element eleJobRef = addChild(eleList, "upref", "");
            eleJobRef.setAttribute("uploadtable",uwsUps.getUploadTable());
            addChild(eleJobRef, "uploadstatus", uwsUps.getUploadStatus());
        }
        eleList.addNamespaceDeclaration(xsi);
        eleList.setAttribute( new Attribute("schemaLocation",LoadProperties.propMain.getProperty("uri.uwsloc"), uws));
        return eleList;
    }


    //for user friendly input page
    public Document getNewJobXMLDoc(String jobId){

        Document document = new Document();
        document.addContent(this.xmlUWSnewJob(jobId));
        //System.out.println("sw:"+document.toString());
        return document;
    }
    
    public Document xmlUWSnewJob() 
    {
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));
        Namespace schemaLocation = Namespace.getNamespace("schemaLocation", LoadProperties.propMain.getProperty("uri.uwsloc"));       

        Document document = new Document();
        //System.out.println("sw;"+jobId);
        Element eleUWSJob = new Element("job",uws.getPrefix(), uws.getURI());
        addChild(eleUWSJob, "jobId", "");       
        addChild(eleUWSJob, "phase", "PENDING");        
        addChild(eleUWSJob, "starttime", "");        
        addChild(eleUWSJob, "endtime", "");
        addChild(eleUWSJob, "duration", "");        
        addChild(eleUWSJob, "destruction", "");        
       
        Element elePara = addChild(eleUWSJob, "parameters", "");
        for(int i=0; i<3; i++){
                uwsJobParametersEle e = uwsJobParametersEle.values()[i];
                Element childPara = addChild(elePara, "parameter","");
                childPara.setAttribute("id",e.name());
        }       
        addChild(eleUWSJob,"results","");
        eleUWSJob.addNamespaceDeclaration(xsi);
        eleUWSJob.addNamespaceDeclaration(schemaLocation);

        document.addContent(eleUWSJob);
        return document;
        
    }
    /**
     * For the user friendly new job submission ui 
     * @param jobId
     * @return 
     */
     private Element xmlUWSnewJob(String jobId) 
    {
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace uws = Namespace.getNamespace("uws",  LoadProperties.propMain.getProperty("uri.uws"));
        Namespace schemaLocation = Namespace.getNamespace("schemaLocation", LoadProperties.propMain.getProperty("uri.uwsloc"));
        //System.out.println("sw;"+jobId);
        Element eleUWSJob = new Element("job",uws.getPrefix(), uws.getURI());
        addChild(eleUWSJob, "jobId", jobId);       
        addChild(eleUWSJob, "phase", "PENDING");        
        addChild(eleUWSJob, "starttime", "");        
        addChild(eleUWSJob, "endtime", "");
        addChild(eleUWSJob, "duration", "");        
        try{
            java.sql.Timestamp  sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());   
            //System.out.println("sqlDate:"+sqlDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(sqlDate);
            cal.add(Calendar.DAY_OF_MONTH, 30);           
            java.sql.Timestamp sqlDateDestruction = new java.sql.Timestamp(cal.getTime().getTime());
            addChild(eleUWSJob, "destruction", sqlDateDestruction.toString());
        }catch(Exception exp){
            System.out.println("Exception in parseing date:"+exp.getMessage());
        }
       
        Element elePara = addChild(eleUWSJob, "parameters", "");
           for(int i=0; i<3; i++){
                uwsJobParametersEle e = uwsJobParametersEle.values()[i];
                Element childPara = addChild(elePara, "parameter","");
                childPara.setAttribute("id",e.name());
            }
       
        addChild(eleUWSJob,"results","");
        eleUWSJob.addNamespaceDeclaration(xsi);
        eleUWSJob.addNamespaceDeclaration(schemaLocation);

        return eleUWSJob;
    }
     /**
      * To get Error xml in the specified VOtable format.
      * @param errorString
      * @return 
      */        
     public Document getVotableError(String errorString, String queryStatus){
        Document document = new Document();
        Element eleVotable = new Element("VOTABLE");
        Element resource = new Element("RESOURCE");
        resource.setAttribute("type","results");
        Element info = new Element("INFO");
        info.setAttribute("name","QUERY_STATUS");
        info.setAttribute("value",queryStatus);
        info.setText(errorString);
        resource.addContent(info);
        Element sInfo = new Element("INFO");
        sInfo.setAttribute("SPECIFICATION",LoadProperties.propMain.getProperty("specification.id") );
        resource.addContent(sInfo);
        Element vInfo = new Element("INFO");
        vInfo.setAttribute("VERSION",LoadProperties.propMain.getProperty("version.implemented") );        
        resource.addContent(vInfo);
        eleVotable.addContent(resource);
        document.addContent(eleVotable);
        return document;
    }
     /**
      * Generating XMl for  Availability resources
      */
    private Element addChildAvail(Element eleParent, String chdName, String chdText)
    {
        Namespace vosiavail = Namespace.getNamespace("avail", LoadProperties.propMain.getProperty("uri.vosiavail"));

        Element ele = new Element(chdName, vosiavail.getPrefix(), vosiavail.getURI());
        if (chdText != null && !chdText.equals("")) {
            ele.setText(chdText);
        }
        eleParent.addContent(ele);
        return ele;
    }
     public Document getAvailability(String tf, String note, String upSince, String downAt, String backAt){
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace vosiavail = Namespace.getNamespace("avail",  LoadProperties.propMain.getProperty("uri.vosiavail"));
        Namespace vod = Namespace.getNamespace("vod", LoadProperties.propMain.getProperty("uri.vod"));        
        Document document = new Document();
        Element eleAvail  = new Element("availability",vosiavail.getPrefix(),vosiavail.getURI());
        eleAvail.addNamespaceDeclaration(xsi);
        eleAvail.addNamespaceDeclaration(vod);
        addChildAvail(eleAvail,"available", tf);
        addChildAvail(eleAvail,"note", note);
        try{
         //InputStreamReader is = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("/tapwebservice.properties"));
         //Properties propMain = new Properties();
         //propMain.load(is);
         System.out.println("start date:"+CheckJobs.uptime);
         addChildAvail(eleAvail,"upSince",CheckJobs.uptime.toString());
         }catch(Exception exp){
             
         }
         //addChildAvail(eleAvail,"upSince",upSince );
         //addChildAvail(eleAvail,"downAt",downAt);
         //addChildAvail(eleAvail,"backAt",backAt);
         document.addContent(eleAvail);
         return document;
     }

     /**
      * Here are functions for capability Resource xml (VOSI standard)
      */
    /**
      * Used for Capability resource childrens
      * @param eleParent parent to which child added
      * @param chdName Name of the child
      * @param chdText text in child 
      * @param attriName attribute of child 
      * @param attriVal  attribute value of child
      * @param attriNamespace  namespace for attribute
      * @return element 
      */        
    private Element addChildCap(Element eleParent, String chdName, String chdText, 
            String attriName, String attriVal, Namespace attriNamespace)
    {
        Element ele = new Element(chdName);
        if (chdText != null && !chdText.equals("")) 
            ele.setText(chdText);
        if(attriName != null && attriVal != null && attriNamespace != null)
            ele.setAttribute(attriName, attriVal, attriNamespace);
        else if (attriName != null && attriVal != null )
            ele.setAttribute(attriName, attriVal);
        eleParent.addContent(ele);
        return ele;
    }
     /**
     * Returns the XMl document for capability resource
     * @param baseUrl baseURL used
     * @return Document
     */
     public Document getCapability(String baseUrl){
        Namespace xsi = Namespace.getNamespace("xsi", LoadProperties.propMain.getProperty("uri.xsi"));
        Namespace vod = Namespace.getNamespace("vo",  LoadProperties.propMain.getProperty("uri.vod"));
        Namespace vosicap = Namespace.getNamespace("capabilities", LoadProperties.propMain.getProperty("uri.vosicap"));
        
        Document document = new Document();
        Element eleCap = new Element("capabilities",vosicap.getPrefix(),vosicap.getURI()); 
        eleCap.addNamespaceDeclaration(xsi);
        eleCap.addNamespaceDeclaration(vod);
        String [] listCap = new String[4];
        listCap[0]="tables";
        listCap[1]="availability";                  
        listCap[2]="capabilities";
        listCap[3]="";
        for(int i = 0; i< 4 ; i++){
             Element cap = null;
             if(i == 3){
              cap = addChildCap(eleCap,"capability", null,"standardID","ivo://ivoa.net/std/TAP",null);
              Element lang = addChildCap(cap,"language",null,null,null, null);
                      addChildCap(lang,"name","ADQL",null,null, null);
                      addChildCap(lang,"version","2.0","ivo-id","ivo://ivoa.net/ADQL/2.0", null);
                      //lver.setAttribute("ivo-id","ivo://ivoa.net/ADQL/2.0"); 
                      addChildCap(lang,"description","Astronomical data query language",null,null, null);
             }
             else
              cap = addChildCap(eleCap,"capability", null,"standardID","ivo://ivoa.net/std/VOSI#"+listCap[i],null);
             
             Element capAcc = addChildCap(cap,"interface",null,"type","vod:ParamHTTP", xsi);
             addChildCap(capAcc,"accessURL",baseUrl+listCap[i],"use","full",null);
         }
         
         document.addContent(eleCap);
         return document;
     }
     
     
//     String newJob = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
//                 + "<vos:transfer xmlns:vos=\"http://www.ivoa.net/xml/VOSpace/v2.0\">"
//                 + "<vos:target>vos://edu.pha!vospace/tapdata1/testfromtap2</vos:target>"
//                 + "<vos:direction>pullToVoSpace</vos:direction>"
//                 + "<vos:view>ivo://ivoa.net/vospace/core#fits</vos:view>"
//                 + "<vos:protocol uri=\"ivo://ivoa.net/vospace/core#httpget\">"
//                 + "<vos:protocolEndpoint>http://azure.pha.jhu.edu:8080/sdss/tap/async/a8cb1f05-9560-498f-a692-a6ff448798c8/results/result</vos:protocolEndpoint>"
//                 + "</vos:protocol></vos:transfer>";
     
    public String xmlVospace(String nodeurl, String resulturl,String direction) throws IOException
    {
        Namespace vospace = Namespace.getNamespace("vos", LoadProperties.propMain.getProperty("uri.vospace"));

        org.jdom.output.XMLOutputter out = new org.jdom.output.XMLOutputter(org.jdom.output.Format.getPrettyFormat());
        java.io.StringWriter sw = new java.io.StringWriter();
        
        Element eletransfer = new Element("transfer",vospace.getPrefix(), vospace.getURI());
        
        addChildVospace(eletransfer,"target",nodeurl);
        addChildVospace(eletransfer,"direction",direction);
        addChildVospace(eletransfer,"view","ivo://ivoa.net/vospace/core#fits");
        Element protocol = addChildVospace(eletransfer,"protocol","").setAttribute("uri", "ivo://ivoa.net/vospace/core#httpget");        
        addChildVospace(protocol,"protocolEndpoint",resulturl);
        
        Document document = new Document();
        document.addContent(eletransfer);
        out.output(document,sw);
        return sw.toString();
    }
    
    private Element addChildVospace(Element eleParent, String chdName, String chdText)
    {
        Namespace vospace = Namespace.getNamespace("vos", LoadProperties.propMain.getProperty("uri.vospace"));
        Element ele = new Element(chdName, vospace.getPrefix(),vospace.getURI());
        if (chdText != null && !chdText.equals("")) {
            ele.setText(chdText);
        }
        eleParent.addContent(ele);
        return ele;
    }
}

