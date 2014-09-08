package net.ivoa.voview.tests;

public class filter_test_SF extends FilterTests {
	public void setUp() throws Exception{
		setUp("http://localhost:8080/", "*safariproxy");
	};

	public void testFilter_test_SF() throws Exception {
		FilterTestProcedure();
	}
}
