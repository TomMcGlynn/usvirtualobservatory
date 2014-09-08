package net.ivoa.voview.tests;

public class render_test_IE extends RenderTests {
	public void setUp() throws Exception{
		setUp("http://localhost:8080/", "*iexplore");
	};

	public void testRender_test_IE() throws Exception {
		RenderTestProcedure();
	}
}
