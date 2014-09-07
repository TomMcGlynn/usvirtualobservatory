/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.certificateStorage;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.purse.exceptions.MyProxyAccessException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.registration.RegisterUtil;
import org.globus.gsi.gssapi.auth.IdentityAuthorization;
import org.globus.myproxy.ChangePasswordParams;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;

/**
 * Class to handle MyProxy server interactions.
 */
public class MyProxyManager {
    
    static Log logger =
	LogFactory.getLog(MyProxyManager.class.getName());

    private final static String ADMIN_LOAD_CRED = 
	"myproxy-admin-load-credential";
    private final static String ADMIN_REMOVE_CRED = 
	"myproxy-admin-query";
    private final static String ADMIN_QUERY_CRED =
		"myproxy-admin-query";

    private final static String USER_CERT = "usercert.pem";
    private final static String USER_KEY = "userkey.pem";
    private static boolean initialized = false;
    static MyProxyOptions myProxyOpts = null;

    /**
     * Initialzie MyProxy client side executable location
     * This need to be called prior to using any other methods in this class.
     *
     * @param <code>MyProxyOptions</code>
     *        MyProxy initialization
     */
    public static void initialize(MyProxyOptions myProxyOpts_) 
	throws MyProxyAccessException {

	if (!initialized) {
	    if (myProxyOpts_ == null) {
		logger.error("Option cannot be null");
		throw new MyProxyAccessException("Option cannot be null");
	    }
	    myProxyOpts = myProxyOpts_;
	    initialized =true;
	}
    }

