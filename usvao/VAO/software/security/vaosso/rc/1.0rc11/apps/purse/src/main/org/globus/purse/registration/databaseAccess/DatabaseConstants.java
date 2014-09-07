/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.databaseAccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.UserRegistrationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConstants {

    static Log logger =
	LogFactory.getLog(DatabaseConstants.class.getName());

    // User table
    public static String USER_TABLE_NAME = null;
    public static String USER_COL_ID = null;
    public static String USER_COL_TOKEN = null;
    public static String USER_COL_FIRST_NAME = null;
    public static String USER_COL_LAST_NAME = null;
    public static String USER_COL_CONTACT_PERSON = null;
    public static String USER_COL_STMT_WORK = null;
    public static String USER_COL_USER_NAME = null;
    public static String USER_COL_PASSWORD_SHA = null;
    public static String USER_COL_SALT = null;
    public static String USER_COL_PASSWORD_METHOD = null;
    public static String USER_COL_INSTITUTION = null;
    public static String USER_COL_PROJECT_NAME = null;
    public static String USER_COL_EMAIL = null;
    public static String USER_COL_PHONE = null;
    public static String USER_COL_COUNTRY = null;
    public static String USER_COL_DN = null;
    public static String USER_COL_STATUS = null;
    public static String USER_COL_RA_ID = null;
    public static String USER_COL_CREATION = null;
    public static String USER_COL_LAST_ACCESS = null;
    public static String USER_COL_NUM_LOGINS = null;
    public static String USER_COL_PORTAL_CONFIRM_URL = null;
    public static String USER_COL_PORTAL_NAME = null;

    // RA Table
    public static String RA_TABLE_NAME = null;
    public static String RA_COL_ID = null;
    public static String RA_COL_EMAIL_ID = null;
    public static String RA_COL_NAME = null;
    public static String RA_COL_DESC = null;

    // Role Table 
    public static String ROLE_TABLE_NAME = null;
    public static String ROLE_COL_ID = null;
    public static String ROLE_COL_NAME = null;
    public static String ROLE_COL_DESC = null;

    // User role table 
    public static String USER_ROLE_TABLE_NAME = null;
    public static String USER_ROLE_ROLE_ID = null;
    public static String USER_ROLE_USER_ID = null;

    // Status Table constants
    public static String STATUS_TABLE_NAME = null;
    public static String STATUS_COL_ID = null;
    public static String STATUS_COL_NAME = null;
    public static String STATUS_COL_DESC = null;

    // Group table
    public static String GROUP_TABLE_NAME = null;
    public static String GROUP_COL_ID = null;
    public static String GROUP_COL_NAME = null;
    public static String GROUP_COL_DESC = null;

    // User-group
    public static String USER_GROUP_TABLE_NAME = null;
    public static String USER_GROUP_USER_ID = null;
    public static String USER_GROUP_GROUP_ID = null;


    protected void loadDbProperties(String propFile) 
	throws UserRegistrationException {
	
	Properties prop = new Properties();
	try {
	    prop.load(new FileInputStream(propFile));
	} catch (FileNotFoundException fnfe) {
	    String err = "Error: Database properties file " + propFile 
		+ " not found";
	    logger.error(fnfe);
	    throw new UserRegistrationException(err, fnfe);
	} catch (IOException ioe) {
	    String err = "Error loading file " + propFile;
	    logger.error(ioe);
	    throw new UserRegistrationException(err, ioe);
	}	

	USER_TABLE_NAME = (String)prop.getProperty("user_table");
	USER_COL_ID = (String)prop.getProperty("user_id");
	USER_COL_TOKEN = (String)prop.getProperty("token");
	USER_COL_FIRST_NAME = (String)prop.getProperty("first_name");
	USER_COL_LAST_NAME = (String)prop.getProperty("last_name");
	USER_COL_CONTACT_PERSON = (String)prop.getProperty("contact_person");
	USER_COL_STMT_WORK = (String)prop.getProperty("stmt_of_work");
	USER_COL_USER_NAME = (String)prop.getProperty("user_name");
	USER_COL_PASSWORD_SHA = (String)prop.getProperty("password_sha");
	USER_COL_SALT = (String)prop.getProperty("salt");
	USER_COL_PASSWORD_METHOD = (String)prop.getProperty("password_method");
	USER_COL_INSTITUTION = (String)prop.getProperty("institution");
	USER_COL_PROJECT_NAME = (String)prop.getProperty("project_name");
	USER_COL_EMAIL = (String)prop.getProperty("email");
	USER_COL_PHONE = (String)prop.getProperty("phone");
	USER_COL_COUNTRY = (String)prop.getProperty("country");
	USER_COL_DN = (String)prop.getProperty("user_dn");
	USER_COL_STATUS = (String)prop.getProperty("user_status_id");
	USER_COL_RA_ID = (String)prop.getProperty("user_ra_id");
	USER_COL_CREATION = (String)prop.getProperty("creation");
	USER_COL_LAST_ACCESS = (String)prop.getProperty("last_access");
	USER_COL_NUM_LOGINS = (String)prop.getProperty("number_logins");
	USER_COL_PORTAL_CONFIRM_URL = (String)prop.getProperty("portal_confirm_url");
	USER_COL_PORTAL_NAME = (String)prop.getProperty("portal_name");

	ROLE_TABLE_NAME = (String)prop.getProperty("role_table");
	ROLE_COL_ID = (String)prop.getProperty("role_id");
	ROLE_COL_NAME = (String)prop.getProperty("role_name");
	ROLE_COL_DESC = (String)prop.getProperty("role_description");

	USER_ROLE_TABLE_NAME = (String)prop.getProperty("user_roles_table");
	USER_ROLE_ROLE_ID = (String)prop.getProperty("user_role_role_id");
	USER_ROLE_USER_ID = (String)prop.getProperty("user_role_user_id");

	STATUS_TABLE_NAME = (String)prop.getProperty("status_table");
	STATUS_COL_ID = (String)prop.getProperty("status_id");
	STATUS_COL_NAME = (String)prop.getProperty("status_name");
	STATUS_COL_DESC = (String)prop.getProperty("status_desc");

	GROUP_TABLE_NAME = (String)prop.getProperty("group_table");
	GROUP_COL_ID = (String)prop.getProperty("group_id");
	GROUP_COL_NAME = (String)prop.getProperty("group_name");
	GROUP_COL_DESC = (String)prop.getProperty("group_desc");

	USER_GROUP_TABLE_NAME = (String)prop.getProperty("user_group_table");
	USER_GROUP_GROUP_ID = (String)prop.getProperty("user_group_group_id");
	USER_GROUP_USER_ID = (String)prop.getProperty("user_group_user_id");

        RA_TABLE_NAME = (String)prop.getProperty("ra_table");
        RA_COL_ID = (String)prop.getProperty("ra_id");
        RA_COL_NAME = (String)prop.getProperty("ra_name");
        RA_COL_EMAIL_ID = (String)prop.getProperty("ra_email");
        RA_COL_DESC = (String)prop.getProperty("ra_desc");

	if (!(validateString(USER_TABLE_NAME) || 
              validateString(USER_COL_ID) ||
	      validateString(USER_COL_TOKEN) || 
	      validateString(USER_COL_FIRST_NAME) ||
	      validateString(USER_COL_LAST_NAME) ||
	      validateString(USER_COL_CONTACT_PERSON) ||
	      validateString(USER_COL_STMT_WORK) ||
	      validateString(USER_COL_USER_NAME) ||
	      validateString(USER_COL_PASSWORD_SHA) || 
	      validateString(USER_COL_SALT) || 
	      validateString(USER_COL_PASSWORD_METHOD) || 
	      validateString(USER_COL_INSTITUTION) ||
	      validateString(USER_COL_PROJECT_NAME) ||
	      validateString(USER_COL_EMAIL) || validateString(USER_COL_PHONE)
	      || validateString(USER_COL_DN) || validateString(USER_COL_STATUS)
	      || validateString(USER_COL_CREATION) 
	      || validateString(USER_COL_LAST_ACCESS) 
	      || validateString(ROLE_TABLE_NAME) || validateString(ROLE_COL_ID)
	      || validateString(ROLE_COL_NAME) || validateString(ROLE_COL_DESC)
	      || validateString(USER_ROLE_TABLE_NAME) || 
	      validateString(USER_ROLE_ROLE_ID) || 
	      validateString(USER_ROLE_USER_ID) ||
	      validateString(STATUS_TABLE_NAME) || 
              validateString(STATUS_COL_ID)
	      || validateString(STATUS_COL_NAME) || 
	      validateString(STATUS_COL_DESC) || 
	      validateString(GROUP_TABLE_NAME) ||
	      validateString(GROUP_COL_ID) || validateString(GROUP_COL_NAME) ||
	      validateString(GROUP_COL_DESC) || 
	      validateString(USER_GROUP_TABLE_NAME) || 
	      validateString(USER_GROUP_USER_ID) || 
	      validateString(USER_GROUP_GROUP_ID) || 
              validateString(RA_TABLE_NAME) || validateString(RA_COL_ID) ||
              validateString(RA_COL_EMAIL_ID) || validateString(RA_COL_NAME) ||
              validateString(RA_COL_DESC) || 
              (validateString(USER_COL_RA_ID)))) {
	    String err = "Not all database schema values were initialized.";
	    throw new UserRegistrationException(err);
	}
    }

    private boolean validateString(String str) {
	if ((str == null) || (str.trim().equals("")))
	    return false;
	return true;
    }
}
