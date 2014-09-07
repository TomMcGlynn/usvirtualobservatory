/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.globus.purse.exceptions.UserRegistrationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data type for roles data.
 */
public class RoleData {
    
    static Log logger =
	LogFactory.getLog(RoleData.class.getName());
    
    int id;
    String name = null;
    String description = null;

    /**
     * Constructor for role data
     * 
     * @param name
     *        Role name
     * @param description
     *        Role description
     * @exception <code>UserRegistrationException</code>
     */
    public RoleData(String name, String description) 
	throws UserRegistrationException {
	this(0, name, description);
    }

    /**
     * Constructor for role data
     * 
     * @param id
     *        Role id (from database)
     * @param name
     *        Role name
     * @param description
     *        Role description
     * @exception <code>UserRegistrationException</code>
     */
    public RoleData(int id, String name, String description) 
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

    /** Set Role id */
    public void setId(int id) {
	this.id = id;
    }

    /** Role id */
    public int getId() {
	return this.id;
    }

    /** Role name  */
    public String getName() {
	return this.name;
    }

    /** Role description */
    public String getDescription() {
	return this.description;
    }
    
    public boolean equals(RoleData roleData) {

	logger.debug("equal");
	if (this.id != roleData.getId())
	    return false;

	if (!RegisterUtil.stringsMatch(this.name, roleData.getName()))
	    return false;
	
	if (!RegisterUtil.stringsMatch(this.description, 
				       roleData.getDescription()))
	    return false;
	return true;
    }
}
    
