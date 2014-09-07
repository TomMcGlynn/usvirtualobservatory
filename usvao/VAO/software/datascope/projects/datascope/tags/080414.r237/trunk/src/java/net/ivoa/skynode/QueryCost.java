package net.ivoa.skynode;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/** This class handles QueryCost requests */
class QueryCost {
    
    String input;
    Node   request;
    DBQuery dbq;
    
    
    /** Initialize with the String and parse XML versions of the request */
    QueryCost(String input, Node request) throws Exception {
	this.input   = input;
	this.request = request;
        dbq = new DBQuery();
    }
    
    /** Process the request */
    void run() throws Exception {
	
	// Get SQL
	String sql = XSLTransform.translate(SkyNode.XSLFile, request);
	if (sql == null || sql.length() == 0) {
	    throw new Error("No SQL in cost request");
	}
	
	Node from = DOMUtil.findNode(request, "From");
	if (from == null) {
	    throw new Error("Query cost request with no table specified");
	}
	
	Node tab = DOMUtil.findNode(from, "Table");
	NamedNodeMap att = tab.getAttributes();
	String alias = null;
	if (att != null) {
	    Node aliasNode = att.getNamedItem("Alias");
	    if (aliasNode != null) {
	        alias = DOMUtil.nValue(aliasNode);
	    }
	}
	
	
	Query q = new Query(dbq, sql, alias, null);
	q.transform();
	
	// We assume the query is of the form "select count(*) ...
	// and just return the value
	
	int count = q.count();
	
	
	SoapWrapper sw = new SoapWrapper("QueryCostResponse");
	System.out.println(sw.prefix()+count+sw.suffix());
//	System.out.println(sw.prefix() + "1" + sw.suffix());
//	System.out.println(sw.prefix() + "10000000" + sw.suffix());
    }
}
