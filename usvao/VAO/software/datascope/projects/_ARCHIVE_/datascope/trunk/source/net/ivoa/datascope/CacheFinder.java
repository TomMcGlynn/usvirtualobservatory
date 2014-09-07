package net.ivoa.datascope;

import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;

import net.ivoa.util.Settings;

import java.util.Date;

/** Class to manage the creation of the cache directory if needed */
public class CacheFinder {
    
    /** The name of the cache directory */
    private String name;
    
    /** The File pointing to the cache direcotry */
    private File cacheDirectory;
    
    /** Was the cache pre-existing */
    private boolean existed = true;
    
    /** Locate */
    public CacheFinder() throws Exception {
	
	boolean needNew = Settings.get("skipDataCache").equals("true");
							     
	StringBuilder cds =  new StringBuilder(Settings.get("ra"));
	cds.append("_");
	cds.append(Settings.get("dec"));
	cds.append("_");
	cds.append(Settings.get("size"));
	if (Settings.has("resources")) {
	    cds.append("_");
	    cds.append(Settings.get("resources"));
	}
	name=cds.toString().hashCode() + "";
	name           = DS.getCacheHome()+name+File.separator;
	
	cacheDirectory = new File(name);
	if (cacheDirectory.exists()) {
	    if (needNew) {
		File file = new File(name+DS.timingFile());
		if (file.exists()) {
		    DataInputStream ds = new DataInputStream(new FileInputStream(file));
		    long time = ds.readLong();
		    ds.close();
		    if ( (new Date().getTime()-time) < DS.getMinPurgeTime()) {
			throw new Exception("Unable to refresh cache: cache created too recently.\n"+
					    "Reissue query without cache refresh request, or wait 5 minutes.");
		    }
	        }
		delete();
	    }
	}
	
	if (!cacheDirectory.exists()) {
	    if (!cacheDirectory.mkdir()) {
		throw new Exception("Unable to create cache directory: "+name);
	    }
	    DataOutputStream ds = new DataOutputStream(
			            new FileOutputStream(name+DS.timingFile())
						      );
	    long cacheTime = new Date().getTime();
	    // Identify any requests we make from this cache.
	    Service.setServiceID("DS"+cacheTime);
	    ds.writeLong(cacheTime);
	    ds.close();
	    existed = false;
	}
		
    }
    
    /** Return the name of the cache directory. */
    public String getCacheBase() {
	return DS.homeToBase(name);
    }
    
    /** Return the name of the cache directory. */
    public String getCacheHome() {
	return name;
    }
    
    /** Return the File designating the directory */
    public File getFile() {
	return cacheDirectory;
    }
    
    /** Did the directory already exist? */
    public boolean existed() {
	return existed;
    }
    
    /** Delete this cache directory */
    public void delete() throws Exception {
	rmdir(cacheDirectory);
    }
    
    /** Delete a directory and any subdirectories */
    private static void rmdir(File dir) throws Exception {
	
	for (File f: dir.listFiles()) {
	    if (f.isDirectory()) {
		rmdir(f);
	    } else {
		if (!f.delete()) {
		    throw new Exception("Unable to delete file "+f.getName()+" in old cache directory "+dir.getName());
		}
	    }
	}
	if (!dir.delete()) {
	    throw new Exception("Unable to delete old cache directory "+dir.getName());
	}
    }
}
