<%@ page import='org.globus.purse.registration.*' %>
<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.apache.commons.logging.Log' %>
<%@ page import='org.apache.commons.logging.LogFactory' %>
<%@ page import="org.usvao.sso.ip.SSOProviderSystemException" %>
<%@ page import="org.usvao.sso.ip.SSOProviderSystemException" %>
<%@ page import="org.usvao.sso.ip.db.UserDatabaseAccessException" %>
<%@ page import="org.usvao.sso.ip.db.NoSuchUserException" %>
<%@ page import="org.usvao.sso.ip.db.AuthenticationException" %>
<%@ page import="org.usvao.sso.ip.pw.WeakPasswordException" %>
<%@ page import="org.usvao.sso.ip.service.InvalidInputsException" %>
<jsp:useBean id="pwsrv" 
             class="org.usvao.sso.ip.service.PasswordServices" 
             scope="request"/>
<jsp:setProperty name="pwsrv" property="*" />
<%!
    String formatErrors(String[] msgs) {
        if (msgs == null || msgs.length == 0) return "";
        StringBuilder sb = new StringBuilder("<font color=\"red\" size=\"2\">\n");
        for(String msg : msgs) {
            if (sb.length() > 0) sb.append("<br />\n");
            sb.append(msg);
        }
        sb.append("</font>");
        return sb.toString();
    }
%>
<%@ include file="dbinit.include" %>
<%
    Log logger = LogFactory.getLog("jsp.purse.password");  // goes to catalina.out

    boolean success = false, resetpw = false;
    String result = null;
    if (pwsrv.changeRequest()) {
        logger.info("Processing change request for user: " + pwsrv.getUserName());
        try {
            if (pwsrv.getToken().length() != 0) resetpw = true;
            pwsrv.execute();
            success = true;
            resetpw = false;
            result = "Password changed.";
        }
        catch (InvalidInputsException ex) {
            String[] msgs = pwsrv.getErrorMsgsFor("");
            result = (msgs != null && msgs.length > 0) 
                ? msgs[0] : "Please correct input problems noted below";
        }
        catch (WeakPasswordException ex) {
            result = "Your requested password is too weak; please enter a stronger password as recommended below";
        }
        catch (NoSuchUserException ex) {
            result = "I can't find this username.  If you have forgotten your username, use the username reminder form below to have it sent to you by email.";
        }
        catch (AuthenticationException ex) {
            result = "The old password is not correct.  If you have forgotten your password, use the form below to have a new one resent to you by email.";
        }
        catch (SSOProviderSystemException ex) {
            result = "Sorry! An internal system failure occurred.  Please try again or contact us via the feedback page.";
        }        
    }
    else if (pwsrv.remindRequest()) {
        logger.info("Looking up usernames by email addresses");
        try {
            pwsrv.execute();
            String[] msgs = pwsrv.getErrorMsgsFor("");
            if (msgs != null && msgs.length > 0) {
               StringBuilder sb = new StringBuilder();
               String blk = null;
               for (String msg: msgs) {
                   if (msg.indexOf("@") >= 0) {
                       if (blk == null) {
                           blk = "</p><blockquote>\n";
                           sb.append(blk);
                       }
                       else {
                           sb.append("<br>");
                       }
                   }
                   else {
                       if (blk != null) sb.append("</blockquote>\n\n<p>");
                       blk = null;
                   }
                   sb.append(msg).append('\n');
               }
               if (blk != null) sb.append("</blockquote>\n");
               result = sb.toString();
            }
            success = true;
        }
        catch (InvalidInputsException ex) {
            String[] msgs = pwsrv.getErrorMsgsFor("");
            result = (msgs != null && msgs.length > 0) 
                ? msgs[0] : "Please correct input problems noted below";
        }
        catch (NoSuchUserException ex) {
            result = "I can't find this username.  If you have forgotten your username, use the username reminder form above to have it sent to you by email.";
        }
        catch (SSOProviderSystemException ex) {
            result = "Sorry! An internal system failure occurred.  Please try again or contact us via the feedback page.";
        }        

    }
    else if (pwsrv.resetRequest()) {
        logger.info("Processing pw reset request for user: " + pwsrv.getUserName());
        try {
            pwsrv.execute();
            success = true;

            String[] msgs = pwsrv.getErrorMsgsFor("");
            if (msgs == null || msgs.length == 0) {
                String msg = "reset: missing success id";
                logger.error(msg);
                throw new SSOProviderSystemException(msg);
            }
            result = msgs[0];  

            if (result.endsWith("Reset")) {
                resetpw = true;
                if (result == "ConfirmedReset") 
                    result = "Your account was not yet activated, but we've now activated it; now you can choose a new password.";
                else 
                    result = "Thank you for responding--you're almost done.  Now choose your new password.";
            }
            else {
                result = "We've just sent you an email with the token you need to reset your password.";
            }
        }
        catch (InvalidInputsException ex) {
            String[] msgs = pwsrv.getErrorMsgsFor("");
            result = (msgs != null && msgs.length > 0) 
                ? msgs[0] : "Please correct input problems noted below";
        }
        catch (NoSuchUserException ex) {
            logger.debug("Requested reset for non-existent user: " + 
                         ex.getUser());
            result = "Username, " + ex.getUser() + 
                     ", not found; consider requesting username reminder above.";
        }
        catch (AuthenticationException ex) {
            result = "Your token was not recognized; check the value we sent you by email and try again, or leave the token input field empty to have it sent again.";
        }
        catch (SSOProviderSystemException ex) {
            result = "Sorry! An internal system failure occurred.  Please try again or contact us via the feedback page.";
            logger.error("Password reset: internal error for user=" +
                         pwsrv.getUserName() + ", token " + 
                         ((pwsrv.getToken().length() == 0) ? "unset:" : "set:")+
                         ex.getMessage());
        }        

    }

    String messageColor = (success) ? "#083" : "#830";

    // Ask the browser not to cache this page
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "no-cache");

    //Causes the proxy cache to see the page as "stale"
    response.setDateHeader("Expires", 0); 

    //HTTP 1.0 backward compatibility 
    response.setHeader("Pragma","no-cache"); 
