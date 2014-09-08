/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.vospace.openid;

import edu.jhu.pha.vospace.oauth.MySQLOAuthProvider;
import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

/** A simple implementation of an OpenID relying party, specialized for VOSpace & VAO OpenID.
 *  For more sample code, see OpenID4Java's sample code or the USVAO SSO example
 *  (TODO: get URL once it's in usvao svn). */
public class OpenidClientServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(OpenidClientServlet.class);

    public static final String VAO_IDENTITYLESS_URL = "https://wire.ncsa.uiuc.edu/openid/id/";
    public static final String ACTION_INITIATE = "initiate";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        rememberReferer(request);
        logger.debug("Handling request for \"" + request.getRequestURL() + "\"");
        String action = request.getParameter("action");
        try {
            if (isOpenIdResponse(request)) {
                handleOpenidResponse(request, response);
            }
            else if (ACTION_INITIATE.equalsIgnoreCase(action)) {
                String provider = request.getParameter("provider");
                String idLess = getIdentityless(provider);
                String error = initiateOpenid(request, response, idLess);
                if (error != null)
                    throw new Oops(error);
            }
            else throw new IllegalArgumentException
                    ("Unknown action \"" + action + "\" -- try action=" + ACTION_INITIATE);
        }
        // for local error-reporting, use a private Exception class, Oops (see below)
        catch(Oops e) {
            handleError(e.getMessage(), request, response);
        }
    }

    private void handleOpenidResponse(HttpServletRequest request, HttpServletResponse response)
            throws IOException, Oops {
        ConsumerManager manager = getConsumerManager(request, false);
        if (manager == null)
            throw new Oops("No OpenID session found.  Please try again.");
        DiscoveryInformation discovered
                = (DiscoveryInformation) request.getSession().getAttribute("openid discovered");
        if (discovered == null)
            throw new Oops("No OpenID discovery information found.  Please try again.");
        ParameterList params = new ParameterList(request.getParameterMap());
        try {
            VerificationResult verification = manager.verify(request.getRequestURL().toString(), params, discovered);
            if (!isBlank(verification.getStatusMsg()))
                throw new Oops("OpenID authentication failed: " + verification.getStatusMsg());
            // We're authenticated!  Now approve the request token.
            handleAuthenticated(verification, request, response);
        } catch (OpenIDException e) {
            logger.info("Exception verifying OpenID response.", e);
            throw new Oops("Unable to verify OpenID response: " + e.getMessage());
        }
    }

    /** OpenID authentication succeeded. */
    private void handleAuthenticated
            (VerificationResult verification, HttpServletRequest request, HttpServletResponse response)
            throws IOException, Oops {
        String id = verification.getVerifiedId().getIdentifier();
        // Attribute Exchange: Handle attributes here (if they are requested)
        String username = getUsername(id);
        if (!MySQLOAuthProvider.userExists(username))
            throw new Oops("You've logged in with the identity <i>" + username + "</i>, "
                    + "which is unknown to this VOSpace.  Please contact an administrator to register.");
        String oauthRequest = lookupCookie(request, "oauth_request");
        if (isBlank(oauthRequest))
            throw new Oops("No oauth request token present among cookies.");
        try {
            MySQLOAuthProvider.markAsAuthorized(oauthRequest, username);
            // Copied from AuthorizationServlet.returnToConsumer()
            succeeded("You have successfully authenticated using your VAO ID, " + username + ".\n"
                    + "Please close this browser window and click continue in the client.", response);
        } catch (SQLException e) {
            logger.info("Exception authorizing request token for " + id, e);
            throw new Oops("Database error: " + e.getMessage());
        }
    }

    private void succeeded(String s, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print(s);
    }

    /** Look up the value of a named cookie. Null if not found. */
    private String lookupCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies)
            if (cookie.getName().equals(name))
                return cookie.getValue();
        return null;
    }

    private String getUsername(String id) {
        int slash = id.lastIndexOf('/');
        return id.substring(slash + 1);
    }

    private ConsumerManager getConsumerManager(HttpServletRequest request, boolean createIfAbsent) {
        ConsumerManager manager = (ConsumerManager) request.getSession().getAttribute("openid manager");
        if (manager == null && createIfAbsent) {
            manager = new ConsumerManager();
            request.getSession().setAttribute("openid manager", manager);
        }
        return manager;
    }

    /** Initiate OpenID authentication.  Return null if successful and no further action is necessary;
     *  return an error message if there was a problem. */
    private String initiateOpenid(HttpServletRequest request, HttpServletResponse response, String idLess)
            throws IOException
    {
        ConsumerManager manager = getConsumerManager(request, true);
        try {
            List discoveries = manager.discover(idLess);
            DiscoveryInformation discovered = manager.associate(discoveries);
            request.getSession().setAttribute("openid discovered", discovered);
            String returnUrl = request.getRequestURL().toString();
            if (returnUrl.indexOf('?') > 0)
                returnUrl = returnUrl.substring(0, returnUrl.indexOf('?'));
            AuthRequest authRequest = manager.authenticate(discovered, returnUrl);
            response.sendRedirect(authRequest.getDestinationUrl(true));
        } catch (DiscoveryException e) {
            logger.warn("Exception during OpenID discovery.", e);
            return "Unable to contact OpenID provider: " + e.getMessage();
        } catch (OpenIDException e) {
            logger.warn("Exception processing authentication request.", e);
        }
        return null; // no errors
    }

    /** The URL to use for identityless authentication for a provider.  Not all providers support it
     * -- we will need to do something fancier with discovery etc. for the general case, although
     * this will work fine with VAO SSO. */
    private static String getIdentityless(String providerName) {
        // TODO: get VAO URL from configuration instead
        if (isBlank(providerName))
            throw new IllegalArgumentException("No provider specified.  Try provider=vao.");
        if ("vao".equalsIgnoreCase(providerName))
            return VAO_IDENTITYLESS_URL;
        else throw new IllegalArgumentException("Unknown provider: \"" + providerName + "\".");
    }

    private static boolean isBlank(String s) { return s == null || s.trim().length() == 0; }

    /** Private exception class for displaying error conditions to the user within this servlet. */
    private static class Oops extends Exception {
        Oops(String message) {
            super(message);
            if (message == null)
                throw new NullPointerException("Message is null.");
        }
    }

    /** Show an error to the user. Does not log it, though -- assumes it is already logged, if appropriate. */
    private void handleError(String error, HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        String referer = getOriginalReferer(request);
        // this is kind of a hack -- we want to ensure that the user sees the message,
        // and authorize.jsp supports the ERROR parameter, but that could change, so this
        // may be kind of fragile
        if (!isBlank(referer) && referer.contains("authorize")) {
            // avoid accumulating errors
            if (referer.contains("&ERROR="))
                referer = referer.substring(0, referer.indexOf("&ERROR="));
            if (referer.contains("?ERROR="))
                referer = referer.substring(0, referer.indexOf("?ERROR="));
            // encode & append error message
            referer += (referer.contains("?") ? "&" : "?") + "ERROR=" + URLEncoder.encode(error, "UTF-8");
            logger.debug("Redirecting to " + referer);
            response.sendRedirect(referer);
        }
        // Otherwise, fall back to displaying the error in its own page -- kind of primitive, with no recourse
        // for the user.  Maybe forward to a full dedicated error page?
        else {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println("<html><title>Error - " + error + "</title>");
            response.getWriter().println("<body><h1>Error</h1>");
            response.getWriter().println("<p>" + error + "</p>");
            response.getWriter().println("</body></html>");
        }
    }

    private static final String REFERER_SESSION_KEY = "openid referer";
    /** Try to remember the original referer, so that we can show errors.
     *  It may be better to hardwire the login page (authorize.jsp) as the referer -- that's probably
     *  where we want to show errors anyway. */
    private void rememberReferer(HttpServletRequest request) {
        String oldReferer = (String) request.getSession().getAttribute(REFERER_SESSION_KEY);
        String newReferer = request.getHeader("Referer");
        // if we haven't already made a note of a referer, remember it now
        if (isBlank(oldReferer) && !isBlank(newReferer)) {
            request.getSession().setAttribute(REFERER_SESSION_KEY, newReferer);
            logger.debug("Remembering openid login referer as \"" + newReferer + "\".");
        }
        else
            logger.debug("OpenID login referer: no change (was \""
                    + oldReferer + "\"; not updated to \"" + newReferer + "\".");
    }

    private String getOriginalReferer(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(REFERER_SESSION_KEY);
    }

    private boolean isOpenIdResponse(HttpServletRequest request) {
        return !isBlank(request.getParameter("openid.ns"));
    }
}
