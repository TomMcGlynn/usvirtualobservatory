<%@ page import="org.usvao.sso.openid.portal.VAOLogin" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%@ page import="org.usvao.sso.openid.portal.RegistrationException" %>
<%@ page import="net.myportal.MyUserRegistration" %>
<%@ page import="java.util.Properties" %>
<%@ page import="javax.servlet.ServletException" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String user = "Anonymous User";
    VAOLogin login = SSFOpenID.getLoginInfo();
    if (login != null) {
        user = login.getFullName();   // if we requested this OpenID attribute
        if (user == null) user = login.getUserName();  
        if (user == null) user = login.getOpenID();   // shouldn't happen
    }
    String username = SSFOpenID.getLocalUserName();

    MyUserRegistration regservice = new MyUserRegistration(application);

    Properties atts = new Properties();
    if (! regservice.isRegistered(username)) {
        try {
            // register the user
            atts.setProperty("fullname", 
                                   request.getParameter("fullname"));
            atts.setProperty("email", request.getParameter("email"));
            atts.setProperty("inst", request.getParameter("inst"));
            atts.setProperty("color", request.getParameter("color"));
            
            regservice.registerUser(username, atts);
        }
        catch (RegistrationException ex) {
            throw new ServletException(ex);
        }
    }
%>
<html>
<head>
    <title>VAOLogin: registration completed</title>
</head>
<body>
<h1>Thank you, <%=user%>, for registration.</h1>

<dl>
  <dt> Your profile:
  <dd> <strong>OpenID:</strong> <%=login.getOpenID()%> <br>
       <strong>Fullname:</strong> <%= atts.getProperty("fullname") %> <br>
       <strong>Email:</strong> <%= atts.getProperty("email") %> <br>
       <strong>Institution:</strong> <%= atts.getProperty("inst") %> <br>
       <strong>Favorite Color:</strong> <%= atts.getProperty("color") %> <br>
</dl>

<p>
<a href="../index.jsp">Home</a> --
<a href="../j_spring_security_logout">Logout</a>
</p>
