package org.usvo.openid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.util.Pair;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/** Reads configuration from WEB-INF. */
public class Conf {
    private static final Log log = LogFactory.getLog(Conf.class);

    private static Conf instance;

    public static Conf get() {
        if (instance == null)
            throw new IllegalStateException("Not initialized yet -- call init(ServletContext) first.");
        else return instance;
    }

    static final String CONFIG_FILE_NAME = "openid.properties";

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

    private ServletContext context;

    private Conf(ServletContext context) {
        this.context = context;
    }

    public ServletContext getContext() { return context; }

    public static void init(ServletContext context) {
        if (instance != null)
            throw new IllegalStateException("Already initialized.");
        else
            instance = new Conf(context);
    }

    private static WeakHashMap<ServletContext, Pair<Long, Map<String, String>>> readCache
            = new WeakHashMap<ServletContext, Pair<Long, Map<String, String>>>();
    public Map<String, String> read() {
        Pair<Long, Map<String, String>> cache = readCache.get(context);
        try {
            if (cache == null || cache.getA() < System.currentTimeMillis() - 1000) {
                if (cache != null)
                    log.trace("reloading -- elapsed = " + (System.currentTimeMillis() - cache.getA()));
                Map<String, String> map = new HashMap<String, String>();
                cache = new Pair<Long, Map<String, String>>(System.currentTimeMillis(), map);

                String path = context.getRealPath("WEB-INF");
                Properties properties = new Properties();
                File webInf = new File(path);
                if (!webInf.exists()) throw new IllegalStateException(path + " doesn't exist.");
                File propFile = new File(webInf, CONFIG_FILE_NAME);
                if (!propFile.exists()) {
                    if (Devel.DEVEL) {
                        log.warn("config file \"" + propFile.getPath() + "\" doesn't exist; trying "
                                + Devel.getConfigFile().getPath() + " instead.");
                        propFile = Devel.getConfigFile();
                    }
                    else throw new IllegalStateException(propFile.getPath() + " doesn't exist.");
                }
                properties.load(new FileInputStream(propFile));
                for (Object key : properties.keySet())
                    map.put((String) key, properties.getProperty((String) key));

                readCache.put(context, cache);
            }
        } catch(IOException e) {
            // this is really a configuration exception -- doesn't need to be a checked exception because
            // once it's fixed, it should stay fixed.
            throw new IllegalStateException("Could not load configuration: " + e.getMessage(), e);
        }
        log.trace("not reloading -- elapsed = " + (System.currentTimeMillis() - cache.getA()));
        return new HashMap<String, String>(cache.getB());
    }

    /** The base URL of this OpenID server.
     *  The URL to authenticate to -- where the OpenID service is running. */
    public String getBaseUrl() {
        // return ensureHttp(read(context).get(KEY_APP_URL) + "/provider");
        return read().get(KEY_APP_URL);
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
        return read().get(KEY_APP_URL) + "/id/";
    }

    /** A user's OpenID. */
    public String getId(String username) {
        return getIdBase() + username;
    }

    /** URL to send login requests to. */
    public String getSigninUrl() {
        String result = read().get(KEY_APP_URL) + "/signin";
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (!Devel.DEVEL) // ensure that login is via https
            result = ensureHttps(result);
        else
            log.warn("Warning: allowing non-encrypted login to " + result);
        return result;
    }

    public String getIndexUrl() {
        return ensureHttp(read().get(KEY_APP_URL));
    }

    /** The command to run to test a login. */
    public String getAuthnCmd() {
        return read().get(KEY_AUTHN_CMD);
    }

    /** The command to run to test a login. */
    public String getCredentialCommand() {
        return read().get(KEY_CREDENTIAL_CMD);
    }

    /** The command to run to generate an EEC. */
    public String getEndEntityCredentialCommand() {
        return read().get(KEY_END_ENTITY_CREDENTIAL_CMD);
    }

    /** The parent directory that we fetch credentials from. */
    public String getCredentialDir() {
        return read().get(KEY_CREDENTIAL_DIR);
    }

    public String getCredentialUrl() {
        return read().get(KEY_CREDENTIAL_URL);
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
        Map<String, String> conf = read();
        String daysString    = conf.get(KEY_SESSION_DURATION_DAYS),
               hoursString   = conf.get(KEY_SESSION_DURATION_HOURS), 
               minutesString = conf.get(KEY_SESSION_DURATION_MINUTES);
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
        try { return Integer.parseInt(read().get(key)); }
        catch(NumberFormatException e) {
            log.warn("Unable to parse " + key + ": \"" + key + "\".");
            throw new IllegalStateException(e);
        }
    }
}
