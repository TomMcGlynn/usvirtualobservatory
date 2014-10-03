<%-- 
    Document   : index
    Created on : Feb 10, 2011, 10:35:16 AM
    Author     : deoyani nandrekar-heinis
--%>

<%@page import="java.util.Calendar"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="javax.servlet.*" %>

<%
        
        String t = request.getRequestURL().toString();
        String path = t.substring(0, t.lastIndexOf("/"));        
        String webServiceLink =path;
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JHU TAP Home</title>
    </head>
    <body>
        <h1>TAP webservice for SDSS </h1>
        <p> This service implements the IVOA TAP  recommendation and exposes the SDSS tables. 
        <Br> Details of Tables supported are in TAP_SCHEMA which can be accessed by the 'tables' resource.        
        </p>
        <br/>
        Details of service usage is at the end of this document. 
        <br/>
         
         <b>Main Resources:</b>
         <br/>
         <table border="1px" >
             <tr border="1px;Black">
                 <td>
                     Resources
                 </td>
                 <td>
                     Description
                 </td>
             </tr>
             <tr border="2px">
                 <td>
                     <a href="tap/async"><%=webServiceLink%>/tap/async</a>
                 </td>
                 <td>
                     TAP Asychronous resource.(GET/POST implemented) It lists jobs , allows to submit new job.
                     has many subresources to access details job information.
                     *here job = query submitted, is associated with unique ID
                 </td>
             </tr>
             <tr border="2px">
                 <td>
                     <a href="tap/sync"><%=webServiceLink%>/tap/sync</a>
                 </td>
                 <td>
                     TAP Sychronous resource. (GET/POST method) For small queries. Returns result after query execution
                 </td>
             </tr>
             <tr border="2px">
                 <td>
                     <a href="tap/availability"><%=webServiceLink%>/tap/availability</a>
                 </td>
                 <td>
                     VOSI availability for this TAP service
                 </td>
             </tr>
             <tr border="2px">
                 <td>
                     <a href="tap/capabilities"><%=webServiceLink%>/tap/capabilities</a>
                 </td>
                 <td>
                     VOSI capabilities for this TAP service
                 </td>
             </tr>
             <tr border="2px">
                 <td>
                     <a href="tap/tables"><%=webServiceLink%>/tap/tables</a>
                 </td>
                 <td>
                     VOSI standard to list tables supported for this TAP service
                     TAP_SCHEMA  is implemented. 
                 </td>
             </tr>
         </table>
<p>
 Other IVOA standards:<br>
 The Query language supported are  ADQL (and  SQL).<br/>
 Output in VOTable and CSV format.</p>
<b>Sample queries:</b><br/>
    REQUEST=doQuery<br/>
    LANG=ADQL <br/>
    QUERY= SELECT TOP 10 p.objid,p.ra,p.dec,p.u,p.g,p.r,p.i,p.z FROM PhotoObj as p
           WHERE p.u BETWEEN 0 AND 19.6 <br/> <br/>
    Click here for sync query:       
    <a href="<%=webServiceLink%>/sync?REQUEST=doQuery&LANG=SQL&QUERY=SELECT TOP 10 p.objid,p.ra,p.dec,p.u,p.g,p.r,p.i,p.z FROM PhotoObj as p WHERE  p.u BETWEEN 0 AND 19.6  "><%=webServiceLink%>/sync?REQUEST=doQuery&LANG=SQL&QUERY=SELECT TOP 10 p.objid,p.ra,p.dec,p.u,p.g,p.r,p.i,p.z FROM PhotoObj as p WHERE  p.u BETWEEN 0 AND 19.6</a>
    
    
    <br/> <br/>  
    <b>Sample ADQL spatial query and sdss sql equivalent:</b><br/> Adql:
    SELECT o.ra, o.dec FROM photoobjall as o WHERE CONTAINS( POINT('J2000', o.ra, o.dec), Region('CIRCLE J2000 180 0 0.3')) = 1 
    <br/>Equivalent sql:
    
    <br/>DECLARE @HTMTEMP TABLE (HTMIDSTART BIGINT, HTMIDEND BIGINT);                           
   <br/>INSERT @HTMTEMP SELECT TOP 1000000 HTMIDSTART, HTMIDEND FROM DBO.FSPHGETHTMRANGES(DBO.FSPHSIMPLIFYSTRING('CIRCLE J2000 180 0 0.3'))
   <br/>SELECT TOP 1000000 O.RA, O.DEC FROM PHOTOOBJALL AS O  INNER JOIN  @HTMTEMP H ON O.HTMID BETWEEN H.HTMIDSTART AND H.HTMIDEND  
   <br/>WHERE  DBO.FSPHREGIONCONTAINSXYZ(DBO.FSPHSIMPLIFYSTRING('CIRCLE J2000 180 0 0.3'), O.CX ,O.CY,O.CZ)   = 1 
    <br/>
    <br/>
    <table border="1px">
        <tr>
            <td>Resources</td><td>Status</td>
        </tr>
   
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/phase"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/phase</a> </td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/executionduration"> <%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/executionduration</a></td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/destruction"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/destruction</a> </td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/error" ><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/error</a> </td> <td>implemented --</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/parameters"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/parameters</a> </td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/results/result"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/results/result</a> </td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/starttime"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/starttime</a> </td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/endtime"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/endtime</a> </td> <td>implemented</td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/owner"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/owner</a></td> <td>we dont need it right now for SDSS </td> </tr>
        <tr><td><a href="<%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/quote"><%=webServiceLink%>/async/62701d03-2e12-4089-83f4-8ec5a443b0f9/quote</a> </td> <td> NOT implemented </td> </tr>
    </table>
    <br/>
    <b>Service usage Instructions in brief</b>
    <p>
        For Async Queries
        * Submit new job using link on joblist page ( <a href="<%=webServiceLink%>/newasync.jsp"><%=webServiceLink%>/newasync.jsp</a>)<br/>
        * After giving proper input parameters click on 'Submit Query' button<br/>
        * It calls web service for given parameters<br/>
        * Once job is submitted it will show job status 'PENDING'<br/>
        * use /phase resource to RUN job<br/>
        * use /<jobid> resource to check job status and other related info<br/>                        
    </p>
    <p>
        For Sync Queries <br/>
        <a href="<%=webServiceLink%>/newsync.jsp"><%=webServiceLink%>/newsync.jsp</a>
    </p>
    </body>
</html>
