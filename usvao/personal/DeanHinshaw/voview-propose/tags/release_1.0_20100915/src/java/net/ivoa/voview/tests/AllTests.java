package net.ivoa.voview.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(filter_test_IE.class);
		suite.addTestSuite(filter_test_FF.class);
		//$JUnit-END$
		return suite;
	}

}
