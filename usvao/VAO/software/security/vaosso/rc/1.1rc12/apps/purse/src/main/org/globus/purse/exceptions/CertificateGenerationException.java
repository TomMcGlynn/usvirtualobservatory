/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.exceptions;

/**
 * Exception thrown in case of any exception accessing MyProxy server
 */
public class CertificateGenerationException extends RegistrationException {
    
    public CertificateGenerationException(String msg) {
	super(msg);
    }

    public CertificateGenerationException(String msg, Throwable exp) {
	super(msg, exp);
    }
}
