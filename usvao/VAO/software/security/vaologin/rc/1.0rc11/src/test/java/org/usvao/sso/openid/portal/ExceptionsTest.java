package org.usvao.sso.openid.portal;

import java.util.Collection;
import java.util.TreeSet;
import static java.lang.String.format;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExceptionsTest {

    @Test
    public void testUnrecognizedUserException() {
        UnrecognizedUserException ex = new UnrecognizedUserException("root");
        assertEquals(ex.getUserID(), "root");
        assertNotNull(ex.getMessage());
        assertFalse("Mixed up user id and message", 
                    ex.getMessage().equals("root"));
    }

    @Test
    public void testRegistrationException() {
        RegistrationException ex = new RegistrationException("Yo", "root");
        assertEquals(ex.getUserID(), "root");
        assertEquals(ex.getMessage(), "Yo");
    }

    @Test
    public void testUnrecUserAttMsgGen() {
        TreeSet<String> atts = new TreeSet<String>();
        assertEquals("Unsupported user attribute name(s) detected",
                     UnrecognizedUserAttributeException.defaultMessage(atts));
        assertEquals("Unsupported user attribute name(s) detected",
                     UnrecognizedUserAttributeException.defaultMessage((String)null));
        assertEquals("Unsupported user attribute name(s) detected",
                     UnrecognizedUserAttributeException.defaultMessage((Collection<String>)null));
        atts.add("hair");
        atts.add("likes");
        assertEquals("Unsupported user attribute names: hair, likes",
                     UnrecognizedUserAttributeException.defaultMessage(atts));
        assertEquals("Unsupported user attribute name: sex",
                     UnrecognizedUserAttributeException.defaultMessage("sex"));
    }

    @Test
    public void testUnrecUserAtt() {
        TreeSet<String> atts = new TreeSet<String>();
        atts.add("hair");
        atts.add("likes");
        UnrecognizedUserAttributeException ex = 
            new UnrecognizedUserAttributeException(atts, "jane");
        assertEquals("jane", ex.getUserID());
        assertEquals("Unsupported user attribute names: hair, likes",
                     ex.getMessage());
        String[] list1 = {"hair", "likes"};
        assertArrayEquals(list1, ex.getUnsupportedNames());

        ex = new UnrecognizedUserAttributeException("sex", "joe");
        assertEquals("joe", ex.getUserID()); 
        assertEquals("Unsupported user attribute name: sex",
                     ex.getMessage());
        String[] list2 = {"sex"};
        assertArrayEquals(list2, ex.getUnsupportedNames());
    }

    @Test
    public void testRegOpNotSupported() {
        RegistrationOpNotSupported ex = 
            new RegistrationOpNotSupported("Stop it!", null);
        assertEquals("Stop it!", ex.getMessage());
        ex = new RegistrationOpNotSupported("sneeze");
        assertEquals("Registration operation not supported: sneeze", 
                     ex.getMessage());

        ex = RegistrationOpNotSupported.updateStatus();
        assertEquals("Registration operation not supported: updateStatus", 
                     ex.getMessage());
        ex = RegistrationOpNotSupported.updateAttributes();
        assertEquals("Registration operation not supported: updateAttributes", 
                     ex.getMessage());
        ex = RegistrationOpNotSupported.addAuthorizations();
        assertEquals("Registration operation not supported: addAuthorizations", 
                     ex.getMessage());
        ex = RegistrationOpNotSupported.removeAuthorizations();
        assertEquals("Registration operation not supported: removeAuthorizations", 
                     ex.getMessage());
    }
}