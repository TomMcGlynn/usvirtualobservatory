import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;
import java.text.*;

public class GetServices
{



    public static LinkedHashMap  getServiceObjects(ResultSet result, HashMap testtypes)
    {
        LinkedHashMap  serviceobjects = new LinkedHashMap();
	HashMap ssatheory =  new HashMap();
        String line = null;

        try{
	    
	    BufferedReader br = new BufferedReader(new FileReader("../data/override_ssa_theory_services"));	    
	    while ((line = br.readLine())  != null)
	        {
		    String[] array = line.split("\\|"); 
		    ssatheory.put(array[0],array[1]);
	        }
	    

	    while (result.next())
		{           
		    String urlconcat    = fixURL(result.getString("serviceURL"));		
		    String xsitype      = result.getString("xsitype");
		    String ivoid        = result.getString("ivoid");
		    String method       = null;
		    String validatorid  = null;
		    String ncsaurl      = null;
		    String processing_status; 
		    
		    String testra     = result.getString("test_ra");
		    String testdec    = result.getString("test_dec");
		    String testsr     = result.getString("radius");
                    
		    if (testtypes.containsKey(xsitype))
			{
			    //see what method is needed for validation
			    
			    String[] concat  = ((String)testtypes.get(xsitype)).split("}");
			    method           = concat[0];
	                    
			    //override SSA class if this is a theoretical service
			    if (method.equals("SSA")  && (ssatheory.containsKey(ivoid)))
				{System.out.println("this is the SSA theory");
				    method = "SSATheory";
				} 
			    
			    validatorid      = concat[1];
			    ncsaurl          = concat[2];
			    processing_status = concat[3];			    			    
			}
		    else
			{
			    System.out.println("Unrecognzized Type! The service does not have an xsitype assigned to it." + ivoid);
			    continue;
			}
		                        
		    CreateService  CS =  new CreateService(result.getString("shortname"),urlconcat,ivoid,testra, testdec,testsr,
							   result.getString("role"),xsitype,result.getString("serviceId"),
							   ncsaurl, validatorid,method, processing_status);
		    Service  s = CS.getServiceObject(); 
		    serviceobjects.put(result.getString("serviceId"), s);                     
		}           
	}
        catch (FileNotFoundException e)
	    {
		System.out.println(e);
	    }
        catch (Exception e)
            {
                System.out.println("GetServices.java: "  + e);
            }
        return serviceobjects;     
    }
    
    

    public static String fixURL(String url)
    { 
	if (url.equals("null"))
	    {		
		return url;
	    }
	String surl = url.replace("&amp;","&");
	    
	String[] surlnew    = surl.split(":",2);
	String [] surlnewa  = surlnew[1].split("&");
	String urlconcat = "";
	for (int i=0; i<surlnewa.length;i++)
	    {
		if (i== surlnewa.length-1)
		    {
			urlconcat += surlnewa[i];
			
			if (surl.endsWith("&"))
			    {
			
				urlconcat += "&";
			    }
		    }
		else
		    {
			urlconcat += surlnewa[i] + "&";
		    }                                                                          
	    }
	return urlconcat;
    }


}
