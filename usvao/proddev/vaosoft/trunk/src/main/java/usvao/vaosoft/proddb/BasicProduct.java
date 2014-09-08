package usvao.vaosoft.proddb;

import static usvao.vaosoft.proddb.LoadProduct.*; 

import java.io.File;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * a class representing a product and its attributes
 */
public class BasicProduct extends Product {

    protected String platform = null;
    protected File home = null;
    protected StackConfig stack = null;
    protected File proofFile = null;

    public BasicProduct(String name, String version, String org, 
                        StackConfig stack, Properties props) 
    {
        this(name, version, org, null, null, stack, props);
    }

    public BasicProduct(String name, String version, String org, String home,
                        String proof, StackConfig stack, Properties props) 
    {
        super(name, version, org, props);
        this.stack = stack;

        if (props != null) {
            platform = props.getProperty("platform");
            if (home == null) home = props.getProperty("home");
            if (proof == null) home = props.getProperty("home");
        }

        if (platform == null) platform = StackConfig.detectPlatform();
        if (home != null) {
            this.home = new File(home);
            if (! this.home.isAbsolute() && stack != null) 
                this.home = new File(stack.getProductRoot(platform), home);
        }
        if (proof != null) {
            proofFile = new File(proof);
            if (! proofFile.isAbsolute() && stack != null) 
                proofFile = new File(getHome(), proofFile.toString());
        }
    }

    public BasicProduct(String name, String version, String org, 
                        Properties props) 
    {
        this(name, version, org, null, null, null, props);
    }

    /**
     * create the Product from the raw data array.  The identity of what
     * should be in each element of the input array is defined by the 
     * index constants provided via the LoadProduct interface.  
     */
    public static BasicProduct makeProduct(String[] data, StackConfig cfg) {
        Properties props = parseProperties(data, PROPS);
        return new BasicProduct(data[NAME], data[VERSION], data[ORG], 
                                data[HOME], data[PROOF], cfg, props);
    }

    /**
     * parse the String-encoded properties and load them into a 
     * Properties file.
     */
    public static Properties parseProperties(String[] data, int from) {
        Properties props = null;
        while (from < data.length && data[from] == null) from++;
        if (from >= data.length) return null;
        props = new Properties();
        for(; from < data.length; from++) {
            if (data[from] == null) continue;
            String pair = null;
            int p = -1;
            StringTokenizer st = new StringTokenizer(data[from], "\t");
            while (st.hasMoreTokens()) {
                pair = st.nextToken();
                p = pair.indexOf("=");
                if (p >= 0) 
                    props.setProperty(pair.substring(0,p), pair.substring(p+1));
            }
        }

        return props;
    }

    /**
     * return the name that identifies the platform that this product 
     * instance supports.  The possible values are uncontrolled except
     * that "_" indicates that the product is cross-platform.  
     */
    public String getPlatform() { return platform; }

    /**
     * return the home directory where this product is installed.  
     */
    public File getHome() {
        if (home != null) return home;
        if (stack == null) return null;
        return stack.getDefProductHome(this);
    }

    void setHome(File home) { 
        if (home.exists() && ! home.isDirectory()) 
            throw new IllegalArgumentException("product home File must be a directory");
        this.home = home; 
    }

    /**
     * return true if the product is indeed installed as expected.  Normally
     * this means that the "proof file" exists.  
     */
    public boolean isInstalled() {
        return (proofFile != null && proofFile.exists());
    }



}