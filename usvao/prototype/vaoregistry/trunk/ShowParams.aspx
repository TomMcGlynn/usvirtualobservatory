<%@ Page language="c#" Codebehind="ShowParams.aspx.cs" AutoEventWireup="false" Inherits="registry.ShowParams" %>
<HEAD>
	<LINK href="styles.css" rel="stylesheet">
</HEAD>
<body>
	<iframe src="top.aspx?message=Interface Params" frameBorder="no" width="100%" scrolling="no"
		height="120"></iframe>
	<h1>
		Interface Parameters for: <%=res==null?"":res.Title%></h1>
	<br>
	<% if (null != res) {%>
	<table border="1" class="filled" bordercolor="#6ba5d7">
		<tr>
			<th class="filled">
				Name
			</th>
			<th class="filled">
				Description
			</th>
			<th class="filled">
				Datatype
			</th>
			<th class="filled">
				UCD
			</th>
			<th class="filled">
				UNIT
			</th>
		</tr>
		<% if (res.resourceInterfaces[interfaceNum].interfaceParams!=null)
		   {
			int len = res.resourceInterfaces[interfaceNum].interfaceParams.Length; 
				for (int r =0; r < len; r++){
					ip = res.resourceInterfaces[interfaceNum].interfaceParams[r];		
		%>
		<tr>
			<td class="left">
				<%=ip.name%>
			</td>
			<td class="left">
				<%=ip.description%>
			</td>
					<td class="left">
				<%=ip.datatype%>
			</td>
						<td class="left">
				<%=ip.ucd%>
			</td>
						<td class="left">
				<%=ip.unit%>
			</td>
		</tr>
		
		<%}}else{
		%>
		<tr><td colspan=5>No Interface Parameters Found!</td></tr>
		<%}%>
	</table>
	<% }%>
	</TD></TR></TABLE>
</body>
