/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.CertificateGenerationException;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.MyProxyAccessException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.registration.certificateGeneration.CertificateGenerationOptions;
import org.globus.purse.registration.certificateGeneration.UserCertificateGeneration;
import org.globus.purse.registration.certificateStorage.MyProxyManager;
import org.globus.purse.registration.certificateStorage.MyProxyOptions;
import org.globus.purse.registration.databaseAccess.DatabaseManager;
import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.databaseAccess.UserDataHandler;
import org.globus.purse.registration.mailProcessing.MailManager;
import org.globus.purse.registration.mailProcessing.MailOptions;
import org.globus.gsi.CertUtil;

/**
 * Utility class for registration package
 */
public class RegisterUtil {

    static Log logger =
	LogFactory.getLog(RegisterUtil.class.getName());

    // The text for the following status shld be the first five lines
    // of the status file.
    private static String requestStatus = null;
    private static String pendingStatus = null;
    private static String acceptedStatus = null;
    private static String rejectedStatus = null;
    private static String renewalStatus = null;

    private static boolean isInitialized = false;

    private static Properties userDefinedTags = null;

    /**
     * Initialize method that creates the necessary Options objects
     * and expands any file location references in the values of the
     * provided <code>Properties</code> object with a
     * <code>${purse.dir}</code> macro.
     * 
     * @param p
     *        The configuration file as a Properties object
     * @param defaultPurseDir
     *        Default location to expand <code>${purse.dir}</code> to
     *        in the values of the properties object (unless another
     *        value is defined for it in the properties object)
     * @exception RegistrationException
     *            If any error occurs
     */
    public static void initialize(Properties p, String defaultPurseDir, Properties userTags)
	throws RegistrationException
    {
	String purseDir = p.getProperty("purse.dir", defaultPurseDir);

	for (Enumeration e=p.propertyNames(); e.hasMoreElements(); ) {
	    String key, value = p.getProperty(key = (String) e.nextElement());
	    p.setProperty(key,value.replaceAll("\\$\\{purse.dir\\}",
					       purseDir));
	}

	try {
	
	    DatabaseOptions dbOptions = 
		new DatabaseOptions(p.getProperty("dbDriver"), 
				    p.getProperty("dbConnectionURL"), 
				    p.getProperty("dbUsername"), 
				    p.getProperty("dbPassword"),
				    p.getProperty("dbPropFile"),
				    Integer.parseInt(p.getProperty("hashIterations")));
	    
	    MailOptions mailOptions = 
		new MailOptions(p.getProperty("caAddress"), 
				p.getProperty("userAccount"), 
				p.getProperty("incomingHost"), 
				Integer.parseInt(p.getProperty("incomingPort")),
                p.getProperty("incomingProtocol"),
				p.getProperty("outgoingHost"), 
				Integer.parseInt(p.getProperty("outgoingPort")),
                p.getProperty("outgoingProtocol"),
				p.getProperty("passwordReminderTemplate"),
				p.getProperty("usernameReminderTemplate"),
				p.getProperty("sendTokenTemplate"),
				p.getProperty("caAcceptTemplate"),
				p.getProperty("caRejectTemplate"),
				p.getProperty("expireWarnTemplate"),
				p.getProperty("renewTemplate"),
				p.getProperty("caBaseUrl"),
				p.getProperty("userBaseUrl"),
				p.getProperty("renewBaseUrl"),
				p.getProperty("caTemplate"),
				p.getProperty("purseAdminAddr"),
				p.getProperty("subjectLine"),
				p.getProperty("adminSubjectLine"),
				p.getProperty("caSubjectLine"),
				p.getProperty("caAdmtemplate"),
				p.getProperty("portalBaseUrl"),
				p.getProperty("signerCert"),
				p.getProperty("signerKey"),
				p.getProperty("signerPass"),
				p.getProperty("proxyUploadTemplate"),
                                p.getProperty("raTokenMailTemplate"),
                                p.getProperty("raSubjectLine"));
	    
	    CertificateGenerationOptions certOpts = 
		new CertificateGenerationOptions(p.getProperty("binLocation"),
						 p.getProperty("tmpLocation"),
						 p.getProperty("caDir"),
						 p.getProperty("caHash"),
                         p.getProperty("myProxyIsCA"));
	    
	    MyProxyOptions myProxyOpts = 
		new MyProxyOptions(p.getProperty("myProxyBin"),
				   p.getProperty("myProxyHost"),
				   Integer.parseInt(p
                                          .getProperty("myProxyPort")),
				   p.getProperty("myProxyDn"),
				   p.getProperty("myProxyDir"),
				   Integer.parseInt(p
                                          .getProperty("expirationLeadTime")));
	    
	    userDefinedTags = userTags;
	    initialize(dbOptions, mailOptions, certOpts, myProxyOpts,
		       p.getProperty("statusFilename"));
	    
	} catch (Exception e) {
	    String err = "Initialization error: "+e.getMessage();
	    logger.error(err);
	    throw new RegistrationException(err, e);
	}
    }

