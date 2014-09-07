package org.usvo.openid.serve;

import org.usvo.openid.orm.NvoUser;
import org.usvo.openid.orm.Portal;

import org.openid4java.message.ax.FetchRequest;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.net.URL;

/**
 * a class that resolves attribute URI identifiers into values for a
 * given user and consuming portal.
 */
public class UserAttributes {

    public static enum LocalType {  
        UNSUPPORTED, USERNAME, NAME, FIRSTNAME, LASTNAME, 
        EMAIL, INSTITUTION, PHONE, COUNTRY, CREDENTIAL 
    }

    private NvoUser user = null;
    private Portal portal = null;
    private AxURIInterpreter trx = UserAttributes.VAO;

    /**
     * create access to the attributes for a given user and portal.  
     * Providing a portal gives access to portal-specific attributes.
     * @param user    the user whose attributes we desire
     * @param portal  the portal what wants the attributes. If null,
     *                   portal-specific attributes will not be available.
     * @param interpreter  the AxURIInterpreter to use.  If null, the 
     *                standard VAO one will be used.  
     */
    public UserAttributes(NvoUser user, Portal portal, 
                          AxURIInterpreter interpreter)
    {
        this.user = user;
        this.portal = portal;
        if (interpreter != null) trx = interpreter;
    }

    /**
     * create access to the attributes for a given user and portal.  
     * Providing a portal gives access to portal-specific attributes.
     * @param user    the user whose attributes we desire
     * @param portal  the portal what wants the attributes. If null,
     *                   portal-specific attributes will not be available.
     */
    public UserAttributes(NvoUser user, Portal portal) {
        this(user, portal, null);
    }

    /**
     * create access to the attributes for a given user.  
     * Portal-specific attributes will not be available.
     * @param user    the user whose attributes we desire
     */
    public UserAttributes(NvoUser user) {
        this(user, null);
    }

    /**
     * return a value for the given attribute id
     */
    public String get(String uri) {
        return get(trx.interpret(uri));
    }

    /**
     * return a value for the given attribute type
     */
    String get(LocalType type) {
        switch (type) {
        case USERNAME:
            return user.getUserName();
        case NAME:
            return user.getName();
        case FIRSTNAME:
            return user.getFirstName();
        case LASTNAME:
            return user.getLastName();
        case EMAIL:
            return user.getEmail();
        case INSTITUTION:
            return user.getInstitution();
        case PHONE:
            return user.getPhone();
        case COUNTRY:
            return user.getCountry();
        case CREDENTIAL:
            // return a blank value; we'll switch it out later
            return "";
        }

        return null;
    }

    /**
     * return an Attribute for a given type
     */
    public Attribute makeAttribute(String uri, String alias) {
        LocalType type = trx.interpret(uri);
        Attribute out = new Attribute(type, uri, alias);
        out.setDescription(descriptions.get(type));
        out.setParamName(shareparams.get(type));
        out.addValue(get(type));
        return out;
    }

    /**
     * return a set of Attributes loaded from those requested in 
     * a given FetchRequest.  
     */
    public Attributes getRequestedAtts(FetchRequest freq) {
        Attributes out = new Attributes();
        Attribute att = null;
        Map<String, String> atts = freq.getAttributes(true);
        for (Map.Entry<String, String> req : atts.entrySet()) {
            att = makeAttribute(req.getValue(), req.getKey());
            att.setRequired(true);
            out.add(att);
        }
        atts = freq.getAttributes(false);
        for (Map.Entry<String, String> req : atts.entrySet()) {
            out.add(makeAttribute(req.getValue(), req.getKey()));
        }

        return out;
    }
        
    /**
     * a class that maps OpenID attribute URIs to local attributes
     */
    public static class AxURIInterpreter {
        private Map<String, UserAttributes.LocalType> map = 
            new HashMap<String, UserAttributes.LocalType>(6);

        public AxURIInterpreter() { }

        /**
         * add support for an attribute URI
         * @param uri    the attirbutes identifying URI
         * @param local  the local attribute type to interpret the URI as
         */
        public void addSupport(String uri, UserAttributes.LocalType local) {
            map.put(uri, local);
        }

