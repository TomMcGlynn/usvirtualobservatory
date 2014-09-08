package usvao.vaosoft.proddb;

import java.util.Iterator;
import java.io.IOException;

/**
 * an interface for reading and saving Product data from/to storage
 */
public interface ProductDataIO extends ProductLoader, ProductExporter {

    /**
     * return an iterator that will return each of the available 
     * product data in the store in turn.
     */
    public Iterator<String[]> rawIterator() throws IOException;
}
