/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.certificateGeneration.test;

import org.globus.purse.registration.certificateGeneration.UserCertificateGeneration;
import org.globus.purse.registration.certificateGeneration.CertificateGenerationOptions;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestCertificateGeneration extends TestCase {
    
    static String binLocation = null;
    static String tmpLocation = null;
    static String caDir = null;
    static String caPassPhrase = null;
    static String caHash = null;
    static String userName = null;
    static String myProxyIsCA = null;

    public TestCertificateGeneration(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestCertificateGeneration.class);
    }

    public static void setUserName(String userName_) {
	userName = userName_;
    }
    public static void setExecPath(String bin, String tmp) {
	binLocation = bin;
	tmpLocation = tmp;
    }

    public static void setCaProperties(String caPassPhrase_, String caDir_,
				       String caHash_, String myProxyIsCA_) {
	if ((caDir != null) && (!caDir.trim().equals(""))) 
	    caDir = caDir_;
	caPassPhrase = caPassPhrase_;
	caHash = caHash_;
    myProxyIsCA = myProxyIsCA_;
    }

    public static void testCertficateGeneraion() throws Exception {
	CertificateGenerationOptions certOpts = 
	    new CertificateGenerationOptions(binLocation, tmpLocation,
					     caDir, caHash, myProxyIsCA);
	UserCertificateGeneration.initialize(certOpts);
	certGenerationTest();
	certSigningTest();
	extractDNTest();
    }

    public static void certGenerationTest() throws Exception {
	UserCertificateGeneration.generate(userName,null);
	String certsLocation = tmpLocation + File.separator + userName
	    + File.separator;
	assertTrue(new File(certsLocation + "usercert.pem").exists());
	assertTrue(new File(certsLocation + "usercert.pem").length() == 0);
	assertTrue(new File(certsLocation + "usercert_request.pem").exists());
	assertTrue(new File(certsLocation 
			    + "usercert_request.pem").length() != 0);
	assertTrue(new File(certsLocation + "userkey.pem").exists());
	assertTrue(new File(certsLocation + "userkey.pem").length() != 0);
    }

    public static void certSigningTest() throws Exception {
	
	String certsLocation = tmpLocation + File.separator + userName
	    + File.separator;
	UserCertificateGeneration.signCerts(certsLocation, caPassPhrase);
	assertTrue(new File(certsLocation + "usercert.pem").exists());
	assertTrue(new File(certsLocation + "usercert.pem").length() != 0);
	// FIXME Verify it has been signed ?
    }
    
    public static void extractDNTest() throws Exception {
	String certsLocation = tmpLocation + File.separator + userName
	    + File.separator;
	String dn = UserCertificateGeneration.getDN(certsLocation);
	
    }
}
