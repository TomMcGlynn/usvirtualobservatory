/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.UserRegistrationException;
import org.globus.purse.registration.RoleData;

import java.util.Vector;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to store/get Role Data
 */
public class RoleDataHandler {
    
    static Log logger =
	LogFactory.getLog(RoleDataHandler.class.getName());
 
    /** 
     * Stores role data into table.
     *
     * @param roleData Role data object representing the data to be stored.
     * @exception DatabaseAccessException If any error occurs.
     */
    public static void storeData(RoleData roleData) 
	throws DatabaseAccessException {
	logger.debug("Store data");
	
	String query = "insert into " + DatabaseConstants.ROLE_TABLE_NAME 
	    + "( " + DatabaseConstants.ROLE_COL_NAME + ", " 
	    + DatabaseConstants.ROLE_COL_DESC + ") values ('" 
	    + DatabaseManager.sanitize(roleData.getName()) + "','" + DatabaseManager.sanitize(roleData.getDescription()) + "')";
	DatabaseManager.runUpdateQuery(query);
    }

    /**
     * Returns the request status string
     *
     * @param roleId role id for which role data is required.
     * @return String Role name
     * @exception DatabaseAccessException If any error occurs.
     */
    public static String getRoleName(int roleId) 
	throws DatabaseAccessException {

	logger.debug("Get role name for " + roleId);
	RoleData roleData = getData(roleId);
	return roleData.getName();
    }

    /**
     * Returns the role data for the given id
     *
     * @param id
     *        role id for which status data is required.
     * @return <code>RoleData</code>
     *         Role data for the id
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static RoleData getData(int id) throws DatabaseAccessException {

	logger.debug("getData " + id);
	String query = "select * from " + DatabaseConstants.ROLE_TABLE_NAME 
	    + " where " + DatabaseConstants.ROLE_COL_ID + "=" +  id + "";
	Vector roleVector = getRoleDataForQuery(query);
	if ((roleVector != null) && (roleVector.size() > 0)) {
	    return (RoleData)roleVector.get(0);
        } else {
	    return null;
        }
    }

    /**
     * Returns the role data for the given name
     *
     * @param name
     *        role name for which status data is required.
     * @return <code>RoleData</code>
     *         Role data for the name
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static RoleData getData(String name) throws DatabaseAccessException {

	logger.debug("getData " + name);
	String query = "select * from " + DatabaseConstants.ROLE_TABLE_NAME 
	    + " where " + DatabaseConstants.ROLE_COL_NAME + "='" +  DatabaseManager.sanitize(name) + "'";
	Vector roleVector = getRoleDataForQuery(query);
	if ((roleVector != null) && (roleVector.size() > 0))
	    return (RoleData)roleVector.get(0);
	else 
	    return null;
    }

    /**
     * Returns all the role data 
     *
     * @return Vector
     *         Vector of <code>RoleData</code>
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static Vector getAllData() throws DatabaseAccessException {
	
	logger.debug("Get all role data");
	String query = "select * from " + DatabaseConstants.ROLE_TABLE_NAME;
	return getRoleDataForQuery(query);
    }

    /**
     *
     * Deletes role data from the table for given role id
     * @param id
     *        role id for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(int id) 
	throws DatabaseAccessException {

	logger.debug("delete for is " + id);
	String query = "delete from " + DatabaseConstants.ROLE_TABLE_NAME 
	    + " where " + DatabaseConstants.ROLE_COL_ID + "=" + id;
	DatabaseManager.runUpdateQuery(query);
    }
    
    /**
     *
     * Deletes role data from the table for given role name
     * @param name
     *        role name for which data needs to be deleted
     * @exception <code>DatabaseAccessException</code>
     *            If any error occurs.
     */
    public static void deleteData(String name) 
	throws DatabaseAccessException {

	logger.debug("delete for is " + name);
	String query = "delete from " + DatabaseConstants.ROLE_TABLE_NAME 
	    + " where " + DatabaseConstants.ROLE_COL_NAME + "='" + DatabaseManager.sanitize(name) + "'";
	DatabaseManager.runUpdateQuery(query);
    }

