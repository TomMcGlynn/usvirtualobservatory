/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.UserRegistrationException;

import java.sql.Date;
import java.util.Vector;

/**
 * Data type for user data.
 */
public class UserData {

    static Log logger =
	LogFactory.getLog(UserData.class.getName());

    String firstName = null;
    String lastName = null;
    String contactPerson = null;
    String stmtOfWork = null;
    String userName = null;
    String passwordSha1 = null;
    String institution = null;
    String projectName = null;
    String emailAddress = null;
    String phoneNumber = null;
    String country = null;
    String portalConfirmUrl = null;
    String portalName = null;
    int raId = -1;

    int numberOfLogins = -1;
    Date creationTime = null;
    Date lastLogin = null;
    int userId = -1;
    int statusId = -1;
    String token = null;
    String userDN = null;
    Vector<Integer> roleIds = null;


    /**
     * Constructor for user data at request
     *
     * @param firstName First name of the user
     * @param lastName Last name of the user
     * @param contactPerson Contact person
     * @param stmtOfWork Statement of work
     * @param userName User name used to log onto ESG portal/MyProxy server
     * @param passwordSha1 Password used to log onto ESG portal/MyProxy server
     * @param institution Name of the institution the user identifies with
     * @param projectName Name of the project
     * @param emailAddress Email address of user
     * @param phone Phone number
     * @param country Country
     * @param statusId Request status id
     * @throws UserRegistrationException if there is a problem with registration
     */
    public UserData(String firstName, String lastName, String contactPerson,
		    String stmtOfWork, String userName, String passwordSha1,
		    String institution, String projectName, String emailAddress,
		    String phone, String country, String portalConfirmUrl,
	            String portalName, int statusId)
	throws UserRegistrationException {
        this(-1, firstName, lastName, contactPerson, stmtOfWork, userName,
	     passwordSha1, institution, projectName, emailAddress, phone, country, null,
	     portalConfirmUrl, portalName, statusId, -1, null, null, -1, new Date(System.currentTimeMillis()), null);
    }

    public UserData(String firstName, String lastName, String contactPerson,
		    String stmtOfWork, String userName, String passwordSha1,
		    String institution, String projectName, String emailAddress,
		    String phone, String country, String portalConfirmUrl, String portalName, int statusId, int raId)
        throws UserRegistrationException {
	this(-1, firstName, lastName, contactPerson, stmtOfWork, userName,
	     passwordSha1, institution, projectName, emailAddress, phone, country, null,
	     portalConfirmUrl, portalName, statusId, raId, null, null, -1, new Date(System.currentTimeMillis()), null);
    }

    /**
     * Constructor to be used when user data is contructed from database.
     *
     * @param userName User name used to log onto ESG portal/MyProxy server
     * @param passwordSha1 Password used to log onto ESG portal/MyProxy server
     * @param institution Name of the institution the user identifies with
     * @param projectName Name of the project
     * @param emailAddress Email address of user
     * @param phoneNumber Phone number
     * @param country Country
     * @param token Token sent to user
     * @param statusId Id of the status the user's request is in
     * @param userDN DN of the user
     * @param portalConfirmUrl the URL supplied by the registering portal to return
     * the user to after confirmation
     * @throws UserRegistrationException if there is a problem with registration
     */
    public UserData(int userId, String firstName, String lastName,
		    String contactPerson, String stmtOfWork, String userName,
		    String passwordSha1, String institution, String projectName,
		    String emailAddress, String phoneNumber, String country, String token,
		    String portalConfirmUrl, String portalName, int statusId, int raId, String userDN, Vector<Integer> roleIds,
		    int numOfLogins, Date creation, Date lastLogin)
	throws UserRegistrationException {

        this.userId = userId;
        this.firstName = validateString(firstName, "First name", false);
        this.lastName = validateString(lastName, "Last name");
        this.contactPerson = validateString(contactPerson, "Contact person", false);
        this.stmtOfWork = validateString(stmtOfWork, "Statement of work", false);
        this.userName = validateString(userName, "User name");
        this.passwordSha1 = passwordSha1; // can't use setPassword() in case old passwords fail new, stricter validation
        this.institution = validateString(institution, "Institution", false);
        this.projectName = validateString(projectName, "Project name", false);
        this.emailAddress = validateString(emailAddress, "User email address");
        this.phoneNumber = validateString(phoneNumber, "Phone number");
        this.country = validateString(country, "Country");
        this.portalConfirmUrl = portalConfirmUrl;
        this.portalName = portalName;
        if (token != null) this.token = token.trim();
        this.statusId = statusId;
        this.userDN = userDN;
        this.raId = raId;

        if (roleIds != null) {
            if (this.roleIds == null)
                this.roleIds = new Vector<Integer>();
            this.roleIds.addAll(roleIds);
        }

        this.numberOfLogins = numOfLogins;

        if (creation != null) {
            this.creationTime = creation;
        }

        if (lastLogin != null) {
            this.lastLogin = lastLogin;
        }
    }

