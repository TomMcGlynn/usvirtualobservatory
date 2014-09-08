package edu.harvard.cfa.vo.tapclient.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SunComNetTestServer implements TestServer {
    private Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.util.SunComNetTestServer");

    private HttpServer httpServer;
    private MockHttpExchange exchange;

    public SunComNetTestServer() throws UnknownHostException, IOException {
	this(0);
    }
    
    public SunComNetTestServer(int port) throws UnknownHostException, IOException {
	exchange = new DefaultMockHttpExchange();
	httpServer = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), port), 0);
	List<Filter> filters = httpServer.createContext("/myapp", new HttpHandler() {
		public void handle(HttpExchange exchange) throws IOException {
		    if (logger.isLoggable(Level.INFO)) 
			logger.log(Level.INFO, "handle");

		    setRequestMethod(exchange.getRequestMethod());
		    Headers requestHeaders = exchange.getRequestHeaders();
		    Headers requestParameters = (Headers) exchange.getAttribute(ParametersFilter.ATTRIBUTE_NAME);
		    for (String name: requestParameters.keySet()) {
			List<String> parameters = requestParameters.get(name);
			getRequestParameters().put(name, parameters.toArray(new String[parameters.size()]));
		    }

		    if (logger.isLoggable(Level.INFO)) 
			logger.log(Level.INFO, "getResponseCode(): "+getResponseCode());
		    if (logger.isLoggable(Level.INFO)) 
			logger.log(Level.INFO, "getResponseBody(): "+new String(getResponseBody()));
		    Headers responseHeaders = exchange.getResponseHeaders();
		    exchange.sendResponseHeaders(getResponseCode(), getResponseBody().length);
		    OutputStream responseBody = exchange.getResponseBody();
		    responseBody.write(getResponseBody());
		    responseBody.close();
		}
	    }).getFilters();
	filters.add(new LoggingFilter());
	filters.add(new ParametersFilter());
    }
    
    public void start() {
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "Starting server "+getAddress());

	httpServer.setExecutor(null);
	httpServer.start();
    }
    
    public void stop() {
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "Stopping server "+getAddress());

	httpServer.stop(0);
    }
    
    public String getAddress() {
	return "http:/"+httpServer.getAddress()+"/myapp";
    }

    public int getPort() {
	return httpServer.getAddress().getPort();
    }

    public String getRequestMethod() {
	return exchange.getRequestMethod();
    }

    public void setRequestMethod(String newValue) {
	exchange.setRequestMethod(newValue);
    }

    public Map<String,String[]> getRequestHeaders() {
	return exchange.getRequestHeaders();
    }

    public Map<String,String[]> getRequestParameters() {
	return exchange.getRequestParameters();
    }

    public int getResponseCode() {
	return exchange.getResponseCode();
    }

    public void setResponseCode(int newValue) {
	exchange.setResponseCode(newValue);
    }

    public byte[] getResponseBody() {
	return exchange.getResponseBody();
    }

    public void setResponseBody(byte[] newValue) {
	exchange.setResponseBody(newValue);
    }
    
}
