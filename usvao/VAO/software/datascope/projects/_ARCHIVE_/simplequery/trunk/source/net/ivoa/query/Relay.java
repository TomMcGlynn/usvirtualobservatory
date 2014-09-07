package net.ivoa.query;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import java.util.Map;
import java.util.Map;
import java.util.Set;
import java.util.List;

import java.io.InputStream;

import net.ivoa.util.ShowError;

/** This class simply reads a URL and writes it out
 *  to standard out.
 */
public class Relay {
    
    public static void main(String[] args) throws Exception {
	
	String method = System.getenv("REQUEST_METHOD");
	if (method == null) {
	    method = "GET";
	}
	if (method.toUpperCase().equals("GET")) {
	    String urlString = System.getenv("QUERY_STRING");
	    if (urlString == null || urlString.length() == 0) {
		new ShowError("URL Relay").fail("Error in HTTP Relay request<br>No URL specified");
	    } else {
	    
	        urlString = java.net.URLDecoder.decode(urlString);
	    
	        URL url            = new URL(urlString);
	        URLConnection conn = url.openConnection();
		String type = conn.getContentType();
		String enc  = conn.getContentEncoding();
		int    len  = conn.getContentLength();
		if (type != null) {
		    System.out.println("Content-type: "+type);
		} else {
		    System.out.println("Content-type: text/html");
		}
		if (enc != null) {
		    System.out.println("Content-encoding: "+enc);
		}
		if (len > 0) {
		    System.out.println("Content-length: "+len);
		}
		System.out.println();
	        InputStream is = conn.getInputStream();
	    
	        byte[] buf = new byte[32768];
	        while( (len=is.read(buf)) > 0 ) {
		    System.out.write(buf, 0, len);
	        }
	    } 
	} else {
	    new ShowError("URL Relay").fail("Error in HTTP Relay request<br>Non-GET request:"+method);
	}
    }
}
