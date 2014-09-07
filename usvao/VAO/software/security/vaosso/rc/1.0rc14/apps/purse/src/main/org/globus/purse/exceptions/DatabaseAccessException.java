/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.exceptions;

/**
 * Exception thrown in case of any general mail access exception
 */
public class DatabaseAccessException extends RegistrationException {
    
    public DatabaseAccessException(String msg) {
	super(msg);
    }

    public DatabaseAccessException(String connURL, String userName, 
				   String msg) {
	super("Connection URL: " + connURL + " userName: " + userName + " "
	      + msg);
    }

    public DatabaseAccessException(String msg, Throwable exp) {
	super(msg, exp);
    }

    public DatabaseAccessException(String connURL, String userName, String msg,
				   Throwable exp) {
	super("Connection URL: " + connURL + " userName: " + userName + " "
	      + msg, exp);
    }
}
