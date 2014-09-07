package org.usvo.openid.serve;

import org.usvo.openid.Conf;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.serve.SessionKit;
import org.openid4java.server.ServerManager;
import org.openid4java.OpenIDException;

import org.usvao.service.servlet.sim.TestRequest;
import org.usvao.service.servlet.sim.TestResponse;
import org.usvao.service.servlet.sim.MultiProperties;

import java.io.IOException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.JUnitCore;

/**
 * test of the IdRequestTestCaseBase setup
 */
public class IdRequestTestCase4 extends IdRequestTestCaseBase {

    public IdRequestTestCase4() throws IOException { super("4expire"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        outf = null;
        idreq = null;
        resp = null;
        setupServlet("servlet", "");
        setCookie(req, VALID_TOKEN);
    }

    @Test
    public void testEndSession() throws IOException, OpenIDException {

        UserSession sess = SessionKit.getLoginSession(req, resp, true, false);
        assertNotNull(sess);
        assertTrue(sess.isValid());
        idreq.endSession(sess);
        assertFalse(sess.isValid());
        sess = SessionKit.getLoginSession(req, resp, true, false);
        assertFalse(sess.isValid());
    }



}
