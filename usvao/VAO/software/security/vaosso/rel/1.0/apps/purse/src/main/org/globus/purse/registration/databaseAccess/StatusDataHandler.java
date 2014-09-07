/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.UserRegistrationException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.exceptions.DatabaseAccessException;

import org.globus.purse.registration.StatusData;
import org.globus.purse.registration.RegisterUtil;

import java.util.Vector;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to store/get status data
 */
public class StatusDataHandler {
    
    static Log logger =
	LogFactory.getLog(StatusDataHandler.class.getName());

    /** 
     * Stores status data into table.
     *
     * @param <code>StatusData</code>
     *        Status data object representing the data to be stored.
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void storeData(StatusData statusData) 
	throws DatabaseAccessException {
	logger.debug("Store data");
	
	String query = "insert into " + DatabaseConstants.STATUS_TABLE_NAME 
	    + " (" + DatabaseConstants.STATUS_COL_NAME + ", " 
	    + DatabaseConstants.STATUS_COL_DESC + ") values ('" 
	    + DatabaseManager.sanitize(statusData.getName()) + "','" + DatabaseManager.sanitize(statusData.getDescription())
            + "')";
	DatabaseManager.runUpdateQuery(query);
    }

    /**
     * Returns the request status string
     *
     * @param id
     *        stauts id for which status data is required.
     * @return String
     *         Status request
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static String getRequestStatus(int statusId) 
	throws DatabaseAccessException {

	logger.debug("Get request status " + statusId);
	StatusData statusData = getData(statusId);
	return statusData.getName();
    }

    /**
     * Returns the status data for the given id
     *
     * @param id
     *        stauts id for which status data is required.
     * @return <code>StatusData</code>
     *         Status data for the token
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static StatusData getData(int id) 
	throws DatabaseAccessException {

	logger.debug("getData " + id);
	String query = "select * from " + DatabaseConstants.STATUS_TABLE_NAME 
	    + " where " + DatabaseConstants.STATUS_COL_ID + "=" +  id;
	Vector statusVector =  getStatusDataForQuery(query);
	if ((statusVector != null) && (statusVector.size() > 0)) {
	    return (StatusData)statusVector.get(0);
        } else {
	    return null;
        }
    }

    /**
     * Returns the status data for the given name
     *
     * @param name
     *        status name for which status data is required.
     * @return <code>StatusData</code>
     *         Status data for the token
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static StatusData getData(String name) 
	throws DatabaseAccessException {

	logger.debug("getData " + name);
	String query = "select * from " + DatabaseConstants.STATUS_TABLE_NAME 
	    + " where " + DatabaseConstants.STATUS_COL_NAME + "='" +  DatabaseManager.sanitize(name)
	    + "'";
	Vector statusVector =  getStatusDataForQuery(query);
	if ((statusVector != null) && (statusVector.size() > 0)) {
	    return (StatusData)statusVector.get(0);
        } else {
	    return null;
        }
    }

    /**
     * Returns all status data entries in the table
     *
     * @return Vector
     *         Vector of <code>StatusData</code> 
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static Vector getAllData() throws DatabaseAccessException {
	
	logger.debug("Get all status data");
	String query = "select * from " + DatabaseConstants.STATUS_TABLE_NAME;
	return getStatusDataForQuery(query);
    }

    /**
     *
     * Deletes status data from the table for given status id
     * @param id
     *        status id for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(int id) 
	throws DatabaseAccessException {

	logger.debug("delete for is " + id);
	String query = "delete from " + DatabaseConstants.STATUS_TABLE_NAME 
	    + " where " + DatabaseConstants.STATUS_COL_ID + "=" + id;
	DatabaseManager.runUpdateQuery(query);
    }

    /**
     *
     * Deletes status data from the table for given status name
     * @param name
     *        status name for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(String name) 
	throws DatabaseAccessException {
	
	logger.debug("delete for is " + name);
	String query = "delete from " + DatabaseConstants.STATUS_TABLE_NAME 
	    + " where " + DatabaseConstants.STATUS_COL_NAME + "='" + DatabaseManager.sanitize(name) 
            + "'";
	DatabaseManager.runUpdateQuery(query);
    }

    // Returns the id of the status that implies that a request has
    // been made for access.
    public static int getRequestStatusId() throws DatabaseAccessException {
	String reqStat = RegisterUtil.getRequestStatus();
	if (reqStat == null) {
	    String err = "Database not initialzied with status data";
	    logger.error(err);
	    throw new DatabaseAccessException(err);
	}
	StatusData statusData = getData(reqStat);
	return statusData.getId();
    }

    public static int getRenewalStatusId() throws DatabaseAccessException {
	String reqStat = RegisterUtil.getRenewalStatus();
	if (reqStat == null) {
	    String err = "Database not initialzied with status data";
	    logger.error(err);
	    throw new DatabaseAccessException(err);
	}
	StatusData statusData = getData(reqStat);
	return statusData.getId();
    }

    public static int getId(String name) throws DatabaseAccessException, 
						RegistrationException {
	StatusData statusData = getData(name);
	if (statusData == null) {
	    String err = "Status data with name: " + name + " does not exist";
	    logger.error(err);
	    throw new RegistrationException(err);
	}
	return statusData.getId();
    }

    private static Vector getStatusDataForQuery(String query) 
	throws DatabaseAccessException {
	
	logger.debug("Query is " + query);
	Connection connection = DatabaseManager.getDBConnection();
	Statement statement = null;
	ResultSet resultSet = null;
	try {
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(query);
	    return constructStatusData(resultSet);
	}
	catch (SQLException exp) {
	    logger.error("Error retrieving status data\n " + query, exp);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error retrieving status "
					      + "data. " + exp.getMessage(), 
					      exp);
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
		String err = "Error retrieving status data. Could not "
		    + " close SQL statement.";
		logger.error( err, exp);
		throw new DatabaseAccessException(DatabaseManager
                                                  .connectionURL,
						  DatabaseManager.userName, 
						  err + exp.getMessage(), exp);
	    }
	}
    }

    private static Vector constructStatusData(ResultSet resultSet) 
	throws DatabaseAccessException {
	
	logger.debug("Construct result set");
	Vector statusVector = null;
	StatusData statusData = null;
	try {
	    if (resultSet!=null) {
		while (resultSet.next()) {
		    if (statusVector == null)
			statusVector = new Vector();
		    statusData = 
			new StatusData(
			resultSet.getInt(DatabaseConstants.STATUS_COL_ID), 
			resultSet.getString(DatabaseConstants.STATUS_COL_NAME),
			resultSet.getString(DatabaseConstants
                                            .STATUS_COL_DESC));
		    statusVector.add(statusData);
		}
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
	    String err = "Error constructing status data after retrieval";
	    logger.error(err);
	    throw new DatabaseAccessException(err, exp);
	}
	return statusVector;
    }
}
