import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import java.sql.*;

public class MyContentHandler extends DefaultHandler
{
    boolean tq = false;
    boolean t  = false;
    private Locator locator;
    ArrayList<SubTest>  array = new ArrayList();
    String sid;
    int testid;
    String status;
    String subtestid;
    String j= "";
    String currentTagName = null;
   
    String  stringatt;
    public void startElement(String uri, String ln, String qName, Attributes atts) 
    {
	currentTagName = qName; 
	j = new String();
        if (ln.equals("testQuery")) {
	    tq = true; 
	    t  = false;    
	}
	else if  (ln.equals("test")){
	    tq = false;
	    t = true;	
	    //String value = atts.getValue("", "item");
	    stringatt  =  getAttrsInfo(atts);
	    if (System.getProperty("debug") != null){
		System.out.println("String : " +  stringatt);
	    }
	    subtestid  = getSubid(atts);
	}
    }
    public void endElement(String uri, String ln,
                           String qName) 
    {
        if(ln.equals("testQuery")) {
	    tq = false;		
	}
	if (ln.equals("test")){
	    String shortened = "";
	    SubTest subtest  = null;
	    if (status.equals("fail"))
		{
		    shortened = j;
                    shortened = truncateString(shortened);
		    subtest = new SubTest(shortened,stringatt,status,subtestid);
		}
	    else
		{			
		    subtest = new SubTest("empty string", "none","pass","null");			
		}
            array.add(subtest);
	    //reset to  null for next iteration
	    status = null; 
	    t = false;
	}
    }
    public String truncateString(String s)
    {
        String newstring = s;
	if (s.length() > 390)
	{
	    newstring  = s.substring(0,390);
	}
	return newstring;		 	
    }
    public void characters(char[ ] chars, int start, int length) 
    {
       
        if(tq) {	
	    tq = false;       
	}     
        else if  (t){	    
	    t=true;	   
	    j  = j + new String(chars,start,length);		
	}
    }
    public ArrayList getArraySubTestObjects() throws Exception
    {
      	
 	ArrayList<SubTest>  arraynew = new ArrayList<SubTest>();
  
	if   (array.size() > 0)
	   {	   
	       for (int i=0;i<array.size();i++)
		   {		       
		       SubTest sub = (SubTest) array.get(i);		  
		       arraynew.add(sub);	      
		   }
	   }
       else
	   {
	       SubTest sub = new SubTest("empty string","none","pass","null");
	       arraynew.add(sub);
	   } 
	return arraynew;
    }

    public String getAttrsInfo( Attributes attrs ) 
    {    
	if( attrs.getLength() <= 0 )
	    return "no atributes";
	String info = "";	

	for( int i=0; i<attrs.getLength(); i++ ) 
	    {           
		String qName = attrs.getQName( i );
		String value = attrs.getValue( qName );
		info += ( "|"  +  qName + " " + value + "|" );	
		
		if (value.equals("fail")){status ="fail";}
		else if (! value.equals("fail")) {status = "pass";}
	    }
	    return info;   	    
    }
    private void printAttrs(Attributes attrs)
    {
	//System.out.println(getAttrsInfo(attrs));

    }
    protected String getSubid(Attributes attrs)
    {
	String subtestid = null;
       	if( attrs.getLength() != 0 )
	    {
		String info = "";	

		for( int i=0; i<attrs.getLength(); i++ ) 
		    {           
			String qname = attrs.getQName( i );
			String value = attrs.getValue( qname );
			if (qname.equals("item"))
			    { 	
				subtestid = value;
			    }							
			else if (qname.equals("status"))
			    {
				if (value.equals("fail"))			
				    {
					status = "fail";
				    }
				else if (! value.equals("fail"))
				    {					
					status = "pass";  
				    }
			    }
	             }
	     }
	return subtestid;	

    }
   
    public void setDocumentLocator(Locator locator)
    {
	this.locator = locator;
	
    }
    
}
