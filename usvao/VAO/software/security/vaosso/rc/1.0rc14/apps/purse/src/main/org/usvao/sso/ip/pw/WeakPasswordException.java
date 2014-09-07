package org.usvao.sso.ip.pw;

import org.usvao.sso.ip.SSOProviderServiceException;

/**
 * an exception indicating an authorization failure (e.g. the given 
 * password was incorrect).  
 */
public class WeakPasswordException extends SSOProviderServiceException {

    String user = null;

    /**
     * create the exception with a default message.
     */
    public WeakPasswordException() {  this(null);  }

    /**
     * create the exception 
     * @param msg    the message summarizing the problem; if null, a 
     *                  default message will be generated.
     */
    public WeakPasswordException(String msg) {  this(msg, null);  }

    /**
     * create the exception for a particular user.
     * @param msg    the message summarizing the problem; if null, a 
     *                  default message will be generated.
     * @param user   the username of the user requesting the password.
     */
    public WeakPasswordException(String msg, String user) {  
        super((msg != null) ? msg : defMessage(user));
        setUser(user);
    }

    static String defMessage(String user) {
        String out = "Password is not strong enough";
        if (user != null) 
            out = "User " + user + 
                " requesting password that is not strong enough";
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