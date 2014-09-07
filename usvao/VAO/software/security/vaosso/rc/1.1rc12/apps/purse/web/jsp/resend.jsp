<%@ page import='org.globus.purse.registration.*' %>
<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import="org.globus.purse.registration.mailProcessing.StatusMessage" %>
<%!
    private static final String ACTION_RESEND = "Re-send Registration Email";
    private static final String KEY_NAME = "name";
%>
<%@ include file="dbinit.include" %>
<%
    String action = request.getParameter("action");

    String name = request.getParameter(KEY_NAME);
    if (name == null) name = "";
    boolean resending = name.length() > 0 && ACTION_RESEND.equals(action);

    StatusMessage status = null;
    if (resending)
        status = RegisterUser.resendEmail(name);
    String messageColor = (status != null && status.isSuccessful()) ? "#083" : "#830";
%>

<html>
<head>
<% String title = "Our Logon Registration - Re-send Registration Email"; %>
<%@ include file="head.include" %>
<style>
    p { margin-top: 1.5em; margin-bottom: 1.5em }
</style>
</head>

<body style="margin:0">

<%@ include file="body.open.include" %>

<h1 class="top">Re-send Registration Email</h1>

<% if (resending && status != null) { %>
    <p style="font-size:large; color:<%=messageColor%>; font-weight:bold">
        <%=status.getDescription()%>
    </p>
<% } %>

<p>Request a new copy of a registration email, which will include an account activation code & link:</p>

<form method="post" action="resend.jsp">
Login name or email address:
<input type="text" name="<%=KEY_NAME%>" value="<%=name%>" />
<input type="submit" name="action" value="<%=ACTION_RESEND%>" />
</form>

<h1>Password Management</h1>

<p>
    <a href="password.jsp">Use this form</a> to request a password reminder or to change your password.
</p>

<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>
