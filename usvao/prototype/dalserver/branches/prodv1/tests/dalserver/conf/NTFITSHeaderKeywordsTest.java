package dalserver.conf;

import nom.tam.fits.Header;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.BasicHDU;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class NTFITSHeaderKeywordsTest {

    static final String testfits = "test4Hdr.fits";

    Fits fits = null;
    NTFITSHeaderKeywords hdrs = null;

    public NTFITSHeaderKeywordsTest() throws FitsException {
        fits = new Fits(getClass().getResourceAsStream(testfits));
        hdrs = new NTFITSHeaderKeywords();
        for(BasicHDU hdu : fits.read()) 
            hdrs.addHeader(hdu.getHeader());
    }

    @Test
    public void testCount() {
        assertEquals(2, hdrs.getHDUCount());
    }

    @Test
    public void testContainsKey() {
        assertTrue(hdrs.containsKey(0, "GOOBERS"));
        assertFalse(hdrs.containsKey(1, "GOOBERS"));
        assertTrue(hdrs.containsKey(1, "NITERS"));
        assertFalse(hdrs.containsKey(0, "NITERS"));
        assertTrue(hdrs.containsKey(0, "BPA"));
        assertTrue(hdrs.containsKey(1, "BPA"));
        assertFalse(hdrs.containsKey(0, "FOOBAR"));
        assertFalse(hdrs.containsKey(1, "FOOBAR"));

        assertTrue(hdrs.containsKey("BPA"));
        assertTrue(hdrs.containsKey("GOOBERS"));
        assertTrue(hdrs.containsKey("NITERS"));
        assertFalse(hdrs.containsKey("FOOBAR"));
    }

    @Test
    public void testHdusWithKey() {
        int[] hdus = null;
        hdus = hdrs.hdusWithKey("FOOBAR");
        assertEquals(0, hdus.length);

        hdus = hdrs.hdusWithKey("GOOBERS");
        assertEquals(1, hdus.length);
        assertEquals(0, hdus[0]);

        hdus = hdrs.hdusWithKey("NITERS");
        assertEquals(1, hdus.length);
        assertEquals(1, hdus[0]);

        hdus = hdrs.hdusWithKey("BPA");
        assertEquals(2, hdus.length);
        assertEquals(0, hdus[0]);
        assertEquals(1, hdus[1]);
    }

    @Test
    public void testGetString() {
        assertEquals("JY/BEAM", hdrs.getStringValue(0, "BUNIT"));
        assertEquals("82", hdrs.getStringValue(0, "GOOBERS"));
        assertEquals("1.92342605442E-03", hdrs.getStringValue(0, "BMIN"));
        assertNull(hdrs.getStringValue(1, "GOOBERS"));
        assertEquals("GURN", hdrs.getStringValue(1, "GOOBERS", "GURN"));
    }

    @Test
    public void testGetIntValue() {
        assertEquals(82, hdrs.getIntValue(0, "GOOBERS"));
        assertEquals(0, hdrs.getIntValue(1, "GOOBERS"));
        assertEquals(5, hdrs.getIntValue(1, "GOOBERS", 5));
        assertEquals(0, hdrs.getIntValue(0, "NITERS"));
        assertEquals(0, hdrs.getIntValue(0, "BMIN"));
    }

    @Test
    public void testGetBooleanValue() {
        assertTrue(hdrs.getBooleanValue(0, "SIMPLE"));
        assertFalse(hdrs.getBooleanValue(1, "FOOBAR"));
        assertTrue(hdrs.getBooleanValue(1, "FOOBAR", true));
    }

    public static void main(String args[]) {
      org.junit.runner.JUnitCore.main("dalserver.conf.NTFITSHeaderKeywordsTest");
    }
}
