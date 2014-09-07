package net.ivoa.datascope;

import net.ivoa.util.CGI;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileTee2 {

    static final byte[] zMagic    = new byte[]{0x1f, (byte)0x9d};
    static final byte[] gzipMagic = new byte[]{0x1f, (byte)0x8b};
    
    public static void main(String[] args) throws Exception {
	
	CGI cgi       = new CGI();
	String cache  = cgi.value("cache");
	String url    = cgi.value("url");
	String format = cgi.value("format");
	
	if (url == null || cache == null) {
	    throw new Exception("Required arguments not found");
	}
	if (format == null) {
	    format = "fits";
	}
	
	FileTee2 ft = new FileTee2();
        ft.run(url, format, cache);
    }
    
    static String getURLAsFile(String url, String cache) throws Exception {
	String[] list = cachedFiles(url, cache);
	if (list != null && list.length > 0) {
	    return list[0];
	} else {
	    return copyURL(url, "fits", DS.baseToHome(cache));
	}
    }
	
    
    static String urlHash(String url) {
	return ""+url.hashCode();
    }
    
    
    static String[] cachedFiles(String url, String cache) {
	
	File cacheFile = new File(cache);
	
	String hash = ""+urlHash(url);
	MatchFilter filter = new MatchFilter(hash);
	
	String[] list = cacheFile.list(filter);
	return list;
    }
    
    void run( String url, String format, String cache) throws Exception {
	
	cache = DS.baseToHome(cache);
	String[] list = cachedFiles(url, cache);
	
	if (list != null && list.length > 0) {
	    redirect(list[0]);
	    
	} else {
	    locateThenCopy(url, format, cache);
	}
    }

    
    static void locateThenCopy(String url, String dftType, 
				String cache) throws Exception {
	System.out.println("location: "+url+"\n");
	System.out.close();
	System.err.close();
				      
	// Wait a couple of seconds before we start copying this URL
	// so that we don't get in the way of the user.
	Thread.sleep(2000);
	copyURL(url, dftType, cache);
    }
    
    static String copyURL(String url, String dftType, String cache) throws Exception {
				      
	File f              = new File(cache+urlHash(url)+".temp");
	
	FileOutputStream fo = new FileOutputStream(f);
	byte[] buffer       = new byte[16384];
	
	URLConnection uc   = new URL(url).openConnection();
	InputStream   is   = uc.getInputStream();
	String        enc  = uc.getContentEncoding();
	String        type = uc.getContentType();
	if (type.lastIndexOf('/') >= 0) {
	    type = type.substring(type.lastIndexOf('/')+1);
	}
	
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
				      
	if (type.indexOf(File.pathSeparator) > 0) {
	    type = type.substring(type.indexOf(File.pathSeparator)+1);
	}
	
	String name = cache+urlHash(url)+"."+type+suffix;  
	File to = new File(name);
	f.renameTo(to);
	return name;
    }
    
    void redirect(String localFile) {
	int index = localFile.lastIndexOf(File.pathSeparator);
	if (index < 0) {
	    index = 0;
	}
	String term = localFile.substring(index);
	String url  = "http://"+DS.getHost()+DS.homeToBase(term);
	System.out.println("Location: "+url+"\n");
    }
    
    static class MatchFilter implements java.io.FilenameFilter {
	
	String stem;
	
	MatchFilter(String stem) {
	    this.stem = stem + ".";
	}
       
	public boolean accept(File dir, String name) {
	    return name.indexOf(stem) >= 0;
	}
    }
}
