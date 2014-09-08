package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A description of a foreign keys, one or more columns from the current table that can be used to join with another table.
 * @see Table
 * @see ForeignKey
 */
public class ForeignKey {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.vosi.ForeignKey");

    private String targetTable;
    protected Map<String,String> fkColumns;
    private String description;
    private String utype;

    /**
     * Constructs a ForeignKey object.  For use by subclasses.
     */
    protected ForeignKey() {
	this.targetTable = null;
	this.fkColumns = new HashMap<String,String>();
	this.description = null;
	this.utype = null;
    }

    /**
     * Constructs a ForeignKey object.  For use by subclasses.
     */
    protected ForeignKey(String targetTable, Map<String,String> fkColumns, String description, String utype) {
	this.targetTable = targetTable;
	this.fkColumns = fkColumns;
	this.description = description;
	this.utype = utype;
    }

    // Constructs a FoeignKey object from the underlying binding.
    ForeignKey(net.ivoa.xml.voDataService.v11.ForeignKey xforeignKey) {
	this.targetTable = xforeignKey.getTargetTable();
	if (logger.isLoggable(Level.FINER)) 
	    logger.log(Level.FINER, "targetTable: "+targetTable);
	List<net.ivoa.xml.voDataService.v11.FKColumn> xfkColumnList = xforeignKey.getFkColumnList();
	if (xfkColumnList != null) {
	    this.fkColumns = new HashMap<String,String>();
	    for (net.ivoa.xml.voDataService.v11.FKColumn xfkColumn: xfkColumnList) {
		if (logger.isLoggable(Level.FINER)) 
		    logger.log(Level.FINER, "fkColumn: "+xfkColumn.getFromColumn()+" = "+xfkColumn.getTargetColumn());
		this.fkColumns.put(xfkColumn.getFromColumn(), xfkColumn.getTargetColumn());
	    }
	} else {
	    this.fkColumns = new HashMap<String,String>();
	}
	this.description =  xforeignKey.getDescription();
	this.utype =  xforeignKey.getUtype();
    }

    /**
     * Returns the name of the table which can be joined with the table containing this ForeignKey object.  The name is taken verbatim from the service response but should be a qualified name, with schema name, if applicable.
     * @return the target table of the association or null if not provided.
     */
    public String getTargetTable() { 
	return targetTable; 
    }

    /**
     * Returns a Map of column pairs where the key is the from column in the table containing this foreign key and the value is the column in the target table.
     * <pre>
     *   Map<String,String> fkColumnMap = fkColumnPari: getFKColumns();
     *   Iteartor<Map.Entry<String,String>> iterator = fkColumnMap.entrySet().iterator();
     *   while (iteartor.hasNext()) {
     *       Map.Entry<String,String> fkColumnPair = iterator.next();
     *       System.out.println(table.getName()+"."+fkColumnPair.getKey()+
     *                          " = "+
     *                          foreignKey.getTargetTable()+"."+fkColumnPair.getValue();
     *   }
     * </pre>  
     * @return a list of columns, represented as FKColumn objects, that constitue the foreign key.
     */
    public Map<String,String> getFKColumns() { 
	return Collections.unmodifiableMap(fkColumns);
    }

    protected void add(String fromColumn, String targetColumn) {
	fkColumns.put(fromColumn, targetColumn);
    }

    /**
     * Returns a description of the foreign key.
     * @return a description of this ForeignKey object or null if not defined
     */
    public String getDescription() { 
	return description; 
    }

    /**
     * Returns the utype, usage-specific or unique type, provided for the key.
     * @return the utype of this ForeignKey object or null if not defined
     */
    public String getUtype() { 
	return utype; 
    }
    
    /**
     * Returns a String representation of the this object.  This method is intended to be used only for debugging purposes, and the format of the returned string may vary between implementations.
     * @return a String representation of the this object.
     */
    public String toString() {
	StringBuilder sb = new StringBuilder();

	String prefix = "";
	Iterator<Map.Entry<String,String>> iterator = getFKColumns().entrySet().iterator();
	while (iterator.hasNext()) {
	    Map.Entry<String,String> fkPair = iterator.next();
	    sb.append(prefix);
	    sb.append(fkPair.getKey());
	    sb.append(" = ");
	    sb.append(targetTable);
	    sb.append(".");
	    sb.append(fkPair.getValue());
	    prefix = " and ";
	}
	
	return sb.toString();
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String targetTable = getTargetTable();
	String description = getDescription();
	String utype = getUtype();
	Map<String,String> fkColumns = getFKColumns();
	String prefix = "";
	Iterator<Map.Entry<String,String>> iterator = fkColumns.entrySet().iterator();
	    
	while (iterator.hasNext()) {
	    Map.Entry<String,String> fkPair = iterator.next();
	    output.print(indent+prefix);
	    output.print(fkPair.getKey());
	    output.print(" = ");
	    output.print(targetTable);
	    output.print(".");
	    output.println(fkPair.getValue());
	    prefix = " and ";
	}
    }
}
