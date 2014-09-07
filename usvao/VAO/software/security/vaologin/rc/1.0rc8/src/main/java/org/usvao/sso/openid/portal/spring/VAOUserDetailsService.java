package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.VAOLogin;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;

import org.springframework.security.openid.OpenIDAuthenticationToken;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a default user details service that simply sets authorizations based on 
 * whether the authentication was done by a VAO-compatible login service.  It
 * also modifies the Authentication token to set a VAOLogin instance as the 
 * authorization details.  
 */
public class VAOUserDetailsService extends VAOAuthenBeanBase
    implements AuthenticationUserDetailsService<OpenIDAuthenticationToken>
{
    private UserDetailsService uds = null;

    public VAOUserDetailsService() { }

    /**
     * wrap a portal-provided UserDetailsService
     */
    public VAOUserDetailsService(UserDetailsService service) { 
        this();
        uds = service;
    }

    @Override
    public UserDetails loadUserDetails(OpenIDAuthenticationToken auth) {
        log.info("setting up VAOLogin");
        VAOLogin loginInfo = createLoginInfo(auth);
        auth.setDetails(loginInfo);

        String username = selectLocalUserName(loginInfo);

        if (uds == null) 
            // not interfacing with portal's user database
            return getDefaultUserDetails(auth.getIdentityUrl(), username, 
                                         loginInfo != null &&
                                     loginInfo.getQualifiedName() != null);

        return uds.loadUserByUsername(username);
    }

    protected UserDetails getDefaultUserDetails(String openid, String username,
                                                boolean vaouser) 
    {
        return new SimpleUserDetailsService.User(openid, username, vaouser);
    }

    protected VAOLogin createLoginInfo(OpenIDAuthenticationToken token) {
        String openid = token.getIdentityUrl();

        VAOAuthenBeanBase.QName qualified = getQualifiedName(openid);
        String uname = (qualified != null) ? qualified.name : null;
        String qname = (qualified != null) ? qualified.toString() : null;

        return new SSFOpenIDLogin(token, uname, qname, true);
    }

    public void setUserDetailsService(UserDetailsService service) {
        uds = service;
    }
}
