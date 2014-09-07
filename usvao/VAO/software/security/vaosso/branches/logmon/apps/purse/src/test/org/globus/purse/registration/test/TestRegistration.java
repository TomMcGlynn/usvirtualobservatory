/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.test;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.purse.registration.RegisterUser;
import org.globus.purse.registration.RegisterUtil;
import org.globus.purse.registration.RegisteredUserManager;
import org.globus.purse.registration.UserData;
import org.globus.purse.registration.certificateGeneration.CertificateGenerationOptions;
import org.globus.purse.registration.certificateStorage.MyProxyOptions;
import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.databaseAccess.RoleDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.mailProcessing.MailOptions;
import org.ietf.jgss.GSSCredential;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Vector;

public class TestRegistration {
    
    public static void main(String args[]) throws Exception {

	String msg =  
	    "TestRegistration bootstrap filename"
	    + "TestRegistration register filename\n"
	    + "TestRegistration confirm filename \n"
	    + "TestRegistration caAccept filename \n"
	    + "TestRegistration caAcceptPendingUpload filename \n"
	    + "TestRegistration caReject filename \n"
	    + "TestRegistration changePassword filename\n"
	    + "TestRegistration expireWarn filename\n"
	    + "TestRegistration deleteUser filename\n Value: ";
	
	System.out.println("Args " + args.length);
	if (args.length < 2) {
	    System.err.println(msg);
	    System.exit(-1);
	}

	System.out.println("Args " + args[0] + " " + args[1]);
	String option = args[0];

	// Bootstrap		
	if (option.equals("bootstrap")) {
	    bootstrap(args[1]);
	    System.exit(0);
	}
	
	// Registration data
	if (option.equals("register")) {
	    registerUserData(args[1]);
	    System.exit(0);
	}

	// Registration data
	if (option.equals("enrollUser")) {
	    enrollUserData(args[1]);
	    System.exit(0);
	}

	// User confirms
	if (option.equals("confirm")) {
	    userConfirms(args[1]);
	    System.exit(0);
	}

	// User confirmingUser (External CA) 
	if (option.equals("RAConfirmsUser")) {
	    confirmingUser(args[1]);
	    System.exit(0);
	}

        // User is accepted by CA pending him uploading his proxy
        if (option.equals("caAcceptPendingUpload")) {
            caAcceptPendingUpload(args[1]);
            System.exit(0);
        }

	// CA Accepts
	if (option.equals("caAccept")) {
	    caAccept(args[1]);
	    System.exit(0);
	}
	    
	// CA Rejects
	if (option.equals("caReject")) {
	    caReject(args[1]);
	    System.exit(0);
	}
	
	// User changes password
	if (option.equals("changePassword")) {
	    changePassword(args[1]);
	    System.exit(0);
	}
	    
	// CA revoked certificates
	if (option.equals("deleteUser")) {
	    deleteUser(args[1]);
		System.exit(0);
	}

	if (option.equals("expireWarn")) {
	    sendExpirationWarn(args[1]);
	    System.exit(-1);
	}
	    
	System.out.println("Usage: " + msg);
	System.exit(-1);
    }