%>
<!DOCTYPE HTML>
<html>
<head>
<% String title = "VAO Password Management"; %>
<%@ include file="head.include" %>
<style>
    p { margin-top: 1.5em; margin-bottom: 1.5em }
</style>
</head>

<body onload="window.location.hash = '<%= pwsrv.getAction() %>';">

<%@ include file="body.open.include" %>
<% if (pwsrv.getAction().length() != 0) { %>
<script type="text/javascript"> 

</script>
<% } %>
<div class="main-content">

<h1 class="top">VAO Logon Password Management</h1>

<a name="change"></a>
<h2>Change Password</h2>

<% if (result != null &&
       (pwsrv.changeRequest() || (pwsrv.resetRequest() && resetpw)))
   { %>
    <p style="font-size:large; color:<%=messageColor%>; font-weight:bold">
        <%= result %>
    </p>
<% } %>

<form method="post" action="password.jsp">
<% if (resetpw) { %>
<input type="hidden" name="userName" value="<%= pwsrv.getUserName() %>" />
<input type="hidden" name="token" value="<%= pwsrv.getToken() %>" />
<% } %>

<blockquote>
    <table cellpadding="0" cellspacing="4" border="0">
        <tr>
            <td>Login name</td>
            <td><% if (resetpw) {
              %><%=pwsrv.getUserName()%><%
                   } else {
              %><input type="text" name="<%=pwsrv.USERNAME%>" value="<%=pwsrv.getUserName()%>" /><%
                   } %></td>
                       <td><% if (pwsrv.changeRequest()) { %><%= formatErrors(pwsrv.getErrorMsgsFor(pwsrv.USERNAME)) %> <% } %></td>
        </tr>
<% if (!resetpw) { %>
        <tr>
            <td>Current password</td>
            <td><input type="password" name="<%=pwsrv.PASSWORD%>" /></td>
            <td><%= formatErrors(pwsrv.getErrorMsgsFor(pwsrv.PASSWORD)) %> </td>
        </tr>
<% } %>
        <tr><td colspan="3"></td></tr>
        <tr>
            <td>New password</td>
            <td><input type="password" name="<%=pwsrv.NEWPW1%>" /></td>
            <td><%= formatErrors(pwsrv.getErrorMsgsFor(pwsrv.NEWPW1)) %> </td>
        </tr>
        <tr>
            <td>New password again</td>
            <td><input type="password" name="<%=pwsrv.NEWPW2%>" /></td>
            <td><input type="submit" name="action" value="<%=pwsrv.ACTION_CHANGE%>" /></td>
        </tr>
    </table>
</blockquote>
</form>

<p>
<a href="/help/manage.html#change" target="_help">More help on changing
your password...</a>
</p>

<div <% if (resetpw) { %> style="display: none;" <% } %> >
<a name="remind"></a>
<h2>Username Reminder Email</h2>

<p>
    Forgotten your username?  We can try to remind you via an email.  
    To request a reminder, please enter one or more email addresses that you 
    believe you have used to register your account.  If any of them match 
    addresses in our database, we'll send to each email address the 
    username (or usernames) associated with the address.  It will also 
    describe how to reset your password if you've forgotten that too.  
</p>

<% if (pwsrv.remindRequest() && result != null) { %>
    <div style="color:<%=messageColor%>; font-weight:bold">
        <p>
        <%= result %>
        </p>
    </div>
<% } %>

<form method="post" action="password.jsp">

<blockquote>
<table width="90%" cellpadding="0" cellspacing="4" border="0">
   <tr><td width="35%"> 
     Enter the email you used to register the account.  (More than one can 
     be added, <em>one per line</em>, if you're not sure.)   <%-- ' --%>
   </td><td>
     <textarea name="<%=pwsrv.EMAILS%>" rows="4" cols="40"><%=pwsrv.getEmails()%>
</textarea>
   </td><td>
   <%= formatErrors(pwsrv.getErrorMsgsFor(pwsrv.EMAILS)) %>
   </td></tr>
</table>
<input type="submit" name="action" value="<%=pwsrv.ACTION_REMIND%>" />
</blockquote>
</form>

<p>
<a href="/help/manage.html#username" target="_help">More help on username
reminders...</a>
</p>

<a name="reset"></a>
<h2>Reset Your Password</h2>

Forgotten your password?  We'll let you reset it in a two-step process.  
First, enter your username below and hit "submit request".
We'll send you a special token that authenticates your identity and which
you can enter below along with your username; you will then be able to 
choose a new password.  

<% if (pwsrv.resetRequest() && result != null) { %>
    <p style="font-size:large; color:<%=messageColor%>; font-weight:bold">
        <%= result %>
    </p>
<% } %>

<form method="post" action="password.jsp">

<blockquote>
<table width="100%" cellpadding="0" cellspacing="4" border="0">
  <tr><td>
      Your Username:
     </td><td>
      <input type="text" name="<%=pwsrv.USERNAME%>" value="<%=pwsrv.getUserName()%>" />
     </td><td>
      <%= formatErrors(pwsrv.getErrorMsgsFor(pwsrv.USERNAME)) %>
  </td></tr>
  <tr><td>
      Secret Token: <font color="green" size="2">if you have one</font>
     </td><td>
      <input type="text" name="<%=pwsrv.TOKEN%>" value="<%=pwsrv.getToken()%>" />
     </td><td>
      <%= formatErrors(pwsrv.getErrorMsgsFor(pwsrv.TOKEN)) %>
  </td></tr>
</table>
<input type="submit" name="action" value="<%=pwsrv.ACTION_RESET%>" />
</blockquote>
</form>

<p>
<a href="/help/manage.html#reset" target="_help">More help on resetting
your password...</a>
</p>

<h2>Re-send Registration Email</h2>

<p>
    <a href="resend.jsp">Use this form</a> to re-issue a registration email.
</p>

</div>
</div>

<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>
