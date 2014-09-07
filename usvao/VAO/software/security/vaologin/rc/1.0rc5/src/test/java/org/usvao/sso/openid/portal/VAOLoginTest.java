package org.usvao.sso.openid.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.security.cert.X509Certificate;
import javax.security.cert.CertificateException;
import static java.lang.String.format;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class VAOLoginTest {

    public void testAuthenticated() {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(true, oid);
        assertTrue(login.isAuthenticated());
        login = new ProtoVAOLogin(false, oid);
        assertFalse(login.isAuthenticated());
        login = new ProtoVAOLogin(oid, null, null, false, false);
        assertTrue(login.isAuthenticated());
    }

    @Test
    public void testDefNames() {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(true, oid);

        assertEquals(oid, login.getOpenID());
        assertEquals("joe", login.getUserName());
        assertEquals("joe@usvao", login.getQualifiedName());

        login = new ProtoVAOLogin(oid, "joecool", null, false, false);

        assertEquals(oid, login.getOpenID());
        assertEquals("joecool", login.getUserName());
        assertEquals("joecool@usvao", login.getQualifiedName());
    }

    @Test
    public void testDefUserName() {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        assertEquals("joe", VAOLogin.defaultUserName(oid));
    }

    @Test
    public void testForeignNames() {
        String oid = "http://eurovo.net/id/joe";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, false, false);

        assertEquals(oid, login.getOpenID());
        assertNull(login.getUserName());
        assertNull(login.getQualifiedName());
    }

    @Test
    public void testNames() {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(oid, "joecool", "joecool@stsci", 
                                           false, false);

        assertEquals(oid, login.getOpenID());
        assertEquals("joecool", login.getUserName());
        assertEquals("joecool@stsci", login.getQualifiedName());
    }

    @Test
    public void testRetrieve() 
        throws IOException, AvailabilityExpiredException 
    {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, false, true);
        String certurl = login.getCertificateURL();
        String pem = null;

        // test for insecure protocol
        URL url = new URL(certurl);
        assertFalse("Safe protocol; unable to test unsafe: " + certurl,
                    "https".equals(url.getProtocol().toLowerCase()));
        try {
            pem = VAOLogin.retrieveEncodedCert(url, true);
            fail("Failed to detected insecure protocol: " + url.getProtocol());
        } 
        catch (IOException ex) {
            assertTrue(ex.getMessage().startsWith("Insecure "));
        }

        pem = VAOLogin.retrieveEncodedCert(url, false);
        assertTrue("Data does not look like encodeded cert",
                   pem.contains("-BEGIN CERTIFICATE-") &&
                   pem.contains("-END CERTIFICATE-")        );
        
    }

    @Test
    public void testCacheCert() 
        throws IOException, AvailabilityExpiredException 
    {
        String pem = null;
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";

        VAOLogin login = new ProtoVAOLogin(oid, null, null, false, false);
        pem = login.getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        assertNull(pem);
        assertFalse("Failed to detect missing cert request",
                    login.cacheCertificate());
        pem = login.getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        assertNull(pem);

        login = new ProtoVAOLogin(oid, null, null, false, true);
        pem = login.getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        assertNull(pem);
        assertTrue("Failed to acknowledge successful cert retrieval",
                    login.cacheCertificate());
        pem = login.getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        assertNotNull(pem);
        assertTrue("Data does not look like encodeded cert",
                   pem.contains("-BEGIN CERTIFICATE-") &&
                   pem.contains("-END CERTIFICATE-")        );

        pem = login.getCertificatePEM();
        assertNotNull(pem);
        assertTrue("Data does not look like encodeded cert",
                   pem.contains("-BEGIN CERTIFICATE-") &&
                   pem.contains("-END CERTIFICATE-")        );
    }

    @Test
    public void testCertPEM() 
        throws IOException, AvailabilityExpiredException 
    {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, false, true);
        String certurl = login.getCertificateURL();
        assertTrue(certurl.endsWith("/cert.pem"));

        String pem = login.getCertificatePEM();
        assertNotNull(pem);
        assertTrue(pem.length() > 0);
        assertTrue("Data does not look like encodeded cert",
                   pem.contains("-BEGIN CERTIFICATE-") &&
                   pem.contains("-END CERTIFICATE-")        );
    }

    public static void main(String[] args) 
        throws IOException, AvailabilityExpiredException 
    {
        VAOLoginTest test = new VAOLoginTest();
        test.testCertPEM();
    }

    @Test
    public void testCert() 
        throws IOException, AvailabilityExpiredException 
    {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, false, true);
        String certurl = login.getCertificateURL();
        assertTrue(certurl.endsWith("/cert.pem"));

        X509Certificate cert = login.getCertificate();
        assertNotNull(cert);
    }

    @Test
    public void testArbAttribute() {
        String oid = USVAOConventions.OPENID_BASE_URL + "joe";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, false, false);
        ((ProtoVAOLogin)login).
            setAttribute("color", "http://local.net/color/favorite", "red");

        assertEquals("red", login.getAttribute("color"));
        assertEquals("red", login.getAttribute("http://local.net/color/favorite"));
        assertNull(login.getAttribute("goob"));
        assertNull(login.getAttribute("http://local.net/color"));
    }

    @Test
    public void testStdAttribute() {
        String oid = USVAOConventions.OPENID_BASE_URL + "jane";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, true, false);

        assertEquals("jane", login.getUserName());
        assertEquals("joe", login.getAttribute("username"));
        assertEquals("joe", 
                   login.getAttribute("http://openid.net/namePerson/friendly"));
        assertNull(
            login.getAttribute("http://schema.openid.net/namePerson/friendly"));
        assertEquals("joe", 
   login.getAttribute(USVAOConventions.SupportedAttribute.USERNAME.toString()));
        List<String> names = login.getAttributeValues("username");
        assertEquals(1, names.size());
        assertEquals("joe", names.get(0));

        assertEquals("joe@gmail.com", login.getEmail());
        assertEquals("Cool", login.getLastName());
        assertEquals("Joe", login.getFirstName());
        assertEquals("Mali", login.getCountry());
    }

    @Test
    public void testAttributeKeys() {
        String oid = USVAOConventions.OPENID_BASE_URL + "jane";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, true, false);

        Set<String> names = login.getAttributeKeys();
        assertTrue(names.contains("username"));
        assertTrue(names.contains("email"));
        assertTrue(names.contains("lastname"));
        assertTrue(names.contains("firstname"));
        assertTrue(names.contains("fullname"));
        assertTrue(names.contains("phone"));
        assertTrue(names.contains("country"));

        assertTrue(names.contains("http://openid.net/namePerson/friendly"));
        assertTrue(names.contains("http://sso.usvao.org/schema/email"));
        assertTrue(names.contains("http://sso.usvao.org/schema/namePerson/last"));
        assertTrue(names.contains("http://sso.usvao.org/schema/namePerson/first"));
        assertTrue(names.contains("http://sso.usvao.org/schema/name"));
        assertTrue(names.contains("http://sso.usvao.org/schema/phone"));
        assertTrue(names.contains("http://sso.usvao.org/schema/country"));

        assertTrue(names.contains(USVAOConventions.SupportedAttribute.USERNAME.toString()));
        assertTrue(names.contains(USVAOConventions.SupportedAttribute.EMAIL.toString()));
        assertTrue(names.contains(USVAOConventions.SupportedAttribute.LASTNAME.toString()));
        assertTrue(names.contains(USVAOConventions.SupportedAttribute.FIRSTNAME.toString()));
        assertTrue(names.contains(USVAOConventions.SupportedAttribute.NAME.toString()));
        assertTrue(names.contains(USVAOConventions.SupportedAttribute.PHONE.toString()));
        assertTrue(names.contains(USVAOConventions.SupportedAttribute.COUNTRY.toString()));

        assertEquals(21, names.size());
    }

    @Test
    public void testAttributeURIs() {
        String oid = USVAOConventions.OPENID_BASE_URL + "jane";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, true, false);

        Set<String> names = login.getAttributeURIs();
        assertTrue(names.contains("http://openid.net/namePerson/friendly"));
        assertTrue(names.contains("http://sso.usvao.org/schema/email"));
        assertTrue(names.contains("http://sso.usvao.org/schema/namePerson/last"));
        assertTrue(names.contains("http://sso.usvao.org/schema/namePerson/first"));
        assertTrue(names.contains("http://sso.usvao.org/schema/name"));
        assertTrue(names.contains("http://sso.usvao.org/schema/phone"));
        assertTrue(names.contains("http://sso.usvao.org/schema/country"));

        assertEquals(7, names.size());
    }

    @Test
    public void testAttributeAliases() {
        String oid = USVAOConventions.OPENID_BASE_URL + "jane";
        VAOLogin login = new ProtoVAOLogin(oid, null, null, true, false);

        Set<String> names = login.getAttributeAliases();
        assertTrue(names.contains("username"));
        assertTrue(names.contains("email"));
        assertTrue(names.contains("lastname"));
        assertTrue(names.contains("firstname"));
        assertTrue(names.contains("fullname"));
        assertTrue(names.contains("phone"));
        assertTrue(names.contains("country"));

        assertEquals(7, names.size());
    }
}