    private static Vector getRoleDataForQuery(String query) 
	throws DatabaseAccessException {
	
	logger.debug("Query is " + query);
	Connection connection = DatabaseManager.getDBConnection();
	Statement statement = null;
	ResultSet resultSet = null;
	try {
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(query);
	    return constructRoleData(resultSet);
	}
	catch (SQLException exp) {
	    logger.error("Error retrieving role data\n " + query, exp);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error retrieving role "
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
	    catch (SQLException ignored) { }
    }
    }

    private static Vector constructRoleData(ResultSet resultSet) 
	throws DatabaseAccessException {
	
	logger.debug("Construct result set");
	Vector<RoleData> roleVector = null;
	RoleData roleData;
	try {
	    while ((resultSet!=null) && (resultSet.next())) {
		if (roleVector == null) 
		    roleVector = new Vector<RoleData>();
		roleData = 
		    new RoleData(
			resultSet.getInt(DatabaseConstants.ROLE_COL_ID),
			resultSet.getString(DatabaseConstants.ROLE_COL_NAME),
			resultSet.getString(DatabaseConstants.ROLE_COL_DESC));
		roleVector.add(roleData);
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
	    String err = "Error constructing role data after retrieval";
	    logger.error(err);
	    throw new DatabaseAccessException(err, exp);
	}
	return roleVector;
    }

    // Methods for user-roles 
    public static void addUserRole(int userId, int roleId) 
	throws DatabaseAccessException {
	
	logger.debug("add user " + userId + " id is " + roleId);
	String query = "insert into " + DatabaseConstants.USER_ROLE_TABLE_NAME
	    + " ( " + DatabaseConstants.USER_ROLE_ROLE_ID + "," 
	    + DatabaseConstants.USER_ROLE_USER_ID + ") values (" + roleId  
	    + "," + userId + ")";
	DatabaseManager.runUpdateQuery(query);
    }

    public static void removeUserRole(int userId, int roleId) 
	throws DatabaseAccessException {
	
	logger.debug("remove user " + userId + " Role id is " + roleId);
	String query = "delete from " + DatabaseConstants.USER_ROLE_TABLE_NAME
	    + " where (( " + DatabaseConstants.USER_ROLE_ROLE_ID + "=" + roleId 
	    + ") and  (" + DatabaseConstants.USER_ROLE_USER_ID + "=" + userId 
	    + "))";
	DatabaseManager.runUpdateQuery(query);
    }

    public static void removeAllUserRoles(int userId) 
	throws DatabaseAccessException {
	logger.debug("remove user " + userId);
	String query = "delete from " + DatabaseConstants.USER_ROLE_TABLE_NAME
	    + " where (" + DatabaseConstants.USER_ROLE_USER_ID + "=" + userId 
	    + ")";
	DatabaseManager.runUpdateQuery(query);
    }

    public static Vector<Integer> getUserRolesId(int userId)
	throws DatabaseAccessException {
	
	logger.debug("User id  " + userId);

	// Get all role ids for given user
	String query = "select " + DatabaseConstants.ROLE_COL_ID + " from "
	    + DatabaseConstants.USER_ROLE_TABLE_NAME + " where " + 
	    DatabaseConstants.USER_ROLE_USER_ID + "=" + userId + "";
	
	Connection connection = DatabaseManager.getDBConnection();
	Statement statement = null;
	ResultSet resultSet = null;
	try {
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery(query);
	    return constructUserRoleData(resultSet);
	}
	catch (SQLException exp) {
	    logger.error("Error retrieving role data for user\n " + query,
                         exp);
	    throw new DatabaseAccessException(DatabaseManager.connectionURL, 
					      DatabaseManager.userName, 
					      "Error retrieving role data for "
					      + "user. " + exp.getMessage(), 
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
	    catch (SQLException ignored) { }
    }
    }
    
    private static Vector<Integer> constructUserRoleData(ResultSet resultSet)
	throws DatabaseAccessException {

	Vector<Integer> roleIds = null;
	try {
	    if ((resultSet!=null) && (resultSet.next())) {
		roleIds = new Vector<Integer>();
		roleIds.add(resultSet.getInt(DatabaseConstants.ROLE_COL_ID));
            while (resultSet.next()) {
		   roleIds.add(resultSet.getInt(DatabaseConstants.ROLE_COL_ID));
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
	}
	return roleIds;
    }
}
