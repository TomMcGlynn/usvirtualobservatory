package usvao.vaosoft.proddb;

import static usvao.vaosoft.proddb.LoadProduct.*; 
import usvao.vaosoft.proddb.store.StoreAccessException;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * a ProductDB that reads its entire listing of installed products from 
 * a persisted store (like a file).  The details of the store is encapsulated
 * by the StackConfig object provided during construction.  The contents of 
 * the persisted store can be brought completely into memory via the 
 * {@link cache()} function; the ProductDB-required functions, however, read
 * subsets of the data directly from the store.  
 */
public class PersistedProductDB extends ProductDB {

    ProductDataIO store = null;
    int count = 0;
    long lastread = 0;

    /**
     * set up this DB
     */
    public PersistedProductDB(StackConfig config, List<String> preferredTags) {
        super(config, preferredTags);

        store = config.getDataStore();
        countProducts();
    }

    synchronized void countProducts() {
        try {
            long ts = (new Date()).getTime();
            count = store.countProducts();
            lastread = ts;
        } catch (IOException ex) {
            throw new StoreAccessException(ex);
        }
    }

    /**
     * return the number of products in this database
     * @throws StoreAccessException  if there is a problem reading the 
     *                                 underlying database.
     */
    public int getCount() {
        long lastmod = store.lastModified();
        if (lastmod <= 0 || lastmod > lastread) countProducts();
        return count;
    }

    /**
     * return an in-memory copy of the product database
     */
    public ProductDB cache() throws IOException {
        InMemoryProductDB out = new InMemoryProductDB(config, prefTags, 10);
        store.load(out);
        return out;
    }

    /**
     * return a list of products that match the given name and version 
     * @param name               the name of the product 
     * @param versionConstraint  a string representing a subset of acceptable 
     *                              versions.  The syntax depends on the 
     *                              VersionConstraintSyntax this class has 
     *                              been configured with.  If null or an empty
     *                              empty string, all versions matching the 
     *                              name and platform will be returned.  
     * @param platform   an (optional) string identifying the platform 
     *                    (typically an OS/distribution/version).  If null,
     *                    return the generic (cross-platform) version.  
     * @return ProductLine  a set of matching versions of products.  
     */
    public ProductDB matchProducts(String name, String versionConstraint,
                                  String platform) 
    {
        if (name != null) name = name.trim();
        if (name.length() == 0) name = null;
        int cap = (name != null || lastread <= 0 || count < 5) ? 10 : count;

        VersionMatcher matcher = null;
        if (versionConstraint != null) 
            versionConstraint = versionConstraint.trim();
        if (versionConstraint.length() == 0) versionConstraint = null;
        if (versionConstraint != null) 
            matcher = config.getVersionHandler().getMatcher(versionConstraint);
            
        InMemoryProductDB out = new InMemoryProductDB(config, prefTags, cap);
        synchronized (store) {
            try {
                Iterator<String[]> di = store.rawIterator();
                String[] data = null;
                while (di.hasNext()) {
                    data = di.next();
                    if ((name == null || name.equals(data[NAME])) &&
                        (matcher == null || matcher.matches(data[VERSION])))
                    {
                        out.loadProduct(data);
                    }
                }
                di = null;
            }
            catch (IOException ex) {
                throw new StoreAccessException(ex);
            }
        }
        return out;
    }

    class LoadFilteredProduct implements LoadProduct {
        LoadProduct del = null;
        LoadFilteredProduct(LoadProduct delegate) {  del = delegate;  }
        public void loadProduct(String[] data) { del.loadProduct(data); }
    }

    /**
     * return a list of all the products that match the given name
     * @param name               the name of the product 
     * @return ProductLine  a set of matching versions of products, or null,
     *                         if no versions match.
     */
    public ProductLine getVersions(String name) {
        BasicProductLine pl = null;
        synchronized (store) {
            try {
                Iterator<String[]> it = store.rawIterator();
                String[] data = null;
                while (it.hasNext()) {
                    data = it.next();
                    if (data[NAME].equals(name)) {
                        if (pl == null) 
                            pl = new BasicProductLine(name, data[ORG], 
                                                      config.getVersionHandler(), 
                                                      config);
                        pl.loadProduct(data);
                    }
                }
                it = null;
            }
            catch (IOException ex) {
                throw new StoreAccessException(ex);
            }
        }
        return null;
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
        synchronized (store) {
            try {
                Iterator<String[]> it = store.rawIterator();
                String[] data = null;
                while (it.hasNext()) {
                    data = it.next();
                    if (data[NAME].equals(name) && data[VERSION].equals(version)) {
                        it = null;
                        return BasicProduct.makeProduct(data, config);
                    }
                }
                it = null;
            }
            catch (IOException ex) {
                throw new StoreAccessException(ex);
            }
        }
        return null;
    }
                              
    public Product getProductByTag(String name, String tag, String platform) {
        ProductLine pl = getVersions(name);
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
        ProductLine wrap = getVersions(name);
        return wrap.selectVersion(versionConstraint, tags, platform);
    }
                                 
    /**
     * return an iterator for iterating through the raw product data
     */
    public Iterator<String[]> rawIterator() {  
        try {
            return store.rawIterator();  
        }
        catch (IOException ex) {
            throw new StoreAccessException(ex);
        }
    }

}

