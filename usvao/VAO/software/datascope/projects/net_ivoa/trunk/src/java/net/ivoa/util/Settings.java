package net.ivoa.util;

import java.util.regex.Pattern;

/**
 * This class defines a singleton where preferences/settings can be set and
 * gotten from anywhere in the system. A setting is simply a key=string value.
 * When there is to be more than one value for the key it should be specified as
 * string1,string2,string3. A comma is not allowed as a character within a
 * setting. Keys are case insensitive.
 * <p>
 * When specified in the command line Settings may sometimes be set with just
 * the keyword. This is treated as equivalent to key=1.
 * 
 * This class was adapted from the SkyView Settings class.
 */
public class Settings {

	/** The hashmap storing the settings */
	private static java.util.HashMap<String, String> single = new java.util.HashMap<String, String>();

	/** A copy of the hashmap */
	private static java.util.HashMap<String, String> backup;

	/** Used to split the hashmap */
	private static Pattern comma = Pattern.compile(",");

	/** Used to split the hashmap */
	private static Pattern equal = Pattern.compile("=");

	static {
		initializeSettings();
	}

	// Initialize the settings.
	// First look for the file indicated in
	// the VO_SETTINGS environment variable.
	// Then try the file vo.settings.
	// 
	static void initializeSettings() {

		String settingsFile = System.getenv("VO_SETTINGS");

		if (settingsFile == null) {
			settingsFile = "vo.settings";
		}
		updateFromFile(settingsFile);
	}

	/** Try to read settings from a file */
	static void updateFromFile(String settingsFile) {

		java.io.BufferedReader ir = null;

		try {
			java.io.InputStream is = new java.io.FileInputStream(settingsFile);
			ir = new java.io.BufferedReader(new java.io.InputStreamReader(is));
			readFile(ir);
		} catch (Exception e) {
			System.out.println("No settings found");
			// Go with whatever defaults are provided.
		}
	}

	static void readFile(java.io.BufferedReader ir) {
		try {
			Pattern eq = Pattern.compile("=");
			String line;
			while ((line = ir.readLine()) != null) {

				line = line.trim();

				if (line.length() < 2 || line.charAt(0) == '#') {
					continue;
				}

				String[] parse = eq.split(line, 2);

				if (parse.length > 2) {
					System.err
							.println("Unparseable line in input settings:\n   "
									+ line);
					continue;
				}
				String val = "1";
				if (parse.length == 2) {
					val = parse[1];
				}
				char first = val.charAt(0);
				if (first == '$') {
					// Look for the environment variable and use it.
					val = System.getenv(val.substring(1));
					if (val == null) {
						continue;
					}
				}
				put(parse[0], val);
			}
		} catch (Exception e) {
			System.err.println("Exception caught parsing settings:\n" + e);
		}
	}

	/**
	 * Add settings from a list of arguments. This is probably the argument list
	 * given to main, but needn't be.
	 */
	public static void addArgs(String[] args) {

		for (String arg : args) {

			// Java seems sometimes to leave newline on last character
			// of an argument list... This is probably a bug somewhere.
			if (arg.charAt(arg.length() - 1) == 13) {
				arg = arg.substring(0, arg.length() - 1);
			}

			String[] tokens = equal.split(arg, 2);

			if (tokens.length == 2) {
				String key = tokens[0];
				put(key, tokens[1]);

			} else {
				put(arg, "1");
			}
		}
	}

	/** Don't allow anyone else to create a settings object. */
	private Settings() {
	}

	/** Get a value corresponding to the key */
	public static String get(String key) {
		return single.get(key.toLowerCase());
	}

	/** Get a values corresponding to a key or the default */
	public static String get(String key, String dft) {
		String gt = get(key);
		if (gt == null) {
			return dft;
		} else {
			// multiple leading slashes are causing a problem
			// return
			// single.get(key.toLowerCase()).replaceAll("/+","/").replace("/$","");
			// can't get rid of the dual slashes in http:// !!
			// This should probably be made application and key specific
			return gt.replace("/+$", "");
		}
	}

	/**
	 * Get the values corresponding to a key as an array of strings. Returns a 0
	 * length array if the value is not set.
	 */
	public static String[] getArray(String key) {
		String gt = get(key);
		if (gt == null) {
			return new String[0];
		} else {
			return comma.split(gt);
		}
	}

	/** Save a key and value */
	public static void put(String key, String value) {
		// Allow the user to put an explict null in
		if (value != null && value.equals("null")) {
			single.remove(key.toLowerCase());
			return;
		}

		if (value == null) {
			value = "1";
		}

		if (value.length() > 1
				&& (value.charAt(0) == '\'' || value.charAt(0) == '"')) {
			char last = value.charAt(value.length() - 1);
			if (value.charAt(0) == last) {
				value = value.substring(1, value.length() - 1);
			}
		}

		single.put(key.toLowerCase(), value);
	}

	/** Save the current state of the settings for a later restoration */
	public static void save() {
		backup = new java.util.HashMap<String, String>();
		backup.putAll(single);
	}

	/**
	 * Add a setting to a list -- but only if it is not already in the list.
	 */
	public static void add(String key, String value) {

		// If we try to add a null it's OK if it's the only
		// value, but we can't add it to a list sensibly.
		if (value == null) {
			if (!Settings.has(key)) {
				Settings.put(key, value);
			}
			return;
		}

		String[] oldVals = Settings.getArray(key);
		String newValue = "";
		String comma = "";
		for (int i = 0; i < oldVals.length; i += 1) {
			if (oldVals[i].equals(value)) {
				return;
			}
			newValue += comma + oldVals[i];
			comma = ",";
		}
		newValue += comma + value;
		Settings.put(key, newValue);
	}

	/** Check if the given key has been set */
	public static boolean has(String key) {
		return single.containsKey(key.toLowerCase());
	}

	/** Return the array of keys in the current settings */
	public static String[] getKeys() {
		return single.keySet().toArray(new String[0]);
	}

	/** Restore a previously saved state. */
	public static void restore() {
		if (backup != null) {
			single = backup;
		} else {
			System.err
					.println("Attempt to restore Settings ignored: No previous state saved.");
		}
		backup = null;
	}

	/** Delete the given key */
	public static boolean remove(String key) {
		single.remove(key);
		return Settings.has(key);
	}
}
