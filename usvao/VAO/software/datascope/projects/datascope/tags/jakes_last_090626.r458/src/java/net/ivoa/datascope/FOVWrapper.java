package net.ivoa.datascope;

import net.ivoa.util.CGI;
import net.nvo.Header;
import net.nvo.Footer;

public class FOVWrapper {
    
    
    
    public static void main(String[] args) {
	
	CGI  cgi = new CGI();
	
	String sn      = cgi.value("sn");
	String err     = cgi.value("errorcircle");
	String index   = cgi.value("index");
	Header hdr     = new Header();
	hdr.setBannerTitle("Field of View for image "+index+" of "+sn);
	
	hdr.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
	hdr.printBanner(System.out);
	
	System.out.println("This page shows the orientation of the specified "+
			   "image in the general DataScope field of view.<br>");
	System.out.println("A DSS image of the DataScope FOV is shown with the boundaries "+
			   "of this image overlaid.  If the current image goes outside the "+
			   "region requested for DataScope, then the DSS image is rescaled to ensure that the entire boundary "+
			   "can be shown.<p>");
	if (err != null) {
	    double errc = Double.parseDouble(err);
	    if (errc > 0) {
	        System.out.println("The error radius is shown at the center of the DSS field.");
	    }
	}
	
	
	System.out.println("<p><img height=300 width=300 src='fov.pl?"+System.getenv("QUERY_STRING")+"'><p>");
	new Footer().print(System.out);
    }
}
			     
				 
			     
	
	
