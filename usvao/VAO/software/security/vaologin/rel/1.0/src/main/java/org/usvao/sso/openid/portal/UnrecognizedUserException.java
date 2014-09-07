package org.usvao.sso.openid.portal;

/**
 * an exception that a requested user has not been registered or otherwise 
 * does not have a recognized identifier.
 */
public class UnrecognizedUserException extends PortalSSOException {

    protected String userid = null;

    /**
     * create the exception for a specific userid
     */
    public UnrecognizedUserException(String userid) {
        this("Unrecognized user: " + userid, userid);
    }

    /**
     * create the exception with a custom message
     */
    public UnrecognizedUserException(String message, String userid) {
        this(message, userid, null);
    }

    /**
     * create the exception with a custom message
     */
    public UnrecognizedUserException(String message, String userid, 
                                     Throwable cause) 
    {
        super(message, cause);
        this.userid = userid;
    }

    /**
     * return the unrecognized user identifier.  Return null if the user 
     * ID is unknonwn.
     */
    public String getUserID() { return userid; }

}