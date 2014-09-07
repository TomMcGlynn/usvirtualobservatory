package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.VAOLogin;
import org.usvao.sso.openid.portal.USVAOConventions;

import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import javax.security.cert.X509Certificate;

/**
 * an VAOLogin implementation using Spring Security infrastructure.
 */
public class SSFOpenIDLogin extends VAOLogin {

    final static String LOCAL_NAME_KEY = "VAO.localname";

    protected OpenIDAuthenticationToken authen = null;
    protected HashMap<String, List<String> > attributes = 
        new HashMap<String, List<String> >();

    public SSFOpenIDLogin(OpenIDAuthenticationToken authen) {
        this(authen, authen.getStatus() == OpenIDAuthenticationStatus.SUCCESS);
    }

    public SSFOpenIDLogin(OpenIDAuthenticationToken authen, boolean success) {
        this(authen, null, null, success);
    }

    public SSFOpenIDLogin(OpenIDAuthenticationToken authen, String username,
                          String qualifiedName, boolean success) 
    {
        super(success, authen.getIdentityUrl(), username, qualifiedName);
        this.authen = authen;
        loadAttributes();

        if (username == null) {
            username = getAttribute(USVAOConventions.SupportedAttribute.USERNAME.toString());
            if (username != null) name = username;
        }
        if (qname == null) 
            qname = defaultQualifiedName();
    }

    void setLocalName(String name) {
        ArrayList<String> val = new ArrayList<String>(1);
        val.add(name);
        attributes.put(LOCAL_NAME_KEY, val);
    }

    static String getLocalName(VAOLogin login) {
        return login.getAttribute(LOCAL_NAME_KEY);
    }

    /**
     * load the attributes stored in the Authentication token into the
     * values lookup map.  The values will be stored under multiple keys:
     * the attribute alias and attribute URI (as they were requested) and,
     * where applicable, via the USVAOConventions.SupportedAttribute name. 
     */
    protected void loadAttributes() {
        List<OpenIDAttribute> atts = authen.getAttributes();
        for (OpenIDAttribute attrib : atts) {
            attributes.put(attrib.getName(), attrib.getValues());
            attributes.put(attrib.getType(), attrib.getValues());

            USVAOConventions.SupportedAttribute supported = 
                USVAOConventions.identifyAttributeURI(attrib.getType());
            if (supported != null) 
                attributes.put(supported.toString(), attrib.getValues());
        }
    }

    /**
     * return the reason behind the OpenID authentication status (i.e. 
     * whether the user is authenticated or not).  The values returned 
     * will depend on the underlying OpenID support implementation; however,
     * it generally corresponds to (when authenitcation fails)  the 
     * openid.mode response parameter.  
     */
    public String getReason() {
        String out = super.getReason();
        if (out == null) out = authen.getStatus().toString();
        return out;
    }

    /**
     * do everything that is necessary to invalidate the authentication so 
     * that the use must reauthenticate at the next opportunity.  
     * <p> 
     * This implementation invalidates the authentication token.
     */
    public void deauthenticate() { 
        authen.setAuthenticated(false);
        super.deauthenticate();
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
     * @return List<String>  the list of values or null if the attribute
     *                         was not requested or otherwise not returned 
     *                         by the login service.  
     */
    public List<String> getAttributeValues(String attname) {
        return attributes.get(attname);
    }

    /**
     * return the keys that can be used to retrieve attributes.  Note 
     * that an attribute value can be retrieved through multiple keys,
     * including attribute URIs, aliases, and canonical names.  
     */
    public Set<String> getAttributeKeys() {
        return new HashSet<String>(attributes.keySet());
    }

    protected void setCertificatePEM(String pem) {
        ArrayList<String> list = new ArrayList<String>(1);
        list.add(pem);
        attributes.put(USVAOConventions.SupportedAttribute.CERTPEM.toString(),
                       list);
    }

    /**
     * remove the retrieved certificate from memory.  Call this method
     * if you have already consumed the certificate (perhaps storing it 
     * in a more secure way) and you would prefer not to have it remain 
     * in memory for security reasons.  This will also forget the certificate
     * retrieval URL.
     */
    public void forgetCertificate() {
        attributes.remove(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        attributes.remove(USVAOConventions.SupportedAttribute.CREDENTIAL.toString());
        List<String> uris = USVAOConventions.attributeURIsFor(USVAOConventions.SupportedAttribute.CREDENTIAL);
        for (String uri : uris) {
            attributes.remove(uri);
        }
    }
}