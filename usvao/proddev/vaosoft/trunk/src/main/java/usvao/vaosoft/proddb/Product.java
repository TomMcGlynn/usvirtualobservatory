package usvao.vaosoft.proddb;

import java.io.File;
import java.util.Properties;

/**
 * a class representing a product and its attributes
 */
public abstract class Product {

    /**
     * the string representing a default value
     */
    public final static String DEFAULT = "_";

    /**
     * the string representing the generic, cross-platform platform
     */
    public final static String GENERIC = DEFAULT;

    /**
     * the name of this product
     */
    public final String name;

    /**
     * the string representing the version of this product instance
     */
    public final String version;

    /**
     * the string identifying the organization responsible for releasing 
     * this product 
     */
    public final String org;

    protected Properties extProps = null;

    /**
     * initialize the Product description.  For efficiency, the reference
     * to the given props is stored which could get updated later (by the 
     * this instance or by the caller); to guarantee that this instance
     * has its own independent set, pass in a cloned copy.  
     */
    public Product(String name, String version, String org, Properties props) {
        this.name = name;
        this.version = version;
        this.org = org;
        extProps = props;
    }

    /**
     * initialize the Product description.  For efficiency, the reference
     * to the given props is stored which could get updated later (by the 
     * this instance or by the caller); to guarantee that this instance
     * has its own independent set, pass in a cloned copy.  
     */
    public Product(String name, String version, String org) {
        this(name, version, org, null);
    }

    /**
     * return the string representing the generic, cross-platform platform
     */
    public String getName() { return name; } 

    /**
     * return the name of this product
     */
    public String getVersion() { return version; } 

    /**
     * return the string identifying the organization responsible for 
     * releasing this product 
     */
    public String getOrg() { return org; } 

    /**
     * return the home directory where this product is installed.  
     */
    public abstract File getHome();

    /**
     * return the name that identifies the platform that this product 
     * instance supports.  The possible values are uncontrolled except
     * that "_" indicates that the product is cross-platform.  
     */
    public abstract String getPlatform();

    /**
     * return true if the product is indeed installed as expected.  Normally
     * this means that the "proof file" exists.  
     */
    public abstract boolean isInstalled();

    /**
     * return the directory containing executables for this product.
     * This implementation returns the default, [home]/bin.
     */
    public File getBinDir() { return new File(getHome(), "bin"); }

    /**
     * return the directory containing executables for this product.
     * This implementation returns the default, [home]/bin.
     */
    public File getLibDir() { return new File(getHome(), "lib"); }

    /**
     * return an extended property
     */
    public String getExtProperty(String prop) {
        if (extProps == null) return null;
        return extProps.getProperty(prop);
    }

    // list tags?
}
