package usvao.vaosoft.util;

import java.util.Properties;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class PropertiesToDomTest {

    public Properties createProperties() {
        return PropertiesToDom.sampleProperties();
    }

    @Test public void testConvert() throws ParserConfigurationException {
        PropertiesToDom p2d = new PropertiesToDom("goob");
        Document doc = p2d.convert(createProperties());

        Element root = doc.getDocumentElement(); 
        assertEquals("goob", root.getTagName());

        NodeList nodes = root.getChildNodes();
        assertEquals(3, nodes.getLength());
        // order is alphabetical
        assertEquals("build", nodes.item(0).getNodeName());
        assertEquals("name", nodes.item(1).getNodeName());
        assertEquals("version", nodes.item(2).getNodeName());

        assertEquals("foo", nodes.item(1).getFirstChild().getNodeValue());
        assertEquals("3.2.2", nodes.item(2).getFirstChild().getNodeValue());

        root = (Element) nodes.item(0);  // build
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("install", root.getFirstChild().getNodeName());
        assertEquals("type", root.getLastChild().getNodeName());
        assertEquals("make", root.getLastChild().getFirstChild().getNodeValue());

        root = (Element) root.getFirstChild(); // install
        assertEquals(2, root.getChildNodes().getLength());
        assertEquals("evidence", root.getFirstChild().getNodeName());
        assertEquals("some", root.getFirstChild().getFirstChild().getNodeValue());
        assertEquals("evidence", root.getLastChild().getNodeName());

        root = (Element) root.getLastChild(); // evidence
        nodes = root.getChildNodes();
        assertEquals(7, nodes.getLength());
        Node child = nodes.item(0);
        assertEquals("dirfile", nodes.item(0).getNodeName());
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("#text", child.getFirstChild().getNodeName());
        assertEquals(Node.TEXT_NODE, child.getFirstChild().getNodeType());
        assertEquals("/opt/sw lib/libsw.a", child.getFirstChild().getNodeValue());
                     
        child = nodes.item(1);
        assertEquals("dirfile", child.getNodeName());
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("#text", child.getFirstChild().getNodeName());
        assertEquals("/usr/local lib/libsw.a", child.getFirstChild().getNodeValue());

        child = nodes.item(2);
        assertEquals("dirfile", child.getNodeName());
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals(2, child.getChildNodes().getLength());
        assertEquals("dir", child.getFirstChild().getNodeName());
        assertEquals("file", child.getLastChild().getNodeName());
        assertEquals("/usr/local", child.getFirstChild().getFirstChild().getNodeValue());
        assertEquals("lib/libsw.a", child.getLastChild().getFirstChild().getNodeValue());

        child = nodes.item(3);
        assertEquals("env", child.getNodeName());
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("MY_HOME", child.getFirstChild().getNodeValue());

        child = nodes.item(4);
        assertEquals("stddirfile", child.getNodeName());
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("lib/libsw.a", child.getFirstChild().getNodeValue());

        child = nodes.item(5);
        assertEquals("stddirfile", child.getNodeName());
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("lib/libr.a", child.getFirstChild().getNodeValue());

        child = nodes.item(6);
        assertEquals("swdir", child.getNodeName());
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals(1, child.getChildNodes().getLength());
        assertEquals("/opt/sw", child.getFirstChild().getNodeValue());

    }
}