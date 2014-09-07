<html>
<head>
    <title>PURSE Sample registration vetting: accept/reject</title>
</head>
<body bgcolor="white">

<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.exceptions.*' %>
<%@ page import='org.globus.purse.registration.RegisterUtil' %>
<%@ page import='org.globus.purse.registration.RegisterUser' %>

<!-- Override of the JSP init() method, to make sure PURSE is
     initialized -->

<%!

public void jspInit() {
    if (RegisterUtil.isInitialized())
	return;
    
    try {
	javax.servlet.ServletContext sc = 
	    getServletConfig().getServletContext();
	
	sc.log("PURSE initializing");

	Properties p = new Properties();
	p.load(sc.getResourceAsStream("/WEB-INF/purse.properties"));

	Properties tagprops = new Properties();
	tagprops.load(sc.getResourceAsStream("/WEB-INF/tag.properties"));

	String defaultPurseDir = sc.getRealPath("/")+"WEB-INF";

	RegisterUtil.initialize(p, defaultPurseDir, tagprops);

	sc.log("PURSE initialized");
    } catch (Exception e) {
	throw new RuntimeException(e);
    }

}

%>

<h2>PURSE User registration vetting: Accept/Reject page</h2>

<p>
<b>NOTE: This page must be properly protected against non-authorized
   access, and only accessed across a secure (HTTPS) session!</b>

<hr>

<% 
    String msg="", token=request.getParameter("token");

    if (token == null) {
	throw new IllegalArgumentException("token not specified");
    }

    try {
	if ("Accept".equals(request.getParameter("action"))) {
	    RegisterUser.acceptUser(token, request.getParameter("password"));
	    msg = "User request confirmed. Certificate will be created";
	} else {
	    RegisterUser.rejectUser(token, ""+request.getParameter("message"));
	    msg = "User request rejected. Notification email sent";
	}
    } catch (Exception e) {
	throw new RuntimeException(e);
    }

%>

<%= msg %>

</body>
</html>
