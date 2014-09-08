package edu.harvard.cfa.vo.tapclient.vosi;

import org.junit.Test;
import static org.junit.Assert.*;

public class AccessURLTest {
    @Test public void defaultConstructorTest() {
	AccessURL accessURL = new AccessURL();
	assertNull(accessURL.getUse());
	assertNull(accessURL.getValue());
    }

    @Test public void conversionConstructorTest() throws Exception {
	net.ivoa.xml.voResource.v10.AccessURL xaccessURL = net.ivoa.xml.voResource.v10.AccessURL.Factory.parse("<xml-fragment use=\"full\">http://localhost:8080/tap/capabilities</xml-fragment>");

	AccessURL accessURL = new AccessURL(xaccessURL);
	assertEquals("full", accessURL.getUse());
	assertEquals("http://localhost:8080/tap/capabilities", accessURL.getValue());
    }

    @Test(expected=IllegalArgumentException.class) public void badUseEnumTest() throws Exception {
	net.ivoa.xml.voResource.v10.AccessURL xaccessURL = net.ivoa.xml.voResource.v10.AccessURL.Factory.parse("<xml-fragment use=\"FULL\">http://localhost:8080/tap/capabilities</xml-fragment>");

	AccessURL accessURL = new AccessURL(xaccessURL);
    }
}