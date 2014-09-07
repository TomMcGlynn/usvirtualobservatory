<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String name = "", email = "", inst = "", user = null, val = null;

    Log log = LogFactory.getLog("jsp");
    Object details = 
           SecurityContextHolder.getContext().getAuthentication().getDetails();
    if (! (details instanceof VAOLogin))
        log.info("VAOLogin not set as details: " + 
                 details.getClass().getName());

    VAOLogin login = null;
    try {
//        login = (VAOLogin) 
//           SecurityContextHolder.getContext().getAuthentication().getDetails();
        login = (VAOLogin) details;
           
        val = login.getFullName();
        if (val != null) name = val;
        val = login.getEmail();
        if (val != null) email = val;
        val = login.getInstitution();
        if (val != null) inst = val;

        user = name;
        if (user == null || user.length() == 0)
            user = login.getUserName();
    } 
    catch (ClassCastException ex) { 
        log.error("failed to cast details to VAOLogin");
    }
    catch (NullPointerException ex) { 
        log.error("something was null!");
    }
%>
<html>
<head>
    <title>VAOLogin: a registration page</title>
</head>
<body>
<h1>Welcome <%=user%>: Please register to use this portal</h1>

<form method="GET" action="registered.jsp">

<p>
<label>Your real name: </label>
<input type="text" name="fullname" value="<%=name%>" />
</p>

<p>
<label>Your email address: </label>
<input type="text" name="email" value="<%=email%>" />
</p>

<p>
<label>Your institution: </label>
<input type="text" name="inst" value="<%=inst%>" />
</p>

<p>
<label>Your favorite color: </label>
<input type="text" name="color" value="" />
</p>

<input type="submit" value="Complete registration"/>

</form>

</body>
</html>
