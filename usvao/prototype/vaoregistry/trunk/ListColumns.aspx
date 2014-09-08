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
<h2>VO Directory Metadata Tags Available for Advanced Search</h2>
<p>The following list includes the tag names that may be used in the custom predicate 
to formulate queries to the Directory.  See example queries to the right of the tag listing.
</p><br />

<table cellpadding="5">
<tr><th align="left">Tag</th><th align="left">Data Type</th><th></th><th align="left"><font color="gray">Example</font></th></tr><tr><td></td><td></td></tr>
<tr><td>title</td><td>string</td><td></td><td><font color="gray">title like '%galex%'<br /></font></td></tr>
<tr><td>shortName</td><td>string</td><td></td><td><font color="gray">shortname like 'galex'<br /></font></td></tr>
<tr><td>identifier</td><td>string</td><td></td><td><font color="gray">identifier = 'ivo://mast.stsci.edu/ssap/galex'<br /></font></td></tr>
<tr><td>xsi_type</td><td>string</td><td></td><td></td></tr>
<tr><td>[curation/publisher]</td><td>string</td><td></td><td></td></tr>
<tr><td>[curation/publisher/@ivo-id]</td><td>string</td><td></td><td></td></tr>
<tr><td>[curation/version]</td><td>string</td><td></td><td></td></tr>
<tr><td>[curation/subject]</td><td>string</td><td></td><td></td></tr>
<tr><td>[content/description]</td><td>string</td><td></td><td></td></tr>
<tr><td>[content/source]</td><td>string</td><td></td><td></td></tr>
<tr><td>[content/source/@format]</td><td>string</td><td></td><td></td></tr>
<tr><td>[content/referenceURL]</td><td>string</td><td></td><td></td></tr>
<tr><td>[content/type]</td><td>string</td><td></td><td></td></tr>
<tr><td>[content/contentLevel]</td><td>string</td><td></td><td></td></tr>
<tr><td>[coverage/footprint]</td><td>string</td><td></td><td></td></tr>
<tr><td>[coverage/footprint/@ivo-id]</td><td>string</td><td></td><td></td></tr>
<tr><td>[coverage/waveband]</td><td>string</td><td></td><td></td></tr>
<tr><td>[coverage/regionOfRegard]</td><td>float</td><td></td><td></td></tr>
<tr><td>rights</td><td>string</td><td></td><td></td></tr>
<tr><td>[@created]</td><td>datetime</td><td></td><td><font color="gray">[@created] > '2009-01-01'<br /></font></td></tr>
<tr><td>[@updated]</td><td>datetime</td><td></td><td></td></tr>
<!--tr><td>validationLevel</td><td>int</td><td></td><td></td></tr-->
<tr><td>xml</td><td>string</td><td></td><td></td></tr>

</table>
    
    <!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->
</td><td width="147"></td></tr></table>

<%
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
