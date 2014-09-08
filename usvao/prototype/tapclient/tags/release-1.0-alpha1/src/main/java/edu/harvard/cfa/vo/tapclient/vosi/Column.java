package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A description of a table column.
 * @see Table
 * @see ForeignKey
 */
public class Column {
    private String name;
    private String description;
    private String unit;
    private String ucd;
    private String utype;

    private String dataType;
    private String arraySize;
    private String delim;
    private String extendedType;
    private String extendedSchema;

    protected List<String> flagList;
    private boolean std;

    /**
     * Constructs a Column object.  For use by subclasses.
     */
    protected Column() {
	this.name = null;
	this.description = null;
	this.unit = null;
	this.ucd = null;
	this.utype = null;
	this.dataType = null;
	this.arraySize = null;
	this.delim = null;
	this.extendedType = null;
	this.extendedSchema = null;
	this.flagList = new ArrayList<String>();
	this.std = false;
    }

    /**
     * Constructs a Column object.  For use by subclasses.
     */
    protected Column(String name, String description, String unit, String ucd, String utype, String dataType, String arraySize, String delim, String extendedType, String extendedSchema, List<String> flags, boolean std) {
	this.name = name;
	this.description = description;
	this.unit = unit;
	this.ucd = ucd;
	this.utype = utype;
	this.dataType = dataType;
	this.arraySize = arraySize;
	this.delim = delim;
	this.extendedType = extendedType;
	this.extendedSchema = extendedSchema;
	this.flagList = flags;
	this.std = std;
    }

    // Construct a Column from the underlying binding.
    Column(net.ivoa.xml.voDataService.v11.TableParam xtableParam) {
	this.name = xtableParam.getName();
	this.description = xtableParam.getDescription();
	this.unit = xtableParam.getUnit();
	this.ucd = xtableParam.getUcd();
	this.utype = xtableParam.getUtype();
	net.ivoa.xml.voDataService.v11.TableDataType xdataType = xtableParam.getDataType();
	if (xdataType != null) {
	    this.dataType = xdataType.getStringValue();
	    this.arraySize = xdataType.getArraysize();
	    this.delim = xdataType.getDelim();
	    this.extendedType = xdataType.getExtendedType();
	    this.extendedSchema = xdataType.getExtendedSchema();
	}
	List<String> xflagList = xtableParam.getFlagList();
	if (xflagList != null) {
	    this.flagList = new ArrayList<String>(xflagList.size());
	    for (String xflag: xflagList) {
		this.flagList.add(xflag);
	    }
	} else {
	    this.flagList = new ArrayList<String>();
	}
	this.std = xtableParam.getStd();
    }
    
    /**
     * Returns the column name.
     * @return the name of this Column
     */
    public String getName() { 
	return name; 
    }
    
    /**
     * Returns the column description.
     * @return a description of this Column or null if not defined
     */
    public String getDescription() { 
	return description; 
    }
     
    /**
     * Returns the column unit.
     * @return the units of this Column or null if not defined
     */
    public String getUnit() { 
	return unit;  
    }
    
    /**
     * Returns the column UCD.
     * @return the uniform content description (UCD) of this column for null if not defined
     */
    public String getUcd() { 
	return ucd; 
    }
    
    /**
     * Returns the column utype, usage-specific or unique type.
     * @return the uniform content description (UCD) of this column for null if not defined
     */
    public String getUtype() { 
	return utype; 
    }
     
    /**
     * Returns the column datatype
     * @return the datatype of this column for null if not defined
     */
    public String getDataType() { 
	return dataType; 
    }
    
    /**
     * Returns the column arraysize.
     * @return the arraysize of this column for null if not defined
     */
    public String getArraySize() { 
	return arraySize; 
    }
    
    /**
     * Returns the column delimiter.
     * @return the element delimiter of this column if it is an array or for null if not defined
     */
    public String getDelim() { 
	return delim; 
    }
    
    /** 
     * Returns the column extended type.  
     * @return the extended type of this column for null if not defined
     */
    public String getExtendedType() { 
	return extendedType; 
    }
    
    /**
     * Returns the column extended schema identifier.
     * @return the extended schema of this column for null if not defined
     */
    public String getExtendedSchema() { 
	return extendedSchema;
    }
    
    /**
     * Returns a list of keywords representing traits of the column as defined in the VODataService recommendation.
     * Possible values listed in the VODataService Recommendation include:
     * <ul>
     * <li><code>indexed<code> The column has an index on it for faster search against its values.
     * <li><code>primary<cod> The values column in the column represents in total or in part a primary key for its table.
     * <li><code>nullable<code> The column may contain null or empty values.
     * </ul>
     * Other values are allowed.
     *
     * @return a list of flags for this column.
     */
    public List<String> getFlags() { 
	return Collections.unmodifiableList(flagList);
    }
    
    /**
     * Returns true if this column has been identified as being reserver or defined by a VO standard interface.
     * @return true if the meaning of this column is reserved or defined by a standard interface, false otherwise.
     */
    public boolean isStd() { 
	return std; 
    }

    /**
     * Returns true if this column has been identified as part of the primary key for a table.
     * @return true if the column is part of a primary key, false otherwise.
     */
    public boolean isPrimary() { 
	return flagList.contains("primary");
    }

    /**
     * Returns true if this column has been identified as being part of an index.
     * @return true if the column is part of an index, false otherwise.
     */
    public boolean isIndexed() { 
	return flagList.contains("indexed");
    }

    /**
     * Returns a String representation of this Column object.  This method is intended to be used only for debugging purposes, and the format of the returned string may vary between implementations.
     * @return a String representaion of this object.
     */
    public String toString() {
    	return getName()+" "+getDataType();
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	output.println(indent+"Name: "+getName());
	output.println(indent+"Datatype: "+getDataType());
	output.println(indent+"Indexed: "+(isIndexed() ? "X" : ""));
    }
}
