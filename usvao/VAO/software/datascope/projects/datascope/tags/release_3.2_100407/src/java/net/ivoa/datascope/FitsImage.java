package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.ivoa.util.FieldExtractor;

import ij.process.FloatProcessor;
import ij.ImagePlus;
import ij.io.FileSaver;

import java.io.File;
import java.io.FileOutputStream;

import nom.tam.fits.Fits;
import nom.tam.fits.BasicHDU;
import nom.tam.util.BufferedDataOutputStream;

import java.util.zip.GZIPOutputStream;

public class FitsImage {
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi = new CGI();
	
	String cache = cgi.value("cache");
	String id    = cgi.value("id");
	String sn    = cgi.value("sn");
	String index = cgi.value("index");
	String col   = cgi.value("col");
	
	if (cache == null || sn == null || index == null) {
	    Response.error("Required parameters not found.");
	}
	
	String filename = DS.baseToHome(cache) + DS.validFileName(sn)+"."+id+"."+index+".fits";
	String f = DS.checkFile(filename);
	
	if (f != null) {
	    process(f, null);
	    
	} else {
	
	    int r = Integer.parseInt(index) - 1;
	    int c = Integer.parseInt(col)   - 1;
	    String url = new FieldExtractor().find(DS.baseToHome(cache)+sn+"."+id+".xml", r, c);
	    process(url, filename);
	}
    }
    
    static void process(String inFile, String outFile) throws Exception {
	
	Fits     f;  
	BasicHDU hdu;
	
	// Try both uncompressed and compressed...
	try {
	    f   = new Fits(inFile);
            hdu = f.readHDU();
	} catch (Exception e) {
	    System.err.println("Error reading "+inFile+" as uncompressed FITS");
	    f   = new Fits(inFile, true);
	    hdu = f.readHDU();
	}
	
	int[]    axes= hdu.getAxes();
	int      nx  = axes[axes.length-1];
	int      ny  = axes[axes.length-2];
	Object   o   = hdu.getData().getData();
	o = nom.tam.util.ArrayFuncs.flatten(o);
	double[] d =(double[]) nom.tam.util.ArrayFuncs.convertArray(o, double.class, true);
	if (d.length > nx*ny) {
	    double[] dx = new double[nx*ny];
	    System.arraycopy(d, 0, dx, 0, dx.length);
	    d = dx;
	}
	double mn = 1.e100;
	for (int i=0; i<d.length; i += 1) {
	    if (d[i] > 0 && d[i] < mn) {
		mn = d[i];
	    }
	}
	if (mn < 1.e100) {
	    for (int i=0; i<d.length; i += 1) {
		if (d[i] <= 0) {
		    d[i] = mn;
		}
	    }
	}
	
	FloatProcessor fp = new FloatProcessor(nx, ny, d);
	fp.flipVertical();
	
	if (mn < 1.e100) {
	    fp.log();
        }
	System.out.println("Content-type: image/jpeg\n");
	new FileSaver(new ImagePlus("", fp)).saveAsJpeg("-");
	
	System.err.close();
	System.out.close();
	
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
}
	
                     