    // VY: Used to be public void setPassword(String password)
    public void validatePassword(String password) throws UserRegistrationException {
        // password = password.trim();
        validateString(password, "Password");
        if (password.length() < 6)
            throw new UserRegistrationException("Password is too short; must be at least 6 characters.");
    }

    public void setRaId(int raId) throws UserRegistrationException {
    	this.raId = raId;
    }

    public void setUserId(int userId) throws UserRegistrationException {
        logger.debug("User is " + userId);
        this.userId = userId;
    }

    public void setToken(String token) throws UserRegistrationException {

        if ((token == null) || (token.trim().equals(""))) {
            String err = "Token cannot be null or empty";
            logger.debug(err);
            throw new UserRegistrationException(err);
        }
        logger.debug("token is " + token.trim());
        this.token = token.trim();
    }

    public void setUserDN(String userDN) throws UserRegistrationException {
        if ((userDN == null) || (userDN.trim().equals(""))) {
            String err = "User DN cannot be null or empty";
            logger.debug(err);
            throw new UserRegistrationException(err);
        }
        logger.debug("userDN is " + userDN.trim());
        this.userDN = userDN.trim();
    }

    public void setStatus(int statusId) throws UserRegistrationException {
        logger.debug("Status is " + statusId);
        this.statusId = statusId;
    }

    public void setCreationTime(Date creation) throws
            UserRegistrationException {
        this.creationTime = creation;
    }

    public void setLastLogin(Date lastLogin) throws
            UserRegistrationException {
        this.lastLogin = lastLogin;
    }

    public void setNumberOfLogins(int logins)
            throws UserRegistrationException {

        logger.debug("Number of logins " + logins);
        this.numberOfLogins = logins;
    }

    public void addRole(int roleId) throws UserRegistrationException {

        logger.debug("Role id to add is " + roleId);
        if (roleIds == null) {
            roleIds = new Vector<Integer>();
        }
        roleIds.add(roleId);
    }

    public void addRoles(Vector<Integer> roleIds) throws UserRegistrationException {

        if (roleIds != null) {
            if (this.roleIds == null) {
                this.roleIds = new Vector<Integer>();
            }
            this.roleIds.addAll(roleIds);
        }
    }

    public void removeRole(int roleId) throws UserRegistrationException {

        logger.debug("Role id to remove is " + roleId);
        if (roleIds == null) {
            String err = "There were no assigned roles for this user";
            logger.error(err);
            throw new UserRegistrationException(err);
        }
        roleIds.remove(new Integer(roleId));
    }

    /** RA id **/
    public int getRaId() {
        return this.raId;
    }

    /** User id */
    public int getUserId() {
	    return this.userId;
    }

    /** Roles */
    public Vector<Integer> getUserRoles() {
	    return this.roleIds;
    }

    /** Request status */
    public int getStatus() {
	    return this.statusId;
    }

    /** UserDN */
    public String getUserDN() {
	    return this.userDN;
    }

    /** Token sent to user */
    public String getToken() {
	    return this.token;
    }

    /** First name of the user  */
    public String getFirstName() {
	    return this.firstName;
    }

    /** Last name of the user  */
    public String getLastName() {
	    return this.lastName;
    }

    /** User name used to log onto ESG portal/MyProxy server */
    public String getUserName() {
	    return this.userName;
    }

    /** Password used to log onto ESG portal/MyProxy server */
    public String getPasswordSha1() {
	    return this.passwordSha1;
    }

    /** Name of the institution the user identifies with */
    public String getInstitution() {
	    return this.institution;
    }

    /** Contact person  */
    public String getContactPerson() {
	    return this.contactPerson;
    }

    /** Statement of work  */
    public String getStmtOfWork() {
	    return this.stmtOfWork;
    }

    /** Name of the project */
    public String getProjectName() {
	    return this.projectName;
    }

    /** Email address of user */
    public String getEmailAddress() {
	    return this.emailAddress;
    }

    /** Phone number */
    public String getPhoneNumber() {
    	return this.phoneNumber;
    }

