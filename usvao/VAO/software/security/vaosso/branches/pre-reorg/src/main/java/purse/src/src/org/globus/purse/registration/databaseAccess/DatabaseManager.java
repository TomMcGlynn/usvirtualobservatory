/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.UserRegistrationException;
import org.globus.purse.registration.RoleData;
import org.globus.purse.registration.StatusData;

// Classes for database pooling
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle database access.
 */
public class DatabaseManager {
    
    static Log logger =
	LogFactory.getLog(DatabaseManager.class.getName());
 
    private static boolean initialized = false;
    private static String driverName = null;
    public static String connectionURL = null;
    public static String userName = null;
    public static String passPhrase = null;

    // Track number of connections
    static int connNumber;
    // conenction object pool
    static GenericObjectPool connectionPool = null;
    final public static String baseURI = "jdbc:apache:commons:dbcp:";
    final public static String purseURI = "purse";

    static String singleQuote = "'";
    static String escapedSingleQuote = "\\\'";
    static String doubleQuote = "\"";
    static String escapedDoubleQuote = "\\\"";
    static String backslash = "\\";
    static String escapedBackslash = "\\\\";

    /**
     * This method initializes the databse properties this class uses.
     * This need to be called prior to using any other methods in this class.
     *
     * @param <code>DatabaseOptions</code>
     *        Object to initialize database properties
     * @exception <code>DatabaseAccessException</code>
     *         If any error occurs in initializig the database/pool
     */
    public static void initialize(DatabaseOptions dbOptions) 
	throws DatabaseAccessException, UserRegistrationException {

	if (!initialized) {
	    driverName = dbOptions.getDriver();
	    connectionURL = dbOptions.getConnectionURL();
	    userName = dbOptions.getUserName();
	    try {
		setupDriver(connectionURL, userName,
			    dbOptions.getPassword (), 
			    dbOptions.getActiveConnections(), 
			    dbOptions.getOnExhaustAction(), 
			    dbOptions.getMaxWait(), 
			    dbOptions.getIdleConnections());
	    }
	    catch(Exception e) {
		String err = "Unable to setup driver with pooling ";
		logger.error(err, e);
		throw new DatabaseAccessException(connectionURL, userName, 
						  err + e.getMessage(), e);
	    }
	    
	    passPhrase = dbOptions.getPassphrase();
	    initialized = true;
	}
    } 

    private static void setupDriver(String connectURI, String username, 
				    String password, int activeConnections, 
				    byte onExhaustAction, long maxWait, 
				    int idleConnections) 
	throws Exception {

        // Object pool which is a pool of conection
	connectionPool = new GenericObjectPool(null, activeConnections, 
					       onExhaustAction, maxWait, 
					       idleConnections, true, false);
	// ConnectionFactory that pool uses to create connectiosn
        ConnectionFactory connectionFactory = 
	    new DriverManagerConnectionFactory(connectURI,username,password);

        // PoolableConnectionFactory used for pooling functionality
        PoolableConnectionFactory poolableConnectionFactory = 
	    new PoolableConnectionFactory(connectionFactory,
					  (ObjectPool)connectionPool,
					  null,null,false,true);

	// Create and Register PoolingDriver
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(purseURI, connectionPool);
    }

    public static Connection getDBConnection() throws DatabaseAccessException {
	
	if (!initialized) {
	    String err = "Initialize should be called before method call";
	    logger.error(err);
	    throw new DatabaseAccessException(connectionURL, userName, err);
	}
	try {
            Class.forName(driverName);
	    Connection connection  = 
		DriverManager.getConnection(baseURI + purseURI);
	    connNumber++;
	    if (logger.isDebugEnabled()) {
		logger.debug("Added " + connNumber + " active " 
			    + connectionPool.getNumActive() 
			    + " idle " + connectionPool.getNumIdle());
	    }
	    return connection;
        }
	catch(SQLException e) {
            logger.error("Unable to connect to database ", e);
            throw new DatabaseAccessException(connectionURL, userName, 
					      "Unable to connect to database. "
					      + e.getMessage(), e);
        }
	catch(ClassNotFoundException e) {
	    String err = "Unable to connect to database. Driver class not "
		+ " found.";
            logger.error(err, e);
            throw new DatabaseAccessException(connectionURL, userName, 
					      err + e.getMessage(), e);
	}	

    }

    public static void returnDBConnection(Connection connection) 
	throws DatabaseAccessException {

	try {
	    connection.close();
	    connNumber--;
	    if (logger.isDebugEnabled()) {
		logger.debug("Reduced " + connNumber + " active " 
			    + connectionPool.getNumActive() 
			    + " idle " + connectionPool.getNumIdle());
	    }
	}
	catch (SQLException sqlExcep) {
	    logger.error("Cannot return database connection to pool", 
                         sqlExcep);
	    throw new DatabaseAccessException(connectionURL, userName,
					      "Error returning db connection "
					      + " to pool. " 
					      + sqlExcep.getMessage(), 
					      sqlExcep);
	}
    }
    
