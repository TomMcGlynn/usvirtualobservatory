<%@ page import="java.util.Collection" %>
<%@ page import="java.util.List" %>
<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%@ page import="org.springframework.security.core.userdetails.UserDetails" %>
<%@ page import="org.springframework.security.core.GrantedAuthority" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String status = "not";
    String asname = "anybody";
    boolean authenticated = false;

    // equivalent to 
    //
    //   VAOLogin login = (VAOLogin) 
    //     SecurityContextHolder.getContext().getAuthentication().getDetails()
    //
    VAOLogin login = SSFOpenID.getLoginInfo();

    if (login != null) {
        authenticated = login.isAuthenticated();
        asname = login.getUserName();  
        if (asname == null) asname = login.getOpenID();   // shouldn't happen
        status = "";
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
    if (login == null) {
%>
<p>Consequently, I can't tell you anything.  </p>

<a href="j_spring_openid_security_check?openid_identifier=https%3a%2f%2fvaossotest.ncsa.illinois.edu%2fopenid%2fprovider_id">Login</a>
<%
    } else {
%>
<p>Your authorization roles: 
<%
        UserDetails details = SSFOpenID.getUserDetails();
        Collection<? extends GrantedAuthority> authzs = 
                                                     details.getAuthorities();
        for (GrantedAuthority authz : authzs) {
            %><%=authz.toString()%> &nbsp; <%
        }
%></p>

<table cellspacing="0" cellpadding="0" border="0">
<%
        for(String attname : login.getAttributeAliases()) {
%>
<tr><td><%=attname%>: </td><td>&nbsp;</td><td><%=login.getAttribute(attname)%></td></tr>
<%
        }
%>
</table>
<%
    }
%>

<p>
<a href="../index.jsp">Home</a> --
<a href="../j_spring_security_logout">Logout</a>
</p>
</body>
</html>
