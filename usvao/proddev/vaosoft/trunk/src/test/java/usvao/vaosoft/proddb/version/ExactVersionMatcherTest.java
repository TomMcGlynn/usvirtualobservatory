package usvao.vaosoft.proddb.version;

import usvao.vaosoft.proddb.VersionMatcher;

import java.util.Comparator;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the FieldComparator class
 */
public class ExactVersionMatcherTest {

    Comparator<String> vc = new VersionComparator();

    @Test public void test1() {
        VersionMatcher m = new ExactVersionMatcher("1", vc);
        assertSame(vc, m.getComparator());
        assertTrue(m.matches("1"));
        assertFalse(m.matches("0.2.4"));
        assertFalse(m.matches("1.0"));
        assertFalse(m.matches("1."));
        assertFalse(m.matches(".0"));
    }

    @Test public void test2() {
        VersionMatcher m = new ExactVersionMatcher("0.2.4", vc);
        assertSame(vc, m.getComparator());
        assertTrue(m.matches("0.2.4"));
        assertFalse(m.matches("1"));
        assertFalse(m.matches("1.0"));
        assertFalse(m.matches("0.2."));
        assertFalse(m.matches(".0"));
    }

    @Test public void test3() {
        VersionMatcher m = new ExactVersionMatcher("0.2.4rc3", vc);
        assertSame(vc, m.getComparator());
        assertTrue(m.matches("0.2.4rc3"));
        assertFalse(m.matches("0.2.4"));
        assertFalse(m.matches("1"));
        assertFalse(m.matches("1.0"));
        assertFalse(m.matches("0.2."));
        assertFalse(m.matches("0.2.rc3"));
        assertFalse(m.matches(".0"));
    }

}

