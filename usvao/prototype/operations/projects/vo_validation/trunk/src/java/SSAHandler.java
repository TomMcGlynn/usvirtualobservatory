import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import java.util.regex.*;
import java.lang.StringBuffer;


public class SSAHandler extends MyContentHandler
{

    /**+ httpstring.length());
     */
    public static boolean tq =true;
    public static boolean c; 
    public static boolean done;
    public static boolean d;
    String imageurl;

    StringBuffer httpstring  = new StringBuffer();
      
    public void startDocument() throws SAXException 
    {	
    }   
    
    public void endDocument() throws SAXException 
    {
	//notifies user when parsing done
	
	tq=true;
	c=false;
	done = false;
	d = false;
    }

    public void startElement(String uri, String localName, 
                             String qName, Attributes atts)
    {      	  
       
	if (!done)
	    {
	       	    
		if ((localName.equals("error")) || (localName.equals("fatal"))) 
		    {      		   			
			tq = true;
			
		    }   
	    }	    	       
    }
    public void  endElement(String uri, String localName,String qName) 
    {      
       
        if ((localName.equals("error")) || (localName.equals("fatal")))
            {    
		tq = false;
	        if (System.getProperty("debug") != null){ System.out.println(httpstring.toString());}
		String newhttpstring   =  Utils.getRidOfHTML(httpstring.toString());
		newhttpstring          =  Utils.cleanup(newhttpstring);
	        newhttpstring          =  truncateString(newhttpstring);	
	        SubTest subtest        = new SubTest(newhttpstring,"none","fail","ssanone");
		//System.out.println("newstring is " + newhttpstring);
		array.add(subtest);
		httpstring = new StringBuffer();
	    }
    }
    public void characters(char[ ] chars, int start, int length) 
    {
  	  	    	
	//start with tq true. This will allow each element to be tested.
	if (tq) 
	    {  
		//see if this td element contains string http:...
		String s =  new String(chars, start, length);
		httpstring.append(s);
	    }
	
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
		info += ( qName + " " + value + ":" );
	    }
	return info;   
            
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

    
}
