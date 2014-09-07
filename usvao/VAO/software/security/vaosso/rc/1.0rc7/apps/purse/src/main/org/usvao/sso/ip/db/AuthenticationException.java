package org.usvao.sso.ip.db;

import org.usvao.sso.ip.SSOProviderServiceException;

/**
 * an exception indicating an authentication failure (e.g. the given 
 * password was incorrect).  
 */
public class AuthenticationException extends SSOProviderServiceException {

    String user = null;

    /**
     * create the exception for a given username
     * @param user   the username for which authentication failed
     */
    public AuthenticationException(String user) {  this(user, null);  }

    /**
     * create the exception for a given username
     * @param user   the username for which authentication failed
     * @param msg    the message summarizing the problem; if null, a 
     *                  default message will be generated.
     */
    public AuthenticationException(String user, String msg) {  
        super((msg != null) ? msg : defMessage(user));
        setUser(user);
    }

    static String defMessage(String user) {
        String out = "Authentication failed";
        if (user != null) 
            out += " for user " + user;
        return out;
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