package org.usvao.sso.openid.portal;

import java.util.Date;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * a container for the data about the user's session in the portal.  
 * In general, a portal session has a limited lifetime; this class 
 * keeps track of the start and expiration times of the session.  Once 
 * a session has expired, it is expected the user will have to reauthenticate
 * to create a new session; however, this class allows sessions to be extended
 * regardless of whether it is currently expired.  
 * <p> 
 * This class assumes that the session has been authenticated.
 * <p>
 * Note that portals may opt not to use this class to manage sessions.  
 *
 * @deprecated
 */
public class PortalSession {

    Date strt = null;
    long ref = 0;
    long life = 0;

    /**
     * initialize the session data.
     * @param start     the timestamp of the validated start of the session, 
     *                     in seconds since the "Epoch".
     * @param reference the reference timestamp for the lifetime of the session.
     *                     This is normally equal to or later than the start. 
     * @param lifetime  the total duration, in seconds, of the session from 
     *                     its start
     */
    public PortalSession(long start, long reference, long lifetime) {
        strt = new Date(start * 1000);
        ref = reference;
        life = lifetime;
    }

    /**
     * initialize the session data.
     * @param start     the timestamp of the validated start of the session, 
     *                     in seconds since the "Epoch".
     * @param lifetime  the total duration, in seconds, of the session from 
     *                     its start
     */
    public PortalSession(long start, long lifetime) {
        this(start, start, lifetime);
    }

    /**
     * initialize the session data.
     * @param start     the timestamp of the validated start of the session
     * @param lifetime  the total duration, in seconds, of the session from 
     *                     its start
     */
    public PortalSession(Date start, long lifetime) {
        this((long) Math.ceil(start.getTime() / 1000.0), lifetime);
    }

    /**
     * initialize the session data with a session start time of right now.
     * @param lifetime  the total duration, in seconds, of the session from 
     *                     its start
     */
    public PortalSession(long lifetime) {
        this(new Date(), lifetime);
    }

    /**
     * return the reference timestamp to which the lifetime applies.
     */
    public long getLifeRefTime() { return ref; }

    /**
     * return the start of this session
     */
    public Date getStart() { return strt; }

    /**
     * return the valid duration, in seconds, of this session from its 
     * reference timestamp.
     */
    public long getLifetime() { return life; }

    /**
     * return true if the session has expired
     */
    public boolean isExpired() { return getTimeLeftSec() <= 0; }

    /**
     * return the time left in this session in whole seconds
     */
    public long getTimeLeftSec() {
        // if (getStart() == null) return 0;
        long now = (long) Math.floor((new Date()).getTime() / 1000.0);
        long sofar = now - getLifeRefTime();
        if (sofar < -1) return 0;
        long out = getLifetime() - sofar;
        return (out < 0) ? 0 : out;
    }

    /**
     * return the time left in this session in decimal minutes
     */
    public double getTimeLeftMin() {
        return getTimeLeftSec() / 60.0;
    }

    /**
     * return a given duration, in minutes, as a formatted string of 
     * the form HHH:MM
     */
    public static String formatTimeLeft(double minutes) {
        StringWriter out = new StringWriter();
        PrintWriter fmtr = new PrintWriter(out);
        double hours = Math.floor(minutes / 60.0);
        double mins = Math.floor(minutes - hours*60.0);
        fmtr.printf("%d:%02d", (long) hours, (long) mins);
        return out.toString();
    }

    public static void main(String[] args) {
        Date startDate = new Date();
        PortalSession sess = new PortalSession(startDate, 600);
        long left = sess.getTimeLeftSec();
        long t = sess.getLifeRefTime();
    }
}