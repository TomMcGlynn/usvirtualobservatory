package net.ivoa.datascope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
   
import net.ivoa.registry.RegistryQuery;
import net.ivoa.util.Settings;

public class MakeMetaFile {
    
    
    public static void main(String[] args) throws Exception {
	
	
        String[]   urls = Settings.getArray("RegistryURLs");
	String[]   xsls = Settings.getArray("RegistryXSLs");
	Map<String, Map<String, List<String>>> results = RegistryQuery.query(urls, xsls);
	
	String metaFile = DS.getDataHome() + DS.getMetadataFile();
        File f = new File(metaFile+".tmp");
	ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
	os.writeObject(results);
	File fn = new File(metaFile);
	f.renameTo(fn);
    }
}
