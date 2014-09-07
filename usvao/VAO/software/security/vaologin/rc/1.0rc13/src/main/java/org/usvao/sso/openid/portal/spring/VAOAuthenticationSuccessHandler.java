package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.VAOLogin;
import org.usvao.sso.openid.portal.AvailabilityExpiredException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.
       SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.openid.OpenIDAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class for automatically downloading a user certificate from the 
 * VAO SSO service upon successful authentication.  
 * <p>
 * This class is intended for plugging into the Spring Security Framework
 * configuration.  Spring's OpenID configuration 
 * (via <code>&lt;openid-login&gt;</code>'s 
 * <code>authentication-success-handler-ref</code> attribute)
 * supports providing an AuthenticationSuccessHandler class that will be 
 * invoked just after validating a successful OpenID authentication.  This 
 * class leverages the handler hook to download a user certificate if a URL 
 * pointing to it was returned among the OpenID attributes.  
 * <p>
 * This implementation will initialize a 
 * {@link org.usvao.sso.openid.portal.VAOLogin VAOLogin} instance 
 * and bind it as an attribute of the servlet session (under the name,
 * VAOLogin.SESSION_ATTR_KEY).  It will then download the certificate 
 * if one was requested and store in that VAOLogin instance; it can later
 * be accessed via its 
 * {@link org.usvao.sso.openid.portal.VAOLogin#getCertificate() getCertificate()} 
 * or 
 * {@link org.usvao.sso.openid.portal.VAOLogin#getCertificatePEM() getCertificatePEM()} methods.  
 * 
 */
public class VAOAuthenticationSuccessHandler
    extends SavedRequestAwareAuthenticationSuccessHandler 
{
    private Log log = null;

    public VAOAuthenticationSuccessHandler() {
        this(null);
    }

    public VAOAuthenticationSuccessHandler(Log logger) {
        if (logger == null)
            logger = LogFactory.getLog(VAOAuthenticationSuccessHandler.class);
        log = logger;
    }

    public VAOLogin createLogin(HttpServletRequest request) {
        return SSFOpenID.getLoginInfo();
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

        // create and remember the login info
        VAOLogin login = createLogin(request);
        request.getSession().setAttribute(VAOLogin.SESSION_ATTR_KEY, login);

        try {
            login.cacheCertificate();
        }
        catch (AvailabilityExpiredException ex) {
            if (log.isErrorEnabled())
                log.error("Failed to cache certficiate: " +
                          "URL has apparently expired");
        }
        catch (MalformedURLException ex) {
            if (log.isErrorEnabled())
                log.error("Failed to cache certficiate: " +
                          "URL appears malformed: " + ex.getMessage());
        }
        catch (IOException ex) {
            if (log.isErrorEnabled())
                log.error("Failed to cache certficiate: " + ex.getMessage());
        }
    }
}
