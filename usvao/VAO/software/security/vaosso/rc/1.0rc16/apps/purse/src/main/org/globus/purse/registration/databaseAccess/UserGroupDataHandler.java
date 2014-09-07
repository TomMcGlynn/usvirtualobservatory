/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.UserRegistrationException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.exceptions.DatabaseAccessException;

import org.globus.purse.registration.UserGroupData;

import java.util.Vector;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to store/get User Group data
 */
public class UserGroupDataHandler {
    
    static Log logger =
	LogFactory.getLog(UserGroupDataHandler.class.getName());

    /** 
     * Stores user group data into table.
     *
     * @param <code>UserGroupData</code>
     *        User group data object representing the data to be stored.
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void storeData(UserGroupData groupData) 
	throws DatabaseAccessException {
	logger.debug("Store data");
	
	String query = "insert into " + DatabaseConstants.GROUP_TABLE_NAME 
	    + " (" + DatabaseConstants.GROUP_COL_NAME + ", " 
	    + DatabaseConstants.GROUP_COL_DESC + ") values ('" 
	    + DatabaseManager.sanitize(groupData.getName()) + "','" + DatabaseManager.sanitize(groupData.getDescription()) + "')";
	DatabaseManager.runUpdateQuery(query);
    }

    /**
     * Returns the group data for the given id
     *
     * @param id
     *        group id for which group data is required.
     * @return <code>UserGroupData</code>
     *         User Group data for the token
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static UserGroupData getData(int id) 
	throws DatabaseAccessException {

	logger.debug("getData " + id);
	String query = "select * from " + DatabaseConstants.GROUP_TABLE_NAME 
	    + " where " + DatabaseConstants.GROUP_COL_ID + "=" +  id;
	Vector userGpVector =  getUserGpDataForQuery(query);
	if ((userGpVector != null) && (userGpVector.size() > 0))
	    return (UserGroupData)userGpVector.get(0);
	else 
	    return null;
    }

    /**
     * Returns the group data for the given name
     *
     * @param name
     *        group name for which group data is required.
     * @return <code>UserGroupData</code>
     *         Group data for the token
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static UserGroupData getData(String name) 
	throws DatabaseAccessException {

	logger.debug("getData " + name);
	String query = "select * from " + DatabaseConstants.GROUP_TABLE_NAME 
	    + " where " + DatabaseConstants.GROUP_COL_NAME + "='" +  DatabaseManager.sanitize(name)
	    + "'";
	Vector userGpVector =  getUserGpDataForQuery(query);
	if ((userGpVector != null) && (userGpVector.size() > 0))
	    return (UserGroupData)userGpVector.get(0);
	else 
	    return null;
    }

    /**
     * Returns all group data entries in the table
     *
     * @return Vector
     *         Vector of <code>UserGroupData</code> 
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static Vector getAllData() throws DatabaseAccessException {
	
	logger.debug("Get all user group data");
	String query = "select * from " + DatabaseConstants.GROUP_TABLE_NAME;
	return getUserGpDataForQuery(query);
    }

    /**
     *
     * Deletes user group data from the table for given status id
     * @param id
     *        group id for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(int id) 
	throws DatabaseAccessException {

	logger.debug("delete for is " + id);
	String query = "delete from " + DatabaseConstants.GROUP_TABLE_NAME 
	    + " where " + DatabaseConstants.GROUP_COL_ID + "=" + id;
	DatabaseManager.runUpdateQuery(query);
    }

    /**
     *
     * Deletes group data from the table for given group name
     * @param name
     *        group name for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(String name) 
	throws DatabaseAccessException {
	
	logger.debug("delete for is " + name);
	String query = "delete from " + DatabaseConstants.GROUP_TABLE_NAME 
	    + " where " + DatabaseConstants.GROUP_COL_NAME + "='" + DatabaseManager.sanitize(name) + "'";
	DatabaseManager.runUpdateQuery(query);
    }

    public static int getId(String name) throws DatabaseAccessException, 
						RegistrationException {
	UserGroupData userGpData = getData(name);
	if (userGpData == null) {
	    String err = "Group data with name: " + name + " does not exist";
	    logger.error(err);
	    throw new RegistrationException(err);
	}
	return userGpData.getId();
    }

    private static Vector getUserGpDataForQuery(String query) 
	throws DatabaseAccessException {
	
	logger.debug("Query is " + query);
	Connection connection = DatabaseManager.getDBConnection();
	Statement statement = null;
	ResultSet resultSet = null;
	try {
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(query);
	    return constructUserGpData(resultSet);
	}
	catch (SQLException exp) {
	    logger.error("Error retrieving user group data\n " + query, exp);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error retrieving user group "
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
		String err = "Error retrieving user group data. Could not "
		    + " close SQL statement.";
		logger.error( err, exp);
		throw new DatabaseAccessException(DatabaseManager
                                                  .connectionURL,
						  DatabaseManager.userName, 
						  err + exp.getMessage(), exp);
	    }
	}
    }

    private static Vector constructUserGpData(ResultSet resultSet) 
	throws DatabaseAccessException {
	
	logger.debug("Construct result set");
	Vector userGpVector = null;
	UserGroupData userGpData = null;
	try {
	    if (resultSet!=null) {
		while (resultSet.next()) {
		    if (userGpVector == null)
			userGpVector = new Vector();
		    userGpData = 
			new UserGroupData(
			resultSet.getInt(DatabaseConstants.GROUP_COL_ID), 
			resultSet.getString(DatabaseConstants.GROUP_COL_NAME),
			resultSet.getString(DatabaseConstants.GROUP_COL_DESC));
		    userGpVector.add(userGpData);
		}
	    }
	} catch (SQLException sqlExcep) {
	    logger.error("Cannot return database connection to pool",
                         sqlExcep);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error returning db connection"
					      + " to pool. " 
					      + sqlExcep.getMessage(), 
					      sqlExcep);
	} catch (UserRegistrationException exp) {
	    String err = "Error constructing user group data after retrieval";
	    logger.error(err);
	    throw new DatabaseAccessException(err, exp);
	}
	return userGpVector;
    }
}
