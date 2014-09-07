package org.usvao.sso.ip.service;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;
import org.usvao.sso.ip.SSOProviderSystemException;

public class TestRemindUserNameRequest {

    RemindUserNameRequest inps = null; 
    String[] emails = { "thicks@americanidol.com",                     // 0
                        "Taylor Hicks <thicks@americanidol.com>",      // 1
                        "\"T. Hicks\" <thicks@americanidol.com>",      // 2
                        "<thicks@americanidol.com>",                   // 3
                        "tricks",                                      // 4
                        "@",                                           // 5
                        "@sample.org",                                 // 6
                        "tex@",                                        // 7
                        "thicks@americanidol.com>",                    // 8
                        "<thicks@americanidol.com"                };   // 9


    @Before
    public void makeInst() {
        inps = new RemindUserNameRequest();
    }

    @Test
    public void testInitState() {
        assertEquals("", inps.getEmails());
    }

    @Test
    public void testGetSet() {
        String VAL = "goober";
        int i = 0;
        inps.setEmails(VAL);  
        assertEquals(VAL, inps.getEmails());  
    }

    @Test
    public void testValidateOK() throws SSOProviderSystemException {
        assertFalse(inps.errorsFound());

        inps.setEmails(emails[0]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(1, inps.countAddresses());
        assertEquals(emails[0], inps.getAddresses()[0]);

        inps.setEmails(emails[1]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(1, inps.countAddresses());
        assertEquals(emails[1], inps.getAddresses()[0]);

        inps.setEmails(emails[2]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(1, inps.countAddresses());
        assertEquals(emails[2], inps.getAddresses()[0]);

        inps.setEmails(emails[3]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(1, inps.countAddresses());
        assertEquals(emails[3], inps.getAddresses()[0]);

        inps.setEmails(emails[0]+" "+emails[0]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(2, inps.countAddresses());
        String[] addrs = inps.getAddresses();
        assertEquals(emails[0], addrs[0]);
        assertEquals(emails[0], addrs[1]);

        inps.setEmails(emails[2]+" "+emails[0]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(2, inps.countAddresses());
        addrs = inps.getAddresses();
        assertEquals(emails[2], addrs[0]);
        assertEquals(emails[0], addrs[1]);

        inps.setEmails(emails[1]+"\n"+emails[0]+"\n"+emails[2]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(3, inps.countAddresses());
        addrs = inps.getAddresses();
        assertEquals(emails[1], addrs[0]);
        assertEquals(emails[0], addrs[1]);
        assertEquals(emails[2], addrs[2]);

        inps.setEmails(emails[2]+" "+emails[3]+" "+emails[0]);
        assertTrue(inps.validate());
        assertFalse(inps.errorsFound());
        assertEquals(3, inps.countAddresses());
        addrs = inps.getAddresses();
        assertEquals(emails[2], addrs[0]);
        assertEquals(emails[3], addrs[1]);
        assertEquals(emails[0], addrs[2]);
    }

    @Test
    public void testValidateBad() throws SSOProviderSystemException {
        assertFalse(inps.errorsFound());

        inps.setEmails(emails[4]);
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        System.out.println(emails[4]+" parsed "+inps.countAddresses()+": "+
                           join(inps.getAddresses()));
        assertEquals(1, inps.countAddresses());
        assertEquals(0, inps.countGoodAddresses());
        assertEquals(emails[4], inps.getAddresses()[0]);

        inps.setEmails(emails[5]);
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        System.out.println(emails[5]+" parsed "+inps.countAddresses()+": "+
                           join(inps.getAddresses()));
        assertEquals(1, inps.countAddresses());
        assertEquals(0, inps.countGoodAddresses());
        assertEquals(emails[5], inps.getAddresses()[0]);

        inps.setEmails(emails[6]);
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        System.out.println(emails[6]+" parsed "+inps.countAddresses()+": "+
                           join(inps.getAddresses()));
        assertEquals(1, inps.countAddresses());
        assertEquals(0, inps.countGoodAddresses());
        assertEquals(emails[6], inps.getAddresses()[0]);

        inps.setEmails(emails[7]);
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        System.out.println(emails[7]+" parsed "+inps.countAddresses()+": "+
                           join(inps.getAddresses()));
        assertEquals(1, inps.countAddresses());
        assertEquals(0, inps.countGoodAddresses());
        assertEquals(emails[7], inps.getAddresses()[0]);

        inps.setEmails(emails[8]);
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        System.out.println(emails[8]+" parsed "+inps.countAddresses()+": "+
                           join(inps.getAddresses()));
        assertEquals(1, inps.countAddresses());
        assertEquals(0, inps.countGoodAddresses());
        assertEquals(emails[8], inps.getAddresses()[0]);

        inps.setEmails(emails[9]);
        assertFalse(inps.validate());
        assertTrue(inps.errorsFound());
        System.out.println(emails[9]+" parsed "+inps.countAddresses()+": "+
                           join(inps.getAddresses()));
        assertEquals(1, inps.countAddresses());
        assertEquals(0, inps.countGoodAddresses());
        assertEquals(emails[9], inps.getAddresses()[0]);
    }

    @Test
    public void testValidatePartial() throws SSOProviderSystemException {
        assertFalse(inps.errorsFound());

        inps.setEmails(emails[8]+" "+emails[1]);
        assertTrue(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.countAddresses());
        assertEquals(1, inps.countGoodAddresses());
        assertEquals(emails[1], inps.getGoodAddresses()[0]);
        String[] addrs = inps.getAddresses();
        assertEquals(emails[8], addrs[0]);
        assertEquals(emails[1], addrs[1]);

        inps.setEmails(emails[5]+"\n"+emails[1]);
        assertTrue(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(2, inps.countAddresses());
        assertEquals(1, inps.countGoodAddresses());
        assertEquals(emails[1], inps.getGoodAddresses()[0]);
        addrs = inps.getAddresses();
        assertEquals(emails[5], addrs[0]);
        assertEquals(emails[1], addrs[1]);

        inps.setEmails(emails[6]+"\n"+emails[1]+"\n"+emails[0]);
        assertTrue(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(3, inps.countAddresses());
        assertEquals(2, inps.countGoodAddresses());
        addrs = inps.getAddresses();
        assertEquals(emails[6], addrs[0]);
        assertEquals(emails[1], addrs[1]);
        assertEquals(emails[0], addrs[2]);

        inps.setEmails(emails[7]+"\n"+emails[8]+"\n"+emails[9]+"\n"+
        emails[0]+"\n"+emails[4]+"\n"+emails[5]+"\n"+emails[6]+"\n"+
                       emails[2]+"\n"+emails[1]+"\n"+emails[3]);
        assertTrue(inps.validate());
        assertTrue(inps.errorsFound());
        assertEquals(10, inps.countAddresses());
        assertEquals(4, inps.countGoodAddresses());
        addrs = inps.getGoodAddresses();
        assertEquals(emails[0], addrs[0]);
        assertEquals(emails[2], addrs[1]);
        assertEquals(emails[1], addrs[2]);
        assertEquals(emails[3], addrs[3]);
    }

    String join(String[] sa) {
        StringBuilder sb = new StringBuilder("[");
        for (String s : sa) 
            sb.append(s).append(' ');
        sb.deleteCharAt(sb.length()-1);
        sb.append(']');
        return sb.toString();
    }

    public static void main(String[] args) {
        // JUnitCore.runClasses(TestRemindUserNameRequest.class);
        TestRemindUserNameRequest test = new TestRemindUserNameRequest();
        try {
            test.makeInst();
            test.testValidatePartial();        
        } catch (Exception ex) {
            fail("exception: " + ex);
        }
    }
}
