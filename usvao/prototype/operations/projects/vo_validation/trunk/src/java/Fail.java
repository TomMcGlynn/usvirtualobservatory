import java.io.*;
import java.util.*;


public class Fail
{
    protected ArrayList c_array;
    protected int c_runid;
    protected String c_date;

    public Fail(ArrayList array, int runid, String date)
    {
	c_array = array;
	c_runid = runid;
	c_date = date;
      
	
    }
    public ArrayList  getnewArray()
    {   	
	SubTest sub = new SubTest("empty string","none","fail","null");
	ArrayList array_subtestobjects = new ArrayList();
	array_subtestobjects.add(sub);
	c_array.add(new Integer(c_runid));
	c_array.add(array_subtestobjects);
	c_array.add(c_date);
	return c_array;
    }   
}

