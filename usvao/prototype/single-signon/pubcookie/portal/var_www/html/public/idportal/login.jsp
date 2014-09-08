<!-- ===============================================================
  -  CONFIGURE:
  -     I'm not expecting any configuration to be necessary here.
  -     (Maybe the form action needs changing?)
  -
  - ================================================================ -->
<%@ page import="org.w3c.tidy.servlet.util.HTMLEncode" %>
<%@ page import="java.util.regex.*" %>
<html>
<head>
<title>NVO User Login</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="usvo3.css" type="text/css" rel="stylesheet">
<style type="text/css">
    .tiny		{FONT-SIZE: 7pt;}
    .tinylink	{FONT-SIZE: 7pt; COLOR:#aaaaff;}
    .menuhead	{MARGIN-TOP: 2px; MARGIN-BOTTOM: 2px;
                 PADDING-LEFT:6px; PADDING-TOP:6px; FONT-SIZE:10pt; 
                 FONT-WEIGHT: 800; COLOR:#FFFFFF; WIDTH:128px; HEIGHT:26px;
    	         BACKGROUND-IMAGE:url(NVO-hbar-128.gif);}
    .menulink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:8pt;}
    .boxitem	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:8pt; 
                 BACKGROUND-COLOR:#DDDDEF; }
    .navilink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:7pt; 
                BACKGROUND-COLOR:#7777aa;}
    .navilink A	{TEXT-DECORATION:none;COLOR:#FFFFFF;}
    .navilink A:hover { COLOR: #ffff00; }
    .section	{MARGIN-TOP: 2px; MARGIN-BOTTOM: 2px; BACKGROUND-COLOR:#DDDDDD;}
     .searchbox		{WIDTH: 85px;}
    p 		{MARGIN-TOP: 0px; MARGIN-BOTTOM: 3px;}

    #header	{POSITION: absolute; TOP:  0px; LEFT:  2px;}
    #search	{POSITION: absolute; TOP: 26px; LEFT:648px; WIDTH:160PX;}
    #navibar	{MARGIN-TOP: 0px;POSITION: absolute; TOP: 96px; LEFT:0px; 
                 WIDTH: 450px; PADDING:0px }
    #menubar	{POSITION: absolute; TOP: 96px; LEFT:  0px; WIDTH: 132px; 
                 PADDING:4px; BACKGROUND-COLOR:#EEEEEE;}
    #features	{POSITION: absolute; TOP:128px; LEFT:646px; WIDTH: 132px;}
    #main	{POSITION: absolute; TOP:128px; LEFT:5px; PADDING:8px;}		
    .th         {font-weight:800; background-color:#DDDDEF;}
    .td         {background-color:#EEEEEE;}
</style>
<meta content="Microsoft FrontPage 4.0" name="GENERATOR">
<!-- InstanceParam name="id" type="text" value="main" -->
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>

<body leftmargin="0" topmargin="0">

<div id="header">
<a href="http://us-vo.org/index.cfm"><IMG src="NVO-header-3.gif" 
   width="792" height="116" border="0"></a>
</div>

<div id="navibar">
    <table width="640" cellpadding="2" cellspacing="2"> <tr>
    <td align="middle" class="navilink"><a href="http://us-vo.org/index.cfm">Home</a></td>
    <td align="middle" class="navilink"><a href="http://us-vo.org/getting_started/index.cfm">Getting Started </a></td>
    <td align="middle" class="navilink"><a href="http://us-vo.org/projects/tools.cfm">Tools</a></td>

    <td align="middle" class="navilink"><a href="http://us-vo.org/projects/dataserv.cfm">Data </a></td>
    <td align="middle" class="navilink"><a href="http://us-vo.org/publish.cfm">Publish</a></td>
    <td align="middle" class="navilink"><a href="http://us-vo.org/software/index.cfm">Software Library</a></td>
    <td align="middle" class="navilink"><a href="http://www.virtualobservatory.org">Education</a></td>
    <td align="middle" class="navilink"><a href="http://www.us-vo.org/pubs/index.cfm">Documents</a></td>
    <td width="60"><span class="navilink"></span></td>

    <td align="middle" class="navilink"><a href="mailto:feedback@us-vo.org">Contact Us </a></td>
    </tr></table>
</div>

<!--
<div id="menubar">
    <p class='menuhead'>About</p>

    <p class='menulink'><a href="http://www.us-vo.org/about.cfm">What is the NVO?</a></p>
    <p class='menulink'><a href="http://www.us-vo.org/personnel.cfm">Who is Involved?</a></p>

    <p class='menulink'><a href="http://www.us-vo.org/objectives.cfm">Science 
            Objectives</a><br>
    <a href="http://www.us-vo.org/projects/voscience.cfm">NVO in Use</a>    </p>
    <p class='menulink'><a href="http://www.us-vo.org/grid.cfm">Grid Computing</a></p>
    <p class='menulink'><a href="http://www.us-vo.org/architecture.cfm">Architecture</a></p>
    <p class='menuhead'>News</p>
    
    <p class="menulink">
        <a href="news/story.cfm?ID=29">NVO Research Initiative -
        Proposal Selection</a>  
    </p>
  
    <p class="menulink"><a href="http://us-vo.org/news/story.cfm?ID=28">VOEvent II Workshop</a>
    </p>
    <p class="menulink"><a href="http://us-vo.org/news/index.cfm">NVO News Archive </a></p>
    <p class='menuhead'>Community</p>
    
    <p class='menulink'><a href="http://us-vo.org/meetings/index.cfm">NVO Meetings</a></p>
    <p class='menulink'><a href="http://www.ivoa.net">International VO
    Alliance</a></p> 

    <p class='menulink'><a href="http://us-vo.org/summer-school/index.cfm">NVO Summer
    School</a></p> 
    <p class='menulink'>&nbsp;</p>
    <p align="left" class="menulink"><a href="http://us-vo.org/ack.cfm"> Acknowledging
    NVO </a><br> 
    </p>
    <table width="120">
    <tr><td align="middle" class="tiny">
        <a href="http://www.nsf.gov/"><img
           src="nsflogo_64x.gif" width="64" height="64" border="0">
        </a><br>
        Supported by the <br><a href="http://www.nsf.gov/">National
        Science Foundation</a> 
    </td></tr>
    <tr><td>&nbsp;</td></tr>
    <tr><td align="middle" class="tiny">    
        <a href="http://www.ivoa.net/"><img height="44" 
           src="IVOAlogo.gif" width="80" border="0"></a><br> 
        Member of the International <br> Virtual Observatory Alliance
    </td></tr>
  </table>

</div>
-->

<div id="main">

<%
    final String nvologin = "http://nvologin.ncsa.uiuc.edu";
    String referer = request.getHeader("Referer");
    String portal = request.getParameter("portalName");
    if (portal != null && portal.length() > 0) {
        portal = HTMLEncode.encode(portal);
    }
    else if (referer != null) {
        Pattern sitere = Pattern.compile(":(//)?([^/]*)");
        Matcher mchr = sitere.matcher(referer);
        if (mchr.find()) {
            portal = HTMLEncode.encode(referer.substring(mchr.start(2),
                                                         mchr.end(2)));
        } else {
            portal = HTMLEncode.encode(referer);
        }
    }
    else {
        portal = "the NVO Login Portal";
        referer = nvologin;
    }
%>

<h1>Log Into the Virtual Observatory via <br>
    <%= portal %> </h1>

<p>
<table align="center" border="0" cellpadding="0" cellspacing="0">
  <tr><th>
<font color="green">Upon successful login, secure access to your identity
credentials will be granted temporarily to <%= referer %></font>
  </th></tr>
</table>

<form method="post" action="authenticate.jsp">

<blockquote>
<table border="0" cellspacing="0" cellpadding="0">
<tr><td>Username:</td> <td> &nbsp;&nbsp; </td>
    <td><input type="text" name="username" size="16"></td></tr>
<tr><td>Password:</td> <td> &nbsp;&nbsp; </td>
    <td><input type="password" name="password" size="16" value=""></td></tr>
</table><br>
<input type="submit" value="Login">
</blockquote>
</form>

<strong>Note:</strong> In order to help ensure the safety of your VO
identity, only trusted, registered portals are allowed to secure
access to your credentials through this login page.  <em>You should
never enter your NVO login password into any login page except this
one.</em> For more information about how NVO Logons work with portals,
see our page on <a href="/AboutNVOLogons.html">NVO Logons</a>.
</p>

</div>
<HR WIDTH="80%" SIZE="1" NOSHADE ALIGN=left>

<table border="0" cellpadding="2" cellspacing="2" width="800">
   <tr>
     <td>
      <p class="tiny"> The NVO web site is a community-maintained
collection with content control by the NVO Executive Committee.&nbsp;
Content is judged by the extent to which it: (a) reflects an aspect of
the Virtual Observatory, such as astronomy with federated data, (b)
uses VO standards or software, or (c) exemplifies grid-based
astronomical computing.&nbsp; If you would like a description of your
project, data, or software included here, please write to web at
us-vo.org with a short description of your work. </p> </td>  
    </tr>
  </table>

</body>
</html>
