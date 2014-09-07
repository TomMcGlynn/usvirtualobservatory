/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.certificateGeneration;

import org.globus.purse.exceptions.CertificateGenerationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sets the certificate configuration options
 */
public class CertificateGenerationOptions {

    static Log logger =
	LogFactory.getLog(CertificateGenerationOptions.class.getName());

    String binDir = null;
    String tmpDir = null;
    String caDir= null;
    String caHash = null;
    String myProxyIsCA = null;

    /**
     * Constructor when default CA is used
     */
    public CertificateGenerationOptions(String binDir_, String tmpDir_) 
	throws CertificateGenerationException {
	this(binDir_, tmpDir_, null, null, null);
    }
    
    
    /**
     * Constructor when default CA is not used.
     */
    public CertificateGenerationOptions(String binDir_, String tmpDir_,
					String caDir_, String caHash_, String myProxyIsCA_)
	throws CertificateGenerationException {

	if ((binDir_ == null) || (binDir_.trim().equals(""))) {
	    String err = "Directory with executables for certificate generation"
		+ " cannot be null";
	    logger.error(err);
	    throw new CertificateGenerationException(err);
	}
        this.binDir = binDir_;
	
	if ((tmpDir_ == null) || (tmpDir_.trim().equals(""))) {
	    String err = "Path to temporary directory with write permissions"
		+ " cannot be null";
	    logger.error(err);
	    throw new CertificateGenerationException(err);
	}
        this.tmpDir = tmpDir_;

	if (caDir_ != null) 
	    this.caDir = caDir_;
	if (caHash_ != null)
	    this.caHash = caHash_;
    if (myProxyIsCA_ != null)
        myProxyIsCA = myProxyIsCA_;
    }

    /** Directory where SimpleCA executables are located */
    public String getBinDirectory() {
	return binDir;
    }

    /** Temporary directory to store the certificates generated for users */
    public String getTmpDirectory() {
	return tmpDir;
    }

    public void setCaDirectory(String caDir_) {
	caDir = caDir_;
    }

    /**
     * Directory where the ca certs are located. Required when default CA 
     * is not the ESG CA
     */
    public String getCaDirectory() {
	return caDir;
    }

    public void setCaHash(String caHash_) {
	caHash = caHash_;
    }

    /**
     * Hash of the CA used to sign user generated certificates. Required when 
     * default CA is not the ESG CA
     */
    public String getCaHash() {
	return caHash;
    }

    public boolean isMyProxyCA() {
        return "true".equalsIgnoreCase(myProxyIsCA) || "yes".equalsIgnoreCase(myProxyIsCA);
    }
    public String getMyProxyIsCA() {
        return myProxyIsCA;
    }
}
   
