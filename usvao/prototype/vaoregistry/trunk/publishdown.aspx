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
<h2>Publishing Interface for Registry v1.0 In Testing</h2>

The publishing interface for STScI's Registry, compliant to the IVOA Registry Interface 1.0 standard specification and designed
in accordance with the Universal Publishing Interface documentation, is currently in beta. <br /><br /> If you would like to publish resources directly to The VAO Registry at STScI/JHU, or if you have previously published
resources to The VAO Registry at STScI/JHU and would like to update them, you may use this publishing service, hosted 
<a href="http://nvoprod.stsci.edu/publishing">here</a>.  In order to gain access to old records you have published directly to the STScI/JHU
registry for editing, contact Theresa Dower at <a href="mailto:dower at stsci.edu">dower at stsci.edu</a>.
<br /><br />

New records for STScI's registry version 1.0 can still be published indirectly through a publishing registry at
<a href="http://nvo.ncsa.uiuc.edu/nvoregistration.html">NCSA</a>.<br /><br />

<h2>Links</h2><ul>
<li><a href="http://us-vo.org/pubs/files/PublishHowTo.html">Basic Publishing Overview</a></li>
<li><a href="http://nvoprod.stsci.edu/publishing">Beta publishing interface</a></li>
<li><a href="http://nvo.ncsa.uiuc.edu/nvoregistration.html">NCSA publishing interface</a></li>
</ul>

    
    <!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->
</td><td width="147"></td></tr></table>

<%
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
