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
	HashMap h = new HashMap();
	h.put("Apparent communication error produced an exception inside the validater:.*","1");
	h.put(".*Element TABLE\\:.*This element is not expected\\. Expected is \\( RESOURCE \\).*","1");
	
	try {
	    for (int i =0;i<c_subtestarray.size();i++)
		{
		    SubTest sub        = (SubTest) c_subtestarray.get(i);             
		    String runid       = Integer.toString(c_rid);
		    String res         = sub.getResult(); 		      
		    res                = filter_trim(res,h);		       
		    String subtestid   = sub.getSubTestId();
		    
		    
		    if (! subtestid.equals("null"))
			{	
			    String rescodenum_or_errorid = null;
			    
			    if (c_errorcodes.containsKey(res)) 
				{            				
				    String integer = (String) c_errorcodes.get(res);
				    int number = 1;
				    if (integer != null)
					{
					    number      =  Integer.valueOf(integer).intValue(); 
					}
				    rescodenum_or_errorid  = number + "";
				    
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
				    rescodenum_or_errorid = erroridstring;
				    
				}      
			    
			    List types     = new ArrayList();
			    List columns   = new ArrayList();
			    columns.add("runid");
			    columns.add("subtestid");
			    columns.add("validationResCode");
			    types.add(runid);
			    
			    
			    if (sub.getOveride())
				{				    
				    subtestid = sub.getClassType() + rescodenum_or_errorid;
				}
			    types.add(subtestid);
			    types.add(rescodenum_or_errorid);
			    this.writeRow(types,columns,"Errors");
			}			  			       
		}	
	}
	catch(Exception e)
	    {
		System.out.println("Exception in the writeTable() routine " + e);
	    }
    }
    public String filter_trim(String res,HashMap h)
    {

	Set set = h.entrySet();
	Iterator i = set.iterator();
	while(i.hasNext())
	    {
		
		Map.Entry me = (Map.Entry)i.next();		

		String regexp = (String) me.getKey();		
		Pattern pattern       = Pattern.compile(regexp,Pattern.DOTALL);	 
		Matcher matcher       = pattern.matcher(res);
	
		if (matcher.matches()  == true)
		    {
			res = res.substring(0,72);
		    }
		
	    }
	return res;
    }    
    
}
