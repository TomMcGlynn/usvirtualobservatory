package usvao.vaosoft.proddb.store;

/**
 * an exception indicating an unexpected problem (usually I/O related) 
 * was encountered while accessing a product database store.
 */
public class StoreAccessException extends RuntimeException {

    /**
     * wrap an exception representing the underlying cause
     */
    public StoreAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * wrap an exception representing the underlying cause
     */
    public StoreAccessException(Throwable cause) {
        super("Problem accessing product database store: " + 
              cause.getMessage(), cause);
    }

    /**
     * create the exception
     */
    public StoreAccessException(String message) {
        super(message);
    }
}