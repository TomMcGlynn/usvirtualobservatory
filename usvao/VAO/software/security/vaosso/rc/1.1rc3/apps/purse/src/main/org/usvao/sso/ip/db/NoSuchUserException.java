package org.usvao.sso.ip.db;

import org.usvao.sso.ip.SSOProviderServiceException;

/**
 * an exception indicating a requested username does not exist in the 
 * user database.  
 */
public class NoSuchUserException extends SSOProviderServiceException {

    String user = null;

    /**
     * create the exception for a given username
     * @param user   the requested username which does not exist.
     */
    public NoSuchUserException(String user) {  this(user, null);  }

    /**
     * create the exception for a given username
     * @param user   the requested username which does not exist.
     * @param msg    the message summarizing the problem; if null, a 
     *                  default message will be generated.
     */
    public NoSuchUserException(String user, String msg) {  
        super((msg != null) ? msg : "No such user registered: " + user);  
        setUser(user);
    }

    /**
     * return the requested username that doesn't exist
     */
    public String getUser() {
        return user;
    }

    /**
     * return the requested username that doesn't exist
     */
    public void setUser(String username) {
        user = username;
    }

}