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

    boolean fromAPortal = (inps.getPortalName().length() > 0);
    String param;
%>
<!DOCTYPE HTML>
<html>
<head>
<% String title = "Our User Logon Registration"; %>
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

<body class="narrow">

<div id="head" class="brief">
<script type="text/javascript" src="/styles/jquery.min.js"></script>
<script type="text/javascript" src="/styles/loginstatus.js"></script>
<script type="text/javascript">
statusdisplay.showLogin = false;
var updateForStatus = function(status) {
    displayStatus(status);
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
};

function dologout() {
    jQuery.get("/openid/?logout=true", function(data) { 
            jQuery.get("/openid/loginStatus", updateForStatus);
        });
}

jQuery(document).ready(function() {
    jQuery.getJSON("/openid/loginStatus", updateForStatus);
});
</script>

<div id="banner">
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
  <td class="banner-logo" align="left" valign="top" width="105" rowspan="3">
     <a href="/"><img src="/images/ProviderLogo.png" border="0" width="61" height="50"></a>
  </td>
  <td align="left" valign="top" rowspan="3">
    <h1>
    Create A Our Login <% if (fromAPortal) { %>for <%=HtmlEncode.encode(inps.getPortalName())%> <% } %>
    </h1>
  </td>
</tr>
</table>
</div> <!-- div: id=banner -->

<div class="nav-bar">
<table cellpadding="0" cellspacing="0" width="100%" >
<tr class="buttons">
  <td width="50">
	<a href="/">About</a></td>
  <td style="color: green;" align="left"> <span class="login-label">Status:</span>&nbsp;
<span id="loginStatus" align="center" > Unknown </span> </td>
  <td width="50">
	<a href="/help/faq.html">FAQ</a></td>
</tr>
</table>
</div> <!-- div: class=nav-bar -->
</div> <!-- div: id=head -->

<div class="main-content" style="margin-top: 1.0em;">

<% if (inps.errorsFound()) { %>

<div class="error_box">
<h2>Registration Input Error</h2>

<p>
We&#39;re sorry--we didn&#39;t quite get that.  Please update your inputs 
below to address the following issues:
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

<% if (fromAPortal) { %>
<p class="announce">
  Hello.  You have been sent here from <%=HtmlEncode.encode(inps.getPortalName())%>
  because you need a Our Login to use that portal.  
</p>

<% if (inps.getReturnURL().length() > 0) { %>
<p class="announce">
  <span style="font-size: medium">
    When registration and login are complete, you'll be returned to<br>
    <span class="url"><%=HtmlEncode.encode(inps.getReturnURL())%></span>
  </span>
</p>
<% }} %>

<p>
You are about to create your own <a href="/" onclick="popupHelp('/help/GoodPasswords.html'); return false">Our Login</a>&#8212;a
single unique identity which you can use to log onto any Our-compliant
portal.  Creating a Our Login is not required to use most Our
services; however, a Our Login will give you access to enhanced
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
<dt>Creating a Our Login is a two step process. <p>
   <dd> <strong>Step 1:</strong> 
	Complete the registration form below.
   <dd> <strong>Step 2:</strong>   
	Confirm your identity by replying to a confirmation email
</dl>

<p>Do you need a confirmation email to be resent? You may <a href="/purse/resend.jsp">request another copy</a>.</p>

<p>Forgot your <b>password</b>?  You may <a href="/purse/password.jsp">request a reminder</a>.</p>

<% } %>

<a name="step1"></a>
<h2>Step 1: Fill out and submit the registration form</h2>

<form method="post" action="/purse/process.jsp">
<% if (inps.getReturnURL().length() > 0) { %>
    <input type="hidden" name="returnURL"
           value="<%= HtmlEncode.encode(inps.getReturnURL()) %>" />
<% } %>
<input type="hidden" name="portalName"
       value="<%= HtmlEncode.encodeHREFParam(inps.getPortalName()) %>" />

<center>
   <table border="0" width="95%">
     <tbody>
       <tr>
	 <td width="150em">First Name(s)</td>
	 <td width="190em"><input name="firstName" size="20" type="text" value='<%=HtmlEncode.encode(inps.getFirstName())%>'></td>
         <td align="left"><% if (inps.errorsFoundFor("firstName")) { %>
             <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("firstName"))%></font><br /> <% } %>
             <em><font color="green"><span title='Include first or middle names/initials as desired&#39;(e.g. "John Paul", "Tom T.", "F. Scott").'>Include initials as desired...</span></font></em>
	 </td>
       </tr>

       <tr>
	 <td>Last Name</td>
	 <td><input name="lastName" type="text" size="20" 
		    value='<%=HtmlEncode.encode(inps.getLastName())%>'></td>
         <td align="left"><% if (inps.errorsFoundFor("lastName")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("lastName"))%></font> <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Institution or Company</td>
	 <td><input name="inst" type="text" size="20"
		    value='<%=HtmlEncode.encode(inps.getInst())%>'></td>
         <td align="left"><% if (inps.errorsFoundFor("inst")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("inst"))%></font><br />
             <% } %>
             <em><font color="green">optional</font></em>
	 </td>
       </tr>

       <tr>
	 <td>Email Address</td>
	 <td><input name="email" type="text" size="20"
		    value='<%=HtmlEncode.encode(inps.getEmail())%>'></td>
         <td align="left"><% if (inps.errorsFoundFor("email")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("email"))%></font><br />
             <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Type Email Again</td>
	 <td><input name="email2" type="text" size="20"
		    value='<%=HtmlEncode.encode(inps.getEmail2())%>'></td>
         <td align="left"><% if (inps.errorsFoundFor("email2")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("email2"))%></font><br />
             <% } %>
	 </td>
       </tr>

       <tr>
	 <td>Phone</td>
	 <td><input name="phone" type="text" size="20"
		    value='<%=HtmlEncode.encode(inps.getPhone())%>'></td>
         <td align="left"><% if (inps.errorsFoundFor("phone")) { %>
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
</center>

<p>
The username and password you provide below can be used at any 
Our-compliant portal.  
</p>

<p>
A password must be at least 6 characters long and should follow 
<a href="/help/GoodPasswords.html" 
   onclick="popupHelp('/help/GoodPasswords.html'); return false">good 
practices for secure passwords</a>.  
</p>

<center>

   <table border="0" width="95%">
     <tbody>
       <tr>
	 <td width="150pt">Choose a username</td>
	 <td><input name="userName" type="text" size="20" value="<%=HtmlEncode.encode(inps.getUserName()) %>"> </td>
         <td align="left"><% if (inps.errorsFoundFor("userName")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("userName"))%></font><br />
             <% } %>
	 </td>
       </tr>
       <tr>
	 <td><a href="/help/GoodPasswords.html"
		onclick="popupHelp('/help/GoodPasswords.html'); return false">Choose a
		password</a>:</td>
	 <td><input name="password1" type="password" size="20" value=''>
         <td align="left"><% if (inps.errorsFoundFor("password1")) { %>
	     <font size=2 color=red><%=formatErrors(inps.getErrorMsgsFor("password1"))%></font><br />
             <% } %>
        </td>
       </tr>
       <tr>
	 <td>Re-enter the password:</td>
	 <td><input name="password2" type="password" size="20" value=''>
         <td align="left"><% if (inps.errorsFoundFor("password2")) { %>
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
</center>

</form>
</div>
<hr>

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
</body>
</html>
