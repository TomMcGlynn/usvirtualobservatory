package org.usvao.sso.openid.portal;

import java.util.Date;
import java.util.Collection;
import java.util.TreeSet;
import static java.lang.String.format;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class PortalSessionTest {

    @Test
    public void testProps() {
        Date startDate = new Date(113, 2, 10, 14, 35);
        PortalSession sess = new PortalSession(startDate, 600);

        assertEquals(startDate.getTime() / 1000, sess.getLifeRefTime());
        // assertEquals(startDate.getTime(), sess.getStartDate().getTime());
        // assertTrue(startDate.equals(sess.getStartDate()));
        assertEquals(600, sess.getLifetime());

        sess = new PortalSession(startDate.getTime() / 1000, 600);

        assertEquals(startDate.getTime() / 1000, sess.getLifeRefTime());
        // assertEquals(startDate.getTime(), sess.getStartDate().getTime());
        // assertTrue(startDate.equals(sess.getStartDate()));
        assertEquals(600, sess.getLifetime());
    }

    @Test
    public void testExpired() {
        Date startDate = new Date(113, 2, 10, 14, 35);
        PortalSession sess = new PortalSession(startDate.getTime(), 600);
        assertEquals(0, sess.getTimeLeftSec());
        assertEquals(0.0, sess.getTimeLeftMin(), 0.0);
        assertTrue(sess.isExpired());
    }

    @Test
    public void testTimeLeft() {
        Date startDate = new Date(); // now
        PortalSession sess = new PortalSession(startDate, 600);
        long lefts = sess.getTimeLeftSec();
        double leftm = sess.getTimeLeftMin();
        assertTrue("time left calculation seems off by " + 
                   (new Long(600-lefts)).toString() + " secs.",
                   600-lefts < 2);
        assertTrue("time left calculation seems off by " + 
                   (new Double(10.0-leftm)).toString() + " mins.",
                   10.0-leftm < 1.0);
        assertFalse(sess.isExpired());
    }

    @Test
    public void testFormat() {
        assertEquals("0:22", PortalSession.formatTimeLeft(22.0));
        assertEquals("0:22", PortalSession.formatTimeLeft(22.7));
        assertEquals("2:22", PortalSession.formatTimeLeft(142.7));
        assertEquals("12:22", PortalSession.formatTimeLeft(742.7));
        assertEquals("102:22", PortalSession.formatTimeLeft(6142.7));
    }
}
