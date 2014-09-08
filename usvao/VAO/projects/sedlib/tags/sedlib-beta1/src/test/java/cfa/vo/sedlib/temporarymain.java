/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cfa.vo.sedlib;

import cfa.vo.sedlib.common.SedInconsistentException;
import cfa.vo.sedlib.common.SedNoDataException;
import cfa.vo.sedlib.common.SedParsingException;
import cfa.vo.sedlib.common.SedWritingException;
import cfa.vo.sedlib.io.SedFormat;
import cfa.vo.testtools.SedLibTestUtils;
import java.io.IOException;

/**
 *
 * @author olaurino
 */
public class temporarymain extends SedTestBase {

    public temporarymain(String name) {
        super(name);
    }

    /**
     * @param args the command line arguments
     */
    public void testNoTest() throws SedParsingException, SedInconsistentException, IOException, SedWritingException, SedNoDataException {
        String inputName = "BasicSpectrum+Photom.vot";
        String outputName = "BasicSpectrum+Photom.fits";
        SedFormat inputFormat = SedFormat.VOT;
        SedFormat outputFormat = SedFormat.FITS;

        String input = SedLibTestUtils.mkInFileName(inputName);
        String output = SedLibTestUtils.mkOutFileName(outputName);
        Sed sed = Sed.read(input, inputFormat);

        sed.write(output, outputFormat);
    }

}
