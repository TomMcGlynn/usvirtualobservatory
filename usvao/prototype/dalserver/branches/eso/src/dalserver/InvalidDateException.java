/*
 * InvalidDateException.java
 * $ID*
 */

package dalserver;

/**
 * Numeric and nonnumeric ranges cannot be mixed in the same range list.
 */
public class InvalidDateException extends Exception {
    private static final long serialVersionUID = 1;

    public InvalidDateException() { }
    public InvalidDateException(String s) { super(s); }
}
