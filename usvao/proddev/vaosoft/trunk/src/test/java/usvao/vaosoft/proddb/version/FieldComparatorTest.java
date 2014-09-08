package usvao.vaosoft.proddb.version;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the FieldComparator class
 */
public class FieldComparatorTest {

    FieldComparator fc = new FieldComparator();

    @Test public void testEarlier() {
        assertTrue(fc.compare("1", "2") < 0);
        assertTrue(fc.compare("a", "b") < 0);
        assertTrue(fc.compare("1a", "1b") < 0);
        assertTrue(fc.compare("1a48", "1b46") < 0);
        assertTrue(fc.compare("1a44", "1a46") < 0);
        assertTrue(fc.compare("1b44", "1+26") < 0);
        assertTrue(fc.compare("1a", "1") < 0);
        assertTrue(fc.compare("1beta", "1") < 0);
        assertTrue(fc.compare("1alpha2", "2beta") < 0);
        assertTrue(fc.compare("1", "1+") < 0);
        assertTrue(fc.compare("1+", "16") < 0);
        assertTrue(fc.compare("1+1", "1+2") < 0);
        assertTrue(fc.compare("1+a1", "1+2") < 0);
        assertTrue(fc.compare("1a2rc3", "1a3rc1") < 0);
        assertTrue(fc.compare("1a3rc1", "1a3rc3") < 0);
        assertTrue(fc.compare("1a3rc1", "1+3rc3") < 0);
    }

    @Test public void testLater() {
        assertTrue(fc.compare("2"    , "1"      ) > 0);
        assertTrue(fc.compare("b"    , "a"      ) > 0);
        assertTrue(fc.compare("1b"   , "1a"     ) > 0);
        assertTrue(fc.compare("1b46" , "1a48"   ) > 0);
        assertTrue(fc.compare("1a46" , "1a44"   ) > 0);
        assertTrue(fc.compare("1+26" , "1b44"   ) > 0);
        assertTrue(fc.compare("1"    , "1a"     ) > 0);
        assertTrue(fc.compare("1"    , "1beta"  ) > 0);
        assertTrue(fc.compare("2beta", "1alpha2") > 0);
        assertTrue(fc.compare("1+"   , "1"      ) > 0);
        assertTrue(fc.compare("16"   , "1+"     ) > 0);
        assertTrue(fc.compare("1+2"  , "1+1"    ) > 0);
    }

    @Test public void testEquals() {
        assertTrue(fc.compare("2"    , "2"      ) == 0);
        assertTrue(fc.compare("b"    , "b"      ) == 0);
        assertTrue(fc.compare("1b"   , "1b"     ) == 0);
        assertTrue(fc.compare("1b46" , "1b46"   ) == 0);
        assertTrue(fc.compare("1+26" , "1+26"   ) == 0);
        assertTrue(fc.compare("1beta", "1beta"  ) == 0);
        assertTrue(fc.compare("2beta2","2beta2" ) == 0);
        assertTrue(fc.compare("1+"   , "1+"     ) == 0);
        assertTrue(fc.compare("1+1"  , "1+1"    ) == 0);
    }

}

