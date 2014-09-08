import java.io.*;
import java.util.*;
import java.sql.*;
import java.net.*;
import java.text.*;

public class RunValidator
{
    public static void  validateServiceObjects(HashMap serviceobjects, Connection con,HashMap errorcodes)
	throws Exception, IOException, MalformedURLException, WrongInputException
	{
	    //Get system date and time
	   
	    Long utc                =  System.currentTimeMillis();
	    
            //create sql statement 
	    Statement stmt          = null;             
	    stmt                    = con.createStatement();    
	    DateFormat dateFormat   = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    Iterator it             =  serviceobjects.entrySet().iterator();
	    String  flag          = FlagHolder.getflag(); 
	    while(it.hasNext())
		{
                Map.Entry entry = (Map.Entry)it.next();               
                Service  s      = (Service)entry.getValue();
		String procstatus = s.getProcStatus();
                String originalbaseurl = s.getOriginalBaseURL();
		
                if (originalbaseurl.equals("null")) { 
		    System.out.println("Since the original base url is null, test cannot be run ...skipping");
		    it.remove();
		    continue;
		}
	        String identifier = s.getIvoId();
		ArrayList array = new ArrayList();
                Long t = new java.util.Date().getTime();
                java.util.Date dr  = new java.util.Date(t);
                DateFormat format  = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("EST"));
                String date  = format.format(dr);
                 
                int    runid            = Utils.getHighestRunidPlusOne(stmt); 
                array.add(s.getQuery());
                array.add(s.getId());
                array.add(stmt);
                array.add(s.getXsiType());
                array.add(utc);
                array.add(con);
                array.add(errorcodes);
                array.add(s.getValidatorId());
                array.add(s.getIvoId());
                s.test(array,runid, date,procstatus);
                if (System.getProperty("debug") != null){     
                System.out.println("End of processing\n\n\n");
                }
		}
	    con.close();
	}    
}
