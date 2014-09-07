package org.usvao.service.servlet.sim;

import java.util.Enumeration;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * tests for the TestSession class
 */
public class TestSessionTestCase {

    TestSession sess = null;

    @Before
    public void setup() {
        sess = new TestSession();
    }

    @Test
    public void testGetId() {
        String id = sess.getId();
        assertNotNull(id);
    }

    @Test
    public void testAttributes() {
        Enumeration e = sess.getAttributeNames();
        assertFalse(e.hasMoreElements());

        Integer ti = new Integer(4);
        sess.setAttribute("num", ti);
        e = sess.getAttributeNames();
        int c = 0;
        boolean found = false;
        while(e.hasMoreElements()) { e.nextElement(); c++; }
        assertEquals(1, c);
        Object at = sess.getAttribute("num");
        assertNotNull(at);
        assertSame(ti, at);

        sess.removeAttribute("num");
        at = sess.getAttribute("num");
        assertNull(at);
    }

    @Test
    public void testIsNew() {
        assertFalse(sess.isNew());
    }

    @Test
    public void testInvalidate() {
        Enumeration e = sess.getAttributeNames();
        assertFalse(e.hasMoreElements());

        Integer ti = new Integer(4);
        sess.setAttribute("num", ti);
        e = sess.getAttributeNames();
        assertTrue(e.hasMoreElements());

        sess.invalidate();
        e = sess.getAttributeNames();
        assertFalse(e.hasMoreElements());
    }
}