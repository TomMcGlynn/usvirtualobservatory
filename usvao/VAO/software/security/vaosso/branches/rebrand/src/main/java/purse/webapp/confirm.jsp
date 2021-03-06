<%-- apologies for the glomming: can't allow any output before a response.sendRedirect() because it can cause an IllegalStateException --%><%@
 page import='java.util.*' %><%@
 page import="java.util.regex.*" %><%@
 page import='java.io.*' %><%@
 page import="org.globus.purse.util.HtmlEncode" %><%@
 page import='org.globus.purse.registration.*' %><%@
 page import='org.globus.purse.exceptions.*' %><%@
 page import='org.globus.purse.registration.databaseAccess.StatusDataHandler' %><%@
 page import='org.globus.purse.registration.databaseAccess.UserDataHandler'

 %><jsp:useBean id="formHandler" class="nvo.VORegForm" scope="request"/><jsp:setProperty name="formHandler" property="*" /><%@

 include file="dbinit.include" %><%!

    String errorMsg = null;
    String fullname = null;
    String username = null;
    static final int SLEEP = 5;

%><%

    ServletContext sc = getServletConfig().getServletContext();

    String token = request.getParameter("token");
    String portalName = null;
    String returnURL = null;

    if (isBlank(token)) {
        errorMsg = "Please enter a token.";
    } else {
        // verify the token
        sc.log("got token = ["+token+"] from "+request.getRemoteAddr());
        try {
            RegisterUser.acceptUser(token, "keymaster");

            UserData userData = UserDataHandler.getData(token);
            fullname = userData.getLastName();
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
            errorMsg = e.getMessage();
        } catch (RegistrationException e) {
            errorMsg = e.getMessage();
            sc.log("Unexpected error.", e);
        }
    }
    if (errorMsg != null) {
        String errorUrl = "process.jsp?" + formHandler.makeArgs() + "&confirmError=" + HtmlEncode.encodeHREFParam(errorMsg);
        response.sendRedirect(errorUrl);
    }
%>
<html>
<head>
<% String title = "NVO User Registration Complete"; %>
<%@ include file="head.include" %>

<%-- return the user to the portal after SLEEP seconds -- vestigial since now we just sendRedirect without a delay --%>
<% if (!isBlank(returnURL)) { %><meta http-equiv="refresh" content="<%=SLEEP%>;<%= returnURL %>"><% } %>
</head>

<body>
<%@ include file="body.open.include" %>
<h1 class="top">Welcome, <%= fullname %>, to the Secured Side of the NVO</h1>

<p>
Congratulations!  Your NVO Logon registration is complete.  Your NVO identity is
<strong><%=username%></strong>.  You may use it to log into any NVO-compliant
portal.
</p>

<% if (!isBlank(returnURL)) { %>
    <p>
    In about <%=SLEEP%> seconds, you will be returned to
    <a href="<%=returnURL%>"><%=(!isBlank(portalName)) ? decode(portalName) : returnURL%></a>,
    where you may be redirected to log in to the NVO.
    </p>
<% } %>

<div style="margin:1em 2em; padding:0.5em; background-color:#eee; border: 2px solid #666">
<dl>
   <dt> <strong><em>Remember: </em></strong>  </dt>
   <dd> Only enter your NVO password into the NVO Login Page on this
        official site, <strong>sso.us-vo.org</strong>.  For more information, see the
        <a href="http://nvologin1.ncsa.uiuc.edu/">NVO Identity Portal</a>.  </dd>
</dl>
</div>

<p>
<strong>Credentials</strong>
</p>

<p>
The NVO can supply you and the NVO-compliant portals you visit with a
<em>standard</em> <a href="http://nvologin1.ncsa.uiuc.edu/">security
credential</a>, which has been verified by email exchange.  In the future, some
services may require stronger verifications of your identity than what you've
just given by responding to our email.  To be issued an <em>enhanced</em>
certificate, the NVO will obtain confirmation of your identity from a recognized
authority.
<!--For more information about how to obtain an enchanced
certificate (or confirm the identities of other people at your institution),
please consult our page on <a href="/aboutNVOLogons/enhanced.html">Enhanced
Certificates</a>.-->
</p>

<div class="divider"><hr noshade="noshade"></div>

<p align="center" style="color: #003366">
If you have any questions or comments, please contact us at
<a href="mailto:ysvenkat@ncsa.illinois.edu">ysvenkat@ncsa.illinois.edu</a>.
</p> 

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