    /**
     * Initialize method that sets database options and mail options.
     * 
     * @param <code>DatabaseOptions</code>
     *        Object with database properties.
     * @param <code>MailOptions</code>
     *        Object with mail properties.
     * @param <code>CertificateGenerationOptions</code>
     *        Object with certificate generation (SimpleCA) properties.
     * @param rolesFilename
     *        Path to file with bootstrap role data.
     * @param statusFilename
     *        Path to file with bootstrap status data.
     * @exception <code>RegistrationException</code>
     *            If any error occurs
     */
    public static void initialize(DatabaseOptions dbOptions, 
				  MailOptions mailOptions,
				  CertificateGenerationOptions certOptions,
				  MyProxyOptions myProxyOpts, 
				  String statusFilename)
	throws RegistrationException {

	isInitialized = false;
	
	String paramErr = " cannot be null.";
	if (dbOptions == null) {
	    logger.debug("Database options " + paramErr);
	    throw new RegistrationException("Database options " + paramErr);
	}

	if (mailOptions == null) {
	    logger.debug("Mail options " + paramErr);
	    throw new RegistrationException("Mail options " + paramErr);
	}
	
	if (certOptions == null) {
	    logger.debug("Certificate options " + paramErr);
	    throw new RegistrationException("Certificate options " + paramErr);
	}

	if (myProxyOpts == null) {
	    logger.debug("MyProxy options " + paramErr);
	    throw new RegistrationException("MyProxy Options " + paramErr);
	}

	MailManager.initialize(mailOptions);
	UserCertificateGeneration.initialize(certOptions);
	try {
	    MyProxyManager.initialize(myProxyOpts);
	} catch (MyProxyAccessException exp) {
	    String err = "Error initializing MyProxy .";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	} 

	try {
	    DatabaseManager.initialize(dbOptions);
	} catch (DatabaseAccessException exp) {
	    String err = "Error initializing database.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	} 
	initializeStatus(statusFilename);
	isInitialized = true;
    }

    private static void initializeStatus(String statusFilename) 
	throws RegistrationException {

	BufferedReader bufReader = null;
	int i=0;
	try {
	    String line = null;
	    bufReader = new BufferedReader(new FileReader(statusFilename));
	    while (((line = bufReader.readLine()) != null)) {
		line = line.trim().substring(0, line.indexOf(" "));
		if (i==0) 
		    setRequestStatus(line.trim());
		if (i==1)
		    setPendingStatus(line.trim());
		if (i==2)
		    setAcceptedStatus(line.trim());
		if (i==3)
		    setRejectedStatus(line.trim());
		if (i==4)
		    setRenewalStatus(line.trim());
		i++;
	    }
	} catch (IOException ioe) {
	    logger.error("Error reading from file " + statusFilename
			       + "\n", ioe);
	    throw new RegistrationException("Error reading from file " 
					    + statusFilename, ioe);
	} finally {
	    if (bufReader != null) {
		try {
		    bufReader.close();
		} catch (IOException exp) {
		    logger.error(exp);
		}
	    }
	}
    }

    public static void setRequestStatus(String requestStatusStr) {
	requestStatus = requestStatusStr;
    }

    public static void setPendingStatus(String pendingStatusStr) {
	pendingStatus = pendingStatusStr;
    }

    public static void setAcceptedStatus(String acceptedStatusStr) {
	acceptedStatus = acceptedStatusStr;
    }

    public static void setRejectedStatus(String rejectedStatusStr) {
	rejectedStatus = rejectedStatusStr;
    }

    public static void setRenewalStatus(String renewalStatusStr) {
	renewalStatus = renewalStatusStr;
    }

    public static String getRequestStatus() {
	return requestStatus;
    }

    public static String getPendingStatus() {
	return pendingStatus;
    }

    public static String getAcceptedStatus() {
	return acceptedStatus;
    }

    public static String getRejectedStatus() {
	return rejectedStatus;
    }

    public static String getRenewalStatus() {
	return renewalStatus;
    }

    /**
     * A generalized method to run a command. Assumes all environmant 
     * variables are set so the command can be executed.
     *
     * @param command
     *        Command to run.
     * @param baseErrMesg
     *        Error message that is thrown with the exception
     * @return String 
     *         The output of the process on to standard out.
     * @exception <code>RegistrationException</code>
     *       If any error occurs.
     */
    public static String runCommand(String[] command, String baseErrMesg)
	throws RegistrationException {

	if (command == null) {
	    logger.debug("Command cannot be null");
	    throw new RegistrationException("Command cannot be null");
	}

        if (logger.isDebugEnabled()) {
            logger.debug("Command is ");
            for (int i=0; i<command.length; i++) {
            logger.debug("Command is " + command[i]);
            }
        }
	Runtime runtime = Runtime.getRuntime();
	StringBuffer outBuffer = null;
	BufferedReader out = null;
	BufferedReader err = null;
	try {
	    Process process = runtime.exec(command);

	    int returnCode = -1;
	    try {
		returnCode = process.waitFor();
	    } catch (InterruptedException exp) {
		logger.error("Interupped exp thrown ", exp);
	    }
	    logger.debug("Return code is " + returnCode);

	    // Get process output on stdout
	    out = new BufferedReader(new InputStreamReader
		(process.getInputStream()));
	    if (out != null) {
		String line = out.readLine(); 
		if (line != null) {
		    outBuffer = new StringBuffer(line + "\n");
                    line = out.readLine(); 
		    while (line != null) {
			outBuffer.append(line + "\n");
			line = out.readLine(); 
		    }
		}
	    }	

	    // If return code is not zero, try to get error stream also
	    if (returnCode != 0) {
		err = new BufferedReader(new InputStreamReader
		    (process.getErrorStream()));
		StringBuffer errBuffer = null;
		if (err != null) {
		    String line = err.readLine(); 
		    if (line != null) {
			errBuffer = new StringBuffer(line + "\n");
			while (line != null) {
			    errBuffer.append(line + "\n");
			    line = err.readLine(); 
			}
		    }
		} 
		String errString = baseErrMesg;
		if (errBuffer != null) {
		     errString = errString + errBuffer.toString();
		}
		if (outBuffer != null) {
		    errString = errString + outBuffer.toString();
		}
		logger.error(errString);
		throw new RegistrationException(command + "\n" + errString);
	    }
	} catch (IOException ioe) {
	    logger.error(baseErrMesg, ioe);
	    throw new RegistrationException(command + "\n" + baseErrMesg, ioe);
	} finally {
	    try {
		if (out != null) 
		    out.close();
		if (err != null)
		    err.close();
	    } catch (IOException exp) {
		logger.debug("Could not close stream", exp);
	    }
	}
	if (outBuffer != null) {
	    return outBuffer.toString();
	} else {
	    return null;
	}
    }

    /** 
     * Returns true if both strings match
     */
    public static boolean stringsMatch(String str1, String str2) {
	logger.debug("Str Match " + str1 + " " + str2);
	if (str1 == null) {
	    if (str2 != null) 
		return false;
	    return true;
	} else {
	    if (str2 == null) 
		return false;
	    if (str1.equals(str2)) {
		return true;
	    }
	    return false;
	}
    }

    // Generate certificates for this user
    public static String generateUserCerts(UserData userData, 
					   String caPassPhrase) 
	throws RegistrationException {

	String certDir = null;
    try {
	    certDir = 
		UserCertificateGeneration.generate(userData.getUserName(),
                                                   null);
	    // Use CA to sign the certs
	    UserCertificateGeneration.signCerts(certDir, caPassPhrase);
	} catch (CertificateGenerationException certExp) {
	    String err = "Error creating credentials for the user";
	    logger.error(err);
	    throw new RegistrationException(err, certExp);
	}
	return certDir;
    }

    // Uplaod creds to MyProxy
    public static void storeUserCerts(UserData userData, String certDir ) 
	throws RegistrationException {
	try {
	    MyProxyManager.storeCredential(userData.getUserName(), 
					   userData.getPasswordSha(), certDir);
	} catch (MyProxyAccessException myProxyExp) {
	    String err = "Error storing credential for user";
	    logger.error(err);
	    throw new RegistrationException(err, myProxyExp);
	}
    }

    // Delete the certificates
    public static void deleteCerts(String certDir) {
	File certDirFile = new File(certDir);
	File listOfFiles[] = certDirFile.listFiles();
	if (listOfFiles != null) {
	    for (int i=0; i<listOfFiles.length; i++) {
		listOfFiles[i].delete();
	    }
	}
	certDirFile.delete();
    }

    public static void setUserStatusAsAccepted(String token) 
	throws RegistrationException {

	try {
	    // Set user status as accepted.
	    int acceptedId = 
		StatusDataHandler.getId(RegisterUtil.getAcceptedStatus());
	    UserDataHandler.setStatus(token, acceptedId);
	} catch (DatabaseAccessException exp) {
	    String err = "Error setting user status.";
	    logger.error(err);
	    throw new RegistrationException(err, exp);
	} 
    }
    

    /*  
     * Returns the value of environmental variable
     */
    public static String getEnvVar(String variable) {
        
        String value = null;
        
        try{
            
            Runtime r = Runtime.getRuntime();
            Process p = r.exec("env");
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(
                                   new DataInputStream(
                                   new BufferedInputStream(
                                   p.getInputStream()))));
            String lineRead = null;
            while( (lineRead = reader.readLine() ) != null)
            {
                if(lineRead.indexOf(variable) != -1)
                    value = lineRead.substring(lineRead.indexOf(variable)
                                               +variable.length()+1,
                                               lineRead.length());
            }
        }
        catch(Exception exp){
            logger.error("Reading environmental variable ",exp);
        }
        if(value==null)
            logger.error("Error : The environment variable " + variable 
                         + "is not set");
        return value;
    }

