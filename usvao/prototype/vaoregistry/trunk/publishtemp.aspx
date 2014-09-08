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
<h2>Welcome to the STScI Publishing Interface</h2>

Please <a href="login.xhtml">log in</a> or <a href="login.xhtml">create a new user</a>.

  
    <!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->
</td><td width="147"></td></tr></table>

<%
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
