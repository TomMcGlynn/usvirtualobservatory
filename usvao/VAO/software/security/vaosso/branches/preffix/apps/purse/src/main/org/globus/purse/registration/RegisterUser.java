/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.*;
import org.globus.purse.registration.certificateGeneration.UserCertificateGeneration;
import org.globus.purse.registration.certificateStorage.MyProxyManager;
import org.globus.purse.registration.databaseAccess.RADataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.mailProcessing.MailManager;
import org.globus.purse.registration.mailProcessing.StatusMessage;

import javax.mail.Message;
import java.io.*;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Random;

import java.security.SecureRandom;
import java.math.BigInteger;

/**
 * Class to handle user registration
 */
public class RegisterUser {
    
    static Log logger =
	LogFactory.getLog(RegisterUser.class.getName());
    
    /**
     * Registers the user by placing the user in the user database.
     * To confirm that the registered email address belongs to the person
     * registering, email is sent to user with a token (generating internal
     * to the method and cached in the database); the user must respond to 
     * a validation service with that token before a certificate can be 
     * generated.  
     *
     * @param userData
     *        Data type representing user who needs to be registred.
     * 
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     * @exception <code>UserRegistrationException</code>
     *            If any error that could be exposed to the client.
     */
    public static void register(UserData userData)
         throws RegistrationException 
    {
	register(userData, null);
    }

    /**
     * Registers the user by placing the user in the user database.
     * To confirm that the registered email address belongs to the person
     * registering, email is sent to user with a token (generating internal
     * to the method and cached in the database); the user must respond to 
     * a validation service with that token before a certificate can be 
     * generated.  <p>
     *
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email message.  This is 
     * accomplished via a template identified by the sendTokenTemplate 
     * configuration property (in the purse.properties file).  This template
     * contains tags of the form <code>@<it>tagname</it>@</code> that 
     * locate the places in the message where the information is to be 
     * inserted.  The <code>messageData</code> Properties parameter provides
     * the mapping of <it>tagname</it> to actual data.  Thus, the set of 
     * tags supported is completely up to the author of the template and
     * the caller of this method.  <p>
     *
     * This implementation will add certain tag definitions to the messageData
     * Properties if they are not already defined, set to default values; these 
     * include:
     * <pre>
     *   fullname       the first and last name of the registrant drawn from 
     *                    the userData object.
     *   token          a URL that the user should access validate his/her
     *                    registration.  The internally generated token is 
     *                    embedded in this URL.  <it>Note: this tag definition
     *                    is defined for backward compatability.</it>
     *   secret         the actual value of the token.  
     * </pre>
     * The template is not required to use any of these; they are set to 
     * support the default templates provided by PURSE.  Their values and 
     * intended use can be overridden by the mappings in the messageData
     * and use in the template.  
     *
     * @param userData
     *        Data type representing user who needs to be registred.
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into confirmation request sent by email to the user.  If null,
     *        a default set of properties will used based on the information in 
     *        the userData parameter (see description above).  
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     * @exception <code>UserRegistrationException</code>
     *            If any error that could be exposed to the client.
     */
    public static void register(UserData userData, Properties messageData)
	throws RegistrationException, MailAccessException
    {
	if (userData == null) {
	    String err = "User data cannot be null";
	    logger.error(err);
	    throw new RegistrationException(err);
	}

	String userNameExistsErr = "User name " + userData.getUserName() 
	    + " already exists";
	String errorMsg = "Error verifying if specified username " 
	    + userData.getUserName() + "already exists.";

	// Check if the user name has already been chosen by someone whose
	// request is in process.
	try {
	    if (UserDataHandler.userNameExists(userData.getUserName())) {
		logger.error(userNameExistsErr);
		throw new UserRegistrationException(userNameExistsErr);
	    }
	} catch (DatabaseAccessException exp) {
	    logger.error(errorMsg);
	    throw new RegistrationException(errorMsg, exp);
	} 

	// Generate token
	String token = new UniqueToken(System.currentTimeMillis(), 
				       new Random().nextLong()).toString();
	userData.setToken(token);
	logger.debug("Token is " + token);

	//  set status as requested.
	int reqStatus = StatusDataHandler.getRequestStatusId();
	userData.setStatus(reqStatus);

	// Store details in database.	
	try {
	    UserDataHandler.storeData(userData);
	} catch (DatabaseAccessException exp) {
	    String err = "Error storing data for " + userData.getUserName();
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	} 

	// Get ready to send mail with token by setting default messages tags.
        // The default token tag will be set by the MailManager class below.
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("secret") == null) 
            messageData.setProperty("secret", token);
        if (messageData.getProperty("fullname") == null)
            // deal with bug where lastName is actually full name.  Hmm.
            messageData.setProperty("fullname",
                       (userData.getFirstName() + ' ' + userData.getLastName()).trim());

