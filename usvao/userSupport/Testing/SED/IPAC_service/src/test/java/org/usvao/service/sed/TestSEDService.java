package org.usvao.service.sed;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.usvao.service.testing.HttpServiceAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author thomas
 *
 */
public class TestSEDService 
extends TestCase 
{

	private static final Logger log = Logger.getLogger(TestSEDService.class);

	private HttpServiceAdapter service;

	@Override
	protected void setUp() 
			throws Exception 
			{
		ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"sed-service-testing-config.xml"});
		service = (HttpServiceAdapter) appContext.getBean("serviceConfig");

		assertNotNull("service config is initialized ", service);
			}

	// Test Case 1.0.1.x Conform to standards..tests of bad querySED urls
	public void testCase1_0_1_1() throws Exception { doTest("testcase1.0.1.1. missing REQUEST","querySED","ERROR");  }
	public void testCase1_0_1_2() throws Exception { doTest("testcase1.0.1.2. missing REQUEST, POS value","querySED?POS=","ERROR");  }
	public void testCase1_0_1_3() throws Exception { doTest("testcase1.0.1.3. missing REQUEST, TARGETNAME value","querySED?TARGETNAME=","ERROR");  }
	public void testCase1_0_1_4() throws Exception { doTest("testcase1.0.1.4. missing POS value","querySED?REQUEST=queryData&POS=","ERROR");  }
	public void testCase1_0_1_5() throws Exception { doTest("testcase1.0.1.5. missing TARGETNAME value","querySED?REQUEST=queryData&TARGETNAME=","ERROR");  }
	public void testCase1_0_1_6() throws Exception { doTest("testcase1.0.1.6. bad format info avail","querySED?REQUEST=queryData&TARGETNAME=3c273&FORMAT=doodle","ERROR");  }
	public void testCase1_0_1_7() throws Exception { doTest("testcase1.0.1.7. bad format info discovery","querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.1&FORMAT=thedude","ERROR");  }
	public void testCase1_0_1_8() throws Exception { doTest("testcase1.0.1.8. missing format info dis","querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.1&FORMAT=","ERROR");  }
	public void testCase1_0_1_9() throws Exception { doTest("testcase1.0.1.9. missing format info avail","querySED?REQUEST=queryData&TARGETNAME=3c273&FORMAT=","ERROR");  }
	public void testCase1_0_1_10() throws Exception { doTest("testcase1.0.1.10. bad pos coords","querySED?REQUEST=queryData&POS=187.2779154,uuu&SIZE=.1","ERROR");  }

	// Test Case 1.0.2.x Conform to standards..tests of bad accessSED urls
	public void testCase1_0_2_1() throws Exception { doTest("Test Case 1.0.2.1. missing REQUEST ","accessSED","ERROR"); }
	public void testCase1_0_2_2() throws Exception { doTest("Test Case 1.0.2.2. missing REQUEST ","accessSED?TARGETNAME=3c273","ERROR"); }
	public void testCase1_0_2_3() throws Exception { doTest("Test Case 1.0.2.3. missing REQUEST,TARGETNAME value ","accessSED?REQUEST=getData&TARGETNAME=","ERROR"); }
	public void testCase1_0_2_4() throws Exception { doTest("Test Case 1.0.2.4. missing FORMAT value","accessSED?REQUEST=getData&TARGETNAME=3c274&FORMAT=","ERROR"); }
	public void testCase1_0_2_5() throws Exception { doTest("Test Case 1.0.2.5. bad FORMAT value","accessSED?REQUEST=getData&TARGETNAME=3c274&FORMAT=thedude","ERROR"); }

	/*
	These are disabled pending VAOPD-207

	public void testCase1_0_3() throws Exception
	{
		log.info("Test Case 1.0.3. Conform to standards..tests of format=metadata urls on querySED");

		String response = doBasicTest("querySED?FORMAT=METADATA", service.getResponseSchemaURL());
		checkIsValidSSAPResponse (response, "OK");

		// TODO: parse the response and look for elements we expect
	}

	public void testCase1_0_4() throws Exception
	{
		log.info("Test Case 1.0.4. Conform to standards..tests of format=metadata urls on accessSED");
		String response = doBasicTest("accessSED?FORMAT=METADATA", service.getResponseSchemaURL()); 
		checkIsValidSSAPResponse (response, "OK");

		// TODO: parse the response and look for elements we expect

	}
	 */

	// Info Availability tests
	public void testCase1_1_1() throws Exception {
		doTest("Test Case 1.1.1. Info availability, no object ","querySED?REQUEST=queryData&TARGETNAME=mmm","OK", 0);  
	}

	public void testCase1_1_2() throws Exception {
		doTest("Test Case 1.1.2. Info availability, no object camelcase","querySED?REQUEST=queryData&tArgEtNaMe=mmm","OK", 0);  
	}

	public void testCase1_1_3() throws Exception {
		doTest("Test Case 1.1.3. Info availability, no object, explicit FORMAT=votable","querySED?REQUEST=queryData&TARGETNAME=mmm&FORMAT=votable","OK", 0);  
	}

	public void testCase1_1_4() throws Exception {
		doTest("Test Case 1.1.4. Info availability, object exists ","querySED?REQUEST=queryData&TARGETNAME=3c273","OK", 1);  
	}

	public void testCase1_1_5() throws Exception {
		doTest("Test Case 1.1.5. Info availability, object exists camelcase ","querySED?REQUEST=queryData&TaRgETNAME=3c273","OK", 1);  
	}

	public void testCase1_1_6() throws Exception {
		doTest("Test Case 1.1.6. Info availability, object exists, explicit FORMAT=votable","querySED?REQUEST=queryData&TARGETNAME=3c273&FORMAT=votable","OK", 1);  
	}

	// 1.2.x Info discovery test cases
	public void testCase1_2_1() throws Exception { 
		doTest("Test Case 1.2.1. Info Discovery, object search finds something","querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.01","OK",9);
	}

	public void testCase1_2_2() throws Exception { 
		doTest("Test Case 1.2.2. Info Discovery, object search finds something, camelcase","querySED?REQUEST=queryData&PoS=187.2779154,2.0523883&SiZE=.01","OK",1);  
	}

	public void testCase1_2_3() throws Exception { 
		doTest("Test Case 1.2.3. Info Discovery, object search finds something, FORMAT=votable","querySED?REQUEST=queryData&POS=187.2779154,2.0523883&SIZE=.01&FORMAT=votable","OK",1);  
	}

	// Data Retrieval (getData) requests
	public void testCase1_3_1() throws Exception {
		doTest("Test Case 1.3.1. Access Data, object no exist","accessSED?REQUEST=getData&TARGETNAME=mmm","OK", 0); 
	}

	public void testCase1_3_2() throws Exception {
		doTest("Test Case 1.3.2. Access Data, object exists","accessSED?REQUEST=getData&TARGETNAME=3c273","OK", 1); 
	}

	public void testCase1_3_3() throws Exception {
		doTest("Test Case 1.3.2. Access Data, object exists camelcase","accessSED?REQUEST=getData&TaRgEtNaME=3c273","OK", 1); 
	}

	public void testCase2() 
	throws Exception
	{
		log.info("Test Case 2 : Service Performance");
		// time a battery of queries..
	}

	private String doTest(String msg, String query, String status)
			throws Exception
			{
		StringBuilder logmsg = new StringBuilder(msg).append(" URL:").append(query);
		log.info(logmsg);
		String response = doBasicTest(query, service.getResponseSchemaURL());
		checkIsValidSSAPResponse (response, status);
		return response;
			}

	private void doTest(String msg, String query, String status, int expectedNrofResultRows)
			throws Exception
			{
		String response = doTest(msg, query,status);
		if (expectedNrofResultRows == 0)
		{
			assertEquals("response has NO result rows", countVOTableResultRows(response), 0); 
		} else {
			assertTrue("response has some result rows", countVOTableResultRows(response) >= 1); 
		}
			}

	private String doBasicTest (String urlPath, URL schema) 
			throws Exception
			{

		StringBuilder url = new StringBuilder(service.getBaseUrl()).append(urlPath);
		String response = makeRestCall(url.toString());

		assertNotNull("response is not null", response); log.debug("Got response:"+response);
		assertTrue("response is valid VOTable", isValidVOTable(inputStreamFromString(response), schema));
		return response;
			}


	private static InputStream inputStreamFromString(String str)
			throws Exception
			{
		byte[] bytes = str.getBytes("UTF-8");
		return new ByteArrayInputStream(bytes);
			}

	private static int countVOTableResultRows(String response)
			throws Exception
			{
		int nrofRows = 0;

		Document document = createDocument(response);
		Element root = document.getDocumentElement();

		Element resource = findFirstChildElement ("RESOURCE", root);

		Element table = findFirstChildElement ("TABLE", resource);
		if (table != null)
		{
			Element data = findFirstChildElement ("DATA", table);
			if (data != null) {
				Element tableData = findFirstChildElement ("TABLEDATA", data);
				if (tableData != null) {
					NodeList trNodes = tableData.getElementsByTagName("TR");
					nrofRows = trNodes.getLength();
				}
			}
			//			}
		}
		return nrofRows;
	}

	private static void checkIsValidSSAPResponse (String response, String statusExpected)
			throws Exception
			{

		Document document = createDocument(response);
		Element root = document.getDocumentElement();

		Element resource = findFirstChildElement ("RESOURCE", root);
		assertNotNull("Has resource node", resource);
		assertEquals("Resource has utype attribute", resource.getAttribute("utype"),"spec:Sed"); 

		Element info = findInfoQueryStatusElement(resource); 
		assertNotNull("has INFO element",info); 
		assertEquals("Info element has name=QUERY_STATUS attribute", info.getAttribute("name"),"QUERY_STATUS"); 
		assertEquals("Info element has value attribute of expected status", 
				statusExpected, info.getAttribute("value")); 


			}

	private static Document createDocument (String doc)
			throws Exception
			{
		DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(inputStreamFromString(doc));
		return document;
			}

	private static Element findFirstChildElement (String tagName , Element parent) {
		NodeList children = parent.getElementsByTagName(tagName);
		if (children.getLength() > 0)
			return (Element) children.item(0); 
		return null;
	}

	private static Element findInfoQueryStatusElement (Element parent) {
		NodeList info_children = parent.getElementsByTagName("INFO");
		if (info_children.getLength() > 0)
		{
			for (int index = 0; index <= info_children.getLength() ; index++)
			{
				Element child = (Element) info_children.item(index);
				if (child.getAttribute("name") != null 
						&& child.getAttribute("name").equals("QUERY_STATUS"))
				{
					return child;
				}
			}
		}
		return null;
	}

	private static boolean isValidVOTable(InputStream xml, URL schemaUrl) 
	{

		try {

			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			log.debug("URL:"+schemaUrl);

			Schema schema = factory.newSchema(new File(schemaUrl.getFile()));
			Validator validator = schema.newValidator();

			Source source = new StreamSource(xml);
			validator.validate(source);


		} catch (Exception e) {
			log.error("Exception:"+e.getClass()+" msg:"+e.getMessage());
			return false;
		}      

		return true;
	}

	private static String makeRestCall (String restEndPoint) 
			throws Exception
			{

		log.debug("rest call to "+restEndPoint);

		URL url = new URL(restEndPoint);

		//make connection, use post mode, and send query
		URLConnection urlc = url.openConnection();
		urlc.setDoOutput(true);
		urlc.setAllowUserInteraction(false);

		//retrieve result
		BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		String str;
		StringBuffer sb = new StringBuffer();
		while ((str = br.readLine()) != null) {
			sb.append(str);
			sb.append("\n");
		}
		br.close();

		return sb.toString();

			}

}
