package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.server.ServerManager;
import org.usvo.openid.orm.Portal;
import org.usvo.openid.orm.PortalPreferences;
import org.usvo.openid.orm.PortalManager;
import org.usvo.openid.orm.OrmKit;
import org.usvo.openid.orm.NvoUser;
import org.usvo.openid.orm.UserPreference;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.ui.LoginUI;
import org.usvo.openid.util.Compare;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import static org.usvo.openid.ui.LoginUI.PARAM_PASSWORD;
import static org.usvo.openid.ui.LoginUI.PARAM_USERNAME;

/** 
 *  A service for handling a single request for authentication. <p>
 *  Usually used for OpenID authentication, but also works for simple 
 *  authentication when an OpenID response is not requested. 
 */
public class IdRequest {
    private static final Log log = LogFactory.getLog(IdRequest.class);

    /**
     *  A buffer to in detecting an expiring session <p>
     * 
     *  To avoid a race condition during authentication and attribute 
     *  exchange, how many extra seconds do we require a cookie session 
     *  to be good for in order to use it?  We want to avoid a situation 
     *  where:
     *     1. we authenticate based on an unexpired session, 
     *     2. the session expires, 
     *     3. we request a credential from MyProxy, which rejects the 
     *        request because the session is expired. 
     */
    private static final int SESSION_MARGIN_SECS = 60;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServerManager manager;
    private ParameterList params;

    // The OpenID authentication request that this is handling.
    private AuthRequest authRequest; 

    // requested openid URL and username chopped off end of it
    private String requestedId, requestedUsername; 

    /** 
     * Initialize a request for authentication. 
     */
    public IdRequest(ServerManager manager, HttpServletRequest request, 
                     HttpServletResponse response)
        throws OpenIDException
    {
        this.manager = manager;
        this.request = request;
        this.response = response;
        initOpenidParams();

        // construct an AuthRequest from the OpenID request
        if (isOpenidAuthRequest()) {
            authRequest = AuthRequest.createAuthRequest(params, 
                                              manager.getRealmVerifier());
            requestedId = OpenIdKit.getUserId(authRequest);
            requestedUsername = OpenIdKit.getUsername(authRequest);
        }
    }

    /** 
     * Handle an OpenID request and send the appropriate interface to the user. 
     */
    public void handleOpenidRequest() throws OpenIDException, IOException {
        // no encryption necessary when https used -- but some clients may not specify it
        Parameter sessionType = params.getParameter("openid.session_type");
        log.debug("openid.session_type: " + sessionType + "; scheme: " + request.getScheme());
        log.debug("URL = " + request.getRequestURL());
        if ("https".equals(request.getScheme()) && sessionType == null)
            params.set(new Parameter("openid.session_type", "no-encryption"));

        if (isOpenidAuthRequest())
            // display the signin page
            handleSigninRequest();
        else {
            Message idResponse;
            if (isOpenidAssociateRequest())
                // --- process an association params ---
                idResponse = manager.associationResponse(params);
            // processing a verification params
            // TODO find out what check_authentication means
            else if ("check_authentication".equals(getOpenidMode()))
                idResponse = manager.verify(params);
            else
                idResponse = DirectError.createDirectError("Unknown request");
            // send encoded response
            // - don't println -- linefeed may mess up some parsers (such as OpenID4Java ...)
            response.getWriter().print(idResponse.keyValueFormEncoding());
        }
    }

