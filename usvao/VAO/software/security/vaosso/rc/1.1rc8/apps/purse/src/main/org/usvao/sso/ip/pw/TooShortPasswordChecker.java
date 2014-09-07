package org.usvao.sso.ip.pw;

import java.util.List;
import java.util.Vector; 

/**
 * a simple {@link PasswordChecker PasswordChecker} that checks to see that 
 * the password is greater than a certain length.  This is provided as a 
 * simple implementation of the PasswordChecker interface.  
 */
public class TooShortPasswordChecker extends PasswordCheckerBase {

    int minlength = 6;

    /**
     * create the checker, setting the minimum length
     */
    public TooShortPasswordChecker(int minlen) {
        minlength = minlen;
    }

    /**
     * create the checker, setting the minimum length to 6.
     */
    public TooShortPasswordChecker() { this(6); }

    /**
     * return the minimum number of characters required to be in a password.
     */
    public int getMinLength() { return minlength; }

    /**
     * return true if the password appears to comply with password policies.
     */
    @Override
    public boolean passwordIsValid(String password) {
        if (password == null) return false;
        String[] failures = explainNoncompliance(password);
        return (failures.length == 0);
    }

    /**
     * add a list of reasons as to why the given password does not 
     * comply with password policies to a given List.  If the password
     * is fully compliant with policy, nothing is added to the list and 
     * zero is returned.
     * @return int   the number of failure explanations added to the list
     */
    @Override
    public int explainNoncompliance(String password, List<String> reasons) {
        if (password.length() < minlength) {
            reasons.add("Password must be at least " + minlength + 
                        " characters long");
            return 1;
        }
        return 0;
    }

}
