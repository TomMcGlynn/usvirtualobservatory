/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.databaseAccess.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.globus.purse.registration.*;
import org.globus.purse.registration.databaseAccess.RADataHandler;
import org.globus.purse.registration.databaseAccess.RoleDataHandler;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;
import org.globus.purse.registration.databaseAccess.UserDataHandler;

import java.sql.Date;
import java.util.Vector;

public class TestUserDataHandler extends TestCase {

    int userId;
    String token = "dummy";
    String firstName = "Tester1";
    String lastName = "LastName1";
    String contact = "contact person";
    String stmtOfWork = "Stmt of Work";
    String userName = "tester1";
    String password = "dummyPass";
    String institution = "temp institution";
    String projectName = "temp project";
    String emailAddr = "tester1@foo.bar";
    String phoneNum = "phone";
    String country = "USA";
    
    int status1;
    int status2;
    int status3;

    int role1;
    int role2;

    int ra1;

    public TestUserDataHandler(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestUserDataHandler.class);
    }

    public void test() throws Exception {
	setup();
	storeAndRetrieve();
	userRolesProcessing();
	deleteUserData();
	deleteAllRejectedUsers();
        RADataHandler.deleteData(ra1);
    }

    private void setup() throws Exception {
	
	StatusData statusData = new StatusData("requested", "desc1");
	StatusDataHandler.storeData(statusData);
	status1 = StatusDataHandler.getId("requested");
	
	StatusData statusData1 = new StatusData("rejected", "desc3");
	StatusDataHandler.storeData(statusData1);
	status3 = StatusDataHandler.getId("rejected");
	RegisterUtil.setRejectedStatus("rejected");

	StatusData statusData2 = new StatusData("pending", "desc2");
	StatusDataHandler.storeData(statusData2);
	status2 = StatusDataHandler.getId("pending");

	RoleData roleData = new RoleData("user", "desc1");
	RoleDataHandler.storeData(roleData);
	role1 = RoleDataHandler.getData("user").getId();

	roleData = new RoleData("admin", "desc1");
	RoleDataHandler.storeData(roleData);
	role2 = RoleDataHandler.getData("admin").getId();

        RAData raData = new RAData("temp", "temp@foo.bar", "tempdesc");
        RADataHandler.storeData(raData);
        ra1 = RADataHandler.getDataByName("temp").getId();
    }

    public void storeAndRetrieve() throws Exception {

	Vector<Integer> addRoles = new Vector<Integer>();
	addRoles.add(role1);
	// Data parallel to initial user data.
	UserData userData1 = new UserData(firstName, lastName, contact, 
					  stmtOfWork, "userName1", UserDataHandler.passwordSha1(password), 
					  institution, projectName, emailAddr, 
					  phoneNum, country, null, null, status1, ra1);
	userData1.setToken("token1");
	userData1.addRoles(addRoles);
	UserDataHandler.storeData(userData1);
	userId = UserDataHandler.getUserId("userName1");
	userData1.setUserId(userId);

	// Attempt retrieving by token
	UserData retrievedData = UserDataHandler.getData("token1");
	assertTrue(retrievedData != null);
	assertTrue(userData1.equals(retrievedData));

	// User data with no null values
	UserData userData = new UserData(firstName, lastName, contact, 
					 stmtOfWork, userName, UserDataHandler.passwordSha1(password), 
					 institution, projectName, emailAddr, 
					 phoneNum, country, null, null, status1);
	userData.setToken(token);
	userData.addRoles(addRoles);
	userData.setCreationTime(new Date(new java.util.Date().getTime()));
	userData.setCreationTime(new Date(new java.util.Date().getTime()));
	userData.setNumberOfLogins(4);
	UserDataHandler.storeData(userData);
	userId = UserDataHandler.getUserId(userName);
	userData.setUserId(userId);

	retrievedData = UserDataHandler.getData(token);
	assertTrue(retrievedData != null);
	assertTrue(userData.equals(retrievedData));

	// Attempt retrieving by userId
	retrievedData = UserDataHandler.getData(userId);
	assertTrue(retrievedData != null);
	assertTrue(userData.equals(retrievedData));
	
	// Attempt retrieving by username
	UserData retrievedData1 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData1 != null);
	assertTrue(userData.equals(retrievedData1));

	// set status
	UserDataHandler.setStatus(token, status2);
	UserData retrievedData2 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData2 != null);
	assertFalse(userData.equals(retrievedData2));
	userData.setStatus(status2);
	assertTrue(userData.equals(retrievedData2));
	
	// set dn
	String userDn = "User DN";
	assertTrue(retrievedData2.getUserDN() == null);
	UserDataHandler.setUserDN(token, userDn);
	UserData retrievedData3 = UserDataHandler.getDataForUsername(userName);
	userData.setUserDN(userDn);
	assertTrue(userData.equals(retrievedData3));
	assertTrue(userData.getUserDN().equals(userDn));

	// set password
	UserData retrievedData4 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData4.getPasswordSha1().equals(UserDataHandler.passwordSha1(password)));
	String newPass = "newPassword";
	UserDataHandler.setUserPassword(userName, newPass);
	retrievedData4 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData4.getPasswordSha1().equals(UserDataHandler.passwordSha1(newPass)));
	
	// set status by userName
	UserDataHandler.setStatusForUsername(userName, status3);
	retrievedData4 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData4.getStatus() == status3);

	// try add/removing role from object
	retrievedData3.addRole(role2);
	Vector roles = retrievedData3.getUserRoles();
	assertTrue(roles != null);
	assertTrue(roles.size() == 2);
	roles.contains(new Integer(role1));
	roles.contains(new Integer(role2));

	// Check user nameexists
	assertTrue(UserDataHandler.userNameExists(userName));
	assertFalse(UserDataHandler.userNameExists("Random user"));
    }

    public void userRolesProcessing() throws Exception {
	
	RoleDataHandler.addUserRole(userId, role2);
	UserData retrievedData = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData != null);
	Vector roles = retrievedData.getUserRoles();
	assertTrue(roles != null);
	assertTrue(roles.size() == 2);
	assertTrue(roles.contains(new Integer(role1)));
	assertTrue(roles.contains(new Integer(role2)));

	RoleDataHandler.removeUserRole(userId, role2);
	UserData retrievedData1 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData1 != null);
	roles = retrievedData1.getUserRoles();
	assertTrue(roles != null);
	assertTrue(roles.size() == 1);
	assertTrue(roles.contains(new Integer(role1)));

	// test deleting all roles for a tojen
	RoleDataHandler.addUserRole(userId, role2);
	RoleDataHandler.removeAllUserRoles(userId);

	UserData retrievedData2 = UserDataHandler.getDataForUsername(userName);
	assertTrue(retrievedData2 != null);
	roles = retrievedData2.getUserRoles();
	assertTrue(roles == null);
    }

    public void deleteUserData() throws Exception {
	
	UserDataHandler.deleteData(userId);
	// Attempt retrieving by token
	UserData retrievedData = UserDataHandler.getData(token);
	assertTrue(retrievedData == null);

	// Store it back in to delete
	UserData userData = new UserData(firstName, lastName, contact, 
					 stmtOfWork, userName, UserDataHandler.passwordSha1(password), 
					 institution, projectName, emailAddr, 
					 phoneNum, country, null, null, status1);
	userData.setToken(token);
	UserDataHandler.storeData(userData);

	UserDataHandler.deleteForUsername(userName);
	// Attempt retrieving by token
	UserData retrievedData1 = UserDataHandler.getData(token);
	assertTrue(retrievedData1 == null);
	// assert user name does not exist
	assertFalse(UserDataHandler.userNameExists(userName));
    }

    public void deleteAllRejectedUsers() throws Exception {

	Vector<Integer> addRoles = new Vector<Integer>();
	addRoles.add(role1);
	UserData userData = new UserData(firstName, lastName, contact, 
					 stmtOfWork, userName, UserDataHandler.passwordSha1(password), 
					 institution, projectName, emailAddr, 
					 phoneNum, country, null, null, status1);
	userData.setToken(token);
	userData.addRoles(addRoles);
	UserDataHandler.storeData(userData);
	UserData retrieved1 = UserDataHandler.getData(token);
	assertTrue(retrieved1 != null);
	assertTrue(retrieved1.getUserRoles() != null);
	assertTrue(retrieved1.getUserRoles().size() == 1);
	
	addRoles.add(role2);
	UserData userData1 = new UserData("firstname1", "lastName1", "contact",
					  "stmt", "userNameFoo", UserDataHandler.passwordSha1(password), 
					  institution, projectName, emailAddr, 
					  phoneNum, country, null, null, status3);
	userData1.setToken("tokenFoo");
	userData1.addRoles(addRoles);
	UserDataHandler.storeData(userData1);
	UserData retrieved2 = UserDataHandler.getData("tokenFoo");
	assertTrue(retrieved2 != null);
	assertTrue(retrieved2.getUserRoles() != null);
	assertTrue(retrieved2.getUserRoles().size() == 2);

	UserData userData2 = new UserData("firstname2", "lastname2", 
					  "contact2", "stmt2", "userName2", 
					  UserDataHandler.passwordSha1(password), institution, projectName, 
					  emailAddr, phoneNum, country, null, null, status3);
	userData2.setToken("token2");
	userData2.addRoles(addRoles);
	UserDataHandler.storeData(userData2);
	UserData retrieved3 = UserDataHandler.getData("token2");
	assertTrue(retrieved3 != null);
	assertTrue(retrieved3.getUserRoles() != null);
	assertTrue(retrieved3.getUserRoles().size() == 2);

	UserDataHandler.deleteUsers(status3);

	UserData retrieved4 = UserDataHandler.getData(token);
	assertTrue(retrieved4 != null);

	UserData retrieved5 = UserDataHandler.getData("tokenFoo");
	assertTrue(retrieved5 == null);

	UserData retrieved6 = UserDataHandler.getData("token2");
	assertTrue(retrieved6 == null);
    }
}
