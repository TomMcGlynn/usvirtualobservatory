import java.util.*;


public class  TestWrapper
{
    protected ArrayList array;
    protected ArrayList array_subtestobjects;

    public TestWrapper(ArrayList array,ArrayList array_subtestobjects)
    {
	this.array = array;
	this.array_subtestobjects = array_subtestobjects;
    }
    public void processData()
    {
	
           
          
        if (array_subtestobjects.size() == '0')
            {
                addZeroSizeArray();
            }      
        else
            {
                SubTest subempty = (SubTest) array_subtestobjects.get(0);
		array.add(array_subtestobjects);
	   }
       
	DumpToDb.writeToDb(array);
	
		
    }
    public void addZeroSizeArray()
    {
	ArrayList subtestobjects = null;
        SubTest sub = new SubTest("cannot retrieve page/connection problem",
                                  "none","abort","error parsing page");
        subtestobjects.add(sub);
       
        array.add(subtestobjects);
                
        
    }


}
