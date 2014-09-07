package net.ivoa.datascope;

import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.fits.BasicHDU;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.Cursor;
import nom.tam.fits.HeaderCard;

import java.io.FileOutputStream;


public class DSSImg {
    
    public static void gen(double ra, double dec, double size, String cache, String stem) {
	try {
	    double[][] db = new double[300][300];
	    Fits f = new Fits();
	    f.addHDU(Fits.makeHDU(db));
	    BasicHDU hdu = f.getHDU(0);
	    Header hdr = hdu.getHeader();
	    Cursor c=hdr.iterator();
	    while (c.hasNext()) {
		c.next();
	    }
	    c.add(new HeaderCard("CRVAL1", ra, ""));
	    c.add(new HeaderCard("CRVAL2", dec, ""));
	    c.add(new HeaderCard("CDELT1", -size/300, ""));
	    c.add(new HeaderCard("CDELT2", size/300, ""));
	    c.add(new HeaderCard("CRPIX1", 150.5, ""));
	    c.add(new HeaderCard("CRPIX2", 150.5, ""));
	    c.add(new HeaderCard("CTYPE1", "RA---TAN", ""));
	    c.add(new HeaderCard("CTYPE2", "DEC--TAN", ""));
	    BufferedDataOutputStream bdf = new BufferedDataOutputStream(
					     new FileOutputStream(cache+stem+".fits"));
	    hdu.write(bdf);
	    bdf.close();
	    f   = null;
	    bdf = null;
	} catch (Exception e)  {
	    DS.log("Image got exception:"+e);
	}
		
	String[] args = new String[]{
	  "survey=dssold", "position="+ra+","+dec, "surveyxml="+DS.getDataHome()+"dssold.xml",
	  "size="+size, "quicklook=jpg", "pixels=300",
	  "output="+cache+stem, "cache=/tmp/", "purgecache", "noexit"
	};
	try {
	    skyview.executive.Imager.main(args);
        } catch (Throwable e) {
	    DS.log("Error in DSS image:"+e);
	    args[0] = "survey=dss2r";
	    try {
	        skyview.executive.Imager.main(args);
	    } catch (Exception f) {
		DS.log("Error in DSS2 image"+f);
		throw new Error("Unable to find image!");
	    }
	}
    }
    
    public static void main(String [] args) throws Exception {
	
	double ra    = Double.parseDouble(args[0]);
	double dec   = Double.parseDouble(args[1]);
	double size  = Double.parseDouble(args[2]);
	
	gen(ra, dec, size, args[3], args[4]);
    }
}
