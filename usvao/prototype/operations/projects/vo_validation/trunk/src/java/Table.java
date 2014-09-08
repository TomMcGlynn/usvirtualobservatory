import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;

public abstract class Table
{


    protected ArrayList c_subtestarray;  
    protected Statement c_stmt;
    protected Connection c_con;
    protected String c_date; 
    protected HashMap c_errorcodes;
   
    protected String c_type;
    protected ArrayList c_arraylist;
    protected String c_valcode;
    protected int  c_rid;
    protected String c_sid;

    //spn
    public Table(ArrayList arraylist,String type, String date, 
		 Statement stmt, Connection con, HashMap errorcodes,String valcode,int rid, String sid)

    {
	c_subtestarray = arraylist;
	c_stmt = stmt;
	c_con = con;
	c_date = date;
	
	c_type = type;
	c_errorcodes = errorcodes;
	c_valcode = valcode;
	c_rid     = rid;
	c_sid    = sid;
	
    }
    protected abstract void writeTable() throws Exception;
    


   

}
