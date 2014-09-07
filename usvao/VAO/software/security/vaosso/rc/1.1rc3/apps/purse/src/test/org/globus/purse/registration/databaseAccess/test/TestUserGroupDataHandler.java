/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.databaseAccess.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;

import org.globus.purse.registration.UserGroupData;
import org.globus.purse.registration.databaseAccess.UserGroupDataHandler;

public class TestUserGroupDataHandler extends TestCase {

    int id;
    String name = "group1";
    String desc = "Test group 1";
    String name1 = "group2";
    String desc1 = "Test Group 2";

    public TestUserGroupDataHandler(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestUserGroupDataHandler.class);
    }

    public void test() throws Exception {
	storeAndRetrieve();
	deleteData();
    }

    private void storeAndRetrieve() throws Exception {

	UserGroupData userGpData = new UserGroupData(name, desc);
	UserGroupDataHandler.storeData(userGpData);

	// Attempt retrieving by name
	UserGroupData retrievedData = UserGroupDataHandler.getData(name);
	assertTrue(retrievedData != null);

	// set id to the original data 
        id = retrievedData.getId();
	userGpData.setGroupId(id);

	// assert that the data match
	assertTrue(userGpData.equals(retrievedData));

	// Test get id
	int retrievedId = UserGroupDataHandler.getId(name);
	assertTrue(id == retrievedId);

	// Add another
	UserGroupData userGpData1 = new UserGroupData(name1, desc1);
	UserGroupDataHandler.storeData(userGpData1);
	
	// Attempt retrieving by id
	UserGroupData retrievedData1 = UserGroupDataHandler.getData(id);
	assertTrue(retrievedData1 != null);
	assertTrue(userGpData.equals(retrievedData1));

	// Retrieve all data
	Vector vector = UserGroupDataHandler.getAllData();
	assertTrue(vector != null);
	assertTrue(vector.size() == 2);
	assertTrue(((UserGroupData)vector.get(0)).equals(retrievedData));
    }

    private void deleteData() throws Exception {
	
	UserGroupDataHandler.deleteData(name1);
	// Attempt retrieving by token
	UserGroupData retrievedData = UserGroupDataHandler.getData(name1);
	assertTrue(retrievedData == null);
	
	// Delete other data by id
	UserGroupDataHandler.deleteData(id);
	UserGroupData retrievedData1 = UserGroupDataHandler.getData(name);
	assertTrue(retrievedData1 == null);

	// Retrieve all data
	Vector vector = UserGroupDataHandler.getAllData();
	assertTrue(vector == null);
    }
}
