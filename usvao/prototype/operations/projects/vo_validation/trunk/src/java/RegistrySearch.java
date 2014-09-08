import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.Random;
import java.text.*;
 

public class RegistrySearch extends Registry
{    
  
    public String getValUrl()
    {     
	//String query   = "http:" + validatorurl  + "http:";
        //need to get rid of '?' at end of baseurl for this validation test
	String modbaseurl  = baseurl;
	if (modbaseurl.endsWith("?"))
	    {
		modbaseurl  = modbaseurl.replace("?","");
	    }
	query         += modbaseurl;
	
	query         += "&format=xml";
	return query;
    }
   
}
