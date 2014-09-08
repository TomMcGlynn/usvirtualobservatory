package usvao.vaosoft.proddb;

import java.util.List;
import java.util.Iterator;

/**
 * a class representing a database of products that have been installed
 */
public abstract class ProductDB {

    protected StackConfig config = null;
    protected List<String> prefTags = null;

    /**
     * set up this DB
     */
    public ProductDB(StackConfig config, List<String> preferredTags) {
        this.config = config;
        prefTags = preferredTags;
    }

    /**
     * return the number of products in this database
     */
    public abstract int getCount();

    /**
     * return a list of products that match the given name and version 
     * @param name               the name of the product; if null, all
     *                              products matching the versionConstraint
     *                              will be returned.  
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, all versions matching the 
     *                              name and platform will be returned.  
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the versions appropriate for this platform.
     * @return ProductDB  a set of matching products.  
     */
    public abstract ProductDB matchProducts(String name, 
                                            String versionConstraint,
                                            String platform);
    public ProductDB matchProducts(String name, String versionConstraint) {
        return matchProducts(name, versionConstraint, null);
    }

    /**
     * return a list of all the products that match the given name
     * @param name               the name of the product 
     * @return ProductLine  a set of matching versions of products, or null,
     *                         if no versions match.
     */
    public abstract ProductLine getVersions(String name);

    /**
     * return the product matching the exact constraints
     * @param name       the name of the product 
     * @param version    the exact version of a product 
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return either the version for the current platform 
     *                    or the generic (cross-platform) version.  
     * @return Product   the matching product of null if not found in this 
     *                      DB
     */
    public abstract Product getProduct(String name, String version, 
                                       String platform);
    public Product getProduct(String name, String version) {
        return getProduct(name, version, null);
    }
    public abstract Product getProductByTag(String name, String tag, 
                                            String platform);
    public Product getProductByTag(String name, String tag) {
        return getProductByTag(name, tag, null);
    }

    /**
     * return a version of a product that matches the given constraints 
     * and with the most preferred tag. 
     * @param name       the name of the product 
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, the preferred version will 
     *                              be picked from all available versions.
     * @param tags   a list of preferred tags used to select from the 
     *                  versions selected by versionConstraint.  
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the generic (cross-platform) version.  
     */
    public abstract Product selectProduct(String name, 
                                          String versionConstraint,
                                          List<String> tags,
                                          String platform);
    public Product selectProduct(String name, 
                                 String versionConstraint,
                                 String platform) 
    {
        return selectProduct(name, versionConstraint, prefTags, platform);
    }
    public Product selectProduct(String name, 
                                 String versionConstraint,
                                 List<String> tags) 
    {
        return selectProduct(name, versionConstraint, tags, null);
    }
    public Product selectProduct(String name, 
                                 String versionConstraint) 
    {
        return selectProduct(name, versionConstraint, prefTags);
    }

    /**
     * return an iterator for iterating through the raw product data
     */
    public abstract Iterator<String[]> rawIterator();
}
