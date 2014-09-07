package org.usvao.sso.openid.portal;

import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleFileUserDbTest {

    @Test
    public void testDBNotSet() {
        SimpleFileUserDb db = new SimpleFileUserDb();
        try {  
            db.checkDbFileSet();
            fail("checkDbFile: failed to detect lack of DB");
        }
        catch (IllegalStateException ex) { }
        try {  
            db.checkReadable();
            fail("checkReadable: failed to detect lack of DB");
        }
        catch (IOException ex) { 
            fail("findUser: failed to detect lack of DB");
        }
        catch (IllegalStateException ex) { }
        try {  
            db.findUser("goober");
            fail("findUser: failed to detect lack of DB");
        }
        catch (IOException ex) { 
            fail("findUser: failed to detect lack of DB");
        }
        catch (IllegalStateException ex) { }
    }

    @Test
    public void testNotReadable() {
        try {  
            SimpleFileUserDb db = new SimpleFileUserDb("goob.txt");
            fail("ctor: failed to detect lack of DB");
        }
        catch (IllegalStateException ex) {
            fail("ctor: failed to detect setting of DB");
        }
        catch (IOException ex) {  }
    }

    @Test
    public void testLoadFile() throws IOException {
        String dbfile = System.getProperty("test.dbfile");
        assertNotNull("test.dbfile sys property not set", dbfile);

        SimpleFileUserDb db = new SimpleFileUserDb();
        db.setDbFile(dbfile);
        try {
            db.checkReadable();
        }
        catch (IOException ex) {
            fail("Failed to load user db file");
        }
        catch (IllegalStateException ex) {
            fail("Failed to load user db file");
        }

        db = new SimpleFileUserDb(dbfile);
        try {
            db.checkReadable();
        }
        catch (IOException ex) {
            fail("Failed to load user db file");
        }
        catch (IllegalStateException ex) {
            fail("Failed to load user db file");
        }
    }

    @Test
    public void testLoadResource() throws IOException {
        SimpleFileUserDb db = new SimpleFileUserDb();
        db.setDbFile("testUserDb.txt");
        try {
            db.checkReadable();
        }
        catch (IOException ex) {
            fail("Failed to load user db resource");
        }
        catch (IllegalStateException ex) {
            fail("Failed to load user db resource");
        }

        db = new SimpleFileUserDb("testUserDb.txt");
        try {
            db.checkReadable();
        }
        catch (IOException ex) {
            fail("Failed to load user db resource");
        }
        catch (IllegalStateException ex) {
            fail("Failed to load user db resource");
        }
    }

    @Test
    public void testFindUser() throws IOException {
        SimpleFileUserDb db = new SimpleFileUserDb("testUserDb.txt");

        String name = "gurn";
        SimpleFileUserDb.User user = db.findUser(name);
        assertNull("found non-existant user: "+name, user);

        name = "pres";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_ACTIVE, user.getStatus());
        Set<String> roles = user.getAuthorizations();
        assertTrue("Missing role", roles.contains("ROLE_PRES"));
        assertTrue("Missing role", roles.contains("ROLE_CIT"));
        assertEquals(2, roles.size());
        String[] vals = user.getAttributeList();
        assertEquals(3, vals.length);
        Map<String, String> atts = user.getAttributes();
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("Robert F. Kennedy", atts.get("fullname"));
        assertEquals("pres@whitehouse.gov", atts.get("email"));
        assertEquals("red", atts.get("color"));
        assertEquals(3, atts.size());

        name = "jack";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_ACTIVE, user.getStatus());
        roles = user.getAuthorizations();
        assertTrue("Missing role", roles.contains("ROLE_CIT"));
        assertEquals(1, roles.size());
        vals = user.getAttributeList();
        assertEquals(2, vals.length);
        atts = user.getAttributes();
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("Jack Kennedy", atts.get("fullname"));
        assertEquals("rfk@gmail.com", atts.get("email"));
        assertEquals("", atts.get("color"));
        assertEquals(3, atts.size());

        name = "rfk";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_DISABLED, user.getStatus());
        roles = user.getAuthorizations();
        assertEquals(0, roles.size());
        vals = user.getAttributeList();
        assertEquals(0, vals.length);
        atts = user.getAttributes();
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("", atts.get("fullname"));
        assertEquals("", atts.get("email"));
        assertEquals("", atts.get("color"));
        assertEquals(3, atts.size());

        name = "goob";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_ACTIVE, user.getStatus());
        roles = user.getAuthorizations();
        assertEquals(0, roles.size());
        vals = user.getAttributeList();
        assertEquals(0, vals.length);
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("", atts.get("fullname"));
        assertEquals("", atts.get("email"));
        assertEquals("", atts.get("color"));
        assertEquals(3, atts.size());

        name = "goob0";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_ACTIVE, user.getStatus());
        roles = user.getAuthorizations();
        assertTrue("Missing role", roles.contains("ROLE_ME"));
        assertEquals(1, roles.size());
        vals = user.getAttributeList();
        assertEquals(0, vals.length);
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("", atts.get("fullname"));
        assertEquals("", atts.get("email"));
        assertEquals("", atts.get("color"));
        assertEquals(3, atts.size());

        name = "goob1";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_ACTIVE, user.getStatus());
        roles = user.getAuthorizations();
        assertTrue("Missing role", roles.contains("ROLE_ME"));
        assertEquals(1, roles.size());
        vals = user.getAttributeList();
        assertEquals(0, vals.length);
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("", atts.get("fullname"));
        assertEquals("", atts.get("email"));
        assertEquals("", atts.get("color"));
        assertEquals(3, atts.size());

        name = "goob2";
        user = db.findUser(name);
        assertNotNull("Failed to find user: "+name, user);
        assertEquals(name, user.getName());
        assertEquals(db.STATUS_ACTIVE, user.getStatus());
        roles = user.getAuthorizations();
        assertEquals(0, roles.size());
        vals = user.getAttributeList();
        assertEquals(0, vals.length);
        assertNotNull("Failed to return attribute lookup", atts);
        assertEquals("", atts.get("fullname"));
        assertEquals("", atts.get("email"));
        assertEquals("", atts.get("color"));
        assertEquals(3, atts.size());

        name = "goob3";
        user = db.findUser(name);
        assertNull("found deleted user: "+name, user);
    }

    @Test
    public void testGetAttributeNames() 
        throws IOException, UserDbAccessException 
    {
        SimpleFileUserDb db = new SimpleFileUserDb("testUserDb.txt");

        String[] atts = db.getAttributeNames();
        assertNotNull("Failed to return attribute names", atts);
        assertEquals(3, atts.length);
        assertEquals("fullname", atts[0]);
        assertEquals("email", atts[1]);
        assertEquals("color", atts[2]);
    }

    @Test
    public void testGetUserStatus() throws IOException, UserDbAccessException {
        SimpleFileUserDb db = new SimpleFileUserDb("testUserDb.txt");

        assertEquals(db.STATUS_ACTIVE, db.getUserStatus("pres"));
        assertEquals(db.STATUS_ACTIVE, db.getUserStatus("jack"));
        assertEquals(db.STATUS_DISABLED, db.getUserStatus("rfk"));
        assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob"));
        assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob0"));
        assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob1"));
        assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob2"));
        assertEquals(db.STATUS_UNRECOGNIZED, db.getUserStatus("goob3"));
    }

    @Test
    public void testGetUserAttributes() 
        throws IOException, UserDbAccessException 
    {
        SimpleFileUserDb db = new SimpleFileUserDb("testUserDb.txt");

        Map<String, ? extends Object> atts = null;

        try {
            atts = db.getUserAttributes("pres");
            assertNotNull("Failed to return attribute lookup", atts);
            assertEquals("Robert F. Kennedy", atts.get("fullname"));
            assertEquals("pres@whitehouse.gov", atts.get("email"));
            assertEquals("red", atts.get("color"));
            assertEquals(3, atts.size());

            atts = db.getUserAttributes("jack");
            assertNotNull("Failed to return attribute lookup", atts);
            assertEquals("Jack Kennedy", atts.get("fullname"));
            assertEquals("rfk@gmail.com", atts.get("email"));
            assertEquals("", atts.get("color"));
            assertEquals(3, atts.size());

            atts = db.getUserAttributes("rfk");
            assertNotNull("Failed to return attribute lookup", atts);
            assertEquals("", atts.get("fullname"));
            assertEquals("", atts.get("email"));
            assertEquals("", atts.get("color"));
            assertEquals(3, atts.size());
        }
        catch (UnrecognizedUserException ex) {
            fail("failed to find user: " + ex.getUserID());
        }

        try {
            atts = db.getUserAttributes("goob3");
            fail("Found deleted user, goob3");
        }
        catch (UnrecognizedUserException ex) { }
        try {
            atts = db.getUserAttributes("gurn");
            fail("Found non-existent user, goob3");
        }
        catch (UnrecognizedUserException ex) { }
        
    }

    @Test
    public void testGetUserAuthz() 
        throws IOException, UserDbAccessException 
    {
        SimpleFileUserDb db = new SimpleFileUserDb("testUserDb.txt");

        Collection<String> roles = null;
        try {
            roles = db.getUserAuthorizations("pres");
            assertTrue("Missing role", roles.contains("ROLE_PRES"));
            assertTrue("Missing role", roles.contains("ROLE_CIT"));
            assertEquals(2, roles.size());

            roles = db.getUserAuthorizations("jack");
            assertTrue("Missing role", roles.contains("ROLE_CIT"));
            assertEquals(1, roles.size());

            roles = db.getUserAuthorizations("rfk");
            assertEquals(0, roles.size());
        }
        catch (UnrecognizedUserException ex) {
            fail("failed to find user: " + ex.getUserID());
        }

        try {
            roles = db.getUserAuthorizations("goob3");
            fail("Found deleted user, goob3");
        }
        catch (UnrecognizedUserException ex) { }
        try {
            roles = db.getUserAuthorizations("gurn");
            fail("Found non-existent user, goob3");
        }
        catch (UnrecognizedUserException ex) { }
    }

    @Test
    public void testCreateDBOverwrite() {
        String dbfile = System.getProperty("test.dbfile");
        String tmpdir = System.getProperty("test.tmpdir");
        File tmp = new File(tmpdir);
        if (! tmp.exists()) tmp.mkdir();
        assertTrue("Can't find tmp dir: "+tmp, tmp.isDirectory());

        File dbf = new File(dbfile);
        dbf = new File(tmp, dbf.getName() + "2");
        try {  copyFile(dbfile, dbf.getAbsolutePath()); }
        catch (IOException ex) { fail("Failed to copy db file"); }

        assertTrue(dbf.exists());
        try {
            SimpleFileUserDb.createDB(dbf, null, null);
            fail("Overwrote existing db file");
        }
        catch (IOException ex) {
            assertTrue("wrong error: " + ex.getMessage(),
                       ex.getMessage().contains("already exists"));
        }
        finally {
            if (dbf.exists()) dbf.delete();
        }
    }

    @Test
    public void testCreateDB() {
        String tmpdir = System.getProperty("test.tmpdir");
        File tmp = new File(tmpdir);
        File dbfile = new File(tmp, "tstdb.txt");
        try {
            try {
                if (dbfile.exists()) dbfile.delete();
                SimpleFileUserDb.createDB(dbfile, null, null);
            }
            catch (IOException ex) {
                fail("Failed to create db file");
            }
            try {
                SimpleFileUserDb db = new SimpleFileUserDb(dbfile);
            }
            catch (IOException ex) {
                fail("Failed to read created db file");
            }
        }
        finally {
            if (dbfile != null && dbfile.exists()) dbfile.delete();
        }
    }

    @Test
    public void testCreateDB2() {
        String tmpdir = System.getProperty("test.tmpdir");
        File tmp = new File(tmpdir);
        File dbfile = new File(tmp, "tstdb.txt");
        try {
            try {
                if (dbfile.exists()) dbfile.delete();
                ArrayList<String> names = new ArrayList<String>(3);
                String[] n = { "name", "email", "shoesize" };
                for (String name : n)
                    names.add(name);
                SimpleFileUserDb.createDB(dbfile, names, null);
            }
            catch (IOException ex) {
                fail("Failed to create db file");
            }
            try {
                SimpleFileUserDb db = new SimpleFileUserDb(dbfile);
            }
            catch (IOException ex) {
                fail("Failed to read created db file");
            }
        }
        finally {
            if (dbfile != null && dbfile.exists()) dbfile.delete();
        }
    }


    @Test
    public void testCreateDB3() {
        String tmpdir = System.getProperty("test.tmpdir");
        File tmp = new File(tmpdir);
        File dbfile = new File(tmp, "tstdb.txt");
        try {
            try {
                if (dbfile.exists()) dbfile.delete();
                ArrayList<String> names = new ArrayList<String>(3);
                String[] n = { "name", "email", "shoesize" };
                for (String name : n)
                    names.add(name);
                HashSet<String> authz = new HashSet<String>(2);
                for (String role : authz)
                    authz.add(role);
                SimpleFileUserDb.createDB(dbfile, names, authz);
            }
            catch (IOException ex) {
                fail("Failed to create db file");
            }
            try {
                SimpleFileUserDb db = new SimpleFileUserDb(dbfile);
            }
            catch (IOException ex) {
                fail("Failed to read created db file");
            }
        }
        finally {
            if (dbfile != null && dbfile.exists()) dbfile.delete();
        }
    }

    public static void copyFile(String from, String to) throws IOException {
        int n = 0;
        char[] buf = new char[4096];
        FileWriter wtr = null;
        FileReader rdr = null;
        try {
            wtr = new FileWriter(to);
            rdr = new FileReader(from);
            while ((n = rdr.read(buf)) >= 0) {
                if (n > 0)
                    wtr.write(buf, 0, n);
            }
        }
        finally {
            if (wtr != null) wtr.close();
            if (rdr != null) rdr.close();
        }
    }

    public static void main(String[] args) throws IOException {
        SimpleFileUserDbTest test = new SimpleFileUserDbTest();
        test.testFindUser();
    }
}

