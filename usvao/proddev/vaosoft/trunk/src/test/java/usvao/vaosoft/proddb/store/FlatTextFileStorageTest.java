package usvao.vaosoft.proddb.store;

import usvao.vaosoft.proddb.StackConfig;
import usvao.vaosoft.proddb.VAOSoft1StackConfig;
import usvao.vaosoft.proddb.ProductDB;
import usvao.vaosoft.proddb.InMemoryProductDB;
import usvao.vaosoft.proddb.Product;
import usvao.vaosoft.proddb.ProductDataIO;
import usvao.vaosoft.proddb.ConfigurationException;
import static usvao.vaosoft.proddb.LoadProduct.*;

import usvao.vaosoft.proddb.InMemoryProductDBTest;

import java.util.Properties;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class FlatTextFileStorageTest {

    File tmp = new File(System.getProperty("tmp.dir"));
    File home = new File(tmp, "vaosw");
    String sl = File.separator;
    VAOSoft1StackConfig sc = null;

    @Before public void setUp() throws IOException, ConfigurationException  {
        if (! home.exists()) home.mkdir();
        FileUtils.cleanDirectory(home);
        sc = makeConfig();
    }

    @After public void tearDown() throws IOException, ConfigurationException {
        // FileUtils.deleteDirectory(home);
    }

    VAOSoft1StackConfig makeConfig() throws ConfigurationException {
        return new VAOSoft1StackConfig(home, null);
    }

    ProductDB makeProductDB(StackConfig config) {
        return InMemoryProductDBTest.makeProductDB(config);
    }

    @Test public void testWriteNewDb() throws IOException {

        File src = sc.getProductDBPath(null);

        // make sure that we can successfully write over an existing
        // file; note that this will create it's parent directory if nec.
        FileUtils.touch(src);

        ProductDB pdb = makeProductDB(sc);
        ProductDataIO store = new FlatTextFileStorage(src);

        try {
            store.export(pdb.rawIterator());
        }
        catch (IOException ex) {
            fail("Problem writing db: " + ex);
        }
        assertTrue(src.exists());
        assertTrue(FileUtils.sizeOf(src) > 0);

        BufferedReader r = null;
        String line = null;
        HashMap<String, Integer> pcount = new HashMap<String, Integer>();
        pcount.put("vaosoft", 0);
        pcount.put("maven", 0);
        pcount.put("ivy", 0);
        pcount.put("ant", 0);

        try {
            r = new BufferedReader(new FileReader(src));
            while ((line = r.readLine()) != null) {
                String[] data = line.split(" ");
                assertTrue(data.length >= PROPS);
                assertTrue(pcount.containsKey(data[NAME]));
                pcount.put(data[NAME], pcount.get(data[NAME]) + 1);
            }
        }
        finally {
            r.close();
        }

        assertEquals(new Integer(5), pcount.get("vaosoft"));
        assertEquals(new Integer(1), pcount.get("ant"));
        assertEquals(new Integer(1), pcount.get("ivy"));
        assertEquals(new Integer(1), pcount.get("maven"));
    }

    @Test public void testRoundTripLoad() throws IOException {

        ProductDB pdb = makeProductDB(sc);
        File src = sc.getProductDBPath(null);
        ProductDataIO store = new FlatTextFileStorage(src);

        // ensure the parent directory exists
        File parent = src.getParentFile();
        if (! parent.exists()) parent.mkdirs();

        try {
            store.export(pdb.rawIterator());
        }
        catch (IOException ex) {
            fail("Problem writing db: " + ex);
        }
        assertTrue(src.exists());

        InMemoryProductDB mpdb = InMemoryProductDBTest.makeEmptyProductDB(sc);
        try {
            store.load(mpdb);
        } catch (IOException ex) {
            fail("Load IO failure: " + ex);
        }

        assertEquals(8, mpdb.getCount());
        assertNotNull(mpdb.getProduct("vaosoft", "1.0.0"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.0.1"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.0.5"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.1.0rc3"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.1.0"));
        assertNotNull(mpdb.getProduct("ivy", "2.2.0"));
        assertNotNull(mpdb.getProduct("ant", "1.8.2"));
        assertNotNull(mpdb.getProduct("maven", "3.0.3"));

        Product p = mpdb.getProduct("ivy", "2.2.0");
        assertEquals("bar", p.getExtProperty("foo"));
        assertEquals("gurn", p.getExtProperty("goob"));
        assertEquals("sec", p.getExtProperty("first"));
        assertEquals("goose", p.getExtProperty("duck"));
    }

    @Test public void testLoad() throws IOException {
        File src = sc.getProductDBPath(null);
        File tsrc = new File(System.getProperty("etc.dir"));
        tsrc = new File(tsrc, "test-productdb.txt");
        FileUtils.copyFile(tsrc, src);
        assertTrue(src.exists());

        ProductDataIO store = new FlatTextFileStorage(src);
        InMemoryProductDB mpdb = InMemoryProductDBTest.makeEmptyProductDB(sc);
        try {
            store.load(mpdb);
        } catch (IOException ex) {
            fail("Load IO failure: " + ex);
        }

        assertNull(mpdb.getProduct("vaosoft", "1.0.5"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.0.0"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.0.1"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.1.0rc3"));
        assertNotNull(mpdb.getProduct("vaosoft", "1.1.0"));
        assertNotNull(mpdb.getProduct("ivy", "2.2.0"));
        assertNotNull(mpdb.getProduct("ant", "1.8.2"));
        assertNotNull(mpdb.getProduct("maven", "3.0.3"));
        assertEquals(7, mpdb.getCount());

        Product p = mpdb.getProduct("ivy", "2.2.0");
        assertEquals("bar", p.getExtProperty("foo"));
        assertEquals("gurn", p.getExtProperty("goob"));
        assertEquals("sec", p.getExtProperty("first"));
        assertEquals("goose", p.getExtProperty("duck"));
    }

    @Test public void testCount() throws IOException {
        File src = sc.getProductDBPath(null);
        File tsrc = new File(System.getProperty("etc.dir"));
        tsrc = new File(tsrc, "test-productdb.txt");
        FileUtils.copyFile(tsrc, src);
        assertTrue(src.exists());

        ProductDataIO store = new FlatTextFileStorage(src);
        assertEquals(7, store.countProducts());
    }
}