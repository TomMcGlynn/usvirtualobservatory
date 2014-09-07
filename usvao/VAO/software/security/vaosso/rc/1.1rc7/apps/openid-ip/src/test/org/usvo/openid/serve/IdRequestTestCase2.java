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
public class IdRequestTestCase2 extends IdRequestTestCaseBase {

    public IdRequestTestCase2() throws IOException { super("2signin"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        outf = null;
        idreq = null;
        resp = null;
    }

    @Test
    public void testSigninBadUsername() throws IOException, OpenIDException {
        setupSignin("unittest", "success", "signin-bad1-out.msg");

        AuthnAttempt status = idreq.signin("gurn");
        assertFalse(status.isSuccessful());
    }

    @Test
    public void testSigninGood() throws IOException, OpenIDException {
        setupSignin("unittest", "success", "signin-bad1-out.msg");

        AuthnAttempt status = idreq.signin();
        assertTrue(status.isSuccessful());
    }

    public void setupSignin(String username, String password, String outfile) 
        throws IOException, OpenIDException 
    {
        String qstring = "username="+username+"&password="+password+
            "&interactive=true&logout=false&submit=Sign+in";
        setupServlet("signin", qstring, outfile);
    }
}
