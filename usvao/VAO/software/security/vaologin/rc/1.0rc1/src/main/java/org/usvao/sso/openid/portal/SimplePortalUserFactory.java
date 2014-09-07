package org.usvao.sso.openid.portal;

import javax.servlet.http.HttpSession;

/**
 * a factory for the SimplePortalUser.
 * <p>
 * In this model, users do not need 
 * to register to the portal.  User attributes are initialized to those 
 * acquired through OpenID attribute exchange, but others can be added.  
 * The local id is set to the VAO qualified username.  An authenticated user 
 * is given the PortalUser.OPENID_USER authorization.  If the OpenID is 
 * recognized as originating from the official USVAO Login services, the 
 * user will also be given the PortalUser.VAO_USER authorization.  
 */
public class SimplePortalUserFactory implements PortalUserFactory {

    public SimplePortalUserFactory() {}

    /**
     * instantiate a PortalUser for a user that is logged in to portal
     */
    public PortalUser getUser(VAOLogin login, HttpSession hsess) {
        return new SimplePortalUser(login, 100, hsess);
    }
}