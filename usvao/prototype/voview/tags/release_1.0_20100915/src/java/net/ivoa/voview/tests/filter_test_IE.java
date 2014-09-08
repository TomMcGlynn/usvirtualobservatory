package net.ivoa.voview.tests;

public class filter_test_IE extends FilterTests {
	public void setUp() throws Exception{
		setUp("http://localhost:8080/", "*iexplore");
	};

	public void testFilter_test_IE() throws Exception {
		FilterTestProcedure();
	}
}
