package org.usvao.sso.ip.pw;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

import java.util.List;
import java.util.LinkedList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;

public class TestBannedWordsPasswordChecker {

    @Test
    public void testOneWord() {
        BannedWordsPasswordChecker chkr = 
            new BannedWordsPasswordChecker("bosco");

        assertEquals(1, chkr.getWordCount());
        assertTrue(chkr.passwordIsValid("gooberman"));
        assertTrue(chkr.passwordIsValid("GooberMan"));
        assertFalse(chkr.passwordIsValid("bosco"));
        assertFalse(chkr.passwordIsValid("BOSCO"));
        assertFalse(chkr.passwordIsValid("Bosco"));
        assertFalse(chkr.passwordIsValid("bosCo"));

        chkr.addWord("GooberMan");
        assertEquals(2, chkr.getWordCount());
        assertFalse(chkr.passwordIsValid("gooberman"));
        assertFalse(chkr.passwordIsValid("GooberMan"));
        assertFalse(chkr.passwordIsValid("bosco"));
        assertFalse(chkr.passwordIsValid("BOSCO"));
        assertFalse(chkr.passwordIsValid("Bosco"));
        assertFalse(chkr.passwordIsValid("bosCo"));
        assertTrue(chkr.passwordIsValid("goober"));

        // bosco ends with a zero:
        chkr.addWords(new String[] { "gOOberman", "bosc0" });
        assertEquals(3, chkr.getWordCount());
        assertFalse(chkr.passwordIsValid("gooberman"));
        assertFalse(chkr.passwordIsValid("bosco"));
        assertFalse(chkr.passwordIsValid("bosc0"));
        assertTrue(chkr.passwordIsValid("goober"));

    }

    @Test
    public void testMultiWords() {
        // bosco ends with a zero:
        BannedWordsPasswordChecker chkr = 
            new BannedWordsPasswordChecker(
                    new String[] { "gOOberman", "bosc0", "password" }, false); 

        assertEquals(3, chkr.getWordCount());
        assertFalse(chkr.passwordIsValid("gooberman"));
        assertTrue(chkr.passwordIsValid("bosco"));
        assertFalse(chkr.passwordIsValid("bosc0"));
        assertFalse(chkr.passwordIsValid("passWord"));
        assertTrue(chkr.passwordIsValid("goober"));
        
    }

    @Test
    public void testDefaults() {
        BannedWordsPasswordChecker chkr = new BannedWordsPasswordChecker();

        assertTrue(chkr.getWordCount() > 12);
        assertTrue(chkr.passwordIsValid("gooberman"));
        assertFalse(chkr.passwordIsValid("bosco"));
        assertFalse(chkr.passwordIsValid("b0sco"));
        assertFalse(chkr.passwordIsValid("passWord"));
        assertFalse(chkr.passwordIsValid("PASSWORD"));
        assertTrue(chkr.passwordIsValid("goober"));
        
    }

    @Test
    public void testFileNoIgnore() throws IOException {
        InputStream tstrm = 
            getClass().getResourceAsStream("testbannedwords.txt");
        if (tstrm == null)
            throw new RuntimeException("Failed to load test file, " +
                                       "testbannedwords.txt");

        BannedWordsPasswordChecker chkr = 
            new BannedWordsPasswordChecker(new InputStreamReader(tstrm), false);
        
        assertEquals(20, chkr.getWordCount());
        assertTrue(chkr.passwordIsValid("gooberman"));
        assertTrue(chkr.passwordIsValid("bosco"));

        assertFalse(chkr.passwordIsValid("my"));
        assertFalse(chkr.passwordIsValid("jump|ed"));
        assertFalse(chkr.passwordIsValid("you're"));
        assertFalse(chkr.passwordIsValid("dogs."));
        assertFalse(chkr.passwordIsValid("FOX"));

        assertTrue(chkr.passwordIsValid("jumped"));
        assertTrue(chkr.passwordIsValid("dogs"));
        assertTrue(chkr.passwordIsValid("ed"));
    }

    @Test
    public void testFileIgnore() throws IOException {
        InputStream tstrm = 
            getClass().getResourceAsStream("testbannedwords.txt");
        if (tstrm == null)
            throw new RuntimeException("Failed to load test file, " +
                                       "testbannedwords.txt");

        BannedWordsPasswordChecker chkr = 
            new BannedWordsPasswordChecker(new InputStreamReader(tstrm), true);
        
        assertEquals(21, chkr.getWordCount());
        assertTrue(chkr.passwordIsValid("gooberman"));
        assertTrue(chkr.passwordIsValid("bosco"));

        assertFalse(chkr.passwordIsValid("my"));
        assertFalse(chkr.passwordIsValid("you're"));
        assertFalse(chkr.passwordIsValid("FOX"));
        assertFalse(chkr.passwordIsValid("dogs"));
        assertFalse(chkr.passwordIsValid("ed"));

        assertTrue(chkr.passwordIsValid("jump|ed"));
        assertTrue(chkr.passwordIsValid("dogs."));
        assertTrue(chkr.passwordIsValid("jumped"));
    }

    public static void main(String[] args) {
        JUnitCore.runClasses(TestLikeWordsPasswordChecker.class);
    }

}


