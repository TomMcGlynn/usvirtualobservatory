<%@ page import="org.usvao.sso.openid.portal.PortalUser" %>
<%@ page import="org.usvao.sso.openid.portal.RegistrationException" %>
<%@ page import="org.usvao.sso.openid.portal.spring.SSFOpenID" %>
<%@ page import="net.myportal.MyUserRegistration" %>
<%@ page import="java.util.Properties" %>
<%@ page import="javax.servlet.ServletException" %>
<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    Properties atts = new Properties();
    PortalUser user = SSFOpenID.getPortalUser();
    if (! user.isRegistered()) {
        try {
            MyUserRegistration regservice = new MyUserRegistration(application);

            // we prevent these error conditions via our spring security 
            // configuration, so we can handle their appearance here crudely.
            if (user == null || ! user.isSessionValid())
                throw new RegistrationException("User is not logged in!");
            if (regservice.isRegistered(user.getID())) 
                throw new RegistrationException("User is already registered!");

            // register the user
            atts.setProperty("fullname", 
                                   request.getParameter("fullname"));
            atts.setProperty("email", request.getParameter("email"));
            atts.setProperty("inst", request.getParameter("inst"));
            atts.setProperty("color", request.getParameter("color"));
            
            regservice.registerUser(user.getID(), atts);

            // reset the user so that we know who they are now.
            // user.refresh();
            user.endSession();
            request.getSession().invalidate();

            response.sendRedirect("registered.jsp");
            return;
        }
        catch (RegistrationException ex) {
            throw new ServletException(ex);
        }
    }
%>
<html>
<head>
    <title>useratts: registration completed</title>
</head>
<body>
<h1>Thank you, <%= user.getProperty("fullname", user.getID()) %>, 
for registering.</h1>

<dl>
  <dt> Your profile:
  <dd> <strong>OpenID:</strong>   <%= user.getLoginInfo().getOpenID() %> <br>
       <strong>We know you as:</strong> <%= user.getID() %> <br>
       <strong>Fullname:</strong>       <%= user.getProperty("fullname") %><br>
       <strong>Email:</strong>          <%= user.getProperty("email") %> <br>
       <strong>Institution:</strong>    <%= user.getProperty("inst")  %> <br>
       <strong>Favorite Color:</strong> <%= user.getProperty("color") %> <br>
       <strong>You roles: </strong> <%
           for (String role : user.getAuthorizations()) {
               %> <%= role %> <%
           } %>
</dl>

<p>
<a href="../index.html">Home</a> --
<a href="../j_spring_security_logout">Logout</a>
</p>
</body>
</html>
