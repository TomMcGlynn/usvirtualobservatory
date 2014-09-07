package org.usvao.sso.ip.db;

import org.usvao.sso.ip.User;

import java.util.Properties;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Random;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.databaseAccess.DatabaseConstants;
import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.databaseAccess.DatabaseManager;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.UserRegistrationException;
import org.globus.purse.registration.UserData;
import org.globus.purse.registration.UniqueToken;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * an implementation of the UserDatabase using Purse
 */
public class PurseUserDatabase extends UserDatabase {
    Log log = LogFactory.getLog(getClass());

    static TreeMap<Integer, User.Status> statmap = null;
    static Hashtable<User.Status, Integer> idbystat = null;

    static void loadStatMap() throws UserDatabaseAccessException {
        statmap = new TreeMap<Integer, User.Status>();
        idbystat = new Hashtable<User.Status, Integer>();
 
        Hashtable<String, User.Status> byname = 
            new Hashtable<String, User.Status>();
        byname.put("requested", User.Status.REQUESTED);
        byname.put("pending", User.Status.PENDING);
        byname.put("accepted", User.Status.ACCEPTED);
        byname.put("rejected", User.Status.REJECTED);
        byname.put("renewal", User.Status.RENEWAL);

        StringBuilder query = new StringBuilder("select * from ");
        query.append(DatabaseConstants.STATUS_TABLE_NAME);

        try {
            Connection connection = DatabaseManager.getDBConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query.toString());

            while (rs.next()) {
                User.Status stat = byname.get(rs.getString("status_name"));
                int id = rs.getInt("status_id");
                statmap.put(id, stat);
                idbystat.put(stat, id);
            }
        }
        catch (SQLException ex) {
            throw new UserDatabaseAccessException(
                "SQL error while loading status table: "+ex.getMessage(), ex);
        }
        catch (DatabaseAccessException ex) {
            throw new UserDatabaseAccessException(
                "Error loading status table: "+ex.getMessage(), ex);
        }
    }

    static User.Status statusFor(int statusid) 
        throws UserDatabaseAccessException 
    {
        if (statmap == null) loadStatMap();
        return statmap.get(statusid);
    }

    static int statusIdFor(User.Status stat) 
        throws UserDatabaseAccessException 
    {
        if (idbystat == null) loadStatMap();
        return idbystat.get(stat);
    }

    public PurseUserDatabase(Properties props) throws DatabaseConfigException {
        super(new Properties(props));

        String dbpropfile = config.getProperty("dbPropFile");

        if (dbpropfile.contains("${purse.dir}")) {
            String purseDir = config.getProperty("purse.dir");
            if (purseDir == null) purseDir = System.getProperty("purse.dir");
            if (purseDir != null) 
                dbpropfile = dbpropfile.replaceAll("\\$\\{purse.dir\\}", 
                                                   purseDir);
        }
        try {
            int hi =  Integer.parseInt(config.getProperty("hashIterations"));
            DatabaseOptions dbopts = 
                new DatabaseOptions(config.getProperty("dbDriver"), 
                                    config.getProperty("dbConnectionURL"),
                                    config.getProperty("dbUsername"), 
                                    config.getProperty("dbPassword"), 
                                    dbpropfile, hi);
            DatabaseManager.initialize(dbopts);
        }
        catch (DatabaseAccessException ex) {
            throw new DatabaseConfigException(ex.getMessage(), ex);
        }
        catch (UserRegistrationException ex) {
            throw new DatabaseConfigException(ex.getMessage(), ex);
        }
        log.info("Database connection configured");
    }

    /**
     * return true if a user with a given username exists
     */
    public boolean exists(String username) throws UserDatabaseAccessException {
        try {
            return UserDataHandler.userNameExists(username);
        }
        catch (DatabaseAccessException ex) {
            throw new UserDatabaseAccessException("Cannot determine existance "
                                                  + "for " + username + ": " 
                                                  + ex.getMessage(), ex);
        }
    }

    /**
     * return a User from the database with a given username.  Null is 
     * returned if the username does not exist in the database.
     */
    public User getUser(String username) throws UserDatabaseAccessException {
        UserData ud = null;
        try {
            ud = UserDataHandler.getDataForUsername(username);

            Properties atts = new Properties();
            setuserprop(atts,"firstName", ud.getFirstName());
            setuserprop(atts,"lastName", ud.getLastName());
            setuserprop(atts,"salt", ud.getSalt());
            setuserprop(atts,"passwordHash", ud.getPasswordSha());
            setuserprop(atts,"institution", ud.getInstitution());
            setuserprop(atts,"email", ud.getEmailAddress());
            setuserprop(atts,"phone", ud.getPhoneNumber());
            setuserprop(atts,"country", ud.getCountry());
            setuserprop(atts,"postConfirmURL", ud.getPortalConfirmUrl());
            setuserprop(atts,"initialPortal", ud.getPortalName());
            setuserprop(atts,"token", ud.getToken());
            setuserprop(atts,"passwordMethod", ud.getPasswordMethod());

            return new User(atts, username, statusFor(ud.getStatus()));
        }
        catch (DatabaseAccessException ex) {
            throw new UserDatabaseAccessException("Failed to return User "
                                                  + username + ": " 
                                                  + ex.getMessage(), ex);
        }
        
    }

    private void setuserprop(Properties p, String key, String val) {
        if (val != null) p.setProperty(key, val);
    }
        
    /**
     * return true if a user with a given username exists and has an
     * accepted status
     */
    public boolean isAccepted(String username) 
        throws UserDatabaseAccessException
    {
        try {
            UserData ud = UserDataHandler.getDataForUsername(username);
            return statusFor(ud.getStatus()) == User.Status.ACCEPTED;
        }
        catch (DatabaseAccessException ex) {
            throw new UserDatabaseAccessException("Failed to return User "
                                                  + username + ": " 
                                                  + ex.getMessage(), ex);
        }
    }

    /**
     * add a user to the database with a given username and status.
     * The username must not already be in use.
     * @param username   the user's username
     * @param userdata   the user's attributes
     * @param status     the user's initial status. 
     */
    public void addUser(String username, Properties userdata, 
                        User.Status status)
        throws UserDatabaseAccessException
    {
        if (username == null) 
            throw new IllegalArgumentException("addUser(): null username");

        if (exists(username)) {
            log.error("Attempt to re-add username: " + username);
            throw new UserDatabaseAccessException("Username " + username + 
                                                  " already exists");
        }

	try {
            UserData ud = 
                new UserData(userdata.getProperty("firstName",""),
                             userdata.getProperty("lastName",""),
                             ""/*contactPerson*/, ""/*stmtOfWork*/,
                             username, 
                             userdata.getProperty("passwordHash",""),
                             userdata.getProperty("salt",""),
                             userdata.getProperty("passwordMethod","SHA"),
                             userdata.getProperty("institution",""),
                             userdata.getProperty("clientIP",""),
                             userdata.getProperty("email",""),
                             userdata.getProperty("phone",""),
                             userdata.getProperty("country",""),
                             userdata.getProperty("postConfirmURL",""),
                             userdata.getProperty("initialPortal", ""),
                             statusIdFor(status));

            // Generate token if necessary
            String token = userdata.getProperty("token");
            if (token == null || token.equals(""))
                token = new UniqueToken(System.currentTimeMillis(), 
                                        new Random().nextLong()).toString();
            ud.setToken(token);

	    // Store details in database.	
	    UserDataHandler.storeData(ud);
	} catch (DatabaseAccessException exp) {
	    String err = "Error storing data for " + username;
	    log.error(err);
	    throw new UserDatabaseAccessException(err, exp);
	} catch (UserRegistrationException exp) {
            String err = "User data validation error for " + username +
                ": " + exp.getMessage();
            log.error(err);
            throw new UserDatabaseAccessException(err, exp);
        }
        log.info("Added new user: " + username);
    }

}