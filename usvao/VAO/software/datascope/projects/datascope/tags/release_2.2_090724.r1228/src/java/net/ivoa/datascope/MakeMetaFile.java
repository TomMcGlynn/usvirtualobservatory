package net.ivoa.datascope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.ivoa.registry.RegistryQuery;

public class MakeMetaFile {

	public static void main(String[] args) throws Exception {
		make();
	}
	
	public static void make() throws Exception {
		String registry = DS.getRegistryURL();
		ArrayList<String> urls = new ArrayList<String>();
		int i = 0;
		for(String q : DS.getRegistryQuery()){
			urls.add(i, registry + "?" + q);
			i++;
		}
		
		String xslsHome = DS.getXSLHome();
		int j = 0;
		ArrayList<String> xsls = new ArrayList<String>();
		for(String x: DS.getRegistryTranslators()){
			xsls.add(j, xslsHome + x);
			j++;
		}
		Map<String, Map<String, List<String>>> results = RegistryQuery.query(urls, xsls);


		String metaFile = DS.getDataHome() + DS.getMetadataFile();
		File f = new File(metaFile + ".tmp");
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
		os.writeObject(results);
		File fn = new File(metaFile);
		f.renameTo(fn);
		
	}
}
