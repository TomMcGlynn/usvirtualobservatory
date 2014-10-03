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
package org.usvao.servlets.oauth;

import org.usvao.exceptions.PermissionDeniedException;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

/** A simple implementation of an OpenID relying party, specialized for VOSpace & VAO OpenID.
 *  For more sample code, see OpenID4Java's sample code or the USVAO SSO example
 *  (TODO: get URL once it's in usvao svn). */
public class AuthorizationServlet extends BaseServlet {
	private static final long serialVersionUID = -1847330043709412488L;

	private static Logger logger = Logger.getLogger(AuthorizationServlet.class);

    public static final String VAO_IDENTITYLESS_URL = "https://wire.ncsa.uiuc.edu/openid/id/";
    public static final String ACTION_INITIATE = "initiate";
    private static final String ALIAS_CERTIFICATE = "certificate",
            AX_URL_CERTIFICATE = "http://sso.usvao.org/schema/credential/x509";
    
    @Override
    /** Handle GET & POST the same way, because OpenID response may be a URL redirection (GET)
     *  or a form submission (POST). */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    @Override public String getErrorPage() { return "index.jsp"; }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        logger.debug("Handling request for \"" + request.getRequestURL() + "\"");
        try {
            if (isOpenIdResponse(request)) {
            	logger.debug("Handle OpenID");
                handleOpenidResponse(request, response);
            } else {
            	logger.debug("Initiate");
            	String userName = checkCertificate(request);
            	if(null != userName){ // made X.509 authentication
            		logger.debug("Certificate checked. Username: "+userName);
                        System.out.println("Certificate checked. Username: "+userName);

//                    if (!UserHelper.userExists(userName)) {
//                        UserHelper.addDefaultUser(userName);
//                    }

            		authorizeRequestToken(request, response, userName);
            	} else { // need to do openid
            		logger.debug("OpenID init");
                        System.out.println("OpenID init");
	                String provider = request.getParameter("provider");
	                String idLess = getIdentityless(provider);
	                String error = initiateOpenid(request, response, idLess);
	                if (error != null)
	                    throw new Oops(error);
            	}
            } 
        }
        // for local error-reporting, use a private Exception class, Oops (see below)
        catch(Oops e) {
            handleError(request, response, e.getMessage());
        }
    }

    private String checkCertificate(HttpServletRequest request) {
    	java.security.cert.X509Certificate[] certs =
    		(java.security.cert.X509Certificate[]) request.getAttribute(
    				"javax.servlet.request.X509Certificate");

    	if(null != certs){
    		if (certs[0] != null) {
    			String dn = certs[0].getSubjectX500Principal().getName();
    			try {
    				LdapName ldn = new LdapName(dn);
    				Iterator<Rdn> rdns = ldn.getRdns().iterator();
    				String org = null, cn = null;
    				while (rdns.hasNext()) {
    					Rdn rdn = (Rdn) rdns.next();
    					if (rdn.getType().equalsIgnoreCase("O"))
    						org = (String) rdn.getValue();
    					else if (rdn.getType().equalsIgnoreCase("CN"))
    						cn = (String) rdn.getValue();
    				}
    				if (cn != null){
    					return cn;
    				} else {
    					logger.error("Error authenticating the user: cn not found in certificate.");
                                        System.out.println("Error authenticating the user: cn not found in certificate.");
    					throw new PermissionDeniedException("401 Unauthorized");
    				}
    			} catch (javax.naming.InvalidNameException e) {
    			}
    		}
    	}
    	return null;
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
            if (null == verification.getVerifiedId() || !isBlank(verification.getStatusMsg()))
                throw new Oops("OpenID authentication failed. " + ((null != verification.getStatusMsg())?verification.getStatusMsg():""));
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

        // the user's OpenID
        String id = verification.getVerifiedId().getIdentifier();

        // Is the user known to us?
        String username = getUsername(id);
        System.out.println("User Name here :"+username);
//        if (!UserHelper.userExists(username)) {
//            UserHelper.addDefaultUser(username);
//        }

        // approve the user's request token
	        
        // OpenID attribute exchange -- retrieve certificate
        try {
            MessageExtension ext = verification.getAuthResponse().getExtension(AxMessage.OPENID_NS_AX);
            if (ext != null) {
                if (!(ext instanceof FetchResponse))
                    throw new Oops("Unexpected attribute exchange response: " + ext.getClass());
                FetchResponse fetch = (FetchResponse) ext;
                // store credential, if it was returned
                String certUrl = fetch.getAttributeValue(ALIAS_CERTIFICATE);
                if (certUrl == null)
                    throw new Oops("No certificate returned in Attribute Request.");
                logger.debug("For user \"" + username + "\" storing cert \"" + certUrl + "\".");
                //UserHelper.setCertificate(username, certUrl);
                System.out.println("User certificate here :"+username);
            }
        } catch (MessageException e) { // we don't expect this to happen
            logger.warn(e);
            throw new Oops("Unable to fetch OpenID Attributes: " + e.getMessage());
        }

        // TODO: handle case where access token is already present
        authorizeRequestToken(request, response, username);
    }

	/**
	 * @param request
	 * @param response
	 * @param callbackUrl
	 * @throws IOException
	 * @throws Oops
	 */
	private void authorizeRequestToken(HttpServletRequest request, HttpServletResponse response, String username)
			throws IOException, Oops {
        Cookie cookie = lookupCookie(request, OauthCookie.COOKIE_NAME);

        String token = null, callbackUrl = null;
        
        if (null != request.getParameter("oauth_token")) {
        	token = request.getParameter("oauth_token");
        	callbackUrl = request.getParameter("oauth_callback");
        } else if(null != cookie) {
	        OauthCookie parsed = OauthCookie.create(cookie);
	        if (isBlank(parsed.getRequestToken()))
	            throw new Oops("No request token present in oauth cookie (\"" + cookie.getValue() + "\").");
	        logger.debug("Parsed oauth cookie \"" + cookie.getValue() + "\" as \"" + parsed.toString() + "\".");
	        
	        token = parsed.getRequestToken();
	        callbackUrl = parsed.getCallbackUrl();
        } else {
        	throw new Oops("Not found OAuth request token.");
        }

		
		try {
            //MySQLOAuthProvider.markAsAuthorized(token, username);
            
                    System.out.println("MySQLOAuthProvider");
            if(null != callbackUrl && !callbackUrl.isEmpty()){
            	logger.debug("Redirecting user to "+callbackUrl);
            	response.sendRedirect(callbackUrl);
            } else {
                response.setContentType("text/plain");
                PrintWriter out = response.getWriter();
                out.println("You have successfully authorized " 
                        + ".\nPlease close this browser window and click continue"
                        + " in the client.");
                out.close();
            }
        } catch (Exception e) {
            logger.info("Exception authorizing request token for " + username, e);
            throw new Oops("Database error: " + e.getMessage());
        }
	}

    /** Look up the value of a named cookie. Null if not found. */
    private Cookie lookupCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies)
            if (cookie.getName().equals(name))
                return cookie;
        return null;
    }

    private String getUsername(String id) {
        int slash = id.lastIndexOf('/');
        return id.substring(slash + 1);
    }

    public static ConsumerManager getConsumerManager(HttpServletRequest request, boolean createIfAbsent) {
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

            // attribute request: get Certificate (could also get name)
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute(ALIAS_CERTIFICATE, AX_URL_CERTIFICATE, true);
            authRequest.addExtension(fetch);

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

    /** Private exception class for displaying error conditions to the user within this servlet. */
    private static class Oops extends Exception {
        Oops(String message) {
            super(message);
            if (message == null)
                throw new NullPointerException("Message is null.");
        }
    }

    private boolean isOpenIdResponse(HttpServletRequest request) {
        return !isBlank(request.getParameter("openid.ns"));
    }
}
