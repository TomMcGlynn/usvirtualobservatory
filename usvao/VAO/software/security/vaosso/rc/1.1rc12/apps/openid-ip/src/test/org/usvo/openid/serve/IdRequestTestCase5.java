package org.usvo.openid.serve;

import org.usvo.openid.Conf;
import org.openid4java.server.ServerManager;
import org.openid4java.OpenIDException;

import org.usvao.service.servlet.sim.TestRequest;
import org.usvao.service.servlet.sim.TestResponse;
import org.usvao.service.servlet.sim.MultiProperties;

import java.io.IOException;
import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.JUnitCore;

/**
 * test of the IdRequestTestCaseBase setup
 */
public class IdRequestTestCase5 extends IdRequestTestCaseBase {

    public IdRequestTestCase5() throws IOException { super("5cancel"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        outf = null;
        idreq = null;
        resp = null;
    }

    @Test
    public void testCancel1() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-out1.msg");
        assertNotNull(outf);
        idreq.cancelLogin();
        closeResponse();

        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertTrue("Missing openid syntax in checkid_immedidate response", 
                   url.contains("openid.ns="));
        assertTrue("Not a checkid_setup cancel response: " + url,
                   url.contains("openid.mode=cancel"));
    }

    @Test
    public void testCancel2() throws IOException, OpenIDException {
        setupServlet("provider", 
                     OpenIdQuery.checkid_setup(portalUrl, "testuser"), 
                     "checkid_setup-out2.msg");
        assertNotNull(outf);
        idreq.handleOpenIDRequest();
        closeResponse();

        req = new TestRequest("signin", "", req.getSession());
        outf = new File(wrkdir, "cancel-out.msg");
        resp = new TestResponse(outf);
        idreq = new IdRequest(mgr, req, resp);

        idreq.cancelLogin();

        MultiProperties header = parseHeader(outf, 302);
        assertTrue("Missing Location header", header.hasName("Location"));
        String url = header.getFirst("Location");
        assertTrue("Missing openid syntax in checkid_immedidate response", 
                   url.contains("openid.ns="));
        assertTrue("Not a checkid_setup cancel response: " + url,
                   url.contains("openid.mode=cancel"));
    }

}