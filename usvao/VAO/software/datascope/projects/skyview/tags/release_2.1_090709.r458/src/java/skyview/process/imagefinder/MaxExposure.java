package skyview.process.imagefinder;

import skyview.survey.Image;
import skyview.survey.FitsImage;
import nom.tam.fits.Header;
import skyview.executive.Settings;

/** This class selects the best image for a pixel by
 *  looking for the image with the longest exposure
 *  that has the pixel in the field of view.
 *  Use of this Finder may not be optimal when using higher
 *  order samples, since it will tend to take images out
 *  to the edges and thus may have problems there.
 */
public class MaxExposure extends Border {
    
    private double[] exposures;
    
    /** Find the appropriate images.
     *  This routine gets the exposures for all of the
     *  images before calling the standard BorderImageFinder.
     */
    public int[] findImages(Image[] input, Image output) {
	
	String exposureKeyword = Settings.get("exposureKeyword");
	if (exposureKeyword == null) {
	    exposureKeyword = "EXPOSURE";
	}
	exposures       = new double[input.length];
	
	for (int i=0; i<input.length; i += 1) {
	    try {
	        FitsImage f = (FitsImage) input[i];
	        Header    h = f.getHeader();
	        exposures[i] = h.getDoubleValue(exposureKeyword, 0);
	    } catch (Exception e) {
		exposures[i] = 0;
	    }
	}
	
	return super.findImages(input, output);
	
    }
    
    /** The criterion for the best image */
    protected double criterion(double val, int index) {
	return exposures[index];
    }
}
