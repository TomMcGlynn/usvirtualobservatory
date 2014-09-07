package org.usvao.sso.ip.db;

import org.usvao.sso.ip.SSOProviderSystemException;

/**
 * an exception indicating an unexpected failure while access 
 * the user database
 */
public class UserDatabaseAccessException extends SSOProviderSystemException {

    /**
     * create the exception with a message.
     * @param msg    the message summarizing the problem
     */
    public UserDatabaseAccessException(String msg) {  super(msg);  }

    /**
     * wrap another exception that represents the underlying cause of 
     * the problem.  
     * @param msg    the message summarizing the problem
     * @param cause  a caught exception that represents the underlying 
     *                 cause of the problem.  
     */
    public UserDatabaseAccessException(String msg, Throwable cause) {  
        super(msg, cause);  
    }

}