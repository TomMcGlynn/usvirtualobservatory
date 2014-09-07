/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import java.security.Security;

import org.globus.purse.exceptions.UserRegistrationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sets the mail configuration options
 */
public class MailOptions {

    static Log logger =
	LogFactory.getLog(MailOptions.class.getName());
    
    public final static String TOKEN_PATTERN = "@token@";
    public final static String URL_PATTERN = "@url@";
    public final static String URL_PARAM = "token";
    public final static String USER_NAME_PATTERN = "@userName@";
    public final static String TIME_PATTERN = "@time@";
    public final static String RENEW_URL_PATTERN = "@renewUrl@";
    public final static String USER_NAME_PARAM = "username";
    public final static String TOKEN_PREFIX = "Token:";
    public final static String TOKEN_SUFIX = "#";

    String caAddress = null;
    String userAccountAddr = null;
    String incomingHost = null;
    int incomingPort;
    String incomingProtocol = null;
    String outgoingHost = null;
    int outgoingPort;
    String outgoingProtocol = null;
    String passwordReminderTemplateFilename = null;
    String usernameReminderTemplateFilename = null;
    String sendTokenTemplateFilename = null;
    String sendRaTokenTemplateFilename = null;
    String caAcceptTemplateFilename = null;
    String caRejectTemplateFilename = null;
    String caAdmTemplateFileName = null;
    String caTemplateFilename = null;
    String expirationWarnFilename = null;
    String renewTemplateFilename = null;
    String caBaseUrl = null;
    String userBaseUrl = null;
    String renewBaseUrl = null;
    String adminAddr = null;
    String portalBaseUrl = null;
    String subjectLine = null;
    String adminSubjectLine = null;
    String caSubjectLine = null;
    String signerCert = null;
    String signerKey = null;
    String signerPass = null;
    String proxyUploadTemplateFileName = null;
    String raSubjectLine = null;

