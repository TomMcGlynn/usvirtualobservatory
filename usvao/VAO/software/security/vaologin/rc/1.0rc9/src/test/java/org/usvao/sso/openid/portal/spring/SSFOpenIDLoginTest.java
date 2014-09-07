package org.usvao.sso.openid.portal.spring;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.security.cert.X509Certificate;
import javax.security.cert.CertificateException;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.usvao.sso.openid.portal.USVAOConventions;
import org.usvao.sso.openid.portal.VAOLoginTest;
import org.usvao.sso.openid.portal.AvailabilityExpiredException;

import static java.lang.String.format;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SSFOpenIDLoginTest {

    static String oid = USVAOConventions.OPENID_BASE_URL + "ray";
    static String testcert = "cert.pem";
    static URL certurl = VAOLoginTest.class.getResource(testcert);
    static OpenIDAuthenticationToken testtoken = null;
    static {
        ArrayList<GrantedAuthority> authz = new ArrayList<GrantedAuthority>();
        authz.add(new SimpleGrantedAuthority("VAO_USER"));

        ArrayList<OpenIDAttribute> atts = new ArrayList<OpenIDAttribute>();
        atts.add(new OpenIDAttribute("username", 
                                     "http://sso.usvao.org/schema/username",
                                     wrapval("roy")));
        atts.add(new OpenIDAttribute("email", 
                                     "http://sso.usvao.org/schema/email",
                                     wrapval("ray@gmail.com")));
        atts.add(new OpenIDAttribute("cert", 
                                     "http://sso.usvao.org/schema/credential",
                                     wrapval(certurl.toString())));
        atts.add(new OpenIDAttribute("color", 
                                     "http://sso.usvao.org/schema/color/fav",
                                     wrapval("red")));

        testtoken = new OpenIDAuthenticationToken(oid, authz, oid, atts);
    }
    static List<String> wrapval(String val) {
        ArrayList<String> out = new ArrayList<String>();
        out.add(val);
        return out;
    }

    SSFOpenIDLogin login = null;

    @Before
    public void makeLogin() {
        login = new SSFOpenIDLogin(testtoken) {
                protected String retrieveCert(URL url) 
                    throws IOException, AvailabilityExpiredException
                {
                    return retrieveEncodedCert(url, false);
                }
            };
    }

    @Test
    public void testAuthenticated() {
        assertTrue(login.isAuthenticated());
    }

    @Test
    public void testGetReason() {
        assertTrue(login.getReason().length() > 0);
        assertEquals("success", login.getReason());
    }

    @Test
    public void testNames() {
        assertEquals(oid, login.getOpenID());
        assertEquals("roy", login.getUserName());
        assertEquals("roy@usvao", login.getQualifiedName());
    }

    @Test
    public void testAttributes() {
        assertNull(login.getLastName());
        assertEquals("ray@gmail.com", login.getEmail());

        assertEquals("roy", login.getAttribute("username"));
        List<String> names = login.getAttributeValues("username");
        assertEquals(1, names.size());
        assertEquals("roy", names.get(0));

        assertEquals("red", login.getAttribute("color"));
        assertEquals("red", 
                  login.getAttribute("http://sso.usvao.org/schema/color/fav"));
    }

    public void testCacheCert() 
        throws IOException, AvailabilityExpiredException 
    {
        String pem = login.getAttribute(USVAOConventions.SupportedAttribute.CERTPEM.toString());
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
        String certurl = login.getCertificateURL();
        assertTrue(certurl.endsWith("/cert.pem"));

        String pem = login.getCertificatePEM();
        assertNotNull(pem);
        assertTrue(pem.length() > 0);
        assertTrue("Data does not look like encodeded cert",
                   pem.contains("-BEGIN CERTIFICATE-") &&
                   pem.contains("-END CERTIFICATE-")        );
    }

    @Test
    public void testCert() 
        throws IOException, AvailabilityExpiredException 
    {
        String certurl = login.getCertificateURL();
        assertTrue(certurl.endsWith("/cert.pem"));

        X509Certificate cert = login.getCertificate();
        assertNotNull(cert);
    }

    @Test
    public void testForget() 
        throws IOException, AvailabilityExpiredException 
    {
        testCertPEM();
        login.forgetCertificate();
        assertNull(login.getCertificateURL());
        assertNull(login.getCertificatePEM());
        assertNull(login.getCertificate());
    }

    public static void main(String[] args) 
        throws IOException, AvailabilityExpiredException 
    {
        SSFOpenIDLoginTest test = new SSFOpenIDLoginTest();
        test.makeLogin();
        test.testForget();
    }

}



