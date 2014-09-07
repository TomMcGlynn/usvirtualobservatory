/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.databaseAccess.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;

import org.globus.purse.registration.StatusData;
import org.globus.purse.registration.databaseAccess.StatusDataHandler;

public class TestStatusDataHandler extends TestCase {

    int id;
    String name = "status1";
    String desc = "Test status";
    String name1 = "status2";
    String desc1 = "desc2";

    public TestStatusDataHandler(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestStatusDataHandler.class);
    }

    public void test() throws Exception {
	storeAndRetrieve();
	deleteData();
    }

    private void storeAndRetrieve() throws Exception {

	StatusData statusData = new StatusData(name, desc);
	StatusDataHandler.storeData(statusData);

	// Attempt retrieving by name
	StatusData retrievedData = StatusDataHandler.getData(name);
	assertTrue(retrievedData != null);

	// set id to the original data 
        id = retrievedData.getId();
	statusData.setId(id);

	// assert that the data match
	assertTrue(statusData.equals(retrievedData));

	// Test get id
	int retrievedId = StatusDataHandler.getId(name);
	assertTrue(id == retrievedId);

	// Add another
	StatusData statusData1 = new StatusData(name1, desc1);
	StatusDataHandler.storeData(statusData1);
	
	// Attempt retrieving by id
	StatusData retrievedData1 = StatusDataHandler.getData(id);
	assertTrue(retrievedData1 != null);
	assertTrue(statusData.equals(retrievedData1));

	// Attempt retrieving name gven id
	String returnedName = StatusDataHandler.getRequestStatus(id);
	assertTrue(returnedName.equals(name));

	// Retrieve all data
	Vector vector = StatusDataHandler.getAllData();
	assertTrue(vector != null);
	assertTrue(vector.size() >= 2);
    System.out.println( (StatusData) vector.get(0));
    System.out.println( retrievedData );
	assertTrue(((StatusData)vector.get(0)).equals(retrievedData));
    }

    private void deleteData() throws Exception {
	
	StatusDataHandler.deleteData(name1);
	// Attempt retrieving by token
	StatusData retrievedData = StatusDataHandler.getData(name1);
	assertTrue(retrievedData == null);
	
	// Delete other data by id
	StatusDataHandler.deleteData(id);
	StatusData retrievedData1 = StatusDataHandler.getData(name);
	assertTrue(retrievedData1 == null);

	// Retrieve all data
	Vector vector = StatusDataHandler.getAllData();
	assertTrue(vector == null);
    }
}
