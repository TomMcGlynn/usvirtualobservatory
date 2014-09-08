<%@ Page CodeBehind="DeleteEntry.aspx.cs" Language="c#" AutoEventWireup="false" %>
<%
Server.Execute("top.aspx?message=Delete Entry");
%>
<form id="deleteEntry" method="post" action="registryadmin.asmx/DeleteEntry">
<Table>
<tr><th class="left">Identifier (to delete):</th><td><input name="identifier"</td> </tr>
<tr><th class="left">PassPhrase :</th><td> <input type="password" name="passPhrase"></td> </tr>
<tr><td></td><td><input type="Submit" Value="Delete"</td></tr>
</tr>
<%
Server.Execute("bot.aspx?message=Delete Entry");
%>
