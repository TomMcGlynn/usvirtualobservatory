package net.ivoa.datascope;

import java.io.RandomAccessFile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import net.nvo.Header;
import net.nvo.Footer;

import net.ivoa.util.CGI;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;

public class Query {
    
    
    public static void main(String[] args) throws Exception {
	Header hdr = new Header();
	hdr.addCSSFile(DS.getURLBase()+"css/styles.css");
	hdr.setTitle("DataScope Query");
	hdr.setBannerTitle("NVO Portal: DataScope");
	
	// Did this request come wish baggage
	// we need to carry along?
	CGI cgi = new CGI();
	if (cgi.count("resources") > 0) {
	    String res = cgi.value("resources");
	    
	    // Escape all quotes and new-lines.
	    res = res.replaceAll("\"", "\\\"");
	    res = res.replaceAll("\n", "\\n");
	    Settings.put("resources", res);
	    
	}
        if (cgi.count("resourceList") > 0) {
	    Settings.put("resourceList", cgi.value("resourceList"));
	}
	if (cgi.count("requestID") > 0) {
	    Settings.put("requestID", cgi.value("requestID"));
        }
	
	hdr.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
	hdr.printBanner(System.out);
	
        // Any special inputs,news,...							  
	printFile("startFile.inc");
	
        // The actual query form.				
	SettingsFilter.filter("queryform.html", true);
					     
	// The last few requests				     
	printLog(5);
	
	printFile("qryHelp.inc");
	
	new Footer().print(System.out);
    }
    
    static void printFile(String name) {
	File f = new File(DS.getURLHome()+name);
	if (!f.exists()) {
	    return;
	}
	BufferedReader bf = null;
	try {
	    bf = new BufferedReader(new FileReader(f));
	    String line;
	    while ( (line = bf.readLine()) != null) {
		System.out.println(line);
	    }
	} catch (Exception e) {
	} finally {
	    if (bf != null) {
		try {
		    bf.close();
		} catch (Exception e) {
		}
	    }
	}
    }
	      
						   
	
        
    static void printLog(int max) throws Exception {
	String stub = DS.getCGIBase()+"jds.pl?";
	
	String log = DS.getQueryLog();
	String result= null;
	if (new File(log).exists()) {
	    RandomAccessFile raf = null;
	    try {
	        raf = new RandomAccessFile(log, "r");
	        long size = raf.length();
	        if (size == 0) {
		    return;
	        }
		long read = size;
	        if (size > 1000) {
		    read = 1000;
	        }
	        if (size > read) {
		    raf.seek(size-read);
	        }
		byte[] arr = new byte[(int)read];
		raf.readFully(arr);
		result = new String(arr);
		
	    } finally {
		if (raf != null) {
		    raf.close();
		}
	    }
	    if (result != null  && result.indexOf('^') >= 0) {
		result = result.substring(result.indexOf('^'));
		String[] lines = result.split("\n");
		if (lines.length < max) {
		    max = lines.length;
		}
		
		System.out.println("Some recent queries:<br><dl>");
		for (int i=lines.length-max; i<lines.length; i += 1) {
		    String[] fields = lines[i].substring(1).split("\\|");
		    String url = stub + "position="+DS.encode(fields[0])+"&size="+fields[3]+"&errorcircle="+fields[4];
		    // These seem to get de-escaped once, so we
		    // do a double here
		    url = url.replace("%", "%25");
		    System.out.println("<dd><a href=\'" + url + "'>" + fields[0] + " ("+fields[3]+")</a>\n");
		}
		System.out.println("</dl>");
	    }
	}
    }
}
