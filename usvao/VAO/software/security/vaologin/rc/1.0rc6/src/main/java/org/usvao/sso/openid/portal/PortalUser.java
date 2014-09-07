package org.usvao.sso.openid.portal;

import java.util.Collection;
import java.util.Set;
import java.security.Permissions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a class that represents a user of a portal.  This is meant to be used 
 * directly by applications to get information about the user that is logged 
 * in.  This includes user attributes (e.g. from a user database), 
 * authorizations/permissions, and session information.  Applications may
 * save objects as attributes to make them available through out a session.
 */
public abstract class PortalUser {

    /**
     * the key name to use to bind a VAOLogin instance to a servlet session
     */
    public final static String SESSION_ATTR_KEY = "VAO.Portal.User";

    /**
     * an authorization role given to users that successfully authenticated
     * with an arbitrary OpenID identifier
     */
    public final static String ROLE_OPENID_USER = "ROLE_OPENID_USER";

    /**
     * an authorization role given to users that successfully authenticated
     * with the VAO OpenID login service.
     */
    public final static String ROLE_VAO_USER = "ROLE_VAO_USER";

    /**
     * an authorization role given to users that are recognized, registered
     * users of the portal.  
     */
    public final static String ROLE_REGISTERED_USER = "ROLE_REGISTERED_USER";

    protected String userid = null;
    protected VAOLogin login = null;
    protected HttpSession sess = null;
    protected int status = -1;

    /**
     * return the PortalUser instance bound to a servlet session or null
     * if it hasn't been set yet.
     * <p>
     * Whether a PortalUser object is attached to an HttpSession is 
     * application dependent.  Doing so is a common way to persist this 
     * information between sessions; however, Spring Security does not 
     * do this (see 
     * {@link org.usvao.sso.openid.portal.spring.SSFPortalUser SSFPortalUser}).
     * In cases when PortalUser is attached to the HttpSession, it is 
     * recommended that you use 
     * {@link #bindToSession(javax.servlet.http.HttpSession) bindToSession()}.
     */
    public static PortalUser fromSession(HttpSession sess) {
        try {
            return (PortalUser) sess.getAttribute(SESSION_ATTR_KEY);
        } 
        catch (ClassCastException ex) {
            Log log = LogFactory.getLog(PortalUser.class);
            if (log.isErrorEnabled()) {
                log.error("Wrong type (" + 
                    sess.getAttribute(SESSION_ATTR_KEY).getClass().toString() +
                          ") stored in servlet sesstion as " +
                          SESSION_ATTR_KEY);
            }
            return null;
        }
    }

    /**
     * return the PortalUser instance bound to a servlet session or null
     * if it hasn't been set yet.
     * <p>
     * Whether a PortalUser object is attached to an HttpSession is 
     * application dependent.  Doing so is a common way to persist this 
     * information between sessions; however, Spring Security does not 
     * do this (see 
     * {@link org.usvao.sso.openid.portal.spring.SSFPortalUser SSFPortalUser}).
     * In cases when PortalUser is attached to the HttpSession, it is 
     * recommended that you use 
     * {@link #bindToSession(javax.servlet.http.HttpSession) bindToSession()}.
     */
    public static PortalUser fromSession(HttpServletRequest servlet) {
        return fromSession(servlet.getSession());
    }

    /**
     * intialize the common PortalUser data
     * @param userId     the name by which the portal will know the user
     * @param loginInfo  the VAOLogin instance that contains the authentication
     *                       information
     * @param status     an indicator of the status of the user's account 
     *                       (see {@link #getStatus()}).
     */
    protected PortalUser(String userId, VAOLogin loginInfo, int status) {
        userid = userId;
        login = loginInfo;
    }

    /**
     * attempt to refresh the information the information containing in 
     * this representation of the user with the latest known information.
     * This would be done by, say, consulting the user database.  
     * <p>
     * This information always returns false.
     * @return  boolean   True if a refresh was possible and successful.  False
     *                    can be returned if the implementation does not 
     *                    support a refresh or if the user database is not 
     *                    available for some reason.  
     */
    public boolean refresh() {  return false;  }

    /**
     * return the local identifier for the user.  This is how the 
     * user is known to the portal (e.g. to the portal's user database).  
     * How this corresponds to the user's OpenID identity is portal-dependent.
     */
    public String getID() { return userid; }

    /**
     * return a value indicating the status of the account associated with 
     * this user.
     */
    public int getStatus() { return status; }

