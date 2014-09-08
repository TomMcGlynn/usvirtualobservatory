<%@ Import Namespace="System.IO" %>
<%@ Import Namespace="System.Data" %>
<%@ Import Namespace="registry" %>
<%@ Page Language="c#" AutoEventWireup="false" %>
<%
Server.Execute("top.aspx?message=Astronomical Resource Registry");
%>
			<br>
			<table width="800" align="top" border="0"  >
				<tr>
				<td><b>
				This is a directory of online astronomical resources in the global Virtual Observatory network.  We encourage you to use
				this portal to find or publish descriptions of useful resources throughout the world.
				</b></td></tr>
				<tr></tr>
				<tr>
					<td class="center"> <b> This registry is currently down please use the <a href="http://voservices.net/registry"> Mirror</a></b>
				</td>
							</tr>
						</table>
					</td>
				</tr>
			<tr><td>
			<HR width="100%" color="#6ba5d7" SIZE="1">
			<table><tr><td><a href="http://www.nsf.gov/"><img src="nsf.gif"></a></td>
			<td>
			<%
	string page = Request.Params["PATH_TRANSLATED"];
	string htmlRevision = "";
	if (null !=page) {

		FileInfo f = new FileInfo(page);
		htmlRevision = f.LastWriteTime.ToLongDateString() + " at " + f.LastWriteTime.ToLongTimeString();
	}
%>
			<p class="tiny" align=center>
				Sponsored by the National Science Foundation in collaboration 
				with the International Virtual Observatory Alliance
				<br>
				<a href="mailto:feedback@us-vo.org">Contact US-VO Help Desk for Problems and Suggestions </a>
				<br>
				Last Modified :<%=htmlRevision%>
				, $Name:  $, $Revision: 1.1.1.1 $
			</p>
		</td>
		<td><a href="http://ivoa.net"><img src="ivoa_logo137x77.jpg" ></a></td>
		</td></tr>


</table>
</div>
</HTML>
