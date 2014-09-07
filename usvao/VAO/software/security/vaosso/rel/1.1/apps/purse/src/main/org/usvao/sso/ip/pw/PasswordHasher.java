package org.usvao.sso.ip.pw;

import org.usvao.sso.ip.SSOProviderSystemException;

import java.util.Properties;

/**
 * an engine for hashing passwords using some algorithm.  This comes with 
 * its own factory function.
 */
public abstract class PasswordHasher {

    protected final String name;

    /**
     * initialize the engine
     * @param methodName   the password hashing method name (used in DB)
     */
    protected PasswordHasher(String methodName) {
        name = methodName;
    }

    /**
     * return the hashing method's name
     */
    public final String getName() { return name; }

    /**
     * return a hash of the given password applying the implementation's 
     * algorithm
     * @param pwdata   an array in which the first element is the password
     *                   and the subsequent elements are additional needed 
     *                   data (e.g. salt).  The interpretation of the 
     *                   elements (including how many are expected) is 
     *                   implementation-specific.
     */
    public abstract String hash(Object[] pwdata) 
        throws SSOProviderSystemException ;

    /**
     * return password data for hash() given the password and a set of 
     * user attributes
     */
    public abstract Object[] passwordData(String password, Properties useratts);

    /** 
     * return true if the password matches the given hash.
     * @param hashed   the expected hash
     * @param pwdata   an array in which the first element is the password
     *                   and the subsequent elements are additional needed 
     *                   data (e.g. salt).  The interpretation of the 
     *                   elements (including how many are expected) is 
     *                   implementation-specific.
     */
    public boolean matches(String hashed, Object[] pwdata) 
        throws SSOProviderSystemException 
    {
        return this.hash(pwdata).equals(hashed);
    }

    /** 
     * return true if the password matches the given hash.
     * @param hashed   the expected hash
     * @param pwdata   an array in which the first element is the password
     *                   and the subsequent elements are additional needed 
     *                   data (e.g. salt).  The interpretation of the 
     *                   elements (including how many are expected) is 
     *                   implementation-specific.
     */
    public boolean matches(String hashed, String password, Properties useratts)
        throws SSOProviderSystemException 
    {
        return matches(hashed, passwordData(password,useratts));
    }

    /**
     * return a hasher with the given name
     */
    public static PasswordHasher hasherFor(String name) {
        if ("SALTED1".equals(name)) 
            return new Salted1Hasher();
        throw new IllegalArgumentException("Unrecognized hasher name: "+name);
    }


}