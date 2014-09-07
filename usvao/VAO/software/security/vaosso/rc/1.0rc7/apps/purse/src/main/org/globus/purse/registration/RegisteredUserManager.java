/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.globus.purse.exceptions.RegistrationException;

import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.exceptions.DatabaseAccessException;

import org.globus.purse.registration.certificateStorage.MyProxyManager;
import org.globus.purse.exceptions.MyProxyAccessException;
import org.ietf.jgss.GSSCredential;

import org.globus.purse.exceptions.CertificateGenerationException;
import org.globus.purse.registration.certificateGeneration.UserCertificateGeneration;

import org.globus.purse.registration.mailProcessing.MailManager;
import org.globus.purse.registration.mailProcessing.MailOptions;
import org.globus.purse.exceptions.MailAccessException;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle registrated users
 */
public class RegisteredUserManager {
    
    static Log logger =
	LogFactory.getLog(RegisteredUserManager.class.getName());

    /**
     * Deletes user from MyProxy (parallel to revocation)
     *
     * @param userName
     *        The user name whose credentials needs to be revoked.
     * @exception <code>RegistrationException</code>
     *        If any error occurs.
     */
    public static void revokeUser(String userName) 
	throws RegistrationException {

	if (userName == null) {
	    String err = "User name cannot be null";
	    logger.error(err);
	    throw new RegistrationException(err);
	}
	
	try {
	    MyProxyManager.deleteUser(userName);
	} catch (MyProxyAccessException exp) {
	    String err = "Error deleting user from server";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}
    }

    /**
     * Changes user password
     * 
     * @param userName
     *        User name for which password needs to be changed.
     * @param oldPass
     *        Old password.
     * @param newPass
     *        New password
     * @param gssCred
     *        Credential to use for changing password.
     * @exception <code>RegistrationException</code>
     *            If any error occurs.
     */
    public static void changeUserPassword(String userName, String oldPass, 
					  String newPass, 
                                          GSSCredential gssCred)
	throws RegistrationException {

	String err = " cannot be null";

	if (userName == null) {
	    logger.error("User name " + err);
	    throw new RegistrationException("User name " + err);
	}

	if (oldPass == null) {
	    logger.error("Old password " + err);
	    throw new RegistrationException("Old password " + err);
	}   

	if (newPass == null) {
	    logger.error("New password " + err);
	    throw new RegistrationException("New password " + err);
	}   

	if (gssCred == null) {
	    logger.error("Credentials " + err);
	    throw new RegistrationException("Credentials " + err);
	}

	try {
	    MyProxyManager.changeUserPassword(userName, oldPass, newPass,
					      gssCred);
	} catch (MyProxyAccessException exp) {
	    String errMsg = "Error changing password from " + userName;
	    logger.error(errMsg);
	    throw new RegistrationException(errMsg, exp);
	}
    }

    /**
     * deletes all users whose status has been set to rejected.
     *
     * @exception <code>RegistrationException</code>
     *            If any error occurs.
     */
    public static void deleteRejectedUsers() throws RegistrationException {
	int id = StatusDataHandler.getId(RegisterUtil.getRejectedStatus());
	try {
	    UserDataHandler.deleteUsers(id);
	} catch (DatabaseAccessException exp) {
	    String errMsg = "Error deleting rejected users";
	    logger.error(errMsg);
	    throw new RegistrationException(errMsg, exp);
	}
    }

    // Detect cert is going to expire and send mail to user.
    public static void sendCertExpirationWarning() 
	throws RegistrationException {

	Vector expiredUsers = MyProxyManager.getExpiredUsers();
	if (expiredUsers != null) {
	    for (int i=0; i<expiredUsers.size(); i++) {
		String userName = (String)expiredUsers.get(i);
		logger.debug("User name " + userName);
		UserData userData = 
		    UserDataHandler.getDataForUsername(userName);
		if (userData != null) {
		    String emailAddr = userData.getEmailAddress();
		    MailManager.sendExpirationWarning(
				userName, emailAddr,
				MyProxyManager.getExpirationLead());
		}
	    }
	}
    }

    public static void storeRenewalDetails(String userName, String password)
	throws RegistrationException {
	
	logger.debug("Renew credentials for " + userName);
	
	if ((userName == null) || (password == null)) {
	    String str = "Username/Passphrase cannot be null";
	    logger.error(str);
	    throw new RegistrationException(str);
	}
	
	// set new passphrase
	UserDataHandler.setUserPassword(userName, password);
	
	// set status as renewal requested.
	int status = StatusDataHandler.getRenewalStatusId();
	UserDataHandler.setStatusForUsername(userName, status);
    }

    public static void renewUser(String userName, String caPassphrase) 
	throws RegistrationException {
	
	UserData userData = null;
	// Retrieve data from db 
	try {
	    userData = UserDataHandler.getDataForUsername(userName);
	} catch (DatabaseAccessException exp) {
	    String err = "Could not retrieve data from db";
	    logger.error(err, exp);
	    throw new RegistrationException(err, exp);
	}

	// Check if user data is null
	if (userData == null) {
	    String err = "A user with userName " + userName 
		+ " does not exist in database";
	    logger.error(err);
	    throw new RegistrationException(err);
	}

	// Check status is renew
	if (userData.getStatus() != StatusDataHandler.getRenewalStatusId()) {
	    String err = "Renewal not requested for user data " + userName;
	    logger.error(err);
	    throw new RegistrationException(err);
	}

	// Generate certificates
	String certDir = RegisterUtil.generateUserCerts(userData,
							caPassphrase);

	// Store certificates.
	RegisterUtil.storeUserCerts(userData, certDir);

	// Set the user DN
	try {
	    String dn = UserCertificateGeneration.getDN(certDir);
	    UserDataHandler.setUserDNForUsername(userName, dn);
	} catch (CertificateGenerationException exp) {
	    String err = "Error setting user DN";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}

	// Delete local certificates.
	RegisterUtil.deleteCerts(certDir);
	
	// Set status as accepted.
	RegisterUtil.setUserStatusAsAccepted(userData.getToken());
	
	// Send mailto user that all is set.
	try {
	    MailManager.sendRenewalMail(userName, userData.getEmailAddress());
	} catch (MailAccessException exp) {
	    String err = "Error sending CA accept mail.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}
    }
}