    public static boolean isInitialized() {
	return isInitialized;
    }
    
    
    
    public static String readFromFile(String fileName) 
    {

	logger.debug("Reading from filename " + fileName);
	String fileContents = null;
	BufferedReader bufReader = null;
	try {
	    bufReader = new BufferedReader(new FileReader(fileName));
	    while (bufReader.ready()) {
		if (fileContents == null) {
		    fileContents = 
			new String(bufReader.readLine() + "\n");
		} else {
		    fileContents = fileContents 
			+ bufReader.readLine() + "\n";
		}
	    }
	} catch (FileNotFoundException fnfe) {
	    String er = "File not found";
	    logger.error(er);
	} catch (IOException ioe) {
	    String err = "IO exception while reading in from" ;
	    logger.error(err);
	} finally {
	    try {
		bufReader.close();
	    } catch (Exception exp) {
		logger.error("Error closing reader for template file");
	    }
	}
	return fileContents;
    }
    
    public static X509Certificate loadUserCert(InputStream isUserCert) {

        X509Certificate userCert = null;
        try {
            userCert = CertUtil.loadCertificate(isUserCert);
        }catch(Exception e) {
            logger.error("Error loading signer certificate");
        }
        return userCert;
    }

    public static void loadUserTags(Properties props) {
	if (userDefinedTags != null)
	    for (Enumeration e=userDefinedTags.propertyNames(); e.hasMoreElements(); ) {
		String key = (String)e.nextElement();
		String value = (String)userDefinedTags.getProperty(key);
		props.setProperty(key,value);
	    }
    }

}
