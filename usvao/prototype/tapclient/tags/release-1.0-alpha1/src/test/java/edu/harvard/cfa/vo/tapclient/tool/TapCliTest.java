package edu.harvard.cfa.vo.tapclient.tool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.harvard.cfa.vo.tapclient.tap.AsyncJob;
import edu.harvard.cfa.vo.tapclient.tap.TapService;
import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class TapCliTest {
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
    
    @Test public void checkStopOptionHelpTest() throws ParseException {
	Options options = new TapCli().getStopOptions();
	String[] args = { "--help", "--foo", "bar" };
	CommandLine commandLine = new TapCli().parseCommandLine(options, args);
	assertTrue(new TapCli().checkStopOption(commandLine, null, options));
    }

    @Test public void checkStopOptionVersionTest() throws ParseException {
	Options options = new TapCli().getStopOptions();
	String[] args = { "--version", "--foo", "bar" };
	CommandLine commandLine = new TapCli().parseCommandLine(options, args);
	assertTrue(new TapCli().checkStopOption(commandLine, null, options));
    }

    @Test public void checkStopOptionVerboseTest() throws ParseException {
	Options options = new TapCli().getStopOptions();
	String[] args = { "--verbose", "--foo", "bar" };
	CommandLine commandLine = new TapCli().parseCommandLine(options, args);
	assertFalse(new TapCli().checkStopOption(commandLine, null, options));
    }

    @Test public void resultsHelpOptionTest() throws Exception {
	Options options = new TapCli().getJobOptions();
	String[] args = { "results", "--help" };
	CommandLine commandLine = new TapCli().parseCommandLine(options, args);
	assertFalse(new TapCli().checkStopOption(commandLine, null, options));
    }

    /*
    @Test public void resultsTest() throws Exception {
	testServer.setResponseBody("<VOTABLE><RESOURCE><TABLE><FIELD name='ra' datatype='double'/><DATA><TABLEDATA><TR>24.2<TD></TD></TR></TABLEDATA></DATA></TABLE></RESOURCE></VOTABLE>".getBytes());
	Options options = new TapCli().getJobOptions();
	String[] args = { "results", "--baseurl", "localhost:7060/tap", "--jobid", "6", "--output", "/dev/null" };
	CommandLine commandLine = new TapCli().parseCommandLine(options, args);
	new TapCli().doResults(commandLine);
	assertTrue(true);    
    }
    */

    /*
    @Test public void shortOptionTest() throws ParseException {
	String[] args = { "-b", "baseurlvalue", "-o", "outputvalue", "-v", "-?", "-V" };
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("b"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("o"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("V"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("v"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("?"));
    }

    @Test public void longOptionTest() throws ParseException {
	String[] args = { "--baseurl", "baseurlvalue", "--output", "outputvalue", "--version", "--help", "--verbose" };
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("b"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("o"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("V"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("v"));
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("?"));
    }

    @Test public void requiredOptionHelpTest() throws ParseException {
	String[] args = { "-o", "outputvalue", "-V", "-?" };
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("?"));
    }

    @Test public void requiredOptionVersionTest() throws ParseException {
	String[] args = { "-o", "outputvalue", "-V", "-v" };
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("v"));
    }

    @Test public void requiredOptionBaseUrlTest() throws ParseException {
	String[] args = { "-o", "outputvalue", "-v", "-b", "baseurlvalue" };
	assertTrue(new TapCli().newParser().parse(TapCli.newOptions(), args).hasOption("b"));
    }

    @Test(expected=ParseException.class) public void requiredOptionMissingTest() throws ParseException {
	String[] args = { "-o", "outputvalue" };
	CommandLine commandLine = new TapCli().newParser().parse(TapCli.newOptions(), args);
	assertTrue(commandLine.hasOption("b"));
	assertTrue(commandLine.hasOption("?"));
	assertTrue(commandLine.hasOption("V"));
    }

    @Test public void printHelpTest() {
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
	new TapCli().printHelp(output, "basename", TapCli.newOptions());
	assertEquals("usage: basename [-?] -b <url> [-o <file>] [-v] [-V]\n\n -?,--help            Display usage information\n -b,--baseurl <url>   TAP service base URL\n -o,--output <file>   Output file\n -v,--verbose         Enable verbose mode.\n -V,--version         Display version information\n\n", outputBuffer.toString());
    }
    
    @Test public void printVersionTest() {
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
	new TapCli().printVersion(output, "basename", "1.3");
	assertEquals("basename (TAP Client API 1.0) 1.3\n", outputBuffer.toString());
    }

    @Test public void printStreamTest() throws IOException {
	String expected = "The expected value";
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
	new TapCli().printStream(output, new ByteArrayInputStream(expected.getBytes()));
	assertEquals(expected, outputBuffer.toString());
    }

    @Test public void nullPrintStreamTest() throws IOException {
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
	try {
	    new TapCli().printStream(output, null);
	} catch (NullPointerException ex) {
	}
	assertEquals("", outputBuffer.toString());
    }

    @Test public void defaultNewPrintStreamTest() throws Exception {
	PrintStream defaultPrintStream = System.out;
	String[] args = { "-b", "baseurlvalue" };
	CommandLine commandLine = new TapCli().newParser().parse(new TapCli().newOptions(), args);
	PrintStream output = new TapCli().newPrintStream(commandLine, "o", defaultPrintStream);
	assertEquals(System.out, output);
    }

    @Test public void fileNewPrintStreamTest() throws Exception {
	String[] args = { "-b", "baseurlvalue", "-o", "/dev/null" };
	CommandLine commandLine = new TapCli().newParser().parse(new TapCli().newOptions(), args);
	PrintStream output = new TapCli().newPrintStream(commandLine, "o", null);
	assertTrue((output!= null));
    }

    @Test public void printJobTest() {
	AsyncJob asyncJob = new AsyncJob(null);
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
        new TapCli().printJob(output, asyncJob);
	assertEquals("", outputBuffer.toString());
    }

    @Test public void allElementsPrintJobTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService("http://localhost:8080/"), "foo");
	asyncJob.handleJobSummaryResponse(new ByteArrayInputStream("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0'><jobId>foo</jobId><runId>runidvalue</runId><ownerId>owneridvalue</ownerId><phase>PENDING</phase><startTime>2011-01-01T01:01:01Z</startTime><endTime>2011-01-01T01:01:01Z</endTime><quote>2011-02-02T00:00:00Z</quote><destruction>2011-01-01T03:03:03Z</destruction><results><result id='id' href='http://localhost:8008/r'/></results><parameters><parameter id='name'>value</parameter></parameters><errorSummary hasDetail='false' type='fatal'><message>message</message></errorSummary></job>".getBytes()));
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
        new TapCli().printJob(output, asyncJob);
	assertEquals("Job id: foo\nOwner id: owneridvalue\nExecution phase: PENDING\nQuote: 2011-02-02T00:00:00Z\nStart time: 2011-01-01T01:01:01Z\nEnd time: 2011-01-01T01:01:01Z\nExecution duration: 0\nDestruction: 2011-01-01T03:03:03Z\nParameter: name=value\nResult: http://localhost:8008/r\nError message: message\nError type: fatal\n", outputBuffer.toString());
    }

    @Test public void noResultsOrParametersprintJobTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService("http://localhost:8080/"), "foo");
	asyncJob.handleJobSummaryResponse(new ByteArrayInputStream("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0'><jobId>foo</jobId><runId>runidvalue</runId><ownerId>owneridvalue</ownerId><phase>PENDING</phase><startTime>2011-01-01T01:01:01Z</startTime><endTime>2011-01-01T01:01:01Z</endTime><quote>2011-02-02T00:00:00Z</quote><destruction>2011-01-01T03:03:03Z</destruction><errorSummary hasDetail='false' type='fatal'><message>message</message></errorSummary></job>".getBytes()));
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
        new TapCli().printJob(output, asyncJob);
	assertEquals("jobId: foo\nRun id: runidvalue\nOwner id: owneridvalue\nExecution phase: PENDING\nStart time: 2011-01-01T01:01:01Z\nEnd time: 2011-01-01T01:01:01Z\nQuote: 2011-02-02T00:00:00Z\nDestruction: 2011-01-01T03:03:03Z\nError message: message\nError type: fatal\n", outputBuffer.toString());
    }

    @Test public void printJobNullErrorElementsTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService("http://localhost:8080/"), "foo");
	asyncJob.handleJobSummaryResponse(new ByteArrayInputStream("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0'><jobId>foo</jobId><runId>runidvalue</runId><ownerId>owneridvalue</ownerId><phase>PENDING</phase><startTime>2011-01-01T01:01:01Z</startTime><endTime>2011-01-01T01:01:01Z</endTime><quote>2011-02-02T00:00:00Z</quote><destruction>2011-01-01T03:03:03Z</destruction><errorSummary hasDetail='false'></errorSummary></job>".getBytes()));
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
        new TapCli().printJob(output, asyncJob);
	assertEquals("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0'><jobId>foo</jobId><runId>runidvalue</runId><ownerId>owneridvalue</ownerId><phase>PENDING</phase><startTime>2011-01-01T01:01:01Z</startTime><endTime>2011-01-01T01:01:01Z</endTime><quote>2011-02-02T00:00:00Z</quote><destruction>2011-01-01T03:03:03Z</destruction><errorSummary hasDetail='false'></errorSummary></job>", outputBuffer.toString());
    }

    @Test public void printJobNullResultsAndParametersTest() throws Exception {
	AsyncJob asyncJob = new AsyncJob(new TapService("http://localhost:8080/"), "foo");
	asyncJob.handleJobSummaryResponse(new ByteArrayInputStream("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0'><jobId>foo</jobId><runId>runidvalue</runId><ownerId>owneridvalue</ownerId><phase>PENDING</phase><startTime>2011-01-01T01:01:01Z</startTime><endTime>2011-01-01T01:01:01Z</endTime><quote>2011-02-02T00:00:00Z</quote><destruction>2011-01-01T03:03:03Z</destruction><results></results><parameters></parameters><errorSummary hasDetail='false' type='fatal'><message>message</message></errorSummary></job>".getBytes()));
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
        new TapCli().printJob(output, asyncJob);
	assertEquals("<job xmlns='http://www.ivoa.net/xml/UWS/v1.0'><jobId>foo</jobId><runId>runidvalue</runId><ownerId>owneridvalue</ownerId><phase>PENDING</phase><startTime>2011-01-01T01:01:01Z</startTime><endTime>2011-01-01T01:01:01Z</endTime><quote>2011-02-02T00:00:00Z</quote><destruction>2011-01-01T03:03:03Z</destruction><results></results><parameters></parameters><errorSummary hasDetail='false' type='fatal'><message>message</message></errorSummary></job>", outputBuffer.toString());
    }
    */
}