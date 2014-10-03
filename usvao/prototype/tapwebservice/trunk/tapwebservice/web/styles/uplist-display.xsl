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
                <title>List of uploaded tables</title>                
                <script language="javascript" src="../js/testjs.js"></script>
            </head>
            <body onload="">
                <h2> UploadList details</h2>
                
                <table>
                <tr>
                    <td bgcolor="#ADDFFF">Uploaded Table</td>
                    <td bgcolor="#ADDFFF">Uplaod Status</td>
                </tr>
                <xsl:for-each select="uws:uploads/uws:upref">              
                
                <tr>
                    <td>                     
                      <xsl:value-of select="@uploadtable"/>
                    </td>
                    <td><xsl:value-of select="uws:uploadstatus"/></td>
                </tr>
                </xsl:for-each>
                </table>

            
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
