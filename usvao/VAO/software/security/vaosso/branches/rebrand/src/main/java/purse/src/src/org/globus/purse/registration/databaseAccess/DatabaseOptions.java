/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.UserRegistrationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sets the database configuration options
 */
public class DatabaseOptions extends DatabaseConstants {

    static Log logger =
	LogFactory.getLog(DatabaseOptions.class.getName());


    String driver;
    String connectionURL;
    String userName;
    String password;
    String dbProperties;
    String passphrase;

    // Pool object defualt options 
    // no of connections, when pool is exhausted 1==block till max wait, 
    // max wait is in milliseconds
    int activeConnections = 32;
    byte onExhaustAction = 1;
    long maxWait = 100;
    int idleConnections = 2;

    /**
     * Constructor without pool data initializations 
     */
    public DatabaseOptions(String driver, String connectionURL, 
                           String userName, String password, 
                           String dbProperties, String passphrase)
	throws UserRegistrationException {
	setData(driver, connectionURL, userName, password, dbProperties, 
		passphrase);
    }
	
    /**
     * Constructor with pool data initializations 
     */
    public DatabaseOptions(String driver, String connectionURL,
			   String userName, String password, 
			   String dbProperties, String passphrase, 
			   int activeConnections, byte onExhaustAction, 
                           long maxWait, int idleConnections)
	throws UserRegistrationException {

	setData(driver, connectionURL, userName, password, dbProperties, 
		passphrase);
	this.activeConnections = activeConnections;
	this.onExhaustAction = onExhaustAction;
	this.maxWait = maxWait;
	this.idleConnections = idleConnections;
    }

    private void setData(String driver, String connectionURL, String userName, 
			 String password, String dbProperties, 
                         String passphrase)
	throws UserRegistrationException {

	logger.debug("constructor called with " + driver + " " + connectionURL 
		     + " " + userName + " " + dbProperties
		     + " " + passphrase);

	if ((driver == null) || (driver.trim().equals(""))) {
	    String err = "Driver name cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.driver = driver;
	
	if ((connectionURL == null) || (connectionURL.trim().equals(""))) {
	    String err = "ConnectionURL name cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.connectionURL = connectionURL;
	
	if ((userName == null) || (userName.trim().equals(""))) {
	    String err = "User name cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.userName = userName;

	if ((password == null) || (password.trim().equals(""))) {
	    String err = "Password cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.password = password;

	if ((passphrase == null) || (passphrase.trim().equals(""))) {
	    String err = "Passphrase cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.passphrase = passphrase;

	if ((dbProperties == null) || (dbProperties.trim().equals(""))) {
	    String err = "Database properties filename cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.dbProperties = dbProperties;
	loadDbProperties(dbProperties);
    }

    public String getDbPropertiesFile() {
	
	return this.dbProperties;
    }
    
    /** Driver name */    
    public String getDriver() {

        return this.driver;
    }

    /** Connection URL, (host, port, database name) */
    public String getConnectionURL() {

        return this.connectionURL;
    }

    /** User name to access database */
    public String getUserName() {

        return this.userName;
    }

    /** Password for the above user name */
    public String getPassword() {

        return this.password;
    }
    
    /** Passphrase used to encrypt passwords stored in db */
    public String getPassphrase() {

        return this.passphrase;
    }

    /** Coonection pool: number of active connections */
    public int getActiveConnections() {

        return this.activeConnections;
    }

    /** Coonection pool: action if all connections are exhausted */
    public byte getOnExhaustAction() {

        return this.onExhaustAction;
    }

    /** Coonection pool: maximum wait time for idle connections */
    public long getMaxWait() {

        return this.maxWait;
    }

    /** Coonection pool: maximum number of idle connections */
    public int getIdleConnections() {

        return this.idleConnections;
    }

    public void setIdleConnections(int idleConn) {

        this.idleConnections = idleConn;
    }

    public void setMaxWait(long maxWait) {
	this.maxWait = maxWait;
    }

    public void setOnExhaustActions(byte onExhaustAction) {
	this.onExhaustAction = onExhaustAction;
    }
    
    public void setActiveConnections(int activeConn) {
	this.activeConnections = activeConn;
    }
}
