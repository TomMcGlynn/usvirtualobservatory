import java.io.*;
import java.io.BufferedReader;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import nom.tam.fits.*;
import nom.tam.util.*;
//import nom.tam.fits.BasicHDU.*;

public class FitsURLTester  extends Tester
{      

    String imageurl = null;   
    String status = "fail";
    String error = null;
    public FitsURLTester(String iurl){  
	
	imageurl = iurl;
    }
    public void test()
    {
	try  {
	    if (imageurl.length() == 0){	
          	return;
	    }
	    
	    URL url            = new URL(imageurl);
	    URLConnection con  = null;      
	    con                = url.openConnection();
	    URLConnection hcon = con;
	    // Set up a request.
	    hcon.setConnectTimeout(120000);     
	    hcon.setReadTimeout( 120000);    
	    if (hcon != null)
                {                                   
		    
		    //print header information and get fits file size
		    for (int j = 1;; j++) {
			String header = hcon.getHeaderField(j);
			if (header == null)
			    break;                            
		    }
		    
		    InputStream r = hcon.getInputStream(); 
		    ArrayDataInput adi = null;
		    try { System.out.println("you are in Fits tester");   
			Fits fitsfile = new Fits(r);
			adi = fitsfile.getStream();
			Header hdr = Header.readHeader(adi);
                        
			if (hdr != null){                                   
			    status  = "pass";
			}
		    }
		    catch (FitsException e) {
			System.out.println("FitsException  "  + e);
		    }  
		    finally{
			if (adi != null){
			    adi.close();
			}
		    }                        
		    r.close();
		}
	}
	catch (java.net.SocketTimeoutException e){
	    error = "Socket Timeout"; 
	}
	catch (MalformedURLException e){
	    System.out.println("FitsURLTester.java" + e);
	}
	catch (IOException e){           
	    System.out.println(e);
	}
	catch (NumberFormatException e){
	    System.out.println("NumberFormatException: " + e.getMessage());
	}   
    }
    public String getStatus()
    {
	return status;
	
    }
    public String getError()
    {
	
	return error;
    }

}

