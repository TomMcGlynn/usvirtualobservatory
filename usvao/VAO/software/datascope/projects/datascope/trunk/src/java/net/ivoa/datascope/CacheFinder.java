package net.ivoa.datascope;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import net.ivoa.util.Settings;

import java.util.Date;

import com.jezhumble.javasysmon.JavaSysMon;

/** Class to manage the creation of the cache directory if needed */
public class CacheFinder {

	/** The name of the cache directory */
	private String dirName;

	/** The File pointing to the cache direcotry */
	private File cacheDirectory;

	/** Was the cache pre-existing */
	private boolean existed = true;

	/** Locate */
	public CacheFinder() throws Exception {

		boolean needNew = Settings.get("skipDataCache").equals("true");

		StringBuilder cds = new StringBuilder(Settings.get("ra"));
		cds.append("_");
		cds.append(Settings.get("dec"));
		cds.append("_");
		cds.append(Settings.get("size"));
		dirName = cds.toString() + "";
		dirName = DS.getCacheHome() + dirName + File.separator;

		cacheDirectory = new File(dirName);
		if (cacheDirectory.exists()) {
			if (needNew) {
				File timeFile = new File(dirName + DS.timingFile());
				if (timeFile.exists()) {
					DataInputStream ds = new DataInputStream(
							new FileInputStream(timeFile));
					long time = ds.readLong();
					ds.close();
					if ((new Date().getTime() - time) < DS.getMinPurgeTime()) {
						throw new Exception(
								"Unable to refresh cache: cache created too recently.\n"
										+ "Reissue query without cache refresh request, or wait 5 minutes.");
					}
				}
				
				File lockFile = new File(dirName + DS.lockFile());
				if (lockFile.exists()) {
					BufferedReader  ds = new BufferedReader (
							new FileReader(lockFile));
					int pid = Integer.parseInt( ds.readLine() );
					ds.close();
					
					JavaSysMon monitor =   new JavaSysMon();
					PidFinder finder = new PidFinder(pid);
					monitor.visitProcessTree(1, finder);
					
					if( finder.found() ){
						throw new Exception(
								"Unable to refresh cache: cache still being populated by another process.\n"
										+ "Reissue query without cache refresh request, or for process to complete.");						
					}
				}
				
				delete();
			}
		}

		if (!cacheDirectory.exists()) {
			if (!cacheDirectory.mkdir()) {
				throw new Exception("Unable to create cache directory: " + dirName);
			}
			DataOutputStream ds = new DataOutputStream(new FileOutputStream(
					dirName + DS.timingFile()));
			long cacheTime = new Date().getTime();
			// Identify any requests we make from this cache.
			Service.setServiceID("DS" + cacheTime);
			ds.writeLong(cacheTime);
			ds.close();
			
			// Make a lock file
			JavaSysMon monitor =   new JavaSysMon();

			String lockName = dirName + DS.lockFile();
			PrintWriter out =
			    new PrintWriter(
			        new BufferedWriter(
			            new FileWriter(lockName) ) );

			out.println( monitor.currentPid() );
			out.close();
			
			File lockFile = new File(lockName);
			lockFile.deleteOnExit();
			
			existed = false;
		}

	}

	/** Return the name of the cache directory. */
	public String getCacheBase() {
		return DS.homeToBase(dirName);
	}

	/** Return the name of the cache directory. */
	public String getCacheHome() {
		return dirName;
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

		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				rmdir(f);
			} else {
				if (!f.delete()) {
					throw new Exception("Unable to delete file " + f.getName()
							+ " in old cache directory " + dir.getName());
				}
			}
		}
		if (!dir.delete()) {
			throw new Exception("Unable to delete old cache directory "
					+ dir.getName());
		}
	}
}
