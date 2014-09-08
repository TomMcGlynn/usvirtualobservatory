package usvao.vaosoft.proddb;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * an abstract class that encapsulates the conventions for how a software 
 * product stack is organized.  This class also provides the factory 
 * functions, createStackConfig(), for creating a specific instance of the 
 * class.
 */
public abstract class StackConfig {

    public final static String DEFAULT_STACK_TYPE_NAME = "vaosoft-1.0";
    public final static String STACK_CONFIG_PROPERTIES = 
        "stackConfig.properties"; 

    File root = null;

    static Hashtable<String, Class> classLookup = 
        new Hashtable<String, Class>(2);
    static {
        classLookup.put("vaosoft-1.0", VAOSoft1StackConfig.class);
    }

    /**
     * store the common StackConfig information.  This implementation will
     * ensure that the given File representing the stack home directory
     * is indeed a directory.
     * @param stackHome    the home directory for the stack (VAO_HOME)
     */
    public StackConfig(File stackHome) { 
        if (stackHome == null) 
            throw new IllegalArgumentException("StackConfig: stackHome must be non-null");
        if (stackHome.exists() && ! stackHome.isDirectory()) 
            throw new IllegalArgumentException("Stack home File must be a directory");
        root = stackHome;
    }

    /**
     * return the root of the stack, the so-called VAO_HOME.
     */
    public File getHome() { return root; }

    /**
     * return the directory underwhich all products are installed.
     * @param platform   the platform name; if "_", provide the location 
     *                     for the cross-platform product installations.
     *                     Null means to pick the current platform.  
     *                     Implementations may ignore this value.  
     */
    public abstract File getProductRoot(String platform);

    /**
     * return the home directory for a given product assuming it has been
     * installed in the standard place in the stack.  
     */
    public abstract File getDefProductHome(Product prod);

    /**
     * return the version constraint interpreter
     */
    public abstract VersionHandler getVersionHandler();

    /**
     * return the class for connecting to the product database data
     */
    public abstract ProductDataIO getDataStore();

