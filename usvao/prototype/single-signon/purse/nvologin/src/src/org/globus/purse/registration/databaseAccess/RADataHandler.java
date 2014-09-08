/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.UserRegistrationException;
import org.globus.purse.registration.RAData;

import java.util.Vector;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to store/get RA Data
 */
public class RADataHandler {
    
    static Log logger =
	LogFactory.getLog(RADataHandler.class.getName());
 
    /** 
     * Stores ra data into table.
     *
     * @param <code>RAData</code>
     *        RA data object representing the data to be stored.
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void storeData(RAData raData) 
	throws DatabaseAccessException {
	logger.debug("Store data");
	
	String query = "insert into " + DatabaseConstants.RA_TABLE_NAME 
	    + "( " + DatabaseConstants.RA_COL_NAME + ", " 
            + DatabaseConstants.RA_COL_EMAIL_ID + ", "
	    + DatabaseConstants.RA_COL_DESC + ") values ('" 
	    + DatabaseManager.sanitize(raData.getName()) + "','" + DatabaseManager.sanitize(raData.getEmail()) + "','"
            + raData.getDescription() + "')";
	DatabaseManager.runUpdateQuery(query);
    }

    public static String getEmailAddress(int raId) 
        throws DatabaseAccessException {
        
        logger.debug("get email address " + raId);
        String query = "select * from " + DatabaseConstants.RA_TABLE_NAME
            + " where " + DatabaseConstants.RA_COL_ID + "=" + raId;
        Vector raVector = getRADataForQuery(query);
        String returnVal = null;
        if ((raVector != null) && (raVector.size() > 0)) {
            RAData raData = (RAData)raVector.get(0);
            returnVal = raData.getEmail();
        }
        return returnVal;
    }

    public static String getEmailAddress(String name) 
        throws DatabaseAccessException {
        
        logger.debug("get email address " + name);
        String query = "select * from " + DatabaseConstants.RA_TABLE_NAME
            + " where " + DatabaseConstants.RA_COL_NAME + "='" + DatabaseManager.sanitize(name) + "'";
        Vector raVector = getRADataForQuery(query);
        String returnVal = null;
        if ((raVector != null) && (raVector.size() > 0)) {
            RAData raData = (RAData)raVector.get(0);
            returnVal = raData.getEmail();
        }
        return returnVal;
    }
    
    public static RAData getData(int id) throws DatabaseAccessException {
        
        logger.debug("get data " + id);
        String query = "select * from " + DatabaseConstants.RA_TABLE_NAME
            + " where " + DatabaseConstants.RA_COL_ID + "=" + id;
        Vector raVector = getRADataForQuery(query);
        if ((raVector != null) && (raVector.size() > 0)) {
            return (RAData)raVector.get(0);
        }
        return null;
    }

    public static RAData getDataByName(String name) 
        throws DatabaseAccessException {
        
        logger.debug("get email address " + name);
        String query = "select * from " + DatabaseConstants.RA_TABLE_NAME
            + " where " + DatabaseConstants.RA_COL_NAME + "='" + DatabaseManager.sanitize(name) + "'";
        Vector raVector = getRADataForQuery(query);
        if ((raVector != null) && (raVector.size() > 0)) {
            return (RAData)raVector.get(0);
        }
        return null;
    }

    /**
     * Returns all the RA data 
     *
     * @return Vector
     *         Vector of <code>RAData</code>
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static Vector getAllData() throws DatabaseAccessException {
	
	logger.debug("Get all RA data");
	String query = "select * from " + DatabaseConstants.RA_TABLE_NAME;
	return getRADataForQuery(query);
    }

    /**
     *
     * Deletes RA data from the table for given RA id
     * @param id
     *        role id for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(int id) throws DatabaseAccessException {

	logger.debug("delete for is " + id);
	String query = "delete from " + DatabaseConstants.RA_TABLE_NAME 
	    + " where " + DatabaseConstants.RA_COL_ID + "=" + id;
	DatabaseManager.runUpdateQuery(query);
    }
    
    /**
     *
     * Deletes RA data from the table for given RA name
     * @param name
     *        role name for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(String name) 
	throws DatabaseAccessException {

	logger.debug("delete for is " + name);
	String query = "delete from " + DatabaseConstants.RA_TABLE_NAME 
	    + " where " + DatabaseConstants.RA_COL_NAME + "='" + DatabaseManager.sanitize(name) + "'";
	DatabaseManager.runUpdateQuery(query);
    }

    private static Vector getRADataForQuery(String query) 
	throws DatabaseAccessException {
	
	logger.debug("Query is " + query);
	Connection connection = DatabaseManager.getDBConnection();
	Statement statement = null;
	ResultSet resultSet = null;
	try {
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(query);
	    return constructRAData(resultSet);
	}
	catch (SQLException exp) {
	    logger.error("Error retrieving RA data\n " + query, exp);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error retrieving RA data. " 
                                              + exp.getMessage(), exp);
	}
	finally {
	    // returning connection irrespective of whether stmt
	    // and result set are closed or not.
	    DatabaseManager.returnDBConnection(connection);
	    try {
		if (resultSet != null)
		    resultSet.close();
		if (statement != null)
		    statement.close();
	    }
	    catch (SQLException exp) {
		String err = "Error retrieving RA data. Could not "
		    + " close SQL statement.";
		logger.error( err, exp);
		throw new DatabaseAccessException(DatabaseManager
                                                  .connectionURL,
						  DatabaseManager.userName, 
						  err + exp.getMessage(), exp);
	    }
	}
    }

    private static Vector constructRAData(ResultSet resultSet) 
	throws DatabaseAccessException {
	
	logger.debug("Construct result set");
	Vector raVector = null;
	RAData raData = null;
	try {
	    while ((resultSet!=null) && (resultSet.next())) {
		if (raVector == null) 
		    raVector = new Vector();
		raData = 
		    new RAData(
			resultSet.getInt(DatabaseConstants.RA_COL_ID),
			resultSet.getString(DatabaseConstants.RA_COL_NAME),
			resultSet
                        .getString(DatabaseConstants.RA_COL_EMAIL_ID),
			resultSet.getString(DatabaseConstants.RA_COL_DESC));
		raVector.add(raData);
	    }
	} catch (SQLException sqlExcep) {
	    logger.error("Cannot return database connection to pool", 
                         sqlExcep);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error returning db connection "
					      + " to pool. " 
					      + sqlExcep.getMessage(), 
					      sqlExcep);
	} catch (UserRegistrationException exp) {
	    String err = "Error constructing RA data after retrieval";
	    logger.error(err);
	    throw new DatabaseAccessException(err, exp);
	}
	return raVector;
    }
}
