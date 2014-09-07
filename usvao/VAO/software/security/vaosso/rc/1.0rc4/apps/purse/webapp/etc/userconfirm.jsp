<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <meta http-equiv="content-type"
 content="text/html; charset=ISO-8859-1">
  <title> Swegrid User Information </title>
</head>
<body>



<%@ page import='java.util.*' %>
<%@ page import='java.io.*' %>
<%@ page import='org.globus.purse.registration.RegisterUser' %>
<%@ page import='org.globus.purse.exceptions.RegistrationException' %>
<%@ page import='org.globus.purse.registration.RegisterUtil' %>
<%@ page import='org.globus.purse.registration.RegisteredUserManager' %>
<%@ page import='org.globus.purse.registration.databaseAccess.DatabaseManager' %>
<%@ page import='org.globus.purse.registration.databaseAccess.UserDataHandler' %>
<%@ page import='org.globus.purse.registration.databaseAccess.DatabaseOptions' %>
<%@ page import='org.globus.purse.registration.databaseAccess.RoleDataHandler' %>
<%@ page import='org.globus.purse.registration.databaseAccess.StatusDataHandler' %>
<%@ page import='org.globus.purse.registration.UserData' %>
<%@ page import='org.globus.purse.registration.databaseAccess.DatabaseOptions' %>
<%@ page import='org.globus.purse.registration.RegisterUtil' %>
<%@ page import='org.globus.purse.registration.certificateGeneration.CertificateGenerationOptions' %>
<%@ page import='org.globus.purse.registration.certificateStorage.MyProxyOptions' %>
<%@ page import='org.globus.purse.registration.mailProcessing.MailOptions' %>
<%@ page import='org.globus.gsi.GlobusCredential' %>
<%@ page import='org.globus.gsi.gssapi.GlobusGSSCredentialImpl' %>
<%@ page import='org.ietf.jgss.GSSCredential' %>


