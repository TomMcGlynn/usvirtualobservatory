package org.usvao.sso.replicmon;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/** Loads the file <tt>replicmon.properties-sample</tt>, which is copied into this package. */
public class ReplicmonConfig extends Properties {
    public static final String KEY_NAME = "name", KEY_URL = "url",
            KEY_USERNAME = "username", KEY_PASSWORD = "password", KEY_MASTER = "master";

//    public ReplicmonConfig() throws IOException {
//        load(getClass().getResourceAsStream("replicmon.properties"));
//    }

    public ReplicmonConfig(ServletContext context) throws IOException {
            String path = context.getRealPath("WEB-INF");
            File webInf = new File(path);
            File[] propertiesFiles = webInf.listFiles(new FilenameFilter() {
                @Override public boolean accept(File dir, String name) { return name.endsWith(".properties"); }
            });
            for (File prop : propertiesFiles) {
                FileInputStream in = new FileInputStream(prop);
                load(in);
                in.close();
            }
    }

    /** A map of prefix to DbServerConfig. */
    public Map<Integer, DbServerConfig> extractDbServerConfigs() {
        Map<Integer, DbServerConfig> result = new TreeMap<Integer, DbServerConfig>();
        for (Object key : keySet()) {
            if (key != null) {
                String[] kk = ((String) key).split("\\.");
                // scan for keys that start with a number, like "1.url" and "2.password"
                try {
                    Integer prefix = Integer.parseInt(kk[0]);
                    if (!result.containsKey(prefix)) // only extract the first time we see each prefix
                        result.put(prefix, new DbServerConfig(kk[0], this));
                } catch(NumberFormatException ignored) {} // don't care about the rest of the keys
            }
        }
        return result;
    }

    public String getTestTable() { return getProperty("test.table", true); }
    public String getFilterColumn() { return getProperty("filter.column", true); }
    // TODO: update date column
    public String getDateColumn() { return getProperty("date.column", true); }
    public String getTestColumn() { return getProperty("test.column", true); }

    public int getPatienceMillis() {
        String s = getProperty("replication.patience.millis", true);
        try {
            return Integer.parseInt(s);
        } catch(NumberFormatException e) {
            throw new ConfigurationException("replication.patience.millis", "Could not parse as int.");
        }
    }

    public String getProperty(String propertyName, boolean complainIfMissing) {
        String result = getProperty(propertyName);
        if (complainIfMissing && result == null)
            throw new ConfigurationException(propertyName, "Missing.");
        return result;
    }
}
