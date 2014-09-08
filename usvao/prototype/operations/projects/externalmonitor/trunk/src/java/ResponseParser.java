import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.net.URLEncoder;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.text.*;
import java.awt.event.*;



public class ResponseParser
{
    public ArrayList  array_subtestobjects = new ArrayList();
    public String     c_url;
    XMLReader c_r = null;
    MyContentHandler c_myHandler = new MyContentHandler();
    int   timeout  = 1;
    Connection c_con;
    
    
    public ResponseParser(String url,Connection con)		   
    {       
        c_url = url;
	c_con = con; 
	c_r = setHandler();
	setTimeout(40000);       
    }
    public XMLReader setHandler()
    {	
	try {
	    c_r = XMLReaderFactory.createXMLReader(); 			
	    c_r.setContentHandler(c_myHandler); 
	}
	catch (SAXException e)
	    {
		System.out.println("...you had a parsing exception" + e);
		//SubTest  sub = new SubTest("cannot connect to web page or response could not be parsed","none","abort","error parsing page");
		//array_subtestobjects.add(sub);
	    }
	return c_r;
    }
    public String isContentAvailable()
    {
	String content = null;
	try{
	    System.out.println("Testing:");
	    System.out.println(c_url);
	    System.out.println(timeout);
	    GetXmlHttp file         = new GetXmlHttp(c_url,timeout);
	    
	    
	    Object contentobj      = file.getContent();
	    content                = contentobj.toString();	    
	}
	
	catch (Exception e){System.out.println("isContentAvailable method issue:" + e);}	
	return content;
    }   

    public ArrayList  parseDocument()  
    {           	
	try {   	    
	    String contentstring =  isContentAvailable();
	    if (contentstring != null)
		{ 		    
		    //System.out.println();
		    
		    StringToBuffer sb = new StringToBuffer(contentstring);
		    InputStream is = sb.parseStringToIS();
		    
		    c_r.parse(new InputSource(is));	
		    //c_r.parse(new InputSource(new StringReader(contentstring)));		    		    
		}
	    else
		{		    
		    throw new IOException("...Also, URL not queryable");
		}
	    array_subtestobjects = c_myHandler.getArraySubTestObjects();	    
	}
						
	catch (MalformedURLException e)
	    {
		String subid = "E.1";
		System.out.println("You have a malformed url exception...Adding nulls to tables for this service test" + e);
		exceptionCleanup("malformed url exception",subid);
	    }
	catch (IOException e)
	    {
		String subid = "E.2";
		System.out.println("Could not connect to website...Adding nulls to tables for this service test" + e);
		exceptionCleanup("Could not connect to website",subid);
	    }
	catch (ClassCastException e)
	    {
		String subid =  "E.3";
		System.out.println("You have a ClassCastException " + e);
		exceptionCleanup("unknown error",subid);

   	    }
	
	catch (Exception e)
	    {
		String subid = "E.4";
		System.out.println("You have a general exception" + e);
		exceptionCleanup("unknown error",subid);
		
	    }

	return array_subtestobjects;
    }
    public void  exceptionCleanup(String error,String subid)
    {
	SubTest  sub = new SubTest(error,"none","abort",subid);
	array_subtestobjects.add(sub);
    }
    protected void setTimeout(int t)
    {
	timeout =  t;     //set by user	  
    }
}
