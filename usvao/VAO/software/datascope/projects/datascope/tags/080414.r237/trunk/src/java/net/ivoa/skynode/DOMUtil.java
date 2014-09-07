package net.ivoa.skynode;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;


/** This class includes utilies that make it easier
 *  to parse XML documents in a DOM structure.
 */
public class DOMUtil {
    
    /** Find the string value of a node.
     *  Look either at the current node or in its first child.
     *  Normally the value of a named node is in an unnamed text node underneath.
     */
    public static String nValue(Node n) {
	
	if (n.getNodeValue() != null) {
	    return n.getNodeValue();
	} else if (n.getFirstChild() != null) {
	    return n.getFirstChild().getNodeValue();
	} else {
	    return null;
	}
    }
    
    /** Find a node in the tree which matches the given name. */
    public static Node findNode(Node top, String wantName) {
	return findNode(top, wantName, null);
    }
    
    /** Find a  node in the tree which matches the given name and
     *  value.  Namespaces will be ignored in the search.
     *
     *  @param top       The top of the XML tree to search.
     *  @param wantName  The desired name.
     *  @param wantValue The desired value.  If null, the value is not used.
     */
    public static Node findNode(Node top, String wantName, String wantValue) {
	
	String name = top.getNodeName();
	if (name != null) {
	    name = name.replaceAll(".*:", "");
	    if (name.equals(wantName)) {
		if (wantValue == null) {
		    return top;
		} else if (wantValue.equals(nValue(top))) {
		    return top;
		}
	    }
	}
	if (top.hasChildNodes()) {
	    NodeList children = top.getChildNodes();
	    for (int i=0; i<children.getLength(); i += 1) {
		Node test = findNode(children.item(i), wantName, wantValue);
		if (test != null) {
		    return test;
		}
	    }
	}
	return null;
    }
    
    /** Render parsed XML as a string */
    public static String xmlString(Node top) throws Exception {
	
	Transformer  t   = TransformerFactory.newInstance().newTransformer();
	StringWriter sw  = new StringWriter();
	StreamResult res = new StreamResult(sw);
	DOMSource    src = new DOMSource(top);
	t.transform(src, res);
	return sw.toString();
    }
}

