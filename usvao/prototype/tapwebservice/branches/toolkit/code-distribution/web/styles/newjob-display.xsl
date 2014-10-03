<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : job-display.xsl
    Created on : March 4, 2011, 4:07 PM
    Author     : deoyani
    Description:
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
                <title>New Job</title>
                <script type="text/javascript" src="../js/lib/yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
                <script type="text/javascript" src="../js/lib/yui/build/yahoo/yahoo.js"></script>
                <script type="text/javascript" src="../js/lib/yui/build/event/event.js"></script>
                <script type="text/javascript" src="../js/lib/yui/build/connection/connection.js"></script>
                <script type="text/javascript" src="../js/lib/yui/build/json/json-min.js"></script>
                <script type="text/javascript" src="../js/lib/yui/build/element/element-beta.js"></script>

            <script type="text/javascript" src="../js/testjs.js"></script>
            <style type="text/css">            
            table
            {
                width:50%;            
            }
            </style>
            </head>

                <body>
                <h2>Your Job details</h2>
                <table border="1">
                <tr>
                    <td bgcolor="#ADDFFF">JobId</td>
                    <td id="jobidValue">
                    <xsl:value-of select="uws:job/uws:jobId"/></td>
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
                    <td><textarea rows="1" cols="40" id="destructText" >
                        <xsl:value-of select="uws:job/uws:destruction"/>
                    </textarea>
                    change the destruction time if required.
                        </td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Parameters</td>
                    <td>Request: doQuery
                    <p>Lang: <input type="text" id="langText" size="20" value="ADQL" >ADQL</input>
                         Option: SQL</p>
                    <p>Query: <textarea rows="2" cols="40" id="queryText">
                    </textarea>
                    <input type="button" value="Submit Query!">
                    <xsl:attribute name="onclick">javascript: simplesubmit('<xsl:value-of select="uws:job/uws:jobId"/>')</xsl:attribute>
                    <!--xsl:attribute name="name">Submit Query</xsl:attribute-->
                    </input> <BR/>
                    Example ADQL STC-S query :<BR/>                     
                    SELECT o.ra, o.dec FROM photoobjall as o WHERE 	CONTAINS( 
                    POINT('J2000', o.ra, o.dec), Region('CIRCLE J2000 180 0 0.3')) = 1
                    <BR/>
                    *  Please make sure, you use table aliases everywhere.<BR/>
                    *  Current version of adql parser supports some simple adql-stc queries.<BR/>
                    *  for sdss: use photoobjall instead of photobj when you query (simlarly specobjall).
                    
                    </p>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">MAXREC</td>
                    <td>
                        <input type="text" id="maxrecText" size="20" >ALL</input>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">Results</td>
                    <td><xsl:value-of select="uws:job/uws:results"/><a href="javascript:myfunction()" id='Result'>Result</a></td>
                </tr>
                </table>

                <input type="button" id="listAll" onclick="javascript:openList()" value="List all Jobs!"></input>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet> 






