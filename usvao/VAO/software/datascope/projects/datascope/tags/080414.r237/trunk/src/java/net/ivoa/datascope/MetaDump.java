package net.ivoa.datascope;

import java.util.HashMap;

import java.io.ObjectInputStream;
import java.io.FileInputStream;

/** Dump the current metadata file. */
public class MetaDump {
    
    public static void main(String[] args) throws Exception {
	
	ObjectInputStream is = new ObjectInputStream(new FileInputStream(DS.getDataHome() + DS.getMetadataFile()));
	HashMap<String, HashMap<String, String[]>> meta = (HashMap<String, HashMap<String, String[]>>)is.readObject();
	for (String id: meta.keySet()) {
	
            HashMap<String, String[]> result = meta.get(id);
	
	    
	    System.out.println("\n*** "+result.get("ShortName")[0]+" ***\n");
	    
	    printLine("Title", result);
	    printLine("ShortName", result);
	    printLine("Description", result);
	    printLine("Identifier", result);
	    String[] keys = result.keySet().toArray(new String[0]);
	    java.util.Arrays.sort(keys);
	    
	    for (String key: keys) {
		if (!key.equals("Title") && !key.equals("ShortName") && !key.equals("Description")  &&
		    !key.equals("Identifier")) {
		    printLine(key, result);
		}
	    }
	}
    }
    
    private static void printLine(String key, HashMap<String, String[]> resource) {
	
	String[] values = resource.get(key);
	System.out.println("   "+key);
	for (int i=0; i<values.length; i += 1) {
	    System.out.println("      "+values[i]);
	}
    }
}
