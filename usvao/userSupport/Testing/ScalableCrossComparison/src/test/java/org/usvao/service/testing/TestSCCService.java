package org.usvao.service.testing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author thomas
 *
 */
public class TestSCCService 
extends TestCase 
{

	private static final Logger log = Logger.getLogger(TestSCCService.class);

	private HttpServiceAdapter service; 
	private File irasData;
//	private File tmassData;

	@Override
	protected void setUp() 
	throws Exception 
	{
		ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"scc-service-testing-config.xml"});
		
		service = (HttpServiceAdapter) appContext.getBean("serviceConfig");

		assertNotNull("service config is initialized ", service);
		assertNotNull("service config has a basqe url ", service.getBaseUrl() );
		assertNotNull("service config has responseSchema ", service.getResponseSchemaURL() );

		irasData = new File (getClass().getClassLoader().getResource("iras.tbl").getFile());
//		tmassData = new File (getClass().getClassLoader().getResource("tmass.tbl").getFile());
		
	}

	public void testCase_unknown() throws Exception {
		String tableA_url = doTest("Test Case 0.0.0.","nph-fileupload", null, irasData, PostFileType.TEXT); 
		Map<String,String> params = new HashMap<String, String>();
		params.put("maxdist", "10");
		params.put("tableA", tableA_url);
		params.put("tableB", "IRAS_PSC");
		params.put("custom_cntr1", "cntr");
		params.put("custom_ra1", "ra");
		params.put("custom_dec1", "dec");
		
		String response = doTest("Test Case 2. Post text params","nph-catalogCompare", params, null, PostFileType.TEXT);
		Map<String, String> resultParams = parseMatchResponse (response);
		checkMatchResponse(resultParams, "Success", 347, 0, 0);
		
		// TODO: We could do more tests on resulting data tables, pulling them 
		// from the service and parsing/checking here, if desired
		
	}
	
	private String doTest(String msg, String urlPath, Map<String,String> data, File file, PostFileType type)
	throws Exception
	{
		StringBuilder logmsg = new StringBuilder(msg).append(" URL Path:").append(urlPath);
		log.info(logmsg);
		
		String response = service.makePostCall(urlPath, data, file, type);
		
//		String response = doBasicTest(query, serviceConfig.getResponseSchemaURL());
		log.debug("returns response:\n"+response);
		
		return response;
	}

	private Map<String,String> parseMatchResponse (String response)
	throws Exception
	{
		Map<String,String> resultParams = new HashMap<String,String>();
		
		Document document = createDocument(response);
		Element root = document.getDocumentElement();
		
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength() ; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				Element param = (Element) child;
				resultParams.put(param.getNodeName(), param.getTextContent());
			}
			
		}
		
		assertEquals("root node is named 'result'", root.getNodeName(), "result");
		
		return resultParams;
	}
			
	private void checkMatchResponse (Map<String,String> responseParams, 
			String statusExpected,  int matchedrowsExpected, 
			int unmatchedrowsExpected, int badrowsExpected)
	throws Exception
	{

		assertNotNull("has message element",responseParams.containsKey("message")); 
		assertEquals("response is expected status", statusExpected, responseParams.get("message"));
		
		assertNotNull("has baseurl element",responseParams.containsKey("baseurl")); 
		assertTrue("baseurl is non-null", !responseParams.get("baseurl").isEmpty());
		
		assertNotNull("has match element",responseParams.containsKey("match")); 
		assertTrue("match is non-null", !responseParams.get("match").isEmpty());
		
		assertNotNull("has unmatch element",responseParams.containsKey("unmatch")); 
		assertTrue("unmatch is non-null", !responseParams.get("unmatch").isEmpty());
		
		assertNotNull("has bad element",responseParams.containsKey("bad")); 
		assertTrue("bad is non-null", !responseParams.get("bad").isEmpty());
		
		assertNotNull("has matchedrows element",responseParams.containsKey("matchedrows")); 
		assertEquals("matched rows is as expected", Integer.parseInt(responseParams.get("matchedrows")), matchedrowsExpected);
		
		assertNotNull("has unmatchedrows element",responseParams.containsKey("unmatchedrows")); 
		assertEquals("unmatched rows is as expected", Integer.parseInt(responseParams.get("unmatchedrows")), unmatchedrowsExpected);
		
		assertNotNull("has badrows element",responseParams.containsKey("badrows")); 
		assertEquals("bad rows is as expected", Integer.parseInt(responseParams.get("badrows")), badrowsExpected);
		
	}

	private Document createDocument (String doc)
	throws Exception
	{
		DocumentBuilder parser =
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(inputStreamFromString(doc));
		return document;
	}

	/*
	private static Element findFirstChildElement (String tagName , Element parent) {
		NodeList children = parent.getElementsByTagName(tagName);
		if (children.getLength() > 0)
			return (Element) children.item(0); 
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
	*/
	
	private InputStream inputStreamFromString(String str)
	throws Exception
	{
		byte[] bytes = str.getBytes(service.getCharset());
		return new ByteArrayInputStream(bytes);
	}

	/*
	private static String readInputStreamAsString(InputStream in) 
	    throws IOException {

	    BufferedInputStream bis = new BufferedInputStream(in);
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    int result = bis.read();
	    while(result != -1) {
	      byte b = (byte)result;
	      buf.write(b);
	      result = bis.read();
	    }        
	    return buf.toString();
	}
	*/

}
