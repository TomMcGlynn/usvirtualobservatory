package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.openid4java.message.ParameterList;
import org.usvo.openid.Devel;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.serve.IdRequest;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.OpenIdKit;
import org.usvo.openid.ui.ErrorResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 
 * Servlet for handling user interactions as part of an OpenID 
 * authentication. This will handle three possible user-interactive 
 * responses:
 * <ol>
 *   <li> processing the username and password entered into the login page
 *   <li> processing the confirmation to share user attributes
 *   <li> postponing confirmation to log in as another user.  
 * </ol>
 */
public class SigninServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(SigninServlet.class);

    public SigninServlet() { super(true); }

    @Override
    protected void service(Session hibernateSession, HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException, ServletException, OpenIDException
    {
        log.debug("Signin request: " + request.getRequestURL());

        // As this service is not intended to be called directly from 
        // portals, confirm that we were referred to from our own app.
        // This is not spoof-proof, but can serve as an advisory to the
        // portal developer
        String referer = request.getHeader("Referer");
        if (referer != null && ! referer.contains(request.getContextPath())) {
            log.warn("Apparent access from external referer: " + referer);
            ErrorResponse.reportError("signin servlet not intended for " +
                                      "external access", 
                                      500, request, response);
            return;
        }
        if ("http".equalsIgnoreCase(request.getScheme()) && !Devel.DEVEL) {
            log.warn("Rejecting non-SSL access (should not happen)");
            ErrorResponse.reportError("Login requested via http; must be via "+
                                      "https.", 500, request, response);
            return;
        }

        IdRequest idreq = IdServer.createIdRequest(getServletContext(),
                                                   request, response);

        if (! idreq.isForOpenIDAuthRequest()) {
            // an OpenID request appears not to be in progress
            String msg = "<p>Sorry, but your your login session is "+
                         "unavailable.  You may have been sent to this URL "+
                         "incorrectly, or this server may have been "+
                         "restarted recently.</p>"+
                         "<p>Please go back to the site requesting login "+
                         "and try again.</p>";
            ErrorResponse.reportError(msg, 500, request, response);
            return;
        }

        // Determine what stage in the signin process we are responding to
        if ("true".equalsIgnoreCase(request.getParameter("interactive"))) {
            // from the login page
            idreq.handlePortalSignin();
        }
        else if ("true".equalsIgnoreCase(request.getParameter("confirm"))) {
            // from the confirmation page 
            idreq.handleConfirmation();
        }
        else {
            // should be from confirmation page from which the user asked 
            // to change users; regardless, start the process over with the 
            // login page.
            String username = request.getParameter("username");
            if ("true".equalsIgnoreCase(request.getParameter("logout")) &&
                username == null) 
                // backward compatibility
                username = "";

            String why = null;
            if (username != null) {
                log.debug("User requesting to change username");
                why = "Requested to switch to a different user";
            }
            AuthnAttempt needsignin = 
                new AuthnAttempt(username, false, why, null);
            idreq.sendPortalLogin(needsignin, null);
        }
    }
}
