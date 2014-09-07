package net.ivoa.datascope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
    
import net.ivoa.registry.RegistrySearch;

public class MakeMetaFile {
    
    
    public static void main(String[] args) throws Exception {
	
	HashMap<String, HashMap<String, String[]>> results = null;
	
        String   registry = DS.getRegistryURL();
	RegistrySearch rs = new RegistrySearch(registry+"?WSDL");
	
	for (String criterion: DS.getRegistryQuery() ) {
	    
            HashMap<String, HashMap<String, String[]>> res = rs.query(criterion);
	    if (res != null) {
		if (results == null) {
		   results = res;
		} else {
		   results.putAll(res);
		}
	    }
	    
	}
	
	String metaFile = DS.getDataHome() + DS.getMetadataFile();
        File f = new File(metaFile+".tmp");
	ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
	os.writeObject(results);
	File fn = new File(metaFile);
	f.renameTo(fn);
    }
}
