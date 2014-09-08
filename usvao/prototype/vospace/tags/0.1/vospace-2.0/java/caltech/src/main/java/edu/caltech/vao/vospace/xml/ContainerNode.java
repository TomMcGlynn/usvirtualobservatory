
package edu.caltech.vao.vospace.xml;

import edu.caltech.vao.vospace.VOSpaceException;

public class ContainerNode extends DataNode {

    /**
     * Construct a Node from the byte array
     * @param req The byte array containing the Node
     */
    public ContainerNode(byte[] bytes) throws VOSpaceException {
        super(bytes);
    }

    /*
     * Add a child node to the container's list
     * @param uri The identifier of the child node
     */
    public void addNode(String identifier) throws VOSpaceException {
	if (!node.has("/vos:node/vos:nodes")) {
	    node.add("/vos:node/vos:capabilities", node.PREFIX == null ? "<nodes></nodes>" : "<" + node.PREFIX + ":nodes></" + node.PREFIX + ":nodes>");
	} else if (!node.has("/vos:node/vos:nodes/vos:node")) {
	    node.remove("/vos:node/vos:nodes");
	    node.add("/vos:node/vos:capabilities", node.PREFIX == null ? "<nodes></nodes>" : "<" + node.PREFIX + ":nodes></" + node.PREFIX + ":nodes>");
	}
	if (identifier != null)
	    node.addChild("/vos:node/vos:nodes", node.PREFIX == null ? "<node uri=\"" + identifier + "\"/>" : "<" + node.PREFIX + ":node uri=\"" + identifier + "\"/>");
    }

}
