import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;


public abstract class BaseTable implements DBTable
{
    protected ArrayList   c_subtestarray;  
    protected Statement   c_stmt;
    protected Connection  c_con;
    protected String      c_date; 
    protected HashMap     c_errorcodes; 
    protected ArrayList   c_arraylist;
    protected Integer     c_rid;
    protected String      c_sid;
    protected Long        c_utc;
    protected String      c_testname;
    protected  String  c_tid;	
    
    public BaseTable(ArrayList array)
    {
	
       	c_stmt        = (Statement) array.get(0);	 
        c_utc         = (Long) array.get(1);
	c_con         = (Connection) array.get(2);
	c_errorcodes         = (HashMap) array.get(3);
        c_rid          = (Integer) array.get(4);   
        c_date         = (String) array.get(5);
	c_sid          = (String) array.get(6);	
        c_tid          = (String) array.get(7);
	c_testname     = (String) array.get(8);
	c_subtestarray = (ArrayList) array.get(9);     

    }

    public ArrayList readRow()
    {
	ArrayList array = new ArrayList();
	return array;
	
    }
  
    protected PreparedStatement prepareWrite(String sql,List  types, Connection con) throws Exception
    {	
	PreparedStatement prest = con.prepareStatement(sql);
	for (int i=0;i<types.size();i++)
	    {		
		prest.setString(i+1,(String) types.get(i));
	    }	return prest;
    }
    public  void writeRow(List types,List columns,String tablename) throws Exception
    {
 
        String sql           = "insert into " + tablename + " (";
	String questionchars = "(";
        int j = columns.size();
	
	
	for (int i = 0; i<columns.size();i++)
	    {
		if (i  == columns.size()-1)
		    {
			sql += columns.get(i);
			questionchars += "?";
		    }
		else
		    {
			sql += columns.get(i) + ",";
			questionchars += "?,";
		    }		
	    }
	
        sql         +=  ")" + " values" +  questionchars + ")";	
	System.out.println(sql);
        PreparedStatement prest   = prepareWrite(sql,types, c_con);       
        prest.executeUpdate();
    }
}