        /**
         * interpret a given URI.  If the URI is not recognized, 
         * UserAttributes.UNSUPPORTED is returned.
         */
        public UserAttributes.LocalType interpret(String uri) {
            UserAttributes.LocalType out = map.get(uri);
            if (out == null) out = UserAttributes.LocalType.UNSUPPORTED;
            return out;
        }

    }

    /**
     * the standard VAO interpretation of OpenID attribute URIs.  The
     * supported set draws on both the http;//axschema.org and 
     * http://openid.net sets as well as some local URIs.
     */
    public static AxURIInterpreter VAO = new AxURIInterpreter();
    static {
        VAO.addSupport("http://openid.net/namePerson/friendly", LocalType.USERNAME);
        VAO.addSupport("http://sso.usvao.org/schema/username", LocalType.USERNAME);
        VAO.addSupport("http://schema.openid.net/namePerson/friendly", LocalType.USERNAME);
        VAO.addSupport("http://axschema.org/namePerson/friendly", LocalType.USERNAME);
        VAO.addSupport("http://sso.usvao.org/schema/name", LocalType.NAME);
        VAO.addSupport("http://axschema.org/namePerson", LocalType.NAME);
        VAO.addSupport("http://schema.openid.net/namePerson", LocalType.NAME);
        VAO.addSupport("http://openid.net/namePerson", LocalType.NAME);
        VAO.addSupport("http://sso.usvao.org/schema/namePerson", LocalType.NAME);
        VAO.addSupport("http://sso.usvao.org/schema/namePerson/first", LocalType.FIRSTNAME);
        VAO.addSupport("http://sso.usvao.org/schema/namePerson/last", LocalType.LASTNAME);
        VAO.addSupport("http://sso.usvao.org/schema/email", LocalType.EMAIL);
        VAO.addSupport("http://axschema.org/contact/email", LocalType.EMAIL);
        VAO.addSupport("http://schema.openid.net/contact/email", LocalType.EMAIL);
        VAO.addSupport("http://openid.net/schema/contact/email", LocalType.EMAIL);
        VAO.addSupport("http://sso.usvao.org/schema/phone", LocalType.PHONE);
        VAO.addSupport("http://axschema.org/contact/phone/default", LocalType.PHONE);
        VAO.addSupport("http://axschema.org/contact/phone/business", LocalType.PHONE);
        VAO.addSupport("http://sso.usvao.org/schema/institution", LocalType.INSTITUTION);
        VAO.addSupport("http://sso.usvao.org/schema/country", LocalType.COUNTRY);
        VAO.addSupport("http://sso.usvao.org/schema/credential", LocalType.CREDENTIAL);
        VAO.addSupport("http://sso.usvao.org/schema/credential/x509", LocalType.CREDENTIAL);
    }

    static Map<LocalType, String> descriptions = 
        new TreeMap<LocalType, String>();
    static {
        descriptions.put(LocalType.UNSUPPORTED, "unrecognized attribute");
        descriptions.put(LocalType.USERNAME, "VAO login ID (username)");
        descriptions.put(LocalType.NAME, "full name");
        descriptions.put(LocalType.LASTNAME, "last (family) name");
        descriptions.put(LocalType.FIRSTNAME, "first names or initials");
        descriptions.put(LocalType.EMAIL, "email address");
        descriptions.put(LocalType.INSTITUTION, "institution (employer or other organization of affiliation)");
        descriptions.put(LocalType.PHONE, "telephone number");
        descriptions.put(LocalType.COUNTRY, "home country");
        descriptions.put(LocalType.CREDENTIAL, "temporary (X.509) security credentials");
    }

    static Map<LocalType, String> shareparams = 
        new TreeMap<LocalType, String>();
    static {
        shareparams.put(LocalType.UNSUPPORTED, "share_unsupported");
        shareparams.put(LocalType.USERNAME, "share_username");
        shareparams.put(LocalType.NAME, "share_name");
        shareparams.put(LocalType.LASTNAME, "share_lname");
        shareparams.put(LocalType.FIRSTNAME, "share_fname");
        shareparams.put(LocalType.EMAIL, "share_email");
        shareparams.put(LocalType.INSTITUTION, "share_institution");
        shareparams.put(LocalType.PHONE, "share_phone");
        shareparams.put(LocalType.COUNTRY, "share_country");
        shareparams.put(LocalType.CREDENTIAL, "share_credential");
    }
}

