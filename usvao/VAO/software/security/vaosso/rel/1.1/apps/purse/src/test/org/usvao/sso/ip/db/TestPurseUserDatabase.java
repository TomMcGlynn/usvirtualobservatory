package org.usvao.sso.ip.db;

import org.usvao.sso.ip.User;
import org.globus.purse.registration.databaseAccess.DatabaseManager;

import java.util.Properties;
import java.io.IOException;
import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestPurseUserDatabase extends PurseUserDatabaseTestBase {

    public TestPurseUserDatabase() throws IOException { super("0read"); }

    @Test
    public void testSetup() throws IOException {
        assertTrue("Test props not found", propfile.exists());
        Properties prop = loadProperties(propfile);
        assertNotNull(prop.getProperty("dbPropFile"));
        assertNotNull(prop.getProperty("dbConnectionURL"));
        assertTrue("Test config not setup for sqlite", 
                   prop.getProperty("dbConnectionURL").contains("sqlite"));
        assertTrue("Test config not pointing to test database: " +
                   prop.getProperty("dbConnectionURL"), 
                   prop.getProperty("dbConnectionURL").contains(wrkdir.toString()));
        
        assertTrue("Test DB not found", (new File(wrkdir,"test.db")).exists());

        // assertEquals(1, DatabaseManager.config.size());
        assertEquals("yyyy-MM-dd HH:mm:ss",
                     DatabaseManager.getConfigProperty("date_string_format"));

        /*
        System.out.print("config prop: ");
        for(String name : DatabaseManager.config.stringPropertyNames()) 
            System.out.println(name+": "+DatabaseManager.config.getProperty(name));
        */
    }

    @Test
    public void testExists() throws IOException, UserDatabaseAccessException {
        udb = new PurseUserDatabase(loadProperties(propfile));

        assertNotNull(udb);
        assertTrue("Failed to find existing user", udb.exists("unittest"));
        assertTrue("Failed to find existing user", udb.exists("system"));
        assertFalse("Found to non-existent user", udb.exists("bob"));
    }

    @Test
    public void testAccepted() throws IOException, UserDatabaseAccessException {
        udb = new PurseUserDatabase(loadProperties(propfile));

        assertTrue("Failed to determine status of user unittest",
                   udb.isAccepted("unittest"));
        assertFalse("Failed to determine status of user system",
                    udb.isAccepted("system"));
    }

    @Test
    public void testGetUser() throws IOException, UserDatabaseAccessException {
        udb = new PurseUserDatabase(loadProperties(propfile));

        User user = udb.getUser("unittest");
        assertEquals(user.getUsername(), "unittest");
        assertEquals(user.getStatus(), User.Status.ACCEPTED);
        assertEquals(user.getAttribute("firstName"), "Unittest");
        assertEquals(user.getAttribute("lastName"), "User");
        assertEquals(user.getAttribute("passwordMethod"), "SALTED1");
    }

    @Test
    public void testAddUser() throws IOException, UserDatabaseAccessException {
        udb = new PurseUserDatabase(loadProperties(propfile));

        Properties atts = new Properties();
        atts.setProperty("firstName", "Bob");
        atts.setProperty("lastName", "Smith");
        atts.setProperty("email", "dullard@example.org");
        atts.setProperty("passwordMethod", "INSECURE");
        atts.setProperty("passwordHash", "letmein");

        udb.addUser("dull", atts, User.Status.REJECTED);
    }

    public static void main(String[] args) {
        try {
            TestPurseUserDatabase test = new TestPurseUserDatabase();
            test.testAddUser();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("\nERROR: " + ex.getMessage());
            System.exit(1);
        }
    }
}
