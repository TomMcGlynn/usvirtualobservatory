<html>
<head>
    <title>PURSE Sample registration vetting</title>
</head>
<body bgcolor="white">

<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.exceptions.*' %>
<%@ page import='org.globus.purse.registration.UserData' %>
<%@ page import='org.globus.purse.registration.RegisterUtil' %>
<%@ page import='org.globus.purse.registration.databaseAccess.UserDataHandler' %>

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

<h2>PURSE User registration vetting page</h2>

<i>This is the page where an admin can vet the user's registered
   information, and grant/deny the request.</i>
<p>
<b>NOTE: This page must be properly protected against non-authorized
   access, and only accessed across a secure (HTTPS) session!</b>

<hr>

<% 
    String name="", username="", email="", descr="", msg=""; 

    String token = request.getParameter("token");
	
    UserData userData = null;
    try {
	userData = UserDataHandler.getData(token);
    } catch (Exception e) {
	msg = "Error: Could not retrieve data from db: "+e.getMessage();
    }
    if (userData == null) {
	msg = "Error: No pending request with token ["+token+"] exists";
    } 

    // Note: In register.jsp, we don't make use of FirstName/LastName
    // separation.
    name = userData.getLastName();

    username = userData.getUserName();
    email = userData.getEmailAddress();
    descr = userData.getStmtOfWork();        
%>

<%= msg %>

<table border="0">
<tr><td>Name</td><td><%= name %></td</tr>
<tr><td>User Name</td><td><%= username %></td</tr>
<tr><td>Email Address</td><td><%= email %></td</tr>
<tr><td>Project Description</td><td><%= descr %></td</tr>
</table>

<hr>
<b>Registration Authority decision:</b><br>
<form action="adminconfirm.jsp" method="post">
<input type="hidden" name="token" value=<%= "\""+token+"\"" %>>
<table border="0">
<tr>
<td><input type="radio" name="action" value="Accept"/></td>
<td><b>Accept</b></td></tr>
<tr><td></td><td>
CA password:&nbsp;<input type="password" name="password"><br>&nbsp;
</td>
</tr>
<tr><td><input type="radio" name="action" value="Reject"/></td>
<td><b>Reject</b></td></tr>
<tr><td></td><td>
Message to the user (optional):<br>
<textarea rows="5" cols="41" name="message"></textarea><br>
</td>
</tr>
</table>
<input type="submit" value="Submit">
</form>

</body>
</html>
