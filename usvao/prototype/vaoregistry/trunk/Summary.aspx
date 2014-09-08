<%@ Page language="c#" Codebehind="Summary.aspx.cs" AutoEventWireup="false" Inherits="registry.Summary" %>
<%
// fill this with your details 
	string Title = "The Virtual Astronomical Observatory (VAO)";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.6 $";

	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "pubpage";
	string selected = "contents";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				

Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
//Server.Execute("top.aspx?message=Astronomical Resource Summary");
%>
<h1> Registry Contents by ResourceType</h1>
<p>Click on a resource Type to get a browsable listing for that type.</p>
				<br>
				<% if (null != ds2) {%>
				<table border="1" class="filled" bordercolor="#6BA5D7">
					<tr  ><th class="filled"> Total Resources </th></tr>
					<% 
						System.Data.DataRow dr2 = ds2.Tables[0].Rows[0];
					%>	
					<tr><td class="left"> <%=dr2[0]%></td>
					</tr>
				</table>
				<% }%>
				<br>
				<% if (null != ds) {%>
				<table border="1" class="filled" bordercolor="#6BA5D7">
					<tr  ><th class="filled"> ResourceType </th>
					<th class="filled">Resource Count</th>
					<th class="filled">Description</th>
					</tr>
					<% int len = ds.Tables[0].Rows.Count; 
					for (int r =0; r < len; r++){
						System.Data.DataRow dr = ds.Tables[0].Rows[r];
					%>	
					<tr>
						<td class="left"> 
						<a class="small" href="../QueryRegistry.aspx?startres=-1&advanced=true&sql=ResourceType like '<%=dr[0]%>'">
						<%=dr["ResourceType"]%></a></td>
						
									
						<td class="left"> <%=dr[1]%></td>
						<td class="left"> <%=dr[2]%></td>

					</tr>
					<%	}
					%>
				</table>
				<% }%>
				
			<!--</td>
		</tr>
	</table>
</body>-->

<%
//Server.Execute("bot.aspx");
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>
				

