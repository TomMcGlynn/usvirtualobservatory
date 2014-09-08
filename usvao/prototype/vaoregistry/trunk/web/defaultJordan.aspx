<%@ Import Namespace="System.Web" %>
<%@ Page language="c#"  %>
<% // fill this with your details 
	string Title = "The National Virtual Observatory (NVO)";
	string author ="Jordan Raddick";
	string email ="raddick@jhu.edu";
	string cvsRevision = "$Revision: 1.1 $";

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
	Server.Execute("SkyHeader.aspx" + "?" + Parameters);
%>

<table width=200 align=right>
  <tr>
    <td><img src="images/m81s2.jpg" align=right></td>
  </tr>
  <tr>
    <td align=center><p class=caption>Galaxy M81 seen by a visible-light telescope</p></td>
  </tr>
</table>

<p>Welcome to the National Virtual Observatory!</p>

<p>The NVO is a revolutionary new astronomy project. It will develop a set of online tools to 
link all the world's astronomy data together, giving people all over the world easy access to 
data from many different instruments, at all wavelengths of the electromagnetic spectrum from 
radio to gamma rays.</p>

<p>This site is a gateway to all the ways that the NVO will make its data available for education 
and public outreach. The NVO will not create education and outreach resources of its own; instead, it 
will help partner programs use NVO tools to access data that will improve their products.</p>

<p>Click one of the buttons above to find out how the NVO and its education and outreach partners can 
help you. To find out more about how what the NVO is, click one of the links on the left. To search the 
site or contact the NVO outreach staff, click one of the links below.</p>

<p class="tiny">M81 image courtesy of Jonathan Irwin, DSS2</p>

<% // will need to fix path if we have sub dirs .. 
	Server.Execute("SkyFooter.aspx" + "?" + Parameters);
%>