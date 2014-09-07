package org.usvao.sso.openid.portal;

import java.util.Date;
import java.util.Collection;
import java.util.TreeSet;
import static java.lang.String.format;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class USVAOConventionsTest {

    String[] usernames = { "http://openid.net/namePerson/friendly", 
                           "http://sso.usvao.org/schema/username", 
                           "http://schema.openid.net/namePerson/friendly", 
                           "http://axschema.org/namePerson/friendly" };
    String[] names =     { "http://sso.usvao.org/schema/name", 
                           "http://axschema.org/namePerson", 
                           "http://schema.openid.net/namePerson", 
                           "http://openid.net/namePerson" };
    String[] firstnames = { "http://sso.usvao.org/schema/namePerson/first" };
    String[] lastnames = { "http://sso.usvao.org/schema/namePerson/last" };
    String[] emails = { "http://sso.usvao.org/schema/email", 
                        "http://axschema.org/contact/email", 
                        "http://schema.openid.net/contact/email", 
                        "http://openid.net/schema/contact/email" };
    String[] phones = { "http://sso.usvao.org/schema/phone",
                        "http://axschema.org/contact/phone/default", 
                        "http://axschema.org/contact/phone/business" };
    String[] instits = { "http://sso.usvao.org/schema/institution" };
    String[] country = { "http://sso.usvao.org/schema/country" };
    String[] certs = { "http://sso.usvao.org/schema/credential", 
                       "http://sso.usvao.org/schema/credential/x509" };

    @Test
    public void testIdentifyAttrURI() {
        for (String uri: usernames) {
            assertEquals(USVAOConventions.SupportedAttribute.USERNAME,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: names) {
            assertEquals(USVAOConventions.SupportedAttribute.NAME,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: firstnames) {
            assertEquals(USVAOConventions.SupportedAttribute.FIRSTNAME,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: lastnames) {
            assertEquals(USVAOConventions.SupportedAttribute.LASTNAME,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: emails) {
            assertEquals(USVAOConventions.SupportedAttribute.EMAIL,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: phones) {
            assertEquals(USVAOConventions.SupportedAttribute.PHONE,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: instits) {
            assertEquals(USVAOConventions.SupportedAttribute.INSTITUTION,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: country) {
            assertEquals(USVAOConventions.SupportedAttribute.COUNTRY,
                         USVAOConventions.identifyAttributeURI(uri));
        }
        for (String uri: certs) {
            assertEquals(USVAOConventions.SupportedAttribute.CREDENTIAL,
                         USVAOConventions.identifyAttributeURI(uri));
        }
    }

}

