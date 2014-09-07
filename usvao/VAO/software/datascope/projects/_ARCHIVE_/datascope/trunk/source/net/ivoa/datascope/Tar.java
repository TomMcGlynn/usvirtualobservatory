package net.ivoa.datascope;

import net.ivoa.util.CGI;
import com.ice.tar.TarOutputStream;
import com.ice.tar.TarEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import java.net.URL;

public class Tar {
    
    static String          cache;
    static TarOutputStream ts;
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi = new CGI();
	
	  
	String   selectString = cgi.value("selections");
	cache = cgi.value("cache");
	
	String[] selections = selectString.split(";");
	
	for (String selection: selections) {
	    process(selection);
	}
	if (ts != null) {
	    ts.close();
	}
    }

    static void process(String info) throws Exception {
	
	String[] fields = info.split("\\|");
	if (fields[0].endsWith(".vot")) {
	    processXML(fields[0], fields[1]);
	} else {
	    processFits(fields[0], fields[1]);
	}
    }
    static void processXML(String name, String file) throws Exception {
	file = DS.baseToHome(file);
	processFile(file, name);
    }
    
    static void processFits(String name, String url) throws Exception {
	String local = localFile(cache, url);
	if (local != null) {
	    processFile(local, name);
	} else {
	    processURL(name, url);
	}
    }
    
    static void processFile(String name) throws Exception {
	processFile(name, null);
    }
    
    static void processFile(String realName, String wantName) throws Exception {
	
	File f = new File(realName);
	TarEntry tar = new TarEntry(f);
	
	if (wantName != null) {
	    tar.setName(wantName);
	} else {
	    String nn = realName.substring(realName.lastIndexOf("/")+1);
	    tar.setName(nn);
	}
	
	if (ts == null) {
	    openTs();
	}
	
	ts.putNextEntry(tar);
	FileInputStream fi = new FileInputStream(f);
	byte[] buffer = new byte[16384];
	int len;
	
	while ( (len=fi.read(buffer)) > 0) {
	    ts.write(buffer, 0, len);
	}
	
	ts.closeEntry();
    }
    
    static void processURL(String fileName, String url) throws Exception {
	
	byte[] data;
	String suffix = "";
	try  {
	    // First read the URL.
	    InputStream is = new URL(url).openStream();
	    ByteArrayOutputStream bo = new ByteArrayOutputStream();
	    byte[] buffer = new byte[16384];
            int len;
	
	    while ( (len = is.read(buffer) ) > 0) {
	        bo.write(buffer, 0, len);
	    }
	    bo.close();
	    
	    data = bo.toByteArray();
	    
	    if (data[0] == FileTee.gzipMagic[0]  && data[1] == FileTee.gzipMagic[1]) {
	        suffix = ".gz";
	    } else if (data[0] == FileTee.zMagic[0]  && data[1] == FileTee.zMagic[1]) {
	        suffix = ".Z";
	    }
	    
	} catch (Exception e) {
	    String error = "Error accessing url:"+url+"\n"+e;
	    data = error.getBytes();
	    suffix = ".error";
	}
	
	TarEntry te = new TarEntry(fileName+suffix);
	
	if (ts == null) {
	    openTs();
	}
	
	te.setSize(data.length);
	ts.putNextEntry(te);
	ts.write(data);
	ts.closeEntry();
	
	// Now I can do something to cache...
    }
	
    public static void openTs() throws Exception {    
        System.out.println("Content-type: application/tar\n"+
			   "Content-encoding: gzip\n"+
			   "Content-disposition: inline; filename=\"datascope.tar\"\n");
	ts = new TarOutputStream(new GZIPOutputStream(System.out));
    }
    
    public static String localFile(String cache, String url) {
	return null;
    }
}
