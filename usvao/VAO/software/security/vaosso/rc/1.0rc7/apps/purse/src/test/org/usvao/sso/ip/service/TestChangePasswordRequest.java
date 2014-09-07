package org.usvao.sso.ip.service;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

public class TestChangePasswordRequest {

    ChangePasswordRequest inps = null;

    @Before
    public void makeInst() {
        inps = new ChangePasswordRequest();
    }

    @Test
    public void testInitState() {
        assertEquals("", inps.getUserName());
        assertEquals("", inps.getPassword());
        assertEquals("", inps.getNewpw1()); 
        assertEquals("", inps.getNewpw2()); 
        assertEquals("", inps.getToken()); 
    }

    @Test
    public void testGetSet() {
        String VAL = "goober";
        int i = 0;
        inps.setUserName(VAL+i++);  
        inps.setPassword(VAL+i++); 
        inps.setNewpw1(VAL+i++);      
        inps.setNewpw2(VAL+i++);     
        inps.setToken(VAL+i++);     

        i=0;
        assertEquals(VAL+i++, inps.getUserName());  
        assertEquals(VAL+i++, inps.getPassword());  
        assertEquals(VAL+i++, inps.getNewpw1());  
        assertEquals(VAL+i++, inps.getNewpw2());  
        assertEquals(VAL+i++, inps.getToken());  
    }

    @Test
    public void testValidateOK() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setPassword("hinkee");
        inps.setNewpw1("imAgr8Idol");
        inps.setNewpw2("imAgr8Idol");

        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
    }

    @Test
    public void testValidateOKToken() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setToken("hinkee");
        inps.setNewpw1("imAgr8Idol");
        inps.setNewpw2("imAgr8Idol");

        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
    }

    @Test
    public void testValidateNoMatch() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setPassword("hinkee");
        inps.setNewpw1("imAgr8Idol");
        inps.setNewpw2("goober");

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertNull(inps.getErrorMsgsFor(inps.NEWPW2));
        assertEquals(1, inps.getErrorMsgsFor(inps.NEWPW1).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

    @Test
    public void testValidateNoNewPw2() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setPassword("hinkee");
        inps.setNewpw1("imAgr8Idol");

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertNull(inps.getErrorMsgsFor(inps.NEWPW2));
        assertEquals(1, inps.getErrorMsgsFor(inps.NEWPW1).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

    @Test
    public void testValidateNoNewPw1() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setPassword("hinkee");
        inps.setNewpw2("imAgr8Idol");

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertNull(inps.getErrorMsgsFor(inps.NEWPW2));
        assertEquals(1, inps.getErrorMsgsFor(inps.NEWPW1).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

    @Test
    public void testValidateNoNewPw() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setPassword("hinkee");

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertNull(inps.getErrorMsgsFor(inps.NEWPW2));
        assertEquals(1, inps.getErrorMsgsFor(inps.NEWPW1).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

    @Test
    public void testValidateNoPw() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        inps.setNewpw2("imAgr8Idol");
        inps.setNewpw1("imAgr8Idol");

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertNull(inps.getErrorMsgsFor(inps.NEWPW2));
        assertNull(inps.getErrorMsgsFor(inps.NEWPW1));
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

    @Test
    public void testValidateNoUsername() {
        assertFalse(inps.errorsFound());

        inps.setPassword("hinkee");
        inps.setNewpw1("imAgr8Idol");
        inps.setNewpw2("imAgr8Idol");

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertEquals(1, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

    @Test
    public void testValidateNoInputs() {
        assertFalse(inps.errorsFound());

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(4, inps.exportErrors().getMessageCount());
        assertEquals(1, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD).length);
        assertEquals(1, inps.getErrorMsgsFor(inps.NEWPW1).length);
        assertEquals(1, inps.getErrorMsgsFor("").length);
    }

}
