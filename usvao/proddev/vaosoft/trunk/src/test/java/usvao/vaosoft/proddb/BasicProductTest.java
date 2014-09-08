package usvao.vaosoft.proddb;

import java.util.Properties;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the BasicProduct class
 */
public class BasicProductTest {

    File tmp = new File(System.getProperty("tmp.dir"));
    File home = new File(tmp, "vaosw");
    String sl = File.separator;
    StackConfig sc = null;
    BasicProduct p = null;

    @Before public void setUp() throws ConfigurationException {
        sc = StackConfig.createStackConfig(home);
        p = new BasicProduct("vaosoft", "1.0", "usvao", null, "etc", sc, null);
    }

    @Test public void testGetPlatform() {
        assertNotNull(p.getPlatform());
    }

    @Test public void testGetHome() {
        File phome = p.getHome();
        File exp = new File(sc.getProductRoot(p.getPlatform()), 
                            "usvao"+sl+"vaosoft"+sl+"1.0");
        assertEquals(exp, phome);
    }

    @Test public void testIsInstalled() throws IOException {
        assertFalse(p.isInstalled());

        File proof = new File(p.getHome(), "etc");
        assertEquals(proof, p.proofFile);
        proof.mkdirs();
        assertTrue(proof.exists());
        assertTrue(p.isInstalled());
        FileUtils.deleteDirectory(sc.getHome());
    }

    @Test public void testGetOrg() {
        assertEquals("usvao", p.getOrg());
    }

    @Test public void testGetName() {
        assertEquals("vaosoft", p.getName());
    }

    @Test public void testGetVersion() {
        assertEquals("1.0", p.getVersion());
    }

    @Test public void testMakeProduct() {
        String[] data = { "vaosofty", "1.0", "usvao", null, "etc", 
                          "foo=bar\tgoob=gurn\tloose=" };
        p = BasicProduct.makeProduct(data, sc);
        assertEquals("vaosofty", p.getName());
        assertEquals("1.0", p.getVersion());
        assertEquals("usvao", p.getOrg());
        File phome = p.getHome();
        File exp = new File(sc.getProductRoot(p.getPlatform()), 
                            "usvao"+sl+"vaosofty"+sl+"1.0");
        assertEquals(exp, phome);
        assertEquals(new File(phome, "etc"), p.proofFile);
        assertEquals("bar", p.getExtProperty("foo"));
        assertEquals("gurn", p.getExtProperty("goob"));
        assertEquals("", p.getExtProperty("loose"));

    }

}
