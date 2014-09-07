<%@ page import='org.globus.purse.registration.*' %>
<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import="org.globus.purse.registration.mailProcessing.StatusMessage" %>
<%!
    private static final String ACTION_EMAIL = "Email Password",
            ACTION_CHANGE = "Change Password";
    private static final String KEY_USERNAME = "username", KEY_PASSWORD = "password",
            KEY_NEW_PW1 = "newpw1", KEY_NEW_PW2 = "newpw2";
%>
<%@ include file="dbinit.include" %>
<%
    String action = request.getParameter("action");

    String username = request.getParameter(KEY_USERNAME);
    if (username == null) username = "";
    boolean emailing = username.length() > 0 && ACTION_EMAIL.equals(action);
    boolean changing = username.length() > 0 && ACTION_CHANGE.equals(action);

    StatusMessage status = null;
    if (emailing)
        status = RegisterUser.remindPassword(username, request.getRequestURL());
    else if (changing) {
        String password = request.getParameter(KEY_PASSWORD),
                newpw1 = request.getParameter(KEY_NEW_PW1),
                newpw2 = request.getParameter(KEY_NEW_PW2);
        status = RegisterUser.changePassword(username, password, newpw1, newpw2);
    }
    String messageColor = (status != null && status.isSuccessful()) ? "#083" : "#830";
%>

<html>
<head>
<% String title = "VAO Password Management"; %>
<%@ include file="head.include" %>
<style>
    p { margin-top: 1.5em; margin-bottom: 1.5em }
</style>
</head>

<body style="margin:0">

<%@ include file="body.open.include" %>

<h1 class="top">VAO Logon Password Management</h1>

<h2>Change Password</h2>

<% if (changing && status != null) { %>
    <p style="font-size:large; color:<%=messageColor%>; font-weight:bold">
        <%=status.getDescription()%>
    </p>
<% } %>

<form method="post" action="password.jsp">

    <table cellpadding="0" cellspacing="4" border="0">
        <tr>
            <td>Login name</td>
            <td><input type="text" name="<%=KEY_USERNAME%>" value="<%=username%>" /></td>
        </tr>
        <tr>
            <td>Current password</td>
            <td><input type="password" name="<%=KEY_PASSWORD%>" /></td>
        </tr>
        <tr><td colspan="2"></td></tr>
        <tr>
            <td>New password</td>
            <td><input type="password" name="<%=KEY_NEW_PW1%>" /></td>
        </tr>
        <tr>
            <td>New password again</td>
            <td><input type="password" name="<%=KEY_NEW_PW2%>" /></td>
            <td><input type="submit" name="action" value="<%=ACTION_CHANGE%>" /></td>
        </tr>
    </table>

</form>
    
<h2>Password Reminder Email</h2>

<p>
    To request a password reminder, please enter your <em>account name or email address</em>
    below; your password will be sent to the email address that you registered with.
</p>

<% if (emailing && status != null) { %>
    <p style="font-size:large; color:<%=messageColor%>; font-weight:bold">
        <%=status.getDescription()%>
    </p>
<% } %>

<form method="post" action="password.jsp">
Email address or login name
<input type="text" name="<%=KEY_USERNAME%>" value="<%=username%>" />
<input type="submit" name="action" value="<%=ACTION_EMAIL%>" />
</form>

<h2>Re-send Registration Email</h2>

<p>
    <a href="resend.jsp">Use this form</a> to re-issue a registration email.
</p>

<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>
