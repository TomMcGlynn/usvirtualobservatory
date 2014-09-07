package org.usvao.sso.openid.portal.spring;

import org.springframework.security.core.Authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class for setting up a user session in the portal upon successful 
 * authentication.  
 * <p>
 * This class is intended for plugging into the Spring Security Framework
 * configuration.  Spring's OpenID configuration 
 * (via <code>&lt;openid-login&gt;</code>'s 
 * <code>authentication-success-handler-ref</code> attribute)
 * supports providing an AuthenticationSuccessHandler class that will be 
 * invoked just after validating a successful OpenID authentication.  This 
 * class leverages the handler hook to set up a PortalUser instance that 
 * can assist with tracking a user session.  This includes transparently
 * downloading an X.509 certificate if one was requested 
 * (see {@link VAOAuthenticationSuccessHandler VAOAuthenticationSuccessHandler}).  
 * <p>
 * In this implementation, in addition to creating and caching the 
 * {@link org.usvao.sso.openid.portal.spring.VAOLogin VAOLogin} instance, 
 * it will create and cache a 
 * {@link PortalUser org.usvao.sso.openid.portal.PortalUser} instance which 
 * not only gives access to user attribute information and the certificate 
 * but also authorization information.  
 */
public class PortalSessionSuccessHandler 
    extends VAOAuthenticationSuccessHandler
{
    public PortalSessionSuccessHandler() {
        this(null);
    }

    public PortalSessionSuccessHandler(Log logger) {
        super((logger == null) 
                 ? LogFactory.getLog(PortalSessionSuccessHandler.class)
                 : logger);
    }

    /**
     * Called when the user has successfull authenticated.  This will
     * <ol>
     *   <li> create the VAOLogin and bind it to the servlet session,</li>
     *   <li> download the certificate and cache it into the VAOLogin 
     *        instance, and  </li>
     *   <li> create the PortalUser instance, initialize it, and bind it
     *        to the servlet session. </li>
     * </ol>
     */
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
        throws ServletException, IOException 
    {
        super.onAuthenticationSuccess(request, response, authentication);

        // create and initialize the PortalUser instance
    }
}