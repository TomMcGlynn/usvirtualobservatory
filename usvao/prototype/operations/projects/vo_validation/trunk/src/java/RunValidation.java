import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.Properties;
import java.text.*;

import java.util.regex.*;


public class RunValidation 
{  
   
    public static void main(String[] args)
    {
	//Set global var print_line
       //debug option on or off
        String  debug  = System.getProperty("debug");	
        
        MySqlConnection mycon = new MySqlConnection();
        Connection con        = mycon.getConnection(); 

	//process command line options  	    
	Utils.processOptions(args);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	//get current date time with Date()
       java.util.Date date = new java.util.Date();
 
       
       System.out.println("********* Validation of VO Resources ********** ");
       System.out.println("Start time: "  + dateFormat.format(date));   
       
       if  (con != null)
	   {	     
	       System.out.flush();
	    System.out.println("you are not ssn");   
	       // Extract data by: 
	       //a)The default - by time order and by number of rows
	       //b)Special     - by identifier.
	       
	       
	       
	       //default number of services to validate;
	       int rowlimit  = Utils.getRowLimit();
	       
	       
	       //get never validated and those services that have oldest val timestamp
	       System.out.println("Total number of services to test: " + rowlimit);
	       ReadTable RT            =  new ReadTable(con, "Services",rowlimit);
	       ResultSet result        =  RT.readnewServiceTable(FlagHolder.getflag(),FlagHolder.getspecialid()); 
	       ResultSet resultnew      =  RT.readnewServiceTable(FlagHolder.getflag(),FlagHolder.getspecialid()); 
	       HashMap testtypes       =  Utils.getTestTypes(con);
	       HashMap serviceobjects  =  GetServices.getServiceObjects(result, testtypes);
	       
               //get errorcodes and store
	       HashMap errorcodes      =  Utils.getErrorCodes(con);	       
	       Utils.printErrorCodes(errorcodes);
	       
	       
	       try {
	             RunValidator.validateServiceObjects(serviceobjects,con,errorcodes);
			
	       }
	       catch (SQLException sqle)
		   {
		       System.out.println("********"  + sqle);
		   }
	       catch (WrongInputException w)
		   {
		       System.out.println(w);
		    }
	       catch (Exception e)
		   {
		       e.printStackTrace();
		   }  
            
        } 
            
    }
}




 
    
   

   
       
   
	      

      







