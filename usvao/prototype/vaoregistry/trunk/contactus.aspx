<%@ Page language="c#" AutoEventWireup="false" Inherits="registry.contact" %>
<%@ Import Namespace="System.Web" %>
<% // fill this with your details 
	string Title = "NVO - Contact Us";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.4 $";
	
	string path = "";
	
	string bgcolor = "#FF0000";
	string displayTitle = "";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				
%>
<% // will need to fix path if we have sub dirs .. 
	Server.Execute("web/SkyHeader2.aspx" + "?" + Parameters);
%>

<table width="100%" cellspacing = 10>

<tr valign="top"><td width="112"></td>
<td>
<h1>Feedback & Help</h1>

<p>Your feedback is important to us. To make sure we can address your questions, 
please  send email to <b>usvo-feedback ((at)) usvo.org</b>. You may also want to 
take a look at our <a href="http://us-vo.org/getting_started/index.cfm">Get Started with NVO</a> 
page to see if any of your questions are answered there.</p>
</td>
<td width="147"></td>
</tr>
</table><br />
<table width="100%" cellspacing = 10>
<tr valign="top"><td width="112"></td>
<td><h1>Contact Us</h1></td>
</tr><tr><td width="112"></td><td valign="top">
<h3 align="left">NVO STScI Registry Developer</h3>
<p>
	<b>Gretchen Greene</b><br />
	Operations and Data Management<br/>
	Space Telescope Science Institute<br/>
	3700 San Martin Dr.<br/>
	Baltimore, MD 21218<br/>
	(410) 338-4852
</p>
</td>
<td valign="top">
<h3 align="left">NVO STScI Registry Developer</h3>
<p>
	<b>Theresa Dower</b><br />
	Operations and Data Management<br/>
	Space Telescope Science Institute<br/>
	3700 San Martin Dr.<br/>
	Baltimore, MD 21218<br/>
	(410) 338-4978
</p>
</td>
<td valign="top"> 
<h3 align="left">Virtual Observatory Project Manager</h3>
<p>
	<b>Dr. Robert Hanisch</b><br />
	Operations and Data Management<br/>
	Space Telescope Science Institute<br/>
	3700 San Martin Dr.<br/>
	Baltimore, MD 21218<br/>
	(410) 338-4910
	</p>
</td>
<td width="147"></td></tr>

<tr valign="top"><td width="112"></td>
<td><img src="web/images/gretchen.jpg" alt="Gretchen Greene"/></td>
<td><img src="web/images/theresa.jpg" alt="Theresa Dower"/></td>
<td><img src="web/images/hanisch.jpg" alt="Bob Hanisch"/></td>

</tr>

<tr valign="top"><td width="112"></td>
<td><p><a href="mailto:greene at stsci.edu">greene at stsci.edu</a></p></td>
<td><p><a href="mailto:dower at stsci.edu">dower at stsci.edu</a></p></td>
<td><p><a href="mailto:hanisch at stsci.edu">hanisch at stsci.edu</a></p></td>
<td width="147"></td>
</tr>


</table>


<% // will need to fix path if we have sub dirs .. 
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
