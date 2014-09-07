package net.ivoa.datascope;

import java.util.Map;
import java.util.List;

import java.io.ObjectInputStream;
import java.io.FileInputStream;

/** Dump the current metadata file. */
public class MetaDump {
    
    public static void main(String[] args) throws Exception {
	
	ObjectInputStream is = new ObjectInputStream(new FileInputStream(DS.getDataHome() + DS.getMetadataFile()));
	Map<String, Map<String, List<String>>> meta = (Map<String, Map<String, List<String>>>)is.readObject();
        dump(meta);
    }
    
    public static void dump(Map<String, Map<String, List<String>>> meta) {
	
	for (String id: meta.keySet()) {
	
            Map<String, List<String>> result = meta.get(id);
	
	    String sn=" --unnamed-- ";
	    List<String> snList = result.get("ShortName");
	    if (snList != null  && snList.size() > 0) {
		sn = snList.get(0);
	    }
	    System.out.println("\n*** "+ sn+" ***\n");
	    
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
    
    private static void printLine(String key, Map<String, List<String>> resource) {
	
	List<String> values = resource.get(key);
	if (values != null  && values.size() > 0) {
	    System.out.println("   "+key);
	    for (String line: values) {
	        System.out.println("      "+line);
	    }
	}
    }
}
