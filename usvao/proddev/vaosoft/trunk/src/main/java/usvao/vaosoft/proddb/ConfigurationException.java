package usvao.vaosoft.proddb;

/**
 * an exception indicating a problem with the way a stack or its 
 * product database is configured incorrectly.
 */
public class ConfigurationException extends Exception {

    /**
     * wrap an exception representing the underlying cause
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * wrap an exception representing the underlying cause
     */
    public ConfigurationException(Throwable cause) {
        super("Configuration problem: " + cause.getMessage(), cause);
    }

    /**
     * create the exception
     */
    public ConfigurationException(String message) {
        super(message);
    }
}