<%@ Import Namespace="System.IO" %>
<%@ Page language="c#" AutoEventWireup="false" %>
<%
    string url = Request.Url.GetLeftPart(System.UriPartial.Authority)+Request.ApplicationPath;


	string author = Request.Params["author"];
	string email = Request.Params["email"];
	string cvsRevision = Request.Params["cvsRevision"];
	string cvsTag = Request.Params["cvsTag"];
	
	string page = Request.Params["PATH_TRANSLATED"];
	
	string  htmlRevision = "??";
	
	if(null != page)
	{
		FileInfo f = new FileInfo(page);
		htmlRevision = f.LastWriteTime.ToLongDateString() + " at " + f.LastWriteTime.ToLongTimeString();
	}



	
	if(null == author)	author = "";
	if(null == email)  email = "";
	if(null == cvsRevision)  cvsRevision = "$$";

	
%>
<!-- close all the atbles and cells  - put in footer> !-->
	</td>
	</tr>
</table>
<table width="800">
<tr><td colspan=3>
<hr color="#6ba5d7" />
</td></tr>
<tr><td>
<a href="http://www.us-vo.org" target="new"><img border="0" align="bottom" src="<%=url%>/nsf.gif" alt="nsf"></a>
</td><td class="center">

<div class="tiny">
Sponsored all or in part by the National Science Foundation under
Cooperative Agreement AST0122449 with The Johns Hopkins University.
<br>Developed in collaboration with the International Virtual Observatory
Alliance.<br/>
	<a href="mailto:feedback@us-vo.org">
Contact The VAO Help Desk to report problems and suggestions.	
</a>	
	<br/>
	Last Modified:&nbsp;<%=htmlRevision%>
	&nbsp;$Name:  $
	<%=cvsRevision.Replace("$","")%>
</div>
</td><td>
<a target="offsite" href="http://www.ivoa.net"><img border="0" align="bottom" src="<%=url%>/IVOA50.jpg" alt="IVOA">
</td></tr></table>
</td></tr>

	</table>
    </tr>
    </table>

</body>
</html>