class ProtoVAOLogin extends VAOLogin { 

    HashMap<String, List<String> > attributes = 
        new HashMap<String, List<String> >();
    static String testcert = "cert.pem";
    URL credurl = VAOLoginTest.class.getResource(testcert);

    public ProtoVAOLogin(String openid, String name, String qualifiedName,
                         boolean loadAtts, boolean loadCert) 
    {
        super(true, openid, name, qualifiedName);
        if (loadAtts) loadAttributes();
        if (loadCert) loadCert();
    }

    public ProtoVAOLogin(boolean success, String openid) {
        super(success, openid, null, null);
    }

    public String getReason() {
        return (isAuthenticated()) ? "success" : "failed";
    }

    void loadAttributes() {
        setAttribute("username", "http://openid.net/namePerson/friendly","joe");
        setAttribute(USVAOConventions.SupportedAttribute.USERNAME, "joe");
        setAttribute("email", "http://sso.usvao.org/schema/email",
                     "joe@gmail.com");
        setAttribute(USVAOConventions.SupportedAttribute.EMAIL,"joe@gmail.com");
        setAttribute("lastname", "http://sso.usvao.org/schema/namePerson/last",
                     "Cool");
        setAttribute(USVAOConventions.SupportedAttribute.LASTNAME,
                     "Cool");
        setAttribute("firstname","http://sso.usvao.org/schema/namePerson/first",
                     "Joe");
        setAttribute(USVAOConventions.SupportedAttribute.FIRSTNAME,
                     "Joe");
        setAttribute("fullname", "http://sso.usvao.org/schema/name","Joe Cool");
        setAttribute(USVAOConventions.SupportedAttribute.NAME, "Joe Cool");
        setAttribute("phone","http://sso.usvao.org/schema/phone", "725-931");
        setAttribute(USVAOConventions.SupportedAttribute.PHONE, "725-931");
        setAttribute("country","http://sso.usvao.org/schema/country", "Mali");
        setAttribute(USVAOConventions.SupportedAttribute.COUNTRY, "Mali");
    }

