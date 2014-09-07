package org.usvao.sso.openid.portal;

/**
 * an exception indicating an error occurred while trying to 
 * register a userid into the user database.
 */
public class RegistrationException extends UserDbAccessException {

    protected String userid = null;

    /**
     * construct the exception for a specific userid
     * @param message    a message explaining the error detected
     * @param userid     the user identifier that was being registered
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public RegistrationException(String message, String userid, 
                                 Throwable cause) 
    {
        super(message, cause);
        this.userid = userid;
    }

    /**
     * construct the exception for a specific userid
     * @param message    a message explaining the error detected
     * @param userid     the user identifier that was being registered
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public RegistrationException(String message, String userid) {
        this(message, userid, null);
    }

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public RegistrationException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     */
    public RegistrationException(String message) {
        this(message, null, null);
    }

    /**
     * return the user identifier that registration was being attempted 
     * when the error occurred.  Return null if the user id is unknonwn
     * or not relevant
     */
    public String getUserID() { return userid; }
}
