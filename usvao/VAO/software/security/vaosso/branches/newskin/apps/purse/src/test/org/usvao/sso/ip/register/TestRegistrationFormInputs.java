package org.usvao.sso.ip.register;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

public class TestRegistrationFormInputs {

    RegistrationFormInputs inps = null;

    @Before
    public void makeInst() {
        inps = new RegistrationFormInputs();
    }

    @Test
    public void testInitState() {
        assertEquals("", inps.getLastName());
        assertEquals("", inps.getFirstName());
        assertEquals("", inps.getInst());
        assertEquals("", inps.getPhone());
        assertEquals("", inps.getCountry());
        assertEquals("", inps.getEmail());
        assertEquals("", inps.getEmail2());
        assertEquals("", inps.getPortalName());
        assertEquals("", inps.getReturnURL());
        assertEquals("", inps.getUserName());
        assertEquals("", inps.getPassword1());
        assertEquals("", inps.getPassword2()); 
    }

    @Test
    public void testGetSet() {
        String VAL = "goober";
        int i = 0;
        inps.setLastName(VAL+i++);  
        inps.setFirstName(VAL+i++); 
        inps.setInst(VAL+i++);      
        inps.setPhone(VAL+i++);     
        inps.setCountry(VAL+i++);   
        inps.setEmail(VAL+i++);     
        inps.setEmail2(VAL+i++);    
        inps.setPortalName(VAL+i++);
        inps.setReturnURL(VAL+i++); 
        inps.setUserName(VAL+i++);  
        inps.setPassword1(VAL+i++); 
        inps.setPassword2(VAL+i++); 

        i=0;
        assertEquals(VAL+i++, inps.getLastName());
        assertEquals(VAL+i++, inps.getFirstName());
        assertEquals(VAL+i++, inps.getInst());
        assertEquals(VAL+i++, inps.getPhone());
        assertEquals(VAL+i++, inps.getCountry());
        assertEquals(VAL+i++, inps.getEmail());
        assertEquals(VAL+i++, inps.getEmail2());
        assertEquals(VAL+i++, inps.getPortalName());
        assertEquals(VAL+i++, inps.getReturnURL());
        assertEquals(VAL+i++, inps.getUserName());
        assertEquals(VAL+i++, inps.getPassword1());
        assertEquals(VAL+i++, inps.getPassword2()); 
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

        RegistrationFormInputs.ParamErrors errors = inps.exportErrors();
        assertNotNull(errors);
        makeInst();
        assertFalse(inps.errorsFound());
        inps.loadErrors(errors);
        assertTrue(inps.errorsFound());
        assertEquals(3, inps.exportErrors().getMessageCount());
        assertEquals(2, inps.getErrorMsgsFor(inps.FIRSTNAME).length);
    }

    @Test
    public void testValidateMissing() {
        assertFalse(inps.errorsFound());

        assertFalse(inps.validateLastName(""));
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.exportErrors().getMessageCount());
        assertEquals(1, inps.getErrorMsgsFor(inps.LASTNAME).length);

