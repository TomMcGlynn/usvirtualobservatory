/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing.test;

import java.io.FileInputStream;
import java.util.Properties;

import org.globus.purse.registration.RegisterUtil;
import org.globus.purse.registration.certificateGeneration.CertificateGenerationOptions;
import org.globus.purse.registration.certificateGeneration.UserCertificateGeneration;
import org.globus.purse.registration.certificateStorage.MyProxyOptions;
import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.mailProcessing.MailOptions;
import org.globus.purse.registration.mailProcessing.RetrieveMail;

public class StartMailRetrieval {

    public static void main(String args[]) throws Exception {
    	
    	if (args.length < 1) {
    	    System.out.println("Error, need properties file path");
    	    System.exit(-1);
    	}
    	
    	String filename = args[0];
    	System.out.println("Filename is " + filename);
    	
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
    					prop.getProperty("usernameReminderTemplate"),
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
    					prop.getProperty("caAdmtemplate"),
    					prop.getProperty("portalBaseUrl"),
    					prop.getProperty("signerCert"),
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
        	UserCertificateGeneration.initialize(certOpts);
        	RetrieveMail retrieveMail = new RetrieveMail(mailOptions);
        	retrieveMail.setPassword(prop.getProperty("testUserMailPassword"));
        	retrieveMail.startCheckingMail(3000);

    	} catch (Exception exp) {
    	    System.err.println("Since this is invoked numerous times in this "
    			       + " if after the firt time database exceptions"
    			       + " are seen, they maybe ignored");
    	    System.err.println(exp.getMessage());
    	}
    }
}
