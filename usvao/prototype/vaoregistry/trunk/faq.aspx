<%@ Import Namespace="System.Web" %>
<%@ Page language="c#" %>
<%
	string Title = "STScI/JHU VO Registry FAQ";
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


Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
//Server.Execute("../top.aspx" + "?" + Parameters);
%>
	<h1 class="left"> Registry FAQs </h1>

				    <p>To find out more information about NVO registries,  check out these FAQs</p>
				    <li>What is a registry?</li>
				    <P class=MsoBodyText style="TEXT-ALIGN: justify">A <I>registry</I> functions as a sort of yellow pages, or high-level directory, of astronomical
catalogs, data archives, data-providing organizations, and computational
services .  You can search the registry to find data of interest, review the
data source descriptions, and in many cases make direct position-based data
requests.  </P>				    

				    <li>What is a resource?</li>
				    <P class=MsoBodyText style="TEXT-ALIGN: justify">A <I>resource</I> is a general 
term referring to a VO (Virtual Observatory) element that can be described in terms of who curates or 
maintains it and which can be given a name and a unique identifier. Just about 
anything can be a resource: it can be an abstract idea, such as sky coverage or 
an instrumental setup, or it can be fairly concrete, like an organization or a 
data collection. This definition is consistent with its use in the general Web 
community as “anything that has an identity” (Berners-Lee 1998, IETF RFC2396). 
We expand on this definition by saying that it is also describable. 

There are several <a href="..\publish.aspx" ><I>types</i></a> of resources published in the VO registries:  standard VO data services (SIAP, Cone, SSAP, and
SkyNodes,  data collections, organizations, other registries are even considered resources.</P>				    
<%
//Server.Execute("bot.aspx");
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>
