<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : job-display.xsl
    Created on : March 4, 2011, 4:07 PM
    Author     : deoyani
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:uws="http://www.ivoa.net/xml/UWS/v1.0" xmlns:xlink="http://www.w3.org/1999/xlink" >
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title>Job</title>
                <script type="text/javascript" src="../../js/testjs.js"></script>
                <style type="text/css">            
                table
                {
                    width:50%;            
                }
                </style>
            </head>
            <body onLoad= "">
                
                <h2>Your Job details</h2>
                <table border="1">
                <tr>
                    <td bgcolor="#ADDFFF">JobId</td>
                    <td><xsl:value-of select="uws:job/uws:jobId"/></td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">Phase</td>
                    <td><xsl:value-of select="uws:job/uws:phase"/></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Start Time</td>
                    <td><xsl:value-of select="uws:job/uws:startTime"/></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">End Time</td>
                    <td><xsl:value-of select="uws:job/uws:endTime"/></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Duration</td>
                    <td><xsl:value-of select="uws:job/uws:executionDuration"/></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Destruction</td>
                    <td><xsl:value-of select="uws:job/uws:destruction"/></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Parameters</td>
                    <td>Request: <xsl:value-of select="uws:job/uws:parameters/uws:parameter[@id='REQUEST']"/>
                    <p>Lang: <xsl:value-of select="uws:job/uws:parameters/uws:parameter[@id='LANG']"/></p>
                    <p>Query:<BR/> <xsl:value-of select="uws:job/uws:parameters/uws:parameter[@id='QUERY']"/></p>
                    <p>UPLOAD:<BR/> <xsl:value-of select="uws:job/uws:parameters/uws:parameter[@id='UPLOAD']"/></p>
                    </td>
                </tr>
                <script type="text/javascript">
                    var jobid = "<xsl:value-of select="uws:job/uws:jobId"/>";               
                </script>
                <tr>
                    <td bgcolor="#ADDFFF">Results</td>
                    <td>
<!--                        <xsl:value-of select="uws:job/uws:results[@xlink:href]"/>-->
<!--                    <xsl:template match="*[@xlink:href]">-->
                        
<!--                        <a href="{@xlink:href}">-->
<!--                        <xsl:apply-templates />-->
                        <!--
                        <xsl:value-of select="uws:job/uws:results{@xlink:href}"/>
                        </a>-->
<!--                    </xsl:template>-->
                        <a href="" id='Result' value="Results" onClick="displayResult(jobid)" >
                           <xsl:attribute name="onclick">javascript: displayResult('<xsl:value-of select="uws:job/uws:jobId"/>')</xsl:attribute>
                         Result</a>
                         </td>
                </tr>
                </table>
                <button type="button" onclick="javascript:openList('display')">List all Jobs!!</button>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>






