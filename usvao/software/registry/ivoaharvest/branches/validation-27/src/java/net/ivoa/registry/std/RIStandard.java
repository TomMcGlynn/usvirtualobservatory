package net.ivoa.registry.std;

import java.util.Properties;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Locale;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * a static utility class for loading properties of the RI standard into 
 * Properties object
 */
public class RIStandard implements RIProperties {

    public final static String defaultVersion = "1.0";

    public final static String PROPERTIES_BASE_NAME = 
        "registryInterfaceDefinitions";

    /**
     * load the properties associated with the requested version of the 
     * standard.  An example Version string is "1.0";
     */
    public static void loadProperties(Properties props, String version) 
        throws IOException
    {
//         ResourceBundle res = 
//             ResourceBundle.getBundle(PROPERTIES_BASE_NAME, 
//                                      new Locale(version));

        String resname = PROPERTIES_BASE_NAME + "_" + version + ".properties";
        InputStream ris = (RIStandard.class).getResourceAsStream(resname);
        if (ris == null) 
            throw new FileNotFoundException("properties for version " + version
                                            + " not found");
        ResourceBundle res = new PropertyResourceBundle(ris);

        String key = null;
        for(Enumeration e = res.getKeys(); e.hasMoreElements();) {
            key = (String) e.nextElement();
            props.setProperty(key, res.getString(key));
        }
    }

    /**
     * return a set of standard definition for a given version of the Registry
     * Interface standard.
     */
    public static Properties getDefinitionsFor(String version) {
        Properties out = new Properties();
        try {
            loadProperties(out, version);
        } catch (IOException ex) {
            // shouldn't happen
            throw new InternalError("config error: no definitions found for " +
                                    "default RI version");
        }
        return out;
    }

    /**
     * return the set of standard definition for a given version of the Registry
     * Interface standard.
     */
    public static Properties getDefaultDefinitions() {
        return getDefinitionsFor(defaultVersion);
    }

    public static void main(String[] args) {
        String version = "1.0";
        if (args.length > 0) version = args[0];

        Properties p = new Properties();
        try {
            RIStandard.loadProperties(p, version);

            p.store(System.out, "Properties for RI version " + version);
        }
        catch (IOException ex) {
            throw new RuntimeException("Trouble printing with Properties." + 
                                       "store(): " + ex.getMessage());
        }
    }
}
