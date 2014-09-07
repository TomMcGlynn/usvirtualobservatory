package org.usvo.openid.serve;

import org.usvo.openid.Conf;
import org.usvo.openid.orm.OrmKit;
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
 * a base class for separate TestCase classes for IdRequest.  The IdRequest
 * tests employ a test database in the form of an sqlite file.  Due to an 
 * interplay between sqlite file locking and hibernate database connection 
 * management, it is necessary to run each test function in a separate 
 * virtual machine.
 */
public class IdRequestTestCaseBase {

    public final static String portalUrl = "http://rp.example.com/portal";
    public final static String testdir = System.getProperty("test.outdir");
    public final static String srcdir = System.getProperty("test.webinf");
    public final static File idrdir;

    protected String testname = null;
    protected ServerManager mgr = new ServerManager();
    protected IdRequest idreq = null;
    protected TestRequest req = null;
    protected TestResponse resp = null;
    protected File outf = null;
    protected File wrkdir = null;

    static { 
        idrdir = new File(testdir, "idReqTests");
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException ex) {   }
    }
    public final static String VALID_TOKEN = "Valid_Secret_session_token_01234";
    public final static String INVALID_TOKEN = "Invalid_Secret_session_token_012";

    IdRequestTestCaseBase(String name) throws IOException {
        testname = name;
        wrkdir = new File(idrdir, testname);
        if (! (new File(testdir)).exists()) 
            throw new IllegalStateException("missing base test directory: "+
                                            testdir);
        if (! wrkdir.exists()) wrkdir.mkdirs();

        // copy test files to our independent work area
        filterProperties(new File(srcdir, "hibernate.properties"));
        filterProperties(new File(srcdir, "openid.properties"));
        FileUtils.copyFileToDirectory(new File(srcdir, "test.db"), wrkdir);

        System.setProperty("openid.hibernate.properties",
                           (new File(wrkdir, "hibernate.properties")).toString());
        Conf.init(new File(wrkdir, "openid.properties"), srcdir);
        mgr.setOPEndpointUrl(Conf.get().getBaseUrl() + "/provider");
        mgr.setUserSetupUrl(Conf.get().getBaseUrl() + "/signin");
    }

    protected void filterProperties(File propfile) throws IOException {
        BufferedReader rdr = null;
        PrintWriter wrtr = null;
        try {
            rdr = new BufferedReader(new FileReader(propfile));
            wrtr = new PrintWriter(new FileWriter(
                                      new File(wrkdir,propfile.getName())));

            String line = null;
            while ((line = rdr.readLine()) != null) {
                if (line.contains("url=jdbc\\:"))
                    line = line.replaceAll("jdbc\\\\:.*$", 
                                           "jdbc\\\\:sqlite\\:" + 
                                           new File(wrkdir, "test.db"));
                wrtr.println(line);
            }
        }
        finally {
            if (rdr != null) rdr.close();
            if (wrtr != null) wrtr.close();
        }
    }

    protected void setupServlet(String servletPath, String queryString) 
        throws IOException, OpenIDException
    {
        setupServlet(servletPath, queryString, null);
    }
    protected void setupServlet(String servletPath, String queryString, 
                                String outfile)
        throws IOException, OpenIDException
    {
        if (outfile == null) outfile = "response-msg.txt";
        outf = new File(wrkdir, outfile);
        req = new TestRequest(servletPath, "GET", queryString);
        resp = new TestResponse(outf);
        idreq = new IdRequest(mgr, req, resp);
    }

    protected void closeResponse() throws IOException {
        if (resp != null) {
            resp.close();
            resp = null;
        }
    }

    public String getContent(File results) throws IOException {
        String line = null;
        String[] parts = null;
        BufferedReader rdr = new BufferedReader(new FileReader(results));
        StringWriter out = new StringWriter();
        try {
            while ((line = rdr.readLine()) != null) {
                if (line.length() == 0) break;
            }
            while ((line = rdr.readLine()) != null) {
                out.write(line);
                out.write('\n');
            }
        } finally { 
            if (rdr != null) rdr.close(); 
            if (out != null) out.close();
        }

        return out.toString();
    }

    public Properties parsePropertyContent(File results) throws IOException {
        String line = null;
        String[] parts = null;
        BufferedReader rdr = new BufferedReader(new FileReader(results));
        Properties props = new Properties();
        try {
            while ((line = rdr.readLine()) != null) {
                if (line.length() == 0) break;
            }
            while ((line = rdr.readLine()) != null) {
                parts = line.split("\\s*:\\s*");
                props.setProperty(parts[0], parts[1]);
            }
        } finally { rdr.close(); }

        return props;
    }

    public MultiProperties parseQueryString(String qstring) 
        throws IOException 
    {
        MultiProperties out = new MultiProperties();
        String[] args = qstring.split("&");
        String[] parts = null;
        for (String arg : args) {
            arg = URLDecoder.decode(arg, "UTF-8");
            parts = qstring.split("=", 2);
            if (parts.length > 1) 
                out.add(parts[0], parts[1]);
            else 
                out.add(parts[0], "");
        }

        return out;
    }

    public MultiProperties parseHeader(File results, int expStatus) 
        throws IOException 
    {
        MultiProperties out = new MultiProperties();
        BufferedReader rdr = new BufferedReader(new FileReader(results));
        String line = null;

        // check the first line
        try {
            line = rdr.readLine();
            assertNotNull("Empty HTTP output", line);
            String[] parts = line.split("\\s+");
            assertEquals(3, parts.length);
            assertEquals("Bad HTTP header opener: "+line, "HTTP/1.1", parts[0]);
            assertEquals("Unexpected status: "+parts[1], 
                         Integer.toString(expStatus), parts[1]);
            assertTrue("Empty header message", parts[2].length() > 0);

            while ((line = rdr.readLine()) != null) {
                if (line.length() == 0) break;
                parts = line.split(":\\s*", 2);
                if (parts.length > 1) 
                    out.add(parts[0], parts[1]);
            }

            return out;
        }
        finally {
            rdr.close();
        }
    }

    public void setCookie(TestRequest req, String token) {
        HttpCookie cookie = new HttpCookie(SessionKit.SESSION_COOKIE_NAME, token);
        cookie.setMaxAge(5 * 365 * 24 * 3600);
        cookie.setSecure(true);
        req.addHeader("Cookie", cookie.toString());
    }
}