    protected void setCertificatePEM(String pem) {
        setAttribute(USVAOConventions.SupportedAttribute.CERTPEM, pem);
    }

    void loadCert() {
        setAttribute("cert", "http://sso.usvao.org/schema/credential", 
                     credurl.toString());
        setAttribute(USVAOConventions.SupportedAttribute.CREDENTIAL, 
                     credurl.toString());
    }

    List<String> wrapval(String val) {
        ArrayList<String> out = new ArrayList<String>(1);
        out.add(val);
        return out;
    }

    public void setAttribute(String alias, String uri, String val) {
        List<String> wrapped = wrapval(val);
        attributes.put(alias, wrapped);
        attributes.put(uri, wrapped);
    }

    public void setAttribute(USVAOConventions.SupportedAttribute key, 
                             String val)
    {
        attributes.put(key.toString(), wrapval(val));
    }

    public List<String> getAttributeValues(String attname) {
        return attributes.get(attname);
    }

    public Set<String> getAttributeKeys() {
        return new HashSet<String>(attributes.keySet());
    }

    protected String retrieveCert(URL url) 
        throws IOException, AvailabilityExpiredException
    {
        return retrieveEncodedCert(url, false);
    }

    public void forgetCertificate() {
        attributes.remove(USVAOConventions.SupportedAttribute.CERTPEM.toString());
        List<String> uris = USVAOConventions.attributeURIsFor(USVAOConventions.SupportedAttribute.CREDENTIAL);
        for (String uri : uris) {
            attributes.remove(uri);
        }
    }
}