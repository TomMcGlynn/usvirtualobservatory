<%@ page import="java.util.regex.*" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import='org.apache.commons.logging.Log' %>
<%@ page import='org.apache.commons.logging.LogFactory' %>

<%-- Purse stuff --%>
<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.registration.*' %>
<%@ page import='org.globus.purse.exceptions.*' %>
<%@ page import='org.globus.purse.util.HtmlEncode' %>
<%@ page import='org.globus.purse.exceptions.UserRegistrationException' %>
<%@ page import='org.globus.purse.registration.databaseAccess.StatusDataHandler' %>
<%@ page import='org.globus.purse.registration.databaseAccess.UserDataHandler' %>
<%@ page import="org.usvao.sso.ip.register.RegistrationFormInputs" %>
<jsp:useBean id="inps" 
             class="org.usvao.sso.ip.register.RegistrationFormInputs" 
             scope="request"/>
<jsp:setProperty name="inps" property="*" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%!
    // send user back to registration form
    void redirectBack(RegistrationFormInputs inps,
                      HttpSession session, HttpServletResponse response) 
        throws IOException
    {
        session.setAttribute("username",  inps.getUserName());
        session.setAttribute("errors", inps.exportErrors());
        String registerUrl = response.encodeRedirectURL("register.jsp?" + inps.toURLArgs());
        response.sendRedirect(registerUrl);
    }
%>

<%@ include file="dbinit.include" %>

