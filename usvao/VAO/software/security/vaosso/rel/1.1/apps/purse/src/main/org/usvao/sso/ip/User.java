package org.usvao.sso.ip;

import java.util.Properties;

import org.usvao.sso.ip.pw.PasswordHasher;

/**
 * a representation of a User and its attributes.
 */
public class User {

    Properties attrs = null;
    String name = null;
    Status stat = Status.UNKNOWN;
    int loginCount = -1;

    /**
     * an indication of the users status in the system.
     */
    public static enum Status {
        /** current status of user is unknown */
        UNKNOWN,
        /** use of username has been requested by a user */
        REQUESTED,
        /** reservation of username is pending */
        PENDING,
        /** user has been accepted as legitimate with current username */
        ACCEPTED,
        /** user with username is barred from accessing system */
        REJECTED,
        /** user access is suspended pending renewal */
        RENEWAL
    }

    /**
     * instantiate a user
     */
    public User(Properties userdata, String username, Status status) {
        name = username;
        if (name == null) 
            throw new IllegalArgumentException("User: username is null");
        attrs = userdata;
        if (status != null) stat = status;
    }

    /**
     * instantiate a user with an unknown status
     */
    public User(Properties userdata, String username) {
        this(userdata, username, null);
    }

    /**
     * return the User's username 
     */
    public String getUsername() { return name; }

    /**
     * return a named attribute
     * @param attname   the name of the attribute
     * @param defval    a value to return if the attribute is not set
     */
    public String getAttribute(String attname, String defval) {
        return attrs.getProperty(attname, defval);
    }

    /**
     * return a named attribute
     * @param attname   the name of the attribute
     */
    public String getAttribute(String attname) {
        return attrs.getProperty(attname);
    }

    /**
     * return the user's status
     */
    public Status getStatus() { return stat; }

    /**
     * return the number of recorded times the user has logged in.
     * If unknown, -1 is returned.
     */
    public int getLoginCount() { return loginCount; }

    /**
     * return true if the user has one of the named roles
     */
    public boolean hasRole(String role) { return false; }

    /**
     * return true if the user has one of the given label attached it
     */
    public boolean hasLabel(String label) { return false; }

    /**
     * return the name of the password hashing method used for this user's
     * password
     */
    public String getPasswordHashMethod() { 
        return attrs.getProperty("passwordMethod", "SHA1");
    }

    /**
     * return the stored hash of user's the password
     */
    public String getPasswordHash() { 
        return attrs.getProperty("passwordHash");
    }

    /**
     * create a password hasher for this user's password
     */
    public PasswordHasher passwordHasher() {
        return PasswordHasher.hasherFor(getPasswordHashMethod());
    }

    /**
     * return true if the given password matches the user's hashed password
     */
    public boolean passwordMatches(String password) 
        throws SSOProviderSystemException 
    {
        return passwordHasher().matches(getPasswordHash(), password, attrs);
    }
}
