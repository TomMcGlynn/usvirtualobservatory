package org.usvao.sso.ip.pw;

import java.util.List;
import java.util.Vector; 

/**
 * a base class for implementations of the PasswordChecker interface.  
 */
public abstract class PasswordCheckerBase implements PasswordChecker {

    /**
     * create the checker, setting the minimum length to 6.
     */
    public PasswordCheckerBase() {  }

    /**
     * return true if the password appears to comply with password policies.
     */
    @Override
    public boolean passwordIsValid(String password) {
        return (explainNoncompliance(password, new Vector<String>()) == 0);
    }

    /**
     * return a list of reasons as to why the given password does not 
     * comply with password policies.  If the password is compliant,
     * a zero-length array will be returned.  
     * <p>
     * A null value will never be returned. 
     */
    public String[] explainNoncompliance(String password) {
        Vector<String> reasons = new Vector<String>();
        explainNoncompliance(password, reasons);
        return reasons.toArray(new String[reasons.size()]);
    }
}
