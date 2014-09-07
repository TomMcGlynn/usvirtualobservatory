/**
 * @author Ray Plante
 */
package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.orm.NvoUser;
import org.usvo.openid.Conf;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

/**
 * a class that determines the Login status of the web user.  
 * <p>
 * Given an UserSession object, this class can provide the user's name
 * (if known), whether the user is logged in, and, if so, how much
 * time is left in the session.  
 * <p>
 * This is used by the LoginStatusServlet to return information about
 * the session back to web pages via javascript-based call.  
 */
public class LoginStatus {

    private UserSession sess = null;

    /**
     * flags indicating the login session status 
     */
    public static enum State { 
        /** the user is logged in: a valid session is active */
        ACTIVE, 

        /** the user is not logged in: the user either logged out or never logged in */
        NONE, 

        /** the user is not logged in because the session expired. */
        EXPIRED 
    };

    /**
     * create status from a UserSession
     * @param UserSession   the users session, constructed from the
     *                        user's session cookie.  If null, no
     *                        session (expired or otherwise) has been
     *                        established. 
     */
    public LoginStatus(UserSession session) {
        sess = session;
    }

    /**
     * return the status as a Status enumeration value
     */
    public State getState() {
        return ((isLoggedOut()) ? State.NONE 
                                : (isActive() ? State.ACTIVE 
                                              : State.EXPIRED));
    }

    /**
     * return the username associated with the session, or null, if it 
     * is not known (because there is no session). 
     */
    public String getUsername() { 
        return (sess == null) ? null : sess.getUser().getUserName();
    }

    /**
     * return the OpenID for the logged in user or null, if there is no
     * active session.
     */
    public String getOpenId() {
        String user = getUsername();
        if (user == null) return null;
        return Conf.get().getId(user);
    }

    /**
     * return true if the session does not exist, indicating that the user
     * logged out or otherwise has never logged in.
     */
    public boolean isLoggedOut() { return sess == null; }

    /**
     * return true if the session is valid, indicating the user is still 
     * logged in.
     */
    public boolean isActive() { return (sess != null && sess.isValid()); }

    /**
     * return true if the session is not valid, indicating the user is either
     * logged out or the session has expired.
     */
    public boolean isInactive() { return (sess == null || !sess.isValid()); }

    /**
     * return true if the session has expired.  False will be returned if 
     * the session was 
     */
    public boolean isExpired() { return (sess != null && sess.isValid()); }

    /**
     * return the time left in the session in whole seconds
     */
    public long getTimeLeftSec() {
        if (sess == null) return 0L;
        long out = 
            (sess.getExpireTime().getTime() - System.currentTimeMillis()) / 1000L;
        if (out < 0L) out = 0L;
        return out;
    }

    /**
     * return the time left in the session in decimal minutes
     */
    public double getTimeLeftMin() {
        return getTimeLeftSec() / 60.0;
    }

    /**
     * return the time left formatted as a string of the form, HHH:MM
     * @param min   the decimal minutes left in the session as returned 
     *              by getTimeLeftMin().
     */
    static public String formatTimeLeft(double leftMin) {
        int hours = (int) Math.floor(leftMin / 60.0);
        int mins = (int) Math.floor(leftMin - hours*60.0);
        return Integer.toString(hours)+":"+Integer.toString(mins);
    }
    
    /**
     * return the LoginStatus given a HttpServletRequest.  
     */
    static public LoginStatus getInstance(HttpServletRequest request)  {
        return new LoginStatus(SessionKit.getLoginSession(request, null, 
                                                          true, false));
    }

    public String toJSON() {
        StringBuffer buf = new StringBuffer("{ \"state\": \"");
        double left = getTimeLeftMin();
        LoginStatus.State state = getState();
        if (state != State.ACTIVE) left = 0.0;
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
        try {
            user = getOpenId();
            buf.append(", \"openid\": ");
            if (user == null) 
                buf.append("null");
            else
                buf.append('"').append(user).append('"');
        } catch (IllegalStateException ex) { }
        buf.append(", \"minLeft\": ");
        buf.append(Double.toString(left));
        buf.append(", \"dispLeft\": \"").append(LoginStatus.formatTimeLeft(left)).append("\" }");

        return buf.toString();
    }

    public static void main(String[] args) {
        LoginStatus status = new LoginStatus(null);
        System.out.print("no session: ");
        System.out.println(status.toJSON());

        String name = null;
        if (args.length > 1) name = args[1];
        if (name == null) name = "joeuser";
        NvoUser user = new NvoUser();
        user.setName(name);

        UserSession sess = new UserSession(user, "alskdfjpwie");
        long tref = System.currentTimeMillis();
        sess.setExpireTime(new Date(tref + 1000*3600));

        status = new LoginStatus(sess);
        System.out.print("active session: ");
        System.out.println(status.toJSON());

        sess.setExpireTime(sess.getCreateTime());

        status = new LoginStatus(sess);
        System.out.print("active session: ");
        System.out.println(status.toJSON());
    }
}
