/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.globus.purse.exceptions.UserRegistrationException;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data type for user group data.
 */
public class UserGroupData {
    
    static Log logger =
	LogFactory.getLog(UserGroupData.class.getName());
    
    int groupId = -1;
    String name = null;
    String description = null;

    /**
     * Constructor for user data at request
     * 
     * @param groupName
     *        Group name
     * @param groupDesc
     *        Group description
     * @exception <code>UserRegistrationException</code>
     */
    public UserGroupData(String groupName, String groupDesc)
	throws UserRegistrationException {
	this(-1, groupName, groupDesc);
    }

    public UserGroupData(int groupId, String groupName, String groupDesc)
	throws UserRegistrationException {
	
	this.groupId = groupId;
	
	if ((groupName == null) || (groupName.trim().equals(""))) {
	    String err = "Group name cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.name = groupName;

	if ((groupDesc == null) || (groupDesc.trim().equals(""))) {
	    String err = "Group description cannot be null or empty";
	    logger.debug(err);
	    throw new UserRegistrationException(err);
	}
	this.description = groupDesc;
    }

    public void setGroupId(int groupId) throws UserRegistrationException {
	this.groupId = groupId;
    }

    /** Group id */
    public int getId() {
	return this.groupId;
    }

    /** Group Name  */
    public String getName() {
	return this.name;
    }

    /** Group description  */
    public String getDescription() {
	return this.description;
    }

    public boolean equals(UserGroupData userGpData) {

	logger.debug("equal " + this.groupId + " " + userGpData.getId());
	if (this.groupId != userGpData.getId()) 
	    return false;
	
	if (!RegisterUtil.stringsMatch(this.name, 
				       userGpData.getName()))
	    return false;
	
	if (!RegisterUtil.stringsMatch(this.description, 
				       userGpData.getDescription()))
	    return false;

	return true;
    }
}
