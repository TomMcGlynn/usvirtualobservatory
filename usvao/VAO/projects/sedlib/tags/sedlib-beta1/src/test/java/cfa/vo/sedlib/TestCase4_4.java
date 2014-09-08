/***********************************************************************
*
* File: TestCase4_2.java
*
* Author:  olaurino                                Created: 2011-03-14
*
* Virtual Astrophysical Observatory; contributed by Center for Astrophysics
*
* Update History:
*   2011-03-14:  OL  Create
* 
***********************************************************************/


package cfa.vo.sedlib;

import cfa.vo.sedlib.common.SedInconsistentException;
import cfa.vo.sedlib.common.SedNoDataException;
import cfa.vo.testtools.SedLibTestUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;

import cfa.vo.sedlib.io.SedFormat;

/**
 * Tests Sedlib ability to manipulate Sed Data (Requirement 6)
 *
*/	

public class TestCase4_4 extends SedTestBase
{
    boolean keep = true;

    public TestCase4_4( String name )
    {
	super(name);
       
    }

    public static Test suite()
    {
	TestSuite suite = new TestSuite( TestCase4_4.class );

	return suite;
    }

    
    /**
     * Requirement 6.1.1 is covered by Read Test Cases (Test*IO and TestCase4_2), so this test is just a stub
     */
    public void testCase4_4_1() {

    }

    /**
     * Requirement 6.1.2: verify segments have the same observable quantity
     * Requirement 6.1.3: segments may have different x axis quantities
     * Requirement 6.1.4: segments may have different units in either axis
     *
     * This test case present the library with a file which contains two segments. The segments have the same y-axis quantity but different units
     * on both the x and y axes, and different quantities on the x axis. The test passes if both segments are added to the Sed and no exception is thrown.
     *
     */
    public void testCase4_4_2_1() {
            String inputName = "MultipleSpectraDifferentUnits.";

            for(SedFormat format : formats) {

                String inputFilename = SedLibTestUtils.mkInFileName( inputName+format.exten() );

                Sed sed = null;

                try {
                    sed = Sed.read(inputFilename, format);

                    //verify the sed has two segments;
                    int n = sed.getNumberOfSegments();
                    assertEquals("less than 2 segments have been found", n, 2);

                } catch(SedInconsistentException ex) {
                    fail("inconsistent exception thrown. But it shouldn't have.");
                } catch (Exception ex) {
                    Logger.getLogger(TestCase4_2.class.getName()).log(Level.SEVERE, null, ex);
                    fail("An exception was thrown while reading "+inputName+" with format "+format.name());
                }



            }
    }


    /**
     * Requirement 6.1.2: verify segments have the same observable quantity
     *
     * This test case presents the library with a file which contains two segments. The segments have different y-axis quantity.
     * The test passes if a SedInconsistentException is thrown.
     *
     */
    public void testCase4_4_2_2() {
        String inputName = "MultipleSpectraDifferentQuantities.";

        for(SedFormat format : formats) {

            String inputFilename = SedLibTestUtils.mkInFileName( inputName+format.exten() );

            Sed sed = null;

            try {
                sed = Sed.read(inputFilename, format);

            } catch(SedInconsistentException ex) {
                continue; //OK, correct Exception
            } catch (Exception ex) {
                Logger.getLogger(TestCase4_2.class.getName()).log(Level.SEVERE, null, ex);
                fail("An exception was thrown while reading "+inputName+" with format "+format.name());
            }

        }

    }

    /**
     * Requirement 6.1.2: verify segments have the same observable quantity
     *
     * Additional low level test cases for requirement 6.1.2. They just excercise the
     * Segment.isCompatibleWith(Segment other) method.
     *
     * To probe the simmetry of this method all asserts are performed using both segments' methods.
     *
     * If two segments have the same flux ucds and they are not incompatible because of a linear-logarithmic mismatch
     * then they are compatible.
     *
     * If two segments have the same flux ucds but one is linear and the other is logarithmic
     * then they are not compatible.
     *
     * If two segments have ucds that differ only for a ";em.*" then they are compatible
     *
     * If two segments have ucds representing an acceptable pair of magnitudes and flux/luminosity
     * then they are compatible
     *
     * If two segments have ucds representing a non acceptable pair of magnitudes and flux/luminosity
     * then they are not compatible
     *
     * If two segments have ucds following the patterns: phot.fluence;em.* and phot.flux.density;em.*
     * then they are compatible.
     *
     */
    public void testCase4_4_2_3() throws SedNoDataException, SedInconsistentException {

        String inputName = "MultipleSpectraDifferentUnits.";

        SedFormat format = SedFormat.VOT;

        String inputFilename = SedLibTestUtils.mkInFileName( inputName+format.exten() );

        Sed sed = null;

        try {
            sed = Sed.read(inputFilename, format);

            //verify the sed has two segments;
            int n = sed.getNumberOfSegments();
            assertEquals("less than 2 segments have been found", n, 2);

        } catch(SedInconsistentException ex) {
            fail("inconsistent exception thrown. But it shouldn't have.");
        } catch (Exception ex) {
            Logger.getLogger(TestCase4_2.class.getName()).log(Level.SEVERE, null, ex);
            fail("An exception was thrown while reading "+inputName+" with format "+format.name());
        }


        Segment s1 = sed.getSegment(0);
        Segment s2 = sed.getSegment(1);

        s1.createChar().createFluxAxis().setUcd("phot.flux.density;em.wl");
        s1.getChar().getFluxAxis().setUnit("Jy");
        s1.setFluxAxisUnits("Jy");

        s2.createChar().createFluxAxis().setUcd("phot.flux.density;em.wl");
        s2.getChar().getFluxAxis().setUnit("Jy");
        s2.setFluxAxisUnits("Jy");

        compatibleTrue(s1, s2);

        //---------------

        s2.setFluxAxisUnits("Jy Hz");

        compatibleFalse(s1, s2);

        //---------------

        s2.getChar().getFluxAxis().setUcd("phot.flux.density;em.freq");
        compatibleTrue(s1, s2);

        //---------------

        s1.getChar().getFluxAxis().setUcd("phys.magAbs");
        compatibleFalse(s1, s2);

        //---------------

        s1.getChar().getFluxAxis().setUcd("phot.mag");
        compatibleTrue(s1, s2);

        //---------------

        s1.getChar().getFluxAxis().setUcd("phot.fluence;em.wl");
        s2.getChar().getFluxAxis().setUcd("phot.flux.density;em.freq");
        compatibleTrue(s1, s2);
        
    }

    private void compatibleTrue(Segment segment1, Segment segment2) throws SedNoDataException, SedInconsistentException {
        assertTrue(segment1.isCompatibleWith(segment2));
        assertTrue(segment2.isCompatibleWith(segment1));
    }

    private void compatibleFalse(Segment segment1, Segment segment2) throws SedNoDataException, SedInconsistentException {
        assertFalse(segment1.isCompatibleWith(segment2));
        assertFalse(segment2.isCompatibleWith(segment1));
    }

}
