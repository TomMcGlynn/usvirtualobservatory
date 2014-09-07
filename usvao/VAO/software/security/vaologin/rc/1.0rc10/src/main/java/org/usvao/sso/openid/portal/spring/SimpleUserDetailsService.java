package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.PortalUser;
import org.usvao.sso.openid.portal.VAOLogin;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * a UserDetailsService bean that will set the username and authorizations
 * depending on whether the OpenID URL for the user is recognized as 
 * being from a VAO-compatible login service.
 * <p>
 * This bean has two properties that can be set:
 * <dl>
 *   <dt> vaoDomains </dt>
 *   <dd> sets the base OpenID URLs to associate with recognized 
 *        VAO-compatible login service domains.  See 
 * {@link org.usvao.sso.openid.portal.spring.VAOAuthenBeanBase#setVaoDomains(String) setVaoDomains()} 
          for details on the format for the input string.
 *        </dd>
 * 
 *   <dt> useAsLocalUserName </dt>
 *   <dd> sets which form of the user's identifier should be used as 
 *        the username within the portal
 * {@link org.usvao.sso.openid.portal.spring.VAOAuthenBeanBase#setUseAsLocalUserName(String) setUseAsLocalUserName()} 
 *        for the allowed user input strings.  
 *        </dd>
 * </dl>
 * 
 */
public class SimpleUserDetailsService extends VAOAuthenBeanBase
    implements UserDetailsService 
{
    public SimpleUserDetailsService() { 
        this(null);
    }

    public SimpleUserDetailsService(Log logger) { 
        super(logger);
    }

    /**
     * This will assume that the username provided will be the OpenID URL
     */
    public UserDetails loadUserByUsername(String openid) {
        String localName = selectLocalUserName(openid);
        return new User(openid, localName, ! localName.equals(openid));
    }

    static class User implements UserDetails {
        String oid = null;
        String name = null;
        HashSet<String> authzs = new HashSet<String>();

        public User(String openid, String username, boolean vao) {
            oid = openid;
            name = username;
            authzs.add(PortalUser.ROLE_OPENID_USER);
            if (vao) authzs.add(PortalUser.ROLE_VAO_USER);
        }

        public Collection<? extends GrantedAuthority> getAuthorities() {
            ArrayList<SimpleGrantedAuthority> out = 
                new ArrayList<SimpleGrantedAuthority>();
            for(String authz : authzs) {
                out.add(new SimpleGrantedAuthority(authz));
            }
            return out;
        }

        public String toString() { return getUsername(); }

        public String getPassword() { return ""; }

        public String getUsername() { return name; }

        public boolean isAccountNonExpired() { return true; }
        public boolean isAccountNonLocked() { return true; }
        public boolean isCredentialsNonExpired() { return true; }
        public boolean isEnabled() { return true; }
    }
}