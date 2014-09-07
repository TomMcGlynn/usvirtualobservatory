package org.usvao.sso.openid.portal;

import java.util.ArrayList;
import java.util.Collection;

/**
 * an exception indicating that unrecognized or otherwise unsupported 
 * user attribute names were found while registering a user into the 
 * user database.  
 */
public class UnrecognizedUserAttributeException extends RegistrationException {

    ArrayList<String> attnames = new ArrayList<String>();

    /**
     * construct the exception for a selection of attribute names.  A
     * default message will be constructed using the name.
     * @param name       a single unsupported attribute name
     * @param userid     the user identifier that was being registered
     */
    public UnrecognizedUserAttributeException(String name, String userid) {
        this(name, userid, null);
    }

    /**
     * construct the exception for a selection of attribute names.  A
     * default message will be constructed using the names.
     * @param names      the list of unsupported names detected
     * @param userid     the user identifier that was being registered
     */
    public UnrecognizedUserAttributeException(Collection<String> names, 
                                              String userid) 
    {
        this(names, userid, null);
    }

    /**
     * construct the exception for a selection of attribute names.  A
     * default message will be constructed using the name.
     * @param name       a single unsupported attribute name
     * @param userid     the user identifier that was being registered
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public UnrecognizedUserAttributeException(String name, 
                                              String userid, 
                                              Throwable cause) 
    {
        this(UnrecognizedUserAttributeException.defaultMessage(name),
             name, userid, cause);
    }

    /**
     * construct the exception for a selection of attribute names.  A
     * default message will be constructed using the names.
     * @param names      the list of unsupported names detected
     * @param userid     the user identifier that was being registered
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public UnrecognizedUserAttributeException(Collection<String> names, 
                                              String userid, 
                                              Throwable cause) 
    {
        this(UnrecognizedUserAttributeException.defaultMessage(names),
             names, userid, cause);
    }

    /**
     * construct the exception for a specific userid
     * @param message    a message explaining the error
     * @param name       the list of unsupported names detected
     * @param userid     the user identifier that was being registered
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public UnrecognizedUserAttributeException(String message, String name, 
                                              String userid, Throwable cause) 
    {
        super(message, userid, cause);
        if (name != null) 
            addUnsupportedName(name);
    }

    /**
     * construct the exception for a set of attribute names with a 
     * specialized message.
     * @param message    a message explaining the error
     * @param names      the list of unsupported names detected
     * @param userid     the user identifier that was being registered
     * @param cause      a Throwable indicating the underlying cause of 
     *                        the failure.  
     */
    public UnrecognizedUserAttributeException(String message, 
                                              Collection<String> names, 
                                              String userid, 
                                              Throwable cause) 
    {
        super(message, userid, cause);
        if (names != null) {
            for(String name : names) 
                addUnsupportedName(name);
        }
    }

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     */
    public UnrecognizedUserAttributeException(String message) {
        this(message, null, null);
    }

    /**
     * add an attribute name to be included in the list of attribute 
     * names not supported
     */
    public void addUnsupportedName(String attname) {
        attnames.add(attname);
    }

    /**
     * return the names of the attributes that were detected as unsupported
     */
    String[] getUnsupportedNames() {
        return attnames.toArray(new String[attnames.size()]);
    }

    static String defaultMessage(String name) {
        ArrayList<String> names = new ArrayList<String>(1);
        if (name != null) names.add(name);
        return defaultMessage(names);
    }

    static String defaultMessage(Collection<String> names) {
        StringBuilder sb = new StringBuilder("Unsupported user attribute name");

        if (names == null || names.size() == 0) {
            sb.append("(s) detected");
            return sb.toString();
        }

        if (names.size() > 1) 
            sb.append('s');

        sb.append(": ");
        for(String name : names) 
            sb.append(name).append(", ");
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }
}