    /** 
     * Attempt authentication.  Athentication can be accomplished either 
     * by testing the provided authentication token for by responding to 
     * a submitted username and password.  Request parameters can force 
     * the use of username/password, overriding any cookies.
     */
    public AuthnAttempt authenticate() throws OpenIDException {
        boolean logout        = requestParamIsTrue(LoginUI.PARAM_LOGOUT),
                isInteractive = requestParamIsTrue(LoginUI.PARAM_INTERACTIVE);

        // 1. Collect information from OpenID request, form submission, and 
        //    cookies
        //    a. from identity ID in original OpenID request
        boolean isRequested = !Compare.isBlank(requestedUsername);
        //    b. from cookie
        UserSession cookieSession = 
            SessionKit.getLoginSession(request, response, true, true);
        String cookieUsername = 
            cookieSession == null ? null 
                                  : cookieSession.getUser().getUserName();
        // c. from interactive form
        String formUsername = request.getParameter(PARAM_USERNAME);
        String formPassword = request.getParameter(PARAM_PASSWORD);

        // 2. Figure out which username to use

        // Requested ID (from OpenID request) supercedes both session ID and 
        // interactive ID.  (That is, you can't log in with a different ID 
        // than your requested identity ID.  But if you don't specify an 
        // identity ID, you can log in with any ID you know the password for.)
        boolean requestedOverridesSession = isRequested
                && !Compare.isBlank(cookieUsername)
                && Compare.differ(requestedUsername, cookieUsername);

        // interactive ID supercedes session ID
        boolean formOverridesSession = isInteractive && !isRequested
                && !Compare.isBlank(formUsername)
                && Compare.differ(cookieUsername, formUsername);

        // 2. Authenticate based on requested username, form submission, or 
        //    cookie
        String username = null;
        if (isRequested) 
            username = requestedUsername;
        else if (!logout)
            username = formOverridesSession ? formUsername : cookieUsername;

        AuthnAttempt cookieAuthn = null,
                     formAuthn = null;
        if (cookieSession != null)
            // test the cookie
            cookieAuthn = LoginKit.attemptLogin(cookieSession, 
                                                SESSION_MARGIN_SECS);
        if (isInteractive && ! logout)
            // test the username and password
            formAuthn = LoginKit.attemptLogin(username, formPassword);

        // expire session cookie if:
        //  a. logout requested
        //  b. attempted interactive login (either successful or failed, 
        //     doesn't matter which)
        //  c. session is superceded
        if (cookieSession != null && 
            (logout || isInteractive || requestedOverridesSession || 
             formOverridesSession)) 
         {
            log.debug("Session cookie is superceded by interactive authentication: " + formAuthn);
            if (cookieAuthn.isSuccessful()) {
                cookieSession.expire();
                OrmKit.save(cookieSession);
                response.addCookie(cookieSession.getCookie());
            }
            cookieAuthn = null;
        }

        // form overrides cookie, so our final authentication decision is ...
        AuthnAttempt authn = formAuthn != null ? formAuthn : cookieAuthn;

        // at least report back the username, if it is known
        if (authn == null)
            authn = new AuthnAttempt(username, false, null, null);

        if (authRequest != null)
            authn.initAuthRequest(authRequest);

        // set a cookie to save this authentication, if we don't already 
        // have one
        if (authn.isSuccessful() && 
            (cookieSession == null || !cookieSession.isValid())) 
        {
            cookieSession = 
                SessionKit.createLoginSession(authn.getUsername(), 
                                              request, response);
            authn.setCookieSession(cookieSession);
        }

        return authn;
    }

    /* 
     *  What is the username the user is requesting?  Extracted from 
     *  openid.identity.  Return null if no OpenID request is being processed.
     */
    private String getRequestedUsername() throws OpenIDException { 
        return requestedUsername; 
    }

    /**
     * return true if the HTTP request parameter with a given name has 
     * a value recognized as true.
     */
    private boolean requestParamIsTrue(String name) {
        return "true".equalsIgnoreCase(request.getParameter(name));
    }

    /** 
     * Handle a login attempt -- either redirect to a URL or render the 
     * login and confirmation forms. 
     */
    private void handleSigninRequest() throws OpenIDException, IOException {

        // First, authenticate the user
        AuthnAttempt authnAttempt = authenticate();
        String authnUsername = authnAttempt.getUsername();

        // respond to user
        LoginUI ui = new LoginUI(params, request, response, authnAttempt, 
                                 requestedUsername, requestedId);

        if (authnAttempt.isSuccessful()) {
            // authentication was successful

            // first determine the user and portal we're dealing with.
            PortalManager portalMgr = new PortalManager();
            portalMgr.beginTransaction();
            NvoUser user = portalMgr.loadUser(authnUsername);

            String returnUrl = params.getParameterValue("openid.return_to");
            if (returnUrl == null)
                throw new MessageException("Missing openid.return_to " +
                                           "parameter");
            URL rURL = null;
            try {
                rURL = new URL(returnUrl);
            } 
            catch (MalformedURLException ex) {
                throw new MessageException("Malformed openid.return_to " + 
                                           "value.");
            }

            Portal portal = portalMgr.matchPortal(rURL);
            PortalPreferences prefs = null;
            if (portal == null) {
                // unrecognized portal; we'll create a record for it
                portal = new Portal(rURL.getHost());
            }
            else {
                if (portal.isUnsupported()) {
                    cancelLogin();
                    portalMgr.endTransaction();
                    return;
                }

                // get the user's preferences, if they exist
                prefs = portalMgr.getPreferences(authnUsername, portal);

                if (prefs == null) {
                    Long id = portal.getId();
                    if (portal.isApproved() && id != null) {
                        // get the system defaults for a recognized portal
                        log.debug("User " + authnUsername + 
                                  "has no preferences set for " + 
                                  portal.getName());
                        long idv = id.longValue();
                        prefs = portalMgr.getSysDefaultPreferences(idv);
                    }
                }
            }

            if (prefs == null) {
                // create cautious defaults
                log.info("New unrecognized portal: " + portal.getName());
                prefs = portalMgr.getSysDefaultPreferences();
            }

            // this flag tells us if we need to confirm entering the portal
            boolean confirmRequired = false;
            if (prefs.alwaysConfirm()) confirmRequired = true;

            // Check for an attribute request
            FetchRequest fetchReq = null;
            if (authRequest != null && 
                authRequest.hasExtension(AxMessage.OPENID_NS_AX)) 
            {
                MessageExtension ext = 
                    authRequest.getExtension(AxMessage.OPENID_NS_AX);
                try {
                    fetchReq = (FetchRequest) ext;
                }
                catch (ClassCastException ex) {
                    // probably a StoreRequest, which is unsupported
                    String reqname = ext.getClass().getName();
                    if (reqname.startsWith("org.openid4java."))
                        reqname = reqname.substring(reqname.lastIndexOf(".")+1);
                    log.warn("Received unsupported attribute request: " + 
                             reqname + "; ignoring.");
                }
            }
                  
            Attributes requested = null;
            if (fetchReq != null) {
                // attributes have been requested.  

                // Now lets look at the requested attributes
                UserAttributes useratts = new UserAttributes(user, portal);
                requested = useratts.getRequestedAtts(fetchReq);

                if (LoginUI.isConfirm(request)) {
                    // the user has confirmed which attributes to pass along
                    confirmRequired = false;

                    // approve sharing the attributes according to user's 
                    // choices.
                    for (Attribute att : requested) {
                        ui.setApprovalFor(att);
                    }

                    if (requestParamIsTrue(LoginUI.PARAM_ENABLE_SSO)) {
                        // this means save these choices
                        if (portal.getId() == null) {
                            // new unrecognized portal
                            try {
                                URL use = new URL(rURL.getProtocol(), 
                                                  rURL.getHost(), "/");
                                portal = portalMgr.registerPortal(portal, use);
                            } catch (MalformedURLException ex) { 
                                throw new InternalError("building root URL");
                            }

                        }

                        // make sure we're saving for the right user, portal
                        prefs.setPortal(portal);
                        prefs.setUser(user);

                        ui.updatePreferences(prefs);  // according to the PARAMs
                        portalMgr.savePreferences(prefs);
                    }
                }
                else {

                    // check the Attributes requested against our preferences:
                    // require confirmation there is a request for an attribute 
                    // for which an preferences has not been set.  
                    Boolean ok = null;
                    for (Attribute att : requested) {
                        ok = prefs.getPermission(att);
                        if (ok == null) 
                            confirmRequired = true;
                        else
                            att.setPreferSharing(ok);
                    }

                }
            }

            // we're done with the database
            portalMgr.endTransaction();

            if (confirmRequired) {
                ui.displayDecideForm(getCancelResponse(), requested, 
                                     prefs.alwaysConfirm());
            }
            else {
                LoggedInHandler handler = new LoggedInHandler
                    (manager, params, authnAttempt, response, requested);
                log.debug("Successful signin process is complete");
                handler.redirectToPortal();
            }

        }
        else {
            // not authenticated --> show login form
            ui.displayLoginForm();
        }
    }

