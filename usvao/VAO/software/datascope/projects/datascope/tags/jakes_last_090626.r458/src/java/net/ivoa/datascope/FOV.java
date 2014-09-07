package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.ivoa.util.FieldExtractor;

import ij.process.FloatProcessor;
import ij.ImagePlus;
import ij.io.FileSaver;

import skyview.executive.Imager;

import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import java.io.File;
import java.io.FileOutputStream;

import nom.tam.fits.Fits;
import nom.tam.util.BufferedDataOutputStream;

import skyview.geometry.WCS;
import skyview.geometry.Converter;
import nom.tam.fits.BasicHDU;

import java.net.URL;

public class FOV {
    
    static double scale;
    
    public static void main(String[] args) throws Exception {
	CGI cgi = new CGI();
	
	String cache = cgi.value("cache");
	String sn    = cgi.value("sn");
	String index = cgi.value("index");
	String url   = cgi.value("url");
	
	if (cache == null) {
	    Response.error("Required cache parameter not found.");
	}
	double ra    = Double.parseDouble(cgi.value("ra"));
	double dec   = Double.parseDouble(cgi.value("dec"));
	double size  = Double.parseDouble(cgi.value("size"));
	String err   = cgi.value("errorcircle");
	double errorc = -1;
	if (err != null) {
	    errorc = Double.parseDouble(err);
	}
	
	String dssFile  = DS.baseToHome(cache)+"DssImg.fits";
	String dataFile = FileTee2.getURLAsFile(url, cache);
	
	process(dssFile, dataFile, ra, dec, size, errorc);
    }
    
    
    static void process(String dss, String inFile,
			double ra, double dec, double size, double errorc) throws Exception {
	
	Fits     f;  
	BasicHDU hdu;
	
	// Try both uncompressed and compressed...
	try {
	     f   = new Fits(inFile);
             hdu = f.readHDU();
	} catch (Exception e) {
	     f   = new Fits(inFile, true);
	     hdu = f.readHDU();
	}
	
	WCS      w   = new WCS(hdu.getHeader());
	
	int[]    imAxes = w.getHeaderNaxis();
	double[][] ipixels = {{0, imAxes[0], imAxes[0], 0},{0,0,imAxes[1],imAxes[1]}};
	double[][] opixels = new double[2][4];
	
	
	Fits    g    =  new Fits(dss);
	WCS     dwcs =  new WCS(g.readHDU().getHeader());
	int[]   dsAxes = dwcs.getHeaderNaxis();
	
	Converter c = new Converter();
	c.add(w.inverse());
	c.add(dwcs);
	c.transform(ipixels, opixels);
	
	double xmin = 0;
	double ymin = 0;
	double xmax = dsAxes[0];
	double ymax = dsAxes[1];
	scale = 1;
	for (int i=0; i<4; i += 1) {
	    if (Math.abs(opixels[0][i]-150)/(150*scale) > 1) {
		scale = Math.abs(opixels[0][i]-150)/150;
	    }
	    if (Math.abs(opixels[1][i]-150)/(150*scale) > 1) {
		scale = Math.abs(opixels[1][i]-150)/150;
	    }
	}
	String[] newargs = new String[]{
	  "survey=user", "userfile="+dss, 
	      "quicklook=object","noFits", "noexit","ebins=0,1,1",
	      "position="+ra+","+dec, "size="+(size*scale)
	};
	
        Imager.main(newargs);
			    
	ArrayList<ImagePlus> images = skyview.ij.IJProcessor.getSavedImages();
			    
	ImagePlus ip = images.get(0);
	// Draw the borders of the region.
	// 
	ip.getProcessor().setColor(java.awt.Color.RED);
	ip.getProcessor().setLineWidth(2);
	ip.getProcessor().drawLine(scl(0),         300-scl(0),          scl(dsAxes[0]), 300-scl(0));
	ip.getProcessor().drawLine(scl(dsAxes[0]), 300-scl(0),          scl(dsAxes[0]), 300-scl(dsAxes[1]));
	ip.getProcessor().drawLine(scl(dsAxes[0]), 300-scl(dsAxes[1]),  scl(0),         300-scl(dsAxes[1]));
	ip.getProcessor().drawLine(scl(0),         300-scl(dsAxes[1]),  scl(0),         300-scl(0));
	
	// Now draw the borders of the image.
	for (int i=0; i<4; i += 1) {
	    ip.getProcessor().drawLine(
		 scl(opixels[0][i]),       300-scl(opixels[1][i]), 
		 scl(opixels[0][(i+1)%4]), 300-scl(opixels[1][(i+1)%4])
	      );
	}
	
	if (errorc > 0) {		    
	    double rad  = errorc;
	    rad = 300*(rad/size);
	    for (double a=0; a < 360; a += 5) {
		double ap = a + 5;
		double x0 = 150+rad*Math.cos(Math.toRadians(a));
		double y0 = 150+rad*Math.sin(Math.toRadians(a));
		double x1 = 150+rad*Math.cos(Math.toRadians(ap));
		double y1 = 150+rad*Math.sin(Math.toRadians(ap));
		ip.getProcessor().drawLine( scl(x0), 300-scl(y0), scl(x1), 300-scl(y1));
	    }
	}

	System.out.println("Content-type: image/jpeg\n");
	new ij.io.FileSaver(ip).saveAsJpeg("-");
	
	System.out.close();
	System.err.close();
    }
    
    private static int scl(double x) {
	return (int)((x-150)/scale + 150.5);
    }
}
