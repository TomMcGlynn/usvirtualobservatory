/**
 * 
 */
package org.usvao.service.testing;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author thomas
 *
 */
public class TestGetService 
extends TestCase 
{
	
	private static final Logger log = Logger.getLogger(TestGetService.class);
	
	private HttpServiceAdapter service;
	
	@Override
	protected void setUp() 
	throws Exception 
	{
		ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"get-service-testing-config.xml"});
		
		service = (HttpServiceAdapter) appContext.getBean("serviceConfig");

		assertNotNull("service config is initialized ", service);
		assertNotNull("service config has a base url ", service.getBaseUrl() );
		assertNotNull("service config has responseSchema ", service.getResponseSchemaURL() );

	}

	public void testCase1() throws Exception {
		doTest("Test Case 1. Test basic GET call","querySED?REQUEST=queryData&TARGETNAME=mmm","OK");  
	}
	
	private String doTest(String msg, String urlPath, String expectedStatus) 
	throws Exception
	{
		StringBuilder logmsg = new StringBuilder(msg).append(" URL Path:").append(urlPath);
		log.info(logmsg);
		
		String response = service.makeGetCall(urlPath);
		
		log.debug("returns response:\n"+response);
		assertTrue("Response is as expected", response != null);
		
		return response;
		
	}
	
}
