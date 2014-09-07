<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    boolean authenticated = false;
    String username = null;

    // equivalent to 
    //
    //   VAOLogin login = (VAOLogin) 
    //     SecurityContextHolder.getContext().getAuthentication().getDetails()
    //
    VAOLogin login = SSFOpenID.getLoginInfo();

    if (login != null) {
        authenticated = login.isAuthenticated();

        // equivalient to 
        // 
        //  username = ((UserDetails) SecurityContextHolder.getContext().
        //                  getAuthentication().getPrincipal()).getUsername();
        //
        username = SSFOpenID.getLocalUserName();
    }

    // If the user is not logged in, the username will be "anonymousUser"
    // 
    String status = (authenticated) ? "" : "not";
    String asname = (authenticated) ? username : "anybody";

    String baseurl = request.getRequestURI();
    int sl = baseurl.lastIndexOf("/");
    if (sl > 0) baseurl = baseurl.substring(0,sl);
%>
<html>
<head>
    <title>Registration: a Java Spring Example</title>
</head>
<body>
<h1>Regiatration: A Java Spring Security Example Portal</h1>

<p>
You are <%=status%> logged in as <%=asname%>.
</p>

<blockquote>
<p>New to our portal?  
We support <a href="https://vaossotest.ncsa.illinois.edu/">VAO Logins</a>
</p>
<p>
If you do not have a VAO account, 
<a href="http://vaossotest.ncsa.illinois.edu/register?returnURL=<%=baseurl%>/portal/register.jsp">Get one now.</a> <br>
If you already have one, <a href="portal/register.jsp">register now to use this portal.  
</blockquote>

<p>
<a href="portal/attributes.jsp">Attributes</a> -- 
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
-- 

<p>
The Spring framework examples have some common features.  First, this "home" 
page is publicly accessible (i.e. no authentication required); however, what 
is shown depends on whether the user is logged in.  The 
<a href="portal/attributes.jsp">Attributes</a> page, on the other hand, is 
"protected"--that is, accessing it forces the user to log in.  
</p>

<p>
In this example, we assume that a VAO Login is required to use the portal
and that users must register upon their first visit.  Users are identified
their VAO username (or possibly a qualified name).  Registration can happen 
whether the user starts with a VAO Login or not, as implied above.  This 
page shows how to populate a local registration form with user information 
obtained from the VAO Login Service (via OpenID attribute exchange).  
</p>

<p>
This example provides the portal developer with an instance of the
optional helper class, <code>VAOLogin</code>, which eases access to
login information, particularly OpenID attributes.  To make this
object available, we provide a <code>VAOUserDetailsService</code>
bean to the <code>user-service-ref</code> in the
<code>&lt;openid&gt;</code> configuration (within the
<code>conf/security-app-context.xml</code> file).  If you want to
provide your own <code>UserDetailsService</code>, provide its bean
reference to the <code>VAOUserDetailsService</code> bean.  If you
provide your own <code>UserDetailsService</code> directly to the
<code>user-service-ref</code> attribute, it will still work, but you
won't get <code>VAOLogin</code> object created for you.
</p>

</body>
</html>
