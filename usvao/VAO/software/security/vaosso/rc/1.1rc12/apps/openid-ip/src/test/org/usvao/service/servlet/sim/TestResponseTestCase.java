package org.usvao.service.servlet.sim;

import java.util.Collection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * tests for the TestResponse class
 */
public class TestResponseTestCase {
    TestResponse resp = null;
    File outf = new File(System.getProperty("test.outdir"), "testresponse.txt");

    @Before
    public void setup() throws IOException {
        resp = new TestResponse(outf.toString());
    }

    @Test
    public void testCtor() {
        assertEquals("Unexpected Server header", 
                     "servlet-tester/1.1", resp.getHeader("Server"));
        String val = resp.getHeader("Date");
        assertNotNull("Null Date header", val);
        assertTrue("empty Date header", val.length() > 0);

        assertFalse(resp.isCommitted());
    }

    @Test
    public void testHeader() {
        String[] vals = null;
        Collection<String> valset = null;

        resp.containsHeader("Date");
        resp.containsHeader("Server");
        valset = resp.getHeaderNames();
        assertEquals(2, valset.size());
        valset.contains("Date");
        valset.contains("Server");

        assertFalse(resp.containsHeader("Location"));
        assertNull(resp.getHeader("Location"));
        resp.setHeader("Location", "http://www.google.com/");
        assertTrue(resp.containsHeader("Location"));
        assertEquals("http://www.google.com/", resp.getHeader("Location"));
        resp.addHeader("Location", "http://www.facebook.com/");
        assertTrue(resp.containsHeader("Location"));
        assertEquals("http://www.google.com/", resp.getHeader("Location"));

        assertFalse(resp.containsHeader("Goober"));
        resp.addHeader("Goober", "gurn");
        assertTrue(resp.containsHeader("Goober"));
        assertFalse(resp.containsHeader("Number"));
        resp.setIntHeader("Number", 2);
        assertTrue(resp.containsHeader("Number"));
        resp.addIntHeader("Number", 3);
        assertTrue(resp.containsHeader("Number"));

        valset = resp.getHeaderNames();
        assertEquals(5, valset.size());
        valset.contains("Date");
        valset.contains("Server");
        valset.contains("Number");
        valset.contains("Location");
        valset.contains("Goober");

        valset = resp.getHeaders("Location");
        assertEquals(2, valset.size());
        assertTrue(valset.contains("http://www.google.com/"));
        assertTrue(valset.contains("http://www.facebook.com/"));
        vals = resp.getHeaderValues("Location");
        assertEquals(2, vals.length);
        assertEquals(vals[0], "http://www.google.com/");
        assertEquals(vals[1], "http://www.facebook.com/");

        valset = resp.getHeaders("Number");
        assertTrue(valset.contains("3"));
        assertTrue(valset.contains("2"));
    }

    @Test
    public void testStatus() {
        assertEquals(200, resp.getStatus());
        resp.setStatus(330);
        assertEquals(330, resp.getStatus());
        resp.setStatus(400, "blah");
        assertEquals(400, resp.getStatus());
    }

    @Test
    public void testContentLength() {
        assertEquals(-1, resp.getContentLength());
        assertFalse(resp.containsHeader("Content-Length"));
        resp.setContentLength(1096);
        assertTrue(resp.containsHeader("Content-Length"));
        assertEquals(1096, resp.getContentLength());
        assertEquals("1096", resp.getHeader("Content-Length"));
    }

    @Test
    public void testContentType() {
        assertNull(resp.getContentType());
        assertFalse(resp.containsHeader("Content-type"));

        resp.setContentType("text/html");
        assertTrue(resp.containsHeader("Content-Type"));
        assertEquals("text/html", resp.getContentType());
        assertEquals("text/html", resp.getHeader("Content-Type"));
    }

    @Test
    public void testGetResponse() {
        assertSame(resp, resp.getResponse());
    }

    @Test
    public void testCookies() {
        assertFalse(resp.containsHeader("Set-Cookie"));

        Cookie cook = new Cookie("session", "123456789abcdef");
        cook.setDomain("example.net");
        cook.setPath("/request");

        resp.addCookie(cook);
        assertTrue(resp.containsHeader("Set-Cookie"));
        assertEquals(1, resp.getHeaders("Set-Cookie").size());
        assertTrue(resp.getHeader("Set-Cookie").startsWith("session="));

        cook = new Cookie("goober", "gurn");
        cook.setDomain("example.net");
        cook.setPath("/request");

        resp.addCookie(cook);
        assertTrue(resp.containsHeader("Set-Cookie"));
        assertEquals(2, resp.getHeaders("Set-Cookie").size());
        assertTrue(resp.getHeaderValues("Set-Cookie")[1].startsWith("goober="));
    }

    @Test
    public void testWriter() throws IOException {
        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();
        pw.println("Hello world");

        pw.flush();
        pw.close();

        assertTrue(resp.isCommitted());
        
        String text = getResponseText();
        assertTrue(text.length() > 0);
        assertTrue(text.startsWith("HTTP/1.1 200 OK\n"));
        assertTrue(text.endsWith("\n\nHello world\n"));
        assertTrue(text.contains("\nDate: "));
        assertTrue(text.contains("\nServer: servlet-tester/1.1\n"));
        assertTrue(text.contains("\nContent-Type: text/plain\n"));
    }

    @Test
    public void testSendError() throws IOException {
        resp.sendError(404, "Missing");

        assertTrue(resp.isCommitted());
        
        String text = getResponseText();
        assertTrue(text.length() > 0);
        String[] lines = text.split("\n");
        assertTrue(text.startsWith("HTTP/1.1 404 Missing\n"));
        assertTrue(text.endsWith("\n\n"));
    }

    @Test
    public void testSendRedirect() throws IOException {
        resp.sendRedirect("http://www.gooble.com");

        assertTrue(resp.isCommitted());
        
        String text = getResponseText();
        assertTrue(text.length() > 0);
        String[] lines = text.split("\n");
        // assertEquals("HTTP/1.1 302 Redirecting\n", text);
        assertTrue(text.startsWith("HTTP/1.1 302 Redirecting\n"));
        assertTrue(text.contains("\nLocation: http://www.gooble.com\n"));
        assertTrue(text.endsWith("\n\n"));
    }

    public String getResponseText() throws IOException {
        assertTrue(outf.exists());

        char[] buf = new char[8192];
        int n = 0;
        FileReader rdr = new FileReader(outf);
        StringWriter wr = new StringWriter();
        while((n = rdr.read(buf)) >= 0) 
            wr.write(buf, 0, n);
        rdr.close();
        wr.close();
        return wr.toString();
    }


}