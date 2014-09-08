import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.Random;

//import java.lang.reflect.*;
//import java.sql.*;
import java.text.*;
//import java.awt.event.*;
 

public class SIAP extends Service 
{    
  
    public String getValUrl()
    {     
	String query   = "http:" + validatorurl + "heasarc" + getRandomInt() + "&endpoint=http:";
	String add     = "&RA=" + ra +  "&DEC=" + dec;
        add            += "&RASIZE=" + sr +  "&DECSIZE=" + sr;
	add            +=  "&format=xml&show=fail&op=Validate";
	try 
	    {
		String encoded = URLEncoder.encode(baseurl,"UTF-8");  
		query          = query + encoded + add;	
	         
	    }
	catch (UnsupportedEncodingException e)
	    {		
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
	ValidatorResponseParser vrp     = new ValidatorResponseParser((String) array.get(0),handlername,urlerror);
	ArrayList array_subtestobjects  = vrp.getArraySubTestObjects();
	ArrayList array_subtestobjects_1 = null;
        

	/**  new VO resource test
	     VAOResourceTestBuilder vbuilder = new VAOResourceTestBuilder(baseurl,handlername,ivoid);
	     ValidatorResponseParser vrpnew = new ValidatorResponseParser(vbuilder.getValUrl(),handlername,urlerror);
	     array_subtestobjects_1 = vrpnew.parseDocument();
	     for (int i =0;i< array_subtestobjects_1.size();i++)
	     {
		SubTest g = (SubTest) array_subtestobjects_1.get(i);	      
		array_subtestobjects.add(g);
	     }
	*/
       	
	//test to see if array_subtestobjects contains nulls(as in page not parsable)
	
	if (array_subtestobjects.size() == 0)
	    {				
		array  = addZeroSizeArray(array, runid, date);
	        return array;	
	    }
	
	String teststatus = vrp.getFirstTestStatus();

        array.add(new Integer(runid));
	
	
       
	//see if data in the VOTable can be downloaded
	String u = getVOTURL();
	String[] response  = null; 	        
	response  = Utils.runImageTest(u,"siap");
		    
	String status = response[0];
	String error = response[1];
	if (!status.equals("pass"))
	    {			      				
		if (error !=null)
		    {  
			String[] a = error.split(";");
			for (int i=0;i<a.length;i++)
			    {                                                        
				SubTest sub = new SubTest(a[i],"none", "fail", "local");
				array_subtestobjects.add(sub);
			    }
		    }
	    }	    
	array.add(array_subtestobjects); 			
	array.add(date);	
	return array;
    }
    public String getVOTURL()
    { 
	String pos         = Utils.fixPos(baseurl); 
	return "http:" + baseurl + pos + ra  + ","  + dec + "&SIZE="  + sr;     
    }
	 
}
