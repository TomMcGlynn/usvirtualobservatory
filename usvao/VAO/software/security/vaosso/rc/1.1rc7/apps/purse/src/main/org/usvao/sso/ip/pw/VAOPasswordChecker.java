package org.usvao.sso.ip.pw;

import java.util.List;
import java.util.Vector; 

/**
 * a {@link PasswordChecker PasswordChecker} that makes sure that the 
 * password complies with the password policies of the VAO.  This combines 
 * the checking from the following checker implementations:
 * <ul>
 *  <li> {@link BannedWordsPasswordChecker}: checks for match against a set 
 *       of banned words </li>
 *  <li> {@link TooShortPasswordChecker}: ensures the password is a minimum 
 *       of 6 characters. </li>
 *  <li> {@link LikeWordsPasswordChecker}: ensures the password is not too 
 *       similar to the users username, first name, last name, or institution. 
 *       </li>
 * </ul>
 */
public class VAOPasswordChecker extends PasswordCheckerBase {

    PasswordChecker[] checkers = new PasswordChecker[3];

    /**
     * create the checker, providing information about the user.
     * @param firstname    the user's first names and/or initials
     * @param lastname     the user's last name and/or initials
     * @param username     the user's login name
     * @param inst         the user's institution
     * @param email        the user's email address
     * @param minlength    the minimum length required for a password
     */
    public VAOPasswordChecker(String firstname, String lastname, 
                              String username, String inst, String email,
                              int minlength) 
    {
        checkers[0] = new TooShortPasswordChecker(minlength);
        checkers[1] = new BannedWordsPasswordChecker();

        LikeWordsPasswordChecker lckr = 
            new LikeWordsPasswordChecker(username, "your username", minlength);

        // look for passwords too close to the last name.  Check against
        // both the literal value and one with spaces and dashes removed.
        lckr.addWord(lastname, "your last name");
        String alt = lastname.replaceAll("[\\s\\-]+", "");
        if (! alt.equals(lastname))
            lckr.addWord(alt, "your last name");

        // look for passwords too close words in the first name.  
        // We're only going to bother if the word is at least 6 characters
        // long.
        String[] nms = firstname.split("[\\s\\.\\-,]+");
        for (String name : nms) {
            if (name.length() >= minlength) 
                lckr.addWord(name, "your first name");
        }

        // look for passwords too close the institution, if one has been 
        // provided.  (Not sure how useful this is.)
        if (inst != null && inst.length() > 0)
            lckr.addWord(inst, "your institution");

        // look for passwords similar to the email address.
        lckr.addWord(email, "your email address");
        checkers[2] = lckr;
    }

    /**
     * create the checker, providing information about the user.
     * @param firstname    the user's first names and/or initials
     * @param lastname     the user's last name and/or initials
     * @param username     the user's login name
     * @param inst         the user's institution
     * @param email        the user's email address
     */
    public VAOPasswordChecker(String firstname, String lastname, 
                              String username, String inst, String email) 
    {
        this(firstname, lastname, username, inst, email, 6);
    }

    /**
     * add a list of reasons as to why the given password does not 
     * comply with password policies to a given List.  If the password
     * is fully compliant with policy, nothing is added to the list and 
     * zero is returned.
     * <p>
     * This implementation applies all the checks describe above.
     */
    public int explainNoncompliance(String password, List<String> reasons) {
        int nprobs = 0;

        for (PasswordChecker pc : checkers)
            nprobs += pc.explainNoncompliance(password, reasons);

        return nprobs;
    }

}