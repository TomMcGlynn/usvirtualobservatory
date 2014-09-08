package usvao.vaosoft.proddb;

import java.util.List;
import java.util.Iterator;
import java.util.HashSet;

/**
 * a ProductDB containing zero products.  This is used to for simply creating
 * null selection set.  
 */
public class EmptyProductDB extends ProductDB {

    public static EmptyProductDB INSTANCE = new EmptyProductDB();

    /**
     * set up this DB
     */
    public EmptyProductDB(StackConfig config, List<String> preferredTags) {
        super(config, preferredTags);
    }
    public EmptyProductDB() { super(null, null); }

    /**
     * return the number of products in this database
     */
    public int getCount() { return 0; }

    /**
     * return a list of products that match the given name and version.
     * This implementation always returns itself, regardless of the 
     * inputs.
     * @return ProductDB  an empty set of products.  
     */
    public ProductDB matchProducts(String name, String versionConstraint,
                                   String platform) 
    {
        return this;
    }

    /**
     * return a list of all the products that match the given name.  This
     * implementation always returns null.
     * @param name               the name of the product 
     * @return ProductLine  a set of matching versions of products, or null,
     *                         if no versions match.
     */
    public ProductLine getVersions(String name) {
        return null;
    }

    /**
     * return the product matching the exact constraints.  This implementation
     * always returns null.
     */
    public Product getProduct(String name, String version, String platform) {
        return null;
    }
                              
    public Product getProductByTag(String name, String tag, String platform) {
        return null;
    }
                                   
    /**
     * return a version of a product that matches the given constraints 
     * and with the most preferred tag.  This implementation always returns
     * null.
     */
    public Product selectProduct(String name, String versionConstraint,
                                 List<String> tags, String platform) 
    {
        return null;
    }

    /**
     * return an iterator for iterating through the raw product data
     */
    public Iterator<String[]> rawIterator() { 
        return (new HashSet<String[]>()).iterator();
    }


}