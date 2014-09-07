<%@ page import="java.util.Properties" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import='org.apache.commons.logging.Log' %>
<%@ page import='org.apache.commons.logging.LogFactory' %>
<%@ page import="org.globus.purse.util.HtmlEncode" %>
<%@ page import="org.usvao.sso.ip.register.RegistrationFormInputs" %>
<%@ page session="true" %>
<jsp:useBean id="inps" 
             class="org.usvao.sso.ip.register.RegistrationFormInputs" 
             scope="request"/>
<jsp:setProperty name="inps" property="*" />
<%
    Log logger = LogFactory.getLog("jsp.purse.register");  // goes to catalina.out
    if (session.isNew()) {
        // this is the first visit to this page
        inps.setUserName("");
        inps.setPassword1("");
        inps.setPassword2("");   

        String referer = request.getHeader("Referer");
        if (inps.getPortalName().length() == 0) {
            // if we will be returning the user to another site, make
            // sure we have a name for it.

            if (inps.getReturnURL().length() != 0 &&
                (referer == null || referer.length() == 0))
              referer = inps.getReturnURL();

            // try to break down the referer to a host name
            if (referer != null && referer.length() > 0) {
                Pattern sitere = Pattern.compile(":(//)?([^/]*)");
                Matcher mchr = sitere.matcher(referer);
                if (mchr.find()) 
                    referer = referer.substring(mchr.start(2), mchr.end(2));

                if (inps.getReturnURL().length() != 0)
                    inps.setPortalName(referer);
            }
        }

        logger.info("Registration page referred to from " + 
                    ((inps.getPortalName().length() == 0) ?
                    referer : inps.getPortalName()));

    } else {
        // we are returning to this page after submitting because there 
        // some errors detected.
        inps.loadErrors((RegistrationFormInputs.ParamErrors) 
                                            session.getAttribute("errors"));
        String username = (String) session.getAttribute("username");
        if (username == null) 
            username = inps.getUserName();
        else 
            inps.setUserName(username);
        logger.info("Registration page: user " + username + 
                    " returns to correct errors");
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
<!DOCTYPE HTML>
<html>
<head>
<% String title = "VAO User Logon Registration"; %>
<%@ include file="head.include" %>
<script language="javascript" type="text/javascript">
helpwin = null;
function popupHelp(url) {
   if (helpwin == null) {
      helpwin = window.open(url,"helpwin", "height=500,width=550,scollbars=1");
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
       <li><a href="/help/faq.html">FAQ</a></li>
     </ul>
  </td>
  <td id="nav-logo" align="center" valign="top">
     <a href="http://www.usvao.org"><img src="/images/VAOwords_200.png" width="180" height="100" /></a>
  </td>
  <td id="nav-menu">
     <ul>
       <li><a href="http://www.usvao.org/">VAO Home</a></li>
       <li><a href="http://www.usvao.org/tools/">Science Tools</a></li>
       <li><a href="http://www.usvao.org/about-vao/">About the VAO</a></li>
       <li><a href="http://www.usvao.org/news/">VAO News</a></li>
       <li><a href="http://www.usvao.org/contact-connect/">Contact and Connect</a></li>
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
    boolean fromAPortal = (inps.getPortalName().length() > 0);
    String param;
%>

<div class="main-content">

<% if (inps.errorsFound()) { %>
<h1 class="top">Creating a VAO Logon</h1>

<div class="error_box">
<h2 class="top">Registration Input Error</h2>

<p>
We&#39;re sorry--we didn&#39;t quite get that.  Please update your inputs below to address the 
following issues:
</p>
<ul>
    <%
        if (logger.isErrorEnabled()) 
            logger.error("Registration input error(s) for " +
                     inps.getLastName()+" "+inps.getFirstName()+
                         "(username " + inps.getUserName() + " email" +
                         inps.getEmail()+ ")");
        for (String errorMsg : inps.exportErrors()) {
            if (logger.isErrorEnabled()) System.out.println("  * " + errorMsg);
     %>
    <li><%= errorMsg %></li>
    <% } %>
</ul>
</div>

<% } else { %>

<h1 class="top">Create a VAO Logon</h1>

<% if (fromAPortal) { %>
<p class="announce">
  Hello.  You have been sent here from <%=HtmlEncode.encode(inps.getPortalName())%>
  because you need a VAO Login to use that portal.  
</p>

<% if (inps.getReturnURL().length() > 0) { %>
<p class="announce">
  <span style="font-size: medium">
    When registration and login are complete, you'll be returned to<br>
    <em><%=HtmlEncode.encode(inps.getReturnURL())%></em>
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
button to return to <%=HtmlEncode.encode(inps.getPortalName())%>.  
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

<% } %>

<h2>Step 1: Fill out and submit the registration form</h2>

<form method="post" action="/purse/process.jsp">
<% if (inps.getReturnURL().length() > 0) { %>
    <input type="hidden" name="returnURL"
           value="<%= HtmlEncode.encode(inps.getReturnURL()) %>" />
<% } %>
<input type="hidden" name="portalName"
       value="<%= HtmlEncode.encodeHREFParam(inps.getPortalName()) %>" />

<blockquote>
   <table border="0" width="90%">
     <tbody>
       <tr>
	 <td width="150pt">First Name(s)</td>
	 <td><input name="firstName" type="text" value='<%=HtmlEncode.encode(inps.getFirstName())%>'></td>
         <td><% if (inps.errorsFoundFor("firstName")) { %>
             <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("firstName"))%></font><br /> <% } %>
             <em><font color="green">Include first or middle names/initials as 
                 desired (e.g. "John Paul", "Tom T.", "F. Scott").</font></em>
	 </td>
       </tr>

       <tr>
	 <td>Last Name</td>
	 <td><input name="lastName" type="text"
		    value='<%=HtmlEncode.encode(inps.getLastName())%>'></td>
         <td><% if (inps.errorsFoundFor("lastName")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("lastName"))%></font> <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Institution or Company</td>
	 <td><input name="inst" type="text"
		    value='<%=HtmlEncode.encode(inps.getInst())%>'></td>
         <td><% if (inps.errorsFoundFor("inst")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("inst"))%></font><br />
             <% } %>
             <em><font color="green">optional</font></em>
	 </td>
       </tr>

       <tr>
	 <td>Email Address</td>
	 <td><input name="email" type="text"
		    value='<%=HtmlEncode.encode(inps.getEmail())%>'></td>
         <td><% if (inps.errorsFoundFor("email")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("email"))%></font><br />
             <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Type Email Again</td>
	 <td><input name="email2" type="text"
		    value='<%=HtmlEncode.encode(inps.getEmail2())%>'></td>
         <td><% if (inps.errorsFoundFor("email2")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("email2"))%></font><br />
             <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Phone</td>
	 <td><input name="phone" type="text"
		    value='<%=HtmlEncode.encode(inps.getPhone())%>'></td>
         <td><% if (inps.errorsFoundFor("phone")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("phone"))%></font><br />
             <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Country</td>
         <td colspan="2"><select id="countrySelect" name ="country"></select>
             <script language="javascript" type="text/javascript">initCountry('<%= (inps.getCountry().length()==0) ? "US" : inps.getCountry() %>');</script><br>
	     <% if (inps.errorsFoundFor("country")) { %>
	     <font size=2 color=red>
	     <%=formatErrors(inps.getErrorMsgsFor("country"))%></font>
             <% } %>
	 </td>
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

   <table border="0" width="90%">
     <tbody>
       <tr>
	 <td width="150pt">Choose a username</td>
	 <td><input name="userName" type="text" value="<%=HtmlEncode.encode(inps.getUserName()) %>"> </td>
         <td><% if (inps.errorsFoundFor("userName")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("userName"))%></font><br />
             <% } %>
	 </td>
       </tr>
       <tr>
	 <td><a href="/help/GoodPasswords.html"
		onclick="popupHelp('/help/GoodPasswords.html'); return false">Choose a
		password</a>:</td>
	 <td><input name="password1" type="password" value=''>
         <td><% if (inps.errorsFoundFor("password1")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("password1"))%></font><br />
             <% } %>
        </td>
       </tr>
       <tr>
	 <td><a href="/help/GoodPasswords.html"
		onclick="popupHelp('/help/GoodPasswords.html'); return false">Choose a
		password</a>:</td>
	 <td><input name="password2" type="password" value=''>
         <td><% if (inps.errorsFoundFor("password2")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("password2"))%></font><br />
             <% } %>
        </td>
       </tr>
       <tr>
	 <td style="padding-top:0.5em" colspan="3"><em>By clicking, on
         "Register" below, you agree to our 
         <a href="/privacy-policy.html" target="_tac">privacy policy
         and restrictions on use</a></em></td>
       </tr>
       <tr>
         <td></td>
	 <td style="padding-top:0.5em"><input value="Register" type="submit"></td>
         <td></td>
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
    String formatErrors(String[] msgs) {
        StringBuilder sb = new StringBuilder("\n");
        for(String msg : msgs) {
            if (sb.length() > 0) sb.append("<br />\n");
            sb.append(msg);
        }
        return sb.toString();
    }
%>
