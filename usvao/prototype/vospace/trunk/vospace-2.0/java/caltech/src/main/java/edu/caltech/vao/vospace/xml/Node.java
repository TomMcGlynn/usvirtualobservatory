
package edu.caltech.vao.vospace.xml;

import java.util.HashMap;

import edu.caltech.vao.vospace.VOSpaceException;

public class Node {

    protected XMLObject node;

    /**
     * Construct a Node from the byte array
     * @param req The byte array containing the Node
     */
    public Node(byte[] bytes) throws VOSpaceException {
	node = new XMLObject(bytes);
    }

    /**
     * Get the uri of the node
     * @return The uri of the node
     */
    public String getUri() throws VOSpaceException {
	return node.xpath("/vos:node/@uri")[0];
    }

    /**
     * Get the type of the node
     * @return The type of the node
     */
    public String getType() throws VOSpaceException {
	return node.xpath("/vos:node/@xsi:type")[0];
    }

    /**
     * Set the uri of the node
     * @param uri The new uri of the node
     */
    public void setUri(String uri) throws VOSpaceException {
	node.replace("/vos:node/@uri", uri);
    }

    
    /**
     * Check whether the node has any properties set
     * @return whether the node has any properties set
     */
    public boolean hasProperties() throws VOSpaceException {
	try {
	    return node.has("/vos:node/vos:properties/vos:property");
	} catch (Exception e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e);
	}
    }

    /**
     * Get the properties set on the node
     * @return any properties the node has set on it
     */
    public HashMap<String, String> getProperties() throws VOSpaceException {
	try {
	    HashMap<String, String> properties = new HashMap<String, String>();
	    String[] propUris = node.xpath("/vos:node/vos:properties/vos:property/@uri");
	    for (String uri: propUris) {
		String value = node.xpath("/vos:node/vos:properties/vos:property[@uri = '" + uri + "']")[0];
		properties.put(uri, value);
	    }
	    return properties;
	} catch (Exception e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e);
	}
    }

    /**
     * Remove the <properties> element
     */
    public void removeProperties() throws VOSpaceException {
	node.remove("/vos:node/vos:properties");
    }

    /**
     * Remove the <capabilities> element
     */
    public void removeCapabilities() throws VOSpaceException {
	node.remove("/vos:node/vos:capabilities");
    }

    /**
     * Add a <capability> with the specified value to the <capabilities> element creating the latter
     * if it does not exist.
     * @param value The value of the <capability> element
     */
    public void addCapabilities(String value) throws VOSpaceException {
	boolean hasCapabilities = node.has("/vos:node/vos:capabilities");
	if (!hasCapabilities)
	    node.add("/vos:node/vos:provides", node.PREFIX == null ? "<capabilities></capabilities>" : "<" + node.PREFIX + ":capabilities></" + node.PREFIX + ":capabilities>");
	if (value != null)
	    node.addChild("/vos:node/vos:capabilities", node.PREFIX == null ? "<capability uri=\"" + value + "\"/>" : "<" + node.PREFIX + ":capability uri=\"" + value + "\"/>");
    }

    /**
     * Get a string representation of the node
     * @return a string representation of the node
     */
    public String toString() {
	return node.toString();
    }

}
