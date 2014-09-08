package usvao.vaosoft.proddb;

import java.util.Properties;
import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the VAOSoft1StackConfig class
 */
public class VAOSoft1StackConfigTest {

    File tmp = new File(System.getProperty("tmp.dir"));
    File home = new File(tmp, "vaosw");
    String sl = File.separator;
        
    @Test(expected=IllegalArgumentException.class) 
    public void testCtorBadHome1() throws ConfigurationException {
        StackConfig sc = new VAOSoft1StackConfig(null, null);
    }

    @Test(expected=IllegalArgumentException.class) 
    public void testCtorBadHome2() throws ConfigurationException {
        File home = new File("build.xml");
        assertTrue(home.exists());
        StackConfig sc = new VAOSoft1StackConfig(home, null);
    }

    @Test public void testCtor1() throws ConfigurationException {
        StackConfig sc = new VAOSoft1StackConfig(home, null);
        testDirs(sc, home, null);

        Properties props = new Properties();
        props.setProperty("goob",  "foo");
        sc = new VAOSoft1StackConfig(home, props);
        testDirs(sc, home, null);

        File pdir = new File(home, "pkgs");
        props.setProperty("products.dir", pdir.toString());
        sc = new VAOSoft1StackConfig(home, props);
        testDirs(sc, home, pdir);
    }

    void testDirs(StackConfig sc, File home, File proddir) {
        assertEquals(home, sc.getHome());
        if (proddir == null) 
            proddir = new File(home, "products");
        assertEquals(proddir, sc.getProductRoot(null));
    }

    @Test public void testGetDefProductHome() throws ConfigurationException {
        StackConfig sc = new VAOSoft1StackConfig(home, null);
        Product prod = new BasicProduct("vaosoft", "1.0", "usvao", null);
        File phome = sc.getDefProductHome(prod);
        char sl = File.separatorChar;
        File exp = new File(sc.getProductRoot(null), 
                            "usvao"+sl+"vaosoft"+sl+"1.0");
        assertEquals(phome, exp);
    }

    @Test public void testVersionHandler() throws ConfigurationException {
        StackConfig sc = new VAOSoft1StackConfig(home, null);
        VersionHandler vh = sc.getVersionHandler();
        assertNotNull(vh);
        assertNotNull(vh.getComparator());
        assertNotNull(vh.getMatcher("1.3.5"));
    }

    @Test public void testDataStore() throws ConfigurationException {
        StackConfig sc = new VAOSoft1StackConfig(home, null);
        assertNotNull(sc.getDataStore());
    }
}