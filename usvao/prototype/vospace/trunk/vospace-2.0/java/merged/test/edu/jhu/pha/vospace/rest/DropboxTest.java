package edu.jhu.pha.vospace.rest;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.oauth;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import com.jayway.restassured.RestAssured;

public class DropboxTest {

	@Before
    public void setUp() {
		//RestAssured.baseURI = "https://api.dropbox.com";
		//RestAssured.port = 80;
		//RestAssured.basePath = "/1/metadata/sandbox/";
		//RestAssured.authentication = oauth("eaq4yg7bhdh4lhd", "o3v28cynt31ssar", "3phh0n6mugzkx60", "5mkmb3o7j8eihy3");

		RestAssured.baseURI = "http://dimm.pha.jhu.edu";
		RestAssured.port = 8081;
		RestAssured.basePath = "/vospace-2.0/1/";
		RestAssured.authentication = oauth("vosync", "vosync_ssecret", "ffa47e6b0d3ee12644676354062394fa", "40a5eea36eb2464926d37e052afc8354");

	}

	@Test
	public void testGetMetaRoot() {
		expect().
			statusCode(200).
		given().
			get("metadata/sandbox");
	}


	@Test
	public void testSearch() {
		expect().
			statusCode(200).
			response().log().all().
		given().
			get("search/sandbox/footprint?query=756");
	}



}
