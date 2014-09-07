/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.globus.purse.exceptions.UserRegistrationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data type for status data.
 */
public class StatusData {
    
    static Log logger =
	LogFactory.getLog(StatusData.class.getName());
    
    int id;
    String name = null;
    String description = null;

    /**
     * Constructor for status data
     * 
     * @param name
     *        Status name
     * @param description
     *        Status description
     * @exception <code>UserRegistrationException</code>
     */
    public StatusData(String name, String description) 
	throws UserRegistrationException {
	this(0, name, description);
    }

    /**
     * Constructor for status data
     * 
     * @param id
     *        Status id (auto generated in the database)
     * @param name
     *        Status name
     * @param description
     *        Status description
     * @exception <code>UserRegistrationException</code>
     */
    public StatusData(int id, String name, String description) 
	throws UserRegistrationException {
	
	this.id = id;

	if ((name == null) || (name.trim().equals(""))) {
	    String err = "Name cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.name = name.trim();
	
	if ((description == null) || (description.trim().equals(""))) {
	    String err = "Description cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.description = description.trim();
    }

    /** Set status id */
    public void setId(int id) {
	this.id = id;
    }

    /** Status id */
    public int getId() {
	return this.id;
    }

    /** Status name  */
    public String getName() {
	return this.name;
    }

    /** Status description */
    public String getDescription() {
	return this.description;
    }
    
    public boolean equals(StatusData statusData) {

	logger.debug("equal");
	if (this.id != statusData.getId())
	    return false;

	if (!RegisterUtil.stringsMatch(this.name, statusData.getName()))
	    return false;
	
	if (!RegisterUtil.stringsMatch(this.description, 
				       statusData.getDescription()))
	    return false;
	return true;
    }
    public String toString() {
        String statusData = "name: " + this.name +
                            " description: " + this.description +
                            " id: " + this.id;
        return statusData;
    }
}
    