    public static void runUpdateQuery(String query) 
	throws DatabaseAccessException {

	logger.debug("Query is " + query);
	Connection connection = getDBConnection();
	Statement statement = null;
	try {
	    statement = connection.createStatement();
	    statement.executeUpdate(query);
	}
	catch (SQLException exp) {
	    logger.error("Error executing stmt: " + query, exp);
	    throw new DatabaseAccessException(connectionURL, userName, 
					      "Error updating database. " 
					      + exp.getMessage(), exp);
	}
	finally {
	    // retuning connection irrespective of whether stmt
	    // and result set are closed or not.
	    returnDBConnection(connection);
	    try {
		if (statement != null) 
		    statement.close();
	    } catch (SQLException exp) {
		String err = "Error updating database. Could not close SQL"
		    + " statement.";
		logger.warn(err, exp);
	    }
	}
    }

    public static String getPassphrase() {
	return passPhrase;
    }

    public static String escapeSingleQuote(String str) {

	logger.debug("escapeSingleQuote called with " + str);
	int index;
	int indexFrom = 0;
	if (str!=null) {
	    StringBuffer strBuf = new StringBuffer(str);
	    while ((index = str.indexOf(singleQuote, indexFrom)) != -1) {
		strBuf = strBuf.replace(index,index+1,escapedSingleQuote);
		str = strBuf.toString();
		indexFrom = index+2;
	    }
	}
	
	logger.debug("string is " + str);
	return str;
    }

    public static String restoreSingleQuote(String str) {
	logger.debug("restoreSingleQuote called with " + str);
	int index;
	if (str!=null) {
	    StringBuffer strBuf = new StringBuffer(str);
	    while ((index = str.indexOf(escapedSingleQuote)) != -1) {
		strBuf.delete(index, index+1);
		str = strBuf.toString();
	    }
	    logger.debug("string is " + str);
	}
	return str;
    }

    public static String escapeDoubleQuote(String str) {
	logger.debug("escapeDoubleQuote called with " + str);
	int index;
	int indexFrom = 0;
	if (str!=null) {
	    StringBuffer strBuf = new StringBuffer(str);
	    while ((index = str.indexOf(doubleQuote, indexFrom)) != -1) {
		strBuf = strBuf.replace(index,index+1,escapedDoubleQuote);
		str = strBuf.toString();
		indexFrom = index+2;
		logger.debug("string is " + str + "indexFrom is " + indexFrom);
	    }
	}
	return str;
    }

    public static String restoreDoubleQuote(String str) {
	logger.debug("restoreDoubleQuote called with " + str);
	int index;
	if (str!=null) {
	    StringBuffer strBuf = new StringBuffer(str);
	    while ((index = str.indexOf(escapedDoubleQuote)) != -1) {
		strBuf.delete(index, index+1);
		str = strBuf.toString();
		logger.debug("string is " + str);
	    }
	}
	return str;
    }

    public static String escapeBackslash(String str) {
	logger.debug("escape backslash called with " + str);
	int index;
	int indexFrom = 0;
	if (str!=null) {
	    StringBuffer strBuf = new StringBuffer(str);
	    while ((index = str.indexOf(backslash, indexFrom)) != -1) {
		strBuf = strBuf.replace(index,index+1, escapedBackslash);
		str = strBuf.toString();
		indexFrom = index+2;
		logger.debug("string is " + str + "indexFrom is " + indexFrom);
	    }
	}
	return str;
    }

    /** Characters allowed in SQL strings -- note, no semicolons or quotes.  Apostrophes either, which
     *  could be bad, but I'm erring on the side of safety here.  In practice, they will disappear -- Company
     *  name: "Argonne's Pies" will become "Argonnes Pies", which may be annoying but won't get in
     *  anybody's way.  And if you try to log on with a traditional Klingon name like "G'kroh", it will just
     *  get filtered to "Gkroh" every time, and you should be able to log on anyway. */
    public static final String ALLOWED_CHARS
            = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-_=+ !@#$&(),.<>?/:";
    private static final boolean[] ALLOWED;
    static {
        ALLOWED = new boolean[200];
        for (int i = 0; i < ALLOWED_CHARS.length(); ++i) ALLOWED[ALLOWED_CHARS.charAt(i)] = true;
    }
    /** Return a string derived from <tt>dirty</tt> that includes only the characters included in
     *  {@link #ALLOWED_CHARS}. */
    public static String sanitize(String dirty) {
        StringBuffer result = new StringBuffer();
        boolean anyChange = false;
        for (int i = 0; i < dirty.length(); ++i) {
            char c = dirty.charAt(i);
            if (c < ALLOWED.length && ALLOWED[c]) result.append(c);
            else anyChange = true;
        }
        return anyChange ? result.toString() : dirty;
    }
}
