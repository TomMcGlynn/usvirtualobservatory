package org.usvao.sso.openid.portal;

import javax.servlet.http.HttpSession;

/**
 * an interface for creating PortalUser instances.
 */
public interface PortalUserFactory {

    /**
     * instantiate a PortalUser for a user that is logged in to portal
     * @param login    the login information assembled as a result of 
     *                    successful OpenID authentication.
     * @param hsess    the HttpSession instance (as retrieved from the 
     *                    HttpServletRequest) for the session.  Can be null.
     */
    public PortalUser getUser(VAOLogin login, HttpSession hsess);

}