package edu.jhu.pha.vospace.meta;

import java.util.List;

import edu.jhu.pha.vospace.node.Node;

/**
 * The class provides container for paginated nodes list containing current page of nodes and total amount of results found in the DB
 * @author dmitry
 *
 */
public class NodesList {
	private List<Node> nodesList;
	private int nodesCount;
	
	public NodesList(List<Node> nodesList, int nodesCount) {
		super();
		this.nodesList = nodesList;
		this.nodesCount = nodesCount;
	}
	public List<Node> getNodesList() {
		return nodesList;
	}
	public void setNodesList(List<Node> nodesList) {
		this.nodesList = nodesList;
	}
	public int getNodesCount() {
		return nodesCount;
	}
	public void setNodesCount(int nodesCount) {
		this.nodesCount = nodesCount;
	}

}