    /**
     * respond by canceling the login request
     */
    public void cancelLogin() throws IOException {
        response.sendRedirect(getCancelResponse());
    }

    /** URL that indicates to a Relying Party that authentication is declined. */
    private String getCancelResponse() {
        String claimed_id = params.getParameterValue("openid.claimed_id"),
                identity = params.getParameterValue("openid.identity");
        if (claimed_id == null && identity == null) 
            return null;

        Message result = manager.authResponse(params, identity, claimed_id, 
                                              false, true);
        return result.getDestinationUrl(true);
    }

    /** 
     * Initialize OpenID params either persisted in the session or parsed 
     * from request parameters.
     * If this is a simple request that is part of a non-OpenID transaction, 
     * then the result is likely to be null. 
     */
    private void initOpenidParams() {
        if (!isOpenIdRequest())
            // pull the OpenID parameters from the session
            params = OpenIdKit.getParams(request);
        else {
            // pull the OpenID parameters from the query and cached them
            // into the session.
            params = new ParameterList(request.getParameterMap());
            log.trace("Parsing OpenID Parameters = " + params);
            OpenIdKit.setParams(request, params);
        }
    }

    private transient Boolean isOpenidRequest;
    /** 
     * Does this request represent an OpenID request?  This is a braindead 
     * check -- it could be much better.  Currently it just looks for any 
     * param names starting with "openid.". 
     */
    private boolean isOpenIdRequest() {
        if (isOpenidRequest == null) {
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                if (paramNames.nextElement().toLowerCase().startsWith("openid.")) {
                    isOpenidRequest = true;
                    break;
                }
            }
            
            if (isOpenidRequest == null)
                isOpenidRequest = false;
        }
        return isOpenidRequest;
    }

    /** 
     *  Is the request that triggered this sentry an OpenID authentication 
     *  request?  It could be an association request instead.
     *  Only valid after this.params is initialized. 
     */
    private boolean isOpenidAuthRequest() {
        if (params == null || params.getParameters().size() == 0)
            return false;
        else {
            String mode = getOpenidMode();
            return "checkid_setup".equals(mode) || "checkid_immediate".equals(mode);
        }
    }

    /** What kind of OpenID request is this? */
    private String getOpenidMode() {
        return params.getParameterValue("openid.mode");
    }

    /** Is this an OpenID "associate" request -- that is, a request by a relying party to establish
     *  a back-channel session? */
    private boolean isOpenidAssociateRequest() {
        return "associate".equals(getOpenidMode());
    }
}
