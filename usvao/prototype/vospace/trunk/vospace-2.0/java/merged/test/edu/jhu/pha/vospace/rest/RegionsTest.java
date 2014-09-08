package edu.jhu.pha.vospace.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.oauth;
import static com.jayway.restassured.path.xml.XmlPath.from;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public class RegionsTest {

	static final String filesDir = "/Users/dmitry/Documents/workspace/vospace-2.0/xmlFiles/";
	static final String transfersUrl = "http://dimm.pha.jhu.edu:8081/vospace-2.0/rest/transfers";
	
	@Before
    public void setUp() {
		RestAssured.baseURI = "http://dimm.pha.jhu.edu";
		RestAssured.port = 8081;
		RestAssured.basePath = "/vospace-2.0";
		RestAssured.authentication = oauth("sclient", "ssecret", "48dc36f3283e37872d22e911e9fd0632", "f79a589bccc708e863c32177d71f87ac");
    }
	
	@Test
	public void testContRegions() {
		expect().
			statusCode(200).
			log().all().
		given().
			get("/rest/1/regions/sync1");
	}


	@Test
	public void deleteTestContInit() {
		expect().
			statusCode(200).
		given().
			delete("/rest/nodes/test_cont1");
	}
	
	@Test
	//@Ignore
	public void testPutNewContNode() {
		expect().
			statusCode(201).
		given().
			content(getFileContents("newContainerNode.xml")).
			put("/rest/nodes/test_cont1");
	}

	@Test
//	@Ignore
	public void testPutNewDataNode() {
		expect().
			statusCode(201).
		given().
			content(getFileContents("newDataNode1.xml")).
			put("/rest/nodes/test_cont1/data1.bin");
	}

	@Test
//	@Ignore
	public void testPutNodeRegions() {
		expect().
			statusCode(200).
			log().ifError().
		given().
			content(getFileContents("nodeRegions.csv")).
			put("/rest/1/regions_put/test_cont1");
	}
	
	private String getFileContents(String fileName) {
		StringBuffer buf = new StringBuffer();
		
		File readFile = new File(filesDir+fileName);
		try {
			FileReader reader = new FileReader(readFile);
			char[] cbuf = new char[1024];
			int read = reader.read(cbuf);
			while(read >= 0){
				buf.append(cbuf,0,read);
				read = reader.read(cbuf);
			}
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return buf.toString();
	}
}