    private static void bootstrap(String filename) {
	System.out.println("In bootstrap");
	try {
	    Properties prop = new Properties();
	    prop.load(new FileInputStream(filename));
	    DatabaseOptions dbOptions = 
		new DatabaseOptions(prop.getProperty("dbDriver"), 
				    prop.getProperty("dbConnectionURL"), 
				    prop.getProperty("dbUsername"), 
				    prop.getProperty("dbPassword"),
				    prop.getProperty("dbPropFile"),
				    Integer.parseInt(prop.getProperty("hashIterations")));
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
                                prop.getProperty("raTokenMailTemplate"),
                                prop.getProperty("raSubjectLine")); 
		
	CertificateGenerationOptions certOpts = 
	    new CertificateGenerationOptions(prop.getProperty("binLocation"),
					     prop.getProperty("tmpLocation"),
					     prop.getProperty("caDir"),
					     prop.getProperty("caHash"),
                         prop.getProperty("myProxyIsCA"));

	MyProxyOptions myProxyOpts = 
	    new MyProxyOptions(prop.getProperty("myProxyBin"),
			       prop.getProperty("myProxyHost"),
			       Integer.parseInt(
				       prop.getProperty("myProxyPort")),
			       prop.getProperty("myProxyDn"),
			       prop.getProperty("myProxyDir"),
			       Integer.parseInt(
				       prop.getProperty("expirationLeadTime")));
	RegisterUtil.initialize(dbOptions, mailOptions, certOpts, myProxyOpts,
				prop.getProperty("statusFilename"));
	} catch (Exception exp) {
	    System.err.println("Since this is invoked numerous times in this "
			       + " if after the firt time database exceptions"
			       + " are seen, they maybe ignored");
	    System.err.println(exp.getMessage());
	}
	System.out.println("Bootstrap has been done");
    }

    private static void userConfirms(String filename) throws Exception {
	System.out.println("In user confirms ");
	// no need to do this each time from portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
	RegisterUser.processUserResponse(prop.getProperty("confirmToken"));
    }

    private static void registerUserData(String filename) throws Exception {

	
	System.out.println("In register user data");
	// no need to do this each timefrom portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));

	System.out.println("here " + RegisterUtil.getRequestStatus());

	int reqId = 
	    StatusDataHandler.getId(RegisterUtil.getRequestStatus());

	UserData userData = new UserData(prop.getProperty("firstName"), 
					 prop.getProperty("lastName"),
					 prop.getProperty("contactPerson"),
					 prop.getProperty("stmtOfWork"),
					 prop.getProperty("userName"),
					 prop.getProperty("password"),
					 prop.getProperty("salt"),
					 prop.getProperty("passwordMethod"),
					 prop.getProperty("institution"),
					 prop.getProperty("projectName"),
					 prop.getProperty("emailAddress"),
					 prop.getProperty("phone"), null, null, null, reqId);
	
        String raIdStr = prop.getProperty("raId");
        int raId = -1;
        if (raIdStr != null) {
            raId = (new Integer(raIdStr)).intValue();
            userData.setRaId(raId);
        }
	int role1 = RoleDataHandler.getData("user").getId();
        Vector addRoles = new Vector();
	addRoles.add(new Integer(role1));
	userData.addRoles(addRoles);
	RegisterUser.register(userData);
    }
    
    private static void caAccept(String filename) throws Exception {

	System.out.println("In ca accept");
	// no need to do this each timefrom portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
	RegisterUser.acceptUser(prop.getProperty("acceptToken"),
				prop.getProperty("caPassPhrase"));
    }
    
    private static void caAcceptPendingUpload(String filename) 
    throws Exception {

	System.out.println("In caAcceptPendingUpload ");
	// no need to do this each timefrom portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
	RegisterUser.acceptUser(prop.getProperty("acceptToken"));
    }

    private static void caReject(String filename) throws Exception {

	System.out.println("In ca reject");
	// no need to do this each timefrom portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
	RegisterUser.rejectUser(prop.getProperty("rejectToken"),
				prop.getProperty("rejectMsg"));
    }

    private static void changePassword(String filename) throws Exception {
	System.out.println("In change password");
	// no need to do this each timefrom portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
	GlobusCredential globCred = 
	    new GlobusCredential(prop.getProperty("proxyFile"));
	GSSCredential cred = 
	    new GlobusGSSCredentialImpl(globCred, 
					GSSCredential.INITIATE_AND_ACCEPT);
	RegisteredUserManager.changeUserPassword(
			      prop.getProperty("chPassUser"),
			      prop.getProperty("oldPass"),
			      prop.getProperty("newPass"),
			      cred);
    }

    private static void deleteUser(String filename) throws Exception {
	System.out.println("In delete user");
	// no need to do this each timefrom portal
	bootstrap(filename);
	Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
	RegisteredUserManager.revokeUser(prop.getProperty("revokeUser"));
    }

    private static void sendExpirationWarn(String filename) throws Exception {
	bootstrap(filename);
	RegisteredUserManager.sendCertExpirationWarning();
    }

    private static void enrollUserData(String filename) throws Exception {

        System.out.println("In Enroll user data");
    	// no need to do this each timefrom portal
    	bootstrap(filename);
    	Properties prop = new Properties();
    	prop.load(new FileInputStream(filename));

    	int reqId = 
    	    StatusDataHandler.getId(RegisterUtil.getRequestStatus());
    	UserData userData = new UserData(prop.getProperty("firstName"), 
    					 prop.getProperty("lastName"),
    					 prop.getProperty("contactPerson"),
    					 prop.getProperty("stmtOfWork"),
    					 prop.getProperty("userName"),
    					 prop.getProperty("password"),
    					 prop.getProperty("salt"),
					 prop.getProperty("passwordMethod"),
    					 prop.getProperty("institution"),
    					 prop.getProperty("projectName"),
    					 prop.getProperty("emailAddress"),
    					 prop.getProperty("phone"), null, null, null, reqId);
    	
    	int role1 = RoleDataHandler.getData("user").getId();
    	Vector addRoles = new Vector();
    	addRoles.add(new Integer(role1));
    	userData.addRoles(addRoles);
    	RegisterUser.enrollUser(userData);
        }

    private static void confirmingUser(String filename) throws Exception {

    	System.out.println("In ca enroll");
    	// no need to do this each timefrom portal
    	bootstrap(filename);
    	Properties prop = new Properties();
    	prop.load(new FileInputStream(filename));
    	RegisterUser.confirmUser(prop.getProperty("acceptToken"));
        }
}
