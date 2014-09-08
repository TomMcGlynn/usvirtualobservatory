import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import java.sql.*;

public class RegistryHandler extends MyContentHandler
{
    
      
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
                                else if (value.equals("rec"))
                                    {                                   
                                        status = "pass";  
                                    }
                          }
                    }
            }
        return subtestid;
    }
}
