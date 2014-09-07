<html>
<head><title>Registration confirmation page</title></head>
<body bgcolor="white">

<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.registration.RegisterUser' %>
<%@ page import='org.globus.purse.registration.RegisterUtil' %>
<%@ page import='org.globus.purse.exceptions.*' %>

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

<h2>PURSE User registration confirmation page</h2>

<i>This is the page that consumes the token sent to the user's email
address.</i>

<hr>

<%
    ServletContext sc = getServletConfig().getServletContext();
    StringWriter   sw = new StringWriter();
    PrintWriter    pw = new PrintWriter(sw);

    String token = request.getParameter("token");
    if (token==null) {
	pw.println("Error: Wrong input (no token)");
    } else {
	sc.log("got token = ["+token+"] from "+request.getRemoteAddr());
	try {
	    RegisterUser.processUserResponse(token);
	    pw.println( "Email adress confirmed. Your registration "+
			"request will now be processed. You will be "+
			"contacted via email.");
	} catch (RegistrationException e) {
	    pw.println( "There was an error during the registration "+
			"confirmation process: "+e.getMessage()+"<br>"+
			"Contact the administrator for details. "+
			"Stacktrace below.<br><pre>");
	    e.printStackTrace(pw);
	    pw.println("</pre>");
	}
    }
    pw.flush();
%>


<%= sw.getBuffer().toString() %>
     		
</body>
</html>