<% 

     String firstname,lastname,institute,project,tel,username,email,password; 

     System.out.println("Request ... ");
	
	
     firstname = ""; 
     lastname = ""; 
     username = "";
     project = "";
     institute = ""; 
     tel = "";
     email = "";
     password = "";
	
	
     // String token = request.getQueryString();
     String token = request.getParameter("token");


     java.util.Properties prop = new java.util.Properties();

     prop.load(new FileInputStream("/home/jquinn/purse/test.properties"));
     try {

	DatabaseOptions dbOptions =
                new DatabaseOptions(prop.getProperty("dbDriver"),
                                    prop.getProperty("dbConnectionURL"),
                                    prop.getProperty("dbUsername"),
                                    prop.getProperty("dbPassword"),
                                    prop.getProperty("dbPropFile"),
                                    prop.getProperty("passPhrase"));
	//DatabaseManager.initialize(dbOptions);

	MailOptions mailOptions = 
	    	new MailOptions(prop.getProperty("caAddress"), 
					prop.getProperty("userAccount"), 
					prop.getProperty("incomingHost"), 
					Integer.parseInt(
						prop.getProperty("incomingPort")),
					prop.getProperty("incomingProtocol"),
					prop.getProperty("outgoingHost"), 
					Integer.parseInt(
						prop.getProperty("outgoingPort")),
					prop.getProperty("outgoingProtocol"),
					prop.getProperty("passwordReminderTemplate"),
					prop.getProperty("sendTokenTemplate"),
					prop.getProperty("caAcceptTemplate"),
					prop.getProperty("caRejectTemplate"),
					prop.getProperty("expireWarnTemplate"),
					prop.getProperty("renewTemplate"),
					prop.getProperty("caBaseUrl"),
					prop.getProperty("userBaseUrl"),
					prop.getProperty("renewBaseUrl"),
					prop.getProperty("caTemplate"),
					prop.getProperty("purseAdminAddr"),
					prop.getProperty("subjectLine"),
					prop.getProperty("adminSubjectLine"),
					prop.getProperty("caSubjectLine"),
					prop.getProperty("caAdminTemplate"),
					prop.getProperty("portalBaseUrl"),
					prop.getProperty("signerCertificate"),
					prop.getProperty("signerKey"),
					prop.getProperty("signerPass"),
                    prop.getProperty("proxyUploadInstructionsTemplate"),
                    prop.getProperty("raTokenTemplate"), prop.getProperty("raSubjectLine")
            );
		
	CertificateGenerationOptions certOpts = 
	    new CertificateGenerationOptions(prop.getProperty("binLocation"),
					     prop.getProperty("tmpLocation"),
					     prop.getProperty("caDir"),
					     prop.getProperty("caHash"));

	MyProxyOptions myProxyOpts = 
	    new MyProxyOptions(prop.getProperty("myProxyBin"),
			       prop.getProperty("myProxyHost"),
			       Integer.parseInt(
				       prop.getProperty("myProxyPort")),
			       prop.getProperty("myProxyDn"),
			       prop.getProperty("myProxyDir"),
			       Integer.parseInt(
				       prop.getProperty("expirationLeadTime")));
  	RegisterUtil.initialize(dbOptions, mailOptions, certOpts, myProxyOpts, prop.getProperty("statusFilename"));
} catch (RegistrationException exp) {
            System.err.println("RegisterUtil.initialize oops...");
            System.err.println(exp.getMessage());
}
	  
    
    token = token.trim();
	// Retrive data from db to verify its from the same emailid
    UserData userData = null;
    try {
	userData = UserDataHandler.getData(token);
    } catch (Exception exp) {
	String err = "Could not retrieve data from db";
	System.out.println(err);
	return;
    }
    if (userData == null) {
	String err = "Following token does not exist " + token + ". No "
	    + " pending request with such a token exists.";
	System.out.println(err);
	return;
    } 
    
    firstname = userData.getFirstName();
    lastname = userData.getLastName();
    username = userData.getUserName();
    project = userData.getProjectName();
    institute = userData.getInstitution();
    tel = userData.getPhoneNumber();
    country = userData.getCountry();
    email = userData.getEmailAddress();
    password = userData.getPassword();
        
%>

<script language="JavaScript">

function setConfirm() {
  document.userinfo.action.value = "confirm";
//  document.userinfo.token.value = request.getParameter("token");
  return true;
}
</script>

<tr style="font-weight: bold;" align="justify">
        <td><big><big><span style="font-weight: bold;"><span
 style="text-decoration: underline; color: rgb(204, 0, 0);">MyGrid User Information</span><br>
        <br>
        </span>


<table cellpadding="2" cellspacing="2" border="1"
 style="text-align: left; width: 100%;">
  <tbody>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">First Name<br>
      </td>
      <td id="name-id" style="vertical-align: top;"><%= firstname %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Lastt Name<br>
      </td>
      <td id="name-id" style="vertical-align: top;"><%= lastname %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Email<br>
      </td>
      <td id="email-id" style="vertical-align: top;"><%= email %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Username<br>
      </td>
      <td id="username-id" style="vertical-align: top;"><%= username %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Password<br>
      </td>
      <td id="password-id" style="vertical-align: top;"><%= password %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Tel<br>
      </td>
      <td id="tel-id" style="vertical-align: top;"><%= tel %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Project<br>
      </td>
      <td id="project-id" style="vertical-align: top;"><%= project %><br>
      </td>
    </tr>
    <tr>
      <td
 style="vertical-align: top; width: 15%; background-color: rgb(204, 255, 255);">Institute<br>
      </td>
      <td id="institute-id" style="vertical-align: top;"><%= institute %><br>
      </td>
    </tr>
  </tbody>
</table>
<br>
<br>
<form name="userinfo" action="confirm.jsp" method="post">
<input type="hidden" name="action" value="">
<input type="hidden" name="token" value="<%= token %>">
<td align="left"><input type="submit" value="Confirm" onClick="setConfirm()"></td>
<br>
<br>
</form>
</body>
</html>
