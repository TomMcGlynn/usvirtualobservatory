package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.VAOLogin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * a utility base class for processing spring security authentication in 
 * a VAO context.  This is not intended to for direct use in a spring 
 * security configuration but rather for developing up bean classes that 
 * can be.  
 * <p>
 * This base provides support two bean properties:
 * <dl>
 *   <dt> vaoDomains </dt>
 *   <dd> sets the base OpenID URLs to associate with recognized 
 *        VAO-compatible login service domains.  </dd>
 * 
 *   <dt> useAsLocalUserName </dt>
 *   <dd> sets which form of the user's identifier should be used as 
 *        the username within the portal</dd>
 * </dl>
 * 
 * This class can be used either as a base class or as an internal delegate 
 * to another class.
 */
public class VAOAuthenBeanBase {

    enum UseAsUsername { OPENID, USERNAME, QNAME }

    UseAsUsername useAsUsername = UseAsUsername.OPENID;
    protected HashMap<String, String> vaodomains = 
        new HashMap<String, String>();
    protected Log log = null;

    public VAOAuthenBeanBase() {
        this(null);
    }

    public VAOAuthenBeanBase(Log logger) {
        if (logger == null) logger = LogFactory.getLog(getClass());
        log = logger;

        vaodomains.put(VAOLogin.SERVICE_OPENID_BASE_URL, 
                       VAOLogin.SERVICE_USERNAME_DOMAIN);
    }

    /**
     * set the recognized VAO-compatible service domains.  Provided to 
     * enable Spring Security configuration.  The input should be a
     * space-separated list of domain definitions.  Each definition has 
     * form, <i>domain</i>:<i>base_url</i>, where <i>domain</i> is a short
     * name for the domain (e.g. "usvao") and <i>base_url</i> is the base URL
     * that starts all OpenID (OP-Local) identifier URLs issued by the 
     * VAO-compatible login service.  The part of the identifier after the 
     * base is considered the username.  (Thus the base URL should end with 
     * a slash).  
     */
    public void setVaoDomains(String domains) {
        vaodomains.clear();
        String[] domainmap = domains.split("\\s+");
        String[] pair = null;
        for (String map : domainmap) {
            if (map.equals("VAO")) 
                map = VAOLogin.SERVICE_USERNAME_DOMAIN + ":VAO";

            pair = map.split("\\s*:\\s*", 2);
            if (pair.length < 2) continue;
            if (pair[1].equals("VAO")) 
                pair[1] = VAOLogin.SERVICE_OPENID_BASE_URL;

            vaodomains.put(pair[1], pair[0]);
        }
    }

    /**
     * form a qualified name if the given OpendID URL is recognized as from a
     * VAO-compatible login service.
     */
    public QName getQualifiedName(String openid) {
        QName out = null;
        for(String server : vaodomains.keySet()) {
            if (openid.startsWith(server)) {
                out = new QName(openid.substring(server.length()),
                                vaodomains.get(server));
                break;
            }
        }
        return out;
    }

    /**
     * a container for a qualified name
     */
    public static class QName {
        public String name = null;
        public String domain = null;
        public QName() { this(null, null); }
        public QName(String name, String domain) {
            this.name = name;
            this.domain = domain;
        }
        public String toString() { return name + '@' + domain; }
    }

    /**
     * Configure which form of the user's identity string should be used 
     * as the local user used to retrieve information about the user from 
     * the local user database.
     * @param userNameForm  either "OPENID", "USERNAME", or "QNAME" (value 
     *                        is case-insensitive).  The values are interpreted
     *                        as follows:
     *                        <dl>
     *                          <dt>OPENID</dt>
     *                          <dd>the OpenID Identity URL</dd>
     *                          <dt>USERNAME</dt>
     *                          <dd>the username that the user entered into 
     *                              the (VAO) login page.  For non-VO 
     *                              compatible services, this will not be known
     *                              or not applicable and defaults to the 
     *                              OpenID identity URL.  </dd>
     *                          <dt>QNAME</dt>
     *                          <dd>the qualified username which will have the 
     *                              form, 
     *                              <i>username</i><code>@</code><i>domain</i>.
     *                              This username helps distinguish which 
     *                              VO-compatible login service the login was 
     *                              handled by.  This value is recommended.
     *                              If the login was handled by a 
     *                              non-VO-compatible OpenID services, this 
     *                              value will default to the OpenID identity 
     *                              URL.  </dd>
     *                        </dl>
     *                        QNAME is the recommended value but it defaults 
     *                        OPENID if it is not set.  
     */
    public void setUseAsLocalUserName(String userNameForm) {
        if (userNameForm == null) 
            throw new NullPointerException("null value for localUserName "+
                                           "property not allowed");
        userNameForm = userNameForm.toUpperCase();
        if (userNameForm.equals(UseAsUsername.QNAME.toString()))
            useAsUsername = UseAsUsername.QNAME;
        else if (userNameForm.equals(UseAsUsername.USERNAME.toString()))
            useAsUsername = UseAsUsername.USERNAME;
        else if (userNameForm.equals(UseAsUsername.OPENID.toString()))
            useAsUsername = UseAsUsername.OPENID;
        else 
            throw new IllegalArgumentException("unsupported value givenn " +
                                               "localUserName property");
    }

    public String getUseAsLocalUserName() {
        return useAsUsername.toString();
    }

    /**
     * return the username that has been configured to be used as the local
     * username for retrieving user information from the user database.  
     * This particular method will pull the appropriate user identifier 
     * from the given VAOLogin instance based on the value of the 
     * useAsLocalUserName property.
     */
    public String selectLocalUserName(VAOLogin loginInfo) {
        String username = null;
        switch (useAsUsername) {
        case QNAME:
            username = loginInfo.getQualifiedName();
            if (username != null) break;
        case USERNAME:
            username = loginInfo.getUserName();
            if (username != null) break;
        default:
            username = loginInfo.getOpenID();
        }
        return username;
    }

    /**
     * return the username that has been configured to be used as the local
     * username for retrieving user information from the user database.  
     * This particular method will construct a user name from the given 
     * OpenID URL based on the value of the useAsLocalUserName property, 
     * assuming it recognizes the URL as coming from a VAO-compatible 
     * service (as configured via the vaoDomains property).  If it is not 
     * so recognized, the input identifier is simply returned.
     */
    public String selectLocalUserName(String openid) {
        if (useAsUsername == UseAsUsername.OPENID) 
            return openid;

        QName qname = getQualifiedName(openid);
        if (qname == null) return openid;

        if (useAsUsername == UseAsUsername.QNAME) 
            return qname.toString();

        return qname.name;
    }

}