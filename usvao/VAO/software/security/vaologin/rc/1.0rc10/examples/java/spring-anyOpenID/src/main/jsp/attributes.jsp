<%@ page import="java.util.Collection" %>
<%@ page import="java.util.List" %>
<%@ page import="org.usvao.sso.openid.portal.PortalUser" %>
<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.userdetails.UserDetails" %>
<%@ page import="org.springframework.security.core.Authentication" %>
<%@ page import="org.springframework.security.core.GrantedAuthority" %>
<%@ page import="org.springframework.security.openid.OpenIDAuthenticationToken" %>
<%@ page import="org.springframework.security.openid.OpenIDAttribute" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String username = null;
    String status = "not";
    String asname = "anybody";
    boolean authenticated = false;

    OpenIDAuthenticationToken oidauth = null;
    Authentication auth = 
        SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
        username = auth.getPrincipal().toString();
        authenticated = ! username.equals("anonymousUser");

        if (authenticated) {
            if (auth instanceof OpenIDAuthenticationToken) {
                oidauth = (OpenIDAuthenticationToken) auth;
            }

            status = "";
            asname = username;
        }
    }
%>
<html>
<head>
    <title>Test App</title>
</head>
<body>
<h1>What we know about you</h1>

<p>
You are <%=status%> logged in as <%=asname%>.
</p>

<%
    if (auth == null) {
%>
<p>Consequently, I can't tell you anything.  </p>

<a href="j_spring_openid_security_check?openid_identifier=https%3a%2f%2fvaossotest.ncsa.illinois.edu%2fopenid%2fprovider_id">Login</a>
<%
    } else {
%>
<p>Your authorization roles: 
<%
        Collection<? extends GrantedAuthority> authzs = auth.getAuthorities();
        for (GrantedAuthority authz : authzs) {
            %><%=authz.toString()%> &nbsp; <%
        }
%></p>

<%
        if (oidauth == null) {
%>
<p>You have an unrecognized identity, so I can't tell you anything more.</p>
<%
        }
        else {
            List<OpenIDAttribute> atts = oidauth.getAttributes();
%>
<table cellspacing="0" cellpadding="0" border="0">
<%
            for(OpenIDAttribute att : atts) {
%>
<tr><td><%=att.getName()%>: </td><td>&nbsp;</td><td><%=att.getValues().get(0)%></td></tr>
<%
            }
%>
</table>
<%
        }
%>

<p>
<a href="index.jsp">Home</a> --
<a href="j_spring_security_logout">Logout</a>
</p>
<%
    }
%>
</body>
</html>