    /**
     * create an appropriate instance of a StackConfig for a stack at the 
     * given location.  The props must contain at least one of the following 
     * properties
     * <pre>
     * stackTypeName       a name that implies a predefined mapping to an
     *                        instantiable StackConfig class.
     * stackTypeClass      a fully specified name for a StackConfig class.  
     * </pre>
     * If both are specified, stackTypeClass will take precedence.
     *
     * @param home   the home directory for the stack
     * @param props  properties that identify the type of stack and all
     *                 its conventions.  
     */
    public static StackConfig createStackConfig(File home, Properties props) 
        throws ConfigurationException
    {
        try {
            if (props == null)
                throw new IllegalArgumentException("createStackConfig(): props cannot be null");
            String clname = props.getProperty("stackTypeClass");
            Class configcl = null;
            if (clname != null) {
                configcl = Class.forName(clname);
            }
            else {
                clname = props.getProperty("stackTypeName");
                if (clname == null) clname = DEFAULT_STACK_TYPE_NAME; 
                configcl = StackConfig.classLookup.get(clname);
                if (configcl == null)
                    throw new ConfigurationException("No StackConfig class for name: " + clname);
                if (! StackConfig.class.isAssignableFrom(configcl)) 
                    throw new ConfigurationException("Requested class is not a subclass of StackClass: " + clname);
            }

            Constructor[] ctrs = configcl.getConstructors();
            for(int i=0; i < ctrs.length; i++) {
                Class[] params = ctrs[i].getParameterTypes();
                if (params.length == 2 && 
                    params[0] == File.class && params[1] == Properties.class)
                  return (StackConfig) ctrs[i].newInstance(home, props);
            }
            throw new ConfigurationException("Configured class is missing required constructor");
        }
        catch (ClassNotFoundException ex) {
            throw new ConfigurationException(ex);
        }
        catch (InstantiationException ex) {
            throw new ConfigurationException(ex);
        }
        catch (IllegalAccessException ex) {
            throw new ConfigurationException(ex);
        }
        catch (InvocationTargetException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * create an appropriate instance of a StackConfig for a stack at the 
     * given location.  The type of stack instantiated will be determined
     * by loading properties from a file called 
     * <code>stackConfig.properties</code> in the given home directory.  
     * These properties are passed to the 
     * {@link #createStackConfig(File, Properties)} factory method to 
     * create the instance.  If no such file is found, the default 
     * stack type (named DEFAULT_STACK_TYPE_NAME) will be assumed.  
     * @param home   the home directory for the stack
     */
    public static StackConfig createStackConfig(File home) 
        throws ConfigurationException
    {
        Properties props = null;
        if (home.exists()) {
            if (! home.isDirectory())
                throw new ConfigurationException(home.toString() + 
                                                 ": VAO_HOME not a directory");
            File propfile = new File(home, STACK_CONFIG_PROPERTIES);
            if (propfile.exists()) {
                props = new Properties();
                try {
                    props.load(new FileInputStream(propfile));
                }
                catch (IOException ex) {
                    throw new ConfigurationException(ex);
                }
            }
        }

        if (props == null) {
            props = new Properties();
        }

        return createStackConfig(home, props);
    }

    /**
     * return a property from a Properties set with all references resolved.
     * This method will replace the value in the Properties 
     */
    public static String resolveProperty(Properties props, String name) {
        return StackConfig.resolveProperty(props, name, new HashSet<String>());
    }


    static Pattern propRefRE = Pattern.compile("\\$\\{([^\\}]+)\\}");
    static String resolveProperty(Properties props, String name, 
                                  Set<String> seen) 
    {
        String lit = props.getProperty(name);
        if (lit == null) return null;

        StringBuilder val = new StringBuilder(lit);
        int from = 0;
        Matcher m = propRefRE.matcher(val);
        if (! m.find() || seen.contains(name)) return lit;
        seen.add(name);

        while (m.find(from)) {
            String refname = m.group(1);

            lit = StackConfig.resolveProperty(props, refname, seen);
            
            if (lit == null) {
                from = m.end();
            }
            else {
                val.replace(m.start(), m.end(), lit);
                from = m.regionStart() + lit.length();
                m = propRefRE.matcher(val);
            }
        }

        props.setProperty(name, val.toString());
        return val.toString();
    }

    public static String detectPlatform() {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        String dist = "unknown";
        String ver = "unknown";

        if (os.startsWith("Mac OS")) {
            dist = os;
            ver = System.getProperty("os.version");
        }
        else if (os.startsWith("Windows ")) {
            String[] w = os.split("\\s+");
            os = w[0];
            dist = w[1];
            ver = System.getProperty("os.version");
        }
        else if (os.equals("Linux")) {
            File rhrel = new File("/etc/redhat-release");
            File suserel = new File("/etc/suse-release");
            File issue = new File("/etc/issue");
            String fam = null;
            if (rhrel.exists()) {
                // this is a RedHat derivative
                fam = "RedHat/";
                String line = read1stline(rhrel);
                if (line != null) {
                    String[] distver = matchRH(line);
                    if (distver[0] != null) {
                        dist = distver[0];
                        ver = distver[1];
                    }
                }
                
            }
            else if (suserel.exists()) {
                fam = "SUSE/";
                String[] distver = matchFromSuseFile(suserel);
                if (distver[0] != null) {
                    dist = distver[0];
                    ver = distver[1];
                }
            }

            if (dist.equals("unknown") && issue.exists()) {
                String line = read1stline(issue);
                if (line != null) {
                    String[] distver = matchUbuntu(line);
                    if (distver[0] == null) distver = matchRH(line);
                    if (distver[0] == null) distver = matchSuse(line);
                    if (distver[0] != null) {
                        dist = distver[0];
                        ver = distver[1];
                    }
                }
            }
        }

        return os+":"+arch+":"+dist+":"+ver;
    }

    private static String read1stline(File file) {
        String out = null;
        try {
            BufferedReader rdr = 
                new BufferedReader(new FileReader(file));
            out = rdr.readLine();
            rdr.close();
        }
        catch (IOException ex) {  }

        return out;
    }

    static Pattern rhrelRE = Pattern.compile("^\\s*(\\S.*\\S) release +(\\S+)");
    private static String[] matchRH(String line) {
        String[] out = { null, null };
        String fam = "RedHat/";

        Matcher m = rhrelRE.matcher(line);
        if (m.lookingAt()) {
            out[0] = fam + m.group(1);    // distribution
            out[1] = m.group(2);          // version
        }

        return out;
    }

    static Pattern ubrelRE = Pattern.compile("^\\s*Ubuntu +(\\S+)");
    private static String[] matchUbuntu(String line) {
        String[] out = { null, null };

        Matcher m = ubrelRE.matcher(line);
        if (m.lookingAt()) {
            out[0] = "Debian/Ubuntu";     // distribution
            out[1] = m.group(1);          // version
        }

        return out;
    }

    static Pattern suserelRE = Pattern.compile("^SUSE\\s+(\\S.*\\S) (\\d\\S*) \\(");
    private static String[] matchSuse(String line) {
        String[] out = { null, null };
        String fam = "SUSE/";

        Matcher m = suserelRE.matcher(line);
        if (m.lookingAt()) {
            out[0] = fam + m.group(1);    // distribution
            out[1] = m.group(2);          // version
        }

        return out;
    }

    private static String[] matchFromSuseFile(File file) {
        String[] out = null;
        try {
            BufferedReader rdr = 
                new BufferedReader(new FileReader(file));
            String line = rdr.readLine();

            out = matchSuse(line);

            String verline = "VERSION = ";
            while((line = rdr.readLine()) != null) {
                if (line.startsWith(verline)) {
                    out[1] = line.substring(verline.length()).trim();
                    break;
                }
            }
            rdr.close();
        }
        catch (IOException ex) {  }

        return out;
    }



    public static void main(String[] args) {
        Properties p = new Properties();
        p.setProperty("home", "/appl/vaosw");
        p.setProperty("phome", "${home}/products");

        p = new Properties(p);
        System.out.println(StackConfig.resolveProperty(p, "phome"));

        System.out.println("Platform: " + StackConfig.detectPlatform());
    }
}

