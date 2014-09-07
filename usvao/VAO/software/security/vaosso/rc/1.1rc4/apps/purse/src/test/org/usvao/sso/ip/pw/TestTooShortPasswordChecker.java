package org.usvao.sso.ip.pw;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;

public class TestTooShortPasswordChecker {

    @Test
    public void testDefaultLength() {
        TooShortPasswordChecker chkr = new TooShortPasswordChecker();
        assertEquals(chkr.getMinLength(), 6);
        assertTrue(chkr.passwordIsValid("gooberman"));
        assertTrue(chkr.passwordIsValid("goober"));
        assertTrue(chkr.passwordIsValid("goobe "));
        assertTrue(chkr.passwordIsValid(" oober"));
        assertTrue(! (chkr.passwordIsValid("oober")) );
        assertTrue(! (chkr.passwordIsValid("")) );

        String[] why = chkr.explainNoncompliance("oober");
        assertEquals(why.length, 1);
        assertTrue(why[0].startsWith("Password must be at least "));

        why = chkr.explainNoncompliance("goober");
        assertEquals(why.length, 0);
    }

    @Test
    public void testSpecifiedLength() {
        TooShortPasswordChecker chkr = new TooShortPasswordChecker(3);
        assertEquals(chkr.getMinLength(), 3);
        assertTrue(chkr.passwordIsValid("gooberman"));
        assertTrue(chkr.passwordIsValid("goober"));
        assertTrue(chkr.passwordIsValid("goobe "));
        assertTrue(chkr.passwordIsValid(" oober"));
        assertTrue(chkr.passwordIsValid("oober"));
        assertTrue(! (chkr.passwordIsValid("er")) );
        assertTrue(! (chkr.passwordIsValid("")) );

        String[] why = chkr.explainNoncompliance("er");
        assertEquals(why.length, 1);
        assertTrue(why[0].startsWith("Password must be at least "));

        why = chkr.explainNoncompliance("goober");
        assertEquals(why.length, 0);
    }

    @Test
    public void testAddReason() {
        TooShortPasswordChecker chkr = new TooShortPasswordChecker(4);
        LinkedList<String> reasons = new LinkedList<String>();
        assertEquals(chkr.explainNoncompliance("goober", reasons), 0);
        assertEquals(reasons.size(), 0);
        assertEquals(chkr.explainNoncompliance("goo", reasons), 1);
        assertEquals(reasons.size(), 1);
    }
}