    /**
     * Method to check the existence of this user name in the MyProxy server.
     *
     * @param userName
     *        User name whose presence needs to be checked.
     * @return boolean
     *         true is it exists else false.
     * @exception <code>MyProxyAcessException</code>
     *         If any error occurs
     */
    public static boolean userNameExists(String userName)
	throws MyProxyAccessException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}

	logger.debug("Veryfying existence of user " + userName);
	String toCompare = "username: " + userName.trim();
	String baseErr = "Could not verify is user name already exists";
	String[] cmdArray = 
            new String[] { myProxyOpts.getBinDir() + File.separator 
                           + ADMIN_QUERY_CRED, "-s",
                           myProxyOpts.getDirectory() };
	String outputString = null;
	try {
	     outputString = RegisterUtil.runCommand(cmdArray, baseErr);
	} catch (RegistrationException regExp) {
	    logger.debug(baseErr, regExp);
	    throw new MyProxyAccessException(baseErr, regExp);
	}
	
	if (outputString == null) {
	    logger.debug("No output string, no user");
	    return false;
	} 
	logger.debug("Output string " + outputString);
	StringTokenizer strTok = new StringTokenizer(outputString, "\n");
	while (strTok.hasMoreTokens()) {
	    String nextToken = strTok.nextToken();
	    logger.debug("Next token : " + nextToken);
	    if (nextToken.equals(toCompare)) {
		logger.debug("User name exists");
		return true;
	    }
	}
	logger.debug("User name does not exist");
	return false;
    }

    /**
     * Method to store the user credential onto the MyProxy server.
     *
     * @param userName
     *        User name to be used for storing the credentials on MyProxy server
     * @param password
     *        Password on MyProxy server
     * @param credDir
     *        Directory with credentials.
     * @exception <code>MyProxyAccessException</code>
     *            If any error occurs.
     */
    public static void storeCredential(String userName, 
				       String password, String credDir)
	throws MyProxyAccessException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}

	logger.debug("Store credentials  using admin-load");
	String baseErrMesg = "Error storing user's credentials";
	String[] cmdArray = 
            new String[] { myProxyOpts.getBinDir() 
                           + File.separator + ADMIN_LOAD_CRED, 
                           "-s", myProxyOpts.getDirectory(), "-l",
                           userName, "-c", credDir + File.separator 
                           + USER_CERT, "-y", credDir + File.separator 
                           + USER_KEY };
	
	try {
	    RegisterUtil.runCommand(cmdArray, baseErrMesg);
	} catch (RegistrationException exp) {
	    throw new MyProxyAccessException("Command exec failed", exp);
	}
    }

    /**
     * Method to store the user credential onto the MyProxy server.
     * anonymously with out any password
     *
     * @param userName
     *        User name to be used for storing the credentials on MyProxy server
     * @param credDir
     *        Directory with credentials.
     * @exception <code>MyProxyAccessException</code>
     *            If any error occurs.
     */
    public static void storeCredential(String userName, String credDir)
	throws MyProxyAccessException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}

	logger.debug("Store credentials  using admin-load");
	String baseErrMesg = "Error storing user's credentials";
	String[] cmdArray = 
            new String[] { myProxyOpts.getBinDir() + File.separator 
                           + ADMIN_LOAD_CRED, "-s", 
                           myProxyOpts.getDirectory(), "-l", userName, 
                           "-c", credDir + File.separator + USER_CERT,
                           "-y", credDir + File.separator + USER_KEY, 
                           "-n"};

	try {
	    RegisterUtil.runCommand(cmdArray, baseErrMesg);
	} catch (RegistrationException exp) {
	    throw new MyProxyAccessException("Command exec failed", exp);
	}
    }
    
    /**
     * Deletes the user from MyPorxy server.
     *
     * @param userName
     *        User name to be removed from MyProxy server
     * @exception <code>MyProxyAccessException</code>
     *           If any errors occur
     */
    public static void deleteUser(String userName)
	throws MyProxyAccessException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}
	logger.debug("Remove credentials  using admin-query");
	String baseErrMesg = "Error deleting user's credentials";
	String[] cmdArray = 
            new String[] { myProxyOpts.getBinDir() + File.separator 
                           + ADMIN_REMOVE_CRED, "-r", "-s",
                           myProxyOpts.getDirectory(), "-l", userName};
	try {
	    RegisterUtil.runCommand(cmdArray, baseErrMesg);
	} catch (RegistrationException exp) {
	    throw new MyProxyAccessException("Command exec failed", exp);
	}
    }

    public static void changeUserPassword(String userName, String oldPass, 
					  String newPass, 
					  GSSCredential gssCred) 
	throws MyProxyAccessException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}

	logger.debug(userName + " " + oldPass + " " + newPass);
	if ((userName == null) || (oldPass == null)
	    || (newPass == null) || (gssCred == null)) {
	    String err = "None of the paramaters can be null.";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}
	
	logger.debug("Change password: " + myProxyOpts.getHostName() + " " 
		     + myProxyOpts.getPortNumber() + " "
		     + userName + " " + oldPass + " " + newPass);
	
	ChangePasswordParams chPassParams = new ChangePasswordParams();
	chPassParams.setUserName(userName);
	chPassParams.setPassphrase(oldPass);
	chPassParams.setNewPassphrase(newPass);
	MyProxy myProxy = new MyProxy(myProxyOpts.getHostName(), 
				      myProxyOpts.getPortNumber());
	myProxy.setAuthorization(new IdentityAuthorization(myProxyOpts.getDN()));
	try {
	    myProxy.changePassword(gssCred, chPassParams);
	} catch (MyProxyException exp) {
	    String err = "Error changing password ";
	    logger.error(err, exp);
	    throw new MyProxyAccessException(err, exp);
	}
    }

    public static Vector getExpiredUsers() throws MyProxyAccessException {
	
	if (!initialized) {
	    String err = "Initialize method needs to be called prior to "
		+ "other method invocations";
	    logger.error(err);
	    throw new MyProxyAccessException(err);
	}

	// 
	int leadTimeInMins = myProxyOpts.getExpirationLeadTime() * 24;
	String[] cmdArray = 
            new String[] { myProxyOpts.getBinDir() + File.separator 
                           + ADMIN_QUERY_CRED, "-e", 
                           (new Integer(leadTimeInMins)).toString() };

	String baseErr = "Could not ascertain expired user details.";
	String outputString = null;
	try {
	    outputString = RegisterUtil.runCommand(cmdArray, baseErr);
	} catch (RegistrationException exp) {
	    throw new MyProxyAccessException("Command exec failed", exp);
	}

	logger.debug("Output string is\n" + outputString);
	String toCompare = "username: ";
	int length = toCompare.length();
	Vector userNames = null;
	if (outputString != null) {
	    logger.debug("Output string");
	    StringTokenizer strTok = new StringTokenizer(outputString, "\n");
	    while (strTok.hasMoreTokens()) {
		logger.debug("No more tokens");
		String nextToken = strTok.nextToken();
		logger.debug("Next token : " + nextToken);
		if (nextToken.indexOf(toCompare) == 0) {
		    logger.debug("Add user name exists");
		    if (userNames == null)
			userNames = new Vector();
		    String userName = nextToken.substring(length, 
                                                          nextToken.length());
		    logger.debug("User name " + userName);
		    userNames.add(userName);
		}
	    }
	}
	
	return userNames;
    }

    public static String getExpirationLead() {
	
	if (myProxyOpts == null)
	    return null;
	else 
	    return Integer.toString(myProxyOpts.getExpirationLeadTime());
    }
}
