/**
 * @author Ray Plante
 */
package org.usvao.sso.openid.portal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * an abstract class for determining the Login status of the web user.  
 * <p>
 * This is used by the LoginStatusServlet to return information about
 * the session back to web pages via javascript-based call.  
 */
public abstract class LoginStatus {

    protected Log log = null;

    public static enum State { 
        /** the user is logged in: a valid session is active */
        ACTIVE, 

        /** the user is not logged in: the user either logged out or never logged in */
        NONE, 

        /** the user is not logged in because the session expired. */
        EXPIRED 
    };

    protected LoginStatus(Log logger) {
        if (logger == null) logger = LogFactory.getLog(getClass());
        log = logger;
    }

    protected LoginStatus() { this(null); }

    /**
     * return the status as a Status enumeration value
     */
    public State getState() {
        return ((isLoggedOut()) ? State.NONE 
                                : (isActive() ? State.ACTIVE 
                                              : State.EXPIRED));
    }

    /**
     * return true if the session does not exist, indicating that the user
     * logged out or otherwise has never logged in.
     */
    public abstract boolean isLoggedOut();

    /**
     * return true if the session is valid, indicating the user is still 
     * logged in.
     */
    public abstract boolean isActive();

    /**
     * return true if the session is not valid, indicating the user is either
     * logged out or the session has expired.
     */
    public boolean isInactive() { return ! isActive(); }

    /**
     * return true if the session has expired.  False will be returned if 
     * the user explicitly logged out.
     * <p>
     * Note that the current Spring-based support does not support a notion
     * of expired sessions; only timeouts due to inactivity.  
     */
    public abstract boolean isExpired();

    /**
     * return the username associated with the session, or null, if it 
     * is not known (because there is no session). 
     */
    public abstract String getUsername();

    /**
     * return the OpenID for the logged in user or null, if there is no
     * active session.
     */
    public abstract String getOpenId();

    /**
     * export the status in JSON format.  This message will have three 
     * key names:
     * <pre>
     *  state      One of "in" (if current user is logged in), "out" (if 
     *             current user is logged out), "ex" (if current user is 
     *             logged in but the session has expired).  
     *  username   The user name of the user that is currently logged in. 
     *  openid     The OpenID URL for the the user that is currently logged in. 
     * </pre>
     */
    public String toJSON() {
        StringBuilder buf = new StringBuilder("{ \"state\": \"");
        LoginStatus.State state = getState();
        if (state == State.ACTIVE) {
            buf.append("in");
        } else if (state == State.EXPIRED) {
            buf.append("ex");
        } else {
            buf.append("out");
        }

        buf.append("\", \"username\": ");
        String user = getUsername();
        if (user == null) 
            buf.append("null");
        else
            buf.append('"').append(user).append('"');

        user = getOpenId();
        buf.append(", \"openid\": ");
        if (user == null) 
            buf.append("null");
        else
            buf.append('"').append(user).append('"');

        buf.append(" }");

        return buf.toString();
    }
}
