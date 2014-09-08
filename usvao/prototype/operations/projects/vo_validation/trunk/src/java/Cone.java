import java.io.*;
import java.net.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.Random;
import java.lang.reflect.*;
import java.text.*;
import java.sql.*;

//deliberately inserting an error in dec
public class Cone extends Service
{    
     
    private String ncsaurl;

    public String getValUrl()
    {      
       	
	String query     = "http:" + validatorurl + "heasarc" + getRandomInt() + "&endpoint=http:";//	
        String add       =  "&RA=" + ra +  "&DEC=" + dec + "&SR=" + sr;		
	add              +=  "&format=xml&show=fail&op=Validate";
	try {  	    
            String encoded   = URLEncoder.encode(baseurl, "UTF-8");	       	       
	    query            = query +  encoded +add;
	}	   
	catch(UnsupportedEncodingException e){
	    System.out.println(e);
	}
	return query;	    
    }  
    public ArrayList test_default(ArrayList array, int runid, String date, String identifier)
    {
	//query, serviceId,stmt,type,utc ,connection, errorcodes,validatorid,shortname;
	if (System.getProperty("debug") != null){
            System.out.println(" ");
            System.out.println("Testing " +  this.getXsiType() + ": " +  identifier);
        }
	
	String handlername = "MyContentHandler";
	ValidatorResponseParser vrp            = new ValidatorResponseParser((String) array.get(0), handlername,urlerror);
	ArrayList array_subtestobjects = vrp.getArraySubTestObjects();
	ArrayList array_subtestobjects_1 = null;	

	/** new vaoresource test
	   System.out.println("\nRunning new VAOResource test");
	
	VAOResourceTestBuilder vbuilder = new VAOResourceTestBuilder(baseurl,handlername, ivoid);
        ValidatorResponseParser vrpnew = new ValidatorResponseParser(vbuilder.getValUrl(),handlername);
	array_subtestobjects_1 = vrpnew.parseDocument();
	
	for (int i =0;i< array_subtestobjects_1.size();i++)
	    {
		SubTest g = (SubTest) array_subtestobjects_1.get(i);
		array_subtestobjects.add(g);
	    }
        */ 
        
	if (array_subtestobjects.size() == '0')
	    {
		array = addZeroSizeArray(array, runid, date);
	    }	   
	else
	    {
		SubTest subempty = (SubTest) array_subtestobjects.get(0);	
		array.add(new Integer(runid));
		array.add(array_subtestobjects);
		array.add(date);			
	    }	   
	return array;
    }
}
