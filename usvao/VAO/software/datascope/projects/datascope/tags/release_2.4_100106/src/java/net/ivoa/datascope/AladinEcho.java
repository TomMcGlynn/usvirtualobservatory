package net.ivoa.datascope;

import net.ivoa.util.CGI;

public class AladinEcho {

    public static void main(String[] args) {
	
	CGI cgi = new CGI();
	
	String data = cgi.value("script");
	System.out.println("Content-disposition: inline;filename=\"datascope.asc\"");
	System.out.println("Content-type: application/x-aladin\n");
	
	String[] lines = data.split(";");
	for(String line: lines) {
	    System.out.println(line);
	}
    }
}
			   
    
    
