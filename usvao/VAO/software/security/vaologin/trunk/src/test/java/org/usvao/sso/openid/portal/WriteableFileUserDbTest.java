package org.usvao.sso.openid.portal;

import java.io.IOException;
import java.io.File;
import java.util.Properties;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class WriteableFileUserDbTest {

    @Test
    public void testMissingFile() throws IOException {
        String dbfile = System.getProperty("test.dbfile");
        assertNotNull("test.dbfile sys property not set", dbfile);
        File dbf = new File(dbfile);
        if (! dbf.exists()) dbf.mkdir();
        dbf = new File(dbf.getParentFile(), "goober.txt");
        assertFalse(dbf.exists());
        try {
            WriteableFileUserDb db = new WriteableFileUserDb(dbf);
            fail("loaded missing db file");
        } catch (IOException ex) {  }
    }

    @Test
    public void testLoadFile() throws IOException {
        String dbfile = System.getProperty("test.dbfile");
        assertNotNull("test.dbfile sys property not set", dbfile);
        File dbf = new File(dbfile);
        assertTrue(dbf.exists());
        WriteableFileUserDb db = new WriteableFileUserDb(dbf);
    }

    public File createTmpDb() {
        String tmpdir = System.getProperty("test.tmpdir");
        assertNotNull(tmpdir);
        File tmp = new File(tmpdir);
        assertTrue(tmp.isDirectory());
        File dbfile = new File(tmp, "testdb.txt");
        try {
            if (dbfile.exists()) dbfile.delete();
            String[] anames = { "name", "email", "shoesize" };
            ArrayList<String> attnames = new ArrayList<String>(3);
            for(String name : anames) attnames.add(name);

            SimpleFileUserDb.createDB(dbfile, attnames, null);
            assertTrue(dbfile.exists());
        }
        catch (IOException ex) {
            if (dbfile.exists()) dbfile.delete();
            fail("Failed to create tmp user db");
        }
        return dbfile;
    }

    @Test
    public void testRegisterUser() 
        throws IOException, UserDbAccessException, UnrecognizedUserException
    {
        Collection<String> auths = null;
        Map<String, ? extends Object> attr = null;

        HashSet<String> roles = new HashSet<String>(2);
        Properties atts = new Properties();

        File dbfile = createTmpDb();
        try {
            WriteableFileUserDb db = new WriteableFileUserDb(dbfile);

            assertTrue(db.registerUser("goob0", db.STATUS_DISABLED,null,null));
            assertEquals(db.STATUS_DISABLED, db.getUserStatus("goob0"));
            auths = db.getUserAuthorizations("goob0");
            assertNotNull("Failed to return a role set", auths);
            assertEquals(0, auths.size());
            attr = db.getUserAttributes("goob0");
            assertNotNull("Failed to return empty attributes", attr);
            assertEquals("", attr.get("name"));
            assertEquals("", attr.get("email"));
            assertEquals("", attr.get("shoesize"));
            assertEquals(3, attr.size());

            assertFalse(db.registerUser("goob0", db.STATUS_ACTIVE, null, null));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob0"));

            assertTrue(db.registerUser("goob1", db.STATUS_ACTIVE, roles, null));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob1"));
            auths = db.getUserAuthorizations("goob1");
            assertEquals(0, auths.size());

            roles.add("ROLE_USER");
            assertFalse(db.registerUser("goob1", db.STATUS_ACTIVE, roles,null));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob1"));
            auths = db.getUserAuthorizations("goob1");
            assertTrue(auths.contains("ROLE_USER"));
            assertEquals(1, auths.size());

            assertFalse(db.registerUser("goob0", db.STATUS_ACTIVE, roles,null));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob0"));
            auths = db.getUserAuthorizations("goob0");
            assertTrue(auths.contains("ROLE_USER"));
            assertEquals(1, auths.size());

            roles.add("ROLE_SUPERUSER");
            assertTrue(db.registerUser("goob2", db.STATUS_ACTIVE, roles, atts));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob2"));
            auths = db.getUserAuthorizations("goob2");
            assertTrue(auths.contains("ROLE_USER"));
            assertTrue(auths.contains("ROLE_SUPERUSER"));
            assertEquals(2, auths.size());
            attr = db.getUserAttributes("goob2");
            assertNotNull("Failed to return empty attributes", attr);
            assertEquals("", attr.get("name"));
            assertEquals("", attr.get("email"));
            assertEquals("", attr.get("shoesize"));
            assertEquals(3, attr.size());

            atts.setProperty("name", "Goober Pyle");
            assertFalse(db.registerUser("goob2", db.STATUS_ACTIVE,roles, atts));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob2"));
            attr = db.getUserAttributes("goob2");
            assertEquals("Goober Pyle", attr.get("name"));
            assertEquals("", attr.get("email"));
            assertEquals("", attr.get("shoesize"));
            assertEquals(3, attr.size());

            assertFalse(db.registerUser("goob1", db.STATUS_ACTIVE, roles,atts));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob1"));
            auths = db.getUserAuthorizations("goob1");
            assertTrue(auths.contains("ROLE_USER"));
            assertTrue(auths.contains("ROLE_SUPERUSER"));
            assertEquals(2, auths.size());
            assertEquals("Goober Pyle", attr.get("name"));
            assertEquals("", attr.get("email"));
            assertEquals("", attr.get("shoesize"));
            assertEquals(3, attr.size());

            atts.setProperty("email", "goober@mayberry.net");
            atts.setProperty("shoesize", "7");
            atts.setProperty("hat", "crown");
            assertTrue(db.registerUser("goob3", db.STATUS_ACTIVE, roles, atts));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob3"));
            auths = db.getUserAuthorizations("goob3");
            assertTrue(auths.contains("ROLE_USER"));
            assertTrue(auths.contains("ROLE_SUPERUSER"));
            assertEquals(2, auths.size());
            attr = db.getUserAttributes("goob3");
            assertEquals("Goober Pyle", attr.get("name"));
            assertEquals("goober@mayberry.net", attr.get("email"));
            assertEquals("7", attr.get("shoesize"));
            assertEquals(3, attr.size());

            assertFalse(db.registerUser("goob1", db.STATUS_ACTIVE, roles,atts));
            assertEquals(db.STATUS_ACTIVE, db.getUserStatus("goob3"));
            auths = db.getUserAuthorizations("goob1");
            assertTrue(auths.contains("ROLE_USER"));
            assertTrue(auths.contains("ROLE_SUPERUSER"));
            assertEquals(2, auths.size());
            attr = db.getUserAttributes("goob1");
            assertEquals("Goober Pyle", attr.get("name"));
            assertEquals("goober@mayberry.net", attr.get("email"));
            assertEquals("7", attr.get("shoesize"));
            assertEquals(3, attr.size());
        }
        finally {
            if (dbfile.exists()) dbfile.delete();
        }
    }

    public static void main(String[] args) throws Exception {
        WriteableFileUserDbTest test = new WriteableFileUserDbTest();
        test.testRegisterUser();
    }
}