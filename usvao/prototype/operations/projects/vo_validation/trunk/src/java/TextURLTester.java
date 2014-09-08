import java.io.*;
import java.io.BufferedReader;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import nom.tam.fits.*;
import nom.tam.util.*;
//import nom.tam.fits.BasicHDU.*;

public class TextURLTester extends Tester
{      

    String imageurl = null;   
    String status = "fail";
    String error = null;
    public TextURLTester(String iurl){  
	
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
		    String s = null;
		    InputStream r = hcon.getInputStream(); 
		    BufferedReader br = new BufferedReader(new InputStreamReader (r));
                    s = br.readLine();
	            if (s != null)
		    {
	               status = "pass";
	            }
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

