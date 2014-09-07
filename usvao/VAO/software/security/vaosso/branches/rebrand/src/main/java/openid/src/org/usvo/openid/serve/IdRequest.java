package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.server.ServerManager;
import org.usvo.openid.orm.OrmKit;
import org.usvo.openid.orm.UserPreference;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.ui.LoginUI;
import org.usvo.openid.util.Compare;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.usvo.openid.ui.LoginUI.PARAM_PASSWORD;
import static org.usvo.openid.ui.LoginUI.PARAM_USERNAME;

/** Handle a single request for authentication.
 *  Usually used for OpenID authentication, but also works for simple authentication
 *  when an OpenID response is not requested. */
public class IdRequest {
    private static final Log log = LogFactory.getLog(IdRequest.class);

    /** To avoid a race condition during authentication and attribute exchange, how many extra seconds
     *  do we require a cookie session to be good for in order to use it? We want to avoid a situation where
     *  1. we authenticate based on an unexpired session, 2. the session expires, 3. we request a credential
     *  from MyProxy, which rejects the request because the session is expired. */
    private static final int SESSION_MARGIN_SECS = 60;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServerManager manager;
    private AuthRequest authRequest; // The OpenID authentication request that this is handling.

    private ParameterList params;
    private String requestedId, requestedUsername; // requested openid URL and username chopped off end of it

    /** Initialize a request for authentication. */
    public IdRequest(ServerManager manager, HttpServletRequest request, HttpServletResponse response)
            throws OpenIDException
    {
        this.manager = manager;
        this.request = request;
        this.response = response;
        initOpenidParams();
        // construct an AuthRequest from the OpenID request
        if (isOpenidAuthRequest()) {
            authRequest = AuthRequest.createAuthRequest(params, manager.getRealmVerifier());
            requestedId = OpenIdKit.getUserId(authRequest);
            requestedUsername = OpenIdKit.getUsername(authRequest);
        }
    }

    /** Handle an OpenID request and send the appropriate interface to the user. */
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

