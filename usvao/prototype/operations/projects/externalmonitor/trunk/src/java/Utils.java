import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;



public class Utils
{
   
    
    public static ResultSet getData(Connection con)
    {
	ResultSet result = null;
        
        try
            {
                Statement stmt   =  con.createStatement(); 
                String query     = "(select s.serviceId,  s.name, t.testid,testname from Tests t, Services s ";
	        query           += " where t.serviceId =  s.serviceId and t.deleted is null group by t.testid)";
                result = stmt.executeQuery(query);
            }
        catch (Exception e)
            {
                System.out.println(e);
            }
        return result;
    }
    public static ResultSet getServiceData(Connection con)
    {

	ResultSet result = null;
        String servicename = FlagHolder.getspecialid();
        try
            {
                Statement stmt   =  con.createStatement(); 
                String query     = "(select s.serviceId,  s.name, t.testid,testname from Tests t, Services s ";
	        query           += " where t.serviceId =  s.serviceId and s.name = '";
	        query           += servicename  + "' and t.deleted is null group by t.testid) ";
                result = stmt.executeQuery(query);
            }
        catch (Exception e)
            {
                System.out.println(e);
            }
        return result;
    }
   
    public static ResultSet getServiceData(Connection con, Calendar now)
    {
        int h = now.get(Calendar.HOUR_OF_DAY);
	System.out.println("hour "  + h);
	ResultSet result = null;
        String servicename = FlagHolder.getspecialid();
        try
            {
                Statement stmt   =  con.createStatement(); 
                String query     = "(select s.serviceId,  s.name, t.testid,testname from Tests t, Services s ";
	        query           += " where t.serviceId =  s.serviceId and s.name = '";
	        query           += servicename  + "' and t.deleted is null group by t.testid) ";
                result = stmt.executeQuery(query);
            }
        catch (Exception e)
            {
                System.out.println(e);
            }
        return result;
    }
    public static ResultSet getData(Connection con, Calendar now)
    {
        int h = now.get(Calendar.HOUR_OF_DAY);
	System.out.println("hour "  + h);
	ResultSet result = null;
	
        try
            {
                Statement stmt   =  con.createStatement(); 
                String query     = "(select s.serviceId,  s.name, t.testid,testname from Tests t, Services s ";
	        query           += " where t.serviceId =  s.serviceId ";
	        query           +=  " and t.deleted is null and mod (" + h  + ", s.cadence) =0"; 
		query           += " group by t.testid)";
                result = stmt.executeQuery(query);
		System.out.println(query);
            }
        catch (Exception e)
            {
                System.out.println(e);
            }
        return result;
    }

    public static void processOptions(String[] args)
    {   
        MySqlConnection mycon = new MySqlConnection();
        Connection con        = mycon.getConnection(); 
       
	
	FlagHolder.storeflag("default",null); //no options used
        if (args.length == 1)
	    {		
		FlagHolder.storeflag("service",args[0]);
	    }
	else if (args.length == 2)
	    {
		if  (args[1].matches("\\w"))		    
		    {
			FlagHolder.storeflag("service",args[0],"T");
			System.out.println("the cat in thehat");
		    }
	    }
    }
    public static HashMap buildURLs(ResultSet result)
    {
	HashMap hash =  new HashMap();
	try
	    {
		while (result.next())
		    {
			String serviceId = result.getString("serviceId"); 
			
			String name = result.getString("name");
		      
			String testid = result.getString("testid");
			
			String testname = result.getString("testname");
			String value    = serviceId + ":" + name + ":" + testid + ":" + testname;
			String http  =  "http://"; 
			String url  =  "heasarcdev.gsfc.nasa.gov/vo/external_monitor/test.pl?name=";
		        
			
			    
			try
			    {
				//url  = URLEncoder.encode(url, "UTF-8");                
				
				name = URLEncoder.encode(name, "UTF-8");
				testid = URLEncoder.encode(testid,"UTF-8");
				
			    }
			catch(UnsupportedEncodingException e)
			  {
			      System.out.println(e);
			  }
			url = http + url + name + "&testid=" + testid  + "&testresult=yes"; 
			
			 
			 hash.put(url,value);
			System.out.println(url);
			}
	    
	    }
	catch (Exception e)
	    {
                System.out.println("GetServices.java: "  + e);
            }
	
	return hash;
    }
    public static int  getHighestRunidPlusOne(Statement stmt)
    {
        int largestrunid = 1;
        try
            {           
               
                String testtablequery   = "select runid  from Testhistory order by runid desc limit 1";                 
                ResultSet  rs       = stmt.executeQuery(testtablequery);
                boolean found;
                found = rs.next();
		if (found)
		    {                       
                        largestrunid = rs.getInt("runid");
                        largestrunid += 1;
		              
                    }
               
                             
            }
        catch (Exception e)
            {
                System.out.println(e);
            }
        return largestrunid;
    } 
    public static HashMap getErrorCodes(Connection con)
    {
        HashMap  errorcodes = new HashMap();
        ReadTable TR = new ReadTable(con,"ErrorCodes",0);
        ResultSet result  =  TR.readTable();

        try 
            {
                while (result.next())
                    {                  
                        String id        = result.getString("monitorResCode");
		      
                        String message   = result.getString("description");                     
                        errorcodes.put(message,id);
                    }   
            }
        catch (Exception e)
            {
                System.out.println(e);
            }   
        return errorcodes;
    }  
    public static void printErrorCodes(HashMap errorcodes)
    {   
        Iterator it = errorcodes.entrySet().iterator();
        
        while(it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                String a = (String)entry.getKey();
                String b = (String)entry.getValue();     
	    
                
            }
    }

    


}
