package dalserver.conf.image;

import java.io.IOException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImageHeaderUtilsTest {

    static final String testfits = "test4Hdr.fits";
    static final Class rescls = dalserver.conf.NTFITSHeaderKeywords.class;

    ImageHeaderUtils utils = null;

    public ImageHeaderUtilsTest() throws IOException {
        utils = ImageHeaderUtils.loadFromStream(
                                         rescls.getResourceAsStream(testfits));
    }

    @Test
    public void testGetNaxes() {
        assertEquals(3, utils.getNaxes(0));
        assertEquals(3, utils.getNaxes(1));
        assertEquals(0, utils.getNaxes(2));
    }

    @Test
    public void testIsImage() {
        assertTrue(utils.isImageHDU(0));
        assertTrue(utils.isImageHDU(1));
    }

    @Test
    public void testIsLongAxis() {
        assertTrue(ImageHeaderUtils.isLongAxis("RA--TAN"));
        assertTrue(ImageHeaderUtils.isLongAxis("RA---CAR"));
        assertTrue(ImageHeaderUtils.isLongAxis("GLON-SIN"));
        assertTrue(ImageHeaderUtils.isLongAxis("ELON-SIN"));
        assertTrue(ImageHeaderUtils.isLongAxis("PLON-FOO"));
        assertTrue(ImageHeaderUtils.isLongAxis("PPLN-BAR"));
        assertTrue(ImageHeaderUtils.isLongAxis("RA"));

        assertFalse(ImageHeaderUtils.isLongAxis("DEC--SIN"));
        assertFalse(ImageHeaderUtils.isLongAxis("DEC--LON"));
        assertFalse(ImageHeaderUtils.isLongAxis("GLAT-CAR"));

        assertTrue(utils.isLongAxis(0, 1));
        assertFalse(utils.isLongAxis(0,2));
        assertFalse(utils.isLongAxis(0,3));
        assertTrue(utils.isLongAxis(1, 1));
        assertFalse(utils.isLongAxis(1,2));
        assertFalse(utils.isLongAxis(1,3));
        
    }

    @Test
    public void testIsLatAxis() {
        assertTrue(ImageHeaderUtils.isLatAxis("DEC-TAN"));
        assertTrue(ImageHeaderUtils.isLatAxis("DEC--CAR"));
        assertTrue(ImageHeaderUtils.isLatAxis("GLAT-SIN"));
        assertTrue(ImageHeaderUtils.isLatAxis("ELAT-SIN"));
        assertTrue(ImageHeaderUtils.isLatAxis("PLAT-FOO"));
        assertTrue(ImageHeaderUtils.isLatAxis("PPLT-BAR"));
        assertTrue(ImageHeaderUtils.isLatAxis("DEC"));

        assertFalse(ImageHeaderUtils.isLatAxis("RA---SIN"));
        assertFalse(ImageHeaderUtils.isLatAxis("RA--LON"));
        assertFalse(ImageHeaderUtils.isLatAxis("GLON-CAR"));

        assertTrue(utils.isLatAxis(0, 2));
        assertFalse(utils.isLatAxis(0,1));
        assertFalse(utils.isLatAxis(0,3));
        assertTrue(utils.isLatAxis(1, 2));
        assertFalse(utils.isLatAxis(1,1));
        assertFalse(utils.isLatAxis(1,3));        
    }

    @Test
    public void testIsFreqAxis() {
        assertTrue(ImageHeaderUtils.isFreqAxis("FREQ"));
        assertTrue(ImageHeaderUtils.isFreqAxis("WAVE-TBL"));
        assertTrue(ImageHeaderUtils.isFreqAxis("WAVN"));
        assertTrue(ImageHeaderUtils.isFreqAxis("ENER"));

        assertFalse(ImageHeaderUtils.isFreqAxis("VELO"));
        assertFalse(ImageHeaderUtils.isFreqAxis("FELO"));
        assertFalse(ImageHeaderUtils.isFreqAxis("VRAD-CAR"));

        assertFalse(utils.isFreqAxis(0, 3));
        assertFalse(utils.isFreqAxis(0, 1));
        assertFalse(utils.isFreqAxis(0, 2));
        assertFalse(utils.isFreqAxis(1, 3));
        assertFalse(utils.isFreqAxis(1, 1));
        assertFalse(utils.isFreqAxis(1, 2));        
    }

    @Test
    public void testIsVelocityAxis() {
        assertTrue(ImageHeaderUtils.isVelocityAxis("VELO-LSR"));
        assertTrue(ImageHeaderUtils.isVelocityAxis("FELO"));
        assertTrue(ImageHeaderUtils.isVelocityAxis("VRAD-GEO"));
        assertTrue(ImageHeaderUtils.isVelocityAxis("VELOCITY"));
        assertTrue(ImageHeaderUtils.isVelocityAxis("BETA-FOO"));
        assertTrue(ImageHeaderUtils.isVelocityAxis("ZOPT-BAR"));

        assertFalse(ImageHeaderUtils.isVelocityAxis("FREQ"));
        assertFalse(ImageHeaderUtils.isVelocityAxis("ENER"));
        assertFalse(ImageHeaderUtils.isVelocityAxis("WAVE-FOR"));

        assertTrue(utils.isVelocityAxis(0, 3));
        assertFalse(utils.isVelocityAxis(0,1));
        assertFalse(utils.isVelocityAxis(0,2));
        assertTrue(utils.isVelocityAxis(1, 3));
        assertFalse(utils.isVelocityAxis(1,1));
        assertFalse(utils.isVelocityAxis(1,2));        
    }

    @Test
    public void testIsSpectralAxis() {
        assertTrue(ImageHeaderUtils.isSpectralAxis("FREQ"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("WAVE-TBL"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("WAVN"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("ENER"));

        assertTrue(ImageHeaderUtils.isSpectralAxis("VELO"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("FELO"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("VRAD-CAR"));

        assertTrue(ImageHeaderUtils.isSpectralAxis("VELO-LSR"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("FELO"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("VRAD-GEO"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("VELOCITY"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("BETA-FOO"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("ZOPT-BAR"));

        assertTrue(ImageHeaderUtils.isSpectralAxis("FREQ"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("ENER"));
        assertTrue(ImageHeaderUtils.isSpectralAxis("WAVE-FOR"));

        assertTrue(utils.isSpectralAxis(0, 3));
        assertFalse(utils.isSpectralAxis(0,1));
        assertFalse(utils.isSpectralAxis(0,2));
        assertTrue(utils.isSpectralAxis(1, 3));
        assertFalse(utils.isSpectralAxis(1,1));
        assertFalse(utils.isSpectralAxis(1,2));        
    }

    @Test
    public void testDetermineImageType() {
        assertEquals(ImageHeaderUtils.ImageType.UNRECOGNIZED, 
                     utils.determineImageType());
    }

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("dalserver.conf.image.ImageHeaderUtilsTest");
    }
}
