package net.ivoa.skynode;

// SAX classes
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.util.ArrayList;

// SQL classes
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** This class ingests a VOTable into a table in a database.
 *  It skips down until it sees FIELD elements.
 *  These are used to define the rows of the temporary
 *  table being created.  When a TABLEDATA element is
 *  seen the temporary table is created.
 *  Then a new row is added for each TR element read.
 *  The scan is terminated by the first </TABLEDATA>, </RESOURCE>
 *  or </VOTABLE>.
 */
class Ingester {
    
    private String     tabName = SkyNode.TempTableName;
    private DBQuery    dbq;
    private Connection db;
    
    
    /** Create an ingester and give it a link to the
     *  database that will be used to store the results.
     */
    Ingester(DBQuery dbq) throws Exception {
	this.dbq = dbq;
	db  = dbq.getConnection();
    }
    
    /** Set the table name to be used. */
    void setTableName(String newName) {
	tabName = newName;
    }
    
    /** This class does SAX parse of a VOTable and adds the data to
     *  the database.
     */
    private class IngesterCallBack extends DefaultHandler {
	
	ArrayList<String> names = new ArrayList<String>();
	ArrayList<String> types = new ArrayList<String>();
        PreparedStatement updater;
	int fieldCount  = 0;
	int currCol     = 0;
	int currRow     = 0;
	int[] colType;
	
	boolean active    = false;
	StringBuffer buf;
	
    
	/** Called at the beginning of every element.
	 *  A FIELD element gives us the input column names and types.
	 *  The TABLEDATA element tells us we are ready to get data
	 *  and can create the table we are going to copy that data to.
	 *  We need to save the values inside TD elements.
	 */
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    if (qName.equals("FIELD")) {
		// Need to know the name and type of the field.
		String name = attrib.getValue("name");
		String type = attrib.getValue("datatype");
		fieldCount += 1;
		if (name == null || name.length() == 0) {
		    name = "__col"+fieldCount;
		}
		if (type == null || type.length() == 0) {
		    throw new Error("Invalid or null type for column:"+fieldCount);
		}
		names.add(name);
		// ID may be too long to fit in an int.
		if (type.equals("float") || type.equals("double")  || name.equals("id")  ) {
		    types.add("double precision");
		} else if (type.equals("int") || type.equals("long")  || type.equals("byte")) {
		    types.add("int");
		} else {
		    types.add("varchar("+SkyNode.MaxString+")");
		}
		
	    } else if (qName.equals("TABLEDATA")) {
		
		if (fieldCount == 0) {
		    throw new Error("No fields in ingest");
		}
		StringBuffer create = new StringBuffer();
		create.append("create table "+tabName +"(");
		String separator = "";
		colType = new int[fieldCount];
		
		for (int i=0; i<fieldCount; i += 1) {
		    String type = types.get(i);
		    create.append (separator+names.get(i)+" "+type);
		    if (type.equals("double precision")) {
			colType[i] = 0;
		    } else if (type.equals("int")) {
			colType[i] = 1;
		    } else {
			colType[i] = 2;
		    }
		    separator = ",";
		}
		create.append(",_unique_id  int)");
		String createSQL = new String(create);
		
		try {
		    Statement st =  db.createStatement();
	            boolean stat = st.execute(createSQL);
		} catch (SQLException e) {
		    throw new Error("Error creating table:"+e);
		}
		
		
	        String sql = "insert into "+tabName+" values(?";
	        for (int i=1; i<=fieldCount; i += 1) {  // Extra for _unique_id
	            sql += ",?";
	        }
	        sql += ")";
		try {
	            updater = db.prepareStatement(sql);
		   
		} catch (SQLException e) {
		    throw new Error("Error preparing ingest:"+e);
		}
		
	    } else if (qName.equals("TD")) {
		active = true;
		buf = new StringBuffer();
	    }
        }
    
	/** This is called at the end of every elements.
	 *  If we are at the end of a RESOURCE, TABLEDATA or VOTABLE,
	 *  then we are done.  If there was data we'll see the end
	 *  of the TABLEDATA, but we also need to handle the cases
	 *  where there was no data.
	 *  When we finish a TR element, we're done a row and can add it to
	 *  the database table.
	 *  When we finish a TD we can parse the value of the element and
	 *  associate it with the appropriate column for the next time we
	 *  update the table.
	 */
        public void endElement(String uri, String localName, String qName) {
	    if (qName.equals("RESOURCE") || qName.equals("TABLEDATA") || qName.equals("VOTABLE")) {
		// We're done.  Throw an error to terminate processing.
		throw new BreakError("Normal termination of scan");
	    } else if (qName.equals("TR")) {
		try {
		    updater.setInt(fieldCount+1, currRow);
	            int stat = updater.executeUpdate();
		    currRow += 1;
	            if (stat != 1) {
	                throw new Error("Error updating row!  Status="+stat);
	            }
		} catch (SQLException e) {
		    throw new Error("SQL exception: "+e);
		}
		currCol = 0;
		// Finished a row...  Do the insert.
	    } else if (qName.equals("TD")) {
	      try {
		active = false;
		String s = new String(buf).trim();
		
		// Treat empty strings as nulls.
		if (s.length() > 0) {
		    if (colType[currCol] == 2) {
			if (s.length() > SkyNode.MaxString) {
			    s = s.substring(0,SkyNode.MaxString);
			}
			updater.setString(currCol+1, s);
			
		    } else if (colType[currCol] == 1) {
			updater.setInt(currCol+1, Integer.parseInt(s));
			
		    } else if (colType[currCol] == 0) {
			updater.setDouble(currCol+1, Double.parseDouble(s));
		    }
		} else {
		    if (colType[currCol] == 2) {
		        updater.setNull(currCol+1, java.sql.Types.VARCHAR);
		    } else if (colType[currCol] == 1) {
		        updater.setNull(currCol+1, java.sql.Types.INTEGER);
		    } else if (colType[currCol] == 0) {
		        updater.setNull(currCol+1, java.sql.Types.DOUBLE);
		    }
		}
	      } catch (SQLException e) {
		  throw new Error("SQLException:"+e);
	      }
	      currCol += 1;
	    }
	}
	
	/** This just accumulates the values inside elements.  In principal
	 *  a value may be broken up -- but in practice we only worry about
	 *  this for the TD elements and we expect them to come all at once.
	 *  But this isn't guaranteed.
	 */
        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
    }
	
    
    /** Ingest a VOTable from another machine.
     *  @param input   The XML input stream from the remote machine.
     *                 This is assumed to be a VOTable.
     */
    public void ingest(InputStream input) throws Exception {
	try {
	    SAXParser sp    = SAXParserFactory.newInstance().newSAXParser();
	    sp.parse(new InputSource(input), new IngesterCallBack());
	    input.close();
	} catch (BreakError e) {
	    // This is used to terminate the scan.  Other
	    // exceptions are translated to errors.
	}
    }
}
