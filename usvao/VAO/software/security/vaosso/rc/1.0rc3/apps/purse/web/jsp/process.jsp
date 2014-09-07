<%@ page import="java.util.regex.*" %>
<jsp:useBean id="formHandler" class="nvo.VORegForm" scope="request"/>
<jsp:setProperty name="formHandler" property="*" />
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.Enumeration" %>

<%-- Purse stuff --%>
<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.registration.*' %>
<%@ page import='org.globus.purse.exceptions.*' %>
<%@ page import='org.globus.purse.util.HtmlEncode' %>
<%@ page import='org.globus.purse.registration.databaseAccess.StatusDataHandler' %>
<%@ page import='org.globus.purse.registration.databaseAccess.UserDataHandler' %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ include file="dbinit.include" %>

<%
    String confError = request.getParameter("confirmError");
    formHandler.setPortalName(decode(formHandler.getPortalName()));
    if (confError == null && !formHandler.validate()) {
        // redirect with error messages
        session.setAttribute("username",  formHandler.getUserName());
        session.setAttribute("errors", formHandler.getErrors());
	String registerUrl = response.encodeRedirectURL("register.jsp?" + formHandler.makeArgs());
        response.sendRedirect(registerUrl);
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
            try {
                javax.servlet.ServletContext sc = getServletConfig().getServletContext();
                sc.log("PURSE initializing");

                UserData userData = new UserData(
                        formHandler.getFirstName()/*firstName*/, 
                        formHandler.getLastName()/*lastName*/,
                        ""/*contactPerson*/, ""/*stmtOfWork*/,
                        formHandler.getUserName(),
                        UserDataHandler.passwordSha1(formHandler.getPassword1()),
                        formHandler.getInst(), "" /*projectName*/,
                        formHandler.getEmail(), formHandler.getPhone(),
                        formHandler.getCountry(),
                        formHandler.getReturnURL(), formHandler.getPortalName(),
                        StatusDataHandler.getId(RegisterUtil.getRequestStatus()));

                RegisterUser.register(userData);

            } catch (MailAccessException e) {
                confError = "TokenMailAccess";
            } catch (Exception e) {
                getServletConfig().getServletContext().log(
                   "Error while registering user " + formHandler.getUserName(), 
                   e);
                confError = e.getMessage();
            }
        }
%>
<html>
<head>
<% String title = "VAO User Logon Registration"; %>
<%@ include file="head.include" %>
</head>

<body>
<%@ include file="body.open.include" %>
<br />

<div class="main-content">
<h1 class="top">Step 1 Completed Successfully</h1>

<p>You have registered the following identity information with the VAO:</p>

<blockquote> <table border="0" cellspacing="0">
    <tr><td>Full Name: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getFirstName()) %>
                    <%=HtmlEncode.encode(formHandler.getLastName()) %></strong></td> </tr>
    <tr><td>User Name: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getUserName()) %></strong></td> </tr>
    <tr><td>Institution/Company: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getInst()) %></strong></td> </tr>
    <tr><td>Email: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getEmail()) %></strong></td> </tr>
    <tr><td>Phone: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getPhone()) %></strong></td> </tr>
    <tr><td>Country: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getCountry()) %></strong></td> </tr>
</table> </blockquote>

<h1>Step 2: Confirm Your Identity</h1>

<% if (confError == "UnknownToken") {%>
<div class="error_box"><p>
<b>Oops -- we did not recognize your confirmation token.</b>
</p>  

<p>
Please try again below, or if the error persists or you feel
confidant that the token is correct, please contact us via the VAO
<a href="http://www.usvao.org/contact.php">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Unknown Token" in the subject line.  </p></div>

<% } else if (confError == "TokenMailAccess") {%>
<div class="error_box"><p>
<b>We're sorry! -- We appear unable to confirm your email address.</b>
</p>  

<p>
It appears that we failed in an attempt to send you a confirmation email.
This email was to contain a special confirmation token you would use to 
confirm your receipt of the email.  If you actually did receive this email, 
you can continue with the confirmation process below.  Otherwise,
please contact us for more assistance via the VAO 
<a href="http://www.usvao.org/contact.php">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Confirmation Email Error" in the subject line.  </p></div>


<% } else if (confError != null) {%>
<div class="error_box"><p>
<b>We're sorry!  We experienced an internal error while confirming
your token.</b>
</p>  

<p>
You can try submitting your confirmation code again below.  However,
if the problem persists, please contact us for further assistance via
the VAO  
<a href="http://www.usvao.org/contact.php">feedback page</a>; be
sure to include in your message your username as shown above and the
words, "Login: Confirmation Error" in the subject line.  </p></div>
</p>

<% } else {%>
<p>
To confirm that your email address is correct, we have just sent you an
email with a confirmation token.  
</p>
<%}%>

<p>To confirm, you can either:</p>

<ul>
  <li><p>Access the link that appears in that email.</p>
       <p>&#8212; OR &#8212;</p>
  <li><p>Paste the confirmation token given in that email here:</p>

       <form method="post" action="confirm.jsp">
       Confirmation Token: &nbsp;&nbsp;
           <%=formHandler.makeHiddenFields()%>
       <input name="token" type="text" /> &nbsp;&nbsp;
       <input name="Confirm" type="submit" value="Confirm" />
       </form>
</ul>
</div>

<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>
<% } %>
<%!
    String decode(String s) {
        if (s == null) return null;
        else return s.replaceAll("%20", " ").replaceAll("\\+", " ");
    }
%>
