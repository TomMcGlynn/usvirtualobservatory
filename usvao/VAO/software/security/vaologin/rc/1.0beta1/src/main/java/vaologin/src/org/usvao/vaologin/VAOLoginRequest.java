/*
 * Based On SampleConsumer.java by
 * Copyright 2006-2008 Sxip Identity Corporation
 */

package org.usvao.vaologin;

import org.openid4java.discovery.Identifier;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.*;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.*;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.net.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * USVAO Consumer (Relying Party) implementation.
 */
public class VAOLoginRequest
{
    public ConsumerManager manager;
    private String userSupplied;
    private String returnToUrl;
    private DiscoveryInformation discovered;

    private static final String USVAO_SSO_URL = 
                "https://testsso.ncsa.illinois.edu/openid/provider_id";

    public static final String USERNAME    = "username";
    public static final String NAME        = "name";
    public static final String EMAIL       = "email";
    public static final String PHONE       = "phone";
    public static final String CREDENTIAL  = "credential";
    public static final String INSTITUTION = "institution";
    public static final String COUNTRY     = "country";

    private static final Map<String, String> ATTRIBUTE_URI = 
    Collections.unmodifiableMap(new HashMap<String, String>() {{ 
        put(USERNAME,    "http://axschema.org/namePerson/friendly");
        put(NAME,        "http://axschema.org/namePerson");
        put(EMAIL,       "http://axschema.org/contact/email");
        put(PHONE,       "http://axschema.org/contact/phone");
        put(CREDENTIAL,  "http://sso.usvao.org/schema/credential/x509");
        put(INSTITUTION, "http://sso.usvao.org/schema/institution");
        put(COUNTRY,     "http://sso.usvao.org/schema/country");
    }});

    private Map<String, List> reqRequiredAttrs;
    private Map<String, List> reqRequestedAttrs;

    public VAOLoginRequest() throws ConsumerException
    {
        // instantiate a ConsumerManager object
        manager = new ConsumerManager();
    }

