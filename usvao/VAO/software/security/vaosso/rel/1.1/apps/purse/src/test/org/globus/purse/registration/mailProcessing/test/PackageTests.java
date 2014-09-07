/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

import org.globus.purse.registration.mailProcessing.MailOptions;
import org.globus.purse.registration.mailProcessing.MailManager;

public class PackageTests extends TestSuite {

    public PackageTests(String name) {
        super(name);
    }

    Properties loadPropertiesFromFile(String filepath) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(filepath));

        String pursedir = prop.getProperty("purse.dir");
        if (pursedir == null) pursedir = System.getProperty("purse.dir");

        if (pursedir != null) {
            String val = null;
            for(String name : prop.stringPropertyNames()) {
                val = prop.getProperty(name);
                if (val != null && val.contains("${purse.dir}")) {
                    val = val.replaceAll("\\$\\{purse.dir\\}", pursedir);
                    prop.setProperty(name, val);
                }
            }
        }

        return prop;
    }

    public void run(TestResult result) {
	try {
	    String testPropertiesFile = 
		System.getProperty("purseTestProperties");
	    if (testPropertiesFile == null)
		throw new Exception("Test properties file not found");
	    Properties prop = loadPropertiesFromFile(testPropertiesFile);
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
				prop.getProperty("caAdmTemplate"),
				prop.getProperty("portalBaseUrl"),
				prop.getProperty("signerCertificate"),
				prop.getProperty("signerKey"),
				prop.getProperty("signerPass"),
                                prop
                                .getProperty("proxyUploadInstructionsTemplate"),
                                prop.getProperty("raTokenMailTemplate"),
                                prop.getProperty("raSubjectLine"));
	    MailManager.initialize(mailOptions);
	    TestMailManager.setMailOptions(mailOptions);
	    TestMailManager.setPassword(prop.getProperty("testUserMailPassword"));
	    super.run(result);
	} catch (Exception e) {
	    e.printStackTrace();
           result.addError(this, e);
	}
    }

    public static Test suite() throws Exception {
        TestSuite suite = new PackageTests("ESG Mail Process Tests");
	suite.addTestSuite(TestMailManager.class);
	return suite;
    }

    public static void main(String[] args) {
        PackageTests tests = new PackageTests("mailtests");
        tests.run(null);
    }
}
