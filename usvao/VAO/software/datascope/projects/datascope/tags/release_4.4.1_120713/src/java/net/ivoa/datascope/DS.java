package net.ivoa.datascope;

/** This is a package defining constants and simple utilities
 *  used in DataScope.
 * 
 */

import java.io.File;
import net.ivoa.util.Settings;

public final class DS {

	private static String HOST = "heasarc.gsfc.nasa.gov";

	private static String URLBase = "/vo/datascope/";
	private static String CGIBase = "/cgi-bin" + URLBase;
	private static String CacheBase = "/vocache/";

	private static String URLPrefix = "/www/htdocs";

	private static String URLHome = URLPrefix + URLBase;
	private static String CGIHome = URLPrefix + CGIBase;
	private static String CacheHome = URLPrefix + CacheBase;
	private static String XSLHome = URLPrefix + URLBase + "xsl/";

	private static String BaseData = "/vodata/";
	private static String metadataFile = "metadata.file";

	private static String stemName = "metadata.stem";
	private static String statName = "current.status";
	private static String logName = "datascope.log";

	private static int timeOut = 30000; // Milliseconds
	private static boolean timeoutUpd = false;

	// private static int minPurgeTime = 300000; // 5 minutes
	private static int minPurgeTime = 3000; // 5 minutes
	private static boolean minPurgeUpd = false;

	private static double dftSize = 0.25; // Degrees
	private static boolean dftSizeUpd = false;
	
	private static int maxTarSize = 30000000; // bytes
	private static boolean maxTarSizeUdp = false;

	private static String RegistryURL = "http://nvo.stsci.edu/vor10/NVORegInt.asmx/VOTCapabilityPredOpt";
	private static String[] RegistryQuery = new String[] {
			"capability=SimpleImageAccess&VOTStyleOption=2&predicate=",
			"capability=ConeSearch&VOTStyleOption=2&predicate=",
			"capability=SimpleSpectralAccess&VOTStyleOption=2&predicate="};

	private static String[] RegistryTranslators = new String[] { "sia.xsl",
			"cone.xsl" };

	/** DataScope host */
	public static String getHost() {
		return set(HOST, "HOST");
	}

	/** URL Prefix */
	public static String getURLPrefix() {
		return set(URLPrefix, "URLPrefix");
	}

	/** Base of standard URLs (as a URL) */
	public static String getURLBase() {
		return set(URLBase, "URLBase");
	}

	/** Base of standard URS (as a file) */
	public static String getURLHome() {
		return getURLPrefix() + getURLBase();
	}

	/** Base of cached files (as a URL) */
	public static String getCacheBase() {
		return set(CacheBase, "CacheBase");
	}

	/** Base of cached files (as a file) */
	public static String getCacheHome() {
		return getURLPrefix() + getCacheBase();
	}

	/** Base of CGI scripts (as a URL) */
	public static String getCGIBase() {
		return set(CGIBase, "CGIBase");
	}

	/** Base of CGI scripts (as a file) */
	public static String getCGIHome() {
		return getURLPrefix() + getCGIBase();
	}
	
	public static String getBaseData() {
		return set(BaseData, "BaseData");
	}

	
	/** Where non-URL data files are stored */
	public static String getDataHome() {
		return getURLPrefix() + getBaseData();
	}

	public static String getXSLHome() {
		return XSLHome;
	}

	/** How many milliseconds until a request should time out */
	public static int getTimeout() {
		if (!timeoutUpd) {
			if (Settings.has("Timeout")) {
				timeOut = parseInt(Settings.get("Timeout"), timeOut);
			}
			timeoutUpd = true;
		}
		return timeOut;
	}

	/** Size to use if not specified by user */
	public static double getDefaultSize() {
		if (!dftSizeUpd) {
			if (Settings.has("dftSize")) {
				dftSize = parseDouble(Settings.get("dftSize"), dftSize);
			}
			dftSizeUpd = true;
		}
		return dftSize;
	}
	
	
	public static int getMaxTarSize(){
		if (!maxTarSizeUdp){
			if (Settings.has("maxTarSize")){
				maxTarSize = parseInt(Settings.get("maxTarSize"), maxTarSize);
			}
			maxTarSizeUdp = true;
		}
			
		return maxTarSize;
	}
	

	/** Standard coordinate system */
	public static String getCoordinates() {
		if (Settings.has("Coordinates")) {
			return Settings.get("Coordinates");
		} else {
			return "J2000";
		}
	}

