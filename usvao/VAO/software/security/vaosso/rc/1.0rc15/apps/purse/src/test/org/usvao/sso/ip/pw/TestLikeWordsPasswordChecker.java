package org.usvao.sso.ip.pw;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;

public class TestLikeWordsPasswordChecker {

    @Test
    public void testOneWord() {
        LikeWordsPasswordChecker chkr = 
            new LikeWordsPasswordChecker("guest", "the username", 4);
        assertEquals(chkr.getMinLeftoverLength(), 4);
        String[] words = chkr.getWords();
        assertEquals(words.length, 1);
        assertEquals(words[0], "guest");
        words = chkr.getWordLabels();
        assertEquals(words.length, 1);
        assertEquals(words[0], "the username");

        assertTrue(chkr.passwordIsValid("gooberman"));
        assertFalse(chkr.passwordIsValid("guest"));
        assertFalse(chkr.passwordIsValid("newguest"));
        assertFalse(chkr.passwordIsValid("unguesty"));
        assertFalse(chkr.passwordIsValid("guestier"));
        assertTrue(chkr.passwordIsValid("gooberguestman"));

        assertEquals(chkr.explainNoncompliance("gooberman").length, 0);
        assertEquals(chkr.explainNoncompliance("guest").length, 1);

        LinkedList<String> reasons = new LinkedList<String>();
        assertEquals(reasons.size(), 0);
        assertEquals(chkr.explainNoncompliance("gooberman", reasons), 0);
        assertEquals(reasons.size(), 0);
        assertEquals(chkr.explainNoncompliance("guest", reasons), 1);
        assertEquals(reasons.size(), 1);
    }

    @Test
    public void testMultiWord() {
        LikeWordsPasswordChecker chkr = new LikeWordsPasswordChecker();
        assertEquals(chkr.getMinLeftoverLength(), 6);
        assertEquals(chkr.getWords().length, 0);

        chkr.addWord("John", "your first name");
        assertEquals(chkr.getWords().length, 1);
        chkr.addWord("guy", "the username");
        assertEquals(chkr.getWords().length, 2);
        chkr.addWord("Doe", "your last name");
        assertEquals(chkr.getWords().length, 3);

        assertEquals(0, chkr.explainNoncompliance("gooberman").length);
        assertEquals(1, chkr.explainNoncompliance("guy").length);
        assertEquals(3, chkr.explainNoncompliance("johnguy").length);
        assertEquals(1, chkr.explainNoncompliance("johndoeguy").length);
        assertEquals(0, chkr.explainNoncompliance("johndoeguygoober").length);
    }
        

    public static void main(String[] args) {
        JUnitCore.runClasses(TestLikeWordsPasswordChecker.class);
    }

}

