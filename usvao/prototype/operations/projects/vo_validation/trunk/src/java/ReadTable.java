import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;



public class ReadTable
{
    private String c_tablename;
    private Connection c_con;
    private int c_rowlimit;

    public ReadTable(Connection con, String tablename, int rowlimit)
    {
	c_tablename = tablename;
	c_con      = con;
	c_rowlimit = rowlimit;
	
    }
    public ResultSet readTable()
    {

	ResultSet result = null;
        
        try
            {
                Statement stmt   =  c_con.createStatement();
                String query     = "select * from " + c_tablename;
                result = stmt.executeQuery(query);
            }
        catch (Exception e)
            {
                System.out.println(e);
            }
        return result;
    }
    
    public ResultSet readnewServiceTable(String flag, String id_entered)
    {
	ResultSet r =null;
	ResultSet rnew =r;
	
					  
	if (id_entered == null )
	    {
	
		try
		    {             		
			Statement newst  = c_con.createStatement();
			String querynew  =  "(select s.serviceId, s.shortname ,' ', ' ', s.serviceURL, " + 
			                    "  s.xsitype, s.ivoid, s.test_ra,s.test_dec, " +
			                    " s.radius, s.role from  Services s  where  s.deleted is null and s.serviceId not in " +
			                    " (select serviceId from Tests))" + 	                                 
			                    " union all" +
			                    " ( select Tests.serviceId,m.shortname, Tests.validationstatus,m.bigdate, " + 
			                    " m.serviceURL,m.xsitype, m.ivoid, m.test_ra, " + 
			                    "  m.test_dec, m.radius,Tests.runid from  ( select s.shortname, s.xsitype, s.ivoid, " +
			                    " s.test_dec, s.radius,s.test_ra," + 
			                    " s.serviceURL, t.serviceId, max(t.time) as bigdate  from Tests t, Services s " + 
			                    " where   s.serviceId = t.serviceId  and s.deleted is null group by t.serviceId)" +  
			                    " as m join Tests on Tests.serviceId = m.serviceId  and  " + 
		                            " Tests.time = m.bigdate  order by  Tests.time asc limit "    + c_rowlimit  + " ) limit " +  c_rowlimit; 	     	 
                        //System.out.println(querynew); 
			r                = newst.executeQuery(querynew);
		    }
		catch (Exception e)
		    {		       
			System.out.println(" ... you have an exception in ReadTable.java"   + e);
		    }
	    }
	else
	    {
		try 
		    {
			//The user has entered an identifier or filename		
			//Is this an authid or unique id?
			String regexp            = "ivo://.*/.*";
			Pattern pattern       =  Pattern.compile(regexp);
			Matcher matcher       = pattern.matcher(id_entered);
		        if (matcher.matches() == false)
			    {  System.out.println("The identifier could not be matched by the regexp");
				id_entered = id_entered + "%";
			    }
			
			Statement newst  = c_con.createStatement();
			String querynew  =  "select s.serviceId, s.shortname ,' ', ' ', s.serviceURL, " + 
			                    "  s.xsitype, s.ivoid, s.test_ra,s.test_dec, " +
			                    " s.radius, s.role from  Services s  where  s.ivoid like '" + id_entered  + "'";
			r                = newst.executeQuery(querynew);
		    }
		catch (Exception e)
		    {
			System.out.println("...you have an exception in ReadTable.java: " +e);
		    }
	    }
	return r;
    }
}
