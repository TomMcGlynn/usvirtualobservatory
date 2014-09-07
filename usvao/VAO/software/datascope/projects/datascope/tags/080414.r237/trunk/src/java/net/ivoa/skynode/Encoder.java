package net.ivoa.skynode;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;


/** This class creates a VOTable from the results of a query.
 */
public class Encoder {
    
    private ResultSet          results;
    private ResultSetMetaData  rsmd;
    private StringBuffer       buf = new StringBuffer();
    private int                cols;
    private int		       rowCount = 0;
    private int[]              colTypes;
    
    
    /** Create an encoder given a JDBC ResultSet.
     */
    Encoder(ResultSet data) throws Exception {
	this.results = data;
	this.rsmd    = data.getMetaData();
	cols = rsmd.getColumnCount();
	colTypes = new int[cols];
    }
    
    
    /** Encode the results */
    String encode() throws Exception {
	votableHeader();
	votableFields();
	votableData();
	votableFooter();
	return new String(buf);
    }
    
    /** Write out the VOTable Header */
    void votableHeader() {
	buf.append("<VOTABLE>");
	buf.append("<RESOURCE xmlns=\""+SkyNode.VOTableURI+"\">");
	buf.append("<TABLE>");
    }
    
    /** Close the VOTable syntax */
    void votableFooter() {
	buf.append("</TABLE>");
	buf.append("</RESOURCE>");
	buf.append("</VOTABLE>");
    }
    
    /** Write the VOTABLE FIELD entries */
    void votableFields() throws Exception {
	for (int i=0; i<cols; i += 1) {
	    addField(i);
	}
    }
    
    /** Write the VOTABLE data */
    void votableData() throws Exception {
	buf.append("<FIELD name=\"id\" datatype=\"int\" />");
	buf.append("<FIELD name=\"pk\" datatype=\"int\" />");
	buf.append("<DATA><TABLEDATA>");
	while(results.next()) {
	    addRow();
	}
	buf.append("</TABLEDATA></DATA>");
    }
    
    /** Add a field to the VOTable. */
    void addField(int i) throws Exception {
	
	int type = rsmd.getColumnType(i+1);
	String arrSiz = "";
	String votType = null;
	
	if (type == Types.DOUBLE || type == Types.FLOAT  || type == Types.REAL) {
	    colTypes[i] = Types.DOUBLE;
	    votType = "double";
	} else if (type == Types.BIT  || type == Types.INTEGER || 
		   type == Types.SMALLINT  || type == Types.TINYINT) {
	    colTypes[i] = Types.INTEGER;
	    votType = "int";
	} else {
	    colTypes[i] = Types.CHAR;
	    arrSiz      = " arraysize=\"*\"";
	    votType = "char";
	}
	
	String name = rsmd.getColumnName(i+1);
	if (name == null || name.length() == 0) {
	    name = "__col"+(i+1);
	}
	buf.append("<FIELD name=\""+name+"\" datatype=\""+votType+"\" "+arrSiz+"/>");
    }
    
    /** Add a row to the data */
    void addRow() throws Exception {
	buf.append("<TR>");
	for(int i=0; i<cols; i += 1) {
	    addCol(i);
	}
	// Add the PK and ID columns
	rowCount += 1;
	String pk = "<TD>"+rowCount + "</TD>";
	buf.append(pk);
	buf.append(pk);
	buf.append("</TR>");
    }
    
    /** Add a column to the current row */
    void addCol(int i) throws Exception {
	buf.append("<TD>");
	if (colTypes[i] == Types.DOUBLE) {
	    buf.append(results.getDouble(i+1));
	} else if (colTypes[i] == Types.INTEGER) {
	    buf.append(results.getInt(i+1));
	} else {
	    String res = results.getString(i+1);
	    if (res != null) {
	        res = res.replace("&", "&amp;");
	        res = res.replace(">", "&gt;");
	        res = res.replace("<", "&lt;");
	        buf.append(res);
	    }
	}
	buf.append("</TD>");
    }
}
