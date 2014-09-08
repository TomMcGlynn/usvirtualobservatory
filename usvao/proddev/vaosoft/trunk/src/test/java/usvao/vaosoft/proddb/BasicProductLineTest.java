package usvao.vaosoft.proddb;

import usvao.vaosoft.proddb.version.BasicVersionHandler;
import static usvao.vaosoft.proddb.LoadProduct.*;

import java.util.Properties;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the BasicProductLine class
 */
public class BasicProductLineTest {

    ProductLine pl = null;

    public BasicProductLineTest() throws ConfigurationException {
        StackConfig config = new VAOSoft1StackConfig(new File("/sw/vao"), null);
        VersionHandler vh = new BasicVersionHandler();
        BasicProductLine bpl = 
            new BasicProductLine("vaosoft", "usvao", vh, config);
        bpl.addVersion("1.0.0", null, "etc", null );
        bpl.addVersion("1.0.1", null, "etc", null );
        bpl.addVersion("1.0.5", null, "etc", null );
        bpl.addVersion("1.1.0rc3", null, "etc", null);
        bpl.addVersion("1.1.0", null, "etc", null);
        bpl.tagVersion("stable", "1.0.5");
        pl = bpl;
    }

    @Test public void testGetProductInfo() {
        assertEquals("vaosoft", pl.getProductName());
        assertEquals("usvao", pl.getOrg());
    }

    @Test public void testGetCount() {
        assertEquals(5, pl.getCount());
    }

    @Test public void testHasVersion() {
        assertTrue(pl.hasVersion("1.0.5"));
        assertFalse(pl.hasVersion("2.0.5"));
    }

    @Test public void testMatchRange() {
        ProductLine spl = pl.matchVersions("[1.0,1.1[", null);
        assertNotNull(spl);
        assertTrue(spl.hasVersion("1.0.0"));
        assertTrue(spl.hasVersion("1.0.1"));
        assertTrue(spl.hasVersion("1.0.5"));
        assertFalse(spl.hasVersion("1.1.0rc3"));
        assertFalse(spl.hasVersion("1.1.0"));
        assertEquals(3, spl.getCount());

        spl = pl.matchVersions("[1.0,1.1.0[", null);
        assertNotNull(spl);
        assertTrue(spl.hasVersion("1.0.0"));
        assertTrue(spl.hasVersion("1.0.1"));
        assertTrue(spl.hasVersion("1.0.5"));
        assertTrue(spl.hasVersion("1.1.0rc3"));
        assertFalse(spl.hasVersion("1.1.0"));
        assertEquals(4, spl.getCount());

        spl = pl.matchVersions("]1.0.0,1.1.0[", null);
        assertNotNull(spl);
        assertFalse(spl.hasVersion("1.0.0"));
        assertTrue(spl.hasVersion("1.0.1"));
        assertTrue(spl.hasVersion("1.0.5"));
        assertTrue(spl.hasVersion("1.1.0rc3"));
        assertFalse(spl.hasVersion("1.1.0"));
        assertEquals(3, spl.getCount());

        spl = pl.matchVersions("1.1.+", null);
        assertNotNull(spl);
        assertFalse(spl.hasVersion("1.0.0"));
        assertFalse(spl.hasVersion("1.0.1"));
        assertFalse(spl.hasVersion("1.0.5"));
        assertTrue(spl.hasVersion("1.1.0rc3"));
        assertTrue(spl.hasVersion("1.1.0"));
        assertEquals(2, spl.getCount());

        spl = pl.matchVersions("1.0.+", null);
        assertNotNull(spl);
        assertTrue(spl.hasVersion("1.0.0"));
        assertTrue(spl.hasVersion("1.0.1"));
        assertTrue(spl.hasVersion("1.0.5"));
        assertFalse(spl.hasVersion("1.1.0rc3"));
        assertFalse(spl.hasVersion("1.1.0"));
        assertEquals(3, spl.getCount());

        spl = pl.matchVersions("1.0+", null);
        assertNotNull(spl);
        assertTrue(spl.hasVersion("1.0.0"));
        assertTrue(spl.hasVersion("1.0.1"));
        assertTrue(spl.hasVersion("1.0.5"));
        assertTrue(spl.hasVersion("1.1.0rc3"));
        assertTrue(spl.hasVersion("1.1.0"));
        assertEquals(5, spl.getCount());

        spl = pl.matchVersions("1.0.5", null);
        assertNotNull(spl);
        assertFalse(spl.hasVersion("1.0.0"));
        assertFalse(spl.hasVersion("1.0.1"));
        assertTrue(spl.hasVersion("1.0.5"));
        assertFalse(spl.hasVersion("1.1.0rc3"));
        assertFalse(spl.hasVersion("1.1.0"));
        assertEquals(1, spl.getCount());
    }

