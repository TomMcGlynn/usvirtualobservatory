import java.io.*;
import java.net.*;
import java.util.*;


public class SubTest
{


    //the id of the service we are testing.
    private String sid;

    //name of the subtest produced by the validator 
    private String subtestid;
   
    private String res;
    private String status;
    private String attstring;
    private boolean overide;
    private String type;
  

    //the runtime id we assign to a test
    private int runid;
  
    public SubTest(String result,String attsincoming, String statusinput, String subid)
    {      
        res       = result;
	status    = statusinput;	
	attstring = attsincoming;
	subtestid = subid;
	setClassType(subtestid);
    }   

    public String getResult()
    {
        return res;
    }
    public SubTest setServiceIdTestId(String sidinput, int testidinput)
    {
	sid = sidinput;
        runid = testidinput;
      
	return this;	
    }
    public String getStatus()
    {
	return status;	
    }
  
         
    public String getAttStrings()
    {
	return attstring;
    }
    public int getTestId()
    {
	return runid; 	
    } 
    public String getServiceId() 
    {
	return sid; 
    }
    public String getSubTestId()
    {
	return subtestid;
    }
    private void setClassType(String inputType)
    {
	if (inputType.equals("ssanone"))
	    {
		overide = true;
		type = "ssa.";
	    }
	else if (inputType.equals("local"))
	{
	    overide = true;
	    type = "local.";
 	 }
	
    }
    public String getClassType()
    {
	return type;	
    }
    public boolean getOveride()
    {
	return overide;
    }
}
