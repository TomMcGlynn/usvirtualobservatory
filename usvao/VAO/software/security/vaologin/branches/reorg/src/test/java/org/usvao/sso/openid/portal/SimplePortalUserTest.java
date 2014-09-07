package org.usvao.sso.openid.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import static java.lang.String.format;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimplePortalUserTest {

    VAOLogin login = null;
    PortalUser user = null;
    String oid = USVAOConventions.OPENID_BASE_URL + "joe";
    String foreign = "http://foreign.org/openid/joe";

    @Before
    public void setup() {
        login = new ProtoVAOLogin(oid, null, null, true, false);
        user = new SimplePortalUser(login);
    }

    @Test
    public void testLoginData() {
        assertEquals(login, user.getLoginInfo());
        assertEquals("joe@usvao", user.getID());
        assertFalse(user.isRegistered());
        assertTrue(user.isSessionValid());

        login = new ProtoVAOLogin(true, foreign);
        user = new SimplePortalUser(login);
        assertEquals(foreign, user.getID());
    }

    @Test
    public void testAuthz() {
        Set<String> authz = user.getAuthorizations();
        assertEquals(2, authz.size());
        assertTrue(authz.contains(PortalUser.ROLE_OPENID_USER));
        assertTrue(authz.contains(PortalUser.ROLE_VAO_USER));

        login = new ProtoVAOLogin(true, foreign);
        user = new SimplePortalUser(login);
        authz = user.getAuthorizations();
        assertTrue(authz.contains(PortalUser.ROLE_OPENID_USER));
        assertFalse(authz.contains(PortalUser.ROLE_VAO_USER));
    }

    @Test
    public void testSession() {
        assertTrue(user.isSessionValid());
        user.endSession();
        assertFalse(user.isSessionValid());
    }

    @Test
    public void testAttributes() {
        assertEquals("Cool", user.getAttribute("lastname").toString());
        assertNull(user.getAttribute("visitCount"));

        assertEquals("joe@gmail.com", user.getProperty("email"));
        assertEquals("Cool", user.getProperty("lastname"));

        user.setAttribute("visitCount", 3);
        assertEquals(3, ((Integer) user.getAttribute("visitCount")).intValue());
        assertNull(user.getProperty("visitCount"));
        user.setAttribute("firstname", "Coolish");
        assertEquals("Cool", user.getProperty("lastname"));
    }
}