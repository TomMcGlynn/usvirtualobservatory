/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.certificateGeneration;

import org.globus.purse.registration.RegisterUtil;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.exceptions.CertificateGenerationException;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle certificate generation using Simple CA
 */
public class UserCertificateGeneration {
    
    static Log logger =
	LogFactory.getLog(UserCertificateGeneration.class.getName());
    private static String CERT_REQUEST = "grid-cert-request";
    private static String CERT_SIGN = "grid-ca-sign";
    private static String CERT_INFO = "grid-cert-info";
    private static String CERT_TO_SIGN = "usercert_request.pem";
    private static String SIGNED_CERT = "usercert.pem";

    static CertificateGenerationOptions certOptions = null;
    static boolean initialized = false;

    /**
     * Initialize proeprties required for certificate generation
     * This need to be called prior to using any other methods in this class.
     * 
     * @param <code>CertificateGenerationOptions</code>
     */
    public static void initialize(CertificateGenerationOptions certOptions_) {
	if (!initialized) {
	    certOptions = certOptions_;
	    initialized = true;
	}
    }

    public static boolean isMyProxyCA() {
        return certOptions.isMyProxyCA();
    }

    /**
     * Generates certificates for the user.
     * Returns the path to directory where the certs are stored.
     *
     * @param cnName
     *        The cn to be specified in the generated credential.
     * @exception <code>CertificateGenerationException</code>
     *           If any error occurs.
     */
    public static String generate(String cnName, String password)
	throws CertificateGenerationException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new CertificateGenerationException(err);
	}
	logger.debug("Generate certificate " + cnName);

	// If tmp does not exist, create
	File tmpDirFile = new File(certOptions.getTmpDirectory());
	if (!tmpDirFile.exists()) {
	    logger.debug("tmp dir does not exist, create");
	    tmpDirFile.mkdirs();
	}
	String certDir = certOptions.getTmpDirectory() + File.separator 
	    + cnName;
	
	if (new File(certDir).exists()) {
	    logger.info("Certificates appear to have already been generated"
			+ " since " + certDir + " exists. Not generating");
	    return certDir;
	} 
	
        String[] cmdArray = null;

        if (certOptions.getCaHash() == null) {
            cmdArray = new String[6];
        } else {
            cmdArray = new String[8];
        }

	cmdArray[0] = certOptions.getBinDirectory() + File.separator 
	    + CERT_REQUEST;
        cmdArray[1] = "-dir";
        cmdArray[2] = certDir;
        cmdArray[3] = "-cn";
        cmdArray[4] = cnName;
        cmdArray[5] = "-nopassphrase";
	
	if (certOptions.getCaHash() == null) {
	    logger.debug("CA hash is null, hence using default CA");
	} else {
            cmdArray[6] = "-ca";
            cmdArray[7] = certOptions.getCaHash();
	}
     
	try {
	    RegisterUtil.runCommand(cmdArray, "Error generating certificate");
	    if (password != null) { 
	        // there is no non-interaction way of creating cert-req with password - so just change the password
	        // by using openssl libraries
	        String[] cmndChangePass = 
                    new String[] { "openssl", "rsa", "-des3", "-in", 
                                   certDir + "/userkey.pem", "-out",
                                   certDir + "/userkeytemp.pem", 
                                   "-passout", "pass:" + password };
	        RegisterUtil.runCommand(cmndChangePass, 
                                        "error using openssl to change the "
                                        + "password");
	        RegisterUtil.runCommand(new String[] {"mv", certDir 
                                                      + "/userkeytemp.pem",
                                                      certDir 
                                                      + "/userkey.pem"}, "");
	        //SUDO should be added
	    }
	} catch (RegistrationException exp) {
	    throw new CertificateGenerationException("Command exec failed",
                                                     exp);
	}
	return certDir;
    }

    /**
     * Signs the user certificates using the ca cert specified at initialization
     * and the passphrase specified here.
     *
     * @param userCertDir
     *        Directory where the user certs are located. (the file names have
     *        to be usercert.pem and userkey.pem
     * @param caPassphrase
     *        Passphrase for the ca cert
     * @exception <code>CertificateGenerationException</code>
     *        If any error occurs.
     */
    public static void signCerts(String userCertDir, String caPassphrase)
    	throws CertificateGenerationException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new CertificateGenerationException(err);
	}

	logger.debug("Sign certificates for " + userCertDir);

	File signedCert = new File(userCertDir + File.separator + SIGNED_CERT);
	if (signedCert.exists() && signedCert.length() > 0) {
	    logger.info("Certificate appears to have been signed. " + 
			userCertDir + File.separator + SIGNED_CERT);
	    return;
	}

	// Ensure that certDir exists and usercert_request exists there.
	String fileToSign = userCertDir + File.separator + CERT_TO_SIGN;
	if (!new File(fileToSign).exists()) {
	    String err = "Certificate to sign does not exist at " + fileToSign;
	    logger.error(err);
	    throw new CertificateGenerationException(err);
	}
	
        String[] cmdArray = null;
        if (certOptions.getCaDirectory() != null) {
            cmdArray = new String[9];
        } else {
            cmdArray = new String[7];
        }
        cmdArray[0] = certOptions.getBinDirectory() + File.separator 
            + CERT_SIGN;
        cmdArray[1] = "-in";
        cmdArray[2] = fileToSign;
        cmdArray[3] = "-out";
        cmdArray[4] = userCertDir + File.separator + SIGNED_CERT;
        cmdArray[5] = "-key";
        cmdArray[6] = caPassphrase;

	if (certOptions.getCaDirectory() != null) {
            cmdArray[7] = "-dir";
            cmdArray[8] = certOptions.getCaDirectory();
	}

	try {
	    RegisterUtil.runCommand(cmdArray, "Error signing certificate");
	} catch (RegistrationException exp) {
	    throw new CertificateGenerationException("Command exec failed",
						     exp);
	}
	logger.debug("Done signing certificate");
    }

    /**
     * Returns DN 
     *
     * @param userCertDir
     *        Directory where the user certs are located. (the file names have
     *        to be usercert.pem and userkey.pem
     * @exception <code>CertificateGenerationException</code>
     *        If any error occurs.
     */
    public static String getDN(String userCertDir) 
	throws CertificateGenerationException {

	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new CertificateGenerationException(err);
	}

	String userCert = userCertDir + File.separator + SIGNED_CERT;
        String[] cmdArray = new String[] { certOptions.getBinDirectory() 
                                             + File.separator + CERT_INFO,
                                             "-subject", "-f", userCert };
	String dn = null;
	try {
	    dn = RegisterUtil.runCommand(cmdArray, 
                                         "Could not retrieve user DN");
	} catch (RegistrationException exp) {
	    throw new CertificateGenerationException("Command exec failed",
                                                     exp);
	}
	logger.debug("DN is " + dn);
	return dn;
    }
    
    /**
     * Returns location of user's certificates
     * @param usrName
     * @return Location
     */
    public static String getUserCertLocation(String usrName) {
    	return certOptions.getTmpDirectory()+"/"+usrName;
    }
}
