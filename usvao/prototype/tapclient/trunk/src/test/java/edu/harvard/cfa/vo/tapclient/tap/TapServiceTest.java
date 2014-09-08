package edu.harvard.cfa.vo.tapclient.tap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class TapServiceTest {
    private static TestServer testServer;

    @BeforeClass public static void setUpClass() throws Exception {
	testServer = new JettyTestServer("/tap", 7060);
	testServer.start();
    }

    @Before public void setUp() {
    }

    @After public void tearDown() {
    }

    @AfterClass public static void tearDownClass() throws Exception {
	testServer.stop();
    }
    
    @Test public void getBaseURLTest() {
	String baseURL = "http://localhost:7060/tap";
	TapService tapService = new TapService(baseURL);
	assertEquals(baseURL, tapService.getBaseURL());
    }

    @Test public void getJobsTest() throws Exception {
	testServer.setResponseBody("<jobs/>".getBytes());
	String baseURL = "http://localhost:7060/tap";
	TapService tapService = new TapService(baseURL);
	assertEquals(0, tapService.getJobs().size());
    }

    @Ignore @Test public void getTableSetFromMetadataTest() throws Exception {
    }
}