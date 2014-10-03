<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : joblist-display.xsl
    Created on : March 8, 2011, 3:02 PM
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
                <title>List of Jobs</title>                
                <script language="javascript" src="../js/testjs.js"></script>
            </head>
            <body onload="">
                <h2> JobList details</h2>
                <input type="button" value="New Job!!" onclick="newJobPage()" />
                <table>
                <tr>
                    <td bgcolor="#ADDFFF">JobId</td>
                    <td bgcolor="#ADDFFF">Phase</td>
                </tr>
                <xsl:for-each select="uws:jobs/uws:jobref">              
                
                <tr>
                    <td>
                      <xsl:attribute name="onclick">javascript:showJob('<xsl:value-of select="@id"/>')</xsl:attribute>
                      <xsl:value-of select="@id"/>
                    </td>
                    <td><xsl:value-of select="uws:phase"/></td>
                </tr>
                </xsl:for-each>
                </table>

               <script language="javascript">
  
<!--                function newJob(){
                     var url = document.location.href ;
                     url = url.substr(0,url.lastIndexOf("/")+1);                   
                     window.open(url+"async?newjob=newjob","_self");
                }-->
                </script>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
