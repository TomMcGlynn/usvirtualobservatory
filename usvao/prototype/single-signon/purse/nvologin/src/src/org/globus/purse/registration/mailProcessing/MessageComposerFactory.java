/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import org.globus.purse.exceptions.MailAccessException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * a factory for creating various MessageComposer implementations.  <p>
 *
 * To create the desired MessageComposer, this factory will first look for
 * a user-specified MessageComposer class.  (Currently, this can be specified
 * via a system property called <code>purse.tokenMessageComposerClass</code>;
 * however, this may eventually be integrated into the MailOptions class.)
 * If a class is specified but cannot be instantiated, an exception is thrown.
 * If no class is specified, the default MessageComposerFromTemplate class
 * is instantiated and returned.  In this case, each factory method (e.g. 
 * getTokenMessageComposer()) consults the MailOptions class for the proper
 * template message to use.  
 */
public class MessageComposerFactory {

    MailOptions mailOptions = null;
    Log logger = LogFactory.getLog(MessageComposerFactory.class.getName());

    public MessageComposerFactory(MailOptions opts) {
        if (opts == null) {
	    String err = "attempt to initialize with null MailOptions";
	    logger.error(err);
            throw new IllegalArgumentException(err);
        }
        mailOptions = opts;
    }

    /** Composer for a password reminder email. */
    public MessageComposer getPasswordReminderComposer() throws MailAccessException {
        String template = mailOptions.getPasswordReminderFilename();
        return createDefaultMessageComposer(template, "Password-reminder");
    }

    /**
     * return a MessageComposer object that can compose the message to 
     * ask the user to validate their registration.  
     * @exception MailAccessException  
     *     if the configured MessageComposer cannot be found or instantiate,
     *     or if the template file cannot be found or read.
     */
    public MessageComposer getTokenMessageComposer() 
         throws MailAccessException 
    {
        // look for user-defined message composer
        //
        // MailOptions needs to be update to support the 
        // tokenMessageComposerClass property; when it does the following 
        // line will get that value:
        //   
        //   String tokenMCClassName = 
        //       mailOptions.getTokenMessageComposerClass();
        //
        // For now, we look for a system property
        String className = 
            System.getProperty("purse.tokenMessageComposerClass");

        // note: instead of throwing an exception upon detection of a 
        // problem (as this next line does), the implementation could 
        // simply write a warning message to the logger and allow the 
        // default MessageComposer to be formed. 
        if (className != null) 
            return instantiateMessageComposer(className);

        // instantiate the default MessageComposer
        String template = mailOptions.getSendTokenTemplateFilename();
        return createDefaultMessageComposer(template, "Send-token");
    }

    /**
     * return a MessageComposer object that can compose the message 
     * instructing recipient to upload a proxy certificate
     * @exception MailAccessException  
     *     if the configured MessageComposer cannot be found or instantiate,
     *     or if the template file cannot be found or read.
     */
    public MessageComposer getProxyUploadMessageComposer() 
         throws MailAccessException 
    {
        // look for user-defined message composer
        String className = 
            System.getProperty("purse.proxyUploadMessageComposerClass");
        if (className != null) 
            return instantiateMessageComposer(className);

        // instantiate the default MessageComposer
        String template = mailOptions.getProxyUploadTemplateFileName();
        return createDefaultMessageComposer(template, "Proxy-upload");
    }

    /**
     * return a MessageComposer object that can compose the message 
     * notifying the recipient that the registration has been accepted
     * @exception MailAccessException  
     *     if the configured MessageComposer cannot be found or instantiate,
     *     or if the template file cannot be found or read.
     */
    public MessageComposer getAcceptMessageComposer() 
         throws MailAccessException 
    {
        // look for user-defined message composer
        String className = 
            System.getProperty("purse.acceptMessageComposerClass");
        if (className != null) 
            return instantiateMessageComposer(className);

        // instantiate the default MessageComposer
        String template = mailOptions.getCaAcceptTemplateFilename();
        return createDefaultMessageComposer(template, "CA-Accept");
    }

    /**
     * return a MessageComposer object that can compose the message 
     * notifying the recipient that the registration has been rejected
     * @exception MailAccessException  
     *     if the configured MessageComposer cannot be found or instantiate,
     *     or if the template file cannot be found or read.
     */
    public MessageComposer getRejectMessageComposer() 
         throws MailAccessException 
    {
        // look for user-defined message composer
        String className = 
            System.getProperty("purse.rejectMessageComposerClass");
        if (className != null) 
            return instantiateMessageComposer(className);

        // instantiate the default MessageComposer
        String template = mailOptions.getCaRejectTemplateFilename();
        return createDefaultMessageComposer(template, "CA-Reject");
    }

    /**
     * return a MessageComposer object that can compose the message 
     * requesting CA approval of a registrant. 
     * @exception MailAccessException  
     *     if the configured MessageComposer cannot be found or instantiate,
     *     or if the template file cannot be found or read.
     */
    public MessageComposer getAdmMessageComposer() 
         throws MailAccessException 
    {
        // look for user-defined message composer
        String className = 
            System.getProperty("purse.AdminMessageComposerClass");
        if (className != null) 
            return instantiateMessageComposer(className);

        // instantiate the default MessageComposer
        String template = mailOptions.getAdmTemplateFilename();
        return createDefaultMessageComposer(template, "CA-request-approval");
    }

    /**
     * return a MessageComposer object that can compose the message 
     * requesting CA approval of a registrant. 
     * @exception MailAccessException  
     *     if the configured MessageComposer cannot be found or instantiate,
     *     or if the template file cannot be found or read.
     */
    public MessageComposer getCASendMessageComposer() 
         throws MailAccessException 
    {
        // look for user-defined message composer
        String className = 
            System.getProperty("purse.CASendMessageComposerClass");
        if (className != null) 
            return instantiateMessageComposer(className);

        // instantiate the default MessageComposer
        String template = mailOptions.getCaTemplateFilename();
        return createDefaultMessageComposer(template, "CA-request-approval");
    }

    /*
     * @param templateFilename  the filename of the template to use
     * @param templateTypename  a human-oriented name for the type of 
     *                            template.  This is used in an error
     *                            message if the file can't be found.
     */
    MessageComposer createDefaultMessageComposer(String templateFilename,
                                                 String templateTypename)
         throws MailAccessException
    {
        try {
          return new MessageComposerFromTemplate(new File(templateFilename));
        }
        catch (FileNotFoundException ex) {
	    String err = templateTypename + " template file not found: " +
                         templateFilename;
	    logger.error(err);
            throw new MailAccessException(err);
        }
        catch (IOException ex) {
	    String err = "Error while reading template (" + templateFilename + 
                         "): " + ex.getMessage();
	    logger.error(err);
            throw new MailAccessException(err);
        }
    }

    static MessageComposer instantiateMessageComposer(String classname) 
         throws MailAccessException
    {
        try {
            Class tokenMCClass = Class.forName(classname);
            return (MessageComposer) tokenMCClass.newInstance();
        }
        catch (ClassNotFoundException ex) {
            throw new MailAccessException(
                "Configured MessageComposer class (" + 
                classname + ") cannot be found");
        }
        catch (IllegalAccessException ex) {
            throw new MailAccessException(
                "Configured MessageComposer class (" + classname + 
                ") does not have a public no-arg constructor.");
        }
        catch (InstantiationException ex) {
            throw new MailAccessException(
                "Configured MessageComposer class (" + classname + 
                ") encountered an error during construction:\n  " + 
                ex.getMessage());
        }
    }
}
