package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

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
import edu.harvard.cfa.vo.tapclient.vosi.AccessURL;
import edu.harvard.cfa.vo.tapclient.vosi.Capabilities;
import edu.harvard.cfa.vo.tapclient.vosi.Capability;
import edu.harvard.cfa.vo.tapclient.vosi.Interface;
import edu.harvard.cfa.vo.tapclient.vosi.Validation;

public class CapabilitiesTest {
    private static TestServer testServer;

    @BeforeClass public static void setUpClass() throws Exception {
	testServer = new JettyTestServer("/tap/capabilities", 7060);
	testServer.start();
    }


    @Before public void setUp() {
	testServer.setResponseBody(("<?xml version='1.0' encoding='UTF-8' ?><capabilities xmlns='http://www.ivoa.net/xml/VOSICapabilities/v1.0' xmlns:vr='http://www.ivoa.net/xml/VOResource/v1.0' xmlns:vs='http://www.ivoa.net/xml/VODataService/v1.1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><capability standardID='ivo://www.ivoa.net/std/TAP'><validationLevel validatedBy='ivo://some.one/special'>0</validationLevel><description>TAP capability</description><interface xsi:type='vs:ParamHTTP' version='1.0' role='std'><accessURL use='base'>http://localhost:7060/tap</accessURL></interface></capability></capabilities>").getBytes());
    }

    @AfterClass public static void tearDownClass() throws Exception {
	testServer.stop();
    }

    @Test public void getCapabilitiesTest() throws HttpException, ResponseFormatException, IOException {
	testServer.setResponseBody(("<?xml version='1.0' encoding='UTF-8' ?><capabilities xmlns='http://www.ivoa.net/xml/VOSICapabilities/v1.0' xmlns:vr='http://www.ivoa.net/xml/VOResource/v1.0' xmlns:vs='http://www.ivoa.net/xml/VODataService/v1.1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><capability standardID='ivo://www.ivoa.net/std/TAP'><validationLevel validatedBy='ivo://some.one/special'>0</validationLevel><description>TAP capability</description><interface xsi:type='vs:ParamHTTP' version='1.0' role='std'><accessURL use='base'>http://localhost:7060/tap</accessURL></interface></capability></capabilities>").getBytes());
	
	Capabilities capabilities = new TapService("http://localhost:7060/tap").getCapabilities();

	assertFalse(capabilities.getCapabilities().isEmpty());
    } 

    @Test public void getCapabilityTest() throws HttpException, ResponseFormatException, IOException {
	testServer.setResponseBody(("<?xml version='1.0' encoding='UTF-8' ?><capabilities xmlns='http://www.ivoa.net/xml/VOSICapabilities/v1.0' xmlns:vr='http://www.ivoa.net/xml/VOResource/v1.0' xmlns:vs='http://www.ivoa.net/xml/VODataService/v1.1' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><capability standardID='ivo://www.ivoa.net/std/TAP'><validationLevel validatedBy='ivo://some.one/special'>0</validationLevel><description>TAP capability</description><interface xsi:type='vs:ParamHTTP' version='1.0' role='std'><accessURL use='base'>http://localhost:7060/tap</accessURL></interface></capability></capabilities>").getBytes());
	Capabilities capabilities = new TapService("http://localhost:7060/tap").getCapabilities();

	List<Capability> capabilityList = capabilities.getCapabilities();
	assertFalse(capabilities.getCapabilities().isEmpty());
	
	Capability capability = capabilityList.get(0);
	assertEquals("ivo://www.ivoa.net/std/TAP", capability.getStandardId());

	List<Validation> validations = capability.getValidations();
	assertFalse(validations.isEmpty());

	Validation validation = validations.get(0);
	assertEquals(java.math.BigInteger.ZERO, validation.getValue());
	assertEquals("ivo://some.one/special", validation.getValidatedBy());

	List<Interface> interfaces = capability.getInterfaces();
	assertFalse(interfaces.isEmpty());

	Interface iface = interfaces.get(0);
	assertEquals("std", iface.getRole());
	assertEquals("1.0", iface.getVersion());
	
	List<AccessURL> accessURLs = iface.getAccessURLs();
	assertFalse(accessURLs.isEmpty());

	AccessURL accessURL = accessURLs.get(0);
	assertEquals("base", accessURL.getUse());
	assertEquals("http://localhost:7060/tap", accessURL.getValue());
    } 

    @Test(expected=HttpException.class) public void notFoundTest() throws HttpException, ResponseFormatException, IOException {
	Capabilities capabilities = new TapService("http://localhost:7060/").getCapabilities();
    } 
}