    /** Country */
    public String getCountry() {
    	return this.country;
    }

    /** The URL supplied by the registering portal to return the user to after confirmation. */
    public String getPortalConfirmUrl() {
        return portalConfirmUrl;
    }

    /** The name of the registering portal. */
    public String getPortalName() {
        return portalName;
    }

    public Date getCreationTime() {
	    return this.creationTime;
    }

    public Date getLastLogin() {
	    return this.lastLogin;
    }

    public int getNumberOfLogins() {
	    return this.numberOfLogins;
    }

    public boolean equals(UserData userData) {
        logger.debug("equal");

        if (this.userId != userData.getUserId())
            return false;
        logger.debug("User id thro'");

        if (!RegisterUtil.stringsMatch(this.firstName,
                userData.getFirstName()))
            return false;
        logger.debug("first name thro'");

        if (!RegisterUtil.stringsMatch(this.lastName, userData.getLastName()))
            return false;
        logger.debug("last name thro'");

        if (!RegisterUtil.stringsMatch(this.contactPerson,
                userData.getContactPerson()))
            return false;
        logger.debug("contact person thro'");

        if (!RegisterUtil.stringsMatch(this.stmtOfWork,
                userData.getStmtOfWork()))
            return false;
        logger.debug("Stmt of work thro'");

        if (!RegisterUtil.stringsMatch(this.userName, userData.getUserName()))
            return false;
        logger.debug("userName tho'");

        if (!RegisterUtil.stringsMatch(this.passwordSha1, userData.getPasswordSha1()))
            return false;
        logger.debug("password tho'");

        if (!RegisterUtil.stringsMatch(this.institution,
                userData.getInstitution()))
            return false;
        logger.debug("institution tho'");

        if (!RegisterUtil.stringsMatch(this.projectName,
                userData.getProjectName()))
            return false;
        logger.debug("projectName tho'");

        if (!RegisterUtil.stringsMatch(this.emailAddress,
                userData.getEmailAddress()))
            return false;
        logger.debug("emailAddress tho'");

        if (!RegisterUtil.stringsMatch(this.phoneNumber,
                userData.getPhoneNumber()))
            return false;
        logger.debug("phoneNumber tho'");

        if (!RegisterUtil.stringsMatch(this.country,
                userData.getCountry()))
            return false;
        logger.debug("country tho'");

        if (this.statusId != userData.getStatus())
            return false;

        if (!RegisterUtil.stringsMatch(this.userDN,
                userData.getUserDN()))
            return false;

        if (!RegisterUtil.stringsMatch(this.token, userData.getToken()))
            return false;
        logger.debug("token tho'");

        if (this.numberOfLogins != userData.getNumberOfLogins())
            return false;
        logger.debug("number of logins tho'");

        if (!compareDate(userData.getCreationTime(), this.creationTime))
            return false;

        logger.debug("creation time tho'");

        if (!compareDate(userData.getLastLogin(), this.lastLogin))
            return false;
        logger.debug("last login tho'");

        // Check that roles match
        Vector retrievedRoles = userData.getUserRoles();
        if (this.roleIds != null) {
            if (retrievedRoles == null)
                return false;
            if (roleIds.size() != retrievedRoles.size())
                return false;
            if (!roleIds.containsAll(retrievedRoles))
                return false;
        } else {
            if (retrievedRoles != null) {
                return false;
            }
        }

        return true;
    }

    private boolean compareDate(Date d1, Date d2) {

        logger.debug("Date 1 " + d1 + "\nDate 2 " + d2);

        if (d1 != null) {
            if (d2 == null) {
                return false;
            } else {
                logger.debug("Date 1 " + d1.getTime() + "\nDate 2 " + d2.getTime());
                logger.debug("D1 is not null");
                logger.debug("D2 is not null");
                if (!d1.toString().equals(d2.toString())) {
                    logger.debug(".equls jsut returns false " + d1.compareTo(d2));
                    return false;
                }
            }
        } else {
            logger.debug("D1 is null");
            if (d2 != null) {
                logger.debug("D2 is not null");
                return false;
            }
        }
        return true;
    }

    private String validateString(String str, String err, boolean preventNull)
            throws UserRegistrationException {
        if (preventNull && ((str == null) || (str.trim().length() == 0))) {
            String msg = err + " cannot be null or empty.";
            logger.error(msg);
            throw new UserRegistrationException(msg);
        }
        return validateString(str, err);
    }

    private String validateString(String str, String err)
            throws UserRegistrationException {
        return str == null ? null : str.trim();
    }
}
