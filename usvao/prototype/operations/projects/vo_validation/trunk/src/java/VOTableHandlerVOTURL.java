import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.regex.*;
import java.lang.StringBuffer;


public class VOTableHandlerVOTURL extends DefaultHandler
{

    /**+ httpstring.length());
     */
    public static boolean tq =true;
    public static boolean c;
    public static boolean done;
    public static boolean d;
    String imageurl;

    StringBuffer httpstring  = new StringBuffer();
      
    public void startDocument() throws SAXException {}   
    
    public void endDocument() throws SAXException {
	//notifies user when parsing done
	tq   = true;
	c    = false;
	done = false;
	d    = false;
    }
    public void startElement(String uri, String localName, 
                             String qName, Attributes atts)
    {      	       
	if (!done){
	    if (localName.equals("TD")){      		   			
		tq = true;			
	    }     
	}	    	       
    }
    public void  endElement(String uri, String localName,String qName) 
    {      
	tq = false;
        if(localName.equals("TD")) {    
	    if (d){
		imageurl = httpstring.toString();				
		//found the match we want, so do no more processing, set d is true
		done = true;		       
	    }
	}
    }
    public void characters(char[ ] chars, int start, int length) 
    {
  	  	    	
	//start with tq true. This will allow each element to be tested.
	if (tq){  
	    if (httpstring.length()  != 0){
		//only runs if http string was already matched
		String newstring =  new String(chars,start,length);									
		httpstring.append(newstring);			     		  		       
	    }
	    else{
		//see if this td element contains string http:...		
		String s =  new String(chars, start, length);
		
		String regexp = "^http://.*\\.vot";
		Pattern pattern = Pattern.compile(regexp,Pattern.DOTALL);
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()){					
		    httpstring.append(s);
		    
		    //true means we hit an "http://*\.vot"			       
		    d = true;				
		} 
	    }
	}    			
    }
    private String getAttrsInfo( Attributes attrs ) 
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
    public String getImageUrl()
    {
	return imageurl;
    } 
}        
