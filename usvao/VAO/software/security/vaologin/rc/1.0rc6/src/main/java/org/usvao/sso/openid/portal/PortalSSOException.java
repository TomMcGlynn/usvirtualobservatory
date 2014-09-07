package org.usvao.sso.openid.portal;

/**
 * a generic exception for errors that occur while trying to manage 
 * single sign-on within a portal.
 */
public class PortalSSOException extends Exception {

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public PortalSSOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     */
    public PortalSSOException(String message) {
        super(message);
    }
}
