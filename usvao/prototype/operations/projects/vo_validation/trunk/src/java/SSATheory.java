import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.Random;
import java.util.regex.*;
import org.xml.sax.helpers.*;
import java.lang.reflect.*;



//import java.lang.reflect.*;
//import java.sql.*;
import java.text.*;
//import java.awt.event.*;
 

public class SSATheory  extends SSA 
{     
    public ArrayList test_default(ArrayList array, int runid, String date, String identifier)
    {   
	//query, serviceId,stmt,type,utc ,connection, errorcodes,validatorid,shortname;
	
       if (System.getProperty("debug") != null){
            System.out.println(" ");
            System.out.println("Testing " +  this.getXsiType() + ": " +  identifier);
        }
       
	String handlername = "SSAHandler";
	ValidatorResponseParser vrp     = new ValidatorResponseParser((String) array.get(0), handlername, urlerror);
	ArrayList array_subtestobjects = vrp.getArraySubTestObjects();
	ArrayList array_subtestobjects_1 = null; 
	 

	/**new VO resource test
        VAOResourceTestBuilder vbuilder = new VAOResourceTestBuilder(baseurl,"MyContentHandler",ivoid);
        ValidatorResponseParser vrpnew = new ValidatorResponseParser(vbuilder.getValUrl(),"MyContentHandler",urlerror);
	
	array_subtestobjects_1   = vrpnew.parseDocument();
        
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
	String voturl  = getVOTURL();
        System.out.println("testing : " + voturl);
	String[] response = null;
	response  = Utils.runImageTest(voturl,"ssa");
           String status =  response[0];
	   String error = response[1];
	  if (! status.equals("pass"))
	    {   if (error != null)
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
  
        HashMap ssatheory =  new HashMap();
        String line = null;

	try {
	     BufferedReader br = new BufferedReader(new FileReader("../data/override_ssa_theory_services"));         
             while ((line = br.readLine())  != null)
             {
                    String[] array = line.split("\\|"); 
                    ssatheory.put(array[0],array[1]);
             }
             String pos         = Utils.fixPos(baseurl);
            }
            catch (FileNotFoundException e)
            {
                System.out.println(e);
            }
            catch (Exception e)
            {
                 System.out.println(e);
            } 
	return "http:" + baseurl + ssatheory.get(ivoid);        
    }
}
