<%@ page import="java.util.Collection" %>
<%@ page import="java.util.List" %>
<%@ page import="org.usvao.sso.openid.portal.PortalUser" %>
<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%@ page import="org.springframework.security.core.userdetails.UserDetails" %>
<%@ page import="org.springframework.security.core.GrantedAuthority" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    // equivalent to 
    //
    //   PortalUser user = (PortalUser) 
    //     SecurityContextHolder.getContext().getAuthentication().getPrincipal()
    // 
    // a null return value means that the user has not logged in
    // 
    PortalUser user = SSFOpenID.getPortalUser();

    if (user == null) {
        // this should not happen if we have configured access to this
        // page correctly.
        LogFactory.getLog("JSP").error("reached protected page without " +
                                       "authentication; check spring security "+
                                       "configuration");
        response.sendRedirect("../index.jsp");
        return;
    }
    if (user.isAuthorized(user.ROLE_REGISTERED_USER)) {
        // send them to a page that provides more information for registered
        // users
        response.sendRedirect("../portal/attributes.jsp");
        return;
    }

%>
<html>
<head>
    <title>Test App</title>
</head>
<body>
<h1>What we know about you</h1>

<p>
You are logged in as <%= user.getID() %>, but you are not yet
registered to use this portal.
</p>

<h4>Your authorization roles: </h4>
<%
        Collection<String> authzs = user.getAuthorizations();
        for (String role : authzs) {
            %><%=role%> &nbsp; <%
        }
%></p>

<h4> User Attributes </h4>

<p>From the login service: </p>

<table cellspacing="0" cellpadding="0" border="0">
<%
        VAOLogin login = user.getLoginInfo();
        for(String attname : login.getAttributeAliases()) {
%>
<tr><td><%=attname%>: </td><td>&nbsp;</td><td><%=login.getAttribute(attname)%></td></tr>
<%
        }
%>
</table>

<p>
<a href="../index.jsp">Home</a> -- <a href="index.jsp">Register</a> --
<a href="../j_spring_security_logout">Logout</a>
</p>
</body>
</html>
