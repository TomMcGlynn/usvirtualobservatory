package org.usvao.sso.ip;

import java.util.Properties;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestUser {

    public User makeDullUser() {
        Properties props = new Properties();
        props.setProperty("firstName", "Bob");
        return new User(props, "dull", User.Status.PENDING);
    }

    @Test
    public void testCtor() {
        User user = makeDullUser();
        assertNotNull(user);
        assertEquals(user.getStatus(), User.Status.PENDING);
    }

    @Test
    public void testUnknownUser() {
        User user = new User(new Properties(), "dull");
        assertEquals(user.getStatus(), User.Status.UNKNOWN);
    }

    @Test
    public void testUsername() {
        User user = makeDullUser();
        assertEquals(user.getUsername(), "dull");
    }

    public void testAttribute() {
        User user = makeDullUser();
        assertEquals(user.getAttribute("firstName"), "Bob");
        assertNull(user.getAttribute("lastName"));
        assertEquals(user.getAttribute("lastName", "Bond"), "Bond");
    }

    public void testLoginCount() {
        User user = makeDullUser();
        assertEquals(user.getLoginCount(), -1);
    }

    public void testHasRole() {
        User user = makeDullUser();
        assertFalse(user.hasRole("admin"));
    }

    public void testHasLabel() {
        User user = makeDullUser();
        assertFalse(user.hasLabel("interesting"));
    }
}