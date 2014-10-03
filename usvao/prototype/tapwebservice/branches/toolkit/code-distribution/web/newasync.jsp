<%-- 
    Document   : newasync
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
                <form action="/oamp/tap/async" method="POST">
                 <input type="hidden" name="REQUEST" value="doQuery">      
                    
                <h2>Your Job details (Asynchronous query)</h2>
                <table border="1">
                <tr>
                    <td bgcolor="#ADDFFF">JobId</td>
                    <td id="jobidValue"> </td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">Phase</td>
                    <td></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Start Time</td>
                    <td></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">End Time</td>
                    <td></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Duration</td>
                    <td></td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">Destruction</td>
                    <td> </td>
                </tr>
                 <tr>
                    
                    <td bgcolor="#ADDFFF">Parameters</td>
                    <td>Request: doQuery
                    <p>Lang: <input type="text" name="LANG" size="20" value="ADQL" >ADQL</input>Option: SQL</p>
                    <p>Query: <textarea rows="2" cols="40" name="QUERY">
                    </textarea>
                    <input type="submit" id="submit" value="Submit Query!">
                    </input> <BR/>
                    Example ADQL STC-S query :<BR/>                     
                    SELECT o.ra, o.dec FROM photoobjall as o WHERE 	CONTAINS( 
                    POINT('J2000', o.ra, o.dec), Region('CIRCLE J2000 180 0 0.3')) = 1
                    <BR/>
                    *  Please make sure, you use table aliases everywhere.<BR/>
                    *  Current version of adql parser supports some simple adql-stc queries.<BR/>
                    *  Use photoobjall instead of photobj when you query (simlarly specobjall).
                    
                    </p>
                    </td>
                </tr>
                 <tr>
                    <td bgcolor="#ADDFFF">FORMAT</td>
                    <td>
                        <input type="text" id="FORMAT" name="FORMAT" size="20"  value="VOTABLE" />  Option:CSV
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">MAXREC</td>
                    <td>
                        <input type="text" id="maxrecText" name="MAXREC" size="20" value="" ></input>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#ADDFFF">Results</td>
                    <td></td>
                </tr>
                </table>
                <script type="text/javascript">
                    function openList(){
                        var url = document.location.href ;
                        url = url.substr(0,url.lastIndexOf("/"));
                        window.open(url+"/tap/async", "_self");
                    }
                </script>

                <input type="button" id="listAll" onclick="javascript:openList()" value="List all Jobs!"></input>
                </form>
            </body>
        </html>
  







