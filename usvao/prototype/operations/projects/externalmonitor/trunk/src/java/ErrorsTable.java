import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.util.regex.*;



public class  ErrorsTable extends BaseTable
{
    public ErrorsTable(ArrayList array)
    {
	super(array);

    }

    public void writeTable()
    {
	try {
		
	    for (int i =0;i<c_subtestarray.size();i++)
		{
		    SubTest sub        = (SubTest) c_subtestarray.get(i);             
		    String runid       = Integer.toString(c_rid);
		    String res         = sub.getResult(); 
		    res                = filter_trim(res);
		    
		    String subtestid   = sub.getSubTestId();
		    
		    
		    if (! subtestid.equals("null"))
			{
			    List types     = new ArrayList();
			    List columns   = new ArrayList();
			    columns.add("runid");
			    columns.add("subtestid");
			    columns.add("monitorResCode");
			    types.add(runid);
			    types.add(subtestid);
			    
			    
			    if (c_errorcodes.containsKey(res)) 
				{            				
				    String integer = (String) c_errorcodes.get(res);
				    int number = 1;
				    if (integer != null)
					{
					    number      =  Integer.valueOf(integer).intValue(); 
					}
				    String n        = number + "";
				    
				    types.add(n);
				    
				}
			    else 
				{		
				    
				    String sqlcodes       = "insert into ErrorCodes (description)  values (?)";                 
				    PreparedStatement pr  = c_con.prepareStatement(sqlcodes, Statement.RETURN_GENERATED_KEYS);  	    
				    pr.setString(1,res);   					
				    int rc                = pr.executeUpdate();			       
				    ResultSet rs          = pr.getGeneratedKeys();
				    int errorid           = rs.next() ? rs.getInt(1): -1;   				       
				    rs.close();                       
				    pr.close();				    
				    String erroridstring = "" + errorid;
				    c_errorcodes.put(res, erroridstring); 
				    types.add(erroridstring);				       		      
				}        
			    this.writeRow(types,columns,"Errors");
			}
		}	
	}
	catch(Exception e){
	    System.out.println(e);
	}
    }
    public String filter_trim(String res)
    {	
	String regexp         = "(.*)([.?])(.*)";
	Pattern pattern       =  Pattern.compile(regexp, Pattern.DOTALL);
	Matcher matcher       = pattern.matcher(res);
	
	if (matcher.find( ))
	    
	    {
		//System.out.println("P0" + matcher.group(1));
		//System.out.println("Cc" + matcher.group(2));
		//System.out.println("Cg" + matcher.group(0));
		res =  matcher.group(3);		
		res = res.replaceAll("\\s*$","");
		res = res.replaceAll("^\\s*","");
		
	    }
	return res;
    }
    

}
