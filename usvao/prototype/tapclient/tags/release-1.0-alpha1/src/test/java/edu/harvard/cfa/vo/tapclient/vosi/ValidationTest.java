package edu.harvard.cfa.vo.tapclient.vosi;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.harvard.cfa.vo.tapclient.vosi.Validation;

public class ValidationTest {
    @Test public void defaultConstructorTest() {
	Validation validation = new Validation();
	assertNull(validation.getValidatedBy());
	assertNull(validation.getValue());
    }

    @Test public void conversionConstructorTest() throws Exception {
	net.ivoa.xml.voResource.v10.Validation xvalidation = net.ivoa.xml.voResource.v10.Validation.Factory.parse("<xml-fragment validatedBy=\"ivo://no.one\">4</xml-fragment>");

	Validation validation = new Validation(xvalidation);
	assertEquals("ivo://no.one", validation.getValidatedBy());
	assertEquals(java.math.BigInteger.valueOf(4), validation.getValue());
    }

    @Test public void badValidatedByTest() throws Exception {
	net.ivoa.xml.voResource.v10.Validation xvalidation = net.ivoa.xml.voResource.v10.Validation.Factory.parse("<xml-fragment validatedBy=\"http://www.google.com\">1345124313</xml-fragment>");

	Validation validation = new Validation(xvalidation);
	assertEquals("http://www.google.com", validation.getValidatedBy());
	assertEquals(java.math.BigInteger.valueOf(1345124313), validation.getValue());
    }

    @Test(expected=IllegalArgumentException.class) public void badValueTest() throws Exception {
	net.ivoa.xml.voResource.v10.Validation xvalidation = net.ivoa.xml.voResource.v10.Validation.Factory.parse("<xml-fragment validatedBy=\"ivo://some.one\">Fore</xml-fragment>");

	Validation validation = new Validation(xvalidation);
    }
}