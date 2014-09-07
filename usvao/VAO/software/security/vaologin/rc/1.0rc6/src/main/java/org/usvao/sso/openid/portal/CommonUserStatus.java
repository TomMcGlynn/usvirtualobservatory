package org.usvao.sso.openid.portal;

/**
 * an interface that gives meaning to a set of user status values that
 * that may be supported by a {@link UserRegistration} implementation.  
 * <p>
 * The "Common" in the interface class name is meant to suggest that it 
 * defines a set of status that are commonly useful when managing registered
 * users of a portal.  A {@link UserRegistration} implementation should 
 * specify that this interface is implemented to indicate that the convention
 * of values it defines are supported.  Not all of the values need necessarily 
 * be used.  
 * <p>
 * A few of the definitions are intended for determining the broad category 
 * that the userid status falls into:
 * <ul>
 *   <li> status &gt;= STATUS_REGISTERED:  indicates that it the userid is 
 *          recognized as registered, though not necessarily active.  
 *   <li> status &lt; STATUS_ACTIVE: indicates that the userid is recognized
 *          as registered but it not (yet) authorized to use the portal. 
 * </ul>
 */
public interface CommonUserStatus {

    /**
     * a status value (0) indicating that the status of the userid is not known
     */ 
    public final static int STATUS_UNKNOWN = 0;

    /**
     * a status value (-1) indicating that the userid is not recognized.  This 
     * is normally because it has not been registered yet. 
     */ 
    public final static int STATUS_UNRECOGNIZED = -1;

    /**
     * a special status value (1) for delineating between registered and 
     * unregistered userids.  If status &gt;= STATUS_REGISTERED, then
     * the userid has not been registered yet.
     */ 
    public final static int STATUS_REGISTERED = 1;

    /**
     * a status value (1) indicating that the userid has been disallowed
     * as an acceptable userid
     */
    public final static int STATUS_DISALLOWED = 1;

    /**
     * a status value (5) indicating that the userid has not yet been 
     * activated.
     */
    public final static int STATUS_DISABLED = 5;

    /**
     * a status value (10) indicating that the userid is for an identity
     * whose validity or authorization has expired.  
     */
    public final static int STATUS_EXPIRED = 10;

    /**
     * a status value (50) indicating that the userid has not yet been 
     * activated.
     */
    public final static int STATUS_PENDING = 50;

    /**
     * a status value (100) indicating that the userid has not yet been 
     * activated.
     */
    public final static int STATUS_ACTIVE = 100;

}