package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URI;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;
import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;
import edu.harvard.cfa.vo.tapclient.vosi.Availability;

public class SyncJobTest {
    private static TestServer testServer;
    //    private static HttpServer server;
    
    @BeforeClass public static void setUp() throws Exception {
	testServer = new JettyTestServer("/tap/sync", 7060);
	testServer.start();
	/*
	server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 0), 0);

	server.createContext("/tap/sync", new HttpHandler() {
		public void handle(HttpExchange exchange) throws IOException {
		    String requestMethod = exchange.getRequestMethod();

		    if ("GET".equals(requestMethod) || "POST".equals(requestMethod)) {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.add("Content-Encoding", "UTF-8");
			responseHeaders.add("Content-Type", "text/xml");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write("<?xml version='1.0' encoding='UTF-8' ?><VOTABLE><RESOURCE><INFO name='QUERY_STATUS' value='ERROR'>This is a test service.  There is no actual tableset to query.</INFO></RESOURCE></VOTABLE>".getBytes());
			responseBody.close();

		    } else {
			Headers responseHeaders = exchange.getResponseHeaders();
			responseHeaders.add("Accept", "GET");
			responseHeaders.add("Accept", "POST");
			responseHeaders.add("Content-Encoding", "UTF-8");
			responseHeaders.add("Content-Type", "text/html");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, 0);
			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write("Method Not Allowed".getBytes());
			responseBody.close();	
		    }
		}
	    });

	server.setExecutor(null);
	server.start();
	*/
    }

    @AfterClass public static void tearDown() throws Exception {
	testServer.stop();
    }

    @Test public void runTest() throws HttpException, ResponseFormatException, IOException {
	testServer.setResponseBody("<?xml version='1.0' encoding='UTF-8' ?><VOTABLE><RESOURCE><INFO name='QUERY_STATUS' value='ERROR'>This is a test service.  There is no actual tableset to query.</INFO></RESOURCE></VOTABLE>".getBytes());

	TapService tapService = new TapService("http://localhost:7060/tap");
	SyncJob syncJob = new SyncJob(tapService);

	InputStream inputStream = syncJob.run();
	assertNotNull(inputStream);
	while (-1 != inputStream.read()) {
	}
	inputStream.close();
    } 

    @Test(expected=HttpException.class) public void notFoundTest() throws HttpException, ResponseFormatException, IOException {
	TapService tapService = new TapService("http://localhost:7060/");
	SyncJob syncJob = new SyncJob(tapService);

	syncJob.run();
    } 

    /*
    @Test public void setParameterTest() {
	HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 0), 0);
	server.createContext("/tap/sync", new HttpHandler() {
		public void handle(HttpExchange exchange) throws IOException {
		    Headers parameters = (Headers) exchange.getAttribute("parameters");
		    assertTrue(parameters.contains("foo"));
		    List<String> values = parameters.get("foo");
		    assertEquals(1, values.size());
		    assertEquals("bar", values.get(0));
		}
	    }).getFilters().add(new ParametersFilter());

	TapService tapService = new TapService("http:/"+server.getAddress()+"/tap");
	SyncJob syncJob = new SyncJob(tapService);
	syncJob.setParameter("foo", "bar");

	syncJob.run();
    }
    */

    public class ParameterFilter extends Filter {
	private String name;
	private String value;

	public ParameterFilter(String expectedName, String expectedValue) {
	    name = expectedName;
	    value = expectedValue;
	}

	public void setExpectedName(String expectedName) {
	    name = expectedName;
	}

	public void setExpectedValue(String expectedValue) {
	    value = expectedValue;
	}

	public String description() {
	    return "ParameterFilter";
	}

	public void doFilter(HttpExchange exchange, Filter.Chain chain) throws IOException {
	    URI requestURI = exchange.getRequestURI();

	    String rawQuery = requestURI.getRawQuery();
	    System.out.println("rawQuery: "+rawQuery);
	    String[] pairs = rawQuery.split("&");

	    Headers parameters = new Headers();
	    for (String pair: pairs) {
		String[] nameValue = pair.split("=");
		parameters.add(nameValue[0], nameValue[1]);
	    }
	    
	    exchange.setAttribute("parameters", parameters);

	    chain.doFilter(exchange);
	}
    }
}