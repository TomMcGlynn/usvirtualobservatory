package org.usvo.openid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.util.Pair;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/** 
 * A container class for configuration data.  
 *
 */
public class Conf {
    private static final Log log = LogFactory.getLog(Conf.class);

    private static Conf instance = null;

    /**
     * return the application's global Conf instance.  
     */
    public static Conf get() {
        if (instance == null)
            throw new IllegalStateException("Web app config not loaded yet (call init(ServletContext)).");
        else return instance;
    }

    public static final String CONFIG_FILE_NAME = "openid.properties";

    public static final String KEY_APP_URL = "baseurl";
    public static final String KEY_AUTHN_CMD = "authn.cmd"; // command to use to authenticate username/pwd
    /** Command used to generate a credential from a cookie. */
    public static final String KEY_CREDENTIAL_CMD = "credential.cmd";
    /** Command used to generate an end-entity credential from a cookie. */
    public static final String KEY_END_ENTITY_CREDENTIAL_CMD = "end_entity_credential.cmd";
    /** Time before we delete credential files, in minutes. */
    public static final String KEY_CREDENTIAL_DELETE_MINUTES = "credential.delete.minutes";
    /** Directory where we store credentials, and the URL that references that directory. */
    public static final String KEY_CREDENTIAL_DIR = "credential.dir", KEY_CREDENTIAL_URL = "credential.url";

    public static final String KEY_CREDENTIAL_PATIENCE = "credential.generation.patience.seconds",
        KEY_LOGIN_PATIENCE = "login.patience.seconds";

    public static final String KEY_DB_ASSOCIATIONS = "db.associations",
            KEY_DB_URL = "db.url", KEY_DB_USERNAME = "db.username", KEY_DB_PW = "db.pw",
            KEY_SESSION_DURATION_DAYS = "session.duration.days",
            KEY_SESSION_DURATION_HOURS = "session.duration.hours",
            KEY_SESSION_DURATION_MINUTES = "session.duration.minutes";

    /** The name of the table to store OpenID associations, so that they can be shared
     *  across multiple sessions & servers. */
    public static final String TABLE_OPENID_ASSOCIATION = "openid_association";

    private File cfgf = null;
    private Properties data = null;
    private String wipath = null;
    private long lastRead = 0L;

    /**
     * create a Configuration instance with properties loaded from the 
     * given file.  This constructor will assume that the WEB-INF directory
     * is the directory that contains the config file.  
     * @param configfile  the configuration properties file to load
     */
    public Conf(File configfile) throws IOException { this(configfile, null); }

    /**
     * create a Configuration instance with properties loaded from the 
     * given file.
     * @param configfile  the configuration properties file to load
     * @param webinfpath  the real file path to assume for the WEB-INF directory
     */
    public Conf(File configfile, String webinfpath) throws IOException {
        cfgf = configfile;
        wipath = webinfpath;
        if (wipath == null) wipath = cfgf.getParent();
        reload();
    }

    public String getProperty(String name) {
        ensureLatest();
        return data.getProperty(name);
    }

    private void ensureLatest() {
        if (cfgf.lastModified() > lastRead) {
            log.info("Detected Config file changed; reloading");
            try {
                reload();
            } catch (IOException ex) {
                throw new IllegalStateException("Problem reading config file: "
                                                + ex.getMessage());
            }            
        }
    }

    /**
     * initialize the app's configuration using the properties in the 
     * given file.  
     */
    public static void init(File configfile) throws IOException {
        instance = new Conf(configfile);
    }

    /**
     * initialize the app's configuration using the properties in the 
     * given file.  
     */
    public static void init(File configfile, String webinfpath) 
        throws IOException 
    {
        instance = new Conf(configfile, webinfpath);
    }

    /**
     * initialize the app's configuration for the given servlet context.
     * This will look for a configuration attached to the context; if one 
     * does not exist, one will be loaded from the default file in the 
     * app's WEB-INF directory.
     */
    public static void init(ServletContext context) throws IOException {
        instance = (Conf) context.getAttribute("org.usvo.openid.conf");
        if (instance == null) {
            String webinf = context.getRealPath("WEB-INF");
            if (webinf == null) 
                throw new IllegalStateException("Can't find WEB-INF directory");
            File config = new File(webinf, CONFIG_FILE_NAME);
            if (! config.exists())
                throw new IllegalStateException("Config file not found: " +
                                                config);
            log.info("initializing web app with configuration from WEB-INF");
            instance = new Conf(config, webinf);
            context.setAttribute("org.usvo.openid.conf", instance);
        }
    }

    /**
     * return the real path to the OpenID web application's WEB-INF directory
     */
    public String getWebInfPath() { return wipath; }

