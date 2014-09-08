package usvao.vaosoft.proddb;

import usvao.vaosoft.proddb.version.BasicVersionHandler;
import static usvao.vaosoft.proddb.LoadProduct.*;

import java.util.Properties;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.HashMap;
import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the InMemoryProductDBTest class
 */
public class InMemoryProductDBTest {

    ProductDB pdb = null;

    public InMemoryProductDBTest() throws ConfigurationException {
        StackConfig config = new VAOSoft1StackConfig(new File("/sw/vao"), null);
        pdb = makeProductDB(config);
    }

    public static InMemoryProductDB makeEmptyProductDB(StackConfig config) {
        return new InMemoryProductDB(config);
    }

    public static ProductDB makeProductDB(StackConfig config) {
        InMemoryProductDB mpdb = makeEmptyProductDB(config);

        BasicProductLine bpl = 
            new BasicProductLine("vaosoft", "usvao", null, config);
        bpl.addVersion("1.0.0", null, "etc", null );
        bpl.addVersion("1.0.1", null, "etc", null );
        bpl.addVersion("1.0.5", null, "etc", null );
        bpl.addVersion("1.1.0rc3", null, "etc", null);
        bpl.addVersion("1.1.0", null, "etc", null);
        bpl.tagVersion("stable", "1.0.5");
        mpdb.addProductLine(bpl);

        bpl = new BasicProductLine("ant", "apache", null, config);
        bpl.addVersion("1.8.2", null, "lib/ant.jar", null );
        mpdb.addProductLine(bpl);

        String[] data1 = { "ivy", "2.2.0", "apache", null, 
                           "ivy-2.2.0.jar", null, null, 
                           "foo=bar\tgoob=gurn", "first=sec\tduck=goose" };
        mpdb.loadProduct(data1);

        String[] data2 = { "maven", "3.0.3", "apache", null, 
                           "lib/maven-core-3.0.3.jar", null };
        mpdb.loadProduct(data2);

        return mpdb;
    }

    @Test public void testGetCount() {
        assertEquals(8, pdb.getCount());
        ProductDB p = new InMemoryProductDB(pdb.config);
        assertEquals(0, p.getCount());
    }
    
    @Test public void testMatchProducts() {
        ProductDB db = pdb.matchProducts("goob", "[1.0,1.1[", null);
        assertNotNull(db);
        assertEquals(0, db.getCount());

        db = pdb.matchProducts("vaosoft", "[1.0,1.1[", null);
        assertNotNull(db);
        assertEquals(3, db.getCount());

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
        assertEquals("/sw/vao/products/usvao/vaosoft/1.0.1", 
                     p.getHome().toString());
        assertFalse(p.isInstalled());

        p = pdb.getProduct("maven", "2.0.1");
        assertNull(p);

        p = pdb.getProduct("maven", "3.0.3");
        assertNotNull(p);
        assertEquals("maven", p.name);
        assertEquals("3.0.3", p.version);
        assertEquals("apache", p.org);
        assertEquals("/sw/vao/products/apache/maven/3.0.3", 
                     p.getHome().toString());
        assertFalse(p.isInstalled());
    }

    @Test public void testGetProductByTag() {
        Product p = pdb.getProductByTag("goob", "latest", null);
        assertNull(p);

        p = pdb.getProductByTag("vaosoft", "stable", null);
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.5", p.version);
    }

    @Test public void testSelectProduct() {
        Product p = pdb.selectProduct("goob", "latest", (String)null);
        assertNull(p);

        LinkedList<String> s = new LinkedList<String>();
        s.addFirst("alpha");
        s.addFirst("stable");
        s.addFirst("beta");

        p = pdb.selectProduct("vaosoft", "1+", s, null);
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.5", p.version);
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

        assertEquals(new Integer(5), pcount.get("vaosoft"));
        assertEquals(new Integer(1), pcount.get("ant"));
        assertEquals(new Integer(1), pcount.get("ivy"));
        assertEquals(new Integer(1), pcount.get("maven"));
    }

}