package org.usvao.sso.client;

import java.util.*;
import java.io.*;
import java.security.Principal;
import java.security.Security;
import org.springframework.security.openid.*;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.KeyPair;
import java.security.Key;
import org.bouncycastle.openssl.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class VAOLogin {

    // Attribute URIs
    public static final String USERNAME    = "http://axschema.org/namePerson/friendly";
    public static final String NAME        = "http://axschema.org/namePerson";
    public static final String EMAIL       = "http://axschema.org/contact/email";
    public static final String PHONE       = "http://axschema.org/contact/phone";
    public static final String INSTITUTION = "http://sso.usvao.org/schema/institution";
    public static final String COUNTRY     = "http://sso.usvao.org/schema/country";
    public static final String CREDENTIAL  = "http://sso.usvao.org/schema/credential/x509";
    public static final String RAWCREDENTIAL  = "RAWCREDENTIAL";

    private static OpenIDAuthenticationToken getVAOSuccessToken
                    () {
        Object token = SecurityContextHolder.getContext().getAuthentication();
        if (token != null && token instanceof OpenIDAuthenticationToken)
        {
            OpenIDAuthenticationToken opToken = (OpenIDAuthenticationToken) token;
            if (OpenIDAuthenticationStatus.SUCCESS.equals(opToken.getStatus()))
            {
                return opToken;
            }
        }
        return null;
    }

/**
 * Determine if this is an authenticated user/session.
 * <p>
 * This method returns true if the user/session is authenticated,
 * false if not.
 *
 */
    public static boolean isAuthenticated() {
        if (getVAOSuccessToken() != null)
            return true;
        else
            return false;
    }

/**
 * Get attributes returned by OpenID provider.
 * <p>
 * This method returns a {@link List} of {@link OpenIDAttribute}
 * objects, one for each of the OpenID attributes returned by the
 * OpenID provider as part of OpenID Attribute Exchange.
 *
 */
    public static List<OpenIDAttribute> getAttributes() {
        OpenIDAuthenticationToken token = getVAOSuccessToken();

        if (token != null)
           return token.getAttributes();
        else
           return null;
    }

/**
 * Get attributes returned by OpenID provider as Properties.
 * <p>
 * This method returns a {@link Properties} object containing
 * one entry for each for OpenID attributes returned by the OpenID
 * provider as part of OpenID Attribute Exchange. The keys to Properties
 * will be the names used to specify attributes using the openid-attribute
 * element in the Spring Security configuration file. If an OpenID
 * attribute has multiple values only the first value is returned
 * in the value field of each of the properties.
 *
 */
    public static Properties getAttributesAsProperties() {
        List<OpenIDAttribute> attributes = getAttributes();
        Properties props = new Properties();

        if (attributes != null)
            for (OpenIDAttribute attrib: attributes)
            {
                props.setProperty(attrib.getName(), attrib.getValues().get(0));
            }

        return props;
    }

/**
 * Get value of the specified attribute type.
 * <p>
 * This method returns a {@link List} of {@link String}
 * objects, one for each of the values returned by the
 * OpenID provider for the specified attribute type,
 * as part of OpenID Attribute Exchange.
 *
 * @param	type	A string denoting the type of the attribute
 *			as specified in the openid-attribute in the
 *                      Spring Security configuration file.
 *
 */
    public static List<String> getAttribute(String type) {
        List<OpenIDAttribute> attributes = getAttributes();

        if (attributes != null)
            for (OpenIDAttribute attrib: attributes)
            {
                if (attrib.getType().equals(type))
                    return attrib.getValues();
            }

        return null;
    }

/**
 * Get usernames for the currently authenticated user.
 * <p>
 * Returns a {@link List} of usernames returned by
 * the OpenID Provider for the currently authenticated
 * user. A null value will be returned if authentication
 * has not yet occurred.
 *
 */
    public static List<String> getUsernames() {
        return getAttribute(USERNAME);
    }

/**
 * Get a username for the currently authenticated user.
 * <p>
 * Returns a username returned by the OpenID Provider for
 * the currently authenticated user. If several usernames
 * were returned by the Provider, this method will return
 * the first one. A null value will be returned if
 * authentication has not yet occurred.
 *
 */

    public static String getUsername() {
        List<String> usernames = getUsernames();

        if (usernames != null)
            return usernames.get(0);

        return null;
    }

/**
 * Get names for the currently authenticated user.
 * <p>
 * Returns a {@link List} of names returned by
 * the OpenID Provider for the currently authenticated
 * user. A null value will be returned if authentication
 * has not yet occurred.
 *
 */

    public static List<String> getNames() {
        return getAttribute(NAME);
    }

/**
 * Get a name for the currently authenticated user.
 * <p>
 * Returns a name returned by the OpenID Provider for
 * the currently authenticated user. If several names
 * were returned by the Provider, this method will return
 * the first one. A null value will be returned if
 * authentication has not yet occurred.
 *
 */
    public static String getName() {
        List<String> names = getNames();

        if (names != null)
            return names.get(0);

        return null;
    }

/**
 * Get email addresses for the currently authenticated user.
 * <p>
 * Returns a {@link List} of email addresses returned by
 * the OpenID Provider for the currently authenticated
 * user. A null value will be returned if authentication
 * has not yet occurred.
 *
 */

    public static List<String> getEmails() {
        return getAttribute(EMAIL);
    }

/**
 * Get an email address for the currently authenticated user.
 * <p>
 * Returns an email address returned by the OpenID Provider for
 * the currently authenticated user. If several email addresses
 * were returned by the Provider, this method will return
 * the first one. A null value will be returned if
 * authentication has not yet occurred.
 *
 */
    public static String getEmail() {
        List<String> emails = getEmails();

        if (emails != null)
            return emails.get(0);

        return null;
    }

/**
 * Get phone numbers for the currently authenticated user.
 * <p>
 * Returns a {@link List} of phone numbers returned by
 * the OpenID Provider for the currently authenticated
 * user. A null value will be returned if authentication
 * has not yet occurred.
 *
 */

    public static List<String> getPhones() {
        return getAttribute(PHONE);
    }

/**
 * Get a phone number for the currently authenticated user.
 * <p>
 * Returns a phone number returned by the OpenID Provider for
 * the currently authenticated user. If several phone numbers
 * were returned by the Provider, this method will return
 * the first one. A null value will be returned if
 * authentication has not yet occurred.
 *
 */
    public static String getPhone() {
        List<String> phones = getPhones();

        if (phones != null)
            return phones.get(0);

        return null;
    }

/**
 * Get names of institutions the currently authenticated user
 * is a part of according to the OpenID Provider.
 * <p>
 * Returns a {@link List} of institution names returned by
 * the OpenID Provider for the currently authenticated
 * user. A null value will be returned if authentication
 * has not yet occurred.
 *
 */

    public static List<String> getInstitutions() {
        return getAttribute(INSTITUTION);
    }

/**
 * Get an institution name for the currently authenticated user.
 * <p>
 * Returns an institution name returned by the OpenID Provider for
 * the currently authenticated user. If several institution names
 * were returned by the Provider, this method will return
 * the first one. A null value will be returned if
 * authentication has not yet occurred.
 *
 */
    public static String getInstitution() {
        List<String> institutions = getInstitutions();

        if (institutions != null)
            return institutions.get(0);

        return null;
    }

/**
 * Get countries for the currently authenticated user.
 * <p>
 * Returns a {@link List} of countries returned by
 * the OpenID Provider for the currently authenticated
 * user. A null value will be returned if authentication
 * has not yet occurred.
 *
 */

    public static List<String> getCountries() {
        return getAttribute(COUNTRY);
    }

/**
 * Get a country name for the currently authenticated user.
 * <p>
 * Returns a country name returned by the OpenID Provider for
 * the currently authenticated user. If several country names
 * were returned by the Provider, this method will return
 * the first one. A null value will be returned if
 * authentication has not yet occurred.
 *
 */
    public static String getCountry() {
        List<String> countries = getCountries();

        if (countries != null)
            return countries.get(0);

        return null;
    }

/**
 * Get the URL of the credential for the currently authenticated user.
 * <p>
 * Returns the URL of the short-lived credential file on the OpenID server
 * returned by the OpenID Provider for the currently authenticated user,
 * if a credential for the user was requested/required as part of OpenID
 * Attribute Exchange during OpenID authentication.  This file is typically
 * purged by the OpenID Provider server within a few minutes of generation.
 * <p>
 * So, the VAOLOGIN mechanisms tied into Spring Security framework configuration
 * automatically download the file on successful authentication. You may
 * retrieve the contents of the auto-downloaded file (in PEM format) using the
 * getCredential() method in this package.
 * <p>
 * Thus there is little utility aside from debugging, logging, etc., for this
 * method since all that it returns is the location of a possibly already
 * purged credential file.
 * <p>
 * A null value will be returned if authentication has not yet occurred.
 *
 */
    public static String getCredentialURL() {
        List<String> credentialURL =  getAttribute(CREDENTIAL);

        if (credentialURL != null)
            return credentialURL.get(0);

        return null;
    }

/**
 * Get the contents of the credential file downloaded from the OpenID Provider
 * for the currently authenticated user.
 * <p>
 * If a credential was requested/required for the user as part of OpenID
 * Attribute Exchange, the OpenID Provider generates a credential for the
 * user and returns the URL of the credential file as part of Attribute
 * Exchange.  The credential file on the OpenID Provider is a short-lived file
 * and is purged with a few minutes of creation.
 * <p>
 * So, the VAOLOGIN mechanisms tied into the Spring Security framework
 * configuration automatically download the file as soon as
 * a successful authentication response is received. You may then
 * retrieve the contents of the file (in PEM format) using this
 * getCredential() method.
 * <p>
 * A null value will be returned if authentication has not yet occurred.
 *
 */
    public static String getCredential() {
        List<String> credential = getAttribute(RAWCREDENTIAL);

        if (credential != null)
            return credential.get(0);

        return null;
    }

/**
 * Get the certificate from the credential file downloaded from the OpenID
 * Provider for the currently authenticated user.
 * <p>
 * If a credential was requested/required for the user as part of OpenID
 * Attribute Exchange, the OpenID Provider generates a credential for the
 * user and returns the URL of the credential file as part of Attribute
 * Exchange.  The credential file on the OpenID Provider is a short-lived file
 * and is purged with a few minutes of creation.
 * <p>
 * So, the VAOLOGIN mechanisms tied into the Spring Security framework
 * configuration automatically download the file as soon as
 * a successful authentication response is received. The contents of the
 * credential file are a certificate followed by the Private Key. You may
 * retrieve the {@link X509Certificate} from the credential file using this
 * getCertificate() method.
 *
 */
    public static X509Certificate getCertificate() {
        String cred = getCredential();

        if (cred != null) {

            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate)cf.generateCertificate(
                                 new ByteArrayInputStream(cred.getBytes()));
                return cert;
            } catch (java.security.cert.CertificateException e) {
                // TODO: handle this approp.
            }
        }
        return null;
    }

/**
 * Get a PKCS12 {@link KeyStore} from the credential file downloaded from the
 * OpenID Provider for the currently authenticated user.
 * <p>
 * If a credential was requested/required for the user as part of OpenID
 * Attribute Exchange, the OpenID Provider generates a credential for the
 * user and returns the URL of the credential file as part of Attribute
 * Exchange.  The credential file on the OpenID Provider is a short-lived file
 * and is purged with a few minutes of creation.
 * <p>
 * So, the VAOLOGIN mechanisms tied into the Spring Security framework
 * configuration automatically download the file as soon as
 * a successful authentication response is received. The contents of the
 * credential file are a certificate followed by the Private Key. You may
 * retrieve a PKCS12 {@link KeyStore} loaded with the certificate and private
 * key from the credential file using this getPKCS12KeyStore() method.
 * <p>
 * A {@link KeyStore} can be used to authenticate to services as the user
 * to which the Certificate and Key Pair in the KeyStore pertain.
 *
 */
    public static KeyStore getPKCS12KeyStore(char[] passphrase)
           throws java.io.IOException,
            java.security.KeyStoreException,
            java.security.cert.CertificateException,
            java.security.NoSuchAlgorithmException {
        String cred = getCredential();

        if (Security.getProvider("BC") == null)
            Security.insertProviderAt(new BouncyCastleProvider(), 2);

        if (cred != null) {
            InputStreamReader reader = new InputStreamReader(new 
                                 ByteArrayInputStream(cred.getBytes()));

            PEMReader pem = new PEMReader(reader, new PasswordFinder() {
                @Override public char[] getPassword() {
                    // Key not encrypted
                    return "".toCharArray();
                }
            });

            PrivateKey key = null;
            ArrayList<java.security.cert.Certificate> certs = new ArrayList<java.security.cert.Certificate>();
            Object obj;
            while ((obj = pem.readObject()) != null) {
                if (obj instanceof X509Certificate) {
                    certs.add((java.security.cert.Certificate)obj);
                } else if (obj instanceof PrivateKey) {
                    key = (PrivateKey)obj;
                } else if (obj instanceof KeyPair) {
                    key = ((KeyPair)obj).getPrivate();
                }
            }
            pem.close();
            reader.close();

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null);

            X509Certificate[] certarray = new X509Certificate[1];
            ks.setKeyEntry("vaouser", key, passphrase, certs.toArray(certarray));

            return ks;
        }

        return null;
    }
}
