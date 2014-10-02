/*
 * DalServerException.java
 * $ID*
 */

package dalserver;

/**
 * Numeric and nonnumeric ranges cannot be mixed in the same range list.
 */
public class DalServerException extends Exception {
    private static final long serialVersionUID = 1;

    public DalServerException() { }
    public DalServerException(String s) { super(s); }
}
