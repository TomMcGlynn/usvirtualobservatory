package net.ivoa.voview.tests;

public class render_test_SF extends RenderTests {
	public void setUp() throws Exception{
		setUp("http://localhost:8080/", "*safariproxy");
	};

	public void testRender_test_SF() throws Exception {
		RenderTestProcedure();
	}
}
