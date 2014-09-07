package org.usvao.sso.openid.portal;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.security.cert.X509Certificate;
import javax.security.cert.CertificateException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * a container for data acquired via a successful VAO-OpenID authentication.
 * <p>
 * This abstract class serves as an interface between a portal application 
 * and the underlying framework used to authenticate via the OpenID process.
 * An instance of this class is the result of a successful authentication,
 * providing information--particularly user attribute exchange data--about 
 * user.  It is expected that different OpenID frameworks would call for
 * different implementations of this class.  For example, the 
 * {@link org.usvao.sso.openid.portal.spring.SSFVAOPortal SSFVAOPortal} 
 * class assumes that the Spring Security Framework was used to manage the 
 * OpenID authentication.  
 * <p> 
 * The {@link #getAttribute()} and {@link #getAttributeValues()} provide 
 * generic access to user attributes returned via OpenID Attribute Exchange,
 * taking as input names either the full attribute URIs or the alias names
 * chosen by the portal at request time.  This class also provides explicit
 * access to the standard VAO attributes as defined by the 
 * {@link USVAOConventions} enum {@link SupportedAttributes} (e.g. 
 * {@link #getEmail()}, etc.).  For the default implementations to work,
 * implementations should make the attributes retrievable via the string 
 * representations of the enums as well.
 */
public abstract class VAOLogin {

    /**
     * the base URL to assume for OpenID URLs issued by the preferred 
     * VAO-compatible login service.  By default, this is set to 
     * VAO_OPENID_BASE_URL, but it can be updated.  
     */
    public static String SERVICE_OPENID_BASE_URL = 
        USVAOConventions.OPENID_BASE_URL;

    /**
     * the domain to associate with username from the login service 
     * associated with SERVICE_OPENID_BASE_URL.  By default, this is set to 
     * VAO_USERNAME_DOMAIN ("usvao") but this can be changed.  
     */
    public static String SERVICE_USERNAME_DOMAIN = 
        USVAOConventions.USERNAME_DOMAIN;

    /**
     * the key name to use to bind a VAOLogin instance to a servlet session
     */
    public final static String SESSION_ATTR_KEY = "VAO.Login";

    boolean success = true;
    protected String qname = null;
    protected String name = null;
    protected String oidurl = null;
    protected Date authtime = new Date();
    String authreason = null;

    /**
     * return the VAOLogin instance bound to a servlet session or null
     * if it hasn't been set yet.
     */
    public static VAOLogin fromSession(HttpSession sess) {
        try {
            return (VAOLogin) sess.getAttribute(SESSION_ATTR_KEY);
        } 
        catch (ClassCastException ex) {
            Log log = LogFactory.getLog(VAOLogin.class);
            if (log.isErrorEnabled()) {
                log.error("Wrong type (" + 
                    sess.getAttribute(SESSION_ATTR_KEY).getClass().toString() +
                          ") stored in servlet sesstion as " +
                          SESSION_ATTR_KEY);
            }
            return null;
        }
    }

    /**
     * return the VAOLogin instance bound to a servlet session or null
     * if it hasn't been set yet.
     */
    public static VAOLogin fromSession(HttpServletRequest servlet) {
        return fromSession(servlet.getSession());
    }

    protected VAOLogin(boolean succeeded, String openid, String name, 
                       String qualifiedName) 
    {
        success = succeeded;
        oidurl = openid;
        this.name = name;
        qname = qualifiedName;
    }

    /**
     * return a default username for a user with a given an OpenID URL or 
     * null if one cannot be determined.  
     * In this implementation, if the openid is recognized as being from 
     * sso.usvao.org, the username will be parsed from the URL.  
     */
    public static String defaultUserName(String openid) {
        if (openid.startsWith(SERVICE_OPENID_BASE_URL))
            return openid.substring(SERVICE_OPENID_BASE_URL.length());
        return null;
    }

    /**
     * return a default username for a user with a given an OpenID URL or 
     * null if one cannot be determined.  
     * In this implementation, if the openid is recognized as being from 
     * sso.usvao.org, the username will be parsed from the URL.  
     */
    protected String defaultUserName() {
        if (oidurl == null) return null;
        return VAOLogin.defaultUserName(oidurl);
    }

    /**
     * return a default qualified username for the user based on its OpenID
     * URL and username.  Null is returned if a default cannot be determined.  
     * In this implementation, if the openid is recognized as being from 
     * sso.usvao.org, the qualified name will be username+'@'+domain.  
     */
    protected String defaultQualifiedName() {
        if (getUserName() != null &&
            getOpenID().startsWith(SERVICE_OPENID_BASE_URL))
        {
            return getUserName() + '@' + VAOLogin.SERVICE_USERNAME_DOMAIN;
        }
        return null;
    }

    /**
     * return the unqualified name for the user.  This is the username that 
     * the user entered into the (VAO) login form.  
     * <p>
     * If null, this name was not explicitly returned by the VAO Login 
     * service (presumably because it was not requested).
     */
    public String getUserName() { 
        return (name == null) ? defaultUserName() : name; 
    }

    /**
     * return the qualified name for the user.  This is the username that 
     * the user entered into the (VAO) login form.  
     * <p>
     * A null value indicates that the authentication was not done by 
     * a recognized VAO-compatible login service.  
     */
    public String getQualifiedName() { 
        return (qname == null) ? defaultQualifiedName() : qname; 
    }

    /**
     * return the OpenID the user authenticated against.  This value will 
     * be the so-called OP-Local Identifier which takes the form of a URL.
     */
    public String getOpenID() { return oidurl; }

    /**
     * return true if the authentication was successful
     */
    public boolean isAuthenticated() { return success; }

    /**
     * do everything that is necessary to invalidate the authentication so 
     * that the use must reauthenticate at the next opportunity.  
     * <p> 
     * This implementation simply flips an internal flag, causing 
     * isAuthenticated() to return false.  Implementations should override
     * this to actually invalidate credentials.  Note that this need not 
     * erase all session information.  
     */
    public void deauthenticate() { 
        success = false; 
        setReason("ended by portal");
    }

    /**
     * return the reason behind the OpenID authentication status (i.e. 
     * whether the user is authenticated or not).  The values returned 
     * will depend on the underlying OpenID support implementation; however,
     * it generally corresponds to (when authenitcation fails)  the 
     * openid.mode response parameter.  
     * <p>
     * Implementations can override this to return a dynamic value.
     */
    public String getReason() {
        return authreason;
    }

    /**
     * set the nominal reason for the OpenID authentication status.
     */
    protected void setReason(String reason) {
        authreason = reason;
    }

    /**
     * return the values for a user attribute as returned from the VAO 
     * Login service (via OpenID Attribute Exchange).  Note that 
     * attributes supported by sso.usvao.org are typically single-valued;
     * this method is provided for VAO-compatible login service providers
     * that may provide additional attributes.  
     * @param attname   an identifier for the attribute.  This can be 
     *                    either the URI identifier or the alias name that 
     *                    the attribute was requested via.  
     */
    public abstract List<String> getAttributeValues(String attname);

    /**
     * return the keys that can be used to retrieve attributes.  Note 
     * that an attribute value can be retrieved through multiple keys,
     * including attribute URIs, aliases, and canonical names.  
     */
    public abstract Set<String> getAttributeKeys();

    /**
     * return a subset of attribute names that will return a uniqe set of 
     * attribute values.  These names will be the attribute aliases--i.e.
     * the names given by the portal at authentication time.
     */
    public Set<String> getAttributeAliases() {
        Set<String> keys = getAttributeKeys();
        HashSet<String> out = new HashSet<String>(keys.size() / 3);
        for(String key : keys) {
            if (! key.startsWith("http://") && ! key.startsWith("VAO.")) 
                out.add(key);
        }
        return out;
    }

    /**
     * return a subset of attribute names that will return a uniqe set of 
     * attribute values.  These names will be the attribute aliases--i.e.
     * the names given by the portal at authentication time.
     */
    public Set<String> getAttributeURIs() {
        Set<String> keys = getAttributeKeys();
        HashSet<String> out = new HashSet<String>(keys.size() / 3);
        for(String key : keys) {
            if (key.startsWith("http://"))
                out.add(key);
        }
        return out;
    }

    /**
     * return the first value returned for a user attributereturned from the 
     * VAO Login service (via OpenID Attribute Exchange).  Use this method
     * to retrieve an attribute value that is expected to be single-valued
     * (as with most attributes supported by sso.usvao.org).  
     * @param attname   an identifier for the attribute.  This can be 
     *                    either the URI identifier or the alias name that 
     *                    the attribute was requested via.  
     * @return String   the attribute value.  If the attribute is single-valued,
     *                    that value is returned.  If multiple values were 
     *                    returned by the login service, just the first one is
     *                    returned.  If a value was not returned (either 
     *                    because the attribute is not supported or access was 
     *                    denied by the user), null is returned.  
     */
    public String getAttribute(String attname) {
        List<String> out = getAttributeValues(attname);
        return (out == null || out.size() == 0) ? null : out.get(0);
    }

    /**
     * return the PEM-encoded certificate as a String.  Null is returned if 
     * a certificate id not available.
     * <p> 
     * This method will attempt to download the cert from login service, 
     * via {@link #cacheCertificate() cacheCertificate()}, if it hasn't 
     * been retrieved already.  Failures in downloading are silently 
     * tolerated and result in null being returned.  If you want to catch 
     * such errors, call {@link #cacheCertificate() cacheCertificate()} 
     * directly.  
     */
    public String getCertificatePEM() {
        String out = 
            getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        if (out != null && out.length() == 0)
            // a zero-length string indicates a failure to download on 
            // a previous attempt.  
            return null;

        // attempt to download
        try {
            cacheCertificate();
            out = getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        } 
        catch (IOException ex) { }
        catch (PortalSSOException ex) { }
        if (out != null && out.length() == 0)
            out = null;
        return out;
    }

    /**
     * Download and cache into memory the certificate representing the user.
     * It is recommended that this method be called soon after successful
     * authentication to retrieve the certificate before its availability
     * times out.
     * <p>
     * If the download is successful, <code>true</code> is returned; if a
     * certificate is not available because it wasn't requested (i.e. 
     * {@link #getCertificateURL() getCertificateURL()} returns null), 
     * <code>false</code> is returned.  If a cert had been properly requested 
     * and should be available but a failure occurs, an exception is raised.
     * If the download is successful, the PEM-encoded cert will be stored as 
     * an attribute; it will be subsequently available via 
     * {@link #getCertificatePEM() getCertificatePEM()} and 
     * {@link #getCertificate() getCertificate()}.
     * @return boolean  true if the download was successful, false if the 
     *                  the cert is not available because it was not requested
     *                  at authentication time.  
     * @throws AvailabilityExpiredException  if too much time has elapsed 
     *                  since authentication and the cert cached on the 
     *                  server is no longer available.  
     * @throws IOException  if a protocol error occurs during download or the
     *                  the resulting cert is corrupt.  
     */
    public boolean cacheCertificate() 
        throws IOException, AvailabilityExpiredException
    {
        String url = getCertificateURL();
        if (url == null) return false;

        String pem = "";
        try {
            URL certurl = new URL(url);
            pem = retrieveCert(new URL(url));

            // make sure the cert is decodable
            try {
                X509Certificate.getInstance(pem.getBytes());
            } catch (CertificateException ex) {
                throw new IOException("Undecodable PEM: "+ex.getMessage(), ex);
            }
        }
        finally {
            // if an exception was thrown an empty string will be saved
            // as the PEM-encoded cert; this is a signal to indicate a 
            // download was attempted but failed 
            setCertificatePEM(pem);
        }

        return true;
    }

    protected String retrieveCert(URL url) 
        throws IOException, AvailabilityExpiredException
    {
        return retrieveEncodedCert(url, true);
    }

    /**
     * save the PEM-encoded cert so that it can be retrieved later.
     */
    protected abstract void setCertificatePEM(String pem);

    static int retrieveRedirectLimit = 5;

    /**
     * retrieve the PEM-encoded certificate from a URL.  Retrieval 
     * will follow (a limited number of) redirects.  
     * @param certurl    the URL pointing to the cert
     * @param safe       if true, require that the URL use https; this 
     *                     applies to all redirect URLs as well.
     */
    public static String retrieveEncodedCert(URL certurl, boolean safe) 
        throws IOException, AvailabilityExpiredException
    {
        URLConnection conn = null;

        String scheme = certurl.getProtocol().toLowerCase();
        if ("http".equals(scheme) || "https".equals(scheme)) 
            conn = httpConnectForCert(certurl, safe);
        else if (safe)
            throw new IOException("Insecure transfer protocol: " + 
                                  certurl.toString());
        else 
            conn = certurl.openConnection();

        InputStreamReader rdr = new InputStreamReader(conn.getInputStream());

        char[] buf = new char[3000];
        StringBuilder sb = new StringBuilder();
        int nread = 0;
        while (nread >= 0) {
            nread = rdr.read(buf);
            if (nread > 0) sb.append(buf, 0, nread);
        }
        if (sb.indexOf("-BEGIN CERTIFICATE-") < 0)
            throw new IOException("Certificate URL did not return a cert " +
                                  "in PEM format");
        return sb.toString();        
    }

    static HttpURLConnection httpConnectForCert(URL certurl, boolean safe) 
        throws IOException, AvailabilityExpiredException
    {
        int status = 0;
        HttpURLConnection conn = null;

        // We will follow our own redirects for security's sake
        int redirectCount = 0;
        while (redirectCount < retrieveRedirectLimit) {

            if (safe && certurl.getProtocol().toLowerCase() != "https")
                throw new IOException("Insecure transfer protocol: " + 
                                      certurl.toString());

            conn = (HttpURLConnection) certurl.openConnection(); 
            conn.setInstanceFollowRedirects(false);
            conn.connect();

            status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                // redirection requested
                String url = conn.getHeaderField("Location");
                if (url == null) 
                  throw new IOException("Protocol error: missing redirect URL");
                certurl = new URL(url);
                redirectCount++;
            }
            else {
                break;
            }
        } 
        if (status >= 300 && status < 400) 
            throw new IOException("Redirection count limit exceded");
        if (status > 200 && status < 300)
            throw new IOException("Unexpected Success code: " + 
                                  Integer.toString(status) + " (" +
                                  conn.getResponseMessage() + ")");
        if (status == 404 || status == 410)
            throw new AvailabilityExpiredException("Certificate availability has expired",
                                                   conn.getURL());
        if (status != 200) 
            throw new HTTPErrorResponseException(status, 
                                                 conn.getResponseMessage());

        return conn;
    }

    /** 
     * return an X.509 certificate representing the user or null if one 
     * is not available.  
     * <p>
     * This method will attempt to download the cert from login service, 
     * via {@link #cacheCertificate() cacheCertificate()}, if it hasn't 
     * been retrieved already.  Failures in downloading are silently 
     * tolerated and result in null being returned.  If you want to catch 
     * such errors, call {@link #cacheCertificate() cacheCertificate()} 
     * directly.  
     */
    public X509Certificate getCertificate() {
        String pem = getCertificatePEM();
        if (pem == null || pem.length() == 0) return null;

        try {
            return X509Certificate.getInstance(pem.getBytes());
        } 
        catch (CertificateException ex) {
            return null;
        }
    }

    /**
     * remove the retrieved certificate from memory.  Call this method
     * if you have already consumed the certificate (perhaps storing it 
     * in a more secure way) and you would prefer not to have it remain 
     * in memory for security reasons.  This will also forget the certificate
     * retrieval URL.
     */
    public abstract void forgetCertificate();

    /**
     * return the full name of the user or null if it is not known
     */
    public String getFullName() {
        return getAttribute(USVAOConventions.SupportedAttribute.NAME.toString());
    }

    /**
     * return the last name (i.e. family name) of the user or null if 
     * not known.
     */
    public String getLastName() {
        return getAttribute(USVAOConventions.SupportedAttribute.LASTNAME.toString());
    }

    /**
     * return the first name and any additional middle names or iniials 
     * of the user or null if not known.
     */
    public String getFirstName() {
        return getAttribute(USVAOConventions.SupportedAttribute.FIRSTNAME.toString());
    }

    /**
     * return the email address of the user or null if not known.
     */
    public String getEmail() {
        return getAttribute(USVAOConventions.SupportedAttribute.EMAIL.toString());
    }

    /**
     * return the telephone number of the user or null if not known.
     */
    public String getPhone() {
        return getAttribute(USVAOConventions.SupportedAttribute.PHONE.toString());
    }

    /**
     * return the user's home professional institution or null if not known.
     */
    public String getInstitution() {
        return getAttribute(USVAOConventions.SupportedAttribute.INSTITUTION.toString());
    }

    /**
     * return the user's country of residence or null if not known.
     */
    public String getCountry() {
        return getAttribute(USVAOConventions.SupportedAttribute.COUNTRY.toString());
    }

    /**
     * return the URL to the user certificate or null if not available
     */
    public String getCertificateURL() {
        return getAttribute(USVAOConventions.SupportedAttribute.CREDENTIAL.toString());
    }

    /**
     * return the timestamp when authentication was completed.
     */
    public Date getAuthenticationTime() {  return authtime;  }
}