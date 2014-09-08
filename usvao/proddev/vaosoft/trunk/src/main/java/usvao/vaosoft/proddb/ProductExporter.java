package usvao.vaosoft.proddb;

import java.util.Iterator;
import java.io.IOException;

/**
 * an interface for saving Product data to storage
 */
public interface ProductExporter {

    /**
     * export all product data available via the given iterator.  
     * The values of the String[] elements should conform to the 
     * indexes defined by the integer constants of the LoadProject
     * interface.  
     */
    public void export(Iterator<String[]> di) throws IOException;

}