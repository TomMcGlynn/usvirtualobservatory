/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/

package org.globus.purse.registration.databaseAccess.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Vector;

import org.globus.purse.registration.RAData;
import org.globus.purse.registration.databaseAccess.RADataHandler;

public class TestRADataHandler extends TestCase {

    int id;
    String name = "ra1";
    String email = "ra1Email";
    String desc = "Test RA1";
    String name1 = "ra2";
    String email1 = "ra2Email";
    String desc1 = "Test RA2";

    public TestRADataHandler(String name){
	super(name);
    }

    public static Test suite() {
        return new TestSuite(TestRADataHandler.class);
    }

    public void test() throws Exception {
	storeAndRetrieve();
	deleteData();
    }

    private void storeAndRetrieve() throws Exception {

	RAData raData = new RAData(name, email, desc);
	RADataHandler.storeData(raData);

	// Attempt retrieving by name
	RAData retrievedData = RADataHandler.getDataByName(name);
	assertTrue(raData != null);

	// set id to the original data 
        id = retrievedData.getId();
	raData.setId(id);

	// assert that the data match
	assertTrue(raData.equals(retrievedData));

	// Add another
	RAData raData1 = new RAData(name1, email1, desc1);
	RADataHandler.storeData(raData1);
	
	// Attempt retrieving by id
	RAData retrievedData1 = RADataHandler.getData(id);
	assertTrue(retrievedData1 != null);
	assertTrue(raData.equals(retrievedData1));

	// Attempt retrieving name gven id
	String returnedEmail = RADataHandler.getEmailAddress(id);
	assertTrue(returnedEmail.equals(email));

        returnedEmail = RADataHandler.getEmailAddress(name);
        assertTrue(returnedEmail.equals(email));
                
	// Retrieve all data
	Vector vector = RADataHandler.getAllData();
	assertTrue(vector != null);
	assertTrue(vector.size() == 2);
	assertTrue(((RAData)vector.get(0)).equals(retrievedData));
    }

    private void deleteData() throws Exception {
	
	RADataHandler.deleteData(name1);
	// Attempt retrieving by token
	RAData retrievedData = RADataHandler.getDataByName(name1);
	assertTrue(retrievedData == null);
	
	// Delete other data by id
	RADataHandler.deleteData(id);
	RAData retrievedData1 = RADataHandler.getDataByName(name);
	assertTrue(retrievedData1 == null);

	// Retrieve all data
	Vector vector = RADataHandler.getAllData();
	assertTrue(vector == null);
    }
}