    public void reload() throws IOException {
        data = new Properties();
        FileInputStream strm = new FileInputStream(cfgf);
        lastRead = System.currentTimeMillis();
        data.load(strm);
        strm.close();
        strm = null;
    }

    /** The base URL of this OpenID server.
     *  The URL to authenticate to -- where the OpenID service is running. */
    public String getBaseUrl() {
        // return ensureHttp(read(context).get(KEY_APP_URL) + "/provider");
        return getProperty(KEY_APP_URL);
    }

    /** Ensure that a URL begins with https://. */
    public static String ensureHttps(String url) {
        if (url.startsWith("http://")) url = url.replaceFirst("http", "https");
        return url;
    }

    /** Ensure that a URL begins with http://. */
    public static String ensureHttp(String url) {
        if (url.startsWith("https://")) url = url.replaceFirst("https", "http");
        return url;
    }

    public static String ensureScheme(String scheme, String url) {
        if ("https".equalsIgnoreCase(scheme))
            return Conf.ensureHttps(url);
        else if ("http".equalsIgnoreCase(scheme))
            return Conf.ensureHttp(url);
        else throw new IllegalStateException("Unknown scheme: " + scheme);
    }

    /** The base of a user's OpenID.  Add username to it. */
    public String getIdBase() {
        return getProperty(KEY_APP_URL) + "/id/";
    }

    /** A user's OpenID. */
    public String getId(String username) {
        return getIdBase() + username;
    }

    /** URL to send login requests to. */
    public String getSigninUrl() {
        String result = getProperty(KEY_APP_URL) + "/signin";
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (!Devel.DEVEL) // ensure that login is via https
            result = ensureHttps(result);
        else
            log.warn("Warning: allowing non-encrypted login to " + result);
        return result;
    }

    public String getIndexUrl() {
        return ensureHttp(getProperty(KEY_APP_URL));
    }

    /** The command to run to test a login. */
    public String getAuthnCmd() {
        return getProperty(KEY_AUTHN_CMD);
    }

    /** The command to run to test a login. */
    public String getCredentialCommand() {
        return getProperty(KEY_CREDENTIAL_CMD);
    }

    /** The command to run to generate an EEC. */
    public String getEndEntityCredentialCommand() {
        return getProperty(KEY_END_ENTITY_CREDENTIAL_CMD);
    }

    /** The parent directory that we fetch credentials from. */
    public String getCredentialDir() {
        return getProperty(KEY_CREDENTIAL_DIR);
    }

    public String getCredentialUrl() {
        return getProperty(KEY_CREDENTIAL_URL);
    }

    public int getCredentialDeleteMinutes() {
        return getInt(KEY_CREDENTIAL_DELETE_MINUTES);
    }

    /** http or https. */
    public static String getScheme(String url) {
        int colon = url.indexOf(':');
        if (colon < 0) throw new IllegalArgumentException("No colon found in \"" + url + "\".");
        return url.substring(0, colon).toLowerCase();
    }

    /** Looks up session.duration.... to see what the setting is and converts it to seconds.
     *  Shortest one wins.  If runs into trouble defaults to 5 minutes. */
    public int getSessionDurationSeconds() {
        boolean found = false;
        int fromDays = 1000000, fromHours = 50000000, fromMintues = 1000000000;

        String daysString    = getProperty(KEY_SESSION_DURATION_DAYS),
               hoursString   = data.getProperty(KEY_SESSION_DURATION_HOURS), 
               minutesString = data.getProperty(KEY_SESSION_DURATION_MINUTES);
        try {
            if (daysString != null)
                fromDays = Integer.parseInt(daysString) * 24 * 3600;
            if (hoursString != null)
                fromHours = Integer.parseInt(hoursString) * 3600;
            if (minutesString != null)
                fromMintues = Integer.parseInt(minutesString) * 60;
            found = daysString != null || 
                    hoursString != null || 
                    minutesString != null;
        } catch(NumberFormatException e) {
            log.warn("Number format exception parsing default session period", e);
        }
        if (found)
            return Math.min(fromDays, Math.min(fromHours, fromMintues));
        else
            return 300; // default to five minutes
    }

    /** How many seconds should we wait for credential creation before timing out?
     *  Configured in openid.properties. */
    public int getCredentialPatience() { return getInt(KEY_CREDENTIAL_PATIENCE); }

    /** How many seconds should we wait for a local login before timing out?
     *  Configured in openid.properties. */
    public int getLoginPatience() { return getInt(KEY_LOGIN_PATIENCE); }

    private int getInt(String key) {
        try { return Integer.parseInt(getProperty(key)); }
        catch(NumberFormatException e) {
            log.warn("Unable to parse " + key + ": \"" + key + "\".");
            throw new IllegalStateException(e);
        }
    }
}
