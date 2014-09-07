package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.OpenIDException;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.server.ServerManager;
import org.usvo.openid.Conf;
import org.usvo.openid.orm.UserPreference;
import org.usvo.openid.util.Compare;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/** Create an HTTP response that confirms the success of a login attempt. */
public class LoggedInHandler {
    private static final Log log = LogFactory.getLog(LoggedInHandler.class);

    private ServerManager manager = new ServerManager();
    private ParameterList params;
    private AuthnAttempt authn;
    private AxPreferenceManager prefs;
    private HttpServletResponse response;

    public LoggedInHandler(ServerManager manager, ParameterList params,
                           AuthnAttempt authn, AxPreferenceManager prefs, HttpServletResponse response)
    {
        this.manager = manager;
        this.params = params;
        this.authn = authn;
        this.prefs = prefs;
        this.response = response;
    }

    /** The URL to send the user to that completes the login process. */
    public String getLoggedInUrl() throws OpenIDException, IOException {
        AuthRequest authReq = authn.getAuthRequest();

        // TODO: CHECK TO MAKE SURE WE HAVE AUTHENTICATED THE SAME
        // LOCALID AS HAS BEEN SENT TO US!!!
        String opLocalId = null;
        // if the user chose a different claimed_id than the one in params
        // TODO - is this irrelevant here?
        if (authn.getUsername() != null && authn.getUsername().equals(authReq.getClaimed())) {
            //opLocalId = lookupLocalId(userSelectedClaimedId);
        }

        // Sign after we added extensions.
        String requestedId = params.getParameter("openid.identity").getValue();
        String actualOpenId = Conf.get().getId(authn.getUsername());
        // match scheme (http vs https) to support http at least until we get a commercial cert
        if (requestedId != null && !requestedId.equals(AuthRequest.SELECT_ID))
             actualOpenId = Conf.ensureScheme(Conf.getScheme(requestedId), actualOpenId);
        // VSY: Why disturb authReq with the new ID?
        // authReq.setIdentity(actualOpenId);
        log.info("Creating OpenID authentication response ("
                + (authn.isSuccessful() ? "authenticated" : "not authenticated") + ").");
        String actualClaimedId = null;
        if (authReq.getClaimed() == null ||
            authReq.getClaimed().equals(AuthRequest.SELECT_ID))
            actualClaimedId = actualOpenId;
        Message result = manager.authResponse(params, actualOpenId, actualClaimedId, authn.isSuccessful(), false);

        if (result instanceof DirectError) {
            response.getWriter().print(result.keyValueFormEncoding());
            return null;
        }
        else {
            // handle AttributeExchange
            if (authReq.hasExtension(AxMessage.OPENID_NS_AX)) {
                Map<String, UserPreference> prefsMap = prefs.getAxPrefs();
                MessageExtension ext = authReq.getExtension(AxMessage.OPENID_NS_AX);
                if (ext instanceof FetchRequest) {
                    FetchRequest fetchReq = (FetchRequest) ext;
                    List<AttributeRequest> requestedAttrs = AxPrefsKit.collectRequestedAttributes(fetchReq, true, true);
                    Set<String> requestedKeys = new HashSet<String>();
                    for (AttributeRequest request : requestedAttrs) requestedKeys.add(request.getKey());
                    if (!Compare.sameContents(requestedKeys, prefsMap.keySet()))
                        throw new IllegalStateException("Attributes requested (" + requestedKeys
                                + ") doesn't match user preferences (" + prefsMap.keySet() + ").");
                    // this map's values can be String or List<String>, for multiples (not currently supported by this provider)
                    Map<String, Object> axResponseMap = new HashMap<String, Object>();
                    for (AttributeRequest ax : requestedAttrs) {
                        UserPreference pref = prefsMap.get(ax.getKey());
                        boolean include = pref.isTrue();
                        log.trace("Received AX request for " + ax.getKey() + " (" + ax.getUri() + "); "
                                + "user " + (pref.isTrue() ? "approves" : "disapproves") + "; "
                                + (include ? "sending" : "not sending") + ".");
                        if (include) {
                            String value;
                            if (AxPrefsKit.KEY_CREDENTIAL.equals(ax.getKey())) {
                                AuthnAttempt credentialAuthn;
                                if (authn.getCookieSession() == null) {
                                    log.error("No cookie associated with authentication; unable to retrieve credential.");
                                    value = null;
                                }
                                else {
                                    credentialAuthn = LoginKit.generateCredential(authn);
                                    value = credentialAuthn.getMessage();
                                    if (!credentialAuthn.isSuccessful())
                                        log.error("Failed to create credential for " + authn.getUsername()
                                                + " despite successful login: " + credentialAuthn.getMessage());
                                }
                            }
                            else value = AxPrefsKit.getAttributeValue(ax.getKey(), pref.getUser());
                            if (!Compare.isBlank(value))
                                axResponseMap.put(ax.getAlias(), value);
                        }
                    }
                    FetchResponse fetchResp = FetchResponse.createFetchResponse(fetchReq, axResponseMap);
                    // can manually add attribute values, instead, with fetchResp.addAttribute()
                    result.addExtension(fetchResp);
                }
                else { //if (ext instanceof StoreRequest)
                    log.warn("Attribute StoreRequest received: " + ext + "; not supported.  Ignoring.");
                    // throw new UnsupportedOperationException("not implemented");
                }
            }
            // handle SREG
            if (authReq.hasExtension(SRegMessage.OPENID_NS_SREG)) {
                log.warn("Simple Registration (SREG) request received.  Not supported.  Ignoring.");
//                MessageExtension ext = authReq.getExtension(SRegMessage.OPENID_NS_SREG);
//                if (ext instanceof SRegRequest) {
//                    SRegRequest sregReq = (SRegRequest) ext;
//                    List required = sregReq.getAttributes(true);
//                    List optional = sregReq.getAttributes(false);
//                    log.info("Received SREG request -- required: " + required + "; optional: " + optional);
//                    if (required.contains("email")) {
//                        // data released by the user
//                        Map userDataSReg = new HashMap();
//                        //userData.put("email", "user@example.com");
//
//                        SRegResponse sregResp = SRegResponse.createSRegResponse(sregReq, userDataSReg);
//                        // (alternatively) manually add attribute values
//                        sregResp.addAttribute("email", authn.getEmail());
//                        result.addExtension(sregResp);
//                    }
//                }
//                else {
//                    throw new UnsupportedOperationException("not implemented");
//                }
            }

            // Sign the auth success message.
            // This is required as AuthSuccess.buildSignedList has a `todo' tag now.
            manager.sign((AuthSuccess) result);

            // caller will need to decide which of the following to use:

            // option1: GET HTTP-redirect to the return_to URL
            return result.getDestinationUrl(true);

            // option2: HTML FORM Redirection
            //RequestDispatcher dispatcher =
            //        getServletContext().getRequestDispatcher("formredirection.jsp");
            //request.setAttribute("parameterMap", response.getParameterMap());
            //request.setAttribute("destinationUrl", response.getDestinationUrl(false));
            //dispatcher.forward(params, response);
            //return null;
        }
    }

}
