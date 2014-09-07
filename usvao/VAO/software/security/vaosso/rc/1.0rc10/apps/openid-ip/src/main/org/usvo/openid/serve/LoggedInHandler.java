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
import java.net.URL;
// import java.util.Map;

/** 
 * Create an HTTP response that confirms the success of a login attempt
 * and returns the user back to the relying party (portal).
 */
public class LoggedInHandler {
    private static final Log log = LogFactory.getLog(LoggedInHandler.class);

    private ServerManager manager = new ServerManager();
    private ParameterList params;
    private AuthnAttempt authn;
    private HttpServletResponse response;
    private Attributes attributes = null;
    private String returnURL = null;

    public LoggedInHandler(ServerManager manager, ParameterList params,
                           AuthnAttempt authn, HttpServletResponse response)
    {
        this.manager = manager;
        this.params = params;
        this.authn = authn;
        this.response = response;
    }

    public LoggedInHandler(ServerManager manager, ParameterList params,
                           AuthnAttempt authn, HttpServletResponse response,
                           Attributes attributes)
    {
        this(manager, params, authn, response);
        setAttributes(attributes);
    }

    /**
     * set the attributes that should be sent back to the relying party
     */
    public void setAttributes(Attributes attrs) { attributes = attrs; }

    /**
     * set the returnURL to be used on redirect.  This is to override the 
     * internally generated URL.  
     */
    public void setReturnUrl(URL url) {
        returnURL = url.toString();
    }

    /**
     * generate the X.509 credentials and return the URL to use to retrieve 
     * it.
     */
    public String generateCredential(AuthnAttempt authn) 
        throws IOException
    {
        AuthnAttempt credentialAuthn;
        if (authn.getCookieSession() == null) {
            log.error("No cookie associated with authentication; unable to " +
                      "retrieve credential.");
            return null;
        }

        credentialAuthn = LoginKit.generateCredential(authn);
        if (!credentialAuthn.isSuccessful())
            log.error("Failed to create credential for " + authn.getUsername()
                      + " despite successful login: " + 
                      credentialAuthn.getMessage());
        // else 
        //     log.debug("Created credential at " + credentialAuthn.getMessage());

        return credentialAuthn.getMessage();
    }

    /** 
     * assemble the URL that will return the user's browser back to the 
     * rellying party's portal.  
     */
    public String buildReturnUrl() throws OpenIDException, IOException {
        log.info("Creating OpenID authentication response ("
                + (authn.isSuccessful() ? "authenticated" 
                                        : "not authenticated") + ").");

        String requestedId = params.getParameter("openid.identity").getValue();
        String actualOpenId = Conf.get().getId(authn.getUsername());

        // make sure the URL schemes on the IDs match
        if (requestedId != null && !requestedId.equals(AuthRequest.SELECT_ID))
             actualOpenId = Conf.ensureScheme(Conf.getScheme(requestedId), 
                                              actualOpenId);

        AuthRequest authReq = authn.getAuthRequest();

        // VSY: Why disturb authReq with the new ID?
        // authReq.setIdentity(actualOpenId);

        // if the original request did not request a login ID (i.e. the
        // user entered a login name in the form, set the claimed ID to 
        // actual authentication (local) ID.
        String actualClaimedId = null;
        if (authReq.getClaimed() == null ||
            authReq.getClaimed().equals(AuthRequest.SELECT_ID))
            actualClaimedId = actualOpenId;

        Message result = manager.authResponse(params, actualOpenId, 
                                              actualClaimedId, 
                                              authn.isSuccessful(), false);
        if (result instanceof DirectError) {
            response.getWriter().print(result.keyValueFormEncoding());
            return null;
        }

        // handle AttributeExchange
        MessageExtension ext = null;
        if (authReq.hasExtension(AxMessage.OPENID_NS_AX)) {
            ext = authReq.getExtension(AxMessage.OPENID_NS_AX);
            if (ext != null && ! (ext instanceof FetchRequest)) {
                log.warn("ignoring non-attribute fetch request");
                ext = null;
            }
        }

        if (ext != null && attributes != null) {
            // add attributes to response
            FetchRequest fetchReq = (FetchRequest) ext;
            FetchResponse fetchResp = 
                FetchResponse.createFetchResponse(fetchReq, 
                                                  attributes.export());
            // can manually add attribute values, instead, with 
            // fetchResp.addAttribute()
            result.addExtension(fetchResp);
        }

        // handle SREG
        if (authReq.hasExtension(SRegMessage.OPENID_NS_SREG)) {
            log.warn("Simple Registration (SREG) request received.  Not supported.  Ignoring.");
//            MessageExtension ext = authReq.getExtension(SRegMessage.OPENID_NS_SREG);
//            if (ext instanceof SRegRequest) {
//                SRegRequest sregReq = (SRegRequest) ext;
//                List required = sregReq.getAttributes(true);
//                List optional = sregReq.getAttributes(false);
//                log.info("Received SREG request -- required: " + required + "; optional: " + optional);
//                if (required.contains("email")) {
//                    // data released by the user
//                    Map userDataSReg = new HashMap();
//                    //userData.put("email", "user@example.com");
//
//                    SRegResponse sregResp = SRegResponse.createSRegResponse(sregReq, userDataSReg);
//                    // (alternatively) manually add attribute values
//                    sregResp.addAttribute("email", authn.getEmail());
//                    result.addExtension(sregResp);
//                }
//            }
//            else {
//                throw new UnsupportedOperationException("not implemented");
//            }
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

    /**
     * send the Redirect for sending the user back to the relying party's 
     * portal
     */
    public void redirectToPortal() throws IOException, OpenIDException {
        if (attributes != null && attributes.needsCredential())
            attributes.setCredential(generateCredential(authn));

        String url = returnURL;
        if (url == null) url = buildReturnUrl();
        log.debug("Redirecting to logged-in URL: " + url);

        response.sendRedirect(url);
    }

}
