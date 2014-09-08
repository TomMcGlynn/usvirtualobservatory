package usvao.vaosoft.ant;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class About2IvyTest extends BuildFileTest {

    final static String sl = File.separator;
    String buildfileroot = ".";
    String buildFile = getClass().getName().replace(".", sl) + 
        "-build.xml";
    DocumentBuilder dombuilder = null;

    public About2IvyTest(String s) throws ParserConfigurationException {
        super(s);
        String dir = System.getProperty("tclasses.dir");
        if (dir != null) buildfileroot = dir;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        dombuilder = factory.newDocumentBuilder();
    }

    public void setUp() {
        configureProject(buildfileroot+sl+buildFile);
        getProject().setNewProperty("test.classpath", 
                                    System.getProperty("test.classpath"));
        getProject().setNewProperty("tclasses.dir", 
                                    System.getProperty("tclasses.dir"));
        getProject().setNewProperty("tmp.dir", 
                                    System.getProperty("tmp.dir"));
        getProject().setNewProperty("ivyfile", 
                                    System.getProperty("tmp.dir")+sl+"ivy.xml");
                                    
        getProject().setNewProperty("aboutfile", 
                                    System.getProperty("tjsrc.dir") + 
                                    sl+"usvao"+sl+"vaosoft"+
                                    sl+"test-about.properties");
        tearDown();
    }

    public void tearDown() {
        File f = new File(getProject().getProperty("ivyfile"));
        if (f.exists()) f.delete();
    }

    public void testBasic() throws SAXException,IOException {
        File f = new File(getProject().getProperty("ivyfile"));
        assertFalse(f.exists());
        executeTarget("basic");
        assertTrue(f.exists());

        Document ivy = dombuilder.parse(f);
        Element root = ivy.getDocumentElement();
        assertEquals("ivy-module", root.getNodeName());

        NodeList nl = root.getElementsByTagName("info");
        assertEquals(1, nl.getLength());
        assertEquals("Unset", ((Element) nl.item(0)).getAttribute("revision"));
    }

    public void testWithVersion() throws SAXException,IOException {
        File f = new File(getProject().getProperty("ivyfile"));
        assertFalse(f.exists());
        executeTarget("withVersion");
        assertTrue(f.exists());

        Document ivy = dombuilder.parse(f);
        Element root = ivy.getDocumentElement();
        assertEquals("ivy-module", root.getNodeName());

        NodeList nl = root.getElementsByTagName("info");
        assertEquals(1, nl.getLength());
        assertEquals("4.0", ((Element) nl.item(0)).getAttribute("revision"));
    }

    void _testWithEmptyVersion(String target) throws SAXException,IOException {
        File f = new File(getProject().getProperty("ivyfile"));
        assertFalse(f.exists());
        executeTarget(target);
        assertTrue(f.exists());

        Document ivy = dombuilder.parse(f);
        Element root = ivy.getDocumentElement();
        assertEquals("ivy-module", root.getNodeName());

        NodeList nl = root.getElementsByTagName("info");
        assertEquals(1, nl.getLength());
        assertEquals("Unset", ((Element) nl.item(0)).getAttribute("revision"));
    }
    public void testWithEmptyVersion1() throws SAXException,IOException {
        _testWithEmptyVersion("withEmptyVersion1");
    }
    public void testWithEmptyVersion2() throws SAXException,IOException {
        _testWithEmptyVersion("withEmptyVersion2");
    }

    void _testWithEmptyFile(String target, String which) 
        throws SAXException,IOException 
    {
        try{
            executeTarget(target);
            fail("Missing "+which+" attribute failed to throw exception");
        }
        catch (BuildException ex) {
            assertTrue("Wrong reason for failure" + ex.getMessage(),
                       ex.getMessage().contains("required attribute"));
        }
    }
    public void testWithEmptyAboutFile1() throws SAXException,IOException {
        _testWithEmptyFile("withEmptyAboutFile1", "aboutFile");
    }
    public void testWithEmptyAboutFile2() throws SAXException,IOException {
        _testWithEmptyFile("withEmptyAboutFile2", "aboutFile");
    }
    public void testWithEmptyAboutFile3() throws SAXException,IOException {
        _testWithEmptyFile("withEmptyAboutFile3", "aboutFile");
    }
    public void testWithEmptyIvyFile1() throws SAXException,IOException {
        _testWithEmptyFile("withEmptyIvyFile1", "ivyFile");
    }
    public void testWithEmptyIvyFile2() throws SAXException,IOException {
        _testWithEmptyFile("withEmptyIvyFile2", "ivyFile");
    }
    public void testWithEmptyIvyFile3() throws SAXException,IOException {
        _testWithEmptyFile("withEmptyIvyFile3", "ivyFile");
    }
}