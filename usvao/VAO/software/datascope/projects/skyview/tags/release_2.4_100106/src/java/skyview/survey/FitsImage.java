package skyview.survey;

import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.FitsException;

import skyview.geometry.TransformationException;
import skyview.survey.Image;
import skyview.survey.ImageFactory;

import skyview.executive.Settings;

/** This class defines an image gotten by reading a file */

public class FitsImage extends Image {
    
    private String fitsFile;
    private Header fitsHeader;
    
    public FitsImage(String file) throws SurveyException {
	
	Header h;
	Fits   f;
	skyview.geometry.WCS wcs;
	
	setName(file);
	data = null;
	
	this.fitsFile = file;
	
	try {
	    // We want to read the WCS and then close the file.  The
	    // FITS library will do that if we open it as a file, but
	    // not as an input stream, so we try a file if that exists.
	    if (new java.io.File(fitsFile).exists()) {
                f = new Fits(fitsFile);
	    } else {
		f = new Fits(Util.getResourceOrFile(fitsFile));
	    }
	    h = f.readHDU().getHeader();
	    
	    //  Kludge to accommodate DSS2
	    if (h.getStringValue("REGION") != null) {
		setName(h.getStringValue("REGION")+":"+file);
	    }
	    f.getStream().close();
	    
	} catch (Exception e) {
	    throw new SurveyException("Unable to read file:"+fitsFile);
	}
	
        int naxis = h.getIntValue("NAXIS");
	if (naxis < 2) {
	    throw new SurveyException("Invalid FITS file: "+fitsFile+".  Dimensionality < 2");
	}
	int nx = h.getIntValue("NAXIS1");
	int ny = h.getIntValue("NAXIS2");
	int nz = 1;
	
	if (h.getIntValue("NAXIS") > 2) {
	    nz = h.getIntValue("NAXIS3");
	}
	
	if (naxis > 3) {
	    for(int i=4; i <= naxis; i += 1) {
		if (h.getIntValue("NAXIS"+i) > 1) {
		    throw new SurveyException("Invalid FITS file:"+fitsFile+".  Dimensionality > 3");
		}
	    }
	}
	
	try {
	    if (Settings.has("PixelOffset")) {
		String[] crpOff= Settings.getArray("PixelOffset");
		try {
		    double d1 = Double.parseDouble(crpOff[0]);
		    double d2 = d1;
		    if (crpOff.length > 0) {
			d1 = Double.parseDouble(crpOff[1]);
		    }
		    h.addValue("CRPIX1", h.getDoubleValue("CRPIX1")+d1, "");
		    h.addValue("CRPIX2", h.getDoubleValue("CRPIX2")+d2, "");
		} catch (Exception e) {
		    System.err.println("Error adding Pixel offset:"+Settings.get("PixelOffset"));
		    // Just go on after letting the user know.
		}
	    }
	    wcs = new skyview.geometry.WCS(h);
	} catch (TransformationException e) {
	    throw new SurveyException("Unable to create WCS for file:"+fitsFile+" ("+e+")");
	}
	
	try {
	    // Make sure to close the file.
	    f.getStream().close();
	} catch (Exception e) {
	    throw new SurveyException("Error closing file: "+fitsFile);
	}
	
	try {
	    initialize(null, wcs, nx, ny, nz);
	} catch(TransformationException e) {
	    throw new SurveyException("Error generating tranformation for file: "+file);
	}
	fitsHeader = h;
    }
    
    
    /** Defer reading the data until it is asked for. */
    public double getData(int npix) {
	
	Fits     f;
	Object   o;
	BasicHDU hdu;
	
	if (data == null) {
	    
	    try {
		// We're going to read everything, so
		// don't worry if it's a file or not.
		boolean compressed = false;
		
		if (fitsFile.endsWith(".gz")  || fitsFile.endsWith(".Z")) {
		    compressed = true;
		}
		
                f   = new Fits(Util.getResourceOrFile(fitsFile), compressed);
	        hdu = f.readHDU();
	        o   = hdu.getData().getData();
		f.getStream().close();
	    } catch(Exception e) {
		throw new Error("Error reading FITS data for file: "+fitsFile+"\n\nException was:"+e);
	    }
	    
	    o = nom.tam.util.ArrayFuncs.flatten(o);
	    
	    // Data may not be double (and it may be scaled)
	    // We assume no scaling if the data is double...
	    if (! (o instanceof double[])) {
		
	        Header h = hdu.getHeader();
		double scale = h.getDoubleValue("BSCALE", 1);
		double zero  = h.getDoubleValue("BZERO", 0);
		o = nom.tam.util.ArrayFuncs.convertArray(o, double.class);
		data = (double[]) o;
		if (scale != 1 || zero != 0) {
		    for (int i=0; i<data.length; i += 1) {
			data[i] = scale*data[i] + zero;
		    }
		}
	    } else {
		data = (double[]) o;
	    }
	    double total = 0;
	    for (int i=0; i<data.length; i += 1) {
		total += data[i];
	    }
	}
	
	return data[npix];
    }
    
    public Header getHeader() {
	return fitsHeader;
    }
}
