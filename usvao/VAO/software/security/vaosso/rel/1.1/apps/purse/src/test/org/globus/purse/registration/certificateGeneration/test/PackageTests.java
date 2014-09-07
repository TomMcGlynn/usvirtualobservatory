/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.certificateGeneration.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

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
