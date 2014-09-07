package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.PortalUser;
import org.usvao.sso.openid.portal.VAOLogin;
import org.usvao.sso.openid.portal.UserDatabase;
import org.usvao.sso.openid.portal.UserDbAccessException;

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
 * a user details service that set a PortalUser instance as the UserDetails
 */
public class PortalUserDetailsService extends VAOAuthenBeanBase
    implements AuthenticationUserDetailsService<OpenIDAuthenticationToken>
{
    private SSFUserDatabase udb = null;

    public PortalUserDetailsService() { 
    }

    public PortalUserDetailsService(SSFUserDatabase userdb) { 
        this();
        setUserDatabase(userdb);
    }

    public void setUserDatabase(SSFUserDatabase userdb) {
        udb = userdb;
    }

    public SSFUserDatabase getUserDatabase() {
        return udb;
    }

    @Override
    public UserDetails loadUserDetails(OpenIDAuthenticationToken auth) {
        log.info("setting up VAOLogin/PortalUser");
        VAOLogin loginInfo = createLoginInfo(auth);
        auth.setDetails(loginInfo);

        String username = selectLocalUserName(loginInfo);

        boolean enabled = false, unlocked = false, unexpired = false;
        int status = 0;
        if (udb != null) {
            try {
                status = udb.getUserStatus(username);
            }
            catch (UserDbAccessException ex) {
                log.error("Failed to get status for user, " + username +
                          ": " + ex.getMessage(), ex);
            }
        }

        SSFPortalUser out = new SSFPortalUser(username, loginInfo, status, 
                                              udb, null);

        // adds some default authorizations
        if (udb == null) {
            if (! out.isAuthorized(out.ROLE_OPENID_USER))
                out.addAuthorization(out.ROLE_OPENID_USER);
            String qname = loginInfo.getQualifiedName();
            if (qname != null && qname.endsWith("@usvao"))
                out.addAuthorization(out.ROLE_VAO_USER);
        }
        else if (udb.isRecognized(status)) {
            out.addAuthorization(out.ROLE_REGISTERED_USER);
        }

        return out;
    }

    protected VAOLogin createLoginInfo(OpenIDAuthenticationToken token) {
        String openid = token.getIdentityUrl();

        VAOAuthenBeanBase.QName qualified = getQualifiedName(openid);
        String uname = (qualified != null) ? qualified.name : null;
        String qname = (qualified != null) ? qualified.toString() : null;

        return new SSFOpenIDLogin(token, uname, qname, true);
    }

}

