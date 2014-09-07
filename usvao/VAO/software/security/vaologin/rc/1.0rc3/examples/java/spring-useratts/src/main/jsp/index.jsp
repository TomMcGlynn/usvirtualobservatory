<%@ page import="org.usvao.sso.openid.portal.PortalUser" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    // equivalent to 
    //
    //   PortalUser login = (PortalUser) 
    //     SecurityContextHolder.getContext().getAuthentication().getPrincipal()
    // 
    // If the user is not logged in, the user will be null and
    // username will be "anonymousUser". 
    // 
    //
    PortalUser user = SSFOpenID.getPortalUser();

    boolean authenticated = false, registered = false;
    if (user != null) {
        authenticated = user.isSessionValid(); // should always be true
        registered = user.isRegistered();
    }

    String baseurl = request.getRequestURI();
    int sl = baseurl.lastIndexOf("/");
    if (sl > 0) baseurl = baseurl.substring(0,sl);
%>
<html>
<head>
    <title>useratts: a Java Spring Example</title>
</head>
<body>
<h1>useratts: A Java Spring Security Example Portal</h1>

<p>
<%  if (user == null) { 
%> You are not logged in right now.  <%
    } else {
%> You are logged in as <%= user.getID() %>,<%
        if (registered) {
%> and you are registered to use the portal. <%
        } else {
%> but you are not yet registered to use the portal. <%
        }
    } %>
</p>

<blockquote>
<p>New to our portal?  Not registered?
We support <a href="https://vaossotest.ncsa.illinois.edu/">VAO Logins</a>
</p>
<p>
If you do not have a VAO account, 
<a href="http://vaossotest.ncsa.illinois.edu/register?returnURL=<%=baseurl%>/register">Get one now.</a> <br>
If you already have one, <a href="register/">register now to use this portal.  
</blockquote>

<p>
<a href="register/unregistered-attributes.jsp">Attributes</a> -- 
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
