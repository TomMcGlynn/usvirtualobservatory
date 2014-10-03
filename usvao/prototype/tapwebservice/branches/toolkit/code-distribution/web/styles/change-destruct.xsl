<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : job-display.xsl
    Created on : March 4, 2011, 4:07 PM
    Author     : deoyani
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
xmlns:uws="http://www.ivoa.net/xml/UWS/v1.0" >
    <xsl:output method="html"/>

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title>Job</title>
                <script type="text/javascript" src="../../../js/testjs.js"></script>
                <script type="text/javascript">
                 function submit(){
                        alert("Updated");
                        document.getElementById('destructForm').method = 'POST';
                        document.getElementById('destructForm').action='/<xsl:value-of select="uws:job/uws:jobId"/>/destruction';
                        alert("Updated");
                 }
                    
                </script>
                <style type="text/css">            
                table
                {
                    width:50%;            
                }
                </style>
            </head>
            <body onLoad= "">
                
                <h2>Your Job details</h2>
                <form id="destructForm" method="POST">
                <table border="1">
                <tr>
                    <td bgcolor="#ADDFFF">JobId</td>
                    <td id="jobid"><xsl:value-of select="uws:job/uws:jobId"/></td>
                </tr>                
                <tr>
                    <td bgcolor="#ADDFFF">Destruction</td>
                    <td><textarea rows="1" cols="40" id="DESTRUCTION" name = "DESTRUCTION" >
                        <xsl:value-of select="uws:job/uws:destruction"/>
                        </textarea> 
                        <input type="submit" id="submit" onclick="submit()" value="Change"/>
                    </td>
                </tr>                
                </table>
                </form>
                <button type="button" onclick="javascript:openList('display')">Job Details!!</button>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>






