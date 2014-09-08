package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;
import edu.harvard.cfa.vo.tapclient.util.JettyTestServer;
import edu.harvard.cfa.vo.tapclient.util.TestServer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class AvailabilityTest {
    private static TestServer testServer;

    private static String available;
    private static String upSince;
    private static String downAt;
    private static String backAt;
    private static String[] notes;

    private static Calendar expected;

    @BeforeClass public static void setUpClass() throws Exception {
	testServer = new JettyTestServer();
	testServer.start();

	expected = GregorianCalendar.getInstance();
	expected.set(2011, Calendar.JULY, 06, 12, 12, 12);
	expected.set(Calendar.MILLISECOND, 0);
	expected.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }
    
    @Before public void setUp() {
	testServer.setRequestMethod(null);
	testServer.getRequestParameters().clear();
	testServer.setResponseCode(200);
	testServer.setResponseBody("OK".getBytes());

	available = "false";
	upSince = null;
	downAt = null;
	backAt = null;
	notes = null;
    }

    @After public void tearDown() {
	testServer.setRequestMethod(null);
	testServer.getRequestParameters().clear();
	testServer.setResponseCode(-1);
	testServer.setResponseBody(null);

	available = null;
	upSince = null;
	downAt = null;
	backAt = null;
	notes = null;
     }

    @AfterClass public static void tearDownClass() throws Exception {
	testServer.stop();
    }

    static String getResponseBuffer() {
	StringBuilder sb = new StringBuilder();
	sb.append("<availability xmlns='http://www.ivoa.net/xml/VOSIAvailability/v1.0'>");

	if (available != null) {
	    sb.append("<available>");
	    sb.append(available);
	    sb.append("</available>");
	}

	if (upSince != null) {
	    sb.append("<upSince>");
	    sb.append(upSince);
	    sb.append("</upSince>");
	}

	if (downAt != null) {
	    sb.append("<downAt>");
	    sb.append(downAt);
	    sb.append("</downAt>");
	}

	if (backAt != null) {
	    sb.append("<backAt>");
	    sb.append(backAt);
	    sb.append("</backAt>");
	}

	if (notes != null) {
	    for (String note: notes) {
		sb.append("<note>");
		sb.append(note);
		sb.append("</note>");
	    }
	}

	sb.append("</availability>");

	return sb.toString();
    }

    /**
     * Test where available is false.
     */
    @Test public void isNotAvailableTest() throws HttpException, ResponseFormatException, IOException {
	available = "false";

	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();
	assertEquals(false, availability.isAvailable());
    }

    /**
     * Test available is true.
     */
    @Test public void isAvailableTest() throws HttpException, ResponseFormatException, IOException {
	available = "true";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();
	assertEquals(true, availability.isAvailable());
    }

    /**
     * Test available is not a valid value.
     */
    @Test(expected=ResponseFormatException.class) public void invalidIsAvailableTest() throws HttpException, ResponseFormatException, IOException {
	available = "foobar";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();
	assertEquals(true, availability.isAvailable());
    }

    /**
     * Test where upSince is not null
     */
    @Test public void notNullUpSinceTest() throws HttpException, ResponseFormatException, IOException {
	upSince = "2011-07-06T12:12:12Z";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertEquals(0, expected.compareTo(availability.getUpSince()));
    }

    /**
     * Test where upSince is null
     */
    @Test public void nullUpSinceTest() throws HttpException, ResponseFormatException, IOException {
	upSince = null;
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertNull(availability.getUpSince());
    }

    /**
     * Test where upSince is invalid
     */
    @Test public void invalidUpSinceTest() throws HttpException, ResponseFormatException, IOException {
	try {
	upSince = "2011-07-06 12:12:12Z";
	//                   ^ missing 'T'
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);

	assertEquals(null, availability.getUpSince());
	} catch (RuntimeException ex) {
	    ex.printStackTrace();
	    throw ex;
	}
    }

    /**
     * Test where downAt is not null
     */
    @Test public void notNullDownAtTest() throws HttpException, ResponseFormatException, IOException {
	downAt = "2011-07-06T12:12:12Z";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertEquals(0, expected.compareTo(availability.getDownAt()));
    }

    /**
     * Test where downAt is null
     */
    @Test public void nullDownAtTest() throws HttpException, ResponseFormatException, IOException {
	downAt = null;
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertNull(availability.getDownAt());
    }

    /**
     * Test where downAt is invalid
     */
    @Test public void invalidDownAtTest() throws HttpException, ResponseFormatException, IOException {
	downAt = "2011-07-06 12:12:12Z";
	//                   ^ missing 'T'
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertEquals(null, availability.getDownAt());
    }

    /**
     * Test where backAt is not null
     */
    @Test public void notNullBackAtTest() throws HttpException, ResponseFormatException, IOException {
	backAt = "2011-07-06T12:12:12Z";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertEquals(0, expected.compareTo(availability.getBackAt()));
    }

    /**
     * Test where backAt is null
     */
    @Test public void nullBackAtTest() throws HttpException, ResponseFormatException, IOException {
	backAt = null;
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertNull(availability.getBackAt());
    }

    /**
     * Test where backAt is invalid
     */
    @Test public void invalidBackAtTest() throws HttpException, ResponseFormatException, IOException {
	backAt = "2011-07-06 12:12:12Z";
	//                   ^ missing 'T'
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	expected.set(Calendar.SECOND, 12);
	assertEquals(null, availability.getBackAt());
    }

    @Test(expected=HttpException.class) public void httpExceptionTest() throws HttpException, ResponseFormatException, IOException {
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress().substring(0, testServer.getAddress().length()-1));
	availability.update();
    }

    @Test(expected=ResponseFormatException.class) public void responseFormatExceptionTest() throws HttpException, ResponseFormatException, IOException {
	available = "<invalid_element/>";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();
    }

    @Ignore @Test public void ioExceptionTest() throws HttpException, ResponseFormatException, IOException {
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	// Need to trigger an IOException

	assertEquals(true, false);
    }

    @Test(expected=RuntimeException.class) public void nullConstructorTest() throws HttpException, ResponseFormatException, IOException {
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(null);
	availability.update();
 	assertEquals(true, false);
    }

    @Test public void printAvailabilityTest() throws IOException {
	available = "true";
	upSince = "2011-07-06T12:12:12Z";
	downAt = "2011-07-06T12:12:12Z";
	backAt = "2011-07-06T12:12:12Z";
	notes = new String[2];
	notes[0] = "A note";
	notes[1] = "Another note";
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();

	assertEquals(2, availability.getNotes().size());

	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
	availability.list(output);
	assertEquals("Available: true\nUp since: 2011-07-06T12:12:12Z\nDown at: 2011-07-06T12:12:12Z\nBack at: 2011-07-06T12:12:12Z\nNote: A note\nNote: Another note\n", outputBuffer.toString());
    }

    @Test public void nullPrintAvailabilityTest() throws IOException {
	testServer.setResponseBody(getResponseBuffer().getBytes());

	Availability availability = new Availability(testServer.getAddress());
	availability.update();
	ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
	PrintStream output = new PrintStream(outputBuffer);
	availability.list(output);
	assertEquals("Available: false\n", outputBuffer.toString());
    }
}
