package org.usvao.sso.openid.portal;

/**
 * an exception indicating an error occurred while trying to either 
 * connecting to or retrieving information from the user database
 */
public class UserDbAccessException extends PortalSSOException {

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public UserDbAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     */
    public UserDbAccessException(String message) {
        super(message);
    }
}
