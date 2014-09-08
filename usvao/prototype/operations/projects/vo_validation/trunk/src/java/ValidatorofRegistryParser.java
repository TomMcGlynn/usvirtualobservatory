import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.net.URLEncoder;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.text.*;
import java.awt.event.*;
import java.io.FileReader;


public class ValidatorofRegistryParser extends ValidatorResponseParser
{
    
    
   
    public ValidatorofRegistryParser (String url,String handlername,SubTest urlerror)
    {
	super(url,handlername,urlerror);
	setTimeout(600000);
    }
    
    public ArrayList  parseDocument()  
    {       
	
        try
            {   
                String contentstring =  isContentAvailable();
                if (System.getProperty("debug") != null){
		    System.out.println("timeout is " + this.timeout);
                }
                if (contentstring != null)
                    { 
                        StringToBuffer sb = new StringToBuffer(contentstring);
                        InputStream is = sb.parseStringToIS();
                        c_r.parse(new InputSource(is));                 
                    }
                else
                    {                        
                        throw new IOException("...Also, URL not queryable");
                    }
                String pr =  handler.getClass().getName();
                array_subtestobjects =    ((RegistryHandler)handler).getArraySubTestObjects();                 
            }
        
        catch (IOException e)
            {
                System.out.println("...parsing was not possible.IOException...Adding nulls to tables for this service test" + e);
                exceptionCleanup();
            }
        catch (ClassCastException e)
            {
                System.out.println("You have a ClassCastException " + e);
                exceptionCleanup();
		
            }
        catch (Exception e)
            {
                System.out.println("You have a general exception" + e);
                exceptionCleanup();
                
            }
        return array_subtestobjects;
    }
	
}
