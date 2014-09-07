package net.ivoa.datascope;
    
import net.ivoa.util.CGI;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.File;

import java.util.Date;

public class GetDate {
    
    public static void main(String[] args) {
	
	CGI cgi = new CGI();
	String cache = cgi.value("cache");
	
	if (cache == null) {
	    finish("unknown");
	}
	File f = new File(DS.baseToHome(cache)+DS.timingFile());
	if (!f.exists() ) {
	    finish("unknown");
	}
	try {
	    DataInputStream ds = new DataInputStream(new FileInputStream(f));
	    long data = ds.readLong();
	    Date dt = new Date(data);
	    finish(dt.toString());
	} catch (Exception e) {
	    finish("unknown");
	}
    }
    static void finish(String data) {
	System.out.println("Content-type: text/plain\n\n"+data);
	System.exit(0);
    }
}
	    
	
	
