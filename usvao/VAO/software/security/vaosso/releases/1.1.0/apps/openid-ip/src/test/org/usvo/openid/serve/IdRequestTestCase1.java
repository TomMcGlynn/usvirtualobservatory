package org.usvo.openid.serve;

import org.usvo.openid.Conf;
import org.openid4java.server.ServerManager;
import org.openid4java.OpenIDException;

import org.usvao.service.servlet.sim.TestRequest;
import org.usvao.service.servlet.sim.TestResponse;
import org.usvao.service.servlet.sim.MultiProperties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.net.HttpCookie;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.JUnitCore;

/**
 * test of the IdRequestTestCaseBase setup
 */
public class IdRequestTestCase1 extends IdRequestTestCaseBase {

    public IdRequestTestCase1() throws IOException { super("1ro"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        outf = null;
        idreq = null;
        resp = null;
    }

    @Test
    public void testAssociateHandling() throws IOException, OpenIDException {
        setupServlet("provider", OpenIdQuery.associate(), "associate-out.msg");
        assertNotNull(outf);
        idreq.handleOpenIDRequest();
        closeResponse();

        assertTrue("No output from associate req", outf.exists());
        Properties props = parsePropertyContent(outf);
        assertTrue(props.stringPropertyNames().contains("ns"));
    }

    @Test
    public void testAssociate() throws IOException, OpenIDException {
        setupServlet("provider", OpenIdQuery.associate(), "associate-out.msg");
        assertNotNull(outf);
        idreq.handleAssociation();
        closeResponse();

        assertTrue("No output from associate req", outf.exists());
        Properties props = parsePropertyContent(outf);
        assertFalse("Error reported in associate response",
                    props.stringPropertyNames().contains("error_code"));
        assertTrue(props.stringPropertyNames().contains("assoc_handle"));
    }

    @Test
    public void testCheckidImmediateHandling() 
        throws IOException, OpenIDException 
    {
        setupServlet("provider", 
                     OpenIdQuery.checkid_immediate(portalUrl, "testuser"), 
                     "checkid_immediate-out.msg");
        assertNotNull(outf);
        idreq.handleOpenIDRequest();
        closeResponse();
        
        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertTrue("Missing openid syntax in checkid_immedidate response", 
                   url.contains("openid.ns="));
        assertTrue("Not a checkid_immedidate response: " + url,
                   url.contains("openid.mode=setup_needed"));
    }

    @Test
    public void testCheckidImmediate() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_immediate(portalUrl, "testuser"), 
                     "checkid_immediate-out.msg");
        assertNotNull(outf);
        idreq.handleCheckid(true);
        closeResponse();
        
        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertTrue("Missing openid syntax in checkid_immedidate response", 
                   url.contains("openid.ns="));
        assertTrue("Not a checkid_immedidate response: " + url,
                   url.contains("openid.mode=setup_needed"));
    }

    @Test
    public void testGetAuthStatusValid() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-dummy-out.msg");
        setCookie(req, VALID_TOKEN);
        assertTrue(idreq.getAuthenticationStatus("unittest").isSuccessful());
    }

    @Test
    public void testGetAuthStatusInValid() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-dummy-out.msg");
        setCookie(req, INVALID_TOKEN);
        assertFalse(idreq.getAuthenticationStatus("unittest").isSuccessful());
    }

    @Test
    public void testCheckidSetup() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-out.msg");
        assertNotNull(outf);
        idreq.handleCheckid(false);
        closeResponse();
        
        MultiProperties header = parseHeader(outf, 200);
        assertFalse("Expected login page", header.hasName("Location"));
        String page = getContent(outf);
        assertTrue(page.contains("<input type=\"password\""));
    }

    @Test
    public void testCheckidSetupHandling() 
        throws IOException, OpenIDException 
    {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-out.msg");
        assertNotNull(outf);
        idreq.handleOpenIDRequest();
        closeResponse();
        
        MultiProperties header = parseHeader(outf, 200);
        assertFalse("Expected login page", header.hasName("Location"));
        String page = getContent(outf);
        assertTrue(page.contains("<input type=\"password\""));
    }

    @Test
    public void testCheckidSetupNeedConfirm() 
        throws IOException, OpenIDException 
    {
        String qstring = OpenIdQuery.checkid_setup(portalUrl, "unittest");
        String[] atts = {"email"};
        qstring = OpenIdQuery.appendAxFetch(qstring, atts);
        setupServlet("provider", qstring, "checkid_setup-confirm-out.msg");
        setCookie(req, VALID_TOKEN);
        assertNotNull(outf);
        idreq.handleCheckid(false);
        closeResponse();
        
        MultiProperties header = parseHeader(outf, 200);
        assertFalse("Expected confirm page", header.hasName("Location"));
        String page = getContent(outf);
        assertTrue(page.contains("name=\"nvo_sso_enabled\" "));
    }


    @Test
    public void testCheckAuthentication() throws IOException, OpenIDException {
        setupCheckAuthentication();
        assertNotNull(outf);
        idreq.handleCheckAuthentication();
        closeResponse();

        assertTrue("No output from check_authentication req", outf.exists());
        Properties props = parsePropertyContent(outf);
        assertTrue(props.stringPropertyNames().contains("is_valid"));
        assertEquals("true", props.getProperty("is_valid"));
    }

    public void setupCheckAuthentication() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "unittest"), 
                     "checkid_setup-dummy-out.msg");
        setCookie(req, VALID_TOKEN);
        assertNotNull(outf);
        idreq.handleCheckid(false);
        closeResponse();
        
        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String urls = header.getFirst("Location");
        URL url = null;
        try {
            url = new URL(urls);
        } catch (MalformedURLException ex) {
            fail("Bad valid redirect URL: " + ex.getMessage());
        }

        String qstring = URLDecoder.decode(url.getQuery(), "UTF-8");
        qstring = qstring.replaceAll("openid.mode=\\w+",
                                     "openid.mode=check_authentication");
        setupServlet("provider", qstring, "check_auth-out.msg");
    }

    public static void main(String args[]) {
      org.junit.runner.JUnitCore.main("org.usvo.openid.serve.IdRequestTestCase1");
    }
}