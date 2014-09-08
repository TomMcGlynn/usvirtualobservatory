import java.io.*;
import java.io.BufferedReader;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import nom.tam.fits.*;
import nom.tam.util.*;

public class ImageURLTester extends Tester
{      
    
    String imageurl = null;
    String error    = null;   
    String status = "fail";
    public ImageURLTester(String iurl){
	imageurl = iurl;	
    }
    public void test()
    {
	try 
	    {
		if (imageurl.length()  == 0){ 
		    return; 
	        }
	        
                URL url            = new URL(imageurl);
                URLConnection con  = null;      
                con               = url.openConnection();
                HttpURLConnection hcon = (HttpURLConnection) con;
                // Set up a request.
                hcon.setConnectTimeout(120000);     
                hcon.setReadTimeout( 120000);    
                hcon.setInstanceFollowRedirects( true );
                hcon.setRequestProperty( "User-agent", "spider" );               
		
                if (hcon != null)
                    {                                   
                        InputStream data   = hcon.getInputStream();
			
                        //can only use this input stream once. The 3rd party imageninfo code seems to corrupt
                        //the data stream. Return status from this test only.
                        if (data != null)
                            {                           
                                //code for image test: http://schmidt.devlib.org/image-info/index.html
                                ImageInfo ii  = new ImageInfo();
                                ii.setInput(data);
                                if(ii.check())
                                    {   
                                        status = "pass";
                                    }                                           
                            }
                        data.close();                   
                    }
                hcon.disconnect();
	    }
	catch (java.net.SocketTimeoutException e)
	    {
		error = "Socket Timeout";		
	    }
	catch (MalformedURLException e)
	    {
		System.out.println("ImageURLTester.java" + e);
	    }
	catch (IOException e)
	    {   System.out.println("you should not be in this nnnn");        
		System.out.println(e);
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