        // now send the message.
	try {
	    MailManager.sendTokenMail(userData.getEmailAddress(), messageData);
	} catch (MailAccessException exp) {
	    String err = "Error sending token mail to user " + 
                userData.getUserName();
	    logger.error(err);
	    throw exp;
	}
    }

    public static StatusMessage resendEmail(String name) {
        UserData data = null;
        // is it a user name?
        try {
            data = UserDataHandler.getDataForUsername(name);
            // didn't match username, so check email
            if (data == null) data = UserDataHandler.getDataForEmail(name);
            if (data == null) return new StatusMessage("Sorry, but \"" + name
                    +"\" is not a known login name or registered email address.", false);
            if (data.getStatus() != StatusDataHandler.getRequestStatusId())
                return new StatusMessage("The registration for " + name
                        + " has already been confirmed; a registration email is not necessary.", false);
            Properties msgData = new Properties();
            RegisterUtil.loadUserTags(msgData);
            msgData.setProperty("secret", data.getToken());

            // Note historical artifact: many in the database have their 
            // fullname in the last name field.
            String fullname = data.getFirstName();
            if (fullname == null || fullname.length() == 0)
                fullname = data.getLastName();
            else 
                fullname += " "+data.getLastName();
            msgData.setProperty("fullname", fullname);
            MailManager.sendTokenMail(data.getEmailAddress(), msgData);
            return new StatusMessage("Sent registration confirmation email for "
                    + name + ".", true);
        } catch (DatabaseAccessException e) {
            logger.error(e);
            return new StatusMessage("Error looking up \"" + name + "\": " + e.getMessage(), false, e);
        } catch (MailAccessException e) {
            logger.error(e);
            return new StatusMessage("Error resending email to \"" + name + "\": " + e.getMessage(), false, e);
        }

    }

    public static StatusMessage changePassword(String username, String password, String newpw1, String newpw2) {
        UserData data;
        try {
            data = UserDataHandler.getDataForUsername(username);
            if (data == null) return new StatusMessage("Sorry, but \"" + username
                    + "\" is not a known login name.", false);
            if (password == null || password.length() == 0)
                return new StatusMessage("Please enter your original password.", false);
            else if (!UserDataHandler.passwordMatches(data, password))
                return new StatusMessage("Sorry, but the password you entered does not match your original password.  To recover your original password, please use the email request form below.", false);
            else { // old password matches
                if (newpw1 == null || !newpw1.equals(newpw2))
                    return new StatusMessage("Sorry, but the two new passwords do not match.  Please enter them again.", false);
                else {
                    try {
                        data.validatePassword(newpw1); // validates password strength
                        UserDataHandler.setUserPassword(username, newpw1);
                        return new StatusMessage("Your password has been updated.", true);
                    } catch (UserRegistrationException e) {
                        return new StatusMessage(e.getMessage(), false, e);
                    }
                }
            }
        } catch (DatabaseAccessException e) {
            logger.error(e);
            return new StatusMessage("Internal error updating password.", false, e);
        }
    }

    /** Remind the named user of their password.
     *  @param user either email address or username
     *  @return null if successful, or if there was a problem, a description of the error */
    public static StatusMessage remindPassword(String user, StringBuffer url) {
        UserData data = null;
        // is it a user name?
        try {

            data = UserDataHandler.getDataForUsername(user);
            // didn't match username, so check email
            if (data == null) data = UserDataHandler.getDataForEmail(user);
            if (data == null) return new StatusMessage("Sorry, but \"" + user
                    +"\" is not a known login name or registered email address.", false);
            SecureRandom random = new SecureRandom();
            String tempPasswd = new BigInteger(130, random).toString(32);
            UserDataHandler.setUserPassword(data.getUserName(), tempPasswd);

            data = UserDataHandler.getDataForUsername(data.getUserName());
            if (data == null) return new StatusMessage("Sorry, but \"" + user
                    +"\" is not a known login name or registered email address.", false);
            Properties msgData = new Properties();
            // bug in user registration: last name is full name
            msgData.setProperty("fullname", data.getLastName());
            // String pw = data.getPassword();
            // if (pw == null) return new StatusMessage("Password is null.", false, new Exception()); // shouldn't happen
            //msgData.setProperty("password", data.getPassword());
            msgData.setProperty("password", tempPasswd);
            msgData.setProperty("username", data.getUserName());
            msgData.setProperty("passwordchangeform", url.toString());
            MailManager.sendPasswordReminderMail(data.getEmailAddress(), msgData);
            String msg = "Sent password reminder to "
                    + (user.equals(data.getEmailAddress()) ? "" : "email address registered for ")
                    + user + ".";
            return new StatusMessage(msg, true);
        } catch (DatabaseAccessException e) {
            logger.error(e);
            return new StatusMessage("Error looking up \"" + user + "\": " + e.getMessage(), false, e);
        } catch (MailAccessException e) {
            logger.error(e);
            return new StatusMessage("Error sending email to \"" + user + "\": " + e.getMessage(), false, e);
        }
    }
    
    /**
     * In case of external CA interaction this method registers the user
     * Generates token and certificate request, and places the user in database.
     * Email is sent to user with token
     *
     * @param userData
     *        Data type representing user who needs to be registred.
     * 
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     * @exception <code>UserRegistrationException</code>
     *            If any error that could be exposed to the client.
     */
    public static void enrollUser(UserData userData) 
	throws RegistrationException 
    {
        enrollUser(userData, null);
    }

    /**
     * Registers the user by placing the user in the user database and creates 
     * certificates in need of approval by a CA administrator.  Approval is 
     * requested by sending email to the CA administrator.
     * To confirm that the registered email address belongs to the person
     * registering, email is sent to user with a token (generating internal
     * to the method and cached in the database); the user must respond to 
     * a validation service with that token before a certificate can be 
     * generated.  In order to complete the registration and make certificates
     * available for use, the user must complete the email validation (via 
     * acceptUser()) <em>and</em> the CA administrator must approve the request 
     * (via approveUser()).  <p>
     *
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email messages sent to the 
     * user and the CA administrator.  This is accomplished via templates
     * identified by the sendTokenTemplate and caAdmtemplate configuration 
     * properties (in the purse.properties file).  These templates can contain
     * tags of the form <code>@<it>tagname</it>@</code> that locate the 
     * places in the message where the information is to be inserted.  
     * The <code>messageData</code> Properties parameter provides the mapping 
     * of <it>tagname</it> to actual data.  Thus, the set of tags supported 
     * is completely up to the author of the templates and the caller of 
     * this method.  <p>
     *
     * This implementation will add certain tag definitions to the messageData
     * Properties if they are not already defined, set to default values; these 
     * include:
     * <pre>
     *   fullname       the first and last name of the registrant drawn from 
     *                    the userData object.
     *   token          a URL that the user should access validate his/her
     *                    registration.  The internally generated token is 
     *                    embedded in this URL.  <it>Note: this tag definition
     *                    is defined for backward compatability.</it>
     *   secret         the actual value of the token.  
     * </pre>
     * The template is not required to use any of these; they are set to 
     * support the default templates provided by PURSE.  Their values and 
     * intended use can be overridden by the mappings in the messageData
     * and use in the template.  
     *
     * @param userData
     *        Data type representing user who needs to be registred.
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into confirmation request sent by email to the user.  If null,
     *        a default set of properties will used based on the information in 
     *        the userData parameter (see description above).  
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     * @exception <code>UserRegistrationException</code>
     *            If any error that could be exposed to the client.
     */
    public static void enrollUser(UserData userData, Properties messageData) 
	throws RegistrationException 
    {
	if (userData == null) {
	    String err = "User data cannot be null";
	    logger.error(err);
	    throw new RegistrationException(err);
	}

	String userNameExistsErr = "User name " + userData.getUserName() 
	    + " already exists";
	String errorMsg = "Error verifying if specified username "
	    + "already exists.";
	// Check if the user name has already been chosen by someone whose
	// request is in process.
	try {
	    if (UserDataHandler.userNameExists(userData.getUserName())) {
		logger.error(userNameExistsErr);
		throw new UserRegistrationException(userNameExistsErr);
	    }
	} catch (DatabaseAccessException exp) {
	    logger.error(errorMsg);
	    throw new RegistrationException(errorMsg, exp);
	} 

	// Generate token
	String token = new UniqueToken(System.currentTimeMillis(), 
				       new Random().nextLong()).toString();
	userData.setToken(token);
	logger.debug("Token is " + token);

        String usrPassToRmv = userData.passwordSha1;
        userData.passwordSha1 = UserDataHandler.passwordSha1("dummy"); // can be encrypted and saved in userData
 	
	//  set status as requested.
	int reqStatus = StatusDataHandler.getRequestStatusId();
	userData.setStatus(reqStatus);

	// Store details in database.	
	try {
	    UserDataHandler.storeData(userData);
	} catch (DatabaseAccessException exp) {
	    String err = "Error storing data.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	} 

	// Generate certificates for this user
	String certDir = null;
	try {
	    certDir = UserCertificateGeneration.generate(userData.getUserName(),
                                                         usrPassToRmv);
	} catch (CertificateGenerationException certExp) {
	    String err = "Error creating credentials for the user";
	    logger.error(err);
	    throw new RegistrationException(err, certExp);
	}
	
	// Get ready to send emails by setting default messages tags.
        // The default token tag will be set by the MailManager class below.
        // Note that we need to use "secret" as the token tag due to some 
        // previously inconsistant use of a "token" across messages; this 
        // will allow backward compatibility with old templates.  
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("secret") == null) 
            messageData.setProperty("secret", token);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                       userData.getFirstName() + ' ' + userData.getLastName());

	// Send mail with token
	try {
	    MailManager.sendTokenMail(userData.getEmailAddress(), messageData);
	} catch (MailAccessException exp) {
	    String err = "Error sending token mail to User.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}

	// Send mail to Admin 
	try {
	    MailManager.sendAdmMailNotification(messageData);
	} catch (MailAccessException exp) {
	    String err = "Error sending token mail to Admin.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}

    }

    /**
     * Method invoked by mail retrieval code to process the user's reply
     *
     * @param email
     *        Email contents as string
     * @param emailFromAddress
     *        Email address from which the mail was received
     * This method does not throw any exceptions. The errors are logged
     * and depending on scenario, mail is sent to an administrator and/or
     * user.
     */
    
    public static void mailHandler(Message email, String emailFromAddress) {
        
        String emailContents = null;
        
        try{
            emailContents = email.getContent().toString().trim();
        }catch(Exception eio){
            logger.error(eio);
        }
        
        String token = MailManager.getMailToken(emailContents);
        token = token.trim();
        int result = MailManager.processMail(email, emailFromAddress);
        if (result == 1) // Mail From CA
            try {
                approveUser(emailContents);
            }catch(RegistrationException e) {
                logger.error("Error : Signing user certificate ");    
            }
            else if (result==2) { // MAil from Admin
                try{ 
                    confirmUser(token);
                } catch (RegistrationException exp) {
                    logger.error("Error setting user status.");
                }
                try {
                    int pendingId = StatusDataHandler.getId(RegisterUtil.getPendingStatus());
                    UserDataHandler.setStatus(token, pendingId);
                } catch (DatabaseAccessException exp) {
                    logger.error("Error setting user status.");
                } catch (RegistrationException exp) {
                    logger.error("Error setting user status.");
                }
            }else if (result == -1)
                logger.error("");
    }


    /**
     * Method that sends out mail to user to confirm that request has been 
     * accepted. Prior to that, a user certificate is generated and signed
     * by CA and stored in MyProxy server. This method is invoked by
     * RA or CA
     * 
     * @param token
     *        Token of the user whose registration was accepted
     * @param caPassPhrase
     *        Passphrase of the ca used to sign the certificates generated for
     *        the user.
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */    
    public static void acceptUser(String token, String caPassPhrase) 
         throws RegistrationException 
    {
        acceptUser(token, caPassPhrase, null);
    }

    /**
     * Method that sends out mail to user to confirm that request has been 
     * accepted. Prior to that, a user certificate is generated and signed
     * by CA and stored in MyProxy server.
     * 
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email message.  This is 
     * accomplished via a template identified by the caAcceptTemplate.  
     * This template contains tags of the form <code>@<it>tagname</it>@</code> 
     * that locate the places in the message where the information is to 
     * be inserted.  The <code>messageData</code> Properties parameter 
     * provides the mapping of <it>tagname</it> to actual data.  Thus, the
     * tags support is completely up to the author of the template and the 
     * caller of this method.  <p>
     *
     * This implementation will add certain tag definitions to the messageData
     * Properties if they are not already defined, set to default values; these 
     * include:
     * <pre>
     *   fullname       the first and last name of the registrant drawn from 
     *                    the userData object.
     * </pre>
     * The template is not required to use any of these; they are set to 
     * support the default templates provided by PURSE.  Their values and 
     * intended use can be overridden by the mappings in the messageData
     * and use in the template.  
     *
     * @param token
     *        Token of the user whose registration was accepted
     * @param caPassPhrase
     *        Passphrase of the ca used to sign the certificates generated for
     *        the user.
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into the acceptance notice sent by email to the user.  If null,
     *        a default set of properties will used based on the user's 
     *        registration information (see description above).
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */    
    public static void acceptUser(String token, String caPassPhrase, 
                                  Properties messageData) 
        throws RegistrationException, MailAccessException
    {
        String paramErr = " cannot be null.";
        if (token == null) {
            logger.debug("Token " + paramErr);
            throw new RegistrationException("Token " + paramErr);
        }
        
        if (caPassPhrase == null) {
            logger.debug("CA pass phrase " + paramErr);
            throw new RegistrationException("CA pass phrase " + paramErr);
        }
        
        logger.debug("Accept user " + token);
        UserData userData;
        // Retrieve data from db 
        try {
            userData = UserDataHandler.getData(token);
        } catch (DatabaseAccessException exp) {
            String err = "Could not retrieve data from db";
            logger.error(err, exp);
            throw new RegistrationException(err, exp);
        }
        // Check if user data is null
        if (userData == null) {
            String err = "Unrecognized registration token: \"" + token + "\".";
            logger.error(err);
            throw new UnknownTokenException(err);
        }

        if (!UserCertificateGeneration.isMyProxyCA()) {
            // Generate certificates
            String certDir =
                RegisterUtil.generateUserCerts(userData, caPassPhrase);

            // Store certificates.
            RegisterUtil.storeUserCerts(userData, certDir);

            // Set the user DN
            try {
                String dn = UserCertificateGeneration.getDN(certDir);
                UserDataHandler.setUserDN(token, dn);
            } catch (CertificateGenerationException exp) {
                String err = "Error setting user DN";
                logger.error(err);
                throw new RegistrationException(err, exp);
            }

            // Delete local certificates.
            RegisterUtil.deleteCerts(certDir);
        }
        
        // Set status as accepted.
        RegisterUtil.setUserStatusAsAccepted(token);

        // Get ready to send mail to user
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                       (userData.getFirstName() + ' ' + userData.getLastName()).trim());

        if (messageData.getProperty("username") == null)
                messageData.setProperty("username", userData.getUserName());
        
        // Now send the mail to user that all is set.
        try {
            MailManager.sendAcceptMail(userData.getEmailAddress(), messageData);
        } catch (MailAccessException exp) {
            String err = "Error sending CA accept mail to user" + 
                userData.getUserName();
            logger.error(err);
            throw exp;
        }
    }

    /**
     * Method that sends out mail to user to confirm that request has been 
     * accepted. This method is invoked when a user already has a certificate
     * 
     * @param token
     *        Token of the user whose registration was accepted
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */    
    public static void acceptUser(String token) 
         throws RegistrationException 
    {
        acceptUser(token, (Properties) null);
    }

    /**
     * Method that sends out mail to user to confirm that request has been 
     * accepted. This method is invoked when a user already has a certificate
     * 
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email message.  This is 
     * accomplished via a template identified by the proxyUploadTemplate PURSE 
     * configuration property (in the purse.properties file).  
     * This template contains tags of the form <code>@<it>tagname</it>@</code> 
     * that locate the places in the message where the information is to 
     * be inserted.  The <code>messageData</code> Properties parameter 
     * provides the mapping of <it>tagname</it> to actual data.  Thus, the
     * tags support is completely up to the author of the template and the 
     * caller of this method.  <p>
     *
     * This implementation will add certain tag definitions to the messageData
     * Properties if they are not already defined, set to default values; these 
     * include:
     * <pre>
     *   fullname       the first and last name of the registrant drawn from 
     *                    the userData object.
     * </pre>
     * The template is not required to use any of these; they are set to 
     * support the default templates provided by PURSE.  Their values and 
     * intended use can be overridden by the mappings in the messageData
     * and use in the template.  
     * 
     * @param token
     *        Token of the user whose registration was accepted
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into upload request sent by email to the user.  If null,
     *        a default set of properties will used based on the user's 
     *        registration information (see description above).
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */    
    public static void acceptUser(String token, Properties messageData) 
         throws RegistrationException 
    {
        String paramErr = " cannot be null.";
        if (token == null) {
            logger.debug("Token " + paramErr);
            throw new RegistrationException("Token " + paramErr);
        }

        logger.debug("Accept user " + token);
        UserData userData;
        // Retrieve data from db 
        try {
            userData = UserDataHandler.getData(token);
        } catch (DatabaseAccessException exp) {
            String err = "Could not retrieve data from db";
            logger.error(err, exp);
            throw new RegistrationException(err, exp);
        }
        // Check if user data is null
        if (userData == null) {
            String err = "A user with token " + token + " does not exist in"
            + " database";
            logger.error(err);
            throw new RegistrationException(err);
        }
        
        // Set status as accepted.
        RegisterUtil.setUserStatusAsAccepted(token);
        
        // Get ready to send mail to user
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                       userData.getFirstName() + ' ' + userData.getLastName());
        
        // Send mailto user that all is set.
        try {
            MailManager.sendProxyUploadMail(userData.getEmailAddress(), 
                                            messageData);
        } catch (MailAccessException exp) {
            String err = "Error sending CA accept mail.";
            logger.error(err);
            throw new RegistrationException(err, exp);
        }
    }

    
    /**
     * Request a signature on a certificate from the external CA.  This 
     * method should be called in response to a user who has confirmed their 
     * email address.  This will send the cert request to the CA by email. 
     * (If an external CA is not being used, call either acceptUser() instead.)
     * This method assumes that the certificate has already been generated 
     * (in the form of a cert-request file).  
     * 
     * @param token
     *        Token of the user whose registration was accepted
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */
    public static void confirmUser(String token) 
	throws RegistrationException 
    {
        confirmUser(token, null);
    }

    /**
     * Request a signature on a certificate from the external CA.  This 
     * method should be called in response to a user who has confirmed their 
     * email address.  This method will send the cert request to the CA by 
     * email.  (If an external CA is not being used, call either acceptUser() 
     * or processUserResponse() instead.)
     * This method assumes that the certificate has already been generated 
     * (in the form of a cert-request file).  
     * 
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email message.  This is 
     * accomplished via a template identified by the caTemplate PURSE 
     * configuration property (in the purse.properties file).  
     * This template contains tags of the form <code>@<it>tagname</it>@</code> 
     * that locate the places in the message where the information is to 
     * be inserted.  The <code>messageData</code> Properties parameter 
     * provides the mapping of <it>tagname</it> to actual data.  Thus, the
     * tags support is completely up to the author of the template and the 
     * caller of this method.  <p>
     *
     * This implementation will add certain tag definitions to the messageData
     * Properties if they are not already defined, set to default values; these 
     * include:
     * <pre>
     *   fullname       the first and last name of the registrant drawn from 
     *                    the userData object.
     *   secret         the token sent to the user which they use to confirm
     *                    their email address
     *   certrequest    the contents of the the usercert_request.pem file 
     *                    that was created for the user
     *   token          the secret token (see above) appended by 2 carriage 
     *                    returns and the value of certrequest.  This is 
     *                    provided for backward compatibility with previous 
     *                    versions of PURSE.
     * </pre>
     * The template is not required to use any of these; they are set to 
     * support the default templates provided by PURSE.  Their values and 
     * intended use can be overridden by the mappings in the messageData
     * and use in the template.  
     * 
     * @param token
     *        Token of the user whose registration was accepted
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into signature request sent by email to the CA.  If null,
     *        a default set of properties will used based on the user's 
     *        registration information (see description above).
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */
    public static void confirmUser(String token, Properties messageData) 
	throws RegistrationException 
    {
	String paramErr = " cannot be null.";
	if (token == null) {
	    logger.debug("Token " + paramErr);
	    throw new RegistrationException("Token " + paramErr);
	}
	
	logger.debug("Confirm user " + token);
	UserData userData;
	
	// Retrieve data from db 
	try {
	    userData = UserDataHandler.getData(token);
	} catch (DatabaseAccessException exp) {
	    String err = "Could not retrieve data from db";
	    logger.error(err, exp);
	    throw new RegistrationException(err, exp);
	}
	
	// Check if user data is null
	if (userData == null) {
	    String err = "A user with token " + token + " does not exist in"
		+ " database";
	    logger.error(err);
	    throw new RegistrationException(err);
	}

	// Get access to location of usercert request
	
	String certDir = 
            UserCertificateGeneration.getUserCertLocation(userData.userName);

        // Get ready to send mail to CA.  The "token" default is set to 
        // provide backward compatibility with previous versions of purse.
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                       userData.getFirstName() + ' ' + userData.getLastName());
        if (messageData.getProperty("secret") == null) 
            messageData.setProperty("secret", token);
        if (messageData.getProperty("certrequest") == null) 
            messageData.setProperty("certrequest",
                 RegisterUtil.readFromFile(certDir + "/usercert_request.pem"));
        if (messageData.getProperty("token") == null) 
            messageData.setProperty("token", token + "\n \n" + 
                                    messageData.getProperty("certrequest"));

	// Send CA mail
	try {
	    MailManager.sendSignedCAMail(messageData);
	} catch (MailAccessException exp) {
	    String err = "Could not send CA email for user " 
		+ userData.getUserName()  + " with email address"
		+ userData.getEmailAddress();
	    logger.error("Could not send CA email", exp);
	    try {
		MailManager.sendAdminMail(err);
	    } catch (MailAccessException mailExp) {
		logger.error("Could not send admin error mail", mailExp);
	    }
	}
	
    }

    /**
     * Complete a registration in response to CA approval by uploading the 
     * signed certificate to the Myproxy server.  This method also removes
     * the directory that contains user's certificate.  Email is sent to the 
     * user letting them know that registration is complete.  
     * @param emailContents
     *        Email contains signed certificate from external CA 
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */   
    public static void approveUser(String emailContents)
         throws RegistrationException
    {
        approveUser(emailContents, null);
    }

    /**
     * Complete a registration in response to CA approval by uploading the 
     * signed certificate to the Myproxy server.  This method also removes
     * the directory that contains user's certificate.  Email is sent to the 
     * user letting them know that registration is complete.  
     *
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email message.  See documentation
     * for acceptUser for details.  
     * 
     * @param emailContents
     *        Email contains signed certificate from external CA 
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into the acceptance sent by email to the user.  If null,
     *        a default set of properties will used based on the user's 
     *        registration information (see description above).
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */   
    public static void approveUser(String emailContents, Properties messageData)
         throws RegistrationException
    {
        
        X509Certificate cert;
        
        try
        {	
            InputStream is = new ByteArrayInputStream(emailContents.getBytes());
            cert = RegisterUtil.loadUserCert(is);
        }
        catch (Exception e)
        {
            System.out.println("error reading certifcate from email content " + 
                               e.toString());
            return;
        }
	
        logger.debug("Reading X509 Certificate:");	
        logger.debug("   Subject DN: " + cert.getSubjectDN());

        //---->	Get the user name from the certificate ;
        String subjectDN = cert.getSubjectDN().toString();
        String userName = subjectDN.substring(subjectDN.indexOf("DN=")+4,
                                              subjectDN.indexOf(","));
        logger.debug(userName);
        UserData userData;
        
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
            String err = "A user with username " + userName + 
                         " does not exist in database";
            logger.error(err);
            throw new RegistrationException(err);
        }
        
        //---->	String certDir = Get the proper location of cert according to the username

        String certDir = 
            UserCertificateGeneration.getUserCertLocation(userData.userName);
        
        //---->	Put the signed cert in proper location; 
        String fileName = certDir+"/"+"usercert.pem";
        try
        {
            FileOutputStream fos = new FileOutputStream(fileName,true);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeBytes(emailContents);
            dos.flush();
            dos.close();
            fos.close();
        }
        catch (Exception e) 
        { 
            logger.error(e + ": writing String to file " + fileName);
        }	
        
        /*
        
        // SUDO should be added
        
        */
        
        // Upload creds to MyProxy
        try {
            MyProxyManager.storeCredential(userData.getUserName(), certDir);
        } catch (MyProxyAccessException myProxyExp) {
            String err = "Error storing credential for user";
            logger.error(err);
            throw new RegistrationException(err, myProxyExp);
        }
        
        // Set the user DN
        try {
            String dn = UserCertificateGeneration.getDN(certDir);
            UserDataHandler.setUserDN(userData.getToken(), dn);
        } catch (CertificateGenerationException exp) {
            String err = "Error setting user DN";
            logger.error(err);
            throw new RegistrationException(err, exp);
        }
        
    	RegisterUtil.deleteCerts(certDir);

    	//	 Set status as accepted.
    	RegisterUtil.setUserStatusAsAccepted(userData.getToken());
        
        // Get ready to send mail to user
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                       userData.getFirstName() + ' ' + userData.getLastName());
        
        // Send mailto user that all is set.
        try {
            MailManager.sendAcceptMail(userData.getEmailAddress(), messageData);
        } catch (MailAccessException exp) {
            String err = "Error sending CA accept mail.";
            logger.error(err);
            throw new RegistrationException(err, exp);
        }
    }

    /**
     * Method that sends out mail to user to inform that the request has been
     * rejected.
     *
     * @param token
     *        Token for the user whose registration has been rejected.
     * @param message
     *        Message from the CA, typically explaining the rejection.
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */
    public static void rejectUser(String token, String message) 
	throws RegistrationException 
    {
        rejectUser(token, message, null);
    }

    /**
     * Method that sends out mail to user to inform that the request has been
     * rejected.
     *
     * @param token
     *        Token for the user whose registration has been rejected.
     * @param reason
     *        Message from the CA, typically explaining the rejection.  This 
     *        parameter can be null only if messageData is not null.
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into confirmation request sent by email to the user.  If 
     *        this parameter is provided, the value of the reason parameter 
     *        will be added to mapping with the keyword, "reason"
     *        If this parameter is null, the reason will be simply appended to 
     *        the message template (for backward compatibility).  
     * @exception <code>RegistrationException</code>
     *            If any error that need not be exposed to the client.
     */
    public static void rejectUser(String token, String reason, 
                                  Properties messageData) 
	throws RegistrationException 
    {
	String paramErr = " cannot be null.";
	if (token == null) {
	    logger.debug("Token " + paramErr);
	    throw new RegistrationException("Token " + paramErr);
	}
	
	if (reason == null && messageData == null) {
	    logger.debug("Message " + paramErr);
	    throw new RegistrationException("Message " + paramErr);
	}

	logger.debug("Reject user " + token);
	UserData userData;
	// Retrieve data from db 
	try {
	     userData = UserDataHandler.getData(token);
	} catch (DatabaseAccessException exp) {
	    String err = "Could not retrieve data from db";
	    logger.error(err, exp);
	    throw new RegistrationException(err, exp);
	}
	// Check if user data is null
	if (userData == null) {
	    String err = "Such a user does not exist in the database";
	    logger.error(err);
	    throw new RegistrationException(err);
	}
	
	// generate pointer to the location of pre-generated user certificates

	String certDir = 
            UserCertificateGeneration.getUserCertLocation(userData.userName);

        // Delete the certificates
        File certDirFile = new File(certDir);
        File listOfFiles[] = certDirFile.listFiles();
        if (listOfFiles != null) {
            for (int i=0; i<listOfFiles.length; i++) {
                listOfFiles[i].delete();
            }
        }
        certDirFile.delete();

        if (messageData != null) {
            if (reason != null && messageData.getProperty("reason") == null) 
                messageData.setProperty("reason", reason);
            if (messageData.getProperty("fullname") == null) 
                messageData.setProperty("fullname", 
                       userData.getFirstName() + ' ' + userData.getLastName());
        }
	
	// Send mailto user that all is set.
	try {
            if (messageData == null) 
                // for backward compatibility to old template mechanism
                MailManager.sendRejectMail(userData.getEmailAddress(), reason);
            else 
                MailManager.sendRejectMail(userData.getEmailAddress(), 
                                           messageData);
	} catch (MailAccessException exp) {
	    String err = "Error sending CA reject mail.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}

	// Set user status as rejcted
	try {
	    int rejectedId = 
		StatusDataHandler.getId(RegisterUtil.getRejectedStatus());
	    UserDataHandler.setStatus(token, rejectedId);
	} catch (DatabaseAccessException exp) {
	    String err = "Error setting user status.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	} 
    }
    
    /**
     * Request a signature on a certificate from the "internal" CA.  This 
     * method should be called in response to a user who has confirmed their 
     * email address.  This method will send the CA administrator an email 
     * requesting action.  
     * (If an human approval is not needed, call acceptUser() instead; if an 
     * external CA is used processUserResponse() instead.)
     * This method assumes that the certificate has already been generated.
     * 
     * In this method, when errors are seen a mail is sent to a
     * configured administrator address.
     *
     * @param token
     *        Token being processed.
     */
    public static void processUserResponse(String token) 
	throws RegistrationException 
    {
        processUserResponse(token, null);
    }

    /**
     * Request a signature on a certificate from the "internal" CA.  This 
     * method should be called in response to a user who has confirmed their 
     * email address.  This method will send the CA administrator an email 
     * requesting action.  
     * (If an human approval is not needed, call acceptUser() instead; if an 
     * external CA is used processUserResponse() instead.)
     * This method assumes that the certificate has already been generated.
     * 
     * In this method, when errors are seen a mail is sent to a
     * configured administrator address.
     *
     * This version of the method allows the caller to provide specific 
     * information that should appear in the email message.  This is 
     * accomplished via a template identified by the caTemplate PURSE 
     * configuration property (in the purse.properties file).  
     * This template contains tags of the form <code>@<it>tagname</it>@</code> 
     * that locate the places in the message where the information is to 
     * be inserted.  The <code>messageData</code> Properties parameter 
     * provides the mapping of <it>tagname</it> to actual data.  Thus, the
     * tags support is completely up to the author of the template and the 
     * caller of this method.  <p>
     *
     * This implementation will add certain tag definitions to the messageData
     * Properties if they are not already defined, set to default values; these 
     * include:
     * <pre>
     *   fullname       the first and last name of the registrant drawn from 
     *                    the userData object.
     *   secret         the token sent to the user which they use to confirm
     *                    their email address
     *   certrequest    the contents of the the usercert_request.pem file 
     *                    that was created for the user
     *   token          the secret token (see above) appended by 2 carriage 
     *                    returns and the value of certrequest.  This is 
     *                    provided for backward compatibility with previous 
     *                    versions of PURSE.
     * </pre>
     * The template is not required to use any of these; they are set to 
     * support the default templates provided by PURSE.  Their values and 
     * intended use can be overridden by the mappings in the messageData
     * and use in the template.  
     * 
     * @param token
     *        Token being processed.
     * @param messageData
     *        a mapping of tokens to specific data that should be inserted 
     *        into CA signature request sent by email to the CA.  If null,
     *        a default set of properties will used based on the user's 
     *        registration information (see description above).
     *
     */
    public static void processUserResponse(String token, Properties messageData)
	throws RegistrationException 
    {
	logger.debug("Processing user response");
	if (token == null) {
	    String err = "Error: Token cannot be null ";
	    logger.error(err);
	    throw new RegistrationException(err);
	}


	UserData userData;
	try {
	    userData = UserDataHandler.getData(token);
	} catch (DatabaseAccessException exp) {
	    String err = "Error retrieving data for token " + token;
	    logger.error(err, exp);
	    // send mail to admin and then throw an exception.
	    try {
		MailManager.sendAdminMail(err);
	    } catch (MailAccessException mailExp) {
		logger.error("Could not send admin error/user mail", mailExp);
	    }
	    throw new RegistrationException(err, exp);
	}
	// This scenario in most cases could be bad URL by user,
	// so sending mail to admin may not be needed. But for now,
	// sending mail to admin in addition to throwing an exception.
	if (userData == null) {
	    String err = "Confirmation request received with token: " + token 
		+ ", but no prior request with this taken was made.";
	    logger.error(err);
	    try {
		MailManager.sendAdminMail(err);
	    } catch (MailAccessException mailExp) {
		logger.error("Could not send admin error/user mail", mailExp);
	    }
	    throw new RegistrationException(err);
	} 

	// Check if the same token request was rejected.
	int rejectedId = 
	    StatusDataHandler.getId(RegisterUtil.getRejectedStatus());
	if (userData.getStatus() == rejectedId) {
	    String err = "Request with token " + token 
		+ " has already been rejected. An attempt to register using"
		+ " this token was made.";
	    logger.error(err);
	    try {
		MailManager.sendAdminMail(err);
	    } catch (MailAccessException mailExp) {
		logger.error("Could not send admin error/user mail", mailExp);
	    }
	    throw new RegistrationException(err);
	}

	// Set user status as pending.
	try {
	    int pendingId = 
		StatusDataHandler.getId(RegisterUtil.getPendingStatus());
	    UserDataHandler.setStatus(token, pendingId);
	} catch (DatabaseAccessException exp) {
	    String err = "Error setting user status.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	}

        // Get ready to send mail to CA.  The "token" default is set to 
        // provide backward compatibility with previous versions of purse.
        if (messageData == null) messageData = new Properties();
	RegisterUtil.loadUserTags(messageData);
        if (messageData.getProperty("fullname") == null) 
            messageData.setProperty("fullname", 
                       userData.getFirstName() + ' ' + userData.getLastName());
        if (messageData.getProperty("secret") == null) 
            messageData.setProperty("secret", token);
        if (messageData.getProperty("token") == null) 
            messageData.setProperty("token", token);

        // (a) if raId for user is -1, no RA conigured, send mail to
        // CA for approval.
        // (b) if raId is set, send mail to configured RA for approval

        int raId = userData.getRaId();

        if (raId == -1) { 
            // Send CA mail
            try {
                MailManager.sendCAMail(messageData);
            } catch (MailAccessException exp) {
                String err = "Could not send CA email for user " 
                    + userData.getFirstName()  + " " + userData.getLastName();
                logger.error("Could not send CA email", exp);
                throw new RegistrationException(err, exp);
            }
        } else {
            String raEmail = RADataHandler.getEmailAddress(raId);
            if (raEmail == null) {
                String err = "Error getting RA Email for id " + raId;
                logger.error(err);
                throw new RegistrationException(err);
            }
            
            // send mail to ra
            try {
                MailManager.sendRATokenMail(raEmail, token);
            } catch (MailAccessException exp) {
                String err = "Error sending token mail to ra with emai "
                    + raEmail;
                logger.error(err);
                throw new RegistrationException(err, exp);
            }
        }
    }
    
    /**
     * Sets the status of the user to said, status id.
     *
     * @param userId 
     *        User id for which status needs to be changed
     * @param statusId
     *        The status id that the user's status needs to be set as
     * @exception <code>RegistrationException</code>
     *            If any error occurs
     */
    public static void setUserStatus(int userId, int statusId) 
	throws RegistrationException {
	
	UserDataHandler.setStatus(userId, statusId);
    }
}