    /**
     * Constructor 
     */
    public MailOptions(String caAddress, String userAccountAddr, 
		       String incomingHost, int incomingPort,
		       String incomingProtocol, String outgoingHost, 
		       int outgoingPort, String outgoingProtocol,
               String passwordReminderTemplate,
               String usernameReminderTemplate,
               String sendTokenTemplate, String caAcceptTemplate,
		       String caRejectTemplate, String expirationWarnFilename,
		       String renewTemplate, String caBaseUrl, 
		       String userBaseUrl, String renewBaseUrl, 
		       String caTemplateFilename, String adminAddr, 
		       String subjectLine, String adminSubjectLine,
		       String caSubjectLine, String caAdmTemplate, 
		       String portalBaseUrl,
		       String signerCert, String signerKey, String signerPass,
                       String proxyUploadTemplateFileName,
                       String raTokenTemplate, String raSubjectLine_)
	throws UserRegistrationException {
        
    Security.insertProviderAt(
    new org.bouncycastle.jce.provider.BouncyCastleProvider(), 6);        
	
	logger.debug("Options: " + caAddress + " " + userAccountAddr + " "
		     + incomingHost + " " + incomingPort + " "
		     + incomingProtocol + " " + outgoingHost + " "
		     + outgoingPort + " " + outgoingProtocol + " "
             + passwordReminderTemplate + " "
             + usernameReminderTemplate + " "
             + sendTokenTemplate + " " + caAcceptTemplate + " "
		     + caRejectTemplate + " " + expirationWarnFilename + " "
		     + adminAddr + " " + subjectLine + " " + adminSubjectLine 
                     + " " + caSubjectLine + " " + renewBaseUrl + " " 
                     + renewTemplate + " " + caAdmTemplate + " " 
                     + portalBaseUrl + " " + signerCert + " " + signerKey 
                     + " " + signerPass + " " + proxyUploadTemplateFileName
                     + " " + raTokenTemplate + " " + raSubjectLine_);

	if ((caAddress == null) || (caAddress.trim().equals(""))) {
	    String err = "Email address cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.caAddress = caAddress;
	
	if ((userAccountAddr == null) || (userAccountAddr.trim().equals(""))) {
	    String err = "User account name cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.userAccountAddr = userAccountAddr;
	
	if ((incomingProtocol == null) || 
	    (incomingProtocol.trim().equals(""))) {
	    String err = "incoming protocol cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.incomingProtocol = incomingProtocol;

	if ((outgoingProtocol == null) || 
	    (outgoingProtocol.trim().equals(""))) {
	    String err = "outgoing protocol cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.outgoingProtocol = outgoingProtocol;

	if ((incomingHost ==null) || (incomingHost.trim().equals(""))) {
	    String err = "Server host cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.incomingHost = incomingHost;
	
        this.incomingPort = incomingPort;

	if ((outgoingHost ==null) || (outgoingHost.trim().equals(""))) {
	    String err = "Server host cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.outgoingHost = outgoingHost;
	
        this.outgoingPort = outgoingPort;

	if ((sendTokenTemplate ==null) || 
	    (sendTokenTemplate.trim().equals(""))) {
	    String err = "Path to template for sending token cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.sendTokenTemplateFilename = sendTokenTemplate;

	if ((passwordReminderTemplate ==null) ||
	    (passwordReminderTemplate.trim().equals(""))) {
	    String err = "Path to password reminder template cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.passwordReminderTemplateFilename = passwordReminderTemplate;

	if ((usernameReminderTemplate ==null) ||
	    (usernameReminderTemplate.trim().equals(""))) {
	    String err = "Path to username reminder template cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.usernameReminderTemplateFilename = usernameReminderTemplate;

	if ((caAcceptTemplate ==null) ||
	    (caAcceptTemplate.trim().equals(""))) {
	    String err = "Path to template for CA accept email cannot "
		+ " be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.caAcceptTemplateFilename = caAcceptTemplate;

	if ((renewTemplate ==null) || 
	    (renewTemplate.trim().equals(""))) {
	    String err = "Path to template for renewal email cannot "
		+ " be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.renewTemplateFilename = renewTemplate;

	if ((caRejectTemplate ==null) || 
	    (caRejectTemplate.trim().equals(""))) {
	    String err = "Path to template for CA reject email cannot "
		+ " be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.caRejectTemplateFilename = caRejectTemplate;

	if ((expirationWarnFilename ==null) || 
	    (expirationWarnFilename.trim().equals(""))) {
	    String err = "Path to template for renewal warning email cannot "
		+ " be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.expirationWarnFilename = expirationWarnFilename;

	if ((caBaseUrl ==null) || (caBaseUrl.trim().equals(""))) {
	    String err = "CA base URL cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.caBaseUrl = caBaseUrl;

	if ((userBaseUrl ==null) || (userBaseUrl.trim().equals(""))) {
	    String err = "User base URL cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.userBaseUrl = userBaseUrl;

	if ((renewBaseUrl ==null) || (renewBaseUrl.trim().equals(""))) {
	    String err = "Renew base URL cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.renewBaseUrl = renewBaseUrl;
	
	if ((caTemplateFilename ==null) || 
	    (caTemplateFilename.trim().equals(""))) {
	    String err = "CA tempalte filename cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.caTemplateFilename = caTemplateFilename;

	if ((adminAddr ==null) || 
	    (adminAddr.trim().equals(""))) {
	    String err = "Admin email cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
        this.adminAddr = adminAddr;

	if ((subjectLine == null) || (subjectLine.trim().equals(""))) {
	    String err = "Subject line cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.subjectLine = subjectLine;

	if ((adminSubjectLine == null) || 
	    (adminSubjectLine.trim().equals(""))) {
	    String err = "Admin subject line cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.adminSubjectLine = adminSubjectLine;
    
	if ((caSubjectLine == null) || (caSubjectLine.trim().equals(""))) {
	    String err = "caSubject line cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.caSubjectLine = caSubjectLine;
	
	if ((caAdmTemplate ==null) || 
	        (caAdmTemplate.trim().equals(""))) {
	    String err = "Admin tempalte filename cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.caAdmTemplateFileName = caAdmTemplate;
	
	if ((portalBaseUrl ==null) || 
	        (portalBaseUrl.trim().equals(""))) {
	    String err = "Portal base URL cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.portalBaseUrl = portalBaseUrl;	        
	        
	if ((signerKey ==null) || 
	        (signerKey.trim().equals(""))) {
	    String err = "signer key cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.signerKey = signerKey;	        

	if ((signerCert ==null) || 
	        (signerCert.trim().equals(""))) {
	    String err = "Signer Certificate cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.signerCert = signerCert;	        
	
	if ((signerPass ==null) || 
	        (signerPass.trim().equals(""))) {
	    String err = "Signer Password cannot be null";
	    logger.error(err);
	    throw new UserRegistrationException(err);
	}
	this.signerPass = signerPass;	        

        if ((proxyUploadTemplateFileName == null) || 
            (proxyUploadTemplateFileName.trim().equals(""))) {
            String err = "Proxy upload instructions template cannot be null";
            logger.error(err);
            throw new UserRegistrationException(err);
        }
        this.proxyUploadTemplateFileName = proxyUploadTemplateFileName;

        // Can be null if no RA is involved
        this.sendRaTokenTemplateFilename = raTokenTemplate;
        this.raSubjectLine = raSubjectLine_;
    }

    public String getRATokenSubjectLine() {
        return this.raSubjectLine;
    }

    public String getRATokenTemplateFilename() {
        return this.sendRaTokenTemplateFilename;
    }

    /** Some admin address to which mail will be sent if there is some error*/
    public String getAdminAddress() {
	
	return this.adminAddr;
    }

    /** Address of CA to send mail to for accept/reject decisions */
    public String getCaAddress() {

        return this.caAddress;
    }

    /** Portal user account that is used to send mail to user */
    public String getUserAccountAddr() {

        return this.userAccountAddr;
    }
    
    /** Email template that contains link to jnlp for uploading 
    *   user's proxy using myproxy
    */
    public String getProxyUploadTemplateFileName() {
     
        return this.proxyUploadTemplateFileName;
    }

    /** Mail server host for incoming mails */
    public String getIncomingHost() {

        return this.incomingHost;
    }
    
    /** Mail server port for incoming mails */
    public int getIncomingPort() {

        return this.incomingPort;
    }

    /** Mail server host for outgoing mails */
    public String getOutgoingHost() {

        return this.outgoingHost;
    }

    /** Mail server port for outgoing mails */    
    public int getOutgoingPort() {

        return this.outgoingPort;
    }

    /** Template for password reminder email */
    public String getPasswordReminderFilename() {

        return this.passwordReminderTemplateFilename;
    }

    /** Template for username reminder email */
    public String getUsernameReminderFilename() {

        return this.usernameReminderTemplateFilename;
    }

    /** Template files for token email sent to user */
    public String getSendTokenTemplateFilename() {

        return this.sendTokenTemplateFilename;
    }

    /** Template files for  email sent to user on a CA accept */
    public String getCaAcceptTemplateFilename() {

        return this.caAcceptTemplateFilename;
    }
    
    /** Template files for  email sent to user on a CA reject */
    public String getCaRejectTemplateFilename() {

        return this.caRejectTemplateFilename;
    }

    /** Template files for  email sent to user for renewal */
    public String getExpirationTemplateFilename() {

        return this.expirationWarnFilename;
    }

    /** Base URL for to which token is appended and sent to CA
     * to access user data.*/
    public String getCABaseUrl() {

        return this.caBaseUrl;
    }

    /** Base URL for to which token is appended and sent to user for 
     * confiramtion*/
    public String getUserBaseUrl() {

        return this.userBaseUrl;
    }

    /** Base URL for to which userName is appended and sent to user for 
     * renewal */
    public String getRenewBaseUrl() {

        return this.renewBaseUrl;
    }

    /** Template files for  email sent to CA for review */
    public String getCaTemplateFilename() {

        return this.caTemplateFilename;
    }

    /** Protocol for outgoing mails */
    public String getOutgoingProtocol() {

        return this.outgoingProtocol;
    }

    /** Protocol for incoming mails */
    public String getIncomingProtocol() {

        return this.incomingProtocol;
    }

    /** Subject line */
    public String getSubjectLine() {

	return this.subjectLine;
    }

    /** Admin Subject line */
    public String getAdminSubjectLine() {

	return this.adminSubjectLine;
    }

    /** CA Subject line */
    public String getCASubjectLine() {

	return this.caSubjectLine;
    }

    /** Admin Mail Template file */
    public String getAdmTemplateFilename() {
        return this.caAdmTemplateFileName;
    }
    
    /** Base URL for portal to which token is appended and sent to CA
     * to access user data.*/
    public String getPortalBaseUrl() {

        return this.portalBaseUrl;
    }

    /** Base URL for portal to which token is appended and sent to CA
     * to access user data.*/
    public String getSignerCert() {

        return this.signerCert;
    }

    /** Base URL for portal to which token is appended and sent to CA
     * to access user data.*/
    public String getSignerKey() {

        return this.signerKey;
    }

    /** Base URL for portal to which token is appended and sent to CA
     * to access user data.*/
    public String getSignerpass() {

        return this.signerPass;
    }

    
    public String geRenewalTemplateFilename() {

	return this.renewTemplateFilename;
    }
    
}
