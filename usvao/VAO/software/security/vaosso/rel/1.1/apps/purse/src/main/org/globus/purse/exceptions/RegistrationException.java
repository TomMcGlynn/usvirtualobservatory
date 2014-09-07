/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.exceptions;

import org.globus.common.ChainedException;

/**
 * Exception thrown in case of any registration errors
 */
public class RegistrationException extends ChainedException {
    
    public RegistrationException(String msg) {
	super(msg);
    }
    
    public RegistrationException(String msg, Throwable exp) {
	super(msg, exp);
    }
}
