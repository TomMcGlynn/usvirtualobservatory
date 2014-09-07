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
import org.usvo.openid.ui.ConfirmUI;
import org.usvo.openid.util.OpenIdConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;

import static org.usvo.openid.ui.LoginUI.PARAM_PASSWORD;
import static org.usvo.openid.ui.LoginUI.PARAM_USERNAME;

/** 
 *  A handler for various establishing identities and managing sessions.
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
    private ServerManager oidmanager;
    private ParameterList oidparams = null;
    private AuthRequest oidreq = null;
    private boolean _isOpenIdRequest = false, _forOpenIdRequest = false;

    /** 
     * Initialize a request for authentication. 
     */
    public IdRequest(ServerManager manager, 
                     HttpServletRequest request, HttpServletResponse response)
        throws OpenIDException
    {
        oidmanager = manager;
        this.request = request;
        this.response = response;

        _isOpenIdRequest = hasOpenIdParams();
        if (_isOpenIdRequest) {
            // pull the OpenID parameters from the query and cached them
            // into the session.
            oidparams = new ParameterList(request.getParameterMap());
            OpenIdKit.setParams(request, oidparams);
            _forOpenIdRequest = true;
        }
        else {
            // pull the current set from the session
            oidparams = OpenIdKit.getParams(request);
            _forOpenIdRequest = 
                oidparams != null && oidparams.hasParameter("openid.mode");
        }

        if (isForOpenIDAuthRequest()) 
            oidreq = 
                AuthRequest.createAuthRequest(oidparams, 
                                              oidmanager.getRealmVerifier());
    }

    /** 
     * Determine if any of the request parameters are OpenID-specific
     */
    private boolean hasOpenIdParams() {
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            if (paramNames.nextElement().toLowerCase().startsWith("openid.")) 
                return true;
        }
        return false;
    }

    /**
     * return true if the current session is authenticated as a particular 
     * user.  
     * @param username  the username to check.  If null, assume the name
     *                    associated with the current session.
     */
    public AuthnAttempt getAuthenticationStatus(String username) {
        UserSession sess = SessionKit.getLoginSession(request, response, 
                                                      true, true);

        AuthnAttempt out = null;
        if (sess == null || 
            (username != null && 
             ! username.equals(sess.getUser().getUserName())))
            out = new AuthnAttempt(username, false, null, null);
        else 
            out = LoginKit.attemptLogin(sess, SESSION_MARGIN_SECS);

        if (oidreq != null && out.getAuthRequest() == null)
            out.initAuthRequest(oidreq);
        return out;
    }

    /**
     * return true if the current session is authenticated as the user
     * currently associated with the session.
     */
    public AuthnAttempt getAuthenticationStatus() {
        return getAuthenticationStatus(null);
    }

    /**
     * return true if this request is explicitly an OpenID request.
     * This will return true if the current request parameters include
     * any with the "openid." prefix.
     */
    public boolean isOpenIDRequest() { return _isOpenIdRequest; }

    /**
     * return true if this request is in service for an OpenID request.
     * The current request paramters will not include any with the 
     * "openid." prefix; however, such parameters were cached with the 
     * current session, indicating that a request is in progress.  
     */
    public boolean isForOpenIDRequest() { 
        return (oidparams.getParameterValue("openid.mode") == null);
    }

    /**
     * return the OpenID mode or an empty string if this is not 
     * an OpenID request
     */
    public String getOpenIDMode() { 
        String out = oidparams.getParameterValue("openid.mode");
        if (out == null) out = "";
        return out;
    }

    /**
     * send the web page for logging into a local OP servlet.  This will 
     * present the login form, indicating that the user is logging into a 
     * local service
     * @param servicePath    the relative path to the servlet
     */
    public void sendLocalLogin(String servicePath, String serviceLabel,
                               AuthnAttempt status) 
        throws OpenIDException, IOException
    {
        LoginUI ui = LoginUI.forLocal(request, response, status,
                                      servicePath, serviceLabel);
        ui.display(status.getUsername());
    }

    /**
     * send the web page for logging into an external portal
     */
    protected void sendPortalLogin(AuthnAttempt status, Portal portal) 
        throws OpenIDException, IOException
    {
        LoginUI ui = LoginUI.forPortal(request, response, status, 
                                       getReturnURL(), portal);

        LoginUI.UsernameField uedit = LoginUI.UsernameField.EDITABLE;
        String oid = null;
        if (oidreq != null) oid = OpenIdKit.getUserId(oidreq);
        if (oid != null) uedit = LoginUI.UsernameField.LOCKED;
        ui.display(status.getUsername(), uedit);
    }

    /**
     * handle a OpenID request as specified by the openid.mode
     */
    public void handleOpenIDRequest() 
        throws OpenIDException, IOException
    {
        handleOpenIDRequest(false);
    }

    /**
     * handle a OpenID request as specified by the openid.mode
     * @param force   if true, enforce the need to explicitly sign in
     *                  with a password.  If openid.mode=checkid_setup,
     *                  a true value will force the display of the login
     *                  form.  This value is ignored for other modes.
     */
    public void handleOpenIDRequest(boolean force) 
        throws OpenIDException, IOException
    {
        String mode = getOpenIDMode();
        if (log.isDebugEnabled())
            log.debug("handling OpenID mode: " + mode);

        Parameter sessionType = oidparams.getParameter("openid.session_type");
        if ("https".equals(request.getScheme()) && sessionType == null)
            oidparams.set(new Parameter("openid.session_type","no-encryption"));

        if ("checkid_setup".equals(mode))
            handleCheckid(false, force);
        else if ("associate".equals(mode)) 
            handleAssociation();
        else if ("checkid_immediate".equals(mode)) 
            handleCheckid(true);
        else if ("check_authentication".equals(mode))
            handleCheckAuthentication();
        else if ("".equals(mode))
            throw new IllegalStateException("Not an OpenID request");
        else {
            String msg = "Unrecognized OpenID request";
            sendMessage(DirectError.createDirectError(msg));
        }
    }

    /**
     * handle the association request
     */
    protected void handleAssociation() 
        throws OpenIDException, IOException 
    {
        log.debug("handling association");
        sendMessage(oidmanager.associationResponse(oidparams));
    }
    
    /**
     * handle the check_authentication request
     */
    protected void handleCheckAuthentication() 
        throws OpenIDException, IOException 
    {
        log.debug("handling check_authentication");
        sendMessage(oidmanager.verify(oidparams));
    }
    
    /**
     * handle the checkid_setup request
     * @param immediate    respond to RP with an immediate yea/nay without
     *                      allowing user interaction.  If user interaction 
     *                      is at all required, a negative response will be
     *                      sent.
     */
    public void handleCheckid(boolean immediate) 
        throws OpenIDException, IOException 
    {
        handleCheckid(immediate, false);
    }
    
    /**
     * handle the checkid_setup request
     * @param immediate    respond to RP with an immediate yea/nay without
     *                      allowing user interaction.  If user interaction 
     *                      is at all required, a negative response will be
     *                      sent.
     * @param force        if force is true, force the user to login with 
     *                      a password.
     */
    public void handleCheckid(boolean immediate, boolean force) 
        throws OpenIDException, IOException 
    {
        if (log.isDebugEnabled())
            log.debug("handling checkid_"+((immediate) ? "immediate":"setup"));
        boolean usingDefPrefs = false;

        if (oidreq == null) 
            throw new IllegalStateException("No current OpenID Auth request in "+
                                            "progress");
        String username = OpenIdKit.getUsername(oidreq); // usually null for VAO
        AuthnAttempt status = getAuthenticationStatus(username);
        if (status.isSuccessful() && force)
            status = new AuthnAttempt(username, false, 
                                 "Portal requests that you confirm your login", 
                                      null);

        // first determine the user and portal we're dealing with.
        PortalManager portalMgr = new PortalManager();
        portalMgr.beginTransaction();

        Portal portal = getRequestingPortal(portalMgr, null);
        if (portal.isUnsupported()) {
            portalMgr.endTransaction();
            cancelLogin();
            return;
        }

        if (status.isSuccessful()) {
            // authentication was successful
            NvoUser user = portalMgr.loadUser(username);
            username = status.getUsername();

            // get the user's preferences (or defaults)
            PortalPreferences prefs = portalMgr.getPreferences(username, portal);
            if (prefs == null) {
                prefs = getDefaultPreferencesFor(portal, portalMgr);
                usingDefPrefs = true;
            }
            // we're done with the database
            portalMgr.endTransaction();

            // this flag tells us if we need to confirm entering the portal
            boolean confirmRequired = false;
            if (prefs.alwaysConfirm()) {
                log.debug("User preferences call for confirmation");
                confirmRequired = true;
            }

            // Check for an attribute request
            FetchRequest fetchReq = getFetchRequest();
            Attributes requested = null;
            if (fetchReq == null) {
                // no attributes asked for; no need to ask for confirmation
                confirmRequired = false;
            }
            else {
                // attributes have been requested.  

                // Now lets look at the requested attributes
                UserAttributes useratts = new UserAttributes(user, portal);
                requested = useratts.getRequestedAtts(fetchReq);

                // check the Attributes requested against our preferences:
                // require confirmation there is a request for an attribute 
                // for which an preferences has not been set.  
                Boolean ok = null;
		String confonmsg = null;
                for (Attribute att : requested) {
                    if (att.getType() != 
                                    UserAttributes.LocalType.UNSUPPORTED)
                    {
                        ok = prefs.getPermission(att);
                        if (ok == null) {
                            confirmRequired = true;
                            if (log.isDebugEnabled()) {
                                if (confonmsg == null)
                                    confonmsg = "Request for unconfirmed " +
                                        "attribute(s):";
                                confonmsg += " " + att.getParamName();
                            }
                        }
                        else {
                            att.setPreferSharing(ok);
                            att.setAllowSharing(ok);
                        }
                    }
                }
                if (confonmsg != null) log.debug(confonmsg);

                // override if confirmation if an approved portal is only
                // asking for a user name
                if (usingDefPrefs && portal.isApproved() && 
                    requested.size() == 1 && 
                    requested.includes(UserAttributes.LocalType.USERNAME)) 
                {
                    Attribute att = 
                requested.getAttribute(UserAttributes.LocalType.USERNAME);
                    att.setPreferSharing(true);
                    att.setAllowSharing(true);
                    confirmRequired = false;
                }
            }

            if (confirmRequired) {
                if (immediate) {
                    log.debug("confirmation required; immediate forces neg response");
                    cancelLogin();
                    return;
                }
                log.debug("requesting confirmation of attribute sharing");
                ConfirmUI ui = new ConfirmUI(request, response, status, 
                                             getReturnURL(), portal);
                ui.display(getCancelResponse(), requested, prefs.alwaysConfirm(),
                        (oidreq != null && OpenIdKit.getUserId(oidreq) == null));
            }
            else {
                LoggedInHandler handler = new LoggedInHandler
                    (oidmanager, oidparams, status, response, requested);
                log.debug("Successful signin process is complete");
                clearOpenidParams();
                handler.redirectToPortal();
            }
        }
        else if (immediate) {
            if (log.isDebugEnabled())
                log.debug(status.getMessage()+": immediate forces neg response");
            cancelLogin();
        }
        else {
            // login needed
            sendPortalLogin(status, portal);
        }
        portalMgr.endTransaction();
    }

    /**
     * assume that the current request comes from the login page and 
     * authenticate the user via the provided username and password.
     */
    public AuthnAttempt signin() {
        return signin(null);
    }

    /**
     * assume that the current request comes from the login page and 
     * authenticate the user via the provided username and password.
     * @param username    require that the given username be null or 
     *                      match the username given as a request 
     *                      paramater
     */
    public AuthnAttempt signin(String username) {
        // invalidate the current session
        UserSession sess = SessionKit.getLoginSession(request, response,
                                                      true, true);
        if (sess != null) endSession(sess);
        
        String password = request.getParameter(PARAM_PASSWORD);
        String user = request.getParameter(PARAM_USERNAME);
        if ("".equals(user)) user = null;
        if (username != null) {
            if (user == null) 
                user = username;
            else if (! username.equals(user))
                return new AuthnAttempt(username, false, 
                     "Username provided does not match required username", null);
        }

        AuthnAttempt authn = LoginKit.attemptLogin(user, password);

        if (authn.isSuccessful()) {
            // set the session
            sess = SessionKit.createLoginSession(authn.getUsername(),
                                                 request, response);
            authn.setCookieSession(sess);
        }
        return authn;
    }

    /**
     * attempt to authenticate the user, either via a cookie or by processing
     * a username and password.
     * @param force      if true, force authentication by username and password
     */
    public AuthnAttempt authenticate(boolean force) {
        return authenticate(force, null);
    }

    /**
     * attempt to authenticate the user, either via a cookie or by processing
     * a username and password.
     */
    public AuthnAttempt authenticate() {
        return authenticate(false);
    }

    /**
     * attempt to authenticate the user, either via a cookie or by processing
     * a username and password.  The latter is used if both username and
     * password are present, and cookie authentication is applied otherwise.
     * @param force      if true, force authentication by username and password
     * @param username   require authentication as given name; 
     */
    public AuthnAttempt authenticate(boolean force, String username) {
        if (request.getParameter(PARAM_PASSWORD) != null &&
            (request.getParameter(PARAM_USERNAME) != null || username != null))
        {
            // process username/password provided
            return signin(username);
        }

        if (force) {
            // caller explicitly asked for authentication via username/password
            // but this was not provided.
            if (username == null) 
                username = request.getParameter(PARAM_PASSWORD);
            return new AuthnAttempt(username, false, 
                                    "Login explicitly requested", null);
        }

        // cookie-based authentication
        return getAuthenticationStatus(username);
    }

    /**
     * process portal connection confirmation submission.  This processess 
     * the inputs from the Confirmation page (generated by ConfirmUI) and 
     * sends user onto the portal.
     */
    public void handleConfirmation() 
        throws OpenIDException, IOException
    {
        if (oidreq == null) 
            throw new IllegalStateException("No current OpenID Auth request in "+
                                            "progress");
        String username = OpenIdKit.getUsername(oidreq);
        AuthnAttempt status = getAuthenticationStatus(username);
        if (! status.isSuccessful()) {
            // return to login page
            sendPortalLogin(status, null);
            return;
        }
        String returnUrl = getReturnURL();
        if (returnUrl == null)
            throw new MessageException("Missing openid.return_to parameter");
        URL rURL = null;
        try { rURL = new URL(returnUrl); }
        catch (MalformedURLException ex) {
            throw new MessageException("Malformed openid.return_to value.");
        }

        // first determine the user and portal we're dealing with.
        PortalManager portalMgr = new PortalManager();
        portalMgr.beginTransaction();
        NvoUser user = portalMgr.loadUser(username);

        Portal portal = getRequestingPortal(portalMgr, rURL);
        if (portal.isUnsupported()) {
            portalMgr.endTransaction();
            cancelLogin();
            return;
        }

        // used to check the results
        ConfirmUI ui = new ConfirmUI(request, response, status, 
                                     returnUrl, portal);
        if (ui.cancelRequested()) {
            cancelLogin();
            return;
        }

        // get the user's preferences (or defaults)
        PortalPreferences prefs = portalMgr.getPreferences(username, portal);
        if (prefs == null) 
            prefs = getDefaultPreferencesFor(portal, portalMgr);

        if (log.isDebugEnabled()) {
            StringBuilder sb = 
                new StringBuilder("user's attribute sharing preferences:");
            for (String pname : prefs.paramNames()) {
                sb.append("\n ").append(pname).append("=");
                sb.append(prefs.getPermission(pname));
            }
            log.debug(sb.toString());
        }

        // Check for an attribute request
        FetchRequest fetchReq = getFetchRequest();
                  
        // check sharing
        Attributes requested = null;
        if (fetchReq != null) {
            // attributes have been requested.  

            // Now lets look at the requested attributes
            UserAttributes useratts = new UserAttributes(user, portal);
            requested = useratts.getRequestedAtts(fetchReq);

            // approve sharing the attributes according to user's 
            // choices.
            for (Attribute att : requested) {
                // deal with an unsupported attribute
                if (att.getType() == 
                    UserAttributes.LocalType.UNSUPPORTED) 
                {
                    log.info("Portal (" + portal.getName() + ") requested " +
                             "unsupported attribute: " + att.getURI());
                    if (att.isRequired()) 
                        // perhaps not the best solution (should we fail?)
                        att.setValue("");
                    else {
                        att.setAllowSharing(false);
                        if (log.isDebugEnabled())
                            log.debug("Not sharing unrecognized attr: "+
                                      att.getURI());
                    }
                }
                else {
                    ui.setApprovalFor(att);
                }
            }

            if (requestParamIsTrue(ConfirmUI.PARAM_SAVE_CHOICES)) {
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

        // we're done with the database
        portalMgr.endTransaction();

        LoggedInHandler handler = 
            new LoggedInHandler(oidmanager, oidparams, status, 
                                response, requested);
        log.debug("Successful signin process is complete");
        clearOpenidParams();
        handler.redirectToPortal();
    }

    private FetchRequest getFetchRequest() throws MessageException {
        if (oidreq == null || ! oidreq.hasExtension(AxMessage.OPENID_NS_AX)) 
            return null;

        MessageExtension ext = oidreq.getExtension(AxMessage.OPENID_NS_AX);
        try {
            return ((FetchRequest) ext);
        }
        catch (ClassCastException ex) {
            // probably a StoreRequest, which is unsupported
            String reqname = ext.getClass().getName();
            if (reqname.startsWith("org.openid4java."))
                reqname = reqname.substring(reqname.lastIndexOf(".")+1);
            log.warn("Received unsupported attribute request: " + 
                     reqname + "; ignoring.");
        }
        return null;
    }

    private PortalPreferences getDefaultPreferencesFor(Portal portal,
                                                       PortalManager mgr)
    {
        PortalPreferences prefs = null;

        Long id = portal.getId();
        if (portal.isApproved() && id != null) {
            // get the system defaults for a recognized portal
            log.debug("User has no preferences set for " + portal.getName());
            prefs = mgr.getSysDefaultPreferences(id.longValue());
        }

        if (prefs == null) {
            // create cautious defaults
            log.info("New unrecognized portal: " + portal.getName());
            prefs = mgr.getSysDefaultPreferences();
        }

        return prefs;
    }

    private String getReturnURL() {
        return oidparams.getParameterValue("openid.return_to");
    }

    private Portal getRequestingPortal(PortalManager mgr, URL rURL) 
        throws OpenIDException
    {
        if (rURL == null) {
            String returnUrl = getReturnURL();
            if (returnUrl == null)
                throw new MessageException("Missing openid.return_to parameter");

            try {
                rURL = new URL(returnUrl);
            } 
            catch (MalformedURLException ex) {
                throw new MessageException("Malformed openid.return_to value.");
            }
        }

        Portal portal = mgr.matchPortal(rURL);
        if (portal == null) {
            // unrecognized portal; we'll create a record for it
            if (log.isDebugEnabled()) 
                log.debug("Not recognizing portal at " + rURL.getHost() +
                          "; creating new record for it");
            portal = new Portal(rURL.getHost());
            portal.setUrl(rURL.toString());
        }

        return portal;
    }       

    /**
     * display the login screen
     */

    private void clearOpenidParams() {
        OpenIdKit.clearParams(request);
    }

    /**
     * If logged in with the given session, end it.
     */
    public void endSession(UserSession sess) {
        if (sess == null || ! sess.isValid()) return;

        sess.expire();
        OrmKit.save(sess);
        response.addCookie(sess.getCookie());
    }

    /**
     * respond by canceling the login request
     */
    public void cancelLogin() throws IOException {
        if (_forOpenIdRequest) {
            String url = getCancelResponse();
            clearOpenidParams();
            response.sendRedirect(url);
        }
    }

    /** 
     * return URL to the relying party that indicates that authentication is 
     * declined. 
     */
    private String getCancelResponse() {
        if (! _forOpenIdRequest) return null;
        String claimed_id = oidparams.getParameterValue("openid.claimed_id"),
                identity = oidparams.getParameterValue("openid.identity");
        if (claimed_id == null && identity == null) 
            return null;

        Message result = oidmanager.authResponse(oidparams, identity, 
                                                 claimed_id, false, true);
        return result.getDestinationUrl(true);
    }

    /**
     * respond with an OpenID Message
     */
    protected void sendMessage(Message msg) throws IOException {
        response.getWriter().print(msg.keyValueFormEncoding());
    }

    /**
     * Log the user out by invalidating the session.  If requested, return
     * the user to a specified URL.
     * @param returnto   the URL to return to after ending the session.  If 
     *                      relative, it will be assumed to be a local one.
     *                      If null, try to use the PARAM_RETURN_URL request
     *                      parameter.  If an empty string, PARAM_RETURN_URL
     *                      will not be used; rather, a local default will be.
     */
    public void logout(String returnto) throws OpenIDException, IOException {
        // determine login status from via the cookie
        String user = "unknown";
        UserSession sess = 
            SessionKit.getLoginSession(request, response, true, true);
        if (sess != null) {
            NvoUser u = sess.getUser();
            if (u != null) user = u.getUserName();
        }

        // figure out where to send user
        if (returnto == null && oidparams != null)
           returnto = oidparams.getParameterValue(OpenIdConstants.RETURN_TO);

        if (returnto == null)
            // send back to the page containing the logout link
            returnto = request.getHeader("Referer");

        if (returnto == null) {
            // TODO: make this default configurable
            // send to the sso home page
            returnto = getServerURL().toString();
            log.debug("Post logout, user sent to default: " + returnto);
        }

        // if we are logged in, cancel our session
        log.info("Ending session for user " +user+ " by request");
        clearOpenidParams();
        endSession(sess);

        // send user to post-logout page
        response.sendRedirect(returnto);
    }

    /**
     * return True if there an OpenId authorization request is in progress.
     */
    public boolean isForOpenIDAuthRequest() {
        if (! _forOpenIdRequest) return false;
        String mode = getOpenIDMode();
        return "checkid_setup".equals(mode) || "checkid_immediate".equals(mode);
    }

    /* *
     * return a full URL for a servlet in the current web application.
     * This is used for display purposes.
    protected String getServletURLFor(String servletPath) {
        StringBuffer sb = getServerURL();
        sb.append(request.getContextPath());
        if (servletPath == null || servletPath.charAt(0) != '/')
            sb.append('/');
        sb.append(servletPath);
        return sb.toString();
    }
     */

    /**
     * return the full URL to the application server's home page
     */
    protected StringBuffer getServerURL() {
        StringBuffer out = new StringBuffer(request.getScheme());
        out.append("://");
        String s = request.getHeader("Host");
        if (s != null) {
            out.append(s);
        } else {
            out.append(request.getServerName());
            int p = request.getServerPort();
            if (p != 80 && p != 443) 
                out.append(":").append(Integer.toString(p));
            out.append("/");
        }
        return out;
    }


    /**
     * return true if the HTTP request parameter with a given name has 
     * a value recognized as true.
     */
    private boolean requestParamIsTrue(String name) {
        return "true".equalsIgnoreCase(request.getParameter(name));
    }
}