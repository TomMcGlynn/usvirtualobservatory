<%@ Page Language="c#" AutoEventWireup="false" debug=true %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" >
<HTML>
	<HEAD>
		<title>VO form </title>
		<LINK href="styles.css" rel="stylesheet">
		<LINK REL="shortcut icon" HREF="scnvo.ico">
	</HEAD>

<%
//Server.Execute("top.aspx?message=VO Form");
string url = Request.Params["serviceUrl"];
string st = Request.Params["CapabilityType"];
string ra = Request.Params["RA"];
string dec = Request.Params["DEC"];
string sr = Request.Params["SR"];
string ot = Request.Params["ot"];

if (null ==ra || null == dec || null == sr ||
		ra.Length == 0 || dec.Length == 0 || sr.Length== 0) { 

%>
<iframe frameborder="no" height="120" src="top.aspx?message=VO Form" width="100%" scrolling="no"></iframe>
<p>Please enter parameters to send to the service.
<form id="voform" method="get" > <!-- action="<%=url%>"-->
<Table>
<tr><th class="left">Request to:</th><td class="left"><%=url%></td> </tr>
<tr><th class="left">RA (deg):</th><td class="left"> <input name="RA" value=180></td> </tr>
<tr><th class="left">DEC (deg):</th><td class="left"> <input name="DEC" value=-1></td> </tr>
<tr><th class="left">SR/SIZE(deg):</th><td class="left"> <input name="SR" value=0.1></td> </tr>

<tr><th class="left">Output :</th> <td class="left" >
<input type="radio" name="ot" value="XML"> XML
<input type="radio" name="ot" value="HTML" checked> HTML 
<% if (st.ToUpper().IndexOf("SSAP")>=0){ %>
<input type="radio" name="ot" value="VOSpec"> VOSpec 
<%}%>
<% if (st.ToUpper().IndexOf("SIAP")<0 && st.ToUpper().IndexOf("SSAP")<0){ %>
<input type="radio" name="ot" value="VOPlot"> VOPlot 
<%}%>
</td></tr>
<tr><td></td><td><input type="Submit" Value="Submit"</td></tr>
</tr>
</table>
<input type=hidden name="serviceUrl" value="<%=url%>"/>
<input type=hidden name="CapabilityType" value="<%=st%>"/>

</form>
<% } else { // do the forwarding to the actual service 

  string theurl = url;
  theurl = theurl.TrimEnd('\n');
  theurl = theurl.TrimEnd('\r');
  theurl = theurl.TrimStart('\r');
  theurl = theurl.TrimStart('\n'); 
  if (theurl.IndexOf("?") <0) theurl = theurl+"?";
  
  if ((st.StartsWith("CONE")) && (theurl.EndsWith("&")) ){
	theurl = theurl+"&RA="+ra+"&DEC="+dec+"&SR="+sr; 
  } else if (theurl.EndsWith("&")){
	theurl = theurl+"POS="+ra+","+dec+"&SIZE="+sr; 	
  } else if (st.StartsWith("CONE")) {
  	theurl = theurl+"&RA="+ra+"&DEC="+dec+"&SR="+sr; 
  } else if( st.IndexOf("SSAP") <0) {
	theurl = theurl+"&POS="+ra+","+dec+"&SIZE="+sr;
  }
  string runid="STScIJHURegistry-"+Session.SessionID;
  theurl += "&RUNID="+runid;
%>

 Original request <a href="<%=theurl%>"><%=theurl%></a>
<%
   bool handled = false;

	if (ot.StartsWith("HTML") ){
		handled=true;
	    try {
	   registry.votxslt.transform(theurl,Response.Output,Server.MapPath("votable.xsl"));
		} catch (Exception e) {
%>
<h3> There was an Error </h3>
<p> click on the link above to get the actual output form the called service.
<pre> <%=e+":"+e.StackTrace%>
</pre>
<%
		}
	}
	if (ot.StartsWith("VOPlot") ){
		handled=true;
		Session["voturl"] = theurl;
		Response.Redirect("voplot/loadjvt.aspx");
	}
	if (ot.StartsWith("VOSpec") ){
		handled=true;
		theurl= "http://pma.standby.vilspa.esa.es:8080/vospec/jsp/openVOSpec.jsp?&server1="+url;
		theurl+="&POS="+ra+","+dec+"&SIZE="+sr;
		  theurl += "&RUNID="+runid;

		Session["voturl"] = theurl;
		%>
		<iframe width = 600 height=400 src=<%=theurl%>></iframe>
		<%
		//Response.Redirect(theurl);
	}	
   if (!handled) {
		Response.Redirect(theurl);
	}

}
%>
