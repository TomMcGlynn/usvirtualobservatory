package net.ivoa.datascope;


import net.ivoa.util.CGI;
import net.ivoa.util.FieldExtractor;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileTee {

    public static byte[]    zMagic    = new byte[]{0x1f, (byte)0x9d};
    public static byte[]    gzipMagic = new byte[]{0x1f, (byte)0x8b};
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi      = new CGI();
	String sn    = cgi.value("sn");
	String id    = cgi.value("id");
	String index = cgi.value("index");
	String cache = cgi.value("cache");
	String type  = cgi.value("format");
	String col   = cgi.value("col");
	
	if (sn == null || id == null || index == null || cache == null) {
	    throw new Exception("Required arguments not found");
	}
	
	if (type == null) {
	    type = "fits";
	}
	
	FileTee ft = new FileTee();
        ft.run( sn, id, index, cache, type, col);
    }
    
    public void run( String sn, String id, String index, String cache, String type, String column) throws Exception {
	
	cache = DS.baseToHome(cache);
	sn    = DS.validFileName(sn);
	
	String filename = cache + sn +"."+id+"."+index+"."+type;
	
	String f = DS.checkFile(filename);
	if (f != null) {
	    redirect(f);
	    
	} else {
	
	    int row = Integer.parseInt(index)  - 1;
	    int col = Integer.parseInt(column) - 1;
	    String url = new FieldExtractor().find(cache+sn+"."+id+".xml", row, col);
	    if (url == null) {
	        throw new Exception("Unable to find requested URL");
	    }
	
	    if (url.indexOf("?") < 0) {
	        locationThenCopy(url, cache+sn+"."+id+"."+index, type);
	    } else {
	        transmitThenSave(url, cache+sn+"."+id+"."+index, type);
	    }
	}
    }
    private void locationThenCopy(String url, String filename, String dftType) throws Exception {
	
	System.out.println("location: "+url+"\n");
	System.out.close();
	System.err.close();
	// Wait a couple of seconds before we start copying this URL
	// so that we don't get in the way of the user.
	Thread.sleep(2000);
	
	File f = new File(filename+".temp");
	FileOutputStream fo = new FileOutputStream(f);
	byte[] buffer = new byte[16384];
	
	URLConnection uc  = new URL(url).openConnection();
	InputStream   is  = uc.getInputStream();
	String        enc = uc.getContentEncoding();
	String        type= uc.getContentType();
	
	int len;
	
	while ( (len = is.read(buffer)) > 0) {
	    fo.write(buffer, 0, len);
	}
	fo.close();
	is.close();
	
	String suffix = "";
	
	if (url.endsWith(".gz")) {
	    suffix =  ".gz";
	} else if (url.endsWith(".Z")) {
	    suffix = ".Z";
	} else if (enc != null)  {
	    enc = enc.toLowerCase();
	    if (enc.indexOf("gzip")>= 0) {
		suffix = ".gz";
	    } else if (enc.indexOf("compress")>= 0) {
		suffix = ".Z";
	    }
	}
	if (type == null) {
	    type = dftType;
	}
	if (type.indexOf("/") > 0) {
	    type = type.substring(type.indexOf("/")+1);
	}
	File to = new File(filename+"."+type+suffix);
	f.renameTo(to);
    }
    
    private void transmitThenSave(String url, String filename, String dftType) throws Exception {
	
	URLConnection uc    = new URL(url).openConnection();
	
	
	InputStream   is    = uc.getInputStream();
	String        type  = uc.getContentType();
	String        enc   = uc.getContentEncoding();
	int           len   = uc.getContentLength();
	
	ByteArrayOutputStream bo;
	if (len < 0) {
	    bo = new ByteArrayOutputStream();
	} else {
	    bo = new ByteArrayOutputStream(len);
	}
	
	byte[] buffer = new byte[16384];
	
	String xtype = type;
	if (xtype == null) {
	    xtype = dftType;
	}
	if (xtype.indexOf("/") > 0) {
	    xtype = xtype.substring(xtype.indexOf("/")+1);
	}
	if (xtype.equals("x-fits")) {
	    xtype = "fits";
	}
	
	boolean first = true;
	String suffix = "";
        int totalLen = 0;
	
	while ( (len=is.read(buffer))> 0) {
	    totalLen += len;
	    if (first) {
		if (len > 2  && enc == null) {
		    if (buffer[0] == gzipMagic[0] && buffer[1] == gzipMagic[1]) {
			enc = "gzip";
		    } else if (buffer[0] == zMagic[0] && buffer[1] == zMagic[1]) {
		        enc = "compress";
		    }
		}
	        System.out.println("Content-type: "+type);
	        if (enc != null  && !enc.equals("compress")){
	            System.out.println("Content-encoding: "+enc);
	        }
		System.out.println("");
		first = false;
	    }
	        
	    System.out.write(buffer, 0, len);
	    bo.write(buffer, 0, len);
	}
	if (totalLen <= 0) {
	    throw new Exception("No data was found at specified URL: "+url);
	}
	is.close();
	System.out.close();
	System.err.close();
	bo.close();
	if (enc.equals("gzip")) {
	    suffix = ".gz";
	} else if(enc.equals("compress")) {
	    suffix = ".Z";
	}
	
	File f = new File(filename+".temp");
	FileOutputStream fo = new FileOutputStream(f);
	byte[] z = bo.toByteArray();
	fo.write(z);
	fo.close();
	File to = new File(filename+"."+xtype+suffix);
	f.renameTo(to);
    }
    
    private void redirect(String filename) {
	String url = "http://"+DS.getHost()+DS.homeToBase(filename);
	System.out.println("Location: "+url+"\n");
    }
}
