package org.usvao.sso.ip;

/**
 * an exception indicating a failure of an SSO Provider service due that
 * an internal system error not related to user inputs or usage.
 */
public class SSOProviderSystemException extends SSOProviderException {

    /**
     * create the exception with a message.
     */
    public SSOProviderSystemException(String msg) {  super(msg);  }

    /**
     * wrap another exception that represents the underlying cause of 
     * the problem.  
     * @param msg    the message summarizing the problem
     * @param cause  a caught exception that represents the underlying 
     *                 cause of the problem.  
     */
    public SSOProviderSystemException(String msg, Throwable cause) {  
        super(msg, cause);  
    }

}