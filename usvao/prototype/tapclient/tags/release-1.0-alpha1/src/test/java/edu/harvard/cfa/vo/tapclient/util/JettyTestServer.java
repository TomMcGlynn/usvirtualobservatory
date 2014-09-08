package edu.harvard.cfa.vo.tapclient.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.MultiPartFilter;

public class JettyTestServer implements TestServer, MockHttpExchange {
    public static final String BASE_PATH = "/tap";
    public static final int DEFAULT_PORT = 7060;

    private String basePath;
    private int port;
    private Server server;
    private MockHttpExchange exchange;

    public JettyTestServer() {
	this(BASE_PATH, DEFAULT_PORT);
    }

    public JettyTestServer(String basePath, int port) {
	super();

	this.basePath = basePath.startsWith("/") ? basePath : ("/"+basePath);
	this.port = port;

	exchange = new DefaultMockHttpExchange();

	server = new Server(this.port);
	server.setGracefulShutdown(1000);
	server.setStopAtShutdown(true);

	ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/");
	
	servletContextHandler.addFilter(new FilterHolder(new MultiPartFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
	servletContextHandler.addServlet(new ServletHolder(new DefaultServlet() {
		public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    if (Logger.getLogger("TestServer").isLoggable(Level.INFO))
			Logger.getLogger("TestServer").log(Level.INFO, "doPost");

		    setRequestMethod("GET");
		    getRequestParameters().clear();
		    for (java.util.Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
			String name = (String) e.nextElement();
			getRequestParameters().put(name.toUpperCase(), req.getParameterValues(name));
		    }

		    resp.setStatus(getResponseCode());
		    PrintWriter outp = resp.getWriter();
		    outp.write(new String(getResponseBody()));
		}

		public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    if (Logger.getLogger("TestServer").isLoggable(Level.INFO))
			Logger.getLogger("TestServer").log(Level.INFO, "doPost");

		    setRequestMethod("POST");
		    getRequestParameters().clear();
		    for (java.util.Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
			String name = (String) e.nextElement();
			getRequestParameters().put(name, req.getParameterValues(name));
		    }

		    resp.setStatus(getResponseCode());
		    PrintWriter outp = resp.getWriter();
		    outp.write(new String(getResponseBody()));
		}

		public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    if (Logger.getLogger("TestServer").isLoggable(Level.INFO))
			Logger.getLogger("TestServer").log(Level.INFO, "doPut");

		    setRequestMethod("PUT");
		    getRequestParameters().clear();
		    for (java.util.Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
			String name = (String) e.nextElement();
			getRequestParameters().put(name, req.getParameterValues(name));
		    }

		    resp.setStatus(getResponseCode());
		    PrintWriter outp = resp.getWriter();
		    outp.write(new String(getResponseBody()));
		}

		public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    if (Logger.getLogger("TestServer").isLoggable(Level.INFO))
			Logger.getLogger("TestServer").log(Level.INFO, "doDelete");
		    
		    setRequestMethod("DELETE");
		    getRequestParameters().clear();
		    for (java.util.Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
			String name = (String) e.nextElement();
			getRequestParameters().put(name, req.getParameterValues(name));
		    }

		    resp.setStatus(getResponseCode());
		    PrintWriter outp = resp.getWriter();
		    outp.write(new String(getResponseBody()));
		}

		public void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		    if (Logger.getLogger("TestServer").isLoggable(Level.INFO))
			Logger.getLogger("TestServer").log(Level.INFO, "doHead");

		    setRequestMethod("HEAD");
		    getRequestParameters().clear();
		    for (java.util.Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
			String name = (String) e.nextElement();
			getRequestParameters().put(name, req.getParameterValues(name));
		    }

		    resp.setStatus(getResponseCode());
		    PrintWriter outp = resp.getWriter();
		    outp.write(new String(getResponseBody()));
		}

	    }), this.basePath+"/*");
    }

    public String getAddress() {
	return "http://localhost:"+getPort()+basePath;
    }

    public int getPort() {
	return port;
    }

    public void start() throws Exception {
	start(false);
    }

    public void start(boolean join) throws Exception {
	Logger.getLogger("TestServer").log(Level.INFO, "Starting server");
	server.start();
	if (join)
	    server.join();
    }

    public void stop() throws Exception {
	Logger.getLogger("TestServer").log(Level.INFO, "Stopping server");
	server.stop();
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
    
    public static void main(String[] args) {
	int exitStatus = 0;

	try {
	    final TestServer testServer = new JettyTestServer();
	    
	    Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
			if (testServer != null) {
			    try {
				testServer.stop();
			    } catch (Exception ex) {
				Logger.getLogger("TestServer").log(Level.WARNING, "Error stopping server", ex);
			    }
			}
		    }
		});

	    ((JettyTestServer) testServer).start(true);


	} catch (Throwable ex) {
	    ex.printStackTrace(System.err);
	    exitStatus = 1;

	}

	System.exit(exitStatus);
    }
}
