package org.usvao.sso.ip.pw;

import java.util.List;

/**
 * an interface responsible for validating a password value as acceptable
 * according to policies
 */
public interface PasswordChecker {

    /**
     * return true if the password appears to comply with password policies.
     */
    public boolean passwordIsValid(String password);

    /**
     * add a list of reasons as to why the given password does not 
     * comply with password policies to a given List.  If the password
     * is fully compliant with policy, nothing is added to the list and 
     * zero is returned.
     * @return int   the number of failure explanations added to the list
     */
    public int explainNoncompliance(String password, List<String> reasons);

    /**
     * return a list of reasons as to why the given password does not 
     * comply with password policies.  If the password is compliant,
     * a zero-length array will be returned.  
     * <p>
     * A null value should never be returned. 
     */
    public String[] explainNoncompliance(String password);


}
