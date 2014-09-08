<%-- apologies for the glomming: cannot allow any output before a response.sendRedirect() because it can cause an IllegalStateException --%><%@
 page import='java.util.*' %><%@
 page import="java.util.regex.*" %><%@
 page import='java.io.*' %><%@
 page import='org.apache.commons.logging.Log' %><%@
 page import='org.apache.commons.logging.LogFactory' %><%@
 page import="org.globus.purse.util.HtmlEncode" %><%@
 page import='org.globus.purse.registration.*' %><%@
 page import='org.globus.purse.exceptions.*' %><%@
 page import='org.globus.purse.registration.databaseAccess.StatusDataHandler' %><%@
 page import='org.globus.purse.registration.databaseAccess.UserDataHandler'

 %><jsp:useBean id="inps" 
                class="org.usvao.sso.ip.register.RegistrationFormInputs" 
                scope="request"
/><jsp:setProperty name="inps" property="*" 
/><%@ include file="dbinit.include" 
%><%!
    static final int SLEEP = 5;
%><%
    String errorMsg = null;
    String fullname = null;
    String username = null;

    Log logger = LogFactory.getLog("jsp.purse.confirm");  // goes to catalina.out
    logger.info("Confirming token from user " + inps.getUserName());

    String token = request.getParameter("token");
    String portalName = null;
    String returnURL = null;

    if (isBlank(token)) {
        errorMsg = "EmptyToken";
        logger.error("No token provided");

        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("{ ");
            Map<String,String[]> params = request.getParameterMap();
            for(String param : params.keySet()) {
                sb.append(param).append(" : [");
                for(String val : params.get(param)) 
                    sb.append(val).append(", ");
                sb.append("]\n  ");
            }
            sb.append("}");
            logger.debug("request parameters:\n"+sb.toString());
        }
        
    } else {
        // verify the token
        try {
            // validate the token.  A validataion error throw an exception
            RegisterUser.acceptUser(token, "keymaster");

            UserData userData = UserDataHandler.getData(token);
            fullname = userData.getFirstName() + " " + userData.getLastName();
            username = userData.getUserName();
            returnURL = userData.getPortalConfirmUrl();
            if (!isBlank(returnURL)) {
                portalName = userData.getPortalName();
                int q = returnURL.indexOf("?");
                // append &user=<username> to returnURL
                returnURL += ((q >= 0) ? "&" : "?") + "user=" + username;
                response.sendRedirect(returnURL);
            }
        } catch (UnknownTokenException e) {
            errorMsg = "UnknownToken";
        } catch (MailAccessException e) {
            errorMsg = "MailAccess";
        } catch (RegistrationException e) {
            errorMsg = "InternalConfirm";
            logger.error("Unexpected error while confirming registration " +
                         "for user " + username + ": " + e);
        }
    }
    if (errorMsg != null) {
        logger.error("Redirecting back to confirmation page with error=" +
                     errorMsg);
        String errorUrl = "process.jsp?" + inps.toURLArgs() + 
            "&confirmError=" + HtmlEncode.encodeHREFParam(errorMsg);
        response.sendRedirect(errorUrl);
    }
%>
<!DOCTYPE HTML>
<html>
<head>
<% String title = "VAO User Registration Complete"; %>
<%@ include file="head.include" %>

<%-- return the user to the portal after SLEEP seconds -- vestigial since now we just sendRedirect without a delay --%>
<% if (!isBlank(returnURL)) { %><meta http-equiv="refresh" content="<%=SLEEP%>;<%= returnURL %>"><% } %>
</head>

<body>
<%@ include file="body.open.include" %>
<br />
<div class="main-content">

<h1 class="top">Welcome, <%= fullname %>, to the Secured Side of the VAO</h1>

<p>
Congratulations!  Your VAO Logon registration is complete.  
</p>

<p>
Your VAO user name is <strong><%=username%></strong>.  You may use it to log
into any VAO-compliant portal.  
</p>

<p>
You also have an OpenID:
<strong>https://sso.usvao.org/openid/id/<%=username%></strong>.  You
may use this with any portal that accepts arbitrary OpenIDs.
</p>

<% if (!isBlank(returnURL)) { %>
    <p>
    In about <%=SLEEP%> seconds, you will be returned to
    <a href="<%=returnURL%>"><%=(!isBlank(portalName)) ? decode(portalName) : returnURL%></a>,
    where you may be redirected to log in to the VAO with your new logon.
    </p>
<% } %>

<div style="margin:1em 2em; padding:0.5em; background-color:#eee; border: 2px solid #666">
<dl>
   <dt> <strong><em>Remember: </em></strong>  </dt>
   <dd> Only enter your VAO password into the VAO Login Page on this
        official site, <strong>sso.usvao.org</strong>.  For more information, consult our
        <a href="/help/faq.html#phish">help</a>.  </dd>
</dl>
</div>

<p>
<strong>Credentials</strong>
</p>

<p>
The VAO can supply you and the VAO-compliant portals you visit with a
<em>standard</em> security credential, which has been verified by email exchange.  In the future, some 
services may require stronger verifications of your identity than what you've <!--'-->
just given by responding to our email.  To be issued an <em>enhanced</em>
certificate, the VAO will obtain confirmation of your identity from a recognized
authority.
<!--For more information about how to obtain an enchanced
certificate (or confirm the identities of other people at your institution),
please consult our page on <a href="/aboutNVOLogons/enhanced.html">Enhanced
Certificates</a>.-->
</p>

<div class="divider"><hr noshade="noshade"></div>

<p align="center" style="color: #003366">
If you have any questions or comments, please contact us via the 
<a href="http://www.usvao.org/contact-connect/">VAO feedback page</a>.
</p> 

</div>
<%@ include file="body.close.include" %>
</body>
<%@ include file="foot.include" %>
<%!
    String decode(String s) {
        if (s == null) return null;
        else return s.replaceAll("%20", " ").replaceAll("\\+", " ");
    }
    boolean isBlank(String s) { return s == null || s.length() == 0; }
%>
