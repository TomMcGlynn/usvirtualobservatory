package org.usvo.openid.serve;

import org.usvo.openid.Conf;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.serve.SessionKit;
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
public class IdRequestTestCase6 extends IdRequestTestCaseBase {

    public IdRequestTestCase6() throws IOException { super("6logout"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        outf = null;
        idreq = null;
        resp = null;
    }

    @Test
    public void testLogout1() throws IOException, OpenIDException {
        setupServlet("logout", "returnURL="+portalUrl, 
                     "logout-out1.msg");
        assertNotNull(outf);
        idreq.logout(req.getParameter("returnURL"));
        closeResponse();

        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertEquals(portalUrl, url);
    }

    @Test
    public void testLogout2() throws IOException, OpenIDException {
        setupServlet("logout", "returnURL="+portalUrl, "logout-out2.msg");
        setCookie(req, VALID_TOKEN);
        assertNotNull(outf);

        UserSession sess = SessionKit.getLoginSession(req, resp, true, false);
        assertNotNull(sess);
        assertTrue(sess.isValid());

        idreq.logout(req.getParameter("returnURL"));
        closeResponse();

        sess = SessionKit.getLoginSession(req, resp, true, false);
        assertFalse(sess.isValid());

        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertEquals(portalUrl, url);
    }

    @Test
    public void testLogout3() throws IOException, OpenIDException {
        // test while in the middle of a login process
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-logout.msg");
        assertNotNull(outf);
        idreq.handleOpenIDRequest();
        closeResponse();

        req = new TestRequest("signin", "", req.getSession());
        outf = new File(wrkdir, "logout-out3.msg");
        resp = new TestResponse(outf);
        idreq = new IdRequest(mgr, req, resp);

        idreq.logout(null);
        closeResponse();

        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertTrue(url.startsWith(portalUrl));
        assertTrue(url.contains("openid.mode=cancel"));
    }

    public static void main(String args[]) {
      org.junit.runner.JUnitCore.main("org.usvo.openid.serve.IdRequestTestCase6");
    }
}