import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.Random;
import java.text.*;
//this is an abstract class since it does not
//implement all of the abstract methods defined in Service.java

public abstract class Registry extends Service 
{    
   
       
    public ArrayList test_default(ArrayList array, int runid, String date, String identifier)
    {
       
	//query, serviceId,stmt,type,utc ,connection, errorcodes,validatorid,shortname;
        if (System.getProperty("debug") != null){
	System.out.println(" ");
	System.out.println("Testing: " + identifier);
	System.out.println(baseurl);
        }
	String  handlername = "RegistryHandler";
	ValidatorofRegistryParser vrp     = new ValidatorofRegistryParser((String) array.get(0),handlername,urlerror);
	ArrayList array_subtestobjects = null;
       
	try
	    {	     		
			array_subtestobjects  = vrp.parseDocument();
            }
	catch (Exception e)
	    {
		System.out.println("...an Exception occurred");
	    }
       
	
	//test to see if array_subtestobjects contains nulls(as in page not parsable)
	
	if (array_subtestobjects.size() == 0)
	    {					
		array = addZeroSizeArray(array,runid, date);	
	    }
	else
	    {	
		SubTest subempty = (SubTest) array_subtestobjects.get(0);
	       
		if ( ! subempty.getStatus().equals("pass"))
		    {		
			//System.out.println("Registry service failed\n");
		    }
		array.add(new Integer(runid));
		array.add(array_subtestobjects);
		array.add(date);	
	    }
	return array;
    }
}
