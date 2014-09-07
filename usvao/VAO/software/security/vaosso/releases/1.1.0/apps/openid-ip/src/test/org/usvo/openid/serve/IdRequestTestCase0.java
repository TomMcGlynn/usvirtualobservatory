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
public class IdRequestTestCase0 extends IdRequestTestCaseBase {

    public IdRequestTestCase0() throws IOException { super("0setup"); }

    @Before
    public void setup() throws IOException, OpenIDException {
        setupServlet("provider", "goober=gurn");
    }

    @Test
    public void testSetup() {
        assertNotNull("request is null", req);
        assertNotNull("response is null", resp);
        assertNotNull("outfile is null", outf);
        assertTrue("Specialized work dir not created", wrkdir.exists());
        assertTrue("Test DB not found", (new File(wrkdir,"test.db")).exists());
        assertTrue("Test DB not found", 
                   (new File(wrkdir,"openid.properties")).exists());

        File hprops = new File(wrkdir,"hibernate.properties");
        assertTrue("Test DB not found", hprops.exists());
        assertEquals(hprops.toString(), 
                     System.getProperty("openid.hibernate.properties"));
        try {
            Class.forName("org.usvo.openid.orm.OrmKit");
        } catch (ClassNotFoundException ex) {
            fail("OrmKit class not found: " + ex.getMessage());
        }
        assertNotNull(org.usvo.openid.orm.OrmKit.loadUser("unittest"));
    }
}