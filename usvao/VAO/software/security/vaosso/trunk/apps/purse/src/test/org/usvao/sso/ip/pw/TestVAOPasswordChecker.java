package org.usvao.sso.ip.pw;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;

public class TestVAOPasswordChecker {

    @Test
    public void testCheck() {
        PasswordChecker chkr = 
            new VAOPasswordChecker("Johannes D.", "van der Waals",
                                   "force", "Amsterdam", "waals@gmail.com");

        assertTrue(chkr.passwordIsValid("gooberman"));
        assertFalse(chkr.passwordIsValid("bosco"));
        assertFalse(chkr.passwordIsValid("b0sco"));
        assertFalse(chkr.passwordIsValid("passWord"));
        assertFalse(chkr.passwordIsValid("PASSWORD"));
        assertTrue(chkr.passwordIsValid("goober"));

        assertFalse(chkr.passwordIsValid("van der Waals"));
        assertFalse(chkr.passwordIsValid("vanderWaals"));
        assertFalse(chkr.passwordIsValid("Johannes"));
        assertFalse(chkr.passwordIsValid("JohannesForce"));
        assertFalse(chkr.passwordIsValid("WaalsForce"));
        assertFalse(chkr.passwordIsValid("JohannesD"));
        assertFalse(chkr.passwordIsValid("UAmsterdam"));
        assertTrue(chkr.passwordIsValid("JohannesDiderik"));

        assertFalse(chkr.passwordIsValid("van"));

        assertFalse(chkr.passwordIsValid("waals@gmail.com"));
    }


    public static void main(String[] args) {
        JUnitCore.runClasses(TestVAOPasswordChecker.class);
    }

}
