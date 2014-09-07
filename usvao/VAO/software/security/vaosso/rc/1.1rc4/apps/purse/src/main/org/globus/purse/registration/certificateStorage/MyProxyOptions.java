/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.certificateStorage;

import org.globus.purse.exceptions.MyProxyAccessException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MyProxyOptions {

    static Log logger =
	LogFactory.getLog(MyProxyOptions.class.getName());    

    String binDir = null;
    String hostName = null;
    int portNumber = -1;
    int expirationLeadTimeInDays = 0;
    String dn = null;
    String dir = null;

    public MyProxyOptions(String binDir_, String hostName_, int portNumber_,
			  String dn_, String dir_, int expirationLeadTime_) 
	throws MyProxyAccessException {
	
	String paramErr = " cannot be null";
	if ((binDir_ == null) || (binDir_.trim().equals(""))) {
	    logger.error("Bin directory" + paramErr);
	    throw new MyProxyAccessException("Bin directory" + paramErr);
	}
	binDir = binDir_;

	if ((hostName_ == null) || (hostName_.trim().equals(""))) {
	    logger.error("Host name" + paramErr);
	    throw new MyProxyAccessException("Hoat name" + paramErr);
	}
	hostName = hostName_;

	portNumber = portNumber_;

	if ((dn_ == null) || (dn_.trim().equals(""))) {
	    logger.error("DN of MyProxy " + paramErr);
	    throw new MyProxyAccessException("DN of MyProxy " + paramErr);
	}
	dn = dn_;

	if ((dir_ == null) || (dir_.trim().equals(""))) {
	    logger.error("Storage directory of MyProxy " + paramErr);
	    throw new MyProxyAccessException("Storage directory of MyProxy " 
					     + paramErr);
	}
	dir = dir_;

	expirationLeadTimeInDays = expirationLeadTime_;
    }

    public String getBinDir() {
	return binDir;
    }

    public String getHostName() {
	return hostName;
    }
    
    public String getDN() {
	return dn;
    }

    public int getPortNumber() {
	return portNumber;
    }

    public String getDirectory() {
	return dir;
    }
    
    public int getExpirationLeadTime() {
	return this.expirationLeadTimeInDays;
    }
}
