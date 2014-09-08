package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * A description of one of the tables that makes up the set.
 * @see Schema
 */
public class Table {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.vosi.Table");

    private String name;
    private String title;
    private String description;
    private String utype;
    protected Map<String,Column> columns;
    protected Map<String,ForeignKey> foreignKeys;
    private List<Column> columnList;
    private List<ForeignKey> foreignKeyList;
    private String type;

    /**
     * Constructs a Table object.  For subclasses.
     */
    protected Table() {
	this.name = null;
	this.title = null;
	this.description = null;
	this.utype = null;
	this.columns = new HashMap<String,Column>();
	this.foreignKeys = new HashMap<String,ForeignKey>();
	this.columnList = new ArrayList<Column>();
	this.foreignKeyList = new ArrayList<ForeignKey>();
	this.type = null;
    }

    /**
     * Constructs a Table object.  For subclasses.
     */
    protected Table(String name, String title, String description, String utype, List<Column> columns, List<ForeignKey> foreignKeys, String type) {
	this.name = name;
	this.title = title;
	this.description = description;
	this.utype = utype;
	this.columns = new HashMap<String,Column>();
	this.foreignKeys = new HashMap<String,ForeignKey>();
	this.columnList = columns;
	this.foreignKeyList = foreignKeys;
	this.type = type;
    }

    // Constructs a Table object from the underlying binding.
    Table(net.ivoa.xml.voDataService.v11.Table xtable) {
	this.name = xtable.getName();
	this.title = xtable.getTitle();
	this.description = xtable.getDescription();
	this.utype = xtable.getUtype();
	List<net.ivoa.xml.voDataService.v11.TableParam> xcolumnList = xtable.getColumnList();
	if (xcolumnList != null) {
	    this.columns = new HashMap<String,Column>();
	    this.columnList = new ArrayList<Column>(xcolumnList.size());
	    for (net.ivoa.xml.voDataService.v11.TableParam xcolumn: xcolumnList) {
		Column column = new Column(xcolumn);
		this.columns.put(column.getName(), column);
		this.columnList.add(column);
	    }
	} else {
	    this.columns = new HashMap<String,Column>();
	    this.columnList = new ArrayList<Column>();
	}
	List<net.ivoa.xml.voDataService.v11.ForeignKey> xforeignKeyList = xtable.getForeignKeyList();
	if (xforeignKeyList != null) {
	    this.foreignKeys = new HashMap<String,ForeignKey>();
	    this.foreignKeyList = new ArrayList<ForeignKey>(xforeignKeyList.size());
	    for (net.ivoa.xml.voDataService.v11.ForeignKey xforeignKey: xforeignKeyList) {
		ForeignKey foreignKey = new ForeignKey(xforeignKey);
		this.foreignKeys.put(foreignKey.getTargetTable(), foreignKey);
		this.foreignKeyList.add(foreignKey);
	    }
	} else {
	    this.foreignKeys = new HashMap<String,ForeignKey>();
	    this.foreignKeyList = new ArrayList<ForeignKey>();
	}
	this.type = xtable.getType();
    }

    /**
     * Returns the name provided for the table.
     * @return the table name or null if not specify by the service.
     */
    public String getName() { 
	return name;
    }

    /**
     * Returns the title provided for the table.
     * @return the table title or null if not specify by the service.
     */
    public String getTitle() { 
	return title; 
    }

    /**
     * Returns the description provided for the table.
     * @return the table description or null if not specify by the service.
     */
    public String getDescription() { 
	return description; 
    }

    /**
     * Returns the utype, usage-specific or unique type, provided for the table
     * @return the table utype or null if not specify by the service.
     */
    public String getUtype() { 
	return utype; 
    }

    /**
     * Returns a List of Column objects representing the columns
     * provided for the table.
     * @return a list of table columns
     */
    public List<Column> getColumns() { 
	final Column[] array = columns.values().toArray(new Column[columns.size()]);
	return new AbstractList<Column>() {
	    public Column get(int index) { return array[index]; }
	    public int size() { return array.length; }
	};
    }

    protected void add(Column newValue) {
	columns.put(newValue.getName(), newValue);
	columnList.add(newValue);
    }

    /**
     * Return a list of indexed columns or an empty list if no
     * indexed columns were specified.  This list is constructed based on 
     * column metadata provided by the service.
     * 
     * @return a list of indexed columns or an empty list if no 
     * indexed columns were identified.
     *
     * @see Column#isIndexed
     */
    public List<Column> getIndexedColumns() { 
	List<Column> list = new ArrayList<Column>();

	for (Column column: columnList) {
	    if (column.isIndexed()) {
		list.add(column);
	    }
	}

	return Collections.unmodifiableList(list); 
    }

    /**
     * Returns a list of primary key columns or an empty list if no 
     * primary key columns are identified.
     *
     * @return a list of columns constituting the primary key or an 
     * empty list if no primary key columns were specified.
     *
     * @see Column#isPrimary
     */
    public List<Column> getPrimaryKeys() { 
	List<Column> list = new ArrayList<Column>();

	for (Column column: columnList) {
	    if (column.isPrimary()) {
		list.add(column);
	    }
	}

	return Collections.unmodifiableList(list);
    }

    /**
     * Returns a list of ForeignKey objects.
     * @return a list of foreign keys
     */
    public List<ForeignKey> getForeignKeys() { 
	final ForeignKey[] array = foreignKeys.values().toArray(new ForeignKey[foreignKeys.size()]);
	return new AbstractList<ForeignKey>() {
	    public ForeignKey get(int index) { return array[index]; }
	    public int size() { return array.length; }
	};
    }

    protected void add(ForeignKey newValue) {
	foreignKeys.put(newValue.getTargetTable(), newValue);
	foreignKeyList.add(newValue);
    }

    /**
     * Returns the table type as provided for the table.  The VODataService 
     * Recommendation notes that <code>"table"</code> or <code>"view"</code>
     * may be possible values.
     *
     * @return the table type or null if not specified by the service.
     */
    public String getType() { 
	return type; 
    }

    /**
     * Return a String representation of this object.  This method is intended to be used only for debugging purposes, and the format of the returned string may vary between implementations.
     * @return  a String representation of this Table object
     */
    public String toString() {
	return getName();
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	output.println(indent+"Name: "+getName());
	output.println(indent+"Type: "+getType());
	output.println(indent+"Title: "+getTitle());
	output.println(indent+"Description: "+getDescription());
	output.println(indent+"Utype: "+getUtype());

	for (Column column: getColumns()) {
	    output.println(indent+"Columns: ");
	    column.list(output, indent+"  ");
	}

 	for (ForeignKey foreignKey: getForeignKeys()) {
	    output.println(indent+"Foreign Keys: ");
	    foreignKey.list(output, indent+"  ");
	}
   }
}
