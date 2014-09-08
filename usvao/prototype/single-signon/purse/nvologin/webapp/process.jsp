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
                        ""/*firstName*/, formHandler.getName()/*lastName*/,
                        ""/*contactPerson*/, ""/*stmtOfWork*/,
                        formHandler.getUserName(),
                        UserDataHandler.passwordSha1(formHandler.getPassword1()),
                        formHandler.getInst(), "" /*projectName*/,
                        formHandler.getEmail(), formHandler.getPhone(),
                        formHandler.getCountry(),
                        formHandler.getReturnURL(), formHandler.getPortalName(),
                        StatusDataHandler.getId(RegisterUtil.getRequestStatus()));

                RegisterUser.register(userData);

            } catch (Exception e) {
                getServletConfig().getServletContext().log("Registration error.", e);
                confError = e.getMessage();
            }
        }
%>
<html>
<head>
<% String title = "NVO User Logon Registration"; %>
<%@ include file="head.include" %>
</head>

<body>
<%@ include file="body.open.include" %>

<h1 class="top">Step 1 Completed Successfully</h1>

<p>You have registered the following identity information with the NVO:</p>

<blockquote> <table border="0" cellspacing="0">
    <tr><td>Full Name: </td><td>&nbsp;&nbsp;</td>
        <td><strong><%=HtmlEncode.encode(formHandler.getName()) %></strong></td> </tr>
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

<% if (confError != null) { %>
<div class="error_box"><%=confError%></div>
<% } %>

<p>
    To confirm that your email address is correct, we have sent you an email asking you for confirmation.  To confirm, you can either:
</p>

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