    @Test public void testGetVersion() {
        Product p = pl.getVersion("1.0.1", null);
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.1", p.version);
        assertEquals("usvao", p.org);
        assertEquals("/sw/vao/products/usvao/vaosoft/1.0.1", p.getHome().toString());
        assertFalse(p.isInstalled());
    }

    @Test public void testGetByTag() {
        Product p = pl.getVersionByTag("stable", null);
        assertEquals("1.0.5", p.version);
    }

    @Test public void testGetLatest() {
        Product p = pl.getLatest(null);
        assertNotNull(p);
        assertEquals("1.1.0", p.version);
        p = pl.getVersionByTag("latest", null);
        assertEquals("1.1.0", p.version);
    }

    @Test public void testSelectVersion() {
        Product p = pl.selectVersion("1+", null, null);
        assertEquals("1.1.0", p.version);

        LinkedList<String> s = new LinkedList<String>();
        s.addFirst("alpha");
        s.addFirst("stable");
        s.addFirst("beta");
        p = pl.selectVersion("1+", s, null);
        assertEquals("1.0.5", p.version);

        p = pl.selectVersion("1.0.6+", s, null);
        assertEquals("1.1.0", p.version);
    }

    @Test public void testGetVersions() {
        ProductLine pdb = pl.getVersions("goob");
        assertNull(pdb);

        pdb = pl.getVersions("vaosoft");
        assertNotNull(pdb);
        assertEquals(5, pdb.getCount());
    }

    @Test public void testMatchProducts() {
        ProductDB pdb = pl.matchProducts("goob", "[1.0,1.1[", null);
        assertNotNull(pdb);
        assertEquals(0, pdb.getCount());

        pdb = pl.matchProducts("vaosoft", "[1.0,1.1[", null);
        assertNotNull(pdb);
        assertTrue(pdb instanceof ProductLine);
        assertEquals(3, pdb.getCount());
    }

    @Test public void testGetProduct() {
        Product p = pl.getProduct("vaosoft", "1.0.1");
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.1", p.version);
        assertEquals("usvao", p.org);
        assertEquals("/sw/vao/products/usvao/vaosoft/1.0.1", 
                     p.getHome().toString());
        assertFalse(p.isInstalled());

        p = pl.getProduct("maven", "2.0.1");
        assertNull(p);
    }

    @Test public void testGetProductByTag() {
        Product p = pl.getProductByTag("goob", "latest", null);
        assertNull(p);

        p = pl.getProductByTag("vaosoft", "stable", null);
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.5", p.version);
    }

    @Test public void testSelectProduct() {
        Product p = pl.selectProduct("goob", "latest", (String)null);
        assertNull(p);

        LinkedList<String> s = new LinkedList<String>();
        s.addFirst("alpha");
        s.addFirst("stable");
        s.addFirst("beta");

        p = pl.selectProduct("vaosoft", "1+", s, null);
        assertNotNull(p);
        assertEquals("vaosoft", p.name);
        assertEquals("1.0.5", p.version);
    }

    @Test public void testIterate() {
        String[] pdata = null;
        Iterator<String[]> it = pl.rawIterator();
        assertTrue(it.hasNext());
        pdata = it.next();
        assertEquals("vaosoft", pdata[NAME]);
        assertEquals("1.0.0", pdata[VERSION]);
        assertEquals("usvao", pdata[ORG]);
        assertNull(pdata[HOME]);
        assertEquals("etc", pdata[PROOF]);
        assertNull(pdata[PROPS]);

        assertTrue(it.hasNext());
        pdata = it.next();
        assertEquals("1.0.1", pdata[VERSION]);
        
        assertTrue(it.hasNext());
        pdata = it.next();
        assertEquals("1.0.5", pdata[VERSION]);
        
        assertTrue(it.hasNext());
        pdata = it.next();
        assertEquals("1.1.0rc3", pdata[VERSION]);
        
        assertTrue(it.hasNext());
        pdata = it.next();
        assertEquals("1.1.0", pdata[VERSION]);
        
        assertFalse(it.hasNext());
    }
}