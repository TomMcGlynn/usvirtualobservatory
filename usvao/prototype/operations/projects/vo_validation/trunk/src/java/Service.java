import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.Random;
import java.lang.reflect.*;
import java.sql.*;


public abstract class Service  implements TestRunner
{
    protected String shortname;
    protected String ra;
    protected String dec;
    protected String ivoid;
    protected String type;
    protected String role;
    protected String xsitype;
    protected String sr;
    protected String originalbaseurl;
    protected String baseurl;
    protected String query;
    protected String id;
    protected String validatorurl;
    protected String validatorid;
    protected int randomint;
    protected String testimage;
    protected SubTest urlerror;
    protected String procstatus;

 
    
    public Service(){}
   
   

    public Service init(String sname,String base,String ivoident, String ra_in, String dec_in,String sr_in, 
			String role_in,String xsi,String sid,  String vurl,String vid,String procstatus_in)
    {
       	shortname       = sname;
	originalbaseurl = base;
	baseurl         = fixBaseUrl(base);
	ivoid           = ivoident;
	id              = sid;
	ra              = ra_in;
	dec             = dec_in;
	sr              = sr_in;
	role            = role_in;
	xsitype         = xsi;
	validatorurl    = vurl;
	validatorid     = vid;  
	procstatus      = procstatus_in;
	return this; 
	
    }

    abstract String getValUrl();

    public void setQuery(String queryinput)
    {
      
	this.query = queryinput;
    }
    public String getValidatorUrl()
    {
	return validatorurl;
    }
    public String getValidatorId()
    {
	return validatorid;
    }
    public String  getRa()
    {       
	return ra;	
    }
    public String getDec()
    {
	return dec;
    }
    public  String getRadius()
    {
	return sr;
    }
    public String  getBaseUrl()
    {
	return baseurl;
    }
    public String getOriginalBaseURL()
    {
         return originalbaseurl;  
    } 
    public String getProcStatus()
    {
	return procstatus;
    }
    public String fixBaseUrl(String base)
    {	
	TestUrlCompleteness tc  = new TestUrlCompleteness(base);
	baseurl   = tc.getUrl(); 
	
	this.urlerror  = tc.getError();
	if (urlerror != null) 
	    {
		String sn = urlerror.getResult();		
	    }
	return baseurl;                     
    }
    public String getIvoId()
    {
	return ivoid;
    }
    public String getId()
    {
	return id;
    }   
    public String getRole()
    {
	return role;
    }
    public String getXsiType()
    {
	return xsitype;
    }
    public String getShortname()
    {
	return shortname;
    }     
    public String getQuery()
    {
	return query;
    }
    public int getRandomInt()
    {
	Random randGen = new Random();
	int randomint      = randGen.nextInt(100);
        return randomint;
  	
    }
    public ArrayList addZeroSizeArray (ArrayList array, int runid,String date) 
    {
	ArrayList array_subtestobjects = null;
	SubTest sub = new SubTest("cannot retrieve page/connection problem",
				  "none","abort","error parsing page");
	array_subtestobjects.add(sub);
	array.add(new Integer(runid));
	array.add(array_subtestobjects);
	array.add(date);        
	return array;
    }
		
    public void test(ArrayList array,int runid,String date,String procstatus)  throws IOException, WrongInputException
    {   
        
	String classmethod = this.getClass().getName();
	String flag = FlagHolder.getflag();
	if (originalbaseurl.equals("null")) 
        { 
	   System.out.println("The service url is null or not defined. This test is being skipped");
          //return;
	 }
		
	if ((flag != null) && (flag.equals("toscreen")))
            {
                array=  test_default(array,runid, date, (String) array.get(8));		
            }
	else
	    {
		if (procstatus.equals("Active"))
		    {
			array =  test_default(array,runid, date, (String) array.get(8));
			DumpToDb.writeToDb(array);			
		    }
		else if (procstatus.equals("pass")) 
		    {
			Pass  tfp = new Pass(array, runid, date);
			array  =  tfp.getnewArray();
			DumpToDb.writeToDb(array);			
		    }
		
		else if (procstatus.equals("skip"))
		    {
			Skip s = new Skip(array, runid,date);
			array = s.getnewArray();
			DumpToDb.writeToDb(array);
		    }
	    }
    }
    public abstract  ArrayList test_default(ArrayList array, int runid, String date,String id);

}




