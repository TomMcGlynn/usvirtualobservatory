package net.ivoa.voview.tests;

public class render_test_FF extends RenderTests {
	public void setUp() throws Exception{
		setUp("http://localhost:8080/", "*firefox C:\\Program Files (x86)\\Mozilla Firefox3\\firefox.exe");
		// setUp("http://heasarcdev.gsfc.nasa.gov/", "*firefox3 /usr1/local/firefox-3.6/firefox-bin");
	};

	public void testRender_test_FF() throws Exception {
		RenderTestProcedure();
	}
}
