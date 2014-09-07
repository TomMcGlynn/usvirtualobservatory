<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.usvao.sso.openid.portal.PortalUser" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String name = "", email = "", inst = "", color = "", val = null;

    Log log = LogFactory.getLog("jsp");
    PortalUser user = SSFOpenID.getPortalUser();
    VAOLogin login = user.getLoginInfo();

    // we prevent this error condition via our spring security
    // configuration, se we can handle its un-expected appearance
    // crudely here. 
    if (! user.isSessionValid() || user.getID().equals("anonymousUser")) 
        throw new ServletException("User is not logged in.");

    String disabled = "";
    if (user.isRegistered()) {
        disabled = "disabled=\"1\"";
        name = user.getProperty("fullname", "");
        email = user.getProperty("email", "");
        inst = user.getProperty("inst", "");
        color = user.getProperty("color", "");
    }
    else { 
        val = login.getFullName();
        if (val != null) name = val;
        val = login.getEmail();
        if (val != null) email = val;
        val = login.getInstitution();
        if (val != null) inst = val;
    }

    String who = (name.length()==0) ? user.getID() : name;
%>
<html>
<head>
    <title>PortalUser: a registration page</title>
</head>
<body>
<%
    if (user.isRegistered()) {
%>  
<h1>Welcome back, <%=who%></h1>
<h2> You are already registered to use this portal.</h2>
<%
    } else {
%>
<h1>Welcome, <%=who%></h1>
<h2> Please register to use this portal</h2>
<%
    }
%>

<form method="POST" action="registered.jsp">

<p>
<label>Your real name: </label>
<input type="text" name="fullname" value="<%=name%>" <%=disabled%>/>
</p>

<p>
<label>Your email address: </label>
<input type="text" name="email" value="<%=email%>" <%=disabled%>/>
</p>

<p>
<label>Your institution: </label>
<input type="text" name="inst" value="<%=inst%>" <%=disabled%>/>
</p>

<p>
<label>Your favorite color: </label>
<input type="text" name="color" value="<%=color%>" <%=disabled%>/>
</p>

<input type="submit" value="Complete registration" <%=disabled%>/>

</form>

<p>
<a href="../index.jsp">Home</a> -- 
<a href="unregistered-attributes.jsp">Attributes</a> --
<a href="../j_spring_security_logout">Logout</a>
</p>
</body>
</html>
