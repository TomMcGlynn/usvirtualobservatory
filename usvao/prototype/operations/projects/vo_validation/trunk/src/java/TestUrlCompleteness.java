import java.io.*;
import java.util.*;

public class  TestUrlCompleteness
{

    String c_url;
    SubTest c_error = null;
 
    public TestUrlCompleteness(String url)
    {

	c_url = fixUrl(url);
    }



    public String fixUrl(String baseurl)
    {
	String newurl = baseurl;
        if (! baseurl.endsWith("?") && !baseurl.endsWith("$"))
            {
                if (baseurl.indexOf("?") >0)
		    {  
			if (!baseurl.endsWith("&"))
			    {				
				newurl  = baseurl.concat("&");
				storeError();						       
			    }			
			//if here, url complete and correct
		    }
                else
                    {		
                        newurl  = baseurl.concat("?");
			storeError();		      
                    }
            }           
        return newurl;
    }

    public String getUrl()
    {	
	return c_url;
    }

    public  void storeError()
    {
	String message = "This service url is mal-formed. It is missing either a '?' or '&' at the end";
	SubTest sub    = new SubTest(message,"none", "fail","mal-formed URL"); 
	c_error        =  sub;
    }
    public SubTest getError()
    {
	return c_error; 
    }
}

