/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.databaseAccess.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;

import org.globus.purse.registration.RoleData;
import org.globus.purse.registration.databaseAccess.RoleDataHandler;

public class TestRoleDataHandler extends TestCase {

    int id;
    String name = "role1";
    String desc = "Test role";
    String name1 = "role2";
    String desc1 = "role2 description";

    public TestRoleDataHandler(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestRoleDataHandler.class);
    }

    public void test() throws Exception {
	storeAndRetrieve();
	deleteData();
    }

    private void storeAndRetrieve() throws Exception {

	RoleData roleData = new RoleData(name, desc);
	RoleDataHandler.storeData(roleData);

	// Attempt retrieving by name
	RoleData retrievedData = RoleDataHandler.getData(name);
	assertTrue(retrievedData != null);

	// set id to the original data 
        id = retrievedData.getId();
	roleData.setId(id);

	// assert that the data match
	assertTrue(roleData.equals(retrievedData));

	// Add another
	RoleData roleData1 = new RoleData(name1, desc1);
	RoleDataHandler.storeData(roleData1);
	
	// Attempt retrieving by id
	RoleData retrievedData1 = RoleDataHandler.getData(id);
	assertTrue(retrievedData1 != null);
	assertTrue(roleData.equals(retrievedData1));

	// Attempt retrieving name gven id
	String returnedName = RoleDataHandler.getRoleName(id);
	assertTrue(returnedName.equals(name));

	// Retrieve all data
	Vector vector = RoleDataHandler.getAllData();
	assertTrue(vector != null);
	assertTrue(vector.size() == 2);
	assertTrue(((RoleData)vector.get(0)).equals(retrievedData));
    }

    private void deleteData() throws Exception {
	
	RoleDataHandler.deleteData(name1);
	// Attempt retrieving by token
	RoleData retrievedData = RoleDataHandler.getData(name1);
	assertTrue(retrievedData == null);
	
	// Delete other data by id
	RoleDataHandler.deleteData(id);
	RoleData retrievedData1 = RoleDataHandler.getData(name);
	assertTrue(retrievedData1 == null);

	// Retrieve all data
	Vector vector = RoleDataHandler.getAllData();
	assertTrue(vector == null);
    }
}
