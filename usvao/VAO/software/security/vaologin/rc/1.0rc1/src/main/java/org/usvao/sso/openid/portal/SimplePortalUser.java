package org.usvao.sso.openid.portal;

import java.util.Date;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.security.Permissions;
import javax.servlet.http.HttpSession;

/**
 * a simple notion of a portal user.  In this model, users do not need 
 * to register to user the portal.  User attributes are initialized to those 
 * acquired through OpenID attribute exchange, but others can be added.  
 * The local id is set to the VAO qualified username.  An authenticated user 
 * is given the PortalUser.ROLE_OPENID_USER authorization.  If the OpenID is 
 * recognized as originating from the official USVAO Login services, the 
 * user will also be given the PortalUser.ROLE_VAO_USER authorization.  
 * <p>
 * The model for the life of a session follows that of HttpSession:  the 
 * session's life is extended by the duration returned by 
 * HttpSesion.getMaxInactiveInterval().  This essentially means that the
 * session is always valid as long as the user is authenticated.  
 */
public class SimplePortalUser extends PortalUser {

    HashMap<String, Object> attributes = null;
    HashSet<String> authz = new HashSet<String>();
    /*
    Date sessstrt = null;
    long life = 0;
    */

    //    public SimplePortalUser(VAOLogin loginInfo, long sessionLifetime, 
    //                            Date sessionStart) 

    /**
     * create the user.  This instance will not be bound to an HttpSession.
     * @param loginInfo    the VAOLogin instances resulting from successful
     *                        authenticated.
     * @param status     an indicator of the status of the user's account 
     *                       (see {@link #getStatus()}).
     */
    public SimplePortalUser(VAOLogin loginInfo, int status) {
        this(loginInfo, status, null);
    }

    /**
     * create the user.
     * @param loginInfo    the VAOLogin instances resulting from successful
     *                        authenticated.
     * @param status     an indicator of the status of the user's account 
     *                       (see {@link #getStatus()}).
     * @param session      the HttpSession used to track the session.  This
     *                        instance will be bound to it.  If null, this 
     *                        class will not be bound.  
     */
    public SimplePortalUser(VAOLogin loginInfo, int status, 
                            HttpSession session) 
    {
        super(getPreferedName(loginInfo), loginInfo, status);
        if (session != null) bindToSession(session);

        /*
        if (sessionStart == null) sessionStart = new Date();
        life = sessionLifetime;
        */

        Set<String> atts = loginInfo.getAttributeAliases();
        attributes = new HashMap<String, Object>(atts.size());
        List<String> vals = null;
        for(String name : atts) {
            vals = loginInfo.getAttributeValues(name);
            if (vals.size() == 1) 
                attributes.put(name, vals.get(0));
            else
                attributes.put(name, vals);
        }

        if (loginInfo.isAuthenticated()) {
            authz.add(PortalUser.ROLE_OPENID_USER);
            if (loginInfo.getOpenID().startsWith(VAOLogin.SERVICE_OPENID_BASE_URL))
                authz.add(PortalUser.ROLE_VAO_USER);
        }

        if (session != null) bindToSession(session);
    }

    private static String getPreferedName(VAOLogin info) {
        String name = info.getQualifiedName();
        if (name == null) name = info.getOpenID();
        if (name == null) name = info.getUserName();
        if (name == null) 
            throw new IllegalArgumentException("No usernames set in VAOLogin");
        return name;
    }

    /**
     * return the named roles that indicate what the user is allowed to do
     */
    public Collection<String> getAuthorizations() {
        return new HashSet<String>(authz);
    }

    /**
     * add or remove an authorization
     * @param role       the name of the authorization
     * @param setunset   if true, add the authorization; false, remove it.
     */
    protected void updateAuthorization(String role, boolean setunset) {
        if (setunset) 
            authz.add(role);
        else 
            authz.remove(role);
    }

    protected void removeAllAuthorizations() {
        authz.clear();
    }

    /**
     * return the permissions.  This will include permissions that 
     * encapsulate the authorizations returned by getAuthorizations.
     */
    public Permissions getPermissions() {
        return null;  // FIX: implement
    }

    /**
     * return the named attribute for the user.  Null is returned if the 
     * attribute is not set.
     */
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    /**
     * set or update the named attribute for the user.  
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * end the user session.  Do everything necessary to erase all session
     * information for this user.  
     * <p>
     * This implementation will call {@link #deauthenticate() deauthenticate()};
     * If this instance is bound directly to an HttpSession (via 
     * {@link #bindToSession(javax.servlet.http.HttpSession) bindToSession()}),
     * this method will also unbind this instance and invalidate the session.
     */
    public void endSession() {
        if (sess != null) {
            if (sess.getAttribute(SESSION_ATTR_KEY) == this)
                sess.removeAttribute(SESSION_ATTR_KEY);
            if (sess.getAttribute(login.SESSION_ATTR_KEY) == login)
                sess.removeAttribute(login.SESSION_ATTR_KEY);
            sess.invalidate();
        }
        login.deauthenticate();
    }

    /**
     * return a list of the attribute names
     */
    public Set<String> attributeNames() {
        return new HashSet<String>(attributes.keySet());
    }

}