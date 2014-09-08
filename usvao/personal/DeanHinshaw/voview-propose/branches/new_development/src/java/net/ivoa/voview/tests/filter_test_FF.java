package net.ivoa.voview.tests;

public class filter_test_FF extends FilterTests {
	public void setUp() throws Exception{
		setUp("http://localhost:8080/", "*firefox");
	};

	public void testFilter_test_FF() throws Exception {
		FilterTestProcedure();
	}
}
