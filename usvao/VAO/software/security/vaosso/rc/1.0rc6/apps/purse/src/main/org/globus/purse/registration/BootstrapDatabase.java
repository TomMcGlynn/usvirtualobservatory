/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

import org.globus.purse.registration.databaseAccess.DatabaseManager;
import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.exceptions.UserRegistrationException;

import org.globus.purse.registration.databaseAccess.RADataHandler;
import org.globus.purse.registration.databaseAccess.RoleDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.databaseAccess.UserDataHandler;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;

import java.util.Properties;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.sql.SQLException;

public class BootstrapDatabase {

    static final String mesg = 
	" Usage: <dbPropFile> <statusFile> <roleFile> [raFile]\n"
	+ "\n Options are -help Prints this help message\n"
	+ "               -debug Runs with debug trace\n"
	+ " <dbPropFile> : File name with database properties as follows\n"
	+ "                \"dbDriver=\"<database driver name \n"
	+ "                \"dbConnectionURL=\"<database connection URL\n"
	+ "                \"dbUserName=\"<Username to access database\n"
	+ "                \"dbPassword=\"<Password for the above username\n"
	+ " <statusFile> : File with status data to bootstrap database with.\n"
	+ " <roleFile> : File with role data to bootstrap database with.\n"
        + " <raFile> : File with RA data to bootstrap database with.\n";

    static String adminAddress = null;

    public static void main(String[] args) throws Exception {
	
	boolean debug = false;

	if (args.length < 3) {
	    System.err.println("Inusfficient arguments");
	    System.err.println(mesg);
	    System.exit(-1);
	}

	String dbPropFile = args[0];
	String statusFile = args[1];
	String roleFile = args[2];
	String userFile = args[3];

	initializeDatabase(dbPropFile);
        initializeStatus(statusFile);
	initializeRoles(roleFile);

        RADataHandler.addNilRAData();
        if (args.length == 5) {
            initializeRAs(args[4]);
        }

        initializeReservedUsers(userFile);
    }
    
    private static void initializeRoles(String rolesFilename) 
        throws Exception {
	
	// Read in roles file and set up the database
	loadFromFile(rolesFilename, 0);
    }

    private static void initializeStatus(String statusFilename) 
	throws Exception {

	loadFromFile(statusFilename, 1);
    }

    private static void initializeRAs(String raFilename) 
	throws Exception {

	loadFromFile(raFilename, 2);
    }

    private static void initializeReservedUsers(String userFile) 
        throws Exception 
    {
        String adminEmail = null;  // get from db prop purseAdminAddr

	BufferedReader bufReader = null;
        String line = null;
	try {
	    bufReader = new BufferedReader(new FileReader(userFile));
            while ((line = bufReader.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) continue;

                String[] users = line.split("\\s+");
                String username = users[0];
                String fname = (users.length < 2) ? "Reserved" : users[1];
                String lname = (users.length < 3) ? "User" : users[2];

                System.err.println("Loading " + username + " as " + fname + 
                                   " " + lname);

                // give user a randomized token and password
                String token = new UniqueToken(System.currentTimeMillis(), 
				       new Random().nextLong()).toString();
                String pw = UserDataHandler.passwordSha1(
                                  new UniqueToken(System.currentTimeMillis(), 
                                  new Random().nextLong()).toString());

                UserData userData = 
                    new UserData(fname, lname, ""/*contactPerson*/, 
                                 "disallows real users from choosing this name",
                                 username, pw, ""/*inst*/, ""/*proj*/, 
                                 adminAddress, ""/*phone*/, ""/*country*/, 
                                 ""/*returnurl*/, ""/*portal*/, 
                      StatusDataHandler.getId(RegisterUtil.getRejectedStatus()));
                userData.setToken(token);

                try {
                    UserDataHandler.storeData(userData);
                } catch (DatabaseAccessException exp) {
                    String err = "Error storing data for " + 
                        userData.getUserName() + ": " + exp.getMessage();
                    System.err.println(err);
                } 
            }
        }
        catch (IOException ex) {
            System.err.println("Error reading reserved user data: " + userFile);
            throw ex;
        }
    }

