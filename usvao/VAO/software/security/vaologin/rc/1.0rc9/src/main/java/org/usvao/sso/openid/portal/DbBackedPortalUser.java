package org.usvao.sso.openid.portal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.security.Permissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a {@link PortalUser} implementation that wraps a {@link UserDatabase} 
 * instance which it uses to retrieve user information.
 * <p>
 * This implementation is lazy about retrieving information: the database
 * is only consulted when this PortalUser needs to access particular user
 * information. 
 */
public class DbBackedPortalUser extends PortalUser {

    protected UserDatabase udb = null;
    protected Collection<String> roles = null;
    protected Map<String, Object> atts = null, attsFromDb = null;
    protected Log log = null;

    /**
     * create the PortalUser instance.
     * @param userId     the name the user should be known as by the portal
     * @param loginInfo  the VAOLogin instance which contains informatioh
     *                       from the OpenID authentication process.
     * @param status     the code that indicates the status of the user's 
     *                       account
     * @param userdb     the UserDatabase interface to the user database 
     *                       where user informaatin can be retrieved.  This 
     *                       can be null, but it is not recommended. 
     * @param logger     a Log instance that should be used.  If null, one
     *                       will be created internally.
     */
    public DbBackedPortalUser(String userId, VAOLogin loginInfo, int status,
                              UserDatabase userdb, Log logger) 
    {
        super(userId, loginInfo, status);
        if (logger == null) logger = LogFactory.getLog(getClass());
        udb = userdb;
        log = logger;
    }

    /**
     * attempt to refresh the information the information containing in 
     * this representation of the user with the latest known information.
     * This would be done by, say, consulting the user database.  This 
     * should be called if during the user session, user information was 
     * updated (e.g. the user was registered, the user updated their profile,
     * etc.).  
     * <p>
     * This information always returns false.
     * @return  boolean   True if a refresh was possible and successful.  False
     *                    can be returned if the implementation does not 
     *                    support a refresh or if the user database is not 
     *                    available for some reason.  
     */
    @Override
    public boolean refresh() {  
        if (udb == null) return false;
        try {
            status = udb.getUserStatus(getID());
            attsFromDb = null;
            roles = null;
            return true;
        }
        catch (UserDbAccessException ex) {
            log.error("Problem accessing User DB for a refresh: " + 
                      ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * return a value indicating the status of the account associated with 
     * this user.
     */
    @Override
    public int getStatus() { 
        if (udb == null) return super.getStatus();

        if (! udb.isRecognized(status)) {
            try {
                status = udb.getUserStatus(getID());
            } catch (UserDbAccessException ex) {
                log.error("Problem accessing User DB for user status: " + 
                          ex.getMessage(), ex);
            }
        }

        return super.getStatus(); 
    }

    /**
     * return true if the user is recognized as a registered user of the 
     * portal. 
     * <p>
     * The default implementation always returns false; subclasses should 
     * override this method. 
     */
    @Override
    public boolean isRegistered() { 
        if (udb == null) return false;
        return udb.isRecognized(getStatus());
    }

    /**
     * return the named attribute for the user.  Null is returned if the 
     * attribute is not set.
     */
    @Override
    public Object getAttribute(String name) {
        ensureAtts();
        Object out = null;
        if (atts != null) out = atts.get(name);
        if (out == null && attsFromDb != null) out = attsFromDb.get(name);
        return out;
    }

    boolean ensureAtts() {
        if (attsFromDb == null) {
            if (udb == null) return false;
            try {
                Map<String, ? extends Object> theatts = 
                    udb.getUserAttributes(getID());
                if (theatts != null) {
                    attsFromDb = new HashMap<String, Object>();
                    for (String key : theatts.keySet()) 
                        attsFromDb.put(key, theatts.get(key));
                }
            }
            catch (UnrecognizedUserException ex) {  }
            catch (UserDbAccessException ex) {  
                log.error("Problem accessing User DB while getting attrs: " + 
                          ex.getMessage(), ex);
            }
        }
        return true;
    }

    /**
     * set or update the named attribute for the user.  
     */
    @Override
    public void setAttribute(String name, Object value) {
        if (atts == null) atts = new HashMap<String, Object>();
        atts.put(name, value);
    }

    /**
     * return a list of the attribute names
     */
    @Override
    public Set<String> attributeNames() {
        Set<String> out = (ensureAtts()) 
            ? new HashSet<String>(attsFromDb.keySet()) 
            : new HashSet<String>();
        if (atts != null) 
            out.addAll(atts.keySet());
        return out;
    }

    boolean ensureAuthz() {
        if (roles == null && udb != null) {
            try {
                roles = udb.getUserAuthorizations(getID());
            }
            catch (UnrecognizedUserException ex) {  }
            catch (UserDbAccessException ex) {  
                log.error("Problem accessing User DB while getting authz: " 
                          + ex.getMessage(), ex);
            }
        }
        return (roles != null);
    }
    
    /**
     * return the named roles that indicate what the user is allowed to do
     */
    @Override
    public Collection<String> getAuthorizations() {
        if (! ensureAuthz()) return new HashSet<String>();
        return roles;
    }

    /**
     * add an authorization role to this user
     */
    public void addAuthorization(String role) {
        if (! ensureAuthz()) roles = new HashSet<String>();
        roles.add(role);
    }

    /**
     * return the Permissions that the user has.  This will simply
     * wrap the authorizations returned by 
     * {@link #getAuthorizations() getAuthorizations()}.  
     */
    @Override
    public Permissions getPermissions() {
        Permissions out = new Permissions();
        Collection<String> authz = getAuthorizations();
        for (String role : authz) {
            if (ROLE_OPENID_USER.equals(role)) 
                out.add(RolePermission.OPENID_USER);
            else if (ROLE_VAO_USER.equals(role)) 
                out.add(RolePermission.VAO_USER);
            else if (ROLE_REGISTERED_USER.equals(role)) 
                out.add(RolePermission.REGISTERED_USER);
            else 
                out.add(new RolePermission(role));
        }
        return out;
    }

    /**
     * end the user session.  Do everything necessary to erase all session
     * information for this user.  
     */
    @Override
    public void endSession() {
        getLoginInfo().deauthenticate();
    }

    /** this returns the value of {@link #getID()} */
    @Override
    public String toString() { return getID(); }

}