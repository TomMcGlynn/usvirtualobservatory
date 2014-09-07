package org.usvao.sso.openid.portal;

import java.util.Properties;
import java.util.Collection;

/**
 * an interface for registering a user to a portal.  
 * <p>
 * This interface is intended as the means for moving data extracted from a 
 * registration form into the portal's (encapsulated) user database.  
 * Its use is optional and is provided to support the simple registrations 
 * mechanisms provided by this package; instead of using this interface, 
 * a registration application could work with the portal's specific
 * user database interface directly.  
 * <p>
 * Attributes of the user can be registered and updated through this 
 * interface.  Attributes are named String values and are intended to 
 * correspond to parameters loaded from the registration form, filtered or 
 * unfiltered (such as, for example, "first name", "last name", "email", 
 * etc.).  The implementation of this interface may choose to silently
 * ignore attributes with unrecognized names or to throw an 
 * {@link UnrecognizedUserAttributeException}.
 * <p>
 * The user status is represented as an integer whose meaning is 
 * implementation-dependent.  The {@link CommonUserStatus} interface 
 * defines a set of possible values that may be used or extended, but an 
 * but an application is not required to use them.
 * <p>
 * Note that among the registration exceptions that could be thrown is 
 * {@link RegistrationOpNotSupported}.  Any of the operations can choose to 
 * throw this exception.  
 */
public interface UserRegistration extends UserStatusSource {

    /**
     * register a user.  If the user has already been registered, the
     * user data should be updated with this new information; otherwise,
     * the user should just be added.  
     * <p>
     * Attributes may come directly from a registration form; if so, 
     * this implementation is expected to map the names to those taken 
     * by the user database.
     * 
     * @param userid         the username to register
     * @param status         the integer that represents the user's new 
     *                          status (e.g. active, pending, disabled, etc.)
     * @param authorizations the set of strings that represents the 
     *                          authorization roles granted to the user.
     * @param attributes     the set of attributes to associate with the 
     *                          user.  The implementation is allowed to 
     *                          ignore any attributes whose names it does 
     *                          not recognize.  
     * @return boolean  true if this userid was not previously registered
     */
    public boolean registerUser(String userid, int status, 
                                Collection<String> authorizations,
                                Properties attributes)
        throws RegistrationException;

    /**
     * update the status of a user
     */
    public void updateStatus(String userid, int status)
        throws RegistrationException, UnrecognizedUserException;

    /**
     * update the attributes for a user
     */
    public void updateAttributes(String userid, Properties attributes)
        throws RegistrationException, UnrecognizedUserException;

    /**
     * add authorizations for the user
     */
    public void addAuthorizations(String userid, 
                                  Collection<String> authorizations)
        throws RegistrationException, UnrecognizedUserException;

    /**
     * remove authorizations for the user
     */
    public void removeAuthorizations(String userid, 
                                     Collection<String> authorizations)
        throws RegistrationException, UnrecognizedUserException;
}