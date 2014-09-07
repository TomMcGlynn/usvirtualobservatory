package org.usvao.sso.ip.service;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

public class TestServiceRequestBean {

    static class TestService extends ServiceRequestBean {

        public static final String USERNAME    = "userName";
        public static final String LASTNAME    = "lastName";
        public static final String FIRSTNAME   = "firstName";
        static final String[] parameters = { FIRSTNAME, LASTNAME, USERNAME };

        public TestService() { super(parameters); }

        public String getFirstName() { return getParameter(FIRSTNAME); }
        public String getLastName() { return getParameter(LASTNAME); }
        public void setFirstName(String firstname) { 
            setParameter(FIRSTNAME, firstname); 
        }
        public void setLastName(String lastname) { 
            setParameter(LASTNAME, lastname); 
        }

        public boolean validate() { 
            throw new InternalError("not implemented");
        }

    }

    TestService inps = null;

    @Before
    public void makeInst() {
        inps = new TestService();
    }

    @Test
    public void testInitState() {
        assertEquals("", inps.getLastName());
        assertEquals("", inps.getFirstName());
    }

    @Test
    public void testGetSet() {
        String VAL = "goober";
        int i = 0;
        inps.setLastName(VAL+i++);  
        inps.setFirstName(VAL+i++); 

        i=0;
        assertEquals(VAL+i++, inps.getLastName());
        assertEquals(VAL+i++, inps.getFirstName());
    }

    @Test
    public void testErrorMsg() {
        String[] msgs = null;
        inps.setFirstName("Taylor");
        assertNotNull(inps.exportErrors());

        assertFalse(inps.errorsFound());
        assertEquals(0, inps.exportErrors().getMessageCount());
        assertFalse(inps.errorsFoundFor(inps.FIRSTNAME));
        assertFalse(inps.errorsFoundFor(inps.LASTNAME));
        assertNull(inps.getErrorMsgsFor(inps.FIRSTNAME));
        assertNull(inps.getErrorMsgsFor(inps.LASTNAME));

        inps.addErrorMsg(inps.FIRSTNAME, "You have a dumb first name");
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.exportErrors().getMessageCount());
        assertTrue(inps.errorsFoundFor(inps.FIRSTNAME));
        assertFalse(inps.errorsFoundFor(inps.LASTNAME));
        assertNull(inps.getErrorMsgsFor(inps.LASTNAME));
        msgs = inps.getErrorMsgsFor(inps.FIRSTNAME);
        assertNotNull(msgs);
        assertEquals(1, msgs.length);
        assertEquals("You have a dumb first name", msgs[0]);

        inps.addErrorMsg(inps.FIRSTNAME,"You have a last name for a first name");
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.exportErrors().getMessageCount());
        assertTrue(inps.errorsFoundFor(inps.FIRSTNAME));
        assertFalse(inps.errorsFoundFor(inps.LASTNAME));
        assertNull(inps.getErrorMsgsFor(inps.LASTNAME));
        msgs = inps.getErrorMsgsFor(inps.FIRSTNAME);
        assertNotNull(msgs);
        assertEquals(2, msgs.length);
        assertEquals("You have a dumb first name", msgs[0]);
        assertEquals("You have a last name for a first name", msgs[1]);

        inps.addErrorMsg(inps.LASTNAME, "Do you even have a last name?");
        assertTrue(inps.errorsFound());
        assertEquals(3, inps.exportErrors().getMessageCount());
        assertTrue(inps.errorsFoundFor(inps.FIRSTNAME));
        assertTrue(inps.errorsFoundFor(inps.LASTNAME));
        assertFalse(inps.errorsFoundFor(inps.USERNAME));
        assertNotNull(inps.getErrorMsgsFor(inps.LASTNAME));
        assertEquals(1, inps.getErrorMsgsFor(inps.LASTNAME).length);
        assertNotNull(inps.getErrorMsgsFor(inps.FIRSTNAME));
        assertEquals(2, inps.getErrorMsgsFor(inps.FIRSTNAME).length);

        ParamErrors errors = inps.exportErrors();
        assertNotNull(errors);
        makeInst();
        assertFalse(inps.errorsFound());
        inps.loadErrors(errors);
        assertTrue(inps.errorsFound());
        assertEquals(3, inps.exportErrors().getMessageCount());
        assertEquals(2, inps.getErrorMsgsFor(inps.FIRSTNAME).length);
    }

    @Test
    public void testFormat() {
        String args = inps.toURLArgs();
        assertEquals("", args);

        inps.setFirstName("Taylor");
        inps.setLastName("Hicks");
        args = inps.toURLArgs();
        assertEquals(inps.FIRSTNAME+"="+inps.getFirstName()+"&"+
                     inps.LASTNAME+"="+inps.getLastName(), args);
    }

    @Test
    public void testDefaultExecute() {
        try {
            inps.execute();
            fail("Default execute() failed to throw an exception");
        } catch (ServiceRequestBean.ExecuteUnsupportedException ex) {
            System.out.println("ExecuteUnsupportedException message: " + 
                               ex.getMessage());
        } catch (Exception ex) {
            fail("Unexpected exception: " + ex);
        }
    }

    public static void main(String[] args) {
        JUnitCore.runClasses(TestServiceRequestBean.class);
    }
}


