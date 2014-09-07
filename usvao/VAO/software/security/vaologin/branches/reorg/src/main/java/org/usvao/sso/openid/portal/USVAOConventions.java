package org.usvao.sso.openid.portal;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * a class of constant and static data representing conventions of the 
 * Login Services at sso.usvao.org.
 */
public class USVAOConventions {

    /**
     * the base URL that begins all OpenID URLs issued by the 
     * US VAO login service.  
     */
    public final static String OPENID_BASE_URL = 
        "https://sso.usvao.org/openid/id/";

    /**
     * the recommended domain name to associate with usernames from the US
     * VAO service.  By convention, qualified usernames will be the user's 
     * username (i.e. what they enter into as a username into the login 
     * page) plus '@' plus this value (e.g. "user@usvao").   
     */
    public final static String USERNAME_DOMAIN = "usvao";

    /**
     * an enumeration of the attributes that are available from the US VAO
     * login service.  
     */
    public enum SupportedAttribute {
        /**
         * The username that the user enters into the log into the login page
         */
        USERNAME,

        /**
         * The full name of the user
         */
        NAME,

        /**
         * The last or family name of the user 
         */
        LASTNAME,

        /**
         * The user's first name plus any addition middle names or initials 
         */
        FIRSTNAME,

        /**
         * The user's email adress
         */
        EMAIL,

        /**
         * The user's phone number
         */
        PHONE,

        /**
         * The user's professional home institution (e.g. university, 
         * observatory or lab). 
         */
        INSTITUTION,

        /**
         * The user's current country of residence
         */
        COUNTRY,

        /** 
         * A temporary URL that points to a retrievable X.509 certificate 
         * that represents the user's identity
         */
        CREDENTIAL,

        /** 
         * The PEM-encoded text of an X.509 certificate 
         * that represents the user's identity
         */
        CERTPEM;

        public String toString() { return "VAO."+super.toString(); }

    }

    static HashMap<String, SupportedAttribute> atturis = 
        new HashMap<String, SupportedAttribute>();

    static {
        atturis.put("http://openid.net/namePerson/friendly", SupportedAttribute.USERNAME);
        atturis.put("http://sso.usvao.org/schema/username", SupportedAttribute.USERNAME);
        atturis.put("http://schema.openid.net/namePerson/friendly", SupportedAttribute.USERNAME);
        atturis.put("http://axschema.org/namePerson/friendly", SupportedAttribute.USERNAME);
        atturis.put("http://sso.usvao.org/schema/name", SupportedAttribute.NAME);
        atturis.put("http://axschema.org/namePerson", SupportedAttribute.NAME);
        atturis.put("http://schema.openid.net/namePerson", SupportedAttribute.NAME);
        atturis.put("http://openid.net/namePerson", SupportedAttribute.NAME);
        atturis.put("http://sso.usvao.org/schema/namePerson", SupportedAttribute.NAME);
        atturis.put("http://sso.usvao.org/schema/namePerson/first", SupportedAttribute.FIRSTNAME);
        atturis.put("http://sso.usvao.org/schema/namePerson/last", SupportedAttribute.LASTNAME);
        atturis.put("http://sso.usvao.org/schema/email", SupportedAttribute.EMAIL);
        atturis.put("http://axschema.org/contact/email", SupportedAttribute.EMAIL);
        atturis.put("http://schema.openid.net/contact/email", SupportedAttribute.EMAIL);
        atturis.put("http://openid.net/schema/contact/email", SupportedAttribute.EMAIL);
        atturis.put("http://sso.usvao.org/schema/phone", SupportedAttribute.PHONE);
        atturis.put("http://axschema.org/contact/phone/default", SupportedAttribute.PHONE);
        atturis.put("http://axschema.org/contact/phone/business", SupportedAttribute.PHONE);
        atturis.put("http://sso.usvao.org/schema/institution", SupportedAttribute.INSTITUTION);
        atturis.put("http://sso.usvao.org/schema/country", SupportedAttribute.COUNTRY);
        atturis.put("http://sso.usvao.org/schema/credential", SupportedAttribute.CREDENTIAL);
        atturis.put("http://sso.usvao.org/schema/credential/x509", SupportedAttribute.CREDENTIAL);
        atturis.put("http://sso.usvao.org/schema/credential/x509/pem", SupportedAttribute.CERTPEM);
    }

    /**
     * return the SupportedAttribute enum that represents the interpretation
     * of the given attribute URI.  Null is returned if the URI is not known
     * to be supported.  
     */
    public static SupportedAttribute identifyAttributeURI(String uri) {
        return atturis.get(uri);
    }
       
    /**
     * return the list of URIs that are interpreted as representing 
     * a given supported attribute
     */
    public static List<String> attributeURIsFor(SupportedAttribute att) {
        ArrayList<String> out = new ArrayList<String>();
        for(String key : atturis.keySet()) {
            if (atturis.get(key) == att)
                out.add(key);
        }
        return out;
    }
}