package net.ivoa.voview.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllRenderTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllRenderTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(render_test_IE.class);
		suite.addTestSuite(render_test_FF.class);
		suite.addTestSuite(render_test_SF.class);
		//$JUnit-END$
		return suite;
	}

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
