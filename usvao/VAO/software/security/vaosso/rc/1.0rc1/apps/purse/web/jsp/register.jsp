<%@ page import="java.util.Properties" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.globus.purse.util.HtmlEncode" %>
<%@ page session="true" %>
<jsp:useBean id="formHandler" class="nvo.VORegForm" scope="request"/>
<jsp:setProperty name="formHandler" property="*" />
<%
    formHandler.setName(decode(formHandler.getName()));
    formHandler.setInst(decode(formHandler.getInst()));
    formHandler.setPortalName(decode(formHandler.getPortalName()));
    formHandler.setPhone(decode(formHandler.getPhone()));
    formHandler.setCountry(decode(formHandler.getCountry()));

    if (session.isNew()) {
        formHandler.setUserName("");
        formHandler.setPassword1("");
        formHandler.setPassword2("");
        session.setAttribute("emptyInputs", formHandler.emptyProperties());

        String referer = request.getHeader("Referer");
        session.setAttribute("Referer", referer);
        if (formHandler.getPortalName() == null || formHandler.getPortalName().length() == 0) {
            if (referer != null) { // pick out hostname of referer
                Pattern sitere = Pattern.compile(":(//)?([^/]*)");
                Matcher mchr = sitere.matcher(referer);
                if (mchr.find()) {
                    formHandler.setPortalName(referer.substring(mchr.start(2),
                            mchr.end(2)));
                } else {
                    formHandler.setPortalName(referer);
                }
            } else {
                formHandler.setPortalName("");
            }
        }
    } else {
        formHandler.loadErrors((Properties) session.getAttribute("errors"));
        formHandler.setUserName((String) session.getAttribute("username"));
    }
    // Ask the browser not to cache the page
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");

    //Causes the proxy cache to see the page as "stale"
    response.setDateHeader("Expires", 0); 

    //HTTP 1.0 backward compatibility 
    response.setHeader("Pragma","no-cache"); 
//	session.invalidate();
%>
<html>
<head>
<% String title = "VAO User Logon Registration"; %>
<%@ include file="head.include" %>
<script language="JavaScript" type="text/JavaScript">
helpwin = null;
function popupHelp(url) {
   if (helpwin == null) {
      helpwin = window.open(url,"helpwin", "height=500,width=500,scollbars=on");
   }
   else {
      helpwin.location.href = url;
   }
}
</script>
<script type= "text/javascript" src = "countries.js"></script>
</head>

<body>
<%@ include file="body.open.include" %>

<%
    String portal;
    boolean knownPortal;
    if (formHandler.getPortalName().length() <= 0) {
        knownPortal = false;
	    portal = "an unknown portal";
    } else {
	    knownPortal = true;
        portal = formHandler.getPortalName();
    }
    Properties errors = formHandler.getErrors();
    String editables = (String) session.getAttribute("emptyInputs");
    if (editables == null) editables = "";
    String param;
%>
<% if (errors.size() > 0) { %>
<div class="error_box">
<h1 class="top">Registration Input Error</h1>

<ul>
    <%
        System.out.println("Purse registration error(s) for " + formHandler.getName()
                + " (username " + formHandler.getUserName() + ", email " + formHandler.getEmail() + "):");
        for (Enumeration e=errors.elements(); e.hasMoreElements();) {
            String errorMsg = (String) e.nextElement();
            System.out.println("  * " + errorMsg);
     %>
    <li><%= errorMsg %></li>
    <% } %>
</ul>
</div>

<% } else { %>

<h1 class="top">Create a VAO Logon</h1>

<% } %>

<p class="announce">
  You have been sent here from <%=HtmlEncode.encode(portal)%>.
</p>

<% if (!formHandler.isReturnURLBlank()) { %>
<p class="announce">
  <span style="font-size:70%">
    When registration and login are complete, you'll be returned to<br>
    <em><%=HtmlEncode.encode(formHandler.getReturnURL())%></em>
  </span>
</p>
<% } %>

<p>
You are about to create your own <a href="/">VAO Logon</a>&#8212;a
single unique identity which you can use to log onto any VAO-compliant
portal.  Creating a VAO Logon is not required to use most VAO 
services; however, a VAO Logon will give you access to enhanced
services such as remote storage and access to proprietary data.
</p>

<dl>
<dt>Creating an VAO Logon is a two step process. <p>
   <dd> <strong>Step 1:</strong> 
	Complete the registration form below.
   <dd> <strong>Step 2:</strong>   
	Confirm your identity by replying to a confirmation email
</dl>

<h2>Step 1: Fill out and submit the registration form</h2>

<p>Need to re-send a <b>registration email</b>?  You may <a href="resend.jsp">request another copy</a>.</p>

<p>Forgot your <b>password</b>?  You may <a href="password.jsp">request a reminder</a>.</p>

<form method="post" action="process.jsp">
<p style="color:red">* required fields</p>
<% if (knownPortal) { %>
<p style="color:green">* fields passed to us by <%= HtmlEncode.encode(portal) %>.</p>
<% } %>

<% if (!formHandler.isReturnURLBlank()) { %>
    <input type="hidden" name="returnURL"
           value="<%= HtmlEncode.encode(formHandler.getReturnURL()) %>" />
<% } %>
<input type="hidden" name="portalName"
       value="<%= HtmlEncode.encodeHREFParam(formHandler.getPortalName()) %>" />

<blockquote>
   <table border="0">
     <tbody>
       <tr>
