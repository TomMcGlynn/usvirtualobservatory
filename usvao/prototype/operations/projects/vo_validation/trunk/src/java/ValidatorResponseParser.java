import java.io.*;
import java.util.*;
import java.net.MalformedURLException;
import java.util.Random;
import java.sql.*;
import java.net.URLEncoder;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.text.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.net.URI;



public class ValidatorResponseParser
{
    public ArrayList  array_subtestobjects = new ArrayList();
    
    public ArrayList  array_subtestobjects_1 = new ArrayList();
    public String firsttest_status = "pass";
    public SubTest urlerror;
    public String     c_url;
    XMLReader c_r;
    String c_handlername;
 
    DefaultHandler handler = null;
    //MyContentHandler  handler =null;
    

    int   timeout  = 1;
      
    public ValidatorResponseParser(String url,String handlername, SubTest urlerror_in)		   
    {       
        c_url = url;
	c_handlername= handlername;
        urlerror = urlerror_in;
	try {
	    Class cls = Class.forName(handlername);
	    handler = (DefaultHandler) cls.newInstance();
	    
	    setHandler();
	    setTimeout(60000);
	    array_subtestobjects = parseDocument();
	    if (urlerror != null)
		{    		    
		    Object lastobj  = (SubTest) array_subtestobjects.set(0,urlerror);
		    array_subtestobjects.add(lastobj);
		}
	}
	catch (Throwable e){
		System.out.println(e.getMessage());
	}
		
	
    }
    public String getFirstTestStatus()
    {
       
	//see if any of the tests did not pass
	for (int i =0;i< array_subtestobjects.size();i++)
	    {
		SubTest g = (SubTest) array_subtestobjects.get(i);
		if (g.getStatus().equals("fail") || g.getStatus().equals("abort"))
		    {
			firsttest_status = "fail";
			break;					
		    }				
	    }
	return firsttest_status;
    }
   
    public  void  setHandler()
    {	
	try
	    {
		c_r             = XMLReaderFactory.createXMLReader(); 			
		c_r.setContentHandler(handler); 
	    }
	catch (SAXException e)
	    {
		System.out.println("...you had an xml parsing exception" + e.getMessage());
		SubTest  sub = new SubTest("cannot connect to web page or response could not be parsed","none","abort","error parsing page");
		array_subtestobjects.add(sub);
	    }
    }
    public String isContentAvailable()
    {
	String contentstring = null;
	try
	    {
		WebFile file         = new WebFile(c_url,this.timeout);
		Object content       = file.getContent();
		if ( content instanceof String)
		    {

			String html = (String) content;
		      
		    }
		contentstring        = content.toString();
		
		
	
	    }
	catch (MalformedURLException e)
	    {
		System.out.println("...your URL is malformed"  + e);
	    }
	catch (IOException e)
	    {
		System.out.println("...an IOException: " + e);
	    }
	return contentstring;
    }
    public ArrayList getArraySubTestObjects()
    {
	return array_subtestobjects;
	
    }

    public ArrayList  parseDocument()  
    {       
       	
	try
	    {   
		String contentstring = null;
		if ( this.getHandlerName().equals("SSAHandler"))
		    {
			
			contentstring = Utils.JavaGetUrl(c_url);
			contentstring = contentstring.trim().replaceFirst("^([\\W]+)<","<");
		    }
		else
		    {
			contentstring =  isContentAvailable();
		    }
                if (System.getProperty("debug")  !=  null){
                }	
		if (contentstring != null)		    
		    { 
			StringToBuffer sb = new StringToBuffer(contentstring);
			InputStream is = sb.parseStringToIS();
			c_r.parse(new InputSource(is));			
		    }
		else
		    {			     
			throw new IOException("...page not downloadable");
		    }
		
		String pr =  handler.getClass().getName();
		array_subtestobjects =    ((MyContentHandler)handler).getArraySubTestObjects();	
		for (int i=0;i<array_subtestobjects.size();i++)
		    {
			SubTest g = (SubTest) array_subtestobjects.get(i);
		    
		    }
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
    public void  exceptionCleanup()
    {
	String q  = "cannot connect to web page or response could not be parsed";
	SubTest  sub = new SubTest(q,"none","abort","error parsing page");
	array_subtestobjects.add(sub);
    }
    protected void setTimeout(int t)
    {
	timeout = t; 	
    }
    public String getHandlerName()
    {
	return c_handlername;
    }
    
}
