package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.ivoa.util.FieldExtractor;

import skyview.executive.Imager;

import java.io.File;
import java.io.FileOutputStream;

import nom.tam.fits.Fits;
import nom.tam.fits.BasicHDU;
import nom.tam.util.BufferedDataOutputStream;

import java.net.URL;

import java.util.zip.GZIPOutputStream;

public class FitsImage {
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi = new CGI();
	
	String url   = cgi.value("url");
	String cache = cgi.value("cache");
	
	String file = FileTee2.getURLAsFile(url, cache);
	String[] newargs = new String[]{
	  "survey=user", 
	  "userfile="+file,
	  "quicklook=jpg",
	  "output=-",
	  "noFits",
	  "copyWCS="+file
	};
	
	System.out.println("Content-type: image/jpeg\n");
	// Let SkyView render the image.
        Imager.main(newargs);
    }
}
	
                     
