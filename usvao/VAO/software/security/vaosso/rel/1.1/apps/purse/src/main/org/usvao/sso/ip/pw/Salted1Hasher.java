package org.usvao.sso.ip.pw;

import org.usvao.sso.ip.SSOProviderSystemException;

import java.util.Properties;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.exceptions.DatabaseAccessException;

/**
 * an engine for hashing passwords using some algorithm.
 */
public class Salted1Hasher extends PasswordHasher {
    public final static String NAME = "SALTED1";

    /** not used at the moment; purse finds this its self */
    protected int niters = 1000;

    /**
     * initialize the engine
     * @param hashIterations   the number of hashing iterations to apply
     */
    public Salted1Hasher(int hashIterations) {
        super(Salted1Hasher.NAME);
        if (hashIterations <= 0) 
            throw 
               new IllegalArgumentException("Salted1Hasher: iters must be > 0");
        niters = hashIterations;
    }

    public Salted1Hasher() {
        super(Salted1Hasher.NAME);
    }

    /**
     * return a hash of the given password applying the SALTED1
     * algorithm
     * @param pwdata   an array in which the first element is the password
     *                   and the subsequent elements are additional needed 
     *                   data (e.g. salt).  The interpretation of the 
     *                   elements (including how many are expected) is 
     *                   implementation-specific.
     */
    public String hash(Object[] pwdata) throws SSOProviderSystemException {
        return hash((String) pwdata[0], (String) pwdata[1]);
    }

    /**
     * return a hash of the given password applying the SALTED1
     * algorithm
     * @param password   the user's password
     * @param salt       salt to combine with 
     */
    public String hash(String password, String salt) 
        throws SSOProviderSystemException 
    {
        try {
            String[] hashed = UserDataHandler.passwordSha(password, salt);
            return hashed[0];
        } catch (DatabaseAccessException ex) {
            throw new SSOProviderSystemException(ex.getMessage(), ex);
        }
    }

    /**
     * generate some random salt
     */
    public String newSalt() {
        try {
            // Uses a secure Random not a simple Random
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            // Salt generation 256 bits long
            byte[] bSalt = new byte[32];
            random.nextBytes(bSalt);
            return Hex.encodeHexString(bSalt);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No such algorithm exception", e);
        } 

    }

    /**
     * return password data for hash() given the password and a set of 
     * user attributes
     */
    public Object[] passwordData(String password, Properties useratts) {
        Object[] out = new Object[2];
        out[0] = password;
        out[1] = useratts.getProperty("salt");
        return out;
    }

}

