
package edu.caltech.vao.vospace.xml;

import java.io.InputStream;

import java.lang.reflect.*;
import javax.servlet.http.HttpServletRequest;

import edu.caltech.vao.vospace.VOSpaceException;

/** 
 * A factory for creating nodes
 */
public class NodeFactory {

    private static NodeFactory ref;

    private NodeFactory() {}

    /*
     * Get a NodeFactory
     */
    public static NodeFactory getInstance() {
	if (ref == null) ref = new NodeFactory();
	return ref;
    }

    /*
     * Get a node
     */
    public Node getNode(InputStream in, int len) throws VOSpaceException{
	Node node = null;
	try {
	    byte[] bytes = new byte[len];
	    in.read(bytes, 0, len);
	    String type = getType(bytes);
	    node = (Node) Class.forName("edu.caltech.vao.vospace.xml." + type).getConstructor(byte[].class).newInstance(bytes);
	} catch (Exception e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e.getMessage());
	}
	return node;
    }

    /*
     * Get a node
     */
    public Node getNode(HttpServletRequest req) throws VOSpaceException{
	Node node = null;
	try {
	    InputStream in = req.getInputStream();
	    int len = req.getContentLength();
	    byte[] bytes = new byte[len];
	    in.read(bytes, 0, len);
	    String type = getType(bytes);
	    node = (Node) Class.forName("edu.caltech.vao.vospace.xml." + type).getConstructor(byte[].class).newInstance(bytes);
	} catch (Exception e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e.getMessage());
	}
	return node;
    }

    /*
     * Get a node
     */
    public Node getNode(String req) throws VOSpaceException {
	Node node = null;
	try {
	    byte[] bytes = req.getBytes();
	    String type = getType(bytes);
	    node = (Node) Class.forName("edu.caltech.vao.vospace.xml." + type).getConstructor(byte[].class).newInstance(bytes);
	} catch (Exception e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e.getMessage());
	}
	return node;
    }

    private String getType(byte[] bytes) {
	String doc = new String(bytes).replace("'", "\"");
	int start = doc.indexOf("\"", doc.indexOf("xsi:type"));
	int end = doc.indexOf("\"", start + 1);
	String type = doc.substring(start + 1, end);
	return type.substring(type.indexOf(":") + 1);
    }

    /**
     * Get a node of the default type for the service
     * @return a Node of the default type
     */
    public Node getDefaultNode() throws VOSpaceException {
	String datanode = "<node xmlns=\"http://www.ivoa.net/xml/VOSpace/v2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"vos:DataNode\" uri=\"\" busy=\"false\"><properties></properties><accepts></accepts><provides></provides><capabilities></capabilities></node>";
	return getNode(datanode);
    }

}