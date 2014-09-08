<%@ Import Namespace="System.Web" %>
<%@ Page language="c#" %>
<%
// fill this with your details 
	string Title = "The Virtual Astronomical Observatory (VAO)";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.4 $";

	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "pubpage";
	string selected = "develop";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				

Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
//Server.Execute("../top.aspx" + "?" + Parameters);
%>
<h1 class="left">
	Web Services</h1>
<p>Programmatic interfaces: ?WSDL implemented</p>
<li>
	<a href="registry.asmx">SEARCH services</a>
: Keyword and Predicate
<li>
	<a href="registryadmin.asmx">ADMIN services</a>
:&nbsp; Harvesting
<li>
	<a href="STOAI.asmx">OAI SOAP services</a> :&nbsp; Forms and OAI interface with 
	SOAP
	<UL>
	</UL>
	<h1 class="left">
		Clients of the registry
	</h1>
	<p>The following clients access this Registry and can be found at these links</p>
<li>
	<a href="http://heasarc.gsfc.nasa.gov/vo/">DataScope Service</a>
<li>
	<a href="http://openskyquery.net">OpenSkyQuery</a>
<li>
	<a href="http://skyservice.pha.jhu.edu/develop/vo/mirage/">Mirage</a>
<li>
	<a href="http://skyservice.pha.jhu.edu/develop/vo/ivoa/">Download Manager</a></li>

<h1 class="left">
	Harvesting Schedule</h1>
	<P>
	<br>
	<br>
	<br>
	<br>
	<br>
	&nbsp;</P>
<%
//Server.Execute("bot.aspx");
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>
