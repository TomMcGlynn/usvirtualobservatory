package org.usvo.openid;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.JUnitCore;

/**
 * test the Conf class.  This was created mainly to test changes in the 
 * creation/initializtion of the configuration.  
 */
public class ConfTestCase {

    static String srcdir = System.getProperty("test.webinf");
    static String indir = System.getProperty("test.outdir");
    static File origcfg = new File(srcdir, Conf.CONFIG_FILE_NAME);
    File cfg = null;
    Conf conf = null;

    @Before
    public void setUp() throws IOException {
        cfg = new File(indir, Conf.CONFIG_FILE_NAME);
        copy(origcfg, cfg);
    }
    public void copy(File srcfile, File destfile) throws IOException {
        if (! srcfile.exists())
            throw new FileNotFoundException(srcfile.toString());
        if (srcfile.isDirectory()) 
            throw new IOException("Not a file: "+srcfile);
        if (destfile.isDirectory()) {
            if (! destfile.exists()) 
                throw new FileNotFoundException(destfile.toString());
            destfile = new File(destfile, Conf.CONFIG_FILE_NAME);
        }
        byte[] buf = new byte[81920];
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            int n = 0;
            is = new FileInputStream(srcfile);
            os = new FileOutputStream(destfile);
            while ((n = is.read(buf)) >= 0) {
                if (n > 0) os.write(buf, 0, n);
            }
        } finally {
            if (is != null) is.close();
            if (os != null) os.close();
        }
    }

    @Test
    public void testCtor() throws IOException {
        conf = new Conf(cfg);
        assertEquals(indir, conf.getWebInfPath());
        assertTrue(conf.getIdBase().startsWith("http"));
    }

    @Test
    public void testCtor2() throws IOException {
        conf = new Conf(cfg, "goob");
        assertEquals("goob", conf.getWebInfPath());
        assertTrue(conf.getIdBase().startsWith("http"));
    }

    @Test
    public void testReload() throws IOException {
        testCtor();
        conf.reload();
    }

    @Test
    public void testDetectUpdate() throws IOException {
        testCtor();
        assertNull(conf.getProperty("goober"));
        long lm = cfg.lastModified();
        try { Thread.currentThread().sleep(1000); } 
        catch (InterruptedException ex) { fail("interrupted"); }

        FileWriter w = new FileWriter(cfg, true);
        try {
            w.write("goober=gurn\n");
        } finally {
            w.flush();
            w.close();
        }
        (new FileReader(cfg)).close();

        if (lm == cfg.lastModified())
            fail("config edit not detectable");

        assertEquals("gurn", conf.getProperty("goober"));
    }

    @Test
    public void testInitFromFile() throws IOException {
        Conf.init(cfg);
        assertNotNull(Conf.get());
    }

    

    public static void main(String args[]) {
      org.junit.runner.JUnitCore.main("org.usvo.openid.ConfTestCase");
    }
}