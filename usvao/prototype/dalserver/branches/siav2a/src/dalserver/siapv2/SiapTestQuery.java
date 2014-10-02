/*
 * SiapTestQuery.java
 * $ID*
 */

package dalserver.siapv2;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.RangeList;
import dalserver.TableInfo;
import dalserver.siapv2.SiapParamSet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * SiapTestQuery simulates a SIA database query class, implementing the
 * null query, which returns only standard SIA / ImageDM metadata.
 *
 * @version	2.0, 28-Aug-2013
 * @author	Doug Tody
 */
public class SiapTestQuery {
    /** Connection to the remote DBMS. */
    private Connection conn;

    /** Constructor to generate a new DBMS query object, providing
     * the functionality to query a remote DBMS-hosted catalog.
     */
    public SiapTestQuery(String jdbcDriver) throws DalServerException {
	try {
	    conn = null;
	} catch (Exception ex) {
	    throw new DalServerException(ex.getMessage());
	}
    }


    /**
     * Connect to the remote database.
     *
     * @param	url		JDBC URL of the remote DBMS.
     * @param	database	Database name within remote DBMS.
     * @param	username	User name for database login.
     * @param	password	User password for database login.
     */
    public void connect(String url, String database, String username,
	String password) throws DalServerException {

	try {
	    conn = null;
	} catch (Exception ex) {
	    conn = null;
	    throw new DalServerException(ex.getMessage());
	}
    }


    /**
     * Disconnect from the remote database.
     */
    public void disconnect() {
	if (conn != null) {
	    try {
		conn.close();
	    } catch (SQLException ex) {
		;
	    }
	}
    }


    /**
     * Add fields to the request response table corresponding to the
     * defined fields of the SIAV2 metadata DBMS table for the current
     * service.  In principle the SIAV2 metadata table could contain
     * dozens or hundreds of data model attributes; an actual instance of
     * the metadata table will in general be much narrower.  The DM fields
     * that are defined in a table instance will always include a
     * mandatory core set of fields (some of which could have null
     * values), and may also include selected fields from the full data
     * model.  [We should extend this scheme to support custom
     * service-defined metadata as well].
     *
     * @param	params		The SIAP service input parameters.
     *
     * @param	response	The request response object.
     */
    public void addFields(SiapParamSet params, RequestResponse response)
	throws DalServerException {

	// Step through the SIAPV2 keyword list and output all fields 
	// flagged as mandatory or recommended.  In a real service the
	// DBMS would be queried instead to determine what DM or other
	// fields are used in the actual data service.

	SiapKeywordFactory factory = new SiapKeywordFactory();
	factory.addFields(response, "mq");
    }


    /**
     * Dummy DBMS SIAV2 metadata query, which never finds any data (hence
     * it is a null query).  The method signature matches that of a real
     * SIAV2 DBMS query.
     *
     * @param	params		The SIAP service input parameters.
     *
     * @param	response	The request response object.
     */
    public void query(SiapParamSet params, RequestResponse response)
	throws DalServerException {

	String query = "Dummy SIAV2 SQL query";
	boolean error = false;  // in case we add more logic later

	if (error)
	    throw new DalServerException("dummy error message");
    }
}
