/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.globus.purse.exceptions.UserRegistrationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data type for RA data.
 */
public class RAData {
    
    static Log logger = LogFactory.getLog(RAData.class.getName());
    
    int id;
    String name = null;
    String email = null;
    String description = null;

    /**
     * Constructor for RA data
     * 
     * @param name
     *        Role name
     * @param email
     *        RA Email Address
     * @param description
     *        RA description
     * @exception <code>UserRegistrationException</code>
     */
    public RAData(String name, String email, String description) 
	throws UserRegistrationException {
	this(0, name, email, description);
    }

    /**
     * Constructor for RA data
     * 
     * @param id
     *        RA id (from database)
     * @param name
     *        RA name
     * @param email
     *        RA Email Address
     * @param description
     *        RA description
     * @exception <code>UserRegistrationException</code>
     */
    public RAData(int id, String name, String email, String description) 
	throws UserRegistrationException {

	this.id = id;

	if ((name == null) || (name.trim().equals(""))) {
	    String err = "Name cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.name = name.trim();

	if ((email == null) || (email.trim().equals(""))) {
	    String err = "Email address cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.email = email.trim();
	
	if ((description == null) || (description.trim().equals(""))) {
	    String err = "Description cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.description = description.trim();
    }

    /** Set RA id */
    public void setId(int id) {
	this.id = id;
    }

    /** RA id */
    public int getId() {
	return this.id;
    }

    /** RA name  */
    public String getName() {
	return this.name;
    }

    /** RA emailaddress  */
    public String getEmail() {
	return this.email;
    }

    /** RA description */
    public String getDescription() {
	return this.description;
    }
    
    public boolean equals(RAData raData) {

	logger.debug("equal");
	if (this.id != raData.getId()) {
	    return false;
        }

	if (!RegisterUtil.stringsMatch(this.name, raData.getName())) {
	    return false;
        }

	if (!RegisterUtil.stringsMatch(this.email, raData.getEmail())) {
	    return false;
        }
	
	if (!RegisterUtil.stringsMatch(this.description, 
				       raData.getDescription())) {
	    return false;
        }
	return true;
    }
}
    
