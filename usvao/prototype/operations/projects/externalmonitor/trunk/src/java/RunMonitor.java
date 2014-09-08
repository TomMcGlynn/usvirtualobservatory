import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;

import java.util.regex.*;


public class RunMonitor
{
    public static void main(String[] args)
    {
	//process command line options
      	    
        MySqlConnection mycon = new MySqlConnection();
        Connection con        = mycon.getConnection(); 

	Utils.processOptions(args);
     	
	if  (con != null)
	    {	     
		System.out.flush();
		System.out.println("Connected to the db correctly ...");
	      
		HashMap errorcodes      =  Utils.getErrorCodes(con);           
                Utils.printErrorCodes(errorcodes);
		Calendar  now = Calendar.getInstance();
		ResultSet result;
		if (FlagHolder.getflag().equals("service"))
		    {
			
			result = Utils.getServiceData(con,now);
		    }
		else
		    {			
			result = Utils.getData(con,now); 
		    }
		HashMap url_list     =  Utils.buildURLs(result);
		try
		    {
			RunTests.runTests(url_list,con,errorcodes ); 
		    }
		
		catch (IOException e)
		    {
			
		    }
	catch (Exception e)	
	{
	}	    
	
	    }    
    }
}
                
         
   

 
    
   

   
       
   
	      

      







