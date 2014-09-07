package org.usvao.sso.openid.portal;

/**
 * an exception indicating that an unsupported registration method was 
 * called.  
 */
public class RegistrationOpNotSupported extends RegistrationException {

    /**
     * create an exception with a custom message
     * @param message   the explanatory message
     * @param name      the name of the operation.  This can be null. 
     */
    public RegistrationOpNotSupported(String message, String name) {
        super(message);
    }

    /**
     * create an exception indicating the operation with the given 
     * name is not supported.  
     */
    public RegistrationOpNotSupported(String name) {
        super("Registration operation not supported: " + name);
    }

    /**
     * create an exception to indicate that the unsupported updateStatus
     * method was called.
     */
    public static RegistrationOpNotSupported updateStatus() {
        return new RegistrationOpNotSupported("updateStatus");
    }

    /**
     * create an exception to indicate that the unsupported updateAttributes
     * method was called.
     */
    public static RegistrationOpNotSupported updateAttributes() {
        return new RegistrationOpNotSupported("updateAttributes");
    }

    /**
     * create an exception to indicate that the unsupported addAuthorizations
     * method was called.
     */
    public static RegistrationOpNotSupported addAuthorizations() {
        return new RegistrationOpNotSupported("addAuthorizations");
    }

    /**
     * create an exception to indicate that the unsupported 
     * removeAuthorizations method was called.
     */
    public static RegistrationOpNotSupported removeAuthorizations() {
        return new RegistrationOpNotSupported("removeAuthorizations");
    }
}