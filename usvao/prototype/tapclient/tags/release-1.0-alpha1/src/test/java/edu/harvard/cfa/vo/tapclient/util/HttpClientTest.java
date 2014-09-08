package edu.harvard.cfa.vo.tapclient.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpClientTest {
    private static TestServer testServer;

    @BeforeClass public static void setUpClass() throws Exception {
	testServer = new JettyTestServer();
	//	testServer = new SunComNetTestServer();
	testServer.start();
    }

    @AfterClass public static void tearDownClass() throws Exception {
	testServer.stop();
    }

    @Before public void setUp() {
	testServer.setRequestMethod(null);
	testServer.getRequestParameters().clear();
	testServer.setResponseCode(200);
	testServer.setResponseBody("OK".getBytes());
    }

    @After public void tearDown() {
	testServer.setRequestMethod(null);
	testServer.getRequestParameters().clear();
	testServer.setResponseCode(-1);
	testServer.setResponseBody(null);
    }

    @Test(expected=NullPointerException.class) public void nullGetTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().get(null);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
    }

    @Test(expected=NullPointerException.class) public void nullWithMapGetTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().get(null, Collections.singletonMap("name", "value"));
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
    }

    @Test(expected=NullPointerException.class) public void nullPostTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(null);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
    }

    @Test(expected=NullPointerException.class) public void nullWithMapPostTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(null, Collections.singletonMap("name", "value"));
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
    }

    @Test(expected=NullPointerException.class) public void nullWithMapAndInlinePostTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(null, Collections.singletonMap("name", "value"), Collections.singletonMap("inline", (URI) null));
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
    }

    @Test(expected=NullPointerException.class) public void nullDeleteTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().delete(null);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
    }

    @Test public void getTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().get(testServer.getAddress());
	} catch (HttpException ex) {
	    ex.printStackTrace();
	    throw ex;
	} catch (ResponseFormatException ex) {
	    ex.printStackTrace();
	    throw ex;
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw ex;
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}

	assertEquals("GET", testServer.getRequestMethod());
    }

    @Test public void withMapGetTest() throws Throwable {
	InputStream inputStream = null;
	try {
	    Map<String,String> parameters = new HashMap<String,String>();
	    parameters.put("aa", "bb");
	    parameters.put("cc", "dd");

	    new HttpClient().get(testServer.getAddress(), parameters);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}

	assertEquals("GET", testServer.getRequestMethod());
	assertEquals(2, testServer.getRequestParameters().size());
    }

    @Test public void withEmptyMapGetTest() throws Throwable {
	InputStream inputStream = null;
	try {
	    Map<String,String> parameters = new HashMap<String,String>();
	    new HttpClient().get(testServer.getAddress(), parameters);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
	
	assertEquals("GET", testServer.getRequestMethod());
	assertEquals(0, testServer.getRequestParameters().size());
    }

    @Test public void withNullMapGetTest() throws Throwable {
	InputStream inputStream = null;
	try {
	    Map<String,String> parameters = null;
	    new HttpClient().get(testServer.getAddress(), parameters);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}

	

	assertEquals("GET", testServer.getRequestMethod());
	assertEquals(0, testServer.getRequestParameters().size());
    }

    @Test public void postTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(testServer.getAddress());
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}

 	assertEquals("POST", testServer.getRequestMethod());
	assertEquals(0, testServer.getRequestParameters().size());
   }

    @Test public void withMapPostTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(testServer.getAddress(), Collections.singletonMap("name", "value"));
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
	assertEquals("POST", testServer.getRequestMethod());
	assertEquals(1, testServer.getRequestParameters().size());
    }

    @Test public void withEmptyMapPostTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(testServer.getAddress(), Collections.emptyMap());
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
	assertEquals("POST", testServer.getRequestMethod());
	assertEquals(0, testServer.getRequestParameters().size());
    }

    @Test public void withNullMapPostTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(testServer.getAddress(), null);
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
	assertEquals("POST", testServer.getRequestMethod());
	assertEquals(0, testServer.getRequestParameters().size());
    }

    @Test public void withMapAndInlinePostTest() throws HttpException, ResponseFormatException, IOException, Throwable {
	InputStream inputStream = null;
	try {
	    new HttpClient().post(testServer.getAddress(), Collections.singletonMap("name", "value"), Collections.singletonMap("inline", new URI(testServer.getAddress())));
	} catch (Throwable th) {
	    th.printStackTrace();
	    throw th;
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}

 	assertEquals("POST", testServer.getRequestMethod());
	assertEquals(2, testServer.getRequestParameters().size());
    }

    @Test public void deleteTest() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    new HttpClient().delete(testServer.getAddress());
	} finally {
 	    if (inputStream != null) 
		inputStream.close();
	}
 	assertEquals("DELETE", testServer.getRequestMethod());
    }

    @Test public void asStringTest() throws IOException {
	assertNull(new HttpClient().asString(null));
    }

    @Test public void handleResponseTest() throws IOException {
	assertNull(new HttpClient().handleResponse(null));
    }

    public static void main(String[] args) {
	try {
	    HttpClientTest.setUpClass();
	    Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
			try {
			    HttpClientTest.tearDownClass();
			} catch (Exception ex) {
			    java.util.logging.Logger.getLogger("HttpClientTest").log(java.util.logging.Level.WARNING, "Error stopping server", ex);
			}
		    }
		});
	} catch (Throwable ex) {
	    ex.printStackTrace();
	}
    }
}