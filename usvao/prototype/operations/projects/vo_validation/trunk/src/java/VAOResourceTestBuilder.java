import java.io.*;
import java.net.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.Random;
import java.lang.reflect.*;
import java.text.*;
import java.sql.*;


public class VAOResourceTestBuilder
{    
    private String handlername;
    private String baseurl;
    private String ivoid;
    private String validatorurl = "http://rofr.ivoa.net/regvalidate/VOResourceValidater?record=&record=&recordURL=";
    private String valpars      = "&recordURL=&format=xml&show=fail";
    private String query;
    
    public VAOResourceTestBuilder(String baseurl_in, String handlername_in,String ivoid_in)
    {
	ivoid       = ivoid_in; 
	handlername = handlername_in;
	baseurl     = "http://nvo.stsci.edu/vor10/getRecord.aspx?id="  +ivoid + "&format=xml";
    }    
    public String getValUrl()
    {      
	try
	    {
		
		String encoded   = URLEncoder.encode(baseurl, "UTF-8");	      	       
		query            = validatorurl + encoded + valpars;
	    }	   
	catch(UnsupportedEncodingException e)
	    {
		System.out.println(e);
	    }
	return query;	    
    }  
 
}