    private static void loadFromFile(String filename, int type) 
	throws DatabaseAccessException {
	
	BufferedReader bufReader = null;
	try {
	    String line = null;
	    bufReader = new BufferedReader(new FileReader(filename));
            if (type == 2) {
                while (((line = bufReader.readLine()) != null)) {
                    if (line.trim().equals("")) {
                        continue;
                    }
                    StringTokenizer tokenizer = 
                        new StringTokenizer(line.trim(), " ");
                    if (tokenizer.countTokens() < 3) {
                        (new Exception()).printStackTrace();
                        System.err.println("Bad format: shld be "
                                           + "\"name email description\""
                                           + line);
                        System.exit(-1);
                    }
                    int index = line.indexOf(" ");
                    String name = line.substring(0, index);
                    int newIndex = line.indexOf(" ", index+1);
                    String email = line.substring(index+1, newIndex);
                    index = line.indexOf(" ", newIndex+1);
                    String desc = line.substring(index+1);
                    RAData raData = new RAData(name, email, desc);
                    RADataHandler.storeData(raData);
                } 
            } else {
                while (((line = bufReader.readLine()) != null)) {
                    if (line.trim().equals("")) {
                        continue;
                    }
                    int delimiter = line.trim().indexOf(" ");
                    if (delimiter == -1) {
                        System.err.println("Bad format: shld be "
                                           + "\"name description\"");
                        System.exit(-1);
                    }
                    String name = line.substring(0, delimiter);
                    String desc = line.substring(delimiter, line.length());
                    if (type == 0) {
                        RoleData roleData = new RoleData(name, desc);
                        RoleDataHandler.storeData(roleData);
                    } else if (type == 1) {
                        StatusData statusData = new StatusData(name, desc);
                        StatusDataHandler.storeData(statusData);
                    }
                }
	    }
	} catch (IOException ioe) {
	    System.err.println("Error reading from file " + filename 
			       + "\n" + ioe.getMessage());
	    System.exit(-1);
	} catch (UserRegistrationException exp) {
	    System.err.println("Error storing data \n" + exp.getMessage());
	    System.exit(-1);
	} finally {
	    if (bufReader != null) {
		try {
		    bufReader.close();
		} catch (IOException exp) {
		}
	    }
	}
    }

    private static void initializeDatabase(String propFile) throws Exception {

	Properties prop = new Properties();
	try {
	    prop.load(new FileInputStream(propFile));
	} catch (FileNotFoundException fnfe) {
	    System.err.println("Error: Database properties file " 
			       + propFile + " not found");
	    System.exit(-1);
	} catch (IOException ioe) {
	    System.err.println("Error loading file " + propFile + "\n"
			       + ioe.getMessage());
	    System.exit(-1);
	}

	String purseDir=prop.getProperty("purse.dir", 
					 System.getProperty("purse.dir"));
	if (purseDir != null) {
	    for (Enumeration e=prop.propertyNames(); e.hasMoreElements(); ) {
		String key, value = prop.getProperty(key = (String) e.nextElement());
		prop.setProperty(key,value.replaceAll("\\$\\{purse.dir\\}",
						      purseDir));
	    }
	}
	
        adminAddress = prop.getProperty("purseAdminAddr");
        Matcher m = Pattern.compile("^.*<([^>]*)>.*$").matcher(adminAddress);
        if (m.matches()) 
            adminAddress = m.replaceFirst(m.group(1));

        RegisterUtil.initialize(prop, purseDir, null);

        /*

	DatabaseOptions dbOptions = 
	    new DatabaseOptions(prop.getProperty("dbDriver"), 
				prop.getProperty("dbConnectionURL"), 
				prop.getProperty("dbUsername"), 
				prop.getProperty("dbPassword"),
				prop.getProperty("dbPropFile"),
				prop.getProperty("passPhrase"));
	try {
	    DatabaseManager.initialize(dbOptions);
	} catch (DatabaseAccessException exp) {
	    String err = "Error initializing database.";
	    System.err.println("err " + exp.getMessage());
	    System.exit(-1);
	}
        */
    }
}