	/** Standard equinox */
	public static double getEquinox() {
		return 2000;
	}

	/** Standard resolver */
	public static String getResolver() {
		if (Settings.has("Resolver")) {
			return Settings.get("Resolver");
		} else {
			return "SIMBAD-NED";
		}
	}

	/** Minimum time before we will rewrite a cached entry */
	public static int getMinPurgeTime() {
		if (!minPurgeUpd) {
			if (Settings.has("MinPurgeTime")) {
				minPurgeTime = parseInt(Settings.get("MinPurgeTime"),
						minPurgeTime);
			}
			minPurgeUpd = true;
		}

		return minPurgeTime;
	}

	/** Convert a file name to a url */
	public static String homeToBase(String home) {
		String prefix = getURLPrefix();
		return home.substring(prefix.length());
	}

	/** Convert a URL to a file */
	public static String baseToHome(String base) {
		return getURLPrefix() + base;
	}

	/** The name of the file containing the time the cache entry was created */
	public static String timingFile() {
		if (Settings.has("TimingFile")) {
			return Settings.get("TimingFile");
		} else {
			return "date.created";
		}
	}
	
	public static String lockFile() {
		if (Settings.has("LockFile")) {
			return Settings.get("LockFile");
		} else {
			return ".cacheLock";
		}
	}
	
	/**
	 * An amount to add to the filter to accommodate the intrinsic sizes of the
	 * service requests.
	 */
	public static double getFilterQueryFudge() {
		double val = 0.25;
		if (Settings.has("FilterQueryFudge")) {
			val = parseDouble(Settings.get("FilterQueryFudge"), val);
		}
		return val;
	}

	public static String encode(String input) {
		if (input != null) {
			try {
				input = java.net.URLEncoder.encode(input, "UTF-8");
				input = input.replace("+", "%20");
			} catch (Exception e) {
			}
		}
		return input;
	}

	public static String validFileName(String input) {
		return input.replaceAll("\\W", "_");
	}

	public static String getRegistryURL() {
		if (Settings.has("RegistryURL")) {
			return Settings.get("RegistryURL");
		} else {
			return RegistryURL;
		}
	}

	public static String[] getRegistryQuery() {
		if (Settings.has("RegistryQuery")) {
			return Settings.getArray("RegistryQuery");
		} else {
			return RegistryQuery;
		}
	}

	public static String[] getRegistryTranslators() {
		return RegistryTranslators;
	}

	public static double parseDouble(String str, double dft) {
		double val = dft;
		if (str != null) {
			try {
				val = Double.parseDouble(str);
			} catch (Exception e) {
				val = dft;
			}
		}
		return val;
	}

	public static int parseInt(String str, int dft) {
		int val = dft;
		if (str != null) {
			try {
				val = Integer.parseInt(str);
			} catch (Exception e) {
				val = dft;
			}
		}
		return val;
	}

	public static String getMetadataFile() {
		return set(metadataFile, "MetadataFile");
	}

	public static String getMetadataStem() {
		return set(stemName, "MetadataStem");
	}

	public static String getStatusFile() {
		return set(statName, "StatusFile");
	}

	public static String getLogName() {
		return set(logName, "LogFile");
	}

	public static String checkFile(String base) {
		File f;
		f = new File(base);
		if (f.exists()) {
			return base;
		}
		f = new File(base + ".gz");
		if (f.exists()) {
			return base + ".gz";
		}
		f = new File(base + ".Z");
		if (f.exists()) {
			return base + ".Z";
		}
		return null;
	}

	private static String set(String dft, String setting) {
		if (Settings.has(setting)) {
			return Settings.get(setting);
		} else {
			return dft;
		}
	}

	public static String getQueryLog() {
		return DS.getCacheHome() + "query.log";
	}
	
	public static String getVersionMessage() {
		if (Settings.has("VersMessage")) {
			return Settings.get("VersMessage");
		} else {
			return "";
		}
	}

	static java.io.PrintWriter logWriter;

	public synchronized static void setLogLocation(String dir) {
		try {
			logWriter = new java.io.PrintWriter(new java.io.FileWriter(dir
					+ "/processing.log"));
		} catch (Exception e) {
			System.err.println("Unable to create log file in " + dir);
		}
	}

	public synchronized static void log(String msg) {
		if (logWriter != null) {
			logWriter.println(new java.util.Date() + ":" + msg);
			logWriter.flush();
		}
	}
}
