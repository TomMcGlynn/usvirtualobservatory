package net.ivoa.skynode;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/** This class parses SOAP SkyNode inputs and
 *  creates the classes to handle the individual requests
 *  and hands control to them.
 */
class SoapParser {
    
    String   input;
    Node     soapEnvelope;
    Document doc;
    
    SoapParser() throws Exception {
	
	StringBuffer sb = new StringBuffer();
	java.io.BufferedReader bf = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
	
	String line;
	while ( (line=bf.readLine()) != null) {
	    sb.append(line);
	}
	input = new String(sb);
	FullNode.log("SoapParser: Input:"+input);
	
	InputSource docSource = new InputSource(new java.io.StringReader(input));
	
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);
	
	DocumentBuilder db  = dbf.newDocumentBuilder();
	                doc = db.parse(docSource);
    }
    
    /** Process the request */
    void run() throws Exception {
	
	Node body = DOMUtil.findNode(doc, "Body");
	if (body == null) {
	    throw new Exception("No SOAP Body element found");
	}
	
	NodeList  children = body.getChildNodes();
	
	for (int i=0; i<children.getLength(); i += 1) {
	    Node child  = children.item(i);
	    String name = child.getNodeName().replaceAll(".*:", "");
	    if (name.equals("ExecutePlan")) {
		doPlan(child);
		break;
	    } else if (name.equals("PerformQuery")) {
		doQuery(child);
		break;
	    } else if (name.equals("Table") ||
		       name.equals("Tables") ||
		       name.equals("Column") ||
		       name.equals("Columns") ||
		       name.equals("GetAvailability") ||
		       name.equals("QueryCost") ||
		       name.equals("Formats") ||
		       name.equals("Functions")) {
		doMeta(child);
		break;
	    }
	}
    }
    
    /** When we execute a plan there are two steps to the process:
     *    - Do we need to upload a table from down-node elements.
     *    - Execute a query on the current node.
     */
    void doPlan(Node plan) throws Exception {
	new PlanExecuter(input, doc, plan).run();
    }
    
    /** Do a simple query -- we do this here, but it probably
     *  should be somewhere else.
     */
    void doQuery(Node query) throws Exception {
	
	String sql = XSLTransform.translate(SkyNode.XSLFile, query);
	
	DBQuery dbq = new DBQuery();
    	Query qry = new Query(dbq, sql, null, null, null, null);
	SoapWrapper wrap = new SoapWrapper("PerformQuery", "VOTableData");
	String response  = wrap.prefix();
	try {
	    qry.transform();
	    String votable = qry.execute();
	    response += votable;
	} catch (Exception e) {
	    response = "<ErrorData><Error>"+e+"</Error></ErrorData>";
	}
	response += wrap.suffix();
	System.out.println(response);
    }

    
    /** Perform some sort of metadata query */
   void doMeta(Node request) throws Exception {
       
       String name = request.getNodeName().replaceAll(".*:", "");
       if (name.equals("Columns") || name.equals("Column") ||
	   name.equals("Tables")  || name.equals("Table")) {
	   new MetaQuery(input, name, request).run();
       } else if (name.equals("Footprint")) {
	   new FootPrintQuery(input, request).run();
       } else if (name.equals("GetAvailability")  || name .equals("Functions")) {
	   FullNode.log("GetAvailability query!");
	   throw new Error("Unsupported operation");
       } else if (name.equals("QueryCost")) {
	   new QueryCost(input, request).run();
       } else if (name.equals("Formats")) {
	   formats();
       }
    }
    
    /** Return the formats supported -- only VOTABLE at teh moment */
    void formats() {
	SoapWrapper sw = new SoapWrapper("Formats");
	System.out.println(sw.prefix()+"<string>VOTable</string>"+sw.suffix());
    }
}
