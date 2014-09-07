<html>
<head><title>PURSE Sample registration portal</title></head>
<body bgcolor="white">

<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.registration.*' %>
<%@ page import='org.globus.purse.exceptions.*' %>
<%@ page import='org.globus.purse.registration.databaseAccess.StatusDataHandler' %>

<%-- Override of the JSP init() method, to make sure PURSE is initialized --%>
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

<%-- The actual processing --%>
<%
StringWriter sw = new StringWriter();
PrintWriter pw = new PrintWriter(sw);
UserData userData;

try {
    // Check that the password is long enough. MyProxy has a hard-coded
    // limitation that makes it ignore passphrases that are too short
    // (even if they are correct)
    String s = request.getParameter("password");
    if (s==null || s.length()<6) {
        throw new UserRegistrationException("Password must be 6 characters minimum");
    }


    userData = new UserData(
    /*firstName=*/ 	"",
    /*lastName=*/  	request.getParameter("name"),
    /*contactPerson=*/ 	"",
    /*stmtOfWork=*/  	request.getParameter("statementOfWork"),
    /*userName=*/	request.getParameter("uname"),
    /*password=*/	request.getParameter("password"),
    /*institution=*/ 	request.getParameter("inst"),
    /*projectName=*/ 	"",
    /*emailAddress=*/ 	request.getParameter("email"),
    /*phone=*/	 	request.getParameter("phone"),
    /*statusId=*/	StatusDataHandler.getId(RegisterUtil.getRequestStatus()));

    RegisterUser.register(userData);

    String email = request.getParameter("email");
    pw.println("Registration recorded and awaiting approval.<br>");
    pw.println("A confirmation email has been sent to "+email);
} catch (UserRegistrationException ue) {
    pw.println("The registration form has errors: "+ue.getMessage()+"<br>");
    pw.println("Please check the errors and try again.");
} catch (Exception e) {
    pw.println("There was an error during the registration.<br>");
    pw.println("Contact the administrator and try again.<br>");
    pw.println("Stacktrace below.<br><pre>");
    e.printStackTrace(pw);
    pw.println("</pre>");
}

pw.flush();
%>

<%= sw.getBuffer().toString() %>
</body>
</html>
