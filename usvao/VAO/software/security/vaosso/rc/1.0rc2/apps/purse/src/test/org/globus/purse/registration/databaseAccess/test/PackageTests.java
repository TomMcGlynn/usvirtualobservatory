/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestResult;

import java.util.Properties;
import java.io.FileInputStream;

import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.databaseAccess.DatabaseManager;

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
	    DatabaseOptions dbOptions = 
		new DatabaseOptions(prop.getProperty("dbDriver"), 
				    prop.getProperty("dbConnectionURL"), 
				    prop.getProperty("dbUsername"), 
				    prop.getProperty("dbPassword"),
				    prop.getProperty("dbPropFile"),
				    prop.getProperty("passPhrase"));
	    DatabaseManager.initialize(dbOptions);
	    super.run(result);
	} catch (Exception e) {
	    e.printStackTrace();
           result.addError(this, e);
	}
    }

    public static Test suite() throws Exception {
        TestSuite suite = new PackageTests("ESG Database Process Tests");
        suite.addTestSuite(TestStatusDataHandler.class);
        suite.addTestSuite(TestRoleDataHandler.class);
        suite.addTestSuite(TestUserGroupDataHandler.class);
        suite.addTestSuite(TestUserDataHandler.class);
	suite.addTestSuite(TestRADataHandler.class);
	return suite;
    }
}
