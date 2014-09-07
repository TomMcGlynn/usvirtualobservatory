<%@ page import="java.util.Properties" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.globus.purse.util.HtmlEncode" %>
<%@ page session="true" %>
<jsp:useBean id="formHandler" class="nvo.VORegForm" scope="request"/>
<jsp:setProperty name="formHandler" property="*" />
<%
    formHandler.setLastName(decode(formHandler.getLastName()));
    formHandler.setFirstName(decode(formHandler.getFirstName()));
    formHandler.setInst(decode(formHandler.getInst()));
    formHandler.setPortalName(decode(formHandler.getPortalName()));
    formHandler.setPhone(decode(formHandler.getPhone()));
    formHandler.setCountry(decode(formHandler.getCountry()));

    if (session.isNew()) {
        formHandler.setUserName("");
        formHandler.setPassword1("");
        formHandler.setPassword2("");
        session.setAttribute("emptyInputs", formHandler.emptyProperties());

        if (formHandler.getPortalName() == null) formHandler.setPortalName("");
        String referer = request.getHeader("Referer");
        session.setAttribute("Referer", referer);

        if (! formHandler.isReturnURLBlank() &&
            formHandler.getPortalName().length() == 0) 
        {
            // if we will be returning the user to another site, make
            // sure we have a name for it.
            if (referer == null || referer.length() == 0) 
                referer = formHandler.getReturnURL();

            Pattern sitere = Pattern.compile(":(//)?([^/]*)");
            Matcher mchr = sitere.matcher(referer);
            if (mchr.find()) {
                formHandler.setPortalName(referer.substring(mchr.start(2),
                        mchr.end(2)));
            } else {
                formHandler.setPortalName(referer);
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
<script language="javascript" type="text/javascript">
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
<script type= "text/javascript" src = "/purse/countries.js"></script>
</head>

<body>

<script type="text/javascript" src="/styles/jquery.min.js"></script>
<table width="100%" id="nav-head" border="0" cellpadding="0" cellspacing="0">
 <tr>
  <td id="nav-ul"></td>
  <td></td>
  <td id="nav-ur"></td>
 </tr><tr>
  <td id="nav-menu">
     <ul>
       <li><a href="/">About VAO Logins</a></li>
       <li><a href="/register/">Get a VAO Login</a></li>
       <li><a href="/openid/">Profile and Preferences</a></li>
       <li><a href="/help/support.html">Support VAO Logins</a></li>
     </ul>
  </td>
  <td id="nav-logo" align="center" valign="top">
     <a href="http://www.usvao.org"><img src="/images/VAOwords_200.png" width="180" height="100" /></a>
  </td>
  <td id="nav-menu">
     <ul>
       <li><a href="http://www.usvao.org/">VAO Home</a></li>
       <li><a href="http://www.usvao.org/tools/">Science Tools</a></li>
       <li><a href="http://www.usvao.org/about.html">About the VAO</a></li>
       <li><a href="http://www.usvao.org/news.cfm">VAO News</a></li>
       <li><a href="http://www.usvao.org/contact.php">Contact and Connect</a></li>
     </ul>
  </td>
 </tr><tr>
  <td id="nav-bl"></td>
  <td align="center">
     <span style="color: green;">
     <span id="loginStatus"></span>
<script type="text/javascript">
function makeStatusHTML(status) {
    var htmlt = "";
    if (status.state == "in") {
        htmlt += status.username;
        htmlt += " &nbsp;&nbsp;Time left: ";
        htmlt += status.dispLeft;
        htmlt += ' &nbsp;&nbsp;<a href="/openid/?logout=true">Logout</a> ';
    }
    else if (status.state == "expired") {
        htmlt += "Expired session as ";
        htmlt += status.username;
        htmlt += '&nbsp;&nbsp;<a href="/openid/">Login</a>';
    }
    else {
        htmlt += 'Logged out &nbsp;&nbsp;<a href=\"/openid/\">Login</a>&nbsp;&nbsp;<a href="/register/">Register</a>';
    }
    return htmlt;
}

function displayStatus(status) {
    jQuery("#loginStatus")[0].innerHTML = makeStatusHTML(status);
    var warning = jQuery("#warning");
    var user = jQuery("#loggedinuser");
    if (status.state == "in") {
        if (user != null && user.length > 0) 
            user[0].innerHTML = status.username;
        if (warning != null && warning.length > 0) 
            warning[0].style.display = "block";
    }
    else {
        if (warning != null && warning.length > 0) 
            warning[0].style.display = "none";
        if (user != null && user.length > 0) 
            user[0].innerHTML = "an unknown user";
    }
}

function dologout() {
    jQuery.get("/openid/?logout=true", function(data) { 
            jQuery.get("/openid/loginStatus", displayStatus);
        });
}

jQuery(document).ready(function() {
    jQuery.getJSON("/openid/loginStatus", displayStatus);
})
</script></span>
  </td>
  <td id="nav-br"></td>
 </tr>
</table>


<br/>

<%
    boolean fromAPortal = (formHandler.getPortalName().length() > 0);
    Properties errors = formHandler.getErrors();
    String editables = (String) session.getAttribute("emptyInputs");
    if (editables == null) editables = "";
    String param;
%>

<div class="main-content">

<% if (errors.size() > 0) { %>
<div class="error_box">
<h1 class="top">Registration Input Error</h1>

<ul>
    <%
        System.out.println("Purse registration error(s) for " +
           formHandler.getFirstName() + " " + formHandler.getLastName() +
           " (username " + formHandler.getUserName() + ", email " + formHandler.getEmail() + "):");
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

<% if (fromAPortal) { %>
<p class="announce">
  Hello.  You have been sent here from <%=HtmlEncode.encode(formHandler.getPortalName())%>
  because you need a VAO Login to use that portal.  
</p>

<% if (!formHandler.isReturnURLBlank()) { %>
<p class="announce">
  <span style="font-size: medium">
    When registration and login are complete, you'll be returned to<br>
    <em><%=HtmlEncode.encode(formHandler.getReturnURL())%></em>
  </span>
</p>
<% }} %>

<p>
You are about to create your own <a href="/">VAO Logon</a>&#8212;a
single unique identity which you can use to log onto any VAO-compliant
portal.  Creating a VAO Logon is not required to use most VAO 
services; however, a VAO Logon will give you access to enhanced
services such as remote storage and access to proprietary data from
participating archives.
</p>

<blockquote id="warning" style="display: none; background-color: #ff9999;">
You are currently already logged in as <span id="loggedinuser">an unknown user</span>.
If this is your login, there's no need to proceed; use your back
button to return to <%=HtmlEncode.encode(formHandler.getPortalName())%>.  
<em>If this is not your login</em>, please 
<a href="javascript:void(0)" onclick="dologout(); return false;">logout</a>
before proceeding.
</blockquote>

<dl>
<dt>Creating an VAO Logon is a two step process. <p>
   <dd> <strong>Step 1:</strong> 
	Complete the registration form below.
   <dd> <strong>Step 2:</strong>   
	Confirm your identity by replying to a confirmation email
</dl>

<p>Do you need a confirmation email to be resent? You may <a href="/purse/resend.jsp">request another copy</a>.</p>

<p>Forgot your <b>password</b>?  You may <a href="/purse/password.jsp">request a reminder</a>.</p>

<h2>Step 1: Fill out and submit the registration form</h2>

<form method="post" action="/purse/process.jsp">
<p>
<span style="color:red">* required fields</span>
<% if (fromAPortal) { %>
<br />
<span style="color:green">* fields passed to us by <%= HtmlEncode.encode(formHandler.getPortalName()) %>.</span>
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
param = request.getParameter("lastName");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&lastName=") >= 0)
{
%>
	 <td><span style="position: relative; top: -1ex; color: red">*</span></td>
	 <td>Last Name</td>
	 <td><input name="lastName" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getLastName())%>'></td>
         <td>
             <% if (formHandler.getErrorMsg("lastName").length() > 0) { %>
	     <font size=2 color=red><%=formHandler.getErrorMsg("lastName")%></font>
             <% } %>
	 </td>
<% }
else {
%>
	 <td><span style="position: relative; top: -1ex; color: green">*</span></td>
	 <td>Last Name</td>
         <td> <%= HtmlEncode.encode(formHandler.getLastName()) %>
	      <input type="hidden" name="lastName"
		     value="<%=HtmlEncode.encode(formHandler.getLastName())%>">
	      </td>
         <td></td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("firstName");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&firstName=") >= 0)
{
%>
	 <td><span style="position: relative; top: -1ex; color: red">*</span></td>
	 <td>First Name(s)</td>
	 <td><input name="firstName" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getFirstName())%>'></td>
         <td>
             <% if (formHandler.getErrorMsg("firstName").length() > 0) { %>
	     <font size=2 color=red><%=formHandler.getErrorMsg("firstName")%></font><br />
             <% } %>
             <em>Include first or middle names/initials as desired
                 (e.g. "John Paul", "Tom T.", "F. Scott")</em>
	 </td>
<% }
else {
%>
	 <td><span style="position: relative; top: -1ex; color: green">*</span></td>
	 <td>First Name(s): </td>
         <td> <%= HtmlEncode.encode(formHandler.getFirstName()) %>
	      <input type="hidden" name="firstName"
		     value="<%=HtmlEncode.encode(formHandler.getFirstName())%>">
	      </td>
         <td></td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("inst");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&inst=") >= 0)
{
%>
         <td></td>
	 <td>Institution or Company</td>
	 <td><input name="inst" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getInst())%>'></td>
         <td>
             <% if (formHandler.getErrorMsg("inst").length() > 0) { %>
	     <font size=2 color=red><%=formHandler.getErrorMsg("inst")%></font><br />
             <% } %>
             <em>if applicable</em>
	 </td>
<% }
else {
%>
	 <td><span style="position: relative; top: -1ex; color: green">*</span></td>
	 <td>Institution or Company: </td> 
         <td> <%= HtmlEncode.encode(formHandler.getInst()) %>
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
	 <td><span style="position: relative; top: -1ex; color: red">*</span></td>
	 <td>Email Address</td>
	 <td><input name="email" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getEmail())%>'></td>
         <td>
	     <font size=2 color=red>
	     <%=HtmlEncode.encode(formHandler.getErrorMsg("email"))%></font>
	 </td>
<% }
else {
%>
	 <td><span style="position: relative; top: -1ex; color: green">*</span></td>
	 <td>Email Address:</td>
         <td> <%= HtmlEncode.encode(formHandler.getEmail()) %>
              <input type="hidden" name="email"
	             value="<%=HtmlEncode.encode(formHandler.getEmail())%>">
	 </td>
         <td></td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("phone");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&phone=") >= 0)
{
%>
	 <td><span style="position: relative; top: -1ex; color: red">*</span></td>
	 <td>Phone</td>
	 <td><input name="phone" type="text"
		    value='<%=HtmlEncode.encode(formHandler.getPhone())%>'></td>
         <td><font size=2 color=red>
	     <%=HtmlEncode.encode(formHandler.getErrorMsg("phone"))%></font>
	 </td>
<% }
else {
%>
	 <td><span style="position: relative; top: -1ex; color: green">*</span></td>
	 <td>Phone:</td>
	      <td> <%= HtmlEncode.encode(formHandler.getPhone()) %>
	      <input type="hidden" name="phone"
		     value="<%=HtmlEncode.encode(formHandler.getPhone())%>">
	      </td>
         <td></td>
<% } %>
       </tr>

       <tr>
<%
param = request.getParameter("country");
if (param == null || param.trim().length() == 0 ||
    editables.indexOf("&country=") >= 0)
{
%>
	 <td><span style="position: relative; top: -1ex; color: red">*</span></td>
	 <td>Country</td>
         <td colspan="2"><select id="countrySelect" name ="country"></select>
             <script language="javascript" type="text/javascript">initCountry('US');</script><br>
	     <font size=2 color=red>
	     <%=HtmlEncode.encode(formHandler.getErrorMsg("country"))%></font>
	 </td>
<% }
else {
%>
	 <td><span style="position: relative; top: -1ex; color: green">*</span></td>
	 <td>Country:</td>
         <td> <%= HtmlEncode.encode(formHandler.getCountry()) %>
	      <input type="hidden" name="country"
		     value="<%=HtmlEncode.encode(formHandler.getCountry())%>">
	      </td>
         <td></td>
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
</div>
<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>

<%!
    String decode(String s) {
        if (s == null) return null;
        else return s.replaceAll("%20", " ").replaceAll("\\+", " ");
    }
%>
