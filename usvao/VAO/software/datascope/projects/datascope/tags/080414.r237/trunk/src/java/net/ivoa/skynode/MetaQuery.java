
package net.ivoa.skynode;

import java.sql.ResultSet;
import java.sql.Types;

import org.w3c.dom.Node;

/** Handle the SkyNode database metadata queries.
 */
class MetaQuery {
    
    
    String input;
    String request;
    Node   top;
    String response;
    SoapWrapper sw;
    StringBuffer buf = new StringBuffer();
    DBQuery dbq;

    /** Create a MetaQuery object with the input request string, and
     *  the XML tree of the request.
     */
    MetaQuery (String input, String request, Node top) {
	System.err.println("MetaQuery started:"+request);
	this.input   = input;
	this.request = request;
	this.top     = top;
    }
    
    /** Process the request */
    void run() throws Exception {
	
	SoapWrapper sw = new SoapWrapper(request);
	if (request.equals("Tables")) {
	    doTables();
	
	    
	} else if (request.equals("Table")) {
	    String table = getTable(top);
	    doTable(table);
	    
	} else if (request.equals("Columns")) {
	    String table = getTable(top);
	    doColumn(table, null);
	    
	} else if (request.equals("Column")) {
	    
	    String table = getTable(top);
	    String col   = getColumn(top);
	    doColumn(table,col);
	} else {
	    throw new Error("Invalid metadata request:"+request);
	}
	
	String output = sw.prefix() + new String(buf) + sw.suffix();
	FullNode.log("MetaQuery output:"+output);
	System.out.print(output);
    }
    
    /** Look for the specified table in the request */
    private String getTable(Node top) {
	
	Node tabNode = DOMUtil.findNode(top, "table");
	if (tabNode == null) {
	    throw new Error("No table specified in request:"+request);
	}
	String value = DOMUtil.nValue(tabNode);
	if (value == null || value.length() == 0) {
	    throw new Error("Invalid table node for request:"+request);
	}
	return value;
    }
    
    /** Look for the specified column in the request */
    private String getColumn(Node top) {
	
	Node tabNode = DOMUtil.findNode(top, "column");
	if (tabNode == null) {
	    throw new Error("No column specified in column request");
	}
	String value = DOMUtil.nValue(tabNode);
	if (value == null || value.length() == 0) {
	    throw new Error("Invalid column node in column request");
	}
	return value;
    }
    
    /** Process a Tables request */
    private void doTables() {
	System.err.println("Tables!"+SkyNode.TableName.length);
	for (int i=0; i<SkyNode.TableName.length; i += 1) {
	    System.err.println("Table:"+SkyNode.TableName[i]);
	    doTable(SkyNode.TableName[i]);
	}
    }
    
    
    /** Process a Table request or do individual tables in a Tables */
    private void doTable(String table) {
	int i = checkTable(table);
	buf.append("<MetaTable>");
	buf.append("<Name>"+table+"</Name>");
	buf.append("<Description>"+SkyNode.TableDesc[i]+"</Description>");
	buf.append("<Rows>"+SkyNode.TableSize[i]+"</Rows>");
	buf.append("</MetaTable>");
    }
    
    /** See if we know this table and if so return its index */
    private int checkTable(String table) {
	for (int i=0; i<SkyNode.TableName.length; i += 1) {
	    if (table.equals(SkyNode.TableName[i])) {
		return i;
	    }
	}
	throw new Error("Table "+table+" not found in metadata request.");
    }
    
    /** Process  Columns and Column requests.  The Columns request
     *  sets the column string to null.
     */
    private void doColumn(String table, String column) throws Exception {
	
	checkTable(table);
	dbq = new DBQuery();
	
	ResultSet rs = dbq.getMetadata().getColumns(null, null, table, null);
	doColumns(rs);
    }
    
    /** Process the result set we got in the columns request.
     *  We use a query of the underlying table to get information
     *  about the columns, except that we add in a little
     *  UCD information for the RA and Dec fields.
     */
    private void doColumns(ResultSet rs) throws Exception {
	
	String typFld = "char[*]";
	while (rs.next()) {
	    
	    String name = rs.getString("COLUMN_NAME");
	    int    type = rs.getInt("DATA_TYPE");
	    
	    if (type == Types.DOUBLE  || type == Types.REAL) {
		typFld = "double";
	    } else if (type == Types.FLOAT) {
		typFld = "float";
	    } else if (type == Types.TINYINT  || type == Types.SMALLINT || type == Types.INTEGER) {
		typFld = "int";
	    }
	
	    String ucd = null;
	    String nm = name.toLowerCase();
	    if (nm.equals(SkyNode.raName)) {
	        ucd = "pos.eq.ra;meta.main";
	    } else if (nm.equals(SkyNode.decName)) {
	        ucd = "pos.eq.dec;meta.main";
	    }
	    buf.append("<MetaColumn>");
	    buf.append("<Name>"+name+"</Name>");
	    buf.append("<Type>"+type+"</Type>");
	    if (ucd != null) {
		buf.append("<UCD>"+ucd+"</UCD>");
	    }
	    buf.append("</MetaColumn>");
	}
    }
}