    /**
     * return true if the user is recognized as a registered user of the 
     * portal. 
     * <p>
     * The default implementation always returns false; subclasses should 
     * override this method. 
     */
    public boolean isRegistered() { return false; }

    /**
     * return the login information acquired at authentication time.  
     */
    public VAOLogin getLoginInfo() { return login; }

    /**
     * return the named roles that indicate what the user is allowed to do
     */
    public abstract Collection<String> getAuthorizations();

    /**
     * return true if this user has the named role among its authorizations
     */
    public boolean isAuthorized(String role) {
        return getAuthorizations().contains(role);
    }

    /**
     * return the Permissions that the user has.  This will include 
     * the authorizations returned by 
     * {@link #getAuthorizations() getAuthorizations()}.  
     */
    public abstract Permissions getPermissions();

    /*
     * return a session instance for this user's session
     *
     * This current notion of a session does not work with servlets.
     *
    public abstract PortalSession getSession()
     */

    /**
     * return the HttpSession that this object is bound to.  This normally
     * only returns a non-null value if bindToSession() was previously called 
     * on it.  
     */
    public HttpSession getSession() {  return sess;  }

    /**
     * bind this instance to an HttpSession, which will persist it between
     * servlet calls.  You will later able to retrieve this instance via 
     * {@link #fromSession(HttpSession) fromSession()}.  As part of this, 
     * this method will store the reference to the session internally.  It is 
     * recommended that you use this method to save this instance for two 
     * reasons:
     * <ul>
     *  <li> it will be saved under a standard name, 
     *       PortalUser.SESSION_ATTR_KEY ("VAO.Portal.User") </li>
     *  <li> It allows the endSession() method to remove this class from
     *       the session.</li>
     *  <li> It allows the endSession() method to invalidate the session.  
     *       (Note that different PortalUser implementations may handle this
     *       in different ways.)</li>
     * </ul>
     */
    public void bindToSession(HttpSession session) {
        session.setAttribute(SESSION_ATTR_KEY, this);
        sess = session;
    }

    /**
     * bind this instance to an HttpSession, which will persist it between
     * servlet calls.  You will later able to retrieve this instance via 
     * {@link #fromSession(HttpSession) fromSession()}.  As part of this, 
     * this method will store the reference to the session internally.  It is 
     * recommended that you use this method to save this instance for two 
     * reasons:
     * <ul>
     *  <li> it will be saved under a standard name, 
     *       PortalUser.SESSION_ATTR_KEY ("VAO.Portal.User") </li>
     *  <li> It allows the endSession() method to remove this class from
     *       the session.</li>
     *  <li> It allows the endSession() method to invalidate the session.  
     *       (Note that different PortalUser implementations may handle this
     *       in different ways.)</li>
     * </ul>
     * <p>
     * Note that this is method is not typically used when integrated with 
     * the Spring Security Framework.  Instead, this object would be 
     * available as a <code>UserDetails</code> instance and via the 
     * {@link org.usvao.sso.openid.portal.spring.SSFOpenID#getPortalUser() SSFOpenID.getPortalUser()}
     */
    public void bindToSession(HttpServletRequest request) {
        bindToSession(request.getSession());
    }

    /**
     * return true if the user's session is valid.  In this implementation,
     * the session is valid if the user is authenticated.  Subclasses may 
     * put in other criteria, such as time limits.  
     */
    public boolean isSessionValid() {
        /*
        if (sess == null) return false;
        return (login.isAuthenticated() && ! sess.isExpired());
        */
        return login.isAuthenticated();
    }

    /**
     * end the user session.  Do everything necessary to erase all session
     * information for this user.  
     */
    public abstract void endSession();

    /**
     * return the named attribute of the user.  The default value is 
     * returned if the attribute is not set or otherwise does not 
     * contain a string value.
     */
    public String getProperty(String name, String defaultValue) {
        String out = getProperty(name);
        if (out == null) out = defaultValue;
        return out;
    }

    /**
     * return the named attribute of the user.  Null is returned if the 
     * attribute is not set or otherwise does not contain a string 
     * value.
     */
    public String getProperty(String name) {
        try {
            return (String) getAttribute(name);
        } catch (ClassCastException ex) {
            return null;
        }
    }

    /**
     * return the named attribute for the user.  Null is returned if the 
     * attribute is not set.
     */
    public abstract Object getAttribute(String name);

    /**
     * set or update the named attribute for the user.  
     */
    public abstract void setAttribute(String name, Object value);

    /**
     * return a list of the attribute names
     */
    public abstract Set<String> attributeNames();
}