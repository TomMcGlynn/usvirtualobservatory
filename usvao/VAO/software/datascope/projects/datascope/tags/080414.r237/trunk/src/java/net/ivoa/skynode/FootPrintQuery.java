package net.ivoa.skynode;

import org.w3c.dom.Node;

/** Respond to a request for the intersection of
 *  an input footprint and the coverage of the given service.
 *  This class just returns the input -- i.e., it implies
 *  all sky coverage.
 * 
 */
class FootPrintQuery {
    
    Node   request;
    String input;
    FootPrintQuery(String input, Node node) {
	this.request = node;
	this.input   = input;
    }
    
    /** Currently this just returns the input footprint. */
    void run() throws Exception {
	
	Node top = DOMUtil.findNode(request, "Region");
	if (top == null) {
	    throw new Error("No region specified in Footprint request");
	}
	SoapWrapper sw = new SoapWrapper("Footprint");
	System.out.println(sw.prefix());
	System.out.println(DOMUtil.xmlString(top));
	System.out.println(sw.suffix());
    }
}
