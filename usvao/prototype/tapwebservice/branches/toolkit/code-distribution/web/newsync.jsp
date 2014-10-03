<%-- 
    Document   : newjsp
    Created on : Jun 6, 2011, 3:32:17 PM
    Author     : deoyani
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ page import="javax.servlet.*" %>

<%   
        String t = request.getRequestURL().toString();
        String path = t.substring(0, t.lastIndexOf("/")+1);
        String webServiceLink =path;
%>
<!DOCTYPE html>

        <html>
            <head>
                <title>New Job</title>
              
                <script type="text/javascript" src="/js/testjs.js"></script>
                <style type="text/css">            
            table
            {
                width:50%;            
            }
            </style>
            </head>

                <body>
                <form action="/oamp/tap/sync" method="POST">
                 <input type="hidden" name="REQUEST" value="doQuery">      
                    
                <h3>Enter Query:(Synchronous query)</h3>
                <table border="1">
               
                 <tr>
                    <td bgcolor="#ADDFFF">Parameters</td>
                    <td>
                    <table> 
                    <tr>
                    <td>Request:</td><td> doQuery</td>
                    </tr>
                    <tr>
                    <td>Lang:</td><td> <input type="text" name="LANG" size="20" value="ADQL" /> Option: SQL </td>
                    </tr>
                    <tr>
                    <td>Query:</td><td> <textarea rows="2" cols="40" name="QUERY"></textarea>                         
                    <input type="submit" id="submit" value="Submit Query!"/></td>
                    </tr>
                    </table> 
                    </td>    
                 </tr>
                    <!--xsl:attribute name="name">Submit Query</xsl:attribute-->
                   
                    
                    
                    
                   
                <tr>
                    <td bgcolor="#ADDFFF">FORMAT</td>
                    <td>
                        <input type="text" id="FORMAT" name="FORMAT" size="20" value="VOTABLE" /> Option:CSV
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">MAXREC</td>
                    <td>
                        <input type="text" id="maxrecText" size="20" name="MAXREC" ></input>
                    </td>
                </tr>
                
               
                </table>                       
                    Example ADQL STC-S query :<BR/>                     
                    SELECT o.ra, o.dec FROM photoobjall as o WHERE 	CONTAINS( 
                    POINT('J2000', o.ra, o.dec), Region('CIRCLE J2000 180 0 0.3')) = 1
                    <BR/>
                    *  Please make sure, you use table aliases everywhere.<BR/>
                    *  Current version of adql parser supports some simple adql-stc queries.<BR/>
                    *  Use photoobjall instead of photobj when you query (simlarly specobjall).
                </form>
            </body>
        </html>
  







