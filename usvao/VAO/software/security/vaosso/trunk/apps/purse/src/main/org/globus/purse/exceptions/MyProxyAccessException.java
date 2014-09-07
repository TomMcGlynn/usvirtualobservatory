/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.exceptions;

import org.globus.common.ChainedException;

/**
 * Exception thrown in case of any exception accessing MyProxy server
 */
public class MyProxyAccessException extends RegistrationException {
    
    public MyProxyAccessException(String msg) {
	super(msg);
    }

    public MyProxyAccessException(String msg, Throwable exp) {
	super(msg, exp);
    }

    public MyProxyAccessException(String host, int port, String msg) {
	super("Host: " + host + " Port: " + port + " " + msg);
    }

    public MyProxyAccessException(String host, int port, String msg, 
					Throwable exp) {
	super("Host: " + host + " Port: " + port + " " + msg, exp);
    }

}
