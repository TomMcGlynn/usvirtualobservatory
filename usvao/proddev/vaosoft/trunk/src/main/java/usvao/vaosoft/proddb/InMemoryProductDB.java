package usvao.vaosoft.proddb;

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

/**
 * a class representing a database of products that have been installed
 */
public class InMemoryProductDB extends ProductDB implements LoadProduct {

    HashMap<String, ProductLine> products = null;
    int count = 0;

    InMemoryProductDB(StackConfig config, List<String> preferredTags, 
                      int initCapacity) 
    {
        super(config, preferredTags);
        if (initCapacity <= 0) initCapacity = 4;
        products = new HashMap<String, ProductLine>(initCapacity);
    }

    InMemoryProductDB(StackConfig config) {
        this(config, null, 0);
    }

    /**
     * return the number of products in this database
     */
    public int getCount() { return count; }

    void addProductLine(ProductLine pl) {
        products.put(pl.getProductName(), pl);
        count += pl.getCount();
    }

    public void loadProduct(String[] d) {
        try {
            LoadProduct pl = 
                (LoadProduct) products.get(d[LoadProduct.NAME]);
            if (pl == null) {
                BasicProductLine bpl = 
                    new BasicProductLine(d[LoadProduct.NAME], 
                                         d[LoadProduct.ORG],
                                         config.getVersionHandler(),
                                         config);
                addProductLine(bpl);
                pl = bpl;
            }
            pl.loadProduct(d);
            count++;
        }
        catch (ClassCastException ex) {
            // ignore this product; consider it read-only
        }
    }

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
        ProductLine pl = null;
        if (name != null) {
            pl = getVersions(name);
            if (pl == null) return EmptyProductDB.INSTANCE;
            return pl.matchVersions(versionConstraint, platform);
        }

        InMemoryProductDB out = new InMemoryProductDB(config, prefTags, 
                                                      products.size());
        Iterator<ProductLine> it = products.values().iterator();
        while (it.hasNext()) {
            pl = it.next().matchVersions(versionConstraint, platform);
            out.addProductLine(pl);
        }
        return out;
    }
    
    /**
     * return a list of all the products that match the given name
     * @param name               the name of the product 
     * @return ProductLine  a set of matching versions of products, or null,
     *                         if no versions match.
     */
    public ProductLine getVersions(String name) {
        return products.get(name);
    }

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
        ProductLine pl = getVersions(name);
        if (pl == null) return null;
        return pl.getVersion(version, platform);
    }

    public Product getProductByTag(String name, String tag, String platform) {
        ProductLine pl = getVersions(name);
        if (pl == null) return null;
        return pl.getVersionByTag(tag, platform);
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
    public Product selectProduct(String name, String versionConstraint,
                                 List<String> tags, String platform) 
    {
        ProductLine pl = getVersions(name);
        if (pl == null) return null;
        return pl.selectVersion(versionConstraint, tags, platform);
    }

    /**
     * return an iterator for iterating through the raw product data
     */
    public Iterator<String[]> rawIterator() { return new RawIterator(); }
                                 
    class RawIterator implements Iterator<String[]> {
        Iterator<ProductLine> pli = products.values().iterator();
        Iterator<String[]> pdi = null;

        public RawIterator() {
            if (pli.hasNext()) {
                ProductLine pl = pli.next();
                pdi = pl.rawIterator();
            }
        }

        public boolean hasNext() { return (pdi != null && pdi.hasNext()); }
        public String[] next() {
            String[] out = pdi.next();
            if (! pdi.hasNext() && pli.hasNext()) {
                ProductLine pl = pli.next();
                pdi = pl.rawIterator();
            }
            return out;
        }
        public void remove() { 
            throw new UnsupportedOperationException("ProductDB iterators not editable");
        }
    }

}