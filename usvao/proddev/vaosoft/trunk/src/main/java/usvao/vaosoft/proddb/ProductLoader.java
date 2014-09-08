package usvao.vaosoft.proddb;

import java.io.IOException;

/**
 * an abstract class for loading Product data in a database
 */
public interface ProductLoader {

    /**
     * load all available product data.  Send data for each product 
     * to a LoadProduct destination.  
     */
    public void load(LoadProduct destination) throws IOException;

    /**
     * count the number of product records in the database
     */
    public int countProducts() throws IOException;

    /**
     * return the time of the last modification to the store's contents or 
     * -1 if this cannot be determined.  This value can be used to trigger
     * a re-read of the data.
     */
    public long lastModified();


}