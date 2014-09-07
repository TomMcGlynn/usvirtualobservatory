package org.usvao.sso.ip.register;

import org.usvao.sso.ip.User;
import org.usvao.sso.ip.SSOProviderSystemException;
import org.usvao.sso.ip.db.PurseUserDatabase;
import org.usvao.sso.ip.db.PurseUserDatabaseTestBase;
import org.usvao.sso.ip.db.UserDatabaseAccessException;
import org.usvao.sso.ip.db.DatabaseConfigException;

import org.globus.purse.registration.databaseAccess.DatabaseManager;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestLoadUsersFromCSVApp extends PurseUserDatabaseTestBase {

    public final File testcsv = new File(new File(srcdir), "testnewusers.csv");
    public final File testcsv2 = new File(new File(srcdir),"testnewusers2.csv");

    public TestLoadUsersFromCSVApp() throws IOException { super("1load"); }

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
    public void testCtor() throws IOException, DatabaseConfigException {
        LoadUsersFromCSVApp app = new LoadUsersFromCSVApp(propfile);

        assertFalse(app.passwordIsHashed());
        app.assumePasswordHashed(true);
        assertTrue(app.passwordIsHashed());
    }

    @Test
    public void testLoadUsers() throws IOException, SSOProviderSystemException {
        Properties props = new Properties();
        props.load(new FileInputStream(propfile));

        LoadUsersFromCSVApp app = new LoadUsersFromCSVApp(props);
        PurseUserDatabase udb = new PurseUserDatabase(props);

        assertTrue(testcsv.exists());
        int count = app.loadusers(testcsv, User.Status.PENDING, 2);
        assertEquals(2, count);

        assertTrue(udb.exists("newuser1"));
        User user = udb.getUser("newuser1");
        assertEquals(User.Status.PENDING, user.getStatus());
        assertEquals("SALTED1", user.getAttribute("passwordMethod"));
        assertEquals("US", user.getAttribute("country"));
        assertEquals("Ron", user.getAttribute("firstName"));
        assertEquals("Burghandy", user.getAttribute("lastName"));
        assertTrue("Password check fails on newuser1", 
                   user.passwordMatches("newsteam"));
        
        assertTrue(udb.exists("newuser2"));
        user = udb.getUser("newuser2");
        assertEquals(User.Status.PENDING, user.getStatus());
        assertEquals("SALTED1", user.getAttribute("passwordMethod"));
        assertEquals("US", user.getAttribute("country"));
        assertEquals("Gurn", user.getAttribute("firstName"));
        assertEquals("Cranston", user.getAttribute("lastName"));
        assertTrue("Password check fails on newuser2", 
                   user.passwordMatches("goober"));

        assertFalse(udb.exists("smiles"));        
    }

    @Test
    public void testMain() throws IOException, SSOProviderSystemException {
        final String[] args = new String[] { propfile.toString(), 
                                             "accepted", testcsv2.toString() };
        LoadUsersFromCSVApp.main(args);

        Properties props = new Properties();
        props.load(new FileInputStream(propfile));
        PurseUserDatabase udb = new PurseUserDatabase(props);

        assertTrue(udb.exists("newuser3"));
        User user = udb.getUser("newuser3");
        assertEquals(User.Status.ACCEPTED, user.getStatus());
        assertEquals("SALTED1", user.getAttribute("passwordMethod"));
        assertEquals("US", user.getAttribute("country"));
        assertEquals("Ron", user.getAttribute("firstName"));
        assertEquals("Burghandy", user.getAttribute("lastName"));
        assertTrue("Password check fails on newuser1", 
                   user.passwordMatches("newsteam"));
        
        assertTrue(udb.exists("newuser4"));
        user = udb.getUser("newuser4");
        assertEquals(User.Status.ACCEPTED, user.getStatus());
        assertEquals("SALTED1", user.getAttribute("passwordMethod"));
        assertEquals("US", user.getAttribute("country"));
        assertEquals("Gurn", user.getAttribute("firstName"));
        assertEquals("Cranston", user.getAttribute("lastName"));
        assertTrue("Password check fails on newuser2", 
                   user.passwordMatches("goober"));

        assertTrue(udb.exists("george"));
    }

    public static void main(String[] args) {
        try {
            TestLoadUsersFromCSVApp test = new TestLoadUsersFromCSVApp();
            test.testLoadUsers();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("\nERROR: " + ex.getMessage());
            System.exit(1);
        }
    }
}
