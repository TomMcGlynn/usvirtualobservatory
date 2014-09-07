package org.usvao.sso.ip.service;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;
import org.usvao.sso.ip.SSOProviderSystemException;

public class TestResetPasswordRequest {

    ResetPasswordRequest inps = null;

    @Before
    public void makeInst() {
        inps = new ResetPasswordRequest();
    }

    @Test
    public void testInitState() {
        assertEquals("", inps.getUserName());
        assertEquals("", inps.getToken());
    }

    @Test
    public void testGetSet() {
        String VAL = "goober";
        int i = 0;
        inps.setUserName(VAL+i++);  
        inps.setToken(VAL+i++); 

        i=0;
        assertEquals(VAL+i++, inps.getUserName());  
        assertEquals(VAL+i++, inps.getToken());  
    }

    @Test
    public void testValidateOK() {
        assertFalse(inps.errorsFound());

        inps.setUserName("thicks");
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());

        inps.setToken("hinkee");
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
    }    

    @Test
    public void testValidateNoUsername() {
        assertFalse(inps.errorsFound());

        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.exportErrors().getMessageCount());
        assertEquals(1, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertNull(inps.getErrorMsgsFor(inps.TOKEN));

        inps.setToken("hinkee");
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.exportErrors().getMessageCount());
        assertEquals(1, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertNull(inps.getErrorMsgsFor(inps.TOKEN));
    }        

    public static void main(String[] args) {
        JUnitCore.runClasses(TestRemindUserNameRequest.class);
    }
}
