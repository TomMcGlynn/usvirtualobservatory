/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.certificateGeneration.test;

import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class PackageTests extends TestSuite {

    public PackageTests(String name) {
        super(name);
    }

    public void run(TestResult result) {
	try {
	    String testPropertiesFile = 
		System.getProperty("purseTestProperties");
	    if (testPropertiesFile == null)
		throw new Exception("Test properties file not found");
	    Properties prop = new Properties();
	    prop.load(new FileInputStream(testPropertiesFile));
	    TestCertificateGeneration.setExecPath(
				      prop.getProperty("binLocation"),
				      prop.getProperty("tmpLocation"));
	    TestCertificateGeneration.setCaProperties(
				      prop.getProperty("testCaPassPhrase"), 
				      prop.getProperty("caDir"),
				      prop.getProperty("caHash"),
                      prop.getProperty("myProxyIsCA"));
	    TestCertificateGeneration.setUserName(
				      prop.getProperty("testUserName"));
	    super.run(result);
	} catch (Exception e) {
	    e.printStackTrace();
           result.addError(this, e);
	}
    }

    public static Test suite() throws Exception {
        TestSuite suite = new PackageTests("ESG Certificate Generation Tests");
	suite.addTestSuite(TestCertificateGeneration.class);
	return suite;
    }
}
