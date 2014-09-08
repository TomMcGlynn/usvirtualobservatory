package org.usvao.service.testing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author thomas
 *
 */
public class TestPostService 
extends TestCase 
{

	private static final Logger log = Logger.getLogger(TestPostService.class);

	private HttpServiceAdapter service;
	private File irasData;

	@Override
	protected void setUp() 
	throws Exception 
	{
		ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"post-service-testing-config.xml"});
		
		service = (HttpServiceAdapter) appContext.getBean("serviceConfig");

		assertNotNull("service config is initialized ", service);
		assertNotNull("service config has a base url ", service.getBaseUrl() );
		assertNotNull("service config has charset", service.getCharset() );

		irasData = new File (getClass().getClassLoader().getResource("iras.tbl").getFile());
		
	}

	public void testCase1and2() throws Exception {
		String tableA_url = doTest("Test Case1. Post text file","nph-fileupload", null, irasData, PostFileType.TEXT); 
		
		Map<String,String> params = new HashMap<String, String>();
		params.put("maxdist", "10");
		params.put("tableA", tableA_url);
		params.put("tableB", "IRAS_PSC");
		params.put("custom_cntr1", "cntr");
		params.put("custom_ra1", "ra");
		params.put("custom_dec1", "dec");
		
		doTest("Test Case 2. Post text params","nph-catalogCompare", params, null, PostFileType.TEXT);
		
	}
	
	private String doTest(String msg, String urlPath, Map<String,String> data, File file, PostFileType type)
	throws Exception
	{
		StringBuilder logmsg = new StringBuilder(msg).append(" URL Path:").append(urlPath);
		log.info(logmsg);
		
		String response = service.makePostCall(urlPath, data, file, type);
		
		log.debug("returns response:\n"+response);
		
		assertTrue("Response is as expected", response != null);
		
		return response;
	}

}
