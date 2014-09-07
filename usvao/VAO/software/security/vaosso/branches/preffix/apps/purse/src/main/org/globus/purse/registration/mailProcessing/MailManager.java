/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.MailAccessException;
import org.globus.purse.registration.RegisterUtil;
import org.globus.purse.registration.UserData;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.gsi.OpenSSLKey;
import org.globus.gsi.bc.BouncyCastleOpenSSLKey;

/**
 * Class to handle sending various types of emails relevant to 
 * user registration
 */
public class MailManager {

    static Log logger =
	LogFactory.getLog(MailManager.class.getName());

    static MailOptions mailOptions = null;
    private static Session session = null;
    private static boolean initialized = false;
    static MessageComposerFactory composerFactory = null;

    /**
     * This method initializes the mail server properties this class uses.
     * This need to be called prior to using any other methods in this class.
     *
     * @param <code>MailOptions</code>
     *        Object for initializing objects for receiving/sending mail
     */
    public static void initialize(MailOptions mailOpts) {

	if (!initialized) {
	    mailOptions = mailOpts;
	    // Create new properties
	    Properties props = new Properties();
	    props.put("mail.host", mailOptions.getOutgoingHost());
	    props.put("mail.port", 
		      Integer.toString(mailOptions.getOutgoingPort()));
	    props.put("mail.transport.protocol", 
		      mailOptions.getOutgoingProtocol());
	    session = Session.getDefaultInstance(props, null);

            composerFactory = new MessageComposerFactory(mailOptions);

	    initialized = true;
	}
    }

    /**
     * return the initialize MailOptions.  If null is returned, the options
     * have not been initialized yet.
     */
    public static MailOptions getOptions() { return mailOptions; }

    /**
     * send a message to the user requesting validation of registration. <p>
     * 
     * <emp>
     * This method is deprecated but provided for backward compatability.
     * This implementation calls 
     * sendTokenMail(String toAddress, Properties data) with one property
     * called "token" which contains the URL the message receiver should 
     * access to confirm the registration.
     * </emp><p>
     * 
     * @deprecated  use sendTokenMail(String toAddress, Properties data) 
     *    instead
     */
    public static void sendTokenMail(String toAddress, String token)
	throws MailAccessException 
    {
        Properties data = new Properties();
        data.setProperty("secret", token);

	logger.debug("Sending mail to with token=" + token);

        sendTokenMail(toAddress, data);
    }    

    /**
     * send a message to the user requesting validation of registration
     * @param toAddress   the address to send mail to
     * @param msgData     information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception MailAccessException  if an error occurs while sending message
     */
    public static void sendTokenMail(String toAddress, Properties msgData)
	throws MailAccessException 
    {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}

	logger.debug("Sending token mail to " + toAddress);

        // We shouldn't update the user's Properties directly, so we wrap
        // it with a new instance.  
        Properties data = (msgData == null) ? new Properties() 
                                            : new Properties(msgData);
        String secretToken = data.getProperty("secret");
	logger.debug("secretToken = " + secretToken);
	logger.debug("data.getProperty(\"token\") = " + data.getProperty("token"));
        if (secretToken != null && data.getProperty("token") == null) {
	    data.setProperty("token", secretToken);
	}
        if (secretToken != null && data.getProperty("url") == null) {
            String confirmurl = mailOptions.getUserBaseUrl() + "?" + 
                MailOptions.URL_PARAM + "=" + secretToken;
            data.setProperty("url", confirmurl);
        }

	String mailMessage = getTokenSendMessage(data);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(), 
			  toAddress, mailOptions.getSubjectLine(),
			  mailMessage);
	sendMail(msg);
    }

    /** Send a password token for resetting a forgotten password.  */
    public static void sendPasswordReminderMail(String toAddress, Properties msgData)
            throws MailAccessException
    {
        if (!initialized) {
            String err = "Method call prior to initialization of mail options";
            logger.error(err);
            throw new MailAccessException(err);
        }

        logger.debug("Sending password reset token mail to " + toAddress);

        // We shouldn't update properties directly, so we wrap it with a new instance.
        Properties data = (msgData == null) ? new Properties()
                : new Properties(msgData);

        String mailMessage = getPasswordReminderMessage(data);
        Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
                toAddress, mailOptions.getSubjectLine(),
                mailMessage);
        sendMail(msg);
    }

    /** Send a username reminder email. */
    public static void sendUsernameReminderMail(String toAddress, Properties msgData)
            throws MailAccessException
    {
        if (!initialized) {
            String err = "Method call prior to initialization of mail options";
            logger.error(err);
            throw new MailAccessException(err);
        }

        logger.debug("Sending password reminder mail to " + toAddress);

        // We shouldn't update properties directly, so we wrap it with a new instance.
        Properties data = (msgData == null) ? new Properties()
                : new Properties(msgData);
        String username = data.getProperty("username");
        if (username == null) logger.debug("no username supplied");

        String mailMessage = getUsernameReminderMessage(data);
        Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
                toAddress, mailOptions.getSubjectLine()+": reminder",
                mailMessage);
        sendMail(msg);
    }

    /**
     * This method sends mail to the specified RA receipient with the
     * said token. 
     * @param raEmail
     *        Recipient address.
     * @param token
     *        The token string to replace the @token@ pattern in template
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendRATokenMail(String raEmail, String token) 
	throws MailAccessException {

	logger.debug("Sending mail to " + raEmail + " with token " + token);
	// Read in template file and replace with actual token.
	String mailMessage = getRATokenSendMessage(token);
 	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(), 
                                          raEmail,
                                          mailOptions.getRATokenSubjectLine(),
                                          mailMessage);
 	sendMail(msg);
    }
    private static String getRATokenSendMessage(String token) 
        throws MailAccessException {
        
        logger.debug("Text for sending tokens to RA");
	String templateFileName = mailOptions.getRATokenTemplateFilename();
	String urlRep = grepReplaceInFile(templateFileName, MailOptions.URL_PATTERN, 
					  mailOptions.getUserBaseUrl() + "?" +
					  MailOptions.URL_PARAM + "=" + token);
	String tokenRep = grepReplace(urlRep, MailOptions.TOKEN_PATTERN, token);
	return tokenRep; // + "\n\n\nhelp, I'm trapped in a fortune cookie factory!";
    }

    /**
     * This method sends mail to the specified receipient with instructions
     * for uploading a proxy certificate into a MyProxy server
     *
     * @param toAddress 
     *        Recipient address.
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     *
     * @deprecated  use sendProxyUploadMail(String toAddress, Properties data) 
     *    instead
     */
    public static void sendProxyUploadMail(String toAddress) 
	throws MailAccessException 
    {
        sendProxyUploadMail(toAddress, new Properties());
    }

    /**
     * This method sends mail to the specified receipient with instructions
     * for uploading a proxy certificate into a MyProxy server
     * @param toAddress   the address to send mail to
     * @param data        information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception MailAccessException  if an error occurs while sending message
     */
    public static void sendProxyUploadMail(String toAddress, Properties data)
	throws MailAccessException 
    {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}

	logger.debug("Sending accept mail to " + toAddress);

	String mailMessage = getProxyUploadMessage(data);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(), 
			  toAddress, mailOptions.getSubjectLine(),
			  mailMessage);
	sendMail(msg);
    }

    /**
     * This method sends mail to the specified receipient with the
     * message that the CA has accepted the request.
     *
     * @param toAddress 
     *        Recipient address.
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     * @deprecated  use sendAcceptMail(String toAddress, Properties data) 
     *    instead
     */
    public static void sendAcceptMail(String toAddress) 
	throws MailAccessException 
    {
        sendAcceptMail(toAddress, new Properties());
    }

    /**
     * This method sends mail to the specified receipient with the
     * message that the CA has accepted the request.
     * @param toAddress   the address to send mail to
     * @param data        information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception MailAccessException  if an error occurs while sending message
     */
    public static void sendAcceptMail(String toAddress, Properties data)
	throws MailAccessException 
    {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}

	logger.debug("Sending accept mail to " + toAddress);

	String mailMessage = getAcceptMessage(data);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(), 
			  toAddress, mailOptions.getSubjectLine(),
			  mailMessage);
	sendMail(msg);
    }

    /**
     * This method sends mail to the specified receipient with the
     * message that the CA has rejected the request.  This version simply 
     * appends the message to the end of the "template".
     *
     * @param toAddress 
     *        Recipient address.
     * @param message
     *        Message appended to the end of the template.
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     * @see sendRejectMail(String toAddress, Properties message)
     */
    public static void sendRejectMail(String toAddress, String message) 
	throws MailAccessException {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}
	logger.debug("Send reject mail to " + toAddress);
	
	String mailMessage = 
	    readMessageFromFile(mailOptions.getCaRejectTemplateFilename());
	mailMessage = mailMessage + "\n" + message;
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  toAddress, 
					  mailOptions.getSubjectLine(),
					  mailMessage);
	sendMail(msg);
    }

    /**
     * This method sends mail to the specified receipient with the
     * message that the CA has rejected the request.
     *
     * @param toAddress   Recipient address.
     * @param data        information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendRejectMail(String toAddress, Properties data) 
	throws MailAccessException 
    {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}
	logger.debug("Send reject mail to " + toAddress);
	
        String mailMessage = getRejectMessage(data);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  toAddress, 
					  mailOptions.getSubjectLine(),
					  mailMessage);
	sendMail(msg);
    }

    /**
     * This method sends mail to the configured CA address 
     * (<code>MailOptions.emailAddress</code>)
     * to indicate that a request from a user needs to be approved or rejected.
     *
     * @param token
     *        Token to be appended to the portal's base URL to form a URL that
     *        the CA can use to look at user data.
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     * @deprecated  use sendSignedCAMail(Properties data) instead.
     */
    public static void sendCAMail(String token) throws MailAccessException {
        Properties msgdata = new Properties();
        msgdata.setProperty("token", token);
        sendCAMail(msgdata);
    }

    /**
     * This method sends mail to the configured CA address 
     * (<code>MailOptions.emailAddress</code>)
     * to indicate that a request from a user needs to be approved or rejected
     *
     * @param token
     *        Token to be appended to the portal's base URL to form a URL that
     *        the CA can use to look at user data.
     * @param data     information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendCAMail(Properties data) throws MailAccessException {

	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}
	logger.debug("Send CA mail.");

        String secretToken = data.getProperty("token");
        if (secretToken == null) secretToken = data.getProperty("secret");
        if (secretToken != null && data.getProperty("url") == null) 
            data.setProperty("url", mailOptions.getCABaseUrl() + "?" +
                                    MailOptions.URL_PARAM + "=" + secretToken);

	String mailMessage = getCASendMessage(data);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  mailOptions.getCaAddress(), 
					  mailOptions.getSubjectLine(),
					  mailMessage);
	sendMail(msg);
    }


    /**
     * This method sends a secure mail to the configured CA address 
     * (<code>MailOptions.emailAddress</code>)
     * to indicate that a request from a user needs to be processed.
     *
     * @param token
     *        Token to be appended to the portal's base URL to form a URL that
     *        the CA can use to look at user data.
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     * @deprecated  use sendSignedCAMail(Properties data) instead.
     */
    public static void sendSignedCAMail(String token) 
         throws MailAccessException 
    {
        Properties msgdata = new Properties();
        msgdata.setProperty("token", token);
        sendSignedCAMail(msgdata);
    }

    /**
     * This method sends a secure mail to the configured CA address 
     * (<code>MailOptions.emailAddress</code>)
     * to indicate that a request from a user needs to be processed.
     *
     * @param data     information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendSignedCAMail(Properties data) 
         throws MailAccessException 
    {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}
	logger.debug("Send CA mail.");

        // In previous versions, the @token@ tag has been used in inconsistant
        // ways across mail messages.  (With this method, RegisterUser was 
        // appending to the input token carriage returns followed by the 
        // contents of the cert request file--clearly a hack!)  To provide
        // backward compatibility, the @secret@ tag by default is assumed to 
        // have the unadulterated token in it.  Note that the user is free 
        // to override the use and meaning of any of these default tags.
        String secretToken = data.getProperty("token");
        if (secretToken == null) secretToken = data.getProperty("secret");
        if (secretToken != null && data.getProperty("url") == null) 
            data.setProperty("url", mailOptions.getCABaseUrl() + "?" +
                                    MailOptions.URL_PARAM + "=" + secretToken);

	String mailMessage = getCASendMessage(data);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  mailOptions.getCaAddress(), 
					  mailOptions.getSubjectLine(),
					  mailMessage,true,
                                          mailOptions.getSignerpass());
	sendMail(msg);
    }
   
    
    
    /**
     * This method sends mail to the configured admin address 
     * with the specified messahe
     *
     * @param message
     *        Content that becomes the email body
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendAdminMail(String message) 
	throws MailAccessException {
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  mailOptions.getAdminAddress(),
					  mailOptions.getAdminSubjectLine(),
					  message);
	sendMail(msg);
    }
    
    /**  
     * This method sends notification mail to the configured admin address 
     * with the token message 
     *
     * @param message
     *        Content that becomes the email body
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     * @deprecated  use sendAdmMailNotification(Properties data) instead
     */
    public static void sendAdmMailNotification(String token) 
	throws MailAccessException 
    {
        Properties data = new Properties();
        data.setProperty("secret", token);

	logger.debug("Sending mail to administrator with token=" + token);

        sendAdmMailNotification(data);
    }

    /**  
     * This method sends notification mail to the configured admin address 
     * with the token message 
     *
     * @param message
     *        Content that becomes the email body
     * @param msgData     information to substitute into the template message.
     *                       The properties that should be included is up to
     *                       the template author.  
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendAdmMailNotification(Properties msgData) 
	throws MailAccessException 
    {
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}

	logger.debug("Sending mail to Admin");

        // We manipulate the message data for backward compatibility with 
        // past templates that set the token tag which featured an inconsistant 
        // use of the @token@ tag across templates.  (A previous version 
        // of this template uses @token@ to be just the bare token.)  To 
        // support this situation, we shouldn't update the user's Properties 
        // directly, so we wrap it with a new instance.  
        Properties data = (msgData == null) ? new Properties() 
                                            : new Properties(msgData);
        String secretToken = data.getProperty("secret");
        if (secretToken != null && data.getProperty("token") == null) 
            data.setProperty("token", secretToken);

        if (secretToken != null) {
            String confirmurl = mailOptions.getPortalBaseUrl() + "?" + 
                secretToken;
            data.setProperty("url", confirmurl);
        }

	// Read in template file and replace with actual token.
	String mailMessage = getAdmSendMessage(data);
	boolean signed = true;

	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  mailOptions.getAdminAddress(),
					  mailOptions.getAdminSubjectLine(),
					  mailMessage,signed,
                                          mailOptions.getSignerpass());
	sendMail(msg);
    }


    /**
     * This method sends mail to the configured user
     * with the specified message
     *
     * @param userEmail
     *        Email address to which the mail needs to be sent
     * @param err
     *        Content that becomes the email body
     * @exception <code>MailAccessException</code>
     *        If the message could not be constructed or sent.
     */
    public static void sendUserErrorMsg(String userEmail, String err) 
	throws MailAccessException {
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(),
					  mailOptions.getAdminAddress(),
					  "Re:"+ mailOptions.getSubjectLine(),
					  err);
	sendMail(msg);
    }

    public static void sendExpirationWarning(String userName, String toAddress,
					     String leadTime)
	
	throws MailAccessException {
	
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}

	logger.debug("Expiration mail: Username " + userName + " " 
		     + toAddress + " " + leadTime);
	
	String mailContent = getExpireSendMessage(userName, leadTime);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(), 
					  toAddress, 
					  mailOptions.getSubjectLine(),
					  mailContent);
	sendMail(msg);
    }
    
    public static void sendRenewalMail(String userName, String toAddress)
	throws MailAccessException {
	
	if (!initialized) {
	    String err = "Method call prior to initialization of mail options";
	    logger.error(err);
	    throw new MailAccessException(err);
	}

	logger.debug("Renewal mail: Username " + userName + " " + toAddress);
	
	String mailContent = getRenewalSendMessage(userName);
	Message msg = constructMailToSend(mailOptions.getUserAccountAddr(), 
					  toAddress, 
					  mailOptions.getSubjectLine(),
					  mailContent);
	sendMail(msg);
    }

    private static Message constructMailToSend(String fromAddr, String toAddr, 
					       String subject, String text) 
    throws MailAccessException {
	logger.debug("Constuct mail to send: " + fromAddr + " " + toAddr + " "
		     + subject + " " + text);
	MimeMessage message = null;
	try {
	    message = new MimeMessage(session);
	    InternetAddress fromAddress = null;
	    try {
		fromAddress = new InternetAddress(fromAddr);
	    } catch (AddressException exp) {
		String err = "Error creating 'from' address to send token mail";
		logger.error(err);
		throw new MailAccessException(err, exp);
	    } 
	    message.setFrom(fromAddress);
	    message.addRecipients(Message.RecipientType.TO, toAddr);
	    message.setSubject(subject);
	    message.setText(text);
	} catch (MessagingException exp) {
	    String err = "Error creating messaging object to send token mail";
	    logger.error(err);
	    throw new MailAccessException(err, exp);
	}
	return message;
    }
    
    
    private static Message constructMailToSend(String fromAddr, String toAddr, 
            String subject, String text,boolean beSigned,String password) 
    throws MailAccessException {
        logger.debug("Constuct mail to send: " + fromAddr + " " + toAddr + " "
                + subject + " " + text);
        MimeMessage message = null;
        try {
            message = new MimeMessage(session);
            InternetAddress fromAddress = null;
            try {
                fromAddress = new InternetAddress(fromAddr);
            } catch (AddressException exp) {
                String err = "Error creating 'from' address to send token mail";
                logger.error(err);
                throw new MailAccessException(err, exp);
            } 
            message.setFrom(fromAddress);
            message.addRecipients(Message.RecipientType.TO, toAddr);
            message.setSubject(subject);
            message.setText(text);
        } catch (MessagingException exp) {
            String err = "Error creating messaging object to send token mail";
            logger.error(err);
            throw new MailAccessException(err, exp);
        }
        if(beSigned)
            return sign(message,password);
        else
            return message;
    }

    
    
    private static void sendMail(Message message) throws MailAccessException {
	
	logger.debug("Sending mail");
	try {
	    Transport.send(message);
	} catch (SendFailedException sfe) {
	    String err = "Error sending message";
	    logger.error(err, sfe);
	    throw new MailAccessException(err, sfe);
	} catch (MessagingException exp) {
	    String err = "Error sending message";
	    logger.error(err);
	    throw new MailAccessException(err, exp);
	}
    }

    private static String getRenewalSendMessage(String userName) 
	throws MailAccessException {

	logger.debug("Text for sending tokens");
	String templateFileName = mailOptions.geRenewalTemplateFilename();
	return grepReplaceInFile(templateFileName, 
				 MailOptions.USER_NAME_PATTERN, 
				 userName);
    }

    private static String getTokenSendMessage(Properties info) 
	throws MailAccessException 
    {
	logger.debug("Text for sending tokens");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = composerFactory.getTokenMessageComposer();
            cmp.compose(info, out);
            return out.toString();
        } 
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }

    private static String getPasswordReminderMessage(Properties info)
	throws MailAccessException
    {
	logger.debug("Text for password reminder");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = composerFactory.getPasswordReminderComposer();
            cmp.compose(info, out);
            return out.toString();
        }
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }

    private static String getUsernameReminderMessage(Properties info)
	throws MailAccessException
    {
	logger.debug("Text for username reminder");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = composerFactory.getUsernameReminderComposer();
            cmp.compose(info, out);
            return out.toString();
        }
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }

    private static String getProxyUploadMessage(Properties info) 
	throws MailAccessException 
    {
	logger.debug("Text for proxy upload");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = 
                composerFactory.getProxyUploadMessageComposer();
            cmp.compose(info, out);
            return out.toString();
        } 
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }
    
    private static String getAcceptMessage(Properties info) 
	throws MailAccessException 
    {
	logger.debug("Text for CA Accept");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = 
                composerFactory.getAcceptMessageComposer();
            cmp.compose(info, out);
            return out.toString();
        } 
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }
    
    private static String getRejectMessage(Properties info) 
	throws MailAccessException 
    {
	logger.debug("Text for CA Reject");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = 
                composerFactory.getRejectMessageComposer();
            cmp.compose(info, out);
            return out.toString();
        } 
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }
    
    private static String getExpireSendMessage(String userName, 
					       String timeInDays) 
	throws MailAccessException {
	
	logger.debug("Text for sending expiration warning");
	String templateFileName = mailOptions.getExpirationTemplateFilename();
	String modifiedStr
	    = grepReplaceInFile(templateFileName, 
				MailOptions.RENEW_URL_PATTERN, 
				mailOptions.getRenewBaseUrl() + "?" +
				MailOptions.USER_NAME_PARAM + "=" + userName);
	modifiedStr = grepReplace(modifiedStr, MailOptions.TIME_PATTERN, 
				  timeInDays);
	
	modifiedStr = grepReplace(modifiedStr, MailOptions.USER_NAME_PATTERN, 
				  userName);
	
	logger.debug("Modified email text: " + modifiedStr);
	return modifiedStr;
    }

    private static String getCASendMessage(Properties info) 
	throws MailAccessException 
    {
	logger.debug("Text for CA approval request message");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = 
                composerFactory.getCASendMessageComposer();
            cmp.compose(info, out);
            return out.toString();
        } 
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }
    
    private static String getAdmSendMessage(Properties info) 
	throws MailAccessException 
    {
	logger.debug("Text for requesting CA approval");
        try {
            StringWriter out = new StringWriter();
            MessageComposer cmp = composerFactory.getAdmMessageComposer();
            cmp.compose(info, out);
            return out.toString();
        } 
        catch (IOException ex) {
            String err = "IO error encounter while writing template";
            logger.error(err);
            throw new MailAccessException(err);
        }
    }

    private static String grepReplace(String fileName, 
				      String pattern1, String replaceWith1,
				      String pattern2, String replaceWith2) 
	throws MailAccessException {
	
	logger.debug("Filename: " + fileName + " " + pattern1 + " " + replaceWith1);
	logger.debug("Filename: " + fileName + " " + pattern1 + " " + replaceWith2);
	StringBuffer templateFileContents = 
	    new StringBuffer(readMessageFromFile(fileName));
	logger.debug("Template file contents\n" 
		     + templateFileContents.toString());

	int patternIndex = 
	    templateFileContents.indexOf(pattern1);
	int patternLength = pattern1.length();
	StringBuffer finalFileContents = 
	    templateFileContents.replace(patternIndex, 
					 patternIndex + patternLength,
					 replaceWith1);

	patternIndex = templateFileContents.indexOf(pattern2);
	patternLength = pattern2.length();
	finalFileContents = 
	    templateFileContents.replace(patternIndex, 
					 patternIndex + patternLength,
					 replaceWith2);

	logger.debug("Final file contents " + finalFileContents.toString());
	return finalFileContents.toString();
    }
    
    private static String grepReplaceInFile(String fileName, String pattern, 
					  String replaceWith) 
	throws MailAccessException {
	
	logger.debug("Filename: " + fileName + " " + pattern + " " 
		     + replaceWith);
	
	String string = readMessageFromFile(fileName);
	return grepReplace(string, pattern, replaceWith);
    }

    private static String grepReplace(String string, String pattern, 
				      String replaceWith) 
	throws MailAccessException {
	
	logger.debug("String: " + string + " " + pattern + " " 
		     + replaceWith);
	StringBuffer templateFileContents = new StringBuffer(string);
	int patternIndex = 
	    templateFileContents.indexOf(pattern);
	int patternLength = pattern.length();
	StringBuffer finalFileContents = 
	    templateFileContents.replace(patternIndex, 
					 patternIndex + patternLength,
					 replaceWith);
	logger.debug("Final file contents " + finalFileContents.toString());
	return finalFileContents.toString();
    }

    private static String readMessageFromFile(String fileName) 
	throws MailAccessException {

	logger.debug("Reading from filename " + fileName);
	String templateFileContents = null;
	BufferedReader bufReader = null;
	try {
	    bufReader = new BufferedReader(new FileReader(fileName));
	    while (bufReader.ready()) {
		if (templateFileContents == null) {
		    templateFileContents = 
			new String(bufReader.readLine() + "\n");
		} else {
		    templateFileContents = templateFileContents 
			+ bufReader.readLine() + "\n";
		}
	    }
	} catch (FileNotFoundException fnfe) {
	    String er = "Template file for sending token information not found";
	    logger.error(er);
	    throw new MailAccessException(er, fnfe);
	} catch (IOException ioe) {
	    String err = "IO exception while reading in from template file for "
		+ "sending token information";
	    logger.error(err);
	    throw new MailAccessException(err, ioe);
	} finally {
	    try {
		bufReader.close();
	    } catch (Exception exp) {
		logger.error("Error closing reader for template file");
	    }
	}
	logger.debug("File contents\n" + templateFileContents);
	return templateFileContents;
    }
    
    
    public static MimeMessage sign(MimeMessage msg, String password) {
        String signerCertFile = null; 
        String signerKeyFile  = null;  
        try{
            signerCertFile = mailOptions.getSignerCert();
            signerKeyFile = mailOptions.getSignerKey();
        }catch(Exception e){
            try{
                signerCertFile = RegisterUtil.getEnvVar("X509_USER_CERT");
                signerKeyFile = RegisterUtil.getEnvVar("X509_USER_KEY");
            }
            catch(Exception exp){
                logger.error("No proper PUBLIC & PRIVATE key pair found to Sign the request !");
            }
        }
        
        X509Certificate signerCert = null;
        OpenSSLKey key = null;
        
        try{	  
            InputStream isSignerCert = new FileInputStream(signerCertFile);
            InputStream isSignerKey = new FileInputStream(signerKeyFile);
            signerCert = RegisterUtil.loadUserCert(isSignerCert);
            key = new BouncyCastleOpenSSLKey(isSignerKey);
            if (key.isEncrypted())
                key.decrypt(password);
        }catch(GeneralSecurityException e) {
            logger.error("Wrong password for signer or other security error"+e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            logger.error("Error can't create digest");
        }
        
        PrivateKey pv =  key.getPrivateKey();
        X509Certificate origCert = signerCert;
        String origDN = signerCert.getSubjectDN().toString();
        String signDN = origCert.getIssuerDN().toString();
        
        ArrayList certList = new ArrayList();
        
        certList.add(origCert);
        CertStore certsAndcrls=null; 
        
        try{
            certsAndcrls = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(certList), "BC");
        }catch(Exception iapex){
            logger.error("InvalidAlgorithmParameterException !!" + iapex.toString());
        }
        
        // create some smime capabilities in case someone wants to respond
        ASN1EncodableVector         signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector       caps = new SMIMECapabilityVector();
        
        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.rC2_CBC, 128);
        caps.addCapability(SMIMECapability.dES_CBC);
        
        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));
        // add an encryption key preference for encrypted responses -
        // normally this would be different from the signing certificate...
        IssuerAndSerialNumber   issAndSer = new IssuerAndSerialNumber(
                new X509Name(signDN), signerCert.getSerialNumber());
        
        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));

        // create the generator for creating an smime/signed message
        SMIMESignedGenerator gen = new SMIMESignedGenerator();
        
        // add a signer to the generator 
        try{
            gen.addSigner(pv, origCert, SMIMESignedGenerator.DIGEST_SHA1, new AttributeTable(signedAttrs), null);
            // add our pool of certs and cerls (if any) to go with the signature
            gen.addCertificatesAndCRLs(certsAndcrls);
            MimeMultipart mm = gen.generate(msg, "BC");
            msg.setContent(mm, mm.getContentType());
            msg.saveChanges();
        }catch(Exception sgnexp) {
            logger.error("Error in Signing message !!" + sgnexp.toString());
        }
        return msg;
    }
    
    
    private static int verify(SMIMESigned s)
    throws Exception
    {
        CertStore certs=null;
        int returnVal=0;
        
        try{
            certs = s.getCertificatesAndCRLs("Collection", "BC");
        }catch(Exception e){ System.out.println(e.toString());}
        SignerInformationStore  signers = s.getSignerInfos();
        Collection              c = signers.getSigners();
        Iterator                it = c.iterator();
        while (it.hasNext())
        {
            SignerInformation   signer = (SignerInformation)it.next();
            Collection          certCollection = certs.getCertificates(signer.getSID());
            Iterator        certIt = certCollection.iterator();
            X509Certificate cert = (X509Certificate)certIt.next();
            // verify that the sig is correct and that it was generated
            // when the certificate was current
            if (signer.verify(cert, "BC"))
            {
                System.out.println("signature verified");
                returnVal = 1;
            }
            else
            {
                System.out.println("signature failed!");
                returnVal = 0;
            }
        }
        
        logger.debug("Verification result :" + returnVal);
        return returnVal;
    }
    
    public static int signatureVerification(MimeMessage msg)
    {
        // make sure this was a multipart/signed message - there should be
        // two parts as we have one part for the content that was signed and
        // one part for the actual signature.
        int retVal=0;
        try{
            if (msg.isMimeType("multipart/signed"))
            {
                SMIMESigned  s = new SMIMESigned((MimeMultipart)msg.getContent());
                MimeBodyPart content = s.getContent();
                Object  cont = content.getContent();
                if (cont instanceof String)
                {
                    System.out.println((String)cont);
                }
                else if (cont instanceof Multipart)
                {
                    Multipart   mp = (Multipart)cont;
                    int count = mp.getCount();
                    for (int i = 0; i < count; i++)
                    {
                        BodyPart    m = mp.getBodyPart(i);
                        Object      part = m.getContent();
                        
                        System.out.println("Part " + i);
                        System.out.println("---------------------------");
                        
                        if (part instanceof String)
                        {
                            System.out.println((String)part);
                        }
                        else
                        {
                            System.out.println("can't print...");
                        }
                    }
                }
                
                System.out.println("Status:");
                
                retVal = verify(s);
            }
            else if (msg.isMimeType("application/pkcs7-mime"))
            {
                SMIMESigned s = new SMIMESigned(msg);
                MimeBodyPart content = s.getContent();
                System.out.println("Content pkcs7 :");
                Object  cont = content.getContent();
                if (cont instanceof String)
                {
                    System.out.println((String)cont);
                }
                System.out.println("Status:");
                retVal = verify(s);
            }
            else
            {
                System.out.println("Not a signed message!");
                retVal = -1;
            }
        }catch(Exception e)
        {
        }
        System.out.println("returned value form verification " + retVal);
        return retVal;
    }


    public static int processMail(Message email,String emailFromAddress) {
        
        String emailContents = null;
        try{
            emailContents = new String(email.getContent().toString());
        }catch(Exception eio){
        }
        
        MimeMessage mimeMsg = (MimeMessage)email;
        
        logger.debug("Processing mail");
        
        if ((emailContents == null) || (emailFromAddress == null)) {
            String err = "Error in process mail: email contents or from address"
                + " is null " + emailContents + " " + emailFromAddress;
            logger.error(err);
            try {
                sendAdminMail(err);
            } catch (MailAccessException exp) {
                logger.error("Could not send admin error mail", exp);
            }
            return -1;
        }
        
        // Parse out the token
        
        String inputStr = emailContents.trim();
        
        String caAddress = mailOptions.caAddress; 
        String adminAddress = mailOptions.adminAddr; 
        
        int indexToken = inputStr.indexOf(mailOptions.TOKEN_PREFIX);	
        int indexCaAdd = emailFromAddress.indexOf(caAddress);
        int indexAdminAdd = emailFromAddress.indexOf(adminAddress);
        
        if((indexCaAdd != -1) && (indexToken == -1))
        {
            
            logger.debug("Get certificate from email and add it to repository");
            int result = signatureVerification(mimeMsg);
            if(result == -1)
                logger.error("Email is not signed by CA !!");
            else if(result == 0)
                logger.error("Signature of CA is not valid !!");
            else if(result == 1)
                return 1; // valid email from CA go to add signed the certificate 
        }
        else
        {
            if (indexToken == -1) {
                String err="Request mail sent for SweGrid registration did not have "
                    + " token.";
                logger.error(err);
                try {
                    sendUserErrorMsg(emailFromAddress, err);
                } catch (MailAccessException exp) {
                    logger.error("Could not send uesr error mail", exp);
                }
                return -1;
            }

            String token = null;
            token = getMailToken(inputStr);
            if(token == null)
                return -1;

            token = token.trim();
            UserData userData = null;
            try {
                userData = UserDataHandler.getData(token);
            } catch (DatabaseAccessException exp) {
                String err = "Could not retrieve data from db";
                logger.error(err, exp);
                try {
                    sendAdminMail(err + "\n" + exp.toString());
                } catch (MailAccessException mailExp) {
                    logger.error("Could not send admin error mail", mailExp);
                }
                return -1;
            }
            if (userData == null) {
                String err = "Following token does not exist " + token + ". No "
                + " pending request with such a token exists.";
                logger.error(err);
                try {
                    sendAdminMail(err);
                    sendUserErrorMsg(emailFromAddress, err);
                } catch (MailAccessException exp) {
                    logger.error("Could not send admin error/user mail", exp);
                }
                return -1;
            } 
            
            logger.debug(userData.getEmailAddress() + " " + emailFromAddress);
            if ((emailFromAddress.indexOf(userData.getEmailAddress()) == -1) && (indexAdminAdd == -1)) {
                
                String err = "Email address to which token was sent, does not "
                    + " match neigther to email address token was received from. nor to AdminAddress Token: "
                    + token;
                logger.error(err);
                try {
                    sendAdminMail(err);
                    sendUserErrorMsg(emailFromAddress, err);
                } catch (MailAccessException exp) {
                    logger.error("Could not send admin error/user mail", exp);
                }
                return -1;
            }
            
            if(indexAdminAdd != -1) { // get confirmation from Admin
                return 2;
            }
            
        }
        return -1;
    }

	
    public static String getMailToken(String mailcontent) {
   
    int indexToken = mailcontent.indexOf(mailOptions.TOKEN_PREFIX);	
    int tokenStarts = indexToken + mailOptions.TOKEN_PREFIX.length();
    int tokenEnds =  mailcontent.indexOf(mailOptions.TOKEN_SUFIX);
    
    if (tokenEnds == -1) {
        String err="Request mail sent for SweGrid registration did not have "
            + " token.";
        logger.error(err);
    }
    return (mailcontent.substring(tokenStarts, tokenEnds));

    }
}

