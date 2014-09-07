package org.usvao.sso.openid.portal;

import java.io.IOException;

/**
 * an interface for determining the registration status of a user to a portal.
 * <p>
 * If a class that implements this interface also implements the 
 * {@link CommonUserStatus} interface, then the status values defined there
 * will be supported by {@link #getUserStatus(String) getUserStatus()}.  
 * More precisely, values returned 
 * {@link #getUserStatus(String) getUserStatus()} that match those 
 * defined in {@link CommonUserStatus} will have the meanings defined by 
 * {@link CommonUserStatus}; the class need not actually support all of 
 * concepts defined by {@link CommonUserStatus}, and the class may support 
 * other values as well.  
 *
 * @see CommonUserStatus
 * @see UserRegistration
 * @see UserDatabase
 */
public interface UserStatusSource {

    /**
     * return the current registration status of a user
     * @throws UserDbAccessException   if a failure occurs while trying to 
     *                                   access the user database.
     */
    public int getUserStatus(String userid)
        throws UserDbAccessException;

    /**
     * return true if the user exists in the user database.  
     */
    public boolean isRecognized(String userid)
        throws UserDbAccessException;

    /**
     * return true if the user status value indicates that the user 
     * it applies to exists in the user database.  This method can be 
     * necessary in generic code because the user database is free to 
     * use its own convention for status values.  
     */
    public boolean isRecognized(int status);
}
