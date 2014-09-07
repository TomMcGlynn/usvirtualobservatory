/*
 This file is licensed under the terms of the Globus Toolkit Public
 License, found at http://www.globus.org/toolkit/download/license.html.
 */
package org.globus.purse.registration.mailProcessing;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.globus.purse.registration.RegisterUser;


/**
 * Class to retrieve mail periodically.
 */
public class RetrieveMail {
    
    static Log logger =
        LogFactory.getLog(RetrieveMail.class.getName());
    
    private static MailOptions mailOptions = null;
    private static Session session = null;
    private static boolean initialized = false;
    GetMailThread mailCheck = null;
    static String password = null;
    
    /**
     * Iniitializes things needed to periodically retrieve mail
     */
    public RetrieveMail(MailOptions mailOptions_) {
        mailOptions = mailOptions_;
        Properties props = new Properties();
        props.put("mail.host", mailOptions.getIncomingHost());
        props.put("mail.port", Integer.toString(mailOptions.getIncomingPort()));
        props.put("mail.transport.protocol", mailOptions.getIncomingProtocol());
        if (logger.isDebugEnabled()) 
            props.put("debug", "true");
        session = Session.getDefaultInstance(props, null);
    }
    
    /**
     * Starts the thread that retrieves mail with 'ESG Registration' as subject 
     * line and invoked some method that processes the mail. The mail server
     * is contacted at the specified frequency
     */
    public void startCheckingMail(int frequency) {
        mailCheck = new GetMailThread("MailThead", frequency);
        mailCheck.start();
    }
    
    /**
     * Stops the thread that retrieves mail
     */
    public void stopCheckingMail() {
        if (mailCheck != null)
            mailCheck.setReturnValue();
    }
    
    
    public void setPassword(String pass) {
        password = pass;
    }
    
    private class GetMailThread extends Thread {
        
        boolean returnRun = false;
        long sleepTime;
        
        public GetMailThread(String name, int frequency) {
            super(name);
            sleepTime = (long)frequency;
        }
        
        public void setReturnValue() {
            returnRun = true;
        }
        
        public void run() {
            Store store = null;
            Folder folder= null;
            int result = 0;
            String msg = null;
            
            logger.debug("In run");
            while (!returnRun) {
                try {
                    store = 
                        session.getStore(mailOptions.getIncomingProtocol());
                    String userAccountAddr = mailOptions.getUserAccountAddr();
                    String userAccount = 
                        userAccountAddr.substring(0, 
                                userAccountAddr.indexOf("@"));
                    store.connect(mailOptions.getIncomingHost(), 
                            mailOptions.getIncomingPort(), 
                            userAccount, password);
                    folder = store.getFolder("INBOX");
                    folder.open(Folder.READ_WRITE);
                    
                    SearchTerm stArr[] = new  SearchTerm[3];
                    stArr[0] = new SubjectTerm(mailOptions.getSubjectLine());
                    stArr[1] = new SubjectTerm(mailOptions.getAdminSubjectLine());
                    stArr[2] = new SubjectTerm(mailOptions.getCASubjectLine());
                    
                    SearchTerm st = new OrTerm(stArr);
                    Message message[] = folder.search(st);
                    MimeMessage mimeMsg = new MimeMessage(session);
                    
                    for (int i=0; i<message.length; i++) {
                        Address fromAddr = message[i].getFrom()[0];
                        logger.debug("Message from " + fromAddr +
                                " subject " + message[i].getSubject());
                        try {    
                            mimeMsg = (MimeMessage)folder.getMessage(i+1);
                            result = MailManager.signatureVerification(mimeMsg);
                            
                            if (result == -1) { // Message is not signed 
                                //msg = new String(message[i].getContent().toString());
                                RegisterUser.mailHandler(message[i], fromAddr.toString());
                            }
                            else if (result == 1) { // Signed message is verified 
                                try {
                                    SMIMESigned  s = new SMIMESigned((MimeMultipart)mimeMsg.getContent());
                                    MimeBodyPart content = s.getContent();
                                    Object  cont = content.getContent();
                                    msg = (String)(cont);
                                }catch(CMSException e) {
                                    logger.error("Error : generating SMIMESigneds message");
                                }catch(IOException e) {
                                    logger.error("Error : retrieving message content");
                                }
                                RegisterUser.mailHandler((Message)message[i], fromAddr.toString());
                            }else if (result == 0) // Message is signed BUT the signature is NOT verified 
                                logger.warn("Message From : " + fromAddr + 
                                " Skip recieved message, which signature is not verified, Skip");
                        }catch(MessagingException e) {
                            logger.error("Error : Retrieving messages from the message folder");
                        }
                        
                        message[i].setFlag(Flags.Flag.DELETED, true);
                    }
                    
                    try {
                        sleep(sleepTime);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exp");
                    }
                } catch (NoSuchProviderException noProviderExp) {
                    logger.error("Could not get store", noProviderExp);
                    returnRun = true;
                } catch (AuthenticationFailedException e) {
                    logger.error("Authentication failed on store connect", e);
                    returnRun = true;
                } 
                catch (MessagingException exp) {
                    logger.error("Error retrieving mail", exp);
                    returnRun = true;
                } finally {
                    try {
                        folder.close(true);
                        store.close();
                    } catch (MessagingException exp) {
                        logger.warn("Unable to close folder/store", exp);
                    }
                }
            }
        }
    }
}
