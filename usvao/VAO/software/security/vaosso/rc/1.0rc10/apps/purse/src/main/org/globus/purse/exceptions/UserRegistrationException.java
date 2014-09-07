/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.exceptions;

/**
 * Exception thrown in case of any general registration errors to be exposed
 * to user.
 */
public class UserRegistrationException extends RegistrationException {
    
    public UserRegistrationException(String msg) {
	super(msg);
    }

    public UserRegistrationException(String msg, Throwable exp) {
	super(msg, exp);
    }
    
}
