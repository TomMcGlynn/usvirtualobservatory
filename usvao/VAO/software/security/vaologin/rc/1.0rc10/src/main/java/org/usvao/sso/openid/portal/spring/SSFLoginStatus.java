/**
 * @author Ray Plante
 */
package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.LoginStatus;
import org.usvao.sso.openid.portal.PortalUser;

import org.springframework.security.openid.OpenIDAuthenticationToken;

import org.apache.commons.logging.Log;

/**
 * an abstract class for determining the Login status of the web user.  
 * <p>
 * This is used by the LoginStatusServlet to return information about
 * the session back to web pages via javascript-based call.  
 */
public class SSFLoginStatus extends LoginStatus {

    public SSFLoginStatus(Log logger) { super(logger); }
    public SSFLoginStatus() { super(); }

    /**
     * return the username associated with the session, or null, if it 
     * is not known (because there is no session). 
     */
    @Override
    public String getUsername() {
        return SSFOpenID.getLocalUserName();
    }

    /**
     * return the OpenID for the logged in user or null, if there is no
     * active session.
     */
    @Override
    public String getOpenId() {
        OpenIDAuthenticationToken auth = SSFOpenID.getOpenIDAuthentication();
        if (auth == null) return null;
        return SSFOpenID.getOpenIDURL(auth);
    }

    /**
     * return true if the session does not exist, indicating that the user
     * logged out or otherwise has never logged in.
     */
    @Override
    public boolean isLoggedOut() { 
        return SSFOpenID.getOpenIDAuthentication() == null;
    }

    /**
     * return true if the session is valid, indicating the user is still 
     * logged in.
     */
    @Override
    public boolean isActive() { 
        if (isLoggedOut() || isExpired()) return false;
        return SSFOpenID.getLoginInfo().isAuthenticated();
    }

    /**
     * return true if the session has expired.  False will be returned if 
     * the user explicitly logged out.
     */
    @Override
    public boolean isExpired() { 
        PortalUser puser = SSFOpenID.getPortalUser();
        if (puser == null) return false;
        return ! puser.isSessionValid();
    }

}

