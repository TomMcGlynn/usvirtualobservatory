<%@ Import Namespace="System.Web" %>
<%@ Page language="c#" CodeBehind="projects.aspx.cs" AutoEventWireup="false" Inherits="registry.projects" %>
<%
// fill this with your details 
	string Title = "The Virtual Astronomical Observatory (VAO)";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.5 $";

	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "pubpage";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				

Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
//Server.Execute("../top.aspx" + "?" + Parameters);
%>
<h1 class="left">IVOA Registry Projects</h1>
<p>The following list shows related Virtual Observatory registry activities</p>
<h2 class="left">Full Searchable Registries</h2>
<P></P>
<li>
	<a href="http://nvo.stsci.edu/VOR10/keywordsearch.aspx">Space Telescope Science 
		Institute (STScI) Registry</a>
<li>
	<a href="http://voservices.net/registry/">STScI Mirror at Johns Hopkins University 
		(JHU)</a>
<li>
	<a href="http://esavo.esa.int/registry/">ESAVO Full Registry</a>
<li>
	<a href="http://nvo.caltech.edu:8080/carnivore/">Carnivore at Caltech</a>
(XML based )
<LI>
	<a href="http://galahad.star.le.ac.uk:8081/astrogrid-registry/">Astrogrid</a>
	<h2>Publishing Registries <a href="http://www.openarchives.org/"><FONT size="3">following 
				Open Archive Initiatives (OAI) protocol</FONT></a>
	</h2>
<li>
	<a href="http://nvo.ncsa.uiuc.edu/nvoregistration.html">NCSA Publishing Registry</a>
<LI>
	<a href="http://heasarc.gsfc.nasa.gov/cgi-bin/OAI-XMLFile-2.1/XMLFile/nvo/oai.pl?verb=ListRecords&amp;metadataPrefix=ivo_vor">
		HEASARC Publishing Registry</a>
<li>
	<a href="http://jvo.nao.ac.jp/publishingRegistry/">JVO (Japan Virtual Observatory)</a>
<LI>
	<a href="http://vizier.u-strasbg.fr/cgi-bin/registry/vizier/oai_v0.10.pl?verb=ListRecords&amp;metadataPrefix=ivo_vor">
		CDS Vizier Publishing Registry </a>
	<br>
	<br>
	<br>
	<br>
	<br>
	<%
//Server.Execute("bot.aspx");
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>
	</A>
</LI>
