package org.usvao.sso.openid.portal;

import java.util.Map;
import java.util.Collection;

/**
 * an interface for getting information about registered users
 */
public interface UserDatabase extends UserStatusSource {

    /**
     * return the user attributes associated with a given user identifier
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    public Map<String, ? extends Object> getUserAttributes(String userid)
        throws UserDbAccessException, UnrecognizedUserException;

    /**
     * return the user authorizations as a list.  Each string in the
     * list is a name of some permission (or logical set of permissions)
     * afforded to the user that controls what the user has access to 
     * in the portal.  The supported names are implementation dependent.
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    public Collection<String> getUserAuthorizations(String userid)
        throws UserDbAccessException, UnrecognizedUserException;

               

}