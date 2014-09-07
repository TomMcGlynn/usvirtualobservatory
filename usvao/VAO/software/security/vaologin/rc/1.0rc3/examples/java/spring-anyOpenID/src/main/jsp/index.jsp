<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.core.Authentication" %>
<%@ page import="org.springframework.security.core.userdetails.UserDetails" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String user = null;
    Authentication auth = 
        SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
        Object principal = auth.getPrincipal();
        user = principal.toString();
    }

    // If the user is not logged in, the username will be "anonymousUser"
    // 
    boolean authenticated = ! user.equals("anonymousUser");
    String status = (authenticated) ? "" : "not";
    String asname = (authenticated) ? user : "anybody";
%>
<html>
<head>
    <title>anyOpenID: a Java Spring Example</title>
</head>
<body>
<h1>anyOpenID: A Java Spring Security Example Portal</h1>

<p>
You are <%=status%> logged in as <%=asname%>.
</p>

<p>
<a href="attributes.jsp">Attributes</a> -- 
<%
    if (! authenticated) {
%>
<a href="j_spring_openid_security_check?openid_identifier=https%3a%2f%2fvaossotest.ncsa.illinois.edu%2fopenid%2fprovider_id">Login</a> -- 
<a href="http://vaossotest.ncsa.illinois.edu/register">Get a VAO (Test) Login</a>
<%
    } else {
%>
<a href="j_spring_security_logout">Logout</a>
<%
    }
%>
-- <a href="https://vaossotest.ncsa.illinois.edu/">About VAO Logins</a>

<p>
The Spring framework examples have some common features.  First, this "home" 
page is publicly accessible (i.e. no authentication required); however, what 
is shown depends on whether the user is logged in.  The 
<a href="attributes.jsp">Attributes</a> page, on the other hand, is 
"protected"--that is, accessing it forces the user to log in.  
</p>

<p>
In this example, we assume that any OpenID user may log into our
portal.  By default, users are identified within the portal by their 
OpenID URL; however, this example comes configured to identify a user 
by a shortened qualified name (see more below).  Authorizations are
assigned to the user depending on the whether a recognized 
VAO-compatible service was used to authenticate.  Click on 
<a href="attributes.jsp">Attributes</a> to see your authorizations.
</p>

<p>
This example uses Spring Security's typical support for OpenID apart from 
one specialization.  We have plugged in a <code>UserDetailsService</code>
implementation (SimpleUserDetailsService) which assigns the authorization
roles and determines the form of the username.  This class comes with the 
VAOLogin package and can be plugged in via the configuration file, 
<code>conf/security-app-context.xml</code>.  A portal may plug in their 
own implementation to assign authorizations to the user.  See the code 
embedded in the JSP pages (under <code>src/main/jsp</code>) to see how this 
information is accessed.  
</p>

</body>
</html>
