<%@ Page language="c#" AutoEventWireup="false" %>
<%@ Import Namespace="System.Web" %>
<%
	string Title = "STScI/JHU VO Publishing Resources";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.1 $";
	string cvsTag = "$Name:  $";
	
	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "pubpage";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				


Server.Execute("web/SkyHeader2.aspx" + "?" + Parameters);
//Server.Execute("../top.aspx" + "?" + Parameters);
%>

<!-- =======================================================================
  -  Page Content -->
<!--  -  ======================================================================= -->
        
<table width="100%">
<tr><td width="112"></td>
<td>
<h2>Registration Interface for Directory v1.0 in Testing</h2>

The registration interface for STScI's Directory, compliant to the IVOA Registry Interface 1.0 standard is currently in beta. <br /><br /> If you would like to <span style="color: #cc0000"><strong><span style="color: #009900">
        register resources</span></strong> </span>directly to The VAO Directory at STScI, or if you have previously registered
resources to The VAO Directory at STScI/JHU and would like to update them, you may use this registration service, hosted 
<a href="http://vaotest.stsci.edu/publishing">here</a>.  In order to gain access to old records you have registered directly to the STScI
Directory for editing, contact Theresa Dower at <a href="mailto:dower at stsci.edu">dower at stsci.edu</a>. Note that this publishing interface is still in testing and lacks some advanced features for complex service types.
<br /><br />

<center><a href="http://vaotest.stsci.edu/publishing"><img src="images/publish.gif"  BORDER=0 alt="publish"/></a></center>

<h2>Links</h2><ul>
<li><a href="http://us-vo.org/pubs/files/PublishHowTo.html">Basic Registration/Publishing
    Overview</a></li>
<li><a href="http://vaotest.stsci.edu/publishing">Beta Registration interface</a></li>
<li><a href="http://nvo.ncsa.uiuc.edu/nvoregistration.html">NCSA Registration/Publishing
    &nbsp;interface</a></li>
</ul>

    
    <!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->
</td><td width="147"></td></tr></table>

<%
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
