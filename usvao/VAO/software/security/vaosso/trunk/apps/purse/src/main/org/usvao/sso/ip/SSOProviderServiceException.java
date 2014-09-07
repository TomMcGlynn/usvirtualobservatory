package org.usvao.sso.ip;

/**
 * an exception indicating a failure of an SSO Provider service due that
 * ultimately can be traced back to incorrect inputs or usage by the 
 * user.  
 */
public class SSOProviderServiceException extends SSOProviderException {

    /**
     * create the exception with a message.
     */
    public SSOProviderServiceException(String msg) {  super(msg);  }

    /**
     * wrap another exception that represents the underlying cause of 
     * the problem.  
     * @param msg    the message summarizing the problem
     * @param cause  a caught exception that represents the underlying 
     *                 cause of the problem.  
     */
    public SSOProviderServiceException(String msg, Throwable cause) {  
        super(msg, cause);  
    }

}