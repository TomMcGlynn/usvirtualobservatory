package net.ivoa.voview.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllFilterTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllFilterTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(filter_test_IE.class);
		suite.addTestSuite(filter_test_FF.class);
		suite.addTestSuite(filter_test_SF.class);
		//$JUnit-END$
		return suite;
	}

}
