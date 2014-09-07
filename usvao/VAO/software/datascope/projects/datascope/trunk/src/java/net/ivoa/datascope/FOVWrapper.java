package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.nvo.Header;
import net.nvo.Footer;

public class FOVWrapper {
    
    
    
    public static void main(String[] args) {
	
	CGI  cgi = new CGI();
	
	String sn = cgi.value("sn");
	String err = cgi.value("errorcircle");
	String index = cgi.value("index");
	Header hdr = new Header();
	hdr.setBannerTitle("Field of View for image "+index+" of "+sn);
	
	Header.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
	hdr.printBanner(System.out);
	
	System.out.println("This page shows the orientation of the requested "+
			   "image in the DataScope field of view.");
	if (err != null) {
	    System.out.println("The error radius is shown as a circle at the center of "+
			       "the field.");
	}
	System.out.println("A DSS image of the DataScope FOV is shown with a the boundaries "+
			   "of this image overlaid.  If the current image goes outside the "+
			   "FOV, then the image is rescaled to ensure that the entire boundary "+
			   "can be shown.");
	
	System.out.println("<p><img height=300 width=300 src='fov.pl?"+System.getenv("QUERY_STRING")+"'><p>");
	new Footer().print(System.out);
    }
}
			     
				 
			     
	
	
