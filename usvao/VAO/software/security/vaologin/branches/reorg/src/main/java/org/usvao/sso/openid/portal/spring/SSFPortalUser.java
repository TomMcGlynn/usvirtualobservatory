package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.DbBackedPortalUser;
import org.usvao.sso.openid.portal.VAOLogin;
import org.usvao.sso.openid.portal.UserDatabase;
import org.usvao.sso.openid.portal.RolePermission;
import org.usvao.sso.openid.portal.UnrecognizedUserException;
import org.usvao.sso.openid.portal.UserDbAccessException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.ArrayList;

import org.apache.commons.logging.Log;

/**
 * An implemetation of PortalUser for use with the Spring Security Framework.
 * In that context, it also serves as a UserDetails implementation.  
 */
public class SSFPortalUser extends DbBackedPortalUser implements UserDetails {

    protected SSFUserDatabase ssfudb = null;
    protected boolean enabled = false, unlocked = false, nonexpired = false;

    public SSFPortalUser(String userId, VAOLogin loginInfo, int status,
                         SSFUserDatabase userdb, Log logger) 
    {
        super(userId, loginInfo, status, userdb, logger);
        if (userdb != null) {
            enabled = userdb.statusMeansEnabled(status);
            unlocked = userdb.statusMeansAccountNonLocked(status);
            nonexpired = userdb.statusMeansAccountNonExpired(status);
        }
    }

    public SSFPortalUser(String userId, VAOLogin loginInfo, int status,
                         boolean enabled, boolean unlocked, boolean nonexpired,
                         Log logger) 
    {
        this(userId, loginInfo, status, null, logger);
        this.enabled = enabled;
        this.unlocked = unlocked;
        this.nonexpired = nonexpired;
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
     * @returns boolean   True if a refresh was possible and successful.  False
     *                    can be returned if the implementation does not 
     *                    support a refresh or if the user database is not 
     *                    available for some reason.  
     */
    @Override
    public boolean refresh() {  
        boolean out = super.refresh();
        if (out && ssfudb != null) {
            enabled = ssfudb.statusMeansEnabled(status);
            unlocked = ssfudb.statusMeansAccountNonLocked(status);
            nonexpired = ssfudb.statusMeansAccountNonExpired(status);
        }
        return out;
    }

    /**
     * In support of the UserDetails interface, return the authorizations
     * as a collection of GrantedAuthority objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<SimpleGrantedAuthority> out = 
            new ArrayList<SimpleGrantedAuthority>();
        for(String role : getAuthorizations()) {
            out.add(new SimpleGrantedAuthority(role));
        }
        return out;
    }

    /** required by the UserDetails interface, this returns an empty string */
    @Override
    public String getPassword() { return ""; }

    /** 
     * for the UserDetails interface, this returns the value 
     * of {@link #getID()} 
     */
    @Override 
    public String getUsername() { return getID(); }

    @Override public boolean isAccountNonExpired() { return nonexpired; }
    @Override public boolean isAccountNonLocked() { return unlocked; }
    @Override public boolean isCredentialsNonExpired() { return false; }
    @Override public boolean isEnabled() { return enabled; }
}