<%@ Page language="c#" Codebehind="Publish.aspx.cs" AutoEventWireup="false" Inherits="registry.Publish" %>
<%
// fill this with your details 
	string Title = "The Virtual Astronomical Observatory (VAO)";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.5 $";

	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "pubpage";
	string selected = "publish";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				

Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
//Server.Execute("top.aspx?message=Publish a Resource");
%>
<h1>
	Resource Publication</h1>
<p>Click on the kind of resource you wish to publish to this registry.&nbsp; All 
	resources should have an Authority ID as the primary part of the Identifier to 
	be compliant with the IVOA standards.&nbsp; Click here for examples.</p>
<br>
<% if (null != ds) {%>
<table border="1" class="filled" bordercolor="#6ba5d7">
	<tr>
		<th class="filled">
			ResourceType
		</th>
		<th class="filled">
			Description
		</th>
	</tr>
	<% int len = ds.Tables[0].Rows.Count; 
					for (int r =0; r < len; r++){
						System.Data.DataRow dr = ds.Tables[0].Rows[r];
					%>
	<tr>
		<td class="left">
			<a class="menusmall" href="UpdateRegistry.aspx?InsertMode=t&amp;ResourceType=<%=dr[0]%>">
				<%=dr[0]%>
			</a>
		</td>
		<td><%=dr[1]%></td>
	</tr>
	<%	}
					%>
</table>
<% }%>
<%
//Server.Execute("bot.aspx");
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>
