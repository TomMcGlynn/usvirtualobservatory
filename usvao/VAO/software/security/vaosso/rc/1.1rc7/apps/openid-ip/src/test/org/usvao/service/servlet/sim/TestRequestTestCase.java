package org.usvao.service.servlet.sim;

import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * tests for the TestRequest class
 */
public class TestRequestTestCase {
    TestRequest req = null;

    @Before
    public void setup() {
        req = new TestRequest("/request/my/res",
                              "GET", "goob=gurn&foo=bar&foo=finn");
    }

    @Test
    public void testGetSession() {
        HttpSession sess = req.getSession(false);
        assertNotNull(sess);
        assertSame(sess, req.getSession());
        assertNotSame(sess, req.getSession(true));
    }

    @Test
    public void testCtor1() {
        assertEquals("goob=gurn&foo=bar&foo=finn", req.getQueryString());
        assertEquals("/my/res", req.getPathInfo());
        assertEquals("/request", req.getServletPath());
        assertEquals("http", req.getScheme());
        assertEquals("example.net", req.getServerName());
        assertEquals(80, req.getServerPort());
        assertEquals("GET", req.getMethod());
        assertEquals("example.net", req.getHeader("Host"));
        assertEquals(req.defaultReferer, req.getHeader("Referer"));
        assertEquals("HTTP/1.1", req.getProtocol());
        assertEquals("http://example.net/app/request/my/res", 
                     req.getRequestURL().toString());
        assertEquals("/app/request/my/res", req.getRequestURI());
    }

    @Test
    public void testCtor2() {
        req = new TestRequest("https://exsso.usvao.org/wapp", "/openid",
                              "GET", null);

        assertEquals("", req.getQueryString());
        assertEquals("", req.getPathInfo());
        assertEquals("/openid", req.getServletPath());
        assertEquals("https", req.getScheme());
        assertEquals("exsso.usvao.org", req.getServerName());
        assertEquals(443, req.getServerPort());
        assertEquals("GET", req.getMethod());
        assertEquals("exsso.usvao.org", req.getHeader("Host"));
        assertEquals(req.defaultReferer, req.getHeader("Referer"));
        assertEquals("HTTP/1.1", req.getProtocol());
        assertEquals("https://exsso.usvao.org/wapp/openid",
                     req.getRequestURL().toString());
        assertEquals("/wapp/openid", req.getRequestURI());
    }

    public void testParameters() {
        Map params = req.getParameterMap();
        assertNotNull(params);
        assertEquals(2, params.size());

        Enumeration e = req.getParameterNames();
        assertNotNull(e);
        assertTrue(e.hasMoreElements());
        String val = (String) e.nextElement();
        assertTrue("goober".equals(val) || "foo".equals(val));
        val = (String) e.nextElement();
        assertTrue("goober".equals(val) || "foo".equals(val));
        assertFalse(e.hasMoreElements());

        String[] vals = (String[]) params.get("goober");
        assertNotNull(vals);
        assertEquals(1, vals.length);
        assertEquals("gurn", vals[0]);
        assertArrayEquals(vals, req.getParameterValues("goober"));
        assertEquals("gurn", req.getParameter("goober"));

        vals = (String[]) params.get("foo");
        assertNotNull(vals);
        assertEquals(2, vals.length);
        assertEquals("bar", vals[0]);
        assertEquals("finn", vals[1]);
        assertArrayEquals(vals, req.getParameterValues("foo"));
        assertEquals("bar", req.getParameter("foo"));
    }
}
