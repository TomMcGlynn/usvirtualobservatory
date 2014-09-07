package org.usvao.sso.ip;

/**
 * a general exception indicating an error produced by the Single Sign-on
 * Provider system.
 */
public class SSOProviderException extends Exception {

    /**
     * create the exception with a message.
     */
    public SSOProviderException(String msg) {  super(msg);  }

    /**
     * wrap another exception that represents the underlying cause of 
     * the problem.  
     * @param msg    the message summarizing the problem
     * @param cause  a caught exception that represents the underlying 
     *                 cause of the problem.  
     */
    public SSOProviderException(String msg, Throwable cause) {  
        super(msg, cause);  
    }

}