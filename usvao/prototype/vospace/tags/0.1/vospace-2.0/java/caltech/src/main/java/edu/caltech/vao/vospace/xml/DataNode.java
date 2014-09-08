
package edu.caltech.vao.vospace.xml;

import edu.caltech.vao.vospace.VOSpaceException;

public class DataNode extends Node {

    /**
     * Construct a Node from the byte array
     * @param req The byte array containing the Node
     */
    public DataNode(byte[] bytes) throws VOSpaceException {
        super(bytes);
    }

    /**
     * Validate the structure of the document
     */
    protected boolean validStructure() {
	// Check to see whether accepts, provides and capabilities defined
	return true;
    }

    /**
     * Remove the <accepts> element
     */
    public void removeAccepts() throws VOSpaceException {
	node.remove("/vos:node/vos:accepts");
    }

    /**
     * Remove the <provides> element
     */
    public void removeProvides() throws VOSpaceException {
	node.remove("/vos:node/vos:provides");
    }

    /**
     * Remove the busy attribute
     */
    public void removeBusy() throws VOSpaceException {
	node.remove("/vos:node/@busy");
    }


    /**
     * Add a <view> with the specified value to the <accepts> element creating the latter
     * if it does not exist.
     * @param value The value of the <view> element
     */
    public void addAccepts(String value) throws VOSpaceException {
	boolean hasAccepts = node.has("/vos:node/vos:accepts");
	if (!hasAccepts)
	    node.add("/vos:node/vos:properties", node.PREFIX == null ? "<accepts></accepts>" : "<" + node.PREFIX + ":accepts></" + node.PREFIX + ":accepts>");
	if (value != null)
	    node.addChild("/vos:node/vos:accepts", node.PREFIX == null ? "<view uri=\"" + value + "\"/>" : "<" + node.PREFIX + ":view uri=\"" + value + "\"/>");
    }

    /**
     * Add a <view> with the specified value to the <provides> element creating the latter
     * if it does not exist.
     * @param value The value of the <view> element
     */
    public void addProvides(String value) throws VOSpaceException {
	boolean hasProvides = node.has("/vos:node/vos:provides");
	if (!hasProvides)
	    node.add("/vos:node/vos:accepts", node.PREFIX == null ? "<provides></provides>" : "<" + node.PREFIX + ":provides></" + node.PREFIX + ":provides>");
	if (value != null)
	    node.addChild("/vos:node/vos:provides", node.PREFIX == null ? "<view uri=\"" + value + "\"/>" : "<" + node.PREFIX + ":view uri=\"" + value + "\"/>");
    }

    /**
     * Set the busy attribute
     * @param value The value of the busy attribute
     */
    public void setBusy(boolean value) throws VOSpaceException {
	node.replace("/vos:node/@busy", String.valueOf(value));
    }

}
