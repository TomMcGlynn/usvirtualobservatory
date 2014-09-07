package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.VAOLogin;
import org.usvao.sso.openid.portal.PortalUser;
import org.usvao.sso.openid.portal.PortalUserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.core.Authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * a class for automatically starting a portal session by creating a 
 * PortalUser instance and binding it to the session.  By extension,
 * this also creates a VAOLogin instance and downloads a certificate
 * if one was requested.  
 */
public class PortalSuccessHandler extends VAOAuthenticationSuccessHandler {

    protected PortalUserFactory userfact = null;

    public PortalSuccessHandler() {
        this(null);
    }

    public PortalSuccessHandler(Log logger) {
        super(logger);
    }

    public PortalUserFactory getPortalUserFactory() { return userfact; }

    public void setPortalUserFactory(PortalUserFactory fact) { 
        userfact = fact; 
    }

    /**
     * Called when the user has successfull authenticated.  This will
     * create the VAOLogin and store it with the servlet session.  It 
     * will also download the certificate and cache it into the VAOLogin
     * instance.  
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
        throws ServletException, IOException 
    {
        super.onAuthenticationSuccess(request, response, authentication);

        VAOLogin login = VAOLogin.fromSession(request);

        PortalUser user = userfact.getUser(login, request.getSession());
        request.getSession().setAttribute(PortalUser.SESSION_ATTR_KEY, user);
    }


}