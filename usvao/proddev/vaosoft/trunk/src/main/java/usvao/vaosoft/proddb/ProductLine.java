package usvao.vaosoft.proddb;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * a class that represents a list of different versions of a product
 */
public abstract class ProductLine extends ProductDB {

    protected String name = null;
    protected String org = null;

    /**
     * initialize the ProductLine
     */
    public ProductLine(String name, String org,
                       StackConfig config, List<String> preferredTags) 
    {
        super(config, preferredTags);
        this.name = name;
        this.org = org;
    }

    /**
     * return the name of the product
     */
    public String getProductName() {  return name;  }

    /**
     * return the string identifying the organization responsible for 
     * releasing this product 
     */
    public String getOrg() { return org; } 

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
    public ProductDB matchProducts(String name, String versionConstraint,
                                   String platform) 
    {
        if (! this.name.equals(name)) return EmptyProductDB.INSTANCE;
        return matchVersions(versionConstraint, platform);
    }

    /**
     * return a list of all the products that match the given name
     * @param name               the name of the product 
     * @return ProductLine  a set of matching versions of products, or null,
     *                         if no versions match.
     */
    public ProductLine getVersions(String name) 
    {
        if (! name.equals(this.name)) return null;
        return this;
    }

    /**
     * return the set of the names of the versions in this 
     * ProductLine.
     */
    public abstract Set<String> versionSet();

    /**
     * return a list of products that match the given name and version 
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
    public abstract ProductLine matchVersions(String versionConstraint,
                                              String platform);

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
    public Product getProduct(String name, String version, String platform) {
        if (! name.equals(this.name)) return null;
        return getVersion(version, platform);
    }
                              
    /**
     * return the product matching the exact constraints
     * @param version    the exact version of a product 
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return either the version for the current platform 
     *                    or the generic (cross-platform) version.  
     * @return Product   the matching product of null if not found in this 
     *                      DB
     */
    public abstract Product getVersion(String version, String platform);
                              
    /**
     * return the product matching the exact constraints
     * @param name       the name of the product 
     * @param tag        the product that is has this tag assigned to it.
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return either the version for the current platform 
     *                    or the generic (cross-platform) version.  
     * @return Product   the matching product of null if not found in this 
     *                      DB
     */
    public Product getProductByTag(String name, String tag, String platform) {
        if (! name.equals(this.name)) return null;
        return getVersionByTag(tag, platform);
    }

    /**
     * return the product matching the exact constraints
     * @param tag        the product that is has this tag assigned to it.
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return either the version for the current platform 
     *                    or the generic (cross-platform) version.  
     * @return Product   the matching product of null if not found in this 
     *                      DB
     */
    public abstract Product getVersionByTag(String tag, String platform);
                                            
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
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the generic (cross-platform) version.  
     */
    public Product selectProduct(String name, String versionConstraint,
                                 List<String> tags, String platform) 
    {
        if (! name.equals(this.name)) return null;
        return selectVersion(versionConstraint, tags, platform);
    }
                                 
    /**
     * return a version of a product that matches the given constraints 
     * and with the most preferred tag.  This will select out versions
     * using versionConstraint.  Then the list of tags will be used to
     * select a single Product:  a Product tagged with each tag in the list 
     * in order will be sought; the first Product so found will be returned.
     * If no matching versions have been tagged with any of the tag names,
     * the latest will be returned. 
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, the preferred version will 
     *                              be picked from all available versions.
     * @param tags   a list of preferred tags used to select from the 
     *                  versions selected by versionConstraint.  If null,
     *                  the default list of preferredTags (set at construction
     *                  time; usually that for the whole DB) will be used.  
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the generic (cross-platform) version.  
     */
    public abstract Product selectVersion(String versionConstraint, 
                                          List<String> tags, String platform);
                                 
    /**
     * return a version of a product that matches the given constraints 
     * and with the most preferred tag. 
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, the preferred version will 
     *                              be picked from all available versions.
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the generic (cross-platform) version.  
     */
    public Product selectVersion(String versionConstraint, String platform) {
        return selectVersion(versionConstraint, null, platform);
    }

    /**
     * return the latest version in this product line appropriate for the 
     * given platform.
     */
    public abstract Product getLatest(String platform);

    /**
     * return true if this line includes a given version
     */
    public abstract boolean hasVersion(String version);
}


