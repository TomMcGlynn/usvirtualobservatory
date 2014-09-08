package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;
import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;

public class AsyncJobTest {
    private static TestServer testServer;
    private static TapService tapService;

    @BeforeClass public static void setUpClass() throws Exception {
	testServer = new JettyTestServer("/tap/async", 7060);
	testServer.start();
    }

    @Before public void setUp() {
	tapService = new TapService("http://localhost:7060/tap");
    }

    @After public void tearDown() throws Exception {
	tapService = null;
    }

    @AfterClass public static void tearDownClass() throws Exception {
	testServer.stop();
    }

    @Test public void setFormatTest() throws Exception{
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setFormat("votable");
	assertEquals("votable", asyncJob.getParameters().get("FORMAT"));
    }    

    @Test public void setLangTest() throws Exception{
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setLang("ADQL");
	assertEquals("ADQL", asyncJob.getParameters().get("LANG"));
    }    

    @Test public void setMaxRecTest() throws Exception{
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setMaxRec(18);
	assertEquals("18", asyncJob.getParameters().get("MAXREC"));
    }    

    @Test public void setQueryTest() throws Exception{
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setQuery("select * from foo");
	assertEquals("select * from foo", asyncJob.getParameters().get("QUERY"));
    }    

    @Test public void setRunidTest() throws Exception{
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setRunId("A run identifier");
	assertEquals("A run identifier", asyncJob.getParameters().get("RUNID"));
    }    

    @Test public void setUploadTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setUpload("tablename", new URI("http://localhost:8080/a_votable.xml"));
	assertEquals("tablename,http://localhost:8080/a_votable.xml", asyncJob.getParameters().get("UPLOAD"));
    }
    
    @Test public void addUploadTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setUpload("tablename", new URI("http://localhost:8080/a_votable.xml"));
	asyncJob.addUpload("tablename2", new URI("http://localhost:8080/another_votable.xml"));
	assertEquals("tablename2,http://localhost:8080/another_votable.xml;tablename,http://localhost:8080/a_votable.xml", asyncJob.getParameters().get("UPLOAD"));
    }
    
    @Test public void setInlineUploadTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setInlineUpload("tablename", new URI("http://localhost:8080/a_votable.xml"));
	assertEquals("tablename,param:tablename", asyncJob.getParameters().get("UPLOAD"));
    }
    
    @Test public void addInlineUploadTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setInlineUpload("tablename", new URI("http://localhost:8080/a_votable.xml"));
	asyncJob.addInlineUpload("tablename2", new URI("http://localhost:8080/another_votable.xml"));
	assertEquals("tablename2,param:tablename2;tablename,param:tablename", asyncJob.getParameters().get("UPLOAD"));
    }

    @Test public void createJobOnServerTest() throws Exception {
	testServer.setResponseBody("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><jobId>18</jobId><ownerId xsi:nil='true'/><phase>PENDING</phase><quote xsi:nil='true'/><startTime xsi:nil='true'/><endTime xsi:nil='true'/><executionDuration>0</executionDuration><destruction xsi:nil='true'/><results></results></job>".getBytes());
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()));
	asyncJob.setLang("ADQL");
	asyncJob.synchronize();

	assertEquals("18", asyncJob.getJobId());
	assertEquals(0, asyncJob.getParameters().size());
    }

    @Test public void updateParametersOnServerTest() throws Exception {
	testServer.setResponseBody("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><jobId>18</jobId><ownerId xsi:nil='true'/><phase>PENDING</phase><quote xsi:nil='true'/><startTime xsi:nil='true'/><endTime xsi:nil='true'/><executionDuration>0</executionDuration><destruction xsi:nil='true'/><results></results></job>".getBytes());
	AsyncJob asyncJob = new AsyncJob(new TapService(testServer.getAddress()), "18");
	asyncJob.setLang("ADQL");
	asyncJob.synchronize();
	
	assertEquals("18", asyncJob.getJobId());
	assertEquals(0, asyncJob.getParameters().size());
    }

    @Test public void updateExecutionDurationOnServerTest() {
    }

    @Test public void updateDestructionOnServerTest() {
    }

    @Test public void updateExecutionPhaseOnServerTest() {
    }

    @Test public void readJobFromServerTest() {
    }
}