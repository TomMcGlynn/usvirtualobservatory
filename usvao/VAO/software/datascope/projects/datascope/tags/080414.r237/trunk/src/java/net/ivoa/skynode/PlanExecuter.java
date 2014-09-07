package net.ivoa.skynode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;

/** This class handles an Execution plan. */
class PlanExecuter {
    
    String   input;
    Node     executePlan;
    Document doc;
    
    
    /** Initialize with the user input, and the parsed XML. */
    PlanExecuter(String input, Document doc, Node executePlan) {
	this.input       = input;
	this.executePlan = executePlan;
	this.doc         = doc;
    }
    
    /** Run the plan */
    void run() throws Exception {
	
	// Find the element that we want to work with.
	String myTarget = SkyNode.TargetNode;
	
	
	FullNode.log("PlanExecuter: Target:"+myTarget);
	
	Node target = DOMUtil.findNode(executePlan, "Target", SkyNode.TargetNode.toUpperCase());
	if (target == null) {
	    throw new Exception("Unable to find valid target");
	}
	// Element is the Query plan element for the current skynode.
	Node element = target.getParentNode();
	
	// Query is the Statement for the query for the current skynode.
	Node query   = DOMUtil.findNode(element, "Statement");
	
	// Convert to a SQL (more or less) string.
	String sql = XSLTransform.translate(SkyNode.XSLFile, query);
	FullNode.log("PlanExecuter: SQL after XSL transform is:"+sql);
	
	
	Node upload = DOMUtil.findNode(executePlan, "UploadTableAlias");
	String uploadAlias = null;
	if (upload != null) {
	    uploadAlias = DOMUtil.nValue(upload);
	}
	
	// Find the FROM clause in the query.  We assume that
	// the first table that is not uploaded from a downline
	// node is the table on which REGION constrainst should
	// be applied.
	
	Node from = DOMUtil.findNode(query, "From");
	if (from == null) {
	    throw new Exception("No tables for query");
	}
	NodeList ns   = from.getChildNodes();
	String tableAlias = null;
	
	// Find the first alias that is not the upload alias.
	// This is used in Region and XMATCH processing.
	for (int i=0; i<ns.getLength(); i += 1) {
	    NamedNodeMap att = ns.item(i).getAttributes();
	    if (att != null) {
		Node aliasNode = att.getNamedItem("Alias");
		if (aliasNode != null) {
		    String alias = DOMUtil.nValue(aliasNode);
		    if (alias != null) {
		        if (!alias.equals(uploadAlias)) {
			    tableAlias = alias;
			    break;
			}
		    }
		}
	    }
	}
	
	// Create an object that will connect with the underlying
	// database.
	DBQuery dbq = new DBQuery();
	
	// See if there are any downline nodes in the query plan
	// and if so execute them.
	
	Node next = element.getNextSibling();
	while (next != null && next.getNodeName().equals("#text")) {
	    next = next.getNextSibling();
	}
	if (next != null) {
	    System.err.println("Next type is:"+next.getNodeName());
	    // We seem to have subsequent plan elements.
	    Node hosts = DOMUtil.findNode(next, "Hosts");
	    if (hosts == null) {
		throw new Error("No Hosts for downline query element");
	    }
	    Node str = DOMUtil.findNode(hosts, "string");
	    String host = null;
	    if (str != null) {
		host = DOMUtil.nValue(str);
	    } else {
		host = DOMUtil.nValue(hosts);
	    }
	    if (host == null || host.length() == 0) {
		throw new Error("Unable to find valid host name for downloine query");
	    }
	    
	    String aliasTable = null;
	    // Find the name of the upload table
	    for (int i=0; i<ns.getLength(); i += 1) {
	        NamedNodeMap att = ns.item(i).getAttributes();
	        if (att != null) {
		    Node aliasNode = att.getNamedItem("Alias");
		    if (aliasNode != null) {
		        String alias = DOMUtil.nValue(aliasNode);
		        if (alias != null) {
		            if (alias.equals(uploadAlias)) {
			        Node tname = att.getNamedItem("Name");
				aliasTable = DOMUtil.nValue(tname);
				break;
			    }
			}
		    }
		}
	    }
	    if (aliasTable == null || aliasTable.length() == 0) {
		throw new Error("Unable to find table name to upload to.");
	    }
	    
            URL url = new URL(host);
	    FullNode.log("PlanExecuter: Forwarding query to downside host:"+host);
        
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("SOAPAction", "SkyNode.ivoa.net/ExecutePlan");
	    conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
	    conn.setDoOutput(true);
	    conn.connect();
	    
	    OutputStream os = conn.getOutputStream();
	    os.write(input.getBytes());
	    os.close();
	    
	    
	    // Ingest the data we get from the downline node.
	    FullNode.log("PlanExecuter: Ingesting downside data");
	    Ingester ing  = new Ingester(dbq);
	    ing.setTableName(aliasTable);
	    ing.ingest(conn.getInputStream());
	    
	} else {
	    FullNode.log("PlanExecuter: Leaf node in query");
	}
	
	FullNode.log("PlanExecuter: Initiating local query");
	
	// Execute the query on the local database after any
	// downline data has been uploaded.
	
	Query qry = new Query(dbq, sql, tableAlias, uploadAlias, executePlan, element);
	
	SoapWrapper wrap = new SoapWrapper("ExecutePlan", "VOTableData");
	String response  = wrap.prefix();
	try {
	    qry.transform();
	    String votable = qry.execute();
	    response      += votable;
	} catch (Exception e) {
	    response = "<ErrorData><Error>"+e+"</Error></ErrorData>";
	}
	response += wrap.suffix();
	System.out.println(response);
	FullNode.log("PlanExecuter: Action at "+myTarget+" complete");
    }
}
