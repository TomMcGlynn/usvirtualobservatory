package dalserver.conf.image;

import dalserver.conf.FITSHeaderKeywords;
import dalserver.conf.NTFITSHeaderKeywords;
import dalserver.conf.DataFormatException;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class ImageHeaderUtilsTestNeedsData {

    private File testdatadir = new File("fso");
    private FITSHeaderKeywords fhk = null;
    private ImageHeaderUtils utils = null;
    
    static String[] datasets = 
                      {"Astro1.fits", "EUVE-img-spec.fits", "HST-FGS-ts.fits", 
                       "HST-FOC.fits", "HST-FOS-spec.fits", "HST-HRS-spec.fits",
                       "NICMOS-mos.fits", "WFPC2-mos.fits", "WFPC2-stack.fits" };
    static final int SIMPLE1=0, IMG_SPEC=1, TS=2, SIMPLE2=3, SPEC_STACK1=4,
        SPEC_STACK2=5, RICH_MEF=6, SIMPLE3=7, CCD_STACK=8;

    public ImageHeaderUtilsTestNeedsData() throws IOException {
        String extra_data_dir = System.getProperty("test.extradata.dir");
        if (extra_data_dir != null) testdatadir = new File(extra_data_dir);
        if (! testdatadir.exists())
            throw new FileNotFoundException(testdatadir.toString());
        if (! testdatadir.isDirectory())
            throw new FileNotFoundException(testdatadir.toString() + 
                                            ": not a directory");
    }

    File dsfile(String filename) { return new File(testdatadir, filename); }
    ImageHeaderUtils loadHeaders(String filename) throws IOException {
        return ImageHeaderUtils.loadFromFile(dsfile(filename));
    }

    @Test
    public void testIsSimple() throws IOException {
        boolean is = false;
        String ds = null;
        for(int i=0; i < datasets.length; i++) {
            ds = datasets[i];
            utils = loadHeaders(ds);

            is = utils.isSimpleImage();

            if (i == SIMPLE1 || i == SIMPLE2 || i == SIMPLE3) 
                assertTrue(ds+": Not a simple image", is);
            else 
                assertFalse(ds+": Mistakenly interpreted as a simple image", is);
        }
    }

    @Test
    public void testIsExtension() throws IOException {
        boolean is = false;
        String ds = null;
        for(int i=0; i < datasets.length; i++) {
            ds = datasets[i];
            utils = loadHeaders(ds);

            is = utils.isExtensionImage();

            /*
            if (i == ??) 
                assertTrue(ds+": Not an extension image", is);
            else 
            */
                assertFalse(ds+": Mistakenly interpreted as an extension image", is);
        }
    }

    @Test
    public void testIsRichStack() throws IOException {
        boolean is = false;
        String ds = null;
        for(int i=0; i < datasets.length; i++) {
            ds = datasets[i];
            utils = loadHeaders(ds);

            is = utils.isRichStack();

            /*
            if (i == RICH_STACK) 
                assertTrue(ds+": Not a rich stack", is);
            else 
            */
                assertFalse(ds+": Mistakenly interpreted as a rich stack", is);
        }
    }

    @Test
    public void testIsRichMEF() throws IOException {
        boolean is = false;
        String ds = null;
        for(int i=0; i < datasets.length; i++) {
            ds = datasets[i];
            utils = loadHeaders(ds);

            is = utils.isRichMEF();

            if (i == RICH_MEF) 
                assertTrue(ds+": Not a rich MEF", is);
            else 
                assertFalse(ds+": Mistakenly interpreted as a rich MEF", is);
        }
    }

    @Test
    public void testIsCCDStack() throws IOException {
        boolean is = false;
        String ds = null;
        for(int i=0; i < datasets.length; i++) {
            ds = datasets[i];
            utils = loadHeaders(ds);

            is = utils.isCCDStack();

            if (i == CCD_STACK) 
                assertTrue(ds+": Not a CCD stack", is);
            else 
                assertFalse(ds+": Mistakenly interpreted as a CCD stack", is);
        }
    }


    @Test
    public void testIsCCDMEF() throws IOException {
        boolean is = false;
        String ds = null;
        for(int i=0; i < datasets.length; i++) {
            ds = datasets[i];
            utils = loadHeaders(ds);

            is = utils.isCCDMEF();

            /*
            if (i == CCD_MEF) 
                assertTrue(ds+": Not a CCD mEF", is);
            else 
            */
                assertFalse(ds+": Mistakenly interpreted as a CCD MEF", is);
        }
    }

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("dalserver.conf.image.ImageHeaderUtilsTestNeedsData");
    }
}

