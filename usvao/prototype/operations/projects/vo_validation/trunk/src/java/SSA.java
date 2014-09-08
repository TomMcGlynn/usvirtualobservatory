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
 

public class SSA extends Service 
{     
    public String getValUrl()
    {   
	String query  =null;
	try
	    {
		query          =  "http:" + validatorurl + "&POS=" + ra +"%2C" + dec + "&SIZE=" + sr;
		query          += "&TIME=&BAND=&FORMAT=ALL&spec=";
		String spec    =  "Simple Spectral Access 1.03";
		spec           =  URLEncoder.encode(spec,"UTF-8");
		query          += spec + "&addparams=&service=";
		String valurl  =  "http://voparis-validator.obspm.fr/xml/111.xml?";
		valurl         =  URLEncoder.encode(valurl,"UTF-8");
		query          += valurl;
		String url     =  "http:" + baseurl;		
		url            =  URLEncoder.encode(url,"UTF-8"); 
		query          += "&serviceURL=" + url + "&format=XML"; 	
	    }
	catch (UnsupportedEncodingException e)
	    {		
		System.out.println(e);
	    }	
	return query;	    
    }
    public String removeLastAmp(String url)
    {
	StringBuilder sb = new StringBuilder();
	String r = ".*&$";
	Pattern p = Pattern.compile(r);
	Matcher matcher = p.matcher(url);
	if (matcher.matches() != false)
	    {		
		sb.append(url);
		sb.deleteCharAt(sb.length() - 1);
	    }
	return sb.toString();	
		
    }
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
	String u = getVOTURL();
        System.out.println("URL is: "  +  u );
	String[] response = null;
	response  = Utils.runImageTest(u,"ssa");
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
        String pos         = Utils.fixPos(baseurl); 
	return "http:" + baseurl + pos + ra  + ","  + dec + "&SIZE="  + sr +  "&REQUEST=queryData";        
    }
}
