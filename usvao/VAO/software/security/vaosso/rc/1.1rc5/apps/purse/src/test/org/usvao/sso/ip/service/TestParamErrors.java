package org.usvao.sso.ip.service;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

public class TestParamErrors {

    ParamErrors errs = null;
    public static final String USERNAME    = "userName";
    public static final String LASTNAME    = "lastName";
    public static final String FIRSTNAME   = "firstName";
    static final String[] parameters = { FIRSTNAME, LASTNAME, USERNAME };

    @Before
    public void makeInst() {
        errs = new ParamErrors(parameters);
    }

    @Test
    public void testErrorMsg() {
        String[] msgs = null;

        assertFalse(errs.hasMessages());
        assertEquals(0, errs.getMessageCount());
        assertFalse(errs.hasMessagesFor(FIRSTNAME));
        assertFalse(errs.hasMessagesFor(LASTNAME));
        assertNull(errs.getMessagesFor(FIRSTNAME));
        assertNull(errs.getMessagesFor(LASTNAME));

        errs.addMessage(FIRSTNAME, "You have a dumb first name");
        assertTrue(errs.hasMessages());
        assertEquals(1, errs.getMessageCount());
        assertTrue(errs.hasMessagesFor(FIRSTNAME));
        assertFalse(errs.hasMessagesFor(LASTNAME));
        assertNull(errs.getMessagesFor(LASTNAME));
        msgs = errs.getMessagesFor(FIRSTNAME);
        assertNotNull(msgs);
        assertEquals(1, msgs.length);
        assertEquals("You have a dumb first name", msgs[0]);

        errs.addMessage(FIRSTNAME,"You have a last name for a first name");
        assertTrue(errs.hasMessages());
        assertEquals(2, errs.getMessageCount());
        assertTrue(errs.hasMessagesFor(FIRSTNAME));
        assertFalse(errs.hasMessagesFor(LASTNAME));
        assertNull(errs.getMessagesFor(LASTNAME));
        msgs = errs.getMessagesFor(FIRSTNAME);
        assertNotNull(msgs);
        assertEquals(2, msgs.length);
        assertEquals("You have a dumb first name", msgs[0]);
        assertEquals("You have a last name for a first name", msgs[1]);

        errs.addMessage(LASTNAME, "Do you even have a last name?");
        assertTrue(errs.hasMessages());
        assertEquals(3, errs.getMessageCount());
        assertTrue(errs.hasMessagesFor(FIRSTNAME));
        assertTrue(errs.hasMessagesFor(LASTNAME));
        assertFalse(errs.hasMessagesFor(USERNAME));
        assertNotNull(errs.getMessagesFor(LASTNAME));
        assertEquals(1, errs.getMessagesFor(LASTNAME).length);
        assertNotNull(errs.getMessagesFor(FIRSTNAME));
        assertEquals(2, errs.getMessagesFor(FIRSTNAME).length);
    }

    @Test
    public void testClear() {
        errs.addMessage(LASTNAME, "Do you even have a last name?");
        assertTrue(errs.hasMessages());
        String[] msgs = errs.getMessagesFor(LASTNAME);
        assertNotNull(msgs);
        assertEquals(1, msgs.length);

        errs.clear();
        assertFalse(errs.hasMessages());
        assertNull(errs.getMessagesFor(LASTNAME));
        assertEquals(0, errs.getMessageCount());
    }

    public static void main(String[] args) {
        JUnitCore.runClasses(TestParamErrors.class);
    }
}
