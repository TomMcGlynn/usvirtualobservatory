package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.ivoa.util.FieldExtractor;

import ij.ImagePlus;
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
    
    private static double ra, dec, size;
    private static double scale;
    private static String errorcirc;
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi = new CGI();
	
	String cache = cgi.value("cache");
	String id    = cgi.value("id");
	String sn    = cgi.value("sn");
	String index = cgi.value("index");
	String col   = cgi.value("col");
	errorcirc = cgi.value("errorcircle");
	
	
	String zcache = cache.substring(0,cache.length()-1);
	zcache = zcache.substring(zcache.lastIndexOf('/')+1);
	String[] pos = zcache.split("\\_");
	
	ra = Double.parseDouble(pos[0]);
	dec= Double.parseDouble(pos[1]);
	size= Double.parseDouble(pos[2]);
	
	String dssFile = DS.baseToHome(cache)+"DssImg.fits";
	
	
	if (cache == null || sn == null || index == null) {
	    Response.error("Required parameters not found.");
	}
	
	String filename = DS.baseToHome(cache) + DS.validFileName(sn)+"."+id+"."+index+".fits";
	String f = DS.checkFile(filename);
	
	if (f != null) {
	    process(dssFile, f, null);
	    
	} else {
	
	    int r = Integer.parseInt(index) - 1;
	    int c = Integer.parseInt(col)   - 1;
	    String url = new FieldExtractor().find(DS.baseToHome(cache)+sn+"."+id+".xml", r, c);
	    process(dssFile, url, filename);
	}
    }
    
    
    static void process(String dss, String inFile, String outFile) throws Exception {
	
	Fits     f;  
	BasicHDU hdu;
	
	// Try both uncompressed and compressed...
	try {
	     f   = new Fits(inFile);
             hdu = f.readHDU();
	} catch (Exception e) {
	     f   = new Fits(new URL(inFile), true);
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
	for (int i=0; i<newargs.length; i += 1) {
	    System.err.println("newargs:"+newargs[i]);
	}
	
        Imager.main(newargs);
	ArrayList<ImagePlus> images = skyview.ij.IJProcessor.getSavedImages();
	ImagePlus ip = images.get(0);
	// Draw the borders of the region.
	ip.setColor(java.awt.Color.RED);
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
	
        if (errorcirc != null) {
	    double rad  = Double.parseDouble(errorcirc);
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
	
	if (outFile != null) {
	    try {
		File tmp = new File(outFile+".gz.tmp");
	        BufferedDataOutputStream bds = new BufferedDataOutputStream(
					         new GZIPOutputStream(
						   new FileOutputStream(tmp) ) );
	        f.write(bds);
		bds.close();
		File to = new File(outFile+".gz");
		tmp.renameTo(to);
	    } catch (Exception e) {
		// Just ignore...
	    }
	}
    }
    private static int scl(double x) {
	return (int)((x-150)/scale + 150.5);
    }
}
