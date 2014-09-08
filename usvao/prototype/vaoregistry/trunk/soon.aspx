<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Data" %>
<%@ Import Namespace="registry" %>
<%@ Page  Language="c#" AutoEventWireup="false" %>
			<%
Server.Execute("top.aspx?message=Virtual Astronomical Observatory Searchable Registry");

						
%>
			<br>
			<table width="800" align="top" border="0"  >
				<tr>
				<td>
				<h1> Coming soon !</h1>
				</td></tr>
					<%
	string page = Request.Params["PATH_TRANSLATED"];
	string htmlRevision = "";
	if (null !=page) {

		FileInfo f = new FileInfo(page);
		htmlRevision = f.LastWriteTime.ToLongDateString() + " at " + f.LastWriteTime.ToLongTimeString();
	}
%> 
<tr><td>
			<p class="tiny" align=center>
				<a href="mailto:feedback@us-vo.org">Contact US-VO Help Desk for Problems and Suggestions </a>
				<br>
				Last Modified :<%=htmlRevision%>
				, $Name:  $, $Revision: 1.1.1.1 $
			</p>
</td></tr>


</table>
</HTML>