        assertFalse(inps.validateFirstName(""));
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.getErrorMsgsFor(inps.FIRSTNAME).length);
        assertEquals(2, inps.exportErrors().getMessageCount());

        // Institution is optional
        assertTrue(inps.validateInstitution(""));
        assertNull(inps.getErrorMsgsFor(inps.INSTITUTION));
        assertEquals(2, inps.exportErrors().getMessageCount());

        assertFalse(inps.validatePhone(""));
        assertEquals(1, inps.getErrorMsgsFor(inps.PHONE).length);
        assertEquals(3, inps.exportErrors().getMessageCount());

        assertFalse(inps.validateEmail("", ""));
        assertEquals(1, inps.getErrorMsgsFor(inps.EMAIL).length);
        assertEquals(1, inps.getErrorMsgsFor(inps.EMAIL2).length);
        assertEquals(5, inps.exportErrors().getMessageCount());

        assertFalse(inps.validateUserName(""));
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(6, inps.exportErrors().getMessageCount());

        assertFalse(inps.validatePassword("", ""));
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(7, inps.exportErrors().getMessageCount());
    }

    @Test
    public void testValidateOK() {
        assertFalse(inps.errorsFound());

        // last name
        assertTrue(inps.validateLastName("van der Waals"));
        assertFalse(inps.errorsFound());
        assertEquals(0, inps.exportErrors().getMessageCount());
        assertTrue(inps.validateLastName("Jones"));
        assertTrue(inps.validateLastName("Smith-Barney"));
        assertFalse(inps.errorsFound());

        // first name
        assertTrue(inps.validateFirstName("Johannes Dietrech"));
        assertFalse(inps.errorsFound());
        assertTrue(inps.validateFirstName("J. Dietrech"));
        assertTrue(inps.validateFirstName("John Ronald Raoul"));
        assertTrue(inps.validateFirstName("J.R.R."));
        assertTrue(inps.validateFirstName("J. R. R."));
        assertTrue(inps.validateFirstName("K-Y."));
        assertTrue(inps.validateFirstName("Henry"));
        assertTrue(inps.validateFirstName("K"));
        assertFalse(inps.errorsFound());

        // institution
        assertTrue(inps.validateInstitution("NCSA"));
        assertFalse(inps.errorsFound());
        assertTrue(inps.validateInstitution("National Center for Supercomputing Applications"));
        assertTrue(inps.validateInstitution("National Center for Supercomputing Applications; University of Illinois Urbana-Champaign"));
        assertTrue(inps.validateInstitution(""));
        assertFalse(inps.errorsFound());

        // phone
        assertTrue(inps.validatePhone("1-217-244-0000"));
        assertFalse(inps.errorsFound());
        assertTrue(inps.validatePhone("1.217.244.0000"));
        assertTrue(inps.validatePhone("12 21 724 400"));
        assertFalse(inps.errorsFound());

        // username
        assertTrue(inps.validateUserName("thicks"));
        assertFalse(inps.errorsFound());
        assertTrue(inps.validateUserName("Taylor.Hicks"));
        assertTrue(inps.validateUserName("Taylor.Hicks"));
        assertFalse(inps.errorsFound());
    }

    @Test
    public void testValidateEmail() {
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.EMAIL));
        assertNull(inps.getErrorMsgsFor(inps.EMAIL2));

        // A-OK
        assertTrue(inps.validateEmail("thicks@gmail.com", "thicks@gmail.com"));
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.EMAIL));
        assertNull(inps.getErrorMsgsFor(inps.EMAIL2));

        // don't match
        assertFalse(inps.validateEmail("thicks@gmail.com", "thicks@yahoo.com"));
        assertTrue(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.EMAIL));
        assertEquals(1, inps.getErrorMsgsFor(inps.EMAIL2).length);
        assertEquals(1, inps.exportErrors().getMessageCount());

        // confirm not provided
        assertFalse(inps.validateEmail("thicks@gmail.com", ""));
        assertNull(inps.getErrorMsgsFor(inps.EMAIL));
        assertEquals(2, inps.getErrorMsgsFor(inps.EMAIL2).length);
        assertEquals(2, inps.exportErrors().getMessageCount());
        
        // bad format
        assertFalse(inps.validateEmail("thicks", "thicks"));
        assertEquals(1, inps.getErrorMsgsFor(inps.EMAIL).length);
        assertEquals(2, inps.getErrorMsgsFor(inps.EMAIL2).length);
        assertEquals(3, inps.exportErrors().getMessageCount());
        
        // first not provided:  produce 2 messages
        assertFalse(inps.validateEmail("", "thicks@gmail.com"));
        assertEquals(2, inps.getErrorMsgsFor(inps.EMAIL).length);
        assertEquals(3, inps.getErrorMsgsFor(inps.EMAIL2).length);
        assertEquals(5, inps.exportErrors().getMessageCount());
        
        // neither provided:  produce 2 messages
        assertFalse(inps.validateEmail("", ""));
        assertEquals(3, inps.getErrorMsgsFor(inps.EMAIL).length);
        assertEquals(4, inps.getErrorMsgsFor(inps.EMAIL2).length);
        assertEquals(7, inps.exportErrors().getMessageCount());       
    }

    @Test
    public void testValidateUserName() {
        int c=0;
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.USERNAME));

        // A-OK
        assertTrue(inps.validateUserName("thicks"));
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.USERNAME));

        // OK special characters
        assertTrue(inps.validateUserName("t.Hi-cks_45"));
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.USERNAME));

        // not provided
        assertFalse(inps.validateUserName(""));
        assertTrue(inps.errorsFound());
        assertNotNull(inps.getErrorMsgsFor(inps.USERNAME));
        assertEquals(++c, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(c, inps.exportErrors().getMessageCount());

        // too short
        assertFalse(inps.validateUserName("b"));
        assertEquals(++c, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(c, inps.exportErrors().getMessageCount());

        // various bad characters
        assertFalse(inps.validateUserName("t+hicks"));
        assertEquals(++c, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(c, inps.exportErrors().getMessageCount());
        assertFalse(inps.validateUserName("t?hicks"));
        assertFalse(inps.validateUserName("t!hicks"));
        assertFalse(inps.validateUserName("t$hicks"));
        assertFalse(inps.validateUserName("t@hicks"));
        assertFalse(inps.validateUserName("t&hicks"));
        assertFalse(inps.validateUserName("t hicks"));
        c += 6;
        assertEquals(c, inps.getErrorMsgsFor(inps.USERNAME).length);
        assertEquals(c, inps.exportErrors().getMessageCount());
    }

    @Test
    public void testValidatePassword() {
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD1));
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD2));

        // A-OK
        assertTrue(inps.validatePassword("amId0lized", "amId0lized"));
        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD1));
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD2));

        // missing
        assertFalse(inps.validatePassword("", ""));
        assertTrue(inps.errorsFound());
        assertNotNull(inps.getErrorMsgsFor(inps.PASSWORD1));
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD2));
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(1, inps.exportErrors().getMessageCount());

        // confirm missing
        assertFalse(inps.validatePassword("amId0lized", ""));
        assertNotNull(inps.getErrorMsgsFor(inps.PASSWORD2));
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD2).length);
        assertEquals(2, inps.exportErrors().getMessageCount());

        // first missing
        assertFalse(inps.validatePassword("", "amId0lized"));
        assertEquals(2, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD2).length);
        assertEquals(3, inps.exportErrors().getMessageCount());

        // do not match
        assertFalse(inps.validatePassword("amId8lized", "amId0lized"));
        assertEquals(3, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD2).length);
        assertEquals(4, inps.exportErrors().getMessageCount());
    }

    @Test
    public void testCheckPassword() {
        inps.setUserName("mridol");
        inps.setLastName("Hicks");
        inps.setFirstName("Taylor");
        inps.setEmail("idol@gmail.com");

        assertFalse(inps.errorsFound());
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD1));
        assertNull(inps.getErrorMsgsFor(inps.PASSWORD2));

        // A-OK
        assertTrue(inps.checkPassword("amId8lized"));
        assertFalse(inps.errorsFound());
        assertTrue(inps.checkPassword("I am idolized"));
        assertFalse(inps.errorsFound());

        // Too short
        assertFalse(inps.checkPassword("amId8"));
        assertTrue(inps.errorsFound());
        assertEquals(1, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(1, inps.exportErrors().getMessageCount());

        // is banned word
        assertFalse(inps.checkPassword("vaoss0"));
        assertEquals(2, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        assertEquals(2, inps.exportErrors().getMessageCount());

        // too close to username
        assertFalse(inps.checkPassword("mridol2008"));
        assertEquals(3, inps.getErrorMsgsFor(inps.PASSWORD1).length);

        // too close to last name
        assertFalse(inps.checkPassword("THicks"));
        assertEquals(4, inps.getErrorMsgsFor(inps.PASSWORD1).length);

        // too close to first name
        assertFalse(inps.checkPassword("taylor"));
        assertEquals(5, inps.getErrorMsgsFor(inps.PASSWORD1).length);

        // too close to institution
        assertTrue(inps.checkPassword("american")); // OK, inst not set yet
        assertEquals(5, inps.getErrorMsgsFor(inps.PASSWORD1).length);
        inps.setInst("American Idol");
        assertFalse(inps.checkPassword("best american idol"));
        assertEquals(6, inps.getErrorMsgsFor(inps.PASSWORD1).length);

        // too close to email address
        assertFalse(inps.checkPassword("idol@gmail.com"));
        assertEquals(7, inps.getErrorMsgsFor(inps.PASSWORD1).length);

    }

    public static void main(String[] args) {
        JUnitCore.runClasses(TestRegistrationFormInputs.class);
    }
}