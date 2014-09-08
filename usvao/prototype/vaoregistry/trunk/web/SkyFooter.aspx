<%@ Import Namespace="System.IO" %>
<%@ Page language="c#" %>
<%
    string url = Request.Url.GetLeftPart(System.UriPartial.Authority)+Request.ApplicationPath;

	string  htmlRevision = "??";


	string author = Request.Params["author"];
	string email = Request.Params["email"];
	string cvsRevision = Request.Params["cvsRevision"];
	string cvsTag = Request.Params["cvsTag"];
	
	string page = Request.Params["PATH_TRANSLATED"];
	

	
	if(null != page)
	{
		FileInfo f = new FileInfo(page);
		htmlRevision = f.LastWriteTime.ToLongDateString() + " at " + f.LastWriteTime.ToLongTimeString();
	}



	
	if(null == author)	author = "";
	if(null == email)  email = "";
	if(null == cvsRevision)  cvsRevision = "$$";

	
%>
<!-- close all the tables and cells  - put in footer> !-->
             </td>
           </tr>
         </table>
	   </td>
	 </tr>
	 <tr>
	   <td bgcolor="#557399" height="30"><img src="<%=url%>/web/images/spacer.gif"></td>
	   <td colspan=2 align=right valign=bottom>
	     <table width="325" border="0" cellspacing="0" cellpadding="0">
	       <tr>
		     <td bgcolor="#FFFFFF" align=right><img src="<%=url%>/web/images/footercurve.gif"></td>
	         <td bgcolor="#8BBAE2" align=center><a href="<%=url%>/search.aspx" class="searchbar">Google</a></td>
	         <td bgcolor="#8BBAE2" align=center><a href="<%=url%>/contact.aspx" class="searchbar">Contact Us</a></td>
	         <td bgcolor="#8BBAE2" align=center><a href="mailto:feedback@us-vo.org?subject=NVO Registry Feedback" class="searchbar">Feedback</a></td>
	       </tr>
	     </table>
	   </td>
	 </tr>
	 <tr>
	   <td border="0" colspan=3 bgcolor="#000033" align=left>
	     <table border="0" width="100%" bgcolor="#000033">
	       <tr>
              <td width align="left"><img src="<%=url%>/web/images/spacer.gif" width="16" height="1"></td>
              <td width="48"><a href="http://www.nsf.gov/"><img border="0" src="<%=url%>/web/images/nsflogo.gif" width="48" height="48"></a></td>
              <td width="17"><img src="<%=url%>/web/images/spacer.gif" width="17" height="1"></td>
              <td width="72"><a href="http://www.ivoa.net"><img border="0" src="<%=url%>/web/images/ivoalogo.jpg" width="72" height="39"></a></td>
              <td width="10"><img src="<%=url%>/web/images/spacer.gif" width="10" height="1"></td>
              <td width><img src="<%=url%>/web/images/footerline.gif" width="1" height="55"></td>
              <td width="14"><img src="<%=url%>/web/images/spacer.gif" width="14" height="1"></td>
              <td width="530">
                <div class="tiny"><font color="#FFFFFF">
                Sponsored by the National Science Foundation under Cooperative 
                Agreement AST0122449 with<br>The Johns Hopkins University. 
                Developed in collaboration with the International Virtual Observatory Alliance.<br><br>
                Last Modified:&nbsp;<%=htmlRevision%> by <%=author%><br>
                <%=cvsRevision.Replace("$","")%>
				</font></div></td>
              <td width><img src="../web/images/spacer.gif" width="1" height="84"></td>
	       </tr>
	     </table>
	   </td>
	 </tr>
   </table>


</td>
</tr>
</table>

</body>
</html>