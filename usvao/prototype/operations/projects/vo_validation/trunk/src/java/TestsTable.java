import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class TestsTable extends BaseTable
{

    public TestsTable(ArrayList array)
    {
	super(array);
    }

    protected void writeTable() 
    {
	try 
	    {
		String status    = "pass";
		SubTest subtest  = (SubTest) c_subtestarray.get(0);     
	       	           
		//see if any subtests failed. If so, status is "fail";
		for (int i = 0;i< c_subtestarray.size();i++)
		    {           
			SubTest sub = (SubTest) c_subtestarray.get(i);
			if (sub.getStatus().equals("fail"))
			    {
				status = "fail";
				break;
			    }
			else if (sub.getStatus().equals("abort"))
			    {
				status = "abort";
				break;
			    }
			else if (sub.getStatus().equals("skip"))
			    {
				status = "skip";
				break;
			    }
		    }
		
	
		
		List columns = new ArrayList();
		columns.add("runid");
		columns.add("serviceId");
		columns.add("validationstatus");
		columns.add("type");
		columns.add("time");
		columns.add("validatorCode");
		List types = new ArrayList();
		String ridstring = Integer.toString(c_rid);
		types.add(ridstring);
		types.add(c_sid);
		types.add(status);
		types.add(c_type);
		types.add(c_date);
		types.add(c_valcode);
		this.writeRow(types,columns,"Tests");	     
	    }
	catch (Exception e)
	    {
		System.out.println(e);
	    }	
    }
}

