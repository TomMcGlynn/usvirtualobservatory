package usvao.vaosoft.proddb;

import usvao.vaosoft.proddb.version.BasicVersionHandler;
import static usvao.vaosoft.proddb.LoadProduct.*;

import java.util.Properties;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the PersistedProductDBTest class
 */
public class PersistedProductDBTest {

    File tmp = new File(System.getProperty("tmp.dir"));
    File home = new File(tmp, "vaosw");
    File tsrc = new File(System.getProperty("etc.dir"));
    StackConfig sc = null;
    ProductDB pdb = null;

    public PersistedProductDBTest() throws ConfigurationException {
        sc = new VAOSoft1StackConfig(home, null);
    }

    @Before public void setUp() throws IOException {
        if (! home.exists()) home.mkdir();
        tsrc = new File(tsrc, "test-productdb.txt");
        FileUtils.copyFile(tsrc, new File(home,"productdb.txt"));
        
        pdb = new PersistedProductDB(sc, null);
    }

    @Test public void testCount() {
        assertEquals(7, pdb.getCount());
    }

    @Test public void testCache() throws IOException {
        ProductDB cached = ((PersistedProductDB) pdb).cache();
        assertNotNull(cached);
        assertEquals(7, cached.getCount());
        assertTrue(cached instanceof InMemoryProductDB);
    }

    @Test public void testMatchProducts() {
        ProductDB db = pdb.matchProducts("goob", "[1.0,1.1[", null);
        assertNotNull(db);
        assertEquals(0, db.getCount());

        db = pdb.matchProducts("vaosoft", "[1.0,1.1[", null);
        assertNotNull(db);
        assertEquals(2, db.getCount());

        db = pdb.matchProducts("ivy", "1.0+", null);
        assertNotNull(db);
        assertEquals(1, db.getCount());

        db = pdb.matchProducts("ivy", "3.0+", null);
        assertNotNull(db);
        assertEquals(0, db.getCount());
    }

    @Test public void testGetProduct() {
        Product p = pdb.getProduct("vaosoft", "1.0.1");
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.1", p.version);
        assertEquals("usvao", p.org);
        assertTrue(p.getHome().toString().endsWith("/products/usvao/vaosoft/1.0.1"));
        assertFalse(p.isInstalled());

        p = pdb.getProduct("maven", "2.0.1");
        assertNull(p);

        p = pdb.getProduct("maven", "3.0.3");
        assertNotNull(p);
        assertEquals("maven", p.name);
        assertEquals("3.0.3", p.version);
        assertEquals("apache", p.org);
        assertFalse(p.isInstalled());
    }

    @Test public void testIterator() {
        HashMap<String, Integer> pcount = new HashMap<String, Integer>();
        pcount.put("vaosoft", 0);
        pcount.put("maven", 0);
        pcount.put("ivy", 0);
        pcount.put("ant", 0);

        Iterator<String[]> it = pdb.rawIterator();
        assertTrue(it.hasNext());
        while (it.hasNext()) {
            String[] data = it.next();
            assertNotNull(data);
            assertTrue(data.length > PROPS);
            assertTrue(pcount.containsKey(data[NAME]));
            pcount.put(data[NAME], pcount.get(data[NAME]) + 1);
        }

        assertEquals(new Integer(4), pcount.get("vaosoft"));
        assertEquals(new Integer(1), pcount.get("ant"));
        assertEquals(new Integer(1), pcount.get("ivy"));
        assertEquals(new Integer(1), pcount.get("maven"));
    }


}
