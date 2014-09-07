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
public class IdRequestTestCase3 extends IdRequestTestCaseBase {

    public IdRequestTestCase3() throws IOException { super("3authent"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        outf = null;
        idreq = null;
        resp = null;
    }

    @Test
    public void testAuthentBadUsername() throws IOException, OpenIDException {
        setupSignin("unittest", "success", "authent-bad1-out.msg");

        AuthnAttempt status = idreq.authenticate(false, "gurn");
        assertFalse(status.isSuccessful());
    }

    @Test
    public void testAuthentForce() throws IOException, OpenIDException {
        setupSignin(null, null, "authent-force-out.msg");

        AuthnAttempt status = idreq.authenticate(true, "unittest");
        assertFalse(status.isSuccessful());
        String msg = status.getMessage();
        assertTrue("Not a force response: "+msg, 
                   msg.contains("Login explicitly requested"));
    }

    @Test
    public void testAuthentByCookie() throws IOException, OpenIDException {
        setupSignin(null, null, "authent-bycookie-out.msg");

        AuthnAttempt status = idreq.authenticate(false, "unittest");
        assertTrue(status.isSuccessful());
    }

    @Test
    public void testAuthentByCookie2() throws IOException, OpenIDException {
        setupSignin(null, null, "authent-bycookie2-out.msg");

        AuthnAttempt status = idreq.authenticate();
        assertTrue(status.isSuccessful());
    }

    @Test
    public void testAuthentGoodUsername() throws IOException, OpenIDException {
        setupSignin("unittest", "success", "authent-good-out.msg");

        AuthnAttempt status = idreq.authenticate();
        assertTrue(status.isSuccessful());
    }

    public void setupSignin(String username, String password, String outfile) 
        throws IOException, OpenIDException 
    {
        String qstring = "";
        if (username != null) 
            qstring = "username="+username+"&password="+password+
                "&interactive=true&logout=false&submit=Sign+in";
        setupServlet("servlet", qstring, outfile);
        setCookie(req, VALID_TOKEN);
    }
}
