package usvao.vaosoft.proddb.version;

import usvao.vaosoft.proddb.VersionRangeMatcher;
import usvao.vaosoft.proddb.VersionHandler;
import usvao.vaosoft.proddb.VersionMatcher;

import java.util.Comparator;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the BasicVersionHandler class
 */
public class BasicVersionHandlerTest {

    VersionHandler vh = new BasicVersionHandler();

    @Test public void testCmp() {
        assertNotNull(vh.getComparator());
    }

    @Test public void testRange1() {
        VersionMatcher vm = vh.getMatcher("[1.0,2.0]");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0", m.getMinVersion());
        assertEquals("2.0", m.getMaxVersion());
        assertTrue(m.isInclusiveMin());
        assertTrue(m.isInclusiveMax());
    }

    @Test public void testRange2() {
        VersionMatcher vm = vh.getMatcher("[1,2 ]");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1", m.getMinVersion());
        assertEquals("2", m.getMaxVersion());
        assertTrue(m.isInclusiveMin());
        assertTrue(m.isInclusiveMax());
    }

    @Test public void testRange3() {
        VersionMatcher vm = vh.getMatcher("[ 1.0,2.0rc1[");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0", m.getMinVersion());
        assertEquals("2.0rc1", m.getMaxVersion());
        assertTrue(m.isInclusiveMin());
        assertFalse(m.isInclusiveMax());
    }

    @Test public void testRange4() {
        VersionMatcher vm = vh.getMatcher("]1.0,2.0]");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0", m.getMinVersion());
        assertEquals("2.0", m.getMaxVersion());
        assertFalse(m.isInclusiveMin());
        assertTrue(m.isInclusiveMax());
    }

    @Test public void testRange5() {
        VersionMatcher vm = vh.getMatcher("]1.0, 2.0[");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0", m.getMinVersion());
        assertEquals("2.0", m.getMaxVersion());
        assertFalse(m.isInclusiveMin());
        assertFalse(m.isInclusiveMax());
    }

    @Test public void testRange6() {
        VersionMatcher vm = vh.getMatcher("(,2.0]");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertNull(m.getMinVersion());
        assertEquals("2.0", m.getMaxVersion());
        assertTrue(m.isInclusiveMax());
    }

    @Test public void testRange7() {
        VersionMatcher vm = vh.getMatcher("]1.0,)");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0", m.getMinVersion());
        assertNull(m.getMaxVersion());
        assertFalse(m.isInclusiveMin());
    }

    @Test public void testRange8() {
        VersionMatcher vm = vh.getMatcher("1.0rc3.+");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0rc3", m.getMinVersion());
        assertEquals("1.1", m.getMaxVersion());
        assertTrue(m.isInclusiveMin());
        assertFalse(m.isInclusiveMax());
    }

    @Test public void testRange9() {
        VersionMatcher vm = vh.getMatcher("1.rc3.+");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.rc3", m.getMinVersion());
        assertEquals("1.1", m.getMaxVersion());
        assertTrue(m.isInclusiveMin());
        assertFalse(m.isInclusiveMax());
    }

    @Test public void testRange10() {
        VersionMatcher vm = vh.getMatcher("1.0rc3+");
        assertTrue(vm instanceof VersionRangeMatcher);
        VersionRangeMatcher m = (VersionRangeMatcher) vm;
        assertEquals("1.0rc3", m.getMinVersion());
        assertNull(m.getMaxVersion());
        assertTrue(m.isInclusiveMin());
    }

    @Test(expected=java.lang.IllegalArgumentException.class) 
    public void testBadRange1() {
        VersionMatcher vm = vh.getMatcher("[1.0,4.9)");
    }

    @Test(expected=java.lang.IllegalArgumentException.class) 
    public void testBadRange2() {
        VersionMatcher vm = vh.getMatcher("(1.0,4.9]");
    }

    @Test(expected=java.lang.IllegalArgumentException.class) 
    public void testBadRange3() {
        VersionMatcher vm = vh.getMatcher("1.0,4.9");
    }

    @Test(expected=java.lang.IllegalArgumentException.class) 
    public void testBadRange4() {
        VersionMatcher vm = vh.getMatcher("[1.0, [");
    }

    @Test(expected=java.lang.IllegalArgumentException.class) 
    public void testBadRange5() {
        VersionMatcher vm = vh.getMatcher("[1.0,(");
    }

    @Test(expected=java.lang.IllegalArgumentException.class) 
    public void testBadRange6() {
        VersionMatcher vm = vh.getMatcher("),1.0[");
    }
}