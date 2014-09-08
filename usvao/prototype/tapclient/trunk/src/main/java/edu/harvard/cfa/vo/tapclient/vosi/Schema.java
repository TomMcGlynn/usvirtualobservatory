package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A named description of a set of logically related tables.  The information in this object is only as recent as the call to VosiService#getTableSet that generated this object.  To see if a service's tableset has changed, call VosiService#getTableSet again.  
 *
 * <pre>
   void showSchema(Schema schema) {
	System.out.println(schema+"\t[Schema]");
		
	for (Table table: schema.getTables()) {
	    System.out.println("\t"+table+"\t[Table]");
		    
	    for (Column column: table.getIndexedColumns()) {
		System.out.println("\t\t"+column+"\t[Indexed column]");
	    }
		    
	    for (ForeignKey foreignKey: table.getForeignKeys()) {
		System.out.println("\t\t"+foreignKey+"\t[Foreign key]");
	    } 
	    System.out.println("");
	}
	System.out.println("");
  }
 * </pre>
 */
public class Schema {
    private String name;
    private String title;
    private String description;
    private String utype;
    protected Map<String,Table> tables;
    private List<Table> tableList;

    /**
     * Constructs a Schema object.  For use by subclasses.
     */
    protected Schema() {
	this.name = null;
	this.title = null;
	this.description = null;
	this.utype = null;
	this.tables = new HashMap<String,Table>();
	this.tableList = new ArrayList<Table>();
    }

    /**
     * Constructs a Schema object.  For use by subclasses.
     */
    protected Schema(String name, String title, String description, String utype, List<Table> tables) {
	this.name = name;
	this.title = title;
	this.description = description;
	this.utype = utype;
	this.tables = new HashMap<String,Table>();
	this.tableList = new ArrayList<Table>(tables);
    }

    // Constructs a Schema object from the underlying binding.
    Schema(net.ivoa.xml.voDataService.v11.TableSchema xschema) {
	this.name = xschema.getName();
	this.title = xschema.getTitle();
	this.description = xschema.getDescription();
	this.utype = xschema.getUtype();
	List<net.ivoa.xml.voDataService.v11.Table> xtableList = xschema.getTableList();
	if (xtableList != null) {
	    tables = new HashMap<String,Table>();
	    tableList = new ArrayList<Table>(xtableList.size());
	    for (net.ivoa.xml.voDataService.v11.Table xtable: xtableList) {
		tables.put(xtable.getName(), new Table(xtable));
		tableList.add(new Table(xtable));
	    }
	} else {
	    tables = new HashMap<String,Table>();
	    tableList = new ArrayList<Table>();
	}
    }

    /**
     * Returns the name provided for the schema.
     * @return the name of this Schema object or null if not available.
     */
    public String getName() { 
	return name; 
    }

    /**
     * Returns the title provided for the schema.
     * @return the title of this schema or null if not available.
     */
    public String getTitle() { 
	return title; 
    }

    /**
     * Returns the description provided for the schema
     * @return the description of this schema or null if not available
     */
    public String getDescription() { 
	return description; 
    }

    /**
     * Returns the utype, usage-specific or unique type, provided for the schema
     * @return the utype of the schema or null if not available.
     */
    public String getUtype() { 
	return utype; 
    }

    /**
     * Returns a list of Table objects representing the tables 
     * provided for the schema.
     * @return a list of tables in this schema.
     */
    public List<Table> getTables() { 
	final Table[] array = tables.values().toArray(new Table[tables.size()]);
	return new AbstractList<Table>() {
	    public Table get(int index) {
		return array[index];
	    }
	    public int size() {
		return array.length;
	    }
	};
    }

    protected void add(Table newValue) {
	tables.put(newValue.getName(), newValue);
	tableList.add(newValue);
    }

    /**
     * Returns a string representation of this object.  This method is intended to be used only for debugging purposes, and the format of the returned string may vary between implementations.
     * @return a String representation of this Schema object.
     */
    public String toString() {
	return getName();
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	output.println(indent+"Name: "+getName());
	output.println(indent+"Description: "+getDescription());
	output.println(indent+"Utype: "+getUtype());
	for (Table table: getTables()) {
	    output.println(indent+"Tables: ");
	    table.list(output, indent+"  ");
	}
    }
}