<%
param = request.getParameter("name");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&name=") >= 0)
{
%>
	 <td valign="top"><font color="red">*</font></td>
	 <td>Full Name</td>
	 <td><input name="name" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getName())%>'><br>
	     <font size=2 color=red><%=formHandler.getErrorMsg("name")%></font>
	 </td>
<% }
else {
%>
	 <td valign="top"><font color="green">*</font></td>
	 <td>Full Name: </td>
          <td> <%= HtmlEncode.encode(formHandler.getName()) %>
	      <input type="hidden" name="name"
		     value="<%=HtmlEncode.encode(formHandler.getName())%>">
	      </td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("inst");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&inst=") >= 0)
{
%>
	 <td valign="top"></td>
	 <td>Institution/Company
	     <br>(if applicable)</td>
	 <td><input name="inst" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getInst())%>'><br>
	      <font size=2 color=red><%=formHandler.getErrorMsg("inst")%></font>
	 </td>
<% }
else {
%>
	 <td valign="top"><font color="green">*</font></td>
	 <td>Institution/Company: </td> <td> <%= HtmlEncode.encode(formHandler.getInst()) %>
	      <input type="hidden" name="inst"
		     value="<%=HtmlEncode.encode(formHandler.getInst())%>">
	 </td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("email");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&email=") >= 0)
{
%>
	 <td valign="top"><font color="red">*</font></td>
	 <td>Email Address</td>
	 <td><input name="email" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getEmail())%>'><br>
	     <font size=2 color=red>
	     <%=HtmlEncode.encode(formHandler.getErrorMsg("email"))%></font>
	 </td>
<% }
else {
%>
	 <td valign="top"><font color="green">*</font></td>
	 <td>Email Address:</td>
	      <td> <%= HtmlEncode.encode(formHandler.getEmail()) %>
	      <input type="hidden" name="email"
		     value="<%=HtmlEncode.encode(formHandler.getEmail())%>">
	 </td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("phone");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&phone=") >= 0)
{
%>
	 <td valign="top"><font color="red">*</font></td>
	 <td>Phone</td>
	 <td><input name="phone" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getPhone())%>'><br>
	     <font size=2 color=red>
	     <%=HtmlEncode.encode(formHandler.getErrorMsg("phone"))%></font>
	 </td>
<% }
else {
%>
	 <td valign="top"><font color="green">*</font></td>
	 <td>Phone:</td>
	      <td> <%= HtmlEncode.encode(formHandler.getPhone()) %>
	      <input type="hidden" name="phone"
		     value="<%=HtmlEncode.encode(formHandler.getPhone())%>">
	      </td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("country");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&country=") >= 0)
{
%>
	 <td valign="top"><font color="red">*</font></td>
	 <td>Country</td>
         <td><select id="countrySelect" name ="country"></select>
              <script language="javascript">initCountry('US');</script><br>
	     <font size=2 color=red>
	     <%=HtmlEncode.encode(formHandler.getErrorMsg("country"))%></font>
	 </td>
<% }
else {
%>
	 <td valign="top"><font color="green">*</font></td>
	 <td>Country:</td>
	      <td> <%= HtmlEncode.encode(formHandler.getCountry()) %>
	      <input type="hidden" name="country"
		     value="<%=HtmlEncode.encode(formHandler.getCountry())%>">
	      </td>
<% } %>
       </tr>

       <br>
     </tbody>
   </table>
</blockquote>

<p>
The username and password you provide below can be used at any 
VAO-compliant portal.  
</p>

<p>
A password must be at least 6 characters long and should follow 
<a href="/help/GoodPasswords.html" 
   onclick="popupHelp('/help/GoodPasswords.html'); return false">good 
practices for secure passwords</a>.  
</p>

<blockquote>

   <table border="0">
     <tbody>
       <tr>
	 <td valign="top"><font color="red">*</font></td>
	 <td>Choose a username</td>
	 <td><input name="userName" type="text"
		    value="<%=HtmlEncode.encode(formHandler.getUserName()) %>">
<%
String usrMsg = formHandler.getErrorMsg("userName");
if (usrMsg != null && usrMsg.length() > 0) {
%>
<br><font size=2 color=red><%=usrMsg%></font>
<% } %>
        </td>
       </tr>
       <tr>

	 <td valign="top"><font color="red">*</font></td>
	 <td><a href="/help/GoodPasswords.html"
		onclick="popupHelp('/help/GoodPasswords.html'); return false">Choose a
		password</a>:</td>
	 <td><input name="password1" type="password" value=''>
<%
String pwMsg = formHandler.getErrorMsg("password1");
if (pwMsg != null && pwMsg.length() > 0) {
%>
<br><font size=2 color=red><%=pwMsg%></font>
<% } %>
        </td>
       </tr>
       <tr>
	 <td valign="top"><font color="red">*</font></td>
	 <td valign="top">
	   <a href="/help/GoodPasswords.html" 
	      onclick="popupHelp('/help/GoodPasswords.html'); return false">Repeat 
	      the password</a>:</td>
	 <td><input name="password2" type="password" value=''>
<%
String pwMsg2 = formHandler.getErrorMsg("password2");
if (pwMsg2 != null && pwMsg2.length() > 0) {
%>
<br><font size=2 color=red><%=pwMsg2%></font>
<% } %>
         </td>
       </tr>
       <tr>
         <td colspan="2"></td>
	 <td style="padding-top:0.5em"><input value="Register" type="submit"></td>
       </tr>
     </tbody>
   </table>
</blockquote>

</form>
<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>

<%!
    String decode(String s) {
        if (s == null) return null;
        else return s.replaceAll("%20", " ").replaceAll("\\+", " ");
    }
%>
