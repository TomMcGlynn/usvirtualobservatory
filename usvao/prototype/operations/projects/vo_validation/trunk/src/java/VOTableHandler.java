import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.regex.*;
import java.lang.StringBuffer;
import java.util.*;

public class VOTableHandler extends DefaultHandler
{

    /**+ httpstring.length());
     */
    public boolean tq =false;
    public HashMap handlerhash;
    public boolean foundhandler = false;
    public boolean foundformat  = false;
    public boolean foundref =  false;
    public boolean td  = false;
    public boolean tr  = false;
    public int count   = 0;
    public int pos1    = 0;
    public int pos2    = 0;
    public int tdcount = 0;
    public String att  = null;
    public String type     = "image"; 
    public StringBuffer imageurl = new StringBuffer();
    public StringBuffer error = new StringBuffer();
    public String classtype;
    public String  httpstring  = new String();
    public Object tester;
    private Stack elementStack = new Stack();
      
    public VOTableHandler(String ctype, HashMap hh) {
	classtype = ctype;
        handlerhash = hh;
	if (classtype.equals("siap")){
	    att = "ucd";
	}
	else if (classtype.equals("ssa")){
	    att = "utype";
	}
    }
    
    public void startDocument() throws SAXException {}   
    
    public void endDocument() throws SAXException {
Iterator it = elementStack.iterator();

while (it.hasNext())
{
    String ele = (String) it.next();

}	
	    if (! foundref){                
		error.append(";Could not find reference url element");
	    }
	    if (! foundformat){
		error.append(";Could not find data format element");
	    }
	    if ((imageurl ==null) || (imageurl.toString().length() == 0)){
	        error.append(";Could not find image url");
	    }
    } 
    public String getErrorMessage(){
	if (! foundhandler) {
	  error.append("Could not find proper handler");
        }
	String s = error.toString();
	s = s.startsWith(";")? s.substring(1):s;
	return s;
    }	
    public void startElement(String uri, String ln, String qName, Attributes atts)
    {      	       
        this.elementStack.push(qName);	
	if (ln.equals("FIELD")){ 
            if (atts.getValue(att) != null) {
                //System.out.println(att + atts.getValue(att));
                String value = atts.getValue(att).toLowerCase();
                if ((value.equals("ssa:access.format")) || (value.equals("vox:image_format"))){  
		    foundformat = true;
		    pos1=count;
		}
                else if ((value.equals("ssa:access.reference")) || (value.equals("vox:image_accessreference"))){
                   

                    foundref = true;
                    pos2 = count;
                }
            }
	    count++;   
	}
	if (ln.equals("TD")){
	    td = true;
	    if ((foundref) &&  (foundformat)){
	       
		if (tdcount == pos1){
		    tq = true;
		}
		else if (tdcount == pos2){
		    tr = true;
		}
	    }
	    tdcount++; 			
	}
    }  
    public void  endElement(String uri, String ln,String qName) 
    {   
            String ele = (String) this.elementStack.peek();
	    if (ln.equals("TD")){td = false;}
            if (ln.equals("TD") && (tr == true)){ tr = false; } 
    }
    public void characters(char[ ] chars, int start, int length) 
    { try{
        String s =  new String(chars, start,length);
        
         

	
       if (tq){
	     type =  s.replaceAll("\\s",""); 
             tq =  false; 
	}
	else if (tr) { 
	    if ((s != null) && (td == true))
		{		   
		    imageurl = imageurl.append(s);
		}
	    else {
		error.append("Could not find image url in votable");
	    };
	}
	else if (td){
	    //this code is extra. A url starting with 'http' is stored in case
	    //the user needs it.However, this feature has been deprecated 
	    String regexp = "^http://.*";
	    Pattern pattern = Pattern.compile(regexp,Pattern.DOTALL);
	    Matcher matcher = pattern.matcher(s);
	    if (matcher.find()){                                   
		httpstring = s;
	    }
	    td = false; 
        }
       } 
       catch(Exception e){ System.out.println("Exception is: " + e.getMessage());}
        		
    }
    public Tester  getTester()  
    {   Tester s  = null;
        try{
            if (System.getProperty("debug") != null)
	    {
			System.out.println("Type and URL to test: " + type +  "," +  getImageUrl());
             } 
	      
	    if (handlerhash.get(type) != null){
		String name = (String) handlerhash.get(type);
		Class cls  = Class.forName(name);
		s = (Tester) cls.getDeclaredConstructor(String.class).newInstance(imageurl.toString());
		foundhandler = true;
	    }
	    else {    
		    if (System.getProperty("debug") !=  null) 
	            {
			System.out.println("Could not find a handler class");
		    }
             } 
        }
	catch (Throwable e) {
	    System.err.println("problem " + e.getMessage());
	}   
	return s;       
	
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
		info += ( qName + " " + value + "|" );
	    }
	return info;
    }
    public String getImageUrl()
    {
	return imageurl.toString();
    }
    public String getImageType()
    {
 	    
	return type;
    }
    
}        