<%
    Log logger = LogFactory.getLog("jsp.purse.process");  // goes to catalina.out
    String confError = request.getParameter("confirmError");

    logger.info("Attempting register user " + inps.getUserName());

    if (confError == null && !inps.validate()) {
        // coming from registration form submission (register.jsp)
        // and errors were found; redirect back to register.jsp.
        logger.info("Registration data validation errors detected; " + 
                    "returning user " + inps.getUserName() + 
                    " to registration form");
        redirectBack(inps, session, response);
    }
    // accept the data and create the identity
    else {
        // Ask browser not to cache the page
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        // Causes the proxy cache to see the page as "stale"
        response.setDateHeader("Expires", 0); 

        session.invalidate();

        if (confError == null) {
            // the inputs are valid; load them up.
            try {
                String[] hashNsalt = UserDataHandler.passwordSha(inps.getPassword1());
                UserData userData = new UserData(
                        inps.getFirstName()/*firstName*/, 
                        inps.getLastName()/*lastName*/,
                        ""/*contactPerson*/, ""/*stmtOfWork*/,
                        inps.getUserName(),
                        hashNsalt[0], /* hash */
                        hashNsalt[1], /* salt */
                        hashNsalt[2], /* method */
                        inps.getInst(), request.getRemoteAddr(), 
                        inps.getEmail(), inps.getPhone(),
                        inps.getCountry(),
                        inps.getReturnURL(), inps.getPortalName(),
                        StatusDataHandler.getId(RegisterUtil.getRequestStatus()));

                RegisterUser.register(userData);

            } catch (MailAccessException e) {
                logger.error("Failed to send confirmation token for user " +
                             inps.getUserName() + " to " + inps.getEmail());
                confError = "TokenMailAccess";
            } catch (UserRegistrationException e) {
                // this shouldn't happen as we should have caught all
                // user errors during validation
                logger.error("Unexpected \"user\" error while creating account "
                             + "for " + inps.getUserName() +
                             ": " + e.getMessage());

                // a race on claiming a username may have brought us here;
                // assume this is the case
                inps.addErrorMsg(inps.USERNAME, e.getMessage());
                redirectBack(inps, session, response);
            } catch (Exception ex) {
                confError = "InternalProcess";
                logger.error("Unexpected internal failure: " + ex.getMessage());

                // send stack trace to context log
                ServletContext sc = getServletConfig().getServletContext();
                sc.log(ex.toString());
                StringWriter sb = new StringWriter();
                ex.printStackTrace(new PrintWriter(sb));
                sc.log(sb.toString());
            }
            
        }
%>
<!DOCTYPE HTML>
<html>
<head>
<% String title = "Our User Logon Registration"; %>
<%@ include file="head.include" %>
</head>

<body class="narrow">

<div id="head" class="brief">
<script type="text/javascript" src="/styles/jquery.min.js"></script>
<script type="text/javascript" src="/styles/loginstatus.js"></script>
<script type="text/javascript">
statusdisplay.showLogin = false;
jQuery(document).ready(vaoStatus);
</script>

<div id="banner">
<table width="100%" cellpadding="0" cellspacing="0" border="0">
<tr>
  <td class="banner-logo" align="left" valign="top" width="105" rowspan="3">
     <a href="/"><img src="/images/ProviderLogo.png" border="0" width="100" height="50"></a>
  </td>
  <td align="left" valign="top" rowspan="3">
    <h1>Create A Our Login</h1>
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

<div class="main-content">
<h1 class="top">Step 1 Completed Successfully</h1>

<p>You have registered the following identity information with the Our:</p>

<blockquote> <table border="0" cellspacing="0">
    <tr><td>Full Name: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(inps.getFirstName()) %>
                    <%=HtmlEncode.encode(inps.getLastName()) %></strong></td> </tr>
    <tr><td>User Name: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(inps.getUserName()) %></strong></td> </tr>
    <tr><td>Institution/Company: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(inps.getInst()) %></strong></td> </tr>
    <tr><td>Email: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(inps.getEmail()) %></strong></td> </tr>
    <tr><td>Phone: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(inps.getPhone()) %></strong></td> </tr>
    <tr><td>Country: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(inps.getCountry()) %></strong></td> </tr>
</table> </blockquote>

<h1>Step 2: Confirm Your Identity</h1>

<% if (confError == null) { %>
<p>
To confirm that your email address is correct, we have just sent you an
email with a confirmation token.  
</p>

<% } else if ("UnknownToken".equals(confError)) {%>
<div class="error_box"><p>
<b>Oops -- we did not recognize your confirmation token.</b>
</p>  

<p>
Please try again below, or if the error persists or you feel
confidant that the token is correct, please contact us via the Our
<a href="/contact.html">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Unknown Token" in the subject line.  </p></div>

<% } else if ("EmptyToken".equals(confError)) {%>
<div class="error_box"><p>
<b>Oops -- Did you forget to enter your secret token?</b>
</p>  

<p>
You should have received an email from this service containing a 
confirmation token.  Please paste the token from that email into the
input box below.  </p>
<p>
If you did not get this email (or you continue to see this error after
re-entering your token), please contact us via the Our
<a href="/contact.html/">feedback page</a>; be
sure to include in your message your username and the
words, "Login: Empty Token" in the subject line.  </p></div>

<% } else if ("TokenMailAccess".equals(confError)) {%>
<div class="error_box"><p>
<b>We're sorry! -- We appear unable to confirm your email address.</b> <!-- '-->
</p>  

<p>
It appears that we failed in an attempt to send you a confirmation email.
This email was to contain a special confirmation token you would use to 
confirm your receipt of the email.  If you actually did receive this email, 
you can continue with the confirmation process below.  Otherwise,
please contact us for more assistance via the Our 
<a href="/contact.html">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Confirmation Email Error" in the subject line.  </p></div>


<% } else if ("InternalProcess".equals(confError)) {%>
<div class="error_box"><p>
<b>We're sorry! -- We experienced an internal error while creating your
account.</b> <!-- '-->
</p>  

<p>
It appears that an internal server error occurred while trying to create
your account as requested.  This setup was to include sending you a 
confirmation email which was to contain a special token you would use to 
confirm your receipt of the email.  If you actually did receive this email, 
you can continue with the confirmation process below.  Otherwise,
please contact us for more assistance via the Our 
<a href="/contact.html">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Account Creation Error" in the subject line.  </p></div>


<% } else {
      logger.error("Unable to handle unrecognized error: " + confError);
%>
<div class="error_box"><p>
<b>We're sorry!  We experienced an internal error while confirming <!-- '-->
your token.</b>
</p>  

<p>
You can try submitting your confirmation code again below.  However,
if the problem persists, please contact us for further assistance via
the Our  
<a href="/contact.html">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Confirmation Error" in the subject line.  </p></div>
</p>
<%}%>

<p>To confirm, you can either:</p>

<ul>
  <li><p>Access the link that appears in that email.</p>
       <p>&#8212; OR &#8212;</p>
  <li><p>Paste the confirmation token given in that email here:</p>

       <form method="post" action="confirm.jsp">
       Confirmation Token: &nbsp;&nbsp;
<%=inps.toHiddenInputs()%>
       <input name="token" type="text" size="60"/> &nbsp;&nbsp;
       <input name="Confirm" type="submit" value="Confirm" />
       <% if ("EmptyToken".equals(confError)) { %>&nbsp;&nbsp;<font color="red"><em>Please enter your confirmation token</em></font>
<% } %>       </form>
</ul>
</div>

<hr>
</body>
</html>
<% } %>
