package net.ivoa.skynode;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * This class provides the low level connection
 * to the underlying database using the JDBC methods.
 *
 */
public class DBQuery {
    
    private Connection       db;
    private DatabaseMetaData dbmd;
    private ResultSet        result;
    private static boolean   first = true;
    private String	     tempTableMode;
    
    
    /** Get a new connection to the database */
    public DBQuery() throws Exception {
	if (first) {
	    Class.forName(SkyNode.JDBCDriver);
	    first = false;
	}
        db = DriverManager.getConnection (SkyNode.jdbcURL, SkyNode.jdbcName, SkyNode.jdbcPwd);
    }
    
    public Connection getConnection() {
	return db;
    }
    
    /** Get an object that can be used to extract metadata from the database */
    public DatabaseMetaData getMetadata() throws Exception {
	if (dbmd == null) {
	    dbmd = db.getMetaData();
	}
	return dbmd;
    }
    
    /** Initiate a query of the database */
    public void query(String sql) throws Exception {
        Statement st    = db.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                                             ResultSet.CONCUR_READ_ONLY);
        result          = st.executeQuery(sql);
    }
    
    /** Return the result set from the previous query. */
    public ResultSet getResults() {
	return result;
    }
	   
    /** Close the connection */
    public void close() throws Exception {
	if (db != null) {
	    db.close();
	}
    }
    
    /**
     * Determine the temporary table creation mode.
     * There are at least two different ways to create temporary tables.
     * create temporary table xxx() or
     * create table #xxx()  where the '#' is the first character of the table
     * name.  This function returns either "temporary" or the prefix
     * required for temporary file names.
     * In the current implementation, the first invocation tries to
     * create a temporary table using the "create temporary ..." syntax.
     * If this succeeds "temporary" is returned for all calls.  Otherwise
     * "#" is returned.
     */
    public String getTempTableMode() {
	
	if (tempTableMode == null) {
	    
	    tempTableMode = "#";
	    
	    try {
		Statement st =  db.createStatement();
	        boolean stat = st.execute("create temporary table junkjunk(int a)");
		tempTableMode = "temporary";
		st = db.createStatement();
		stat = st.execute("drop temporary table junkjunk");
	    } catch (SQLException e) {
		// Just ignore exceptions.  
	    }
	}
	return tempTableMode;
    }
}
