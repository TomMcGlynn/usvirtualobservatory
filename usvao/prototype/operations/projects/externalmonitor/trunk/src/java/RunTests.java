import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;



public class RunTests 
{

    public static void  runTests(HashMap url_list,Connection con,HashMap errorcodes) throws MalformedURLException,IOException,
											   Exception
    {
        Iterator it = url_list.entrySet().iterator(); 
        int count = 0;
	Long utc                =  System.currentTimeMillis();
	//create sql statement 
	Statement stmt = null;
	stmt = con.createStatement();
	DateFormat dateFormat   = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
       
        while (it.hasNext())
            {
                //String contentstring = null;
		
                
                Map.Entry entry     = (Map.Entry)it.next();
                String url          =  (String) entry.getKey();
	 	String value        = (String) entry.getValue();       
		String[] arraynew   = value.split(":");	
	
		String sid = arraynew[0];
		String tid = arraynew[2];
		String testname = arraynew[3];
		java.util.Date utilDate   = new java.util.Date();                
                String date               = dateFormat.format(utilDate);  
		
		int runid                 = Utils.getHighestRunidPlusOne(stmt);
		ArrayList array           = new ArrayList();
		array.add(stmt);
		array.add(utc);
		array.add(con);
		array.add(errorcodes);
	       
		
		array.add(new Integer(runid));
		array.add(date);
                array.add(sid);
		array.add(tid);
		array.add(testname);
		
                ArrayList array_subtestobjects = null;
	       System.out.println(" PP " + url);	
                ResponseParser rp              = new ResponseParser(url, con);
	       
		//parse xml response
                array_subtestobjects           = rp.parseDocument();                                       
		TestWrapper  tw                = new TestWrapper(array,array_subtestobjects); 
		tw.processData();
		count++;
                
                
        
            }
        
        


    }
}
