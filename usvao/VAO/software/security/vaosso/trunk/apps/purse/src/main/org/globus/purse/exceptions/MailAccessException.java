/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.exceptions;

/**
 * Exception thrown in case of any general mail access exception
 */
public class MailAccessException extends RegistrationException {
    
    public MailAccessException(String msg) {
	super(msg);
    }

    public MailAccessException(String msg, Throwable exp) {
	super(msg, exp);
    }
}