    // --- placing the authentication request ---
    public String getAuthRedirectURL(String userSuppliedOpenID,
                      String returnUrl, String[] requiredAttributes,
                      String[] requestedAttributes)
            throws Exception
    {
        try
        {
            if (userSuppliedOpenID == null)
                throw new Exception("userSuppliedOpenID is null");
            else
                userSupplied = userSuppliedOpenID;

            // configure the return_to URL where your application will receive
            // the authentication responses from the OpenID provider
            if (returnUrl == null)
                throw new Exception("returnUrl is null");
            else
                returnToUrl = returnUrl;

            // --- Forward proxy setup (only if needed) ---
            // ProxyProperties proxyProps = new ProxyProperties();
            // proxyProps.setProxyName("proxy.example.com");
            // proxyProps.setProxyPort(8080);
            // HttpClientFactory.setProxyProperties(proxyProps);

            // Attribute Exchange example: process attributes
            FetchRequest fetch = FetchRequest.createFetchRequest();
            reqRequiredAttrs = new HashMap<String, List>();
            reqRequestedAttrs = new HashMap<String, List>();

            // Required attributes
            for (String attr: requiredAttributes)
            {
                String URI = ATTRIBUTE_URI.get(attr);

                if (URI == null) {
                    throw new Exception(attr + " is not a supported attribute");
                }
                fetch.addAttribute(attr, // attribute alias
                    URI,   // type URI
                    true); // required
                reqRequiredAttrs.put(attr, null);
            }

            // Requested attributes
            for (String attr: requestedAttributes)
            {
                String URI = ATTRIBUTE_URI.get(attr);

                if (URI == null) {
                    throw new Exception(attr + " is not a supported attribute");
                }
                fetch.addAttribute(attr, // attribute alias
                    URI,   // type URI
                    false); // not required
                reqRequestedAttrs.put(attr, null);
            }

            // perform discovery on the user-supplied identifier
            List discoveries = manager.discover(userSupplied);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            discovered = manager.associate(discoveries);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);

            // attach Attribute Exchange extension to the authentication request
            authReq.addExtension(fetch);

            // TODO: HTTP GET Vs. FORM POST
            return authReq.getDestinationUrl(true);
/*
            if (! discovered.isVersion2() )
            {
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                httpResp.sendRedirect(authReq.getDestinationUrl(true));
                return null;
            }
            else
            {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)

                //RequestDispatcher dispatcher =
                //        getServletContext().getRequestDispatcher("formredirection.jsp");
                //httpReq.setAttribute("parameterMap", response.getParameterMap());
                //httpReq.setAttribute("destinationUrl", response.getDestinationUrl(false));
                //dispatcher.forward(request, response);
            }
*/
        }
        catch (OpenIDException e)
        {
            // present error to the user
            throw new Exception("getAuthRedirectURL has run into an exception",
                                e);
        }
    }

    public void requestAuth(HttpServletResponse httpResp,
            String userSuppliedOpenID, String returnUrl,
            String[] requiredAttributes, String[] requestedAttributes)
            throws Exception
    {
        String redirectUrl = getAuthRedirectURL(userSuppliedOpenID,
                             returnUrl, requiredAttributes,
                                requestedAttributes);

        httpResp.sendRedirect(redirectUrl);
    }

    public void requestVAOAuth(HttpServletResponse httpResp, String returnUrl,
                    String[] requiredAttributes, String[] requestedAttributes)
            throws Exception
    {
        requestAuth(httpResp, USVAO_SSO_URL, returnUrl,
                              requiredAttributes, requestedAttributes);
    }

    // --- processing the authentication response ---
    private void retrieveAttrs(FetchResponse fetchResp,
            Map<String, List> retAttributes, Map<String, List> reqAttributes)
            throws IOException, MalformedURLException
    {
        for (Map.Entry<String, List> entry : reqAttributes.entrySet())
        {
            List values = fetchResp.getAttributeValues(entry.getKey());

            if (values != null)
                //  Get certfile contents for certificate attrib
                if (entry.getKey().equals(CREDENTIAL))
                {
                      // Retrieve the file from the server
                      URL credURL = new URL((String)values.get(0));

                      byte[] credential = new byte[4096];
                      // TODO: Make this dynamic so we can handle
                      // more than 4096
                      InputStream in = credURL.openStream();
                      in.read(credential, 0, 2048);
                      in.close();
                      List<String> list = new ArrayList<String> ();
                      list.add(new String(credential));
                      entry.setValue(list);
                      retAttributes.put(entry.getKey(), list);
                }
                else
                {
                      entry.setValue(values);
                      retAttributes.put(entry.getKey(), values);
                }
        }
    }

    public String verifyResponse(HttpServletRequest httpReq,
           Map<String, List> requiredAttributes, Map<String,
           List> requestedAttributes) throws Exception
    {
        try
        {
            if (manager == null || discovered == null)
            {
                throw new Exception("No preexisting VAOLoginRequest info");
            }

            if (httpReq == null)
            {
                throw new Exception("httpReq is null");
            }

            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());
            if (response == null)
            {
                throw new Exception("Invalid HTTP Request; no parameters found");
            }

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = httpReq.getRequestURL();
            if (receivingURL == null)
            {
                throw new Exception("Invalid HTTP Request; no receivingURL found");
            }

            String queryString = httpReq.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(queryString);
            else
            {
                throw new Exception("Invalid HTTP Request; no queryString found");
            }

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = manager.verify(
                    receivingURL.toString().replaceFirst("///", "//"),
                    response, discovered);

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
                {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    if (fetchResp != null &&
                        requiredAttributes != null && reqRequiredAttrs != null)
                        retrieveAttrs(fetchResp, requiredAttributes, reqRequiredAttrs);

                    if (requestedAttributes != null && reqRequestedAttrs != null)
                        retrieveAttrs(fetchResp, requestedAttributes, reqRequestedAttrs);
                }

                return verified.toString();  // success
            }
            else
                return null; // Failure
        }
        catch (Exception e)
        {
            throw new Exception("Exception in VerifyResponse", e);
        }
    }
}
