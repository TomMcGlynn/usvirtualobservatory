package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.VAOLogin;
import org.usvao.sso.openid.portal.PortalUser;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDAuthenticationStatus;

/**
 * a set of static utility functions for interacting with the OpenID
 * Spring Security context.
 */
public class SSFOpenID {

    /**
     * return the Authentication instance for the currently authenticated
     * user.  This will be an OpenIDAuthenticationToken instance if OpenID
     * was used to authenticate (via spring).
     */
    public static Authentication getCurrentUserAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * return the OpenID Authentication instance or null if OpenID was 
     * not used.
     */
    public static OpenIDAuthenticationToken getOpenIDAuthentication() {
        try {
            return (OpenIDAuthenticationToken) 
                SSFOpenID.getCurrentUserAuthentication();
        } catch (ClassCastException ex) {
            return null;
        }
    }

    /**
     * return the UserDetails instance for the current user.  Null 
     * will be returned if none was set, but this should not happen 
     * with OpenID authentication.
     */
    public static UserDetails getUserDetails() {
        Object principal = getCurrentUserAuthentication().getPrincipal();
        return ((principal instanceof UserDetails)
                ? (UserDetails) principal : null);
    }
                

    /**
     * return the PortalUser instance for the current user.  Null 
     * will be returned if none was set.  The Spring OpenID configuration 
     * must be set up accordingly for this to return an instnace.
     */
    public static PortalUser getPortalUser() {
        Object principal = getCurrentUserAuthentication().getPrincipal();
        return ((principal instanceof PortalUser)
                ? (PortalUser) principal : null);
    }
                

    /**
     * return true if the authentication token indicates that authentication
     * was successful.  
     */
    public static boolean isAuthenticated(OpenIDAuthenticationToken auth) {
        return OpenIDAuthenticationStatus.SUCCESS.equals(auth.getStatus());
    }

    /**
     * return the OpenID URL for the user associated with the given 
     * authentication token. 
     */
    public static String getOpenIDURL(OpenIDAuthenticationToken auth) {
        return auth.getIdentityUrl();
    }

    /**
     * return the local name for the currently authenticated user.  This 
     * is extracted from the UserDetails instance.
     */
    public static String getLocalUserName() {
        OpenIDAuthenticationToken auth = getOpenIDAuthentication();
        if (auth == null) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails)
            return ((UserDetails) principal).getUsername();
        else 
            return principal.toString();
    }

    /**
     * return the VAOLogin instance to access information resulting 
     * from VAO-OpenID authentication process
     */
    public static VAOLogin getLoginInfo() {
        OpenIDAuthenticationToken auth = getOpenIDAuthentication();
        if (auth == null) return null;

        VAOLogin out = null;
        try {
            out = (VAOLogin) auth.getDetails();
        }
        catch (ClassCastException ex) { }

        if (out == null)
            out = new SSFOpenIDLogin(auth);
        return out;
    }
}