package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;
import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TableSetTest {
    //   private static TestServer testServer;
    private static Server server;

    private String id;
    private String resource;
    private int nSchemas;
    private int nTables;

    @Parameters public static Collection<Object[]> newParameters() {
	return Arrays.asList(new Object[][] {
		{ "cadc", "/cadcwww.dao.nrc.ca/caom/tables", 4, -1 },
		{ "cda",  "/cdatest.cfa.harvard.edu/csctap/tables", 2, 3 },
		{ "zah",  "/dc.zah.uni-heidelberg.de/__system__/tap/run/tables", 37, -1 },
		{ "wfau", "/wfaudata.roe.ac.uk/xmm-dsa/TAP/tables", 1, -1 }
	    });
    }
    
    public TableSetTest(String id, String resource, int nSchemas, int nTables) {
	this.id = id;
	this.resource = resource;
	this.nSchemas = nSchemas;
	this.nTables = nTables;
    }
    
    @BeforeClass public static void setUpClass() throws Exception {
//	testServer = new JettyTestServer();
//	testServer.start();
	server = new Server(7060);
	server.setGracefulShutdown(1000);
	server.setStopAtShutdown(true);
    }

    @Before public void setUp() throws Exception {
	server.setHandler(new AbstractHandler() {
		public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		    Reader reader = null;
		    Writer writer = null;
		    try {
			URL url = getClass().getResource(resource);
			
			reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			    
			response.setContentType("text/xml");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);

			writer = response.getWriter();
			
			int c = -1;
			while ((c = reader.read()) != -1) {
			    writer.write(c);		
			}
			
			writer.flush();
			
		    } catch (IOException ioe) {
			ioe.printStackTrace();
			throw ioe;
		    } finally {
			if (reader != null) {
			    try {
				reader.close();
			    } catch (IOException ex) {
				ex.printStackTrace();
			    }
			}
			
			if (writer != null) {
			    try {
				writer.close();
			    } catch (IOException ex) {
				ex.printStackTrace();
			    }
			} 
		    }
		}
	    });
	server.start();
    }
    
//    @Before public void setUp() {
//	testServer.setRequestMethod(null);
//	testServer.getRequestParameters().clear();
//	testServer.setResponseCode(200);
//	testServer.setResponseBody("OK".getBytes());
//    }
    
    @After public void tearDown() throws Exception {
	server.stop();
//	testServer.setRequestMethod(null);
//	testServer.getRequestParameters().clear();
//	testServer.setResponseCode(-1);
//	testServer.setResponseBody(null);
    }
    
    @AfterClass public static void tearDownClass() {
	//	testServer.stop();
    }
    
    /**
     * 
     */
    @Test public void getSchemasTest() throws HttpException, ResponseFormatException, IOException {
	try {
	    TableSet tableSet = new TableSet("http://localhost:7060/tap/tables");
	    tableSet.update();
	    assertEquals("getSchemasTest: "+id, nSchemas, tableSet.getSchemas().size());
	} catch (HttpException ex) {
	    ex.printStackTrace();
	    throw ex;
	} catch (ResponseFormatException ex) {
	    ex.printStackTrace();
	    throw ex;
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw ex;
	}    
    }
}
