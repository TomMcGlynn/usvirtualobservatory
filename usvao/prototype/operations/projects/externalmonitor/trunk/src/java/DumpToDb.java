import java.util.*;


public class DumpToDb
{

	public static void writeToDb(ArrayList array)
        {
        
		TestsTable TT  = new TestsTable(array);  
        	TT.writeTable();       
		ErrorsTable ET  = new ErrorsTable(array);
		ET.writeTable();
	}


}

