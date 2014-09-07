package net.ivoa.datascope;

import net.ivoa.util.CGI;
import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.File;

import skyview.executive.Imager;

import java.util.ArrayList;

public class Overlay {
    
    
    public static void main(final String[] args) throws Exception {
	
	CGI cgi = new CGI();
	
	String cache = cgi.value("cache");
	String id    = cgi.value("id");
	String sn    = cgi.value("sn");
	String sizeS = cgi.value("size");
	String err   = cgi.value("errorcircle");
	
	String xmlFile   = DS.baseToHome(cache) + DS.validFileName(sn)+"."+id+".xml";
	String cacheDir = DS.baseToHome(cache);
	String dssFile  = cacheDir+"DssImg.fits";
	xmlFile = new File(xmlFile).toURL().toString();
	
	final String[] newargs = new String[]{
	  "survey=user", "userfile="+dssFile, 
	      "catalog="+xmlFile, "quicklook=object", "catalogids", "plotcolor=red", "noFits", "noexit",
	      "copywcs="+dssFile,
	      "Output=-"
	};
	try {
            Imager.main(newargs);
	} catch (Exception e) {
	    throw new Error("Unable to generate image:"+e);
	}
	
	ArrayList<ImagePlus> images = skyview.ij.IJProcessor.getSavedImages();
	if (images == null || images.size() < 1) {
	    throw new Error("No images in array");
	}
        try {
	    ImagePlus ip = images.get(0);
	    ip.setColor(java.awt.Color.RED);
	    if (err != null) {
		double rad  = Double.parseDouble(err);
		double size = Double.parseDouble(sizeS);
		rad = 300*(rad/size);
		for (double a=0; a < 360; a += 5) {
		    double ap = a + 5;
		    double x0 = 150+rad*Math.cos(Math.toRadians(a));
		    double y0 = 150+rad*Math.sin(Math.toRadians(a));
		    double x1 = 150+rad*Math.cos(Math.toRadians(ap));
		    double y1 = 150+rad*Math.sin(Math.toRadians(ap));
		    ip.getProcessor().drawLine( (int)(x0+.5), 300-(int)(y0+0.5), (int)(x1+0.5), 300-(int)(y1+0.5));
		}
	    }
	    System.out.println("Content-type: image/jpeg\n");
	    new FileSaver(ip).saveAsJpeg("-");
	} catch (Exception e) {
	    throw new Error("Error processing:"+e);
	}
    }
}
