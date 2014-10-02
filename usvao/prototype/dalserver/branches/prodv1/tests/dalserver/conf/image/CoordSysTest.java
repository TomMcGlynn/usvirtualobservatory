package dalserver.conf.image;

import dalserver.conf.FITSHeaderKeywords;
import dalserver.conf.NTFITSHeaderKeywords;
import dalserver.conf.DataFormatException;

import java.io.IOException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoordSysTest {

    static final String testfits = "test4Hdr.fits";
    static final Class rescls = dalserver.conf.NTFITSHeaderKeywords.class;

    FITSHeaderKeywords fhk = null;
    CoordSys cs = null;

    public CoordSysTest() throws IOException {
        fhk = NTFITSHeaderKeywords.load(rescls.getResourceAsStream(testfits));
    }

    @Before
    public void setup() throws DataFormatException {
        cs = new CoordSys(fhk, 0);
    }

    @Test
    public void testNaxes() {
        assertEquals(3, cs.getNaxes());
    }

    // this needs more diverse data to test against
    @Test
    public void testSysStd() {
        assertEquals("ICRS", cs.getSystemStandard());
    }

    // this needs more diverse data to test against
    @Test
    public void testEquinox() {
        assertEquals(0.0, cs.getEquinox(), 0.001);
    }

    @Test
    public void testLabel() {
        assertEquals("RA---SIN", cs.getLabel(0));
        assertEquals("DEC--SIN", cs.getLabel(1));
        assertEquals("VELO-LSR", cs.getLabel(2));
    }

    @Test
    public void testCoordPos() {
        double[] pos = null;
        double[] origin = { 1.0, 1.0, 1.0 };
        double[] ref = { 33.0, 33.0, 1.0 };
        double[] refpos = { 6.72026578558e+01, 1.80311093150e+01, 5000 };
        double[] orpos = { 6.72213519321e+01, 1.80133306388e+01, 5000 };

        pos = cs.getCoordPos(ref);
        assertNotNull(pos);
        assertEquals(pos.length, ref.length);
        assertArrayEquals(refpos, pos, 1.0e-10);

        pos = cs.getCoordPos(origin);
        assertNotNull(pos);
        assertEquals(pos.length, ref.length);
        assertArrayEquals(orpos, pos, 1.0e-10);
    }

    @Test
    public void testGal2Cel() {
        final double[][] tgal = { {   0.0,       90.0      }, 
                                  {   0.0,        0.0      },
                                  { 114.892,    -13.85     } };
        final double[][] tcel = { { 192.859508,  27.128336 },
                                  { 266.4049,   -28.93617  },
                                  {   1.084120,  48.285093 } };
        double tol = 0.0001;

        double[] cel = null;
        for(int i=0; i < tgal.length; i++) {
            cel = CoordSys.gal2cel(tgal[i][0], tgal[i][1]);
            assertEquals(tcel[i][0], cel[0], tol);
            assertEquals(tcel[i][1], cel[1], tol);
        }
    }

    @Test
    public void testB1950toJ2000() {
        final double[][] tb50 = { {   0.0,       90.0      }, 
                                  {   0.0,        0.0      },
                                  { 114.892,    -13.85     } };
        final double[][] tj2k = { { 180.316337,  89.721687 },
                                  {   0.640691,   0.278309 },
                                  { 115.470300, -13.968455 } };
        double tol = 0.0001;

        double[] cel = null;
        for(int i=0; i < tb50.length; i++) {
            cel = CoordSys.b1950toJ2000(tb50[i][0], tb50[i][1]);
            assertEquals(tj2k[i][0], cel[0], tol);
            assertEquals(tj2k[i][1], cel[1], tol);
        }
    }
        

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("dalserver.conf.image.CoordSysTest");
    }
}