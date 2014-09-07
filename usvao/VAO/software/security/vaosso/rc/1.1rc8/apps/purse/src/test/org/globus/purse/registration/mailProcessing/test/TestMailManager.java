/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.mailProcessing.test;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.Flags;
import javax.mail.search.SubjectTerm;
import javax.mail.search.SearchTerm;

import java.io.BufferedReader;
import java.io.FileReader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.globus.purse.registration.mailProcessing.MailManager;
import org.globus.purse.registration.mailProcessing.MailOptions;

public class TestMailManager extends TestCase {

    static String password = null;
    static MailOptions mailOptions = null;
    Store store = null;

    public TestMailManager(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestMailManager.class);
    }

    public static void setMailOptions(MailOptions mailOptions_) {
	mailOptions = mailOptions_;
    }

    public static void setPassword(String pass) {
	password = pass;
    }

    public void testSendingMail() throws Exception {
	
	initializeMailParams();
	MailManager.sendTokenMail(mailOptions.getUserAccountAddr(), "TEST1");
	Thread.currentThread().sleep(10000);
	verifyTokenMail();
	MailManager.sendCAMail("TEST1");
	//	verifyCAMail(); Needs CA's password!
	MailManager.sendAcceptMail(mailOptions.getUserAccountAddr());
	Thread.currentThread().sleep(10000);
	verifyAcceptEmail();
	String rejMsg = "Message from CA";
	MailManager.sendRejectMail(mailOptions.getUserAccountAddr(), rejMsg);
	Thread.currentThread().sleep(11000);
	verifyRejectEmail(rejMsg);
	MailManager.sendExpirationWarning("testUser", 
					  mailOptions.getUserAccountAddr(), 
					  "10");
	Thread.currentThread().sleep(11000);
	verifyExpirationEmail("testUser", "10");
	MailManager.sendRenewalMail("testUser", 
				    mailOptions.getUserAccountAddr());
	Thread.currentThread().sleep(11000);
	verifyRenewalMail("testUser");
    }

    private void verifyRenewalMail(String userName) throws Exception {
	String msg = retrieveMail();
	assertTrue(msg != null);
	assertTrue(msg.indexOf(userName) !=-1);
    }

    private void verifyExpirationEmail(String userName, String leadTime) 
	throws Exception {
	String msg = retrieveMail();
	assertTrue(msg != null);
	assertTrue(msg.indexOf(mailOptions.getRenewBaseUrl()) !=-1);
	assertTrue(msg.indexOf(userName) !=-1);
	assertTrue(msg.indexOf(leadTime) !=-1);
    }

    private void verifyTokenMail() throws Exception {
	String msg = retrieveMail();
	assertTrue(msg != null);
	assertTrue(msg.indexOf("TEST1") != -1);
    }

    private void verifyRejectEmail(String rejMsg) throws Exception {
	String msg = retrieveMail();
	assertTrue(msg != null);
	String mailMessage = 
	    readMessageFromFile(mailOptions.getCaRejectTemplateFilename());
	assertTrue(msg.trim().indexOf(mailMessage.trim()) !=-1);
	assertTrue(msg.trim().indexOf(rejMsg.trim()) !=-1);
    }

    private void verifyAcceptEmail() throws Exception {
	String msg = retrieveMail();
	assertTrue(msg != null);
	String mailMessage = 
	    readMessageFromFile(mailOptions.getCaAcceptTemplateFilename());
	assertTrue(msg.trim().indexOf(mailMessage.trim())!= -1);
    }

    private void initializeMailParams() throws Exception {
	Properties props = new Properties();
	props.put("mail.host", mailOptions.getIncomingHost());
	props.put("mail.port", Integer.toString(mailOptions.getIncomingPort()));
	Session session = Session.getDefaultInstance(props, null);
	session.setDebug(true);
	store = session.getStore(mailOptions.getIncomingProtocol());
    }
    
    private String retrieveMail() throws Exception {

	String userAccountAddr = mailOptions.getUserAccountAddr();
	String userAccount = 
	    userAccountAddr.substring(0, 
				      userAccountAddr.indexOf("@"));
	store.connect(mailOptions.getIncomingHost(), 
		      mailOptions.getIncomingPort(), userAccount, password);
	Folder folder = store.getFolder("INBOX");
	folder.open(Folder.READ_WRITE);
	SearchTerm st = new SubjectTerm(mailOptions.getSubjectLine());
	Message message[] = folder.search(st);
	System.out.println("Messhaes " + message.length);
	if (message.length > 0) {
	    String ret = (String)message[0].getContent();
	    message[0].setFlag(Flags.Flag.DELETED, true);
	    folder.close(true);
	    store.close();
	    return ret;
	}
	return null;
    }

    private static String readMessageFromFile(String fileName) 
	throws Exception {
	
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
	} finally {
	    bufReader.close();
	}
	return templateFileContents;
    }
}    