    /** Attempt authentication based on form request and cookie.
     *  As a side effect, populates requestedId & requestedUsername. */
    public AuthnAttempt authenticate() throws OpenIDException {
        boolean logout = "true".equalsIgnoreCase(request.getParameter(LoginUI.PARAM_LOGOUT)),
                isInteractive = "true".equalsIgnoreCase(request.getParameter(LoginUI.PARAM_INTERACTIVE));

        // 1. Collect information from OpenID request, form submission, and cookies
        // a. from identity ID in original OpenID request
        boolean isRequested = !Compare.isBlank(requestedUsername);
        // b. from cookie
        UserSession cookieSession = SessionKit.getLoginSession(request, response, true, true);
        String cookieUsername = cookieSession == null ? null : cookieSession.getUser().getUserName();
        // c. from interactive form
        String formUsername = request.getParameter(PARAM_USERNAME);
        String formPassword = request.getParameter(PARAM_PASSWORD);

        // 2. Figure out which username to use

        // Requested ID (from OpenID request) supercedes both session ID and interactive ID
        // (That is, you can't log in with a different ID than your requested identity ID.  But if you
        // don't specify a identity ID, you can log in with any ID you know the password for.)
        boolean requestedOverridesSession = isRequested
                && !Compare.isBlank(cookieUsername)
                && Compare.differ(requestedUsername, cookieUsername);

        // interactive ID supercedes session ID
        boolean formOverridesSession = isInteractive && !isRequested
                && !Compare.isBlank(formUsername)
                && Compare.differ(cookieUsername, formUsername);

        // 2. Authenticate based on requested username, form submission, or cookie
        String username = isRequested ? requestedUsername
                : (logout ? null : (formOverridesSession ? formUsername : cookieUsername));

        AuthnAttempt
                cookieAuthn = cookieSession == null ? null : LoginKit.attemptLogin(cookieSession, SESSION_MARGIN_SECS),
                formAuthn = isInteractive && !logout ? LoginKit.attemptLogin(username, formPassword) : null;

        // expire session cookie if:
        //  a. logout requested
        //  b. attempted interactive login (either successful or failed -- doesn't matter which)
        //  c. session is superceded
        if (cookieSession != null && (logout || isInteractive || requestedOverridesSession || formOverridesSession)) {
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

        // set a cookie to save this authentication, if we don't already have one
        if (authn.isSuccessful() && (cookieSession == null || !cookieSession.isValid())) {
            cookieSession = SessionKit.createLoginSession(authn.getUsername(), request, response);
            authn.setCookieSession(cookieSession);
        }

        return authn;
    }

    /** What is the username the user is requesting?  Extracted from openid.identity.
     *   Null if no OpenID request is being processed. */
    private String getRequestedUsername() throws OpenIDException { return requestedUsername; }

    /** Handle a login attempt -- either redirect to a URL or render the login and confirmation forms. */
    private void handleSigninRequest() throws OpenIDException, IOException {
        // 1. Authenticate
        AuthnAttempt authnAttempt = authenticate();
        boolean isConfirm = "true".equalsIgnoreCase(request.getParameter(LoginUI.PARAM_CONFIRM_LOGIN));

        // 2. Attribute Exchange request
        FetchRequest fetchReq = null;
        if (authRequest != null && authRequest.hasExtension(AxMessage.OPENID_NS_AX))
            fetchReq = (FetchRequest) authRequest.getExtension(AxMessage.OPENID_NS_AX);
        // Map<String, Pair<String, String>> requestedAttributes = AxPrefsKit.collectRequestedAttributes(fetchReq);

        // 3. Handle preferences and respond to user
        LoginUI ui = new LoginUI(params, request, response, authnAttempt, requestedUsername, requestedId);
        // authenticated --> respond based on preferences
        if (authnAttempt.isSuccessful()) {
            // a. check & update preferences
            AxPreferenceManager prefMgr = new AxPreferenceManager(request, authnAttempt, params, fetchReq);
            // Is SSO in effect?  Only trust it if it has been set already -- we don't allow default SSO
            // because a malicious RP could exploit it to get a credential.
            // Note: ID is null if a preference hasn't been saved yet, which means the user
            // hasn't previously confirmed it.
            UserPreference ssoPref = prefMgr.getSsoPreference();
            boolean isSSO = ssoPref.isTrue() && ssoPref.getId() != null;
            // Also preempt SSO if any prefs have not been asked already and are defaulting to false.
            // That is, if they have been saved as false, we interpret that the user wants to
            // never share them with this RP (SSO is presented to the user as "Don't ask me again").
            Collection<UserPreference> prefs = prefMgr.getAllPrefs().values();
            boolean preemptSSO = false;
            for (UserPreference pref : prefs)
                if (pref.getId() == null && !pref.isTrue()) // not saved and not default true
                    preemptSSO = true;

            // b. respond to user
            // signin is complete if (a) the user has clicked Confirm, or (b) SSO is in effect
            boolean signinComplete = isConfirm || (isSSO && !preemptSSO);

            if (isConfirm)
                // 2 reasons to save:
                // (a) may have changed due to user request
                // (b) save a default as a new preference, for later management by the user
                OrmKit.save(prefs);

            if (signinComplete) {
                LoggedInHandler handler = new LoggedInHandler
                        (manager, params, authnAttempt, prefMgr, response);
                String loggedInUrl = handler.getLoggedInUrl();
                log.debug("Signin process is complete; redirecting to logged-in URL: " + loggedInUrl);
                response.sendRedirect(loggedInUrl);
            }
            else
                ui.displayDecideForm(getCancelResponse(), prefMgr);
        }

        // not authenticated --> show login form
        else
            ui.displayLoginForm();
    }

    /** URL that indicates to a Relying Party that authentication is declined. */
    private String getCancelResponse() {
        String claimed_id = params.getParameterValue("openid.claimed_id"),
                identity = params.getParameterValue("openid.identity");
        if (claimed_id == null && identity == null) return null;
        else {
            Message result = manager.authResponse(params, identity, claimed_id, false, true);
            return result.getDestinationUrl(true);
        }
    }

    /** Initialize OpenID params either persisted in the session or parsed from request parameters.
     *  If this is a simple request that is part of a non-OpenID transaction, then the result is likely to be null. */
    private void initOpenidParams() {
        if (!isOpenIdRequest())
            params = OpenIdKit.getParams(request);
        else {
            params = new ParameterList(request.getParameterMap());
            log.trace("Parsing OpenID Parameters = " + params);
            OpenIdKit.setParams(request, params);
        }
    }

    private transient Boolean isOpenidRequest;
    /** Does this request represent an OpenID request?  This is a braindead check -- it could
     *  be much better. Currently it just looks for any param names starting with "openid.". */
    private boolean isOpenIdRequest() {
        if (isOpenidRequest == null) {
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements())
                if (paramNames.nextElement().toLowerCase().startsWith("openid."))
                    isOpenidRequest = true;
            if (isOpenidRequest == null)
                isOpenidRequest = false;
        }
        return isOpenidRequest;
    }

    /** Is the request that triggered this sentry an OpenID authentication request?
     *  It could be an association request instead.
     *  Only valid after this.params is initialized. */
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
