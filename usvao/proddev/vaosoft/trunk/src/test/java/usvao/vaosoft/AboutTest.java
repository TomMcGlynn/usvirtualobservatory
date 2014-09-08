package usvao.vaosoft;

import java.util.Properties;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the About class
 */
public class AboutTest {
    private About about = null;
    private static final File ivyfile = new File(System.getProperty("tmp.dir") +
                                                 File.separator + "ivy.xml");
    private static DocumentBuilderFactory dbfactory = 
        DocumentBuilderFactory.newInstance();
    private DocumentBuilder dombuilder = null;
    private XPath xpath = XPathFactory.newInstance().newXPath();

    public AboutTest() throws ParserConfigurationException {
        dbfactory.setNamespaceAware(true);
        dombuilder = dbfactory.newDocumentBuilder();
    }

    @Before public void setUp() throws IOException {
        InputStream astrm = 
            getClass().getResourceAsStream("test-about.properties");
        about = new About(astrm);
        astrm.close();
        cleanOutFiles();
    }
    @After public void tearDown() {
        about = null;
        cleanOutFiles();
    }

    private void cleanOutFiles() {
        if (ivyfile.exists()) ivyfile.delete();
    }

    @Test public void testGetName() {
        assertEquals("vaosoft", about.getName());
    }

    @Test public void testGetVersion() {
        assertEquals("Unset", about.getVersion());
    }

    @Test public void testSchemaVersion() {
        assertEquals("1.0", about.getSchemaVersion());
    }

    @Test public void testGetProperty() {
        assertEquals("usvao", about.getProperty("org"));     // default
        assertEquals("vaosoft", about.getProperty("name"));  // single field
        assertEquals("ant", about.getProperty("build.type"));  // 2 fields
        // multi-fields, value with spaces
        assertEquals("apache      ivy         2.2.+", 
                     about.getProperty("dep.product.ivy")); 
        assertEquals("jar       jar", 
                     about.getProperty("art.for.ownjars.1")); // has number
    }

    @Test public void testCtor() {
        assertNotNull(about);
        testSchemaVersion();
        testGetName();

        Properties p = new Properties();
        about = new About(p);
        testSchemaVersion();
        assertEquals( "Unknown", about.getName());
        assertEquals( "Unset", about.getVersion());

        about = new About(p, "4.0");
        testSchemaVersion();
        assertEquals( "Unknown", about.getName());
        assertEquals( "4.0", about.getVersion());

        about = new About(p, "3.0", "goober");
        testSchemaVersion();
        assertEquals( "goober", about.getName());
        assertEquals( "3.0", about.getVersion());

        try {
            InputStream astrm = 
                getClass().getResourceAsStream("test-about.properties");
            about = new About(astrm, "2.0");
            testSchemaVersion();
            assertEquals( "vaosoft", about.getName());
            assertEquals("2.0", about.getVersion());
            astrm.close();

            astrm = getClass().getResourceAsStream("test-about.properties");
            about = new About(astrm, "5.0", "gurn");
            testSchemaVersion();
            assertEquals( "gurn", about.getName());
            assertEquals( "5.0", about.getVersion());
            astrm.close();

        } catch (IOException ex) {
            throw new InternalError("trouble closing streams");
        }

    }

    @Test public void testToXML() {
        Document doc = about.toXML();
        assertNotNull(doc);
        Element root = doc.getDocumentElement();
        assertEquals("about", root.getNodeName());
 
        NodeList nodes = root.getChildNodes();
        assertEquals(10, nodes.getLength());
        // order is alphabetical
        assertEquals("about",       nodes.item(0).getNodeName());
        assertEquals("art",         nodes.item(1).getNodeName());
        assertEquals("build",       nodes.item(2).getNodeName());
        assertEquals("conf",        nodes.item(3).getNodeName());
        assertEquals("dep",         nodes.item(4).getNodeName());
        assertEquals("description", nodes.item(5).getNodeName());
        assertEquals("name",        nodes.item(6).getNodeName());
        assertEquals("org",         nodes.item(7).getNodeName());
        assertEquals("status",      nodes.item(8).getNodeName());
        assertEquals("version",     nodes.item(9).getNodeName());
    }

    @Test public void testWriteIvyFile() 
        throws SAXException, IOException, TransformerException,
               XPathExpressionException
    {
        // FIX: Unix-y test to run on Win
        FileOutputStream os = new FileOutputStream(ivyfile);
        assertNotNull(os);
        about.writeIvyFile(os);
        os.close();

        Document ivy = dombuilder.parse(ivyfile);
        Element root = ivy.getDocumentElement();
        assertEquals("ivy-module", root.getNodeName());


        NodeList nl = (NodeList) xpath.evaluate("info", 
                                                root, XPathConstants.NODESET);
        assertEquals(1, nl.getLength());
        Element el = (Element) nl.item(0);
        assertEquals("usvao", el.getAttribute("organisation"));
        assertEquals("vaosoft", el.getAttribute("module"));
        assertEquals("Unset", el.getAttribute("revision"));
    }
}