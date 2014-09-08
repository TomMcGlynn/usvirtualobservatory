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
import org.junit.Test;
import static org.junit.Assert.*;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

public class UdtPullTestRemote {

	static final String filesDir = "/Users/dmitry/Documents/workspace/vospace-2.0/xmlFiles/";
	static final String transfersUrl = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0.1/rest/transfers";
	
	@Before
    public void setUp() {
		RestAssured.baseURI = "http://tempsdss.pha.jhu.edu";
		RestAssured.port = 8080;
		RestAssured.basePath = "/vospace-2.0.1";
		RestAssured.authentication = oauth("sclient", "ssecret", "3512e80fde4b329d6b47f1d3a5b274cb", "7143c9f4d4356b25f612c19fb462f983");
    }
	
	@Test
	public void testProtocols() {
		expect().
			body("protocols.accepts.protocol.@uri",	hasItems(
					"ivo://ivoa.net/vospace/core#httpget",
					"ivo://ivoa.net/vospace/core#httpput",
					"ivo://ivoa.net/vospace/core#udtget"
					)).
			body("protocols.provides.protocol.@uri",	hasItems(
					"ivo://ivoa.net/vospace/core#httpget",
					"ivo://ivoa.net/vospace/core#httpput",
					"ivo://ivoa.net/vospace/core#udtget"
					)).
		when().
			get("/rest/protocols");
	}

	@Test
	public void testPutNewContNode() {
		expect().
			statusCode(201).
		given().
			content(getFileContents("newContainerNode.xml")).
			put("/rest/nodes/test_cont1");
	}

	@Test
	public void testPutNewDataNode() {
		expect().
			statusCode(201).
		given().
			content(getFileContents("newDataNode1.xml")).
			put("/rest/nodes/test_cont1/data1.bin");
	}

	@Test
	public void testPullToVoSpace() throws ClientProtocolException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, IOException, InterruptedException {
		String jobXml = expect().
				statusCode(200).
			given().
				content(getFileContents("pullToVoSpace.xml")).
				post("/rest/transfers").body().asString();
		String jobDetailsUrl = from(jobXml).get("job.results.result.findAll{it.@id == 'transferDetails'}.@href");
		String jobId = from(jobXml).get("job.jobId");

		assertThat("ERROR",not(equalTo(from(jobXml).get("job.phase"))));
		
		int tries = 50;
		String status = "";
		while(tries > 0 && !status.equals("COMPLETED")){
			Response resp2 = expect().
				statusCode(200).
				body(not(equalTo("ERROR"))).
			given().
				get(transfersUrl+"/"+jobId+"/phase");
			status = resp2.body().asString();
			tries--;
			synchronized(this) {
				this.wait(1000);
			}
		}
		assertThat(tries, greaterThan(0));
	}

	@Test
	public void testUdtFrom() throws ClientProtocolException, OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, IOException, InterruptedException {
		String jobXml = expect().log().ifError().
				statusCode(200).
			given().
				content(getFileContents("pullFromVoSpaceUdtRemote.xml")).
				post("/rest/transfers").body().asString();
		String jobDetailsUrl = from(jobXml).get("job.results.result.findAll{it.@id == 'transferDetails'}.@href");
		String jobId = from(jobXml).get("job.jobId");

		assertThat("ERROR",not(equalTo(from(jobXml).get("job.phase"))));
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
