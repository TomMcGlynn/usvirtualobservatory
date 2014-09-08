<%@ Page language="c#" CodeBehind="contact.aspx.cs" AutoEventWireup="false" Inherits="registry.contact" %>
<%@ Import Namespace="System.Web" %>
<% // fill this with your details 
	string Title = "NVO - Contact Us";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.4 $";
	
	string path = "";
	
	string bgcolor = "#FF0000";
	string displayTitle = "";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				
%>
<% // will need to fix path if we have sub dirs .. 
	Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
%>
<h1>Contact Us</h1>
<h2>NVO STScI Registry Developer</h2>
<p>
	<b>Gretchen Greene</b><br>
	Operations and Data Management<br>
	Space Telescope Science Institute<br>
	3700 San Martin Dr.<br>
	Baltimore, MD 21218<br>
	(410) 338-4852
</p>
<table width="200" border="0" align="left">
	<tr>
		<td><img src="web/images/gretchen.jpg"></td>
	</tr>
</table>
<p><a href="mailto:greene@stsci.edu">greene@stsci.edu</a></p>
<p><br>
	&nbsp;</p>
<h2>&nbsp;</h2>
<H2>Virtual Observatory Project Manager</H2>
<p>
	<b>Dr. Robert Hanisch</b><br>
	Operations and Data Management<br>
	Space Telescope Science Institute<br>
	3700 San Martin Dr.<br>
	Baltimore, MD 21218<br>
	(410) 338-4910
</p>
<table width="200" border="0" align="left">
	<tr>
		<td><img src="web/images/hanisch.jpg"></td>
	</tr>
</table>
<p><a href="mailto:hanisch@stsci.edu">hanisch@stsci.edu</a></p>
<% // will need to fix path if we have sub dirs .. 
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>
