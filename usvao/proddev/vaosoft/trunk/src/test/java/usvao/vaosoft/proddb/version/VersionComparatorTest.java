package usvao.vaosoft.proddb.version;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the VersionComparator class
 */
public class VersionComparatorTest {

    VersionComparator vc = new VersionComparator();

    @Test public void testEarlier1() {
        assertTrue(vc.compare("1", "2") < 0);
        assertTrue(vc.compare("a", "b") < 0);
        assertTrue(vc.compare("1a", "1b") < 0);
        assertTrue(vc.compare("1a48", "1b46") < 0);
        assertTrue(vc.compare("1a44", "1a46") < 0);
        assertTrue(vc.compare("1b44", "1+26") < 0);
        assertTrue(vc.compare("1a", "1") < 0);
        assertTrue(vc.compare("1beta", "1") < 0);
        assertTrue(vc.compare("1alpha2", "2beta") < 0);
        assertTrue(vc.compare("1", "1+") < 0);
        assertTrue(vc.compare("1+", "16") < 0);
        assertTrue(vc.compare("1+1", "1+2") < 0);
    }

    @Test public void testEarlier2() {
        assertTrue(vc.compare("1.1", "1.2") < 0);
        assertTrue(vc.compare("1", "1.2") < 0);
        assertTrue(vc.compare("1.1", "2") < 0);
        assertTrue(vc.compare("1.1", "2.4") < 0);
        assertTrue(vc.compare("1.1", "2.4.3") < 0);
        assertTrue(vc.compare("1.1.", "2.4.3") < 0);
        assertTrue(vc.compare("2.4.1", "2.4.3") < 0);
        assertTrue(vc.compare("1.1a", "1.1b") < 0);
        assertTrue(vc.compare("1.1a3", "1.1b3") < 0);
        assertTrue(vc.compare("1.1a2", "1.1a3") < 0);
        assertTrue(vc.compare("1.1a2rc3", "1.1a3rc1") < 0);
        assertTrue(vc.compare("1.1a3rc1", "1.1a3rc3") < 0);
        assertTrue(vc.compare("1.1a3rc1", "1.1+3rc3") < 0);
    }

    @Test public void testLater1() {
        assertTrue(vc.compare("2"    , "1"      ) > 0);
        assertTrue(vc.compare("b"    , "a"      ) > 0);
        assertTrue(vc.compare("1b"   , "1a"     ) > 0);
        assertTrue(vc.compare("1b46" , "1a48"   ) > 0);
        assertTrue(vc.compare("1a46" , "1a44"   ) > 0);
        assertTrue(vc.compare("1+26" , "1b44"   ) > 0);
        assertTrue(vc.compare("1"    , "1a"     ) > 0);
        assertTrue(vc.compare("1"    , "1beta"  ) > 0);
        assertTrue(vc.compare("2beta", "1alpha2") > 0);
        assertTrue(vc.compare("1+"   , "1"      ) > 0);
        assertTrue(vc.compare("16"   , "1+"     ) > 0);
        assertTrue(vc.compare("1+2"  , "1+1"    ) > 0);
    }

    @Test public void testLater2() {
        assertTrue(vc.compare("1.2"     , "1.1"     ) > 0);
        assertTrue(vc.compare("1.2"     , "1"       ) > 0);
        assertTrue(vc.compare("2"       , "1.1"     ) > 0);
        assertTrue(vc.compare("2.4"     , "1.1"     ) > 0);
        assertTrue(vc.compare("2.4.3"   , "1.1"     ) > 0);
        assertTrue(vc.compare("2.4.3"   , "1.1. "   ) > 0);
        assertTrue(vc.compare("2.4.3"   , "2.4.1"   ) > 0);
        assertTrue(vc.compare("1.1b"    , "1.1a"    ) > 0);
        assertTrue(vc.compare("1.1b3"   , "1.1a3"   ) > 0);
        assertTrue(vc.compare("1.1a3"   , "1.1a2"   ) > 0);
        assertTrue(vc.compare("1.1a3rc1", "1.1a2rc3") > 0);
        assertTrue(vc.compare("1.1a3rc3", "1.1a3rc1") > 0);
        assertTrue(vc.compare("1.1+3rc3", "1.1a3rc1") > 0);
    }

    @Test public void testEquals1() {
        assertTrue(vc.compare("2"    , "2"      ) == 0);
        assertTrue(vc.compare("b"    , "b"      ) == 0);
        assertTrue(vc.compare("1b"   , "1b"     ) == 0);
        assertTrue(vc.compare("1b46" , "1b46"   ) == 0);
        assertTrue(vc.compare("1+26" , "1+26"   ) == 0);
        assertTrue(vc.compare("1beta", "1beta"  ) == 0);
        assertTrue(vc.compare("2beta2","2beta2" ) == 0);
        assertTrue(vc.compare("1+"   , "1+"     ) == 0);
        assertTrue(vc.compare("1+1"  , "1+1"    ) == 0);
    }

    @Test public void testEqual2() {
        assertTrue(vc.compare("1.2"     , "1.2"     ) == 0);
        assertTrue(vc.compare("2.4"     , "2.4"     ) == 0);
        assertTrue(vc.compare("2.4.3"   , "2.4.3"   ) == 0);
        assertTrue(vc.compare("1.1a"    , "1.1a"    ) == 0);
        assertTrue(vc.compare("1.1a3"   , "1.1a3"   ) == 0);
        assertTrue(vc.compare("1.1a3rc1", "1.1a3rc1") == 0);
    }


}

