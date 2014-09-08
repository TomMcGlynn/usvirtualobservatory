/**
 * 
 */
package net.vao;

import junit.framework.TestCase;

/**
 * @author thomas
 *
 */
public class TestHelloWorld 
extends TestCase 
{
	
	public void test1() 
	throws Exception 
	{
		HelloWorld test = new HelloWorldImpl();
		assertEquals ("returned message is as expected", test.getMessage(), "Hello World");
	}

}
