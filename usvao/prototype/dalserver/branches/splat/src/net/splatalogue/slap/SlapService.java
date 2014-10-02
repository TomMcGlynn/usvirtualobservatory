/*
 * SlapService.java
 * $ID*
 */

package net.splatalogue.slap;

import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.ParamType;
import dalserver.ParamLevel;
import dalserver.TableInfo;
import dalserver.KeywordFactory;
import dalserver.DalServerException;
import dalserver.InvalidDateException;
import dalserver.conf.KeywordConfig;
import dalserver.slap.SlapParamSet;
import dalserver.slap.SlapKeywordFactory;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
// import java.net.*;
import java.util.Map;

/**
 * A SLAP service interface for accessing a Splatalogue database.  
 * 
 * @version	1.0, 3 Dec 2009
 * @author	Ray Plante
 */
public class SlapService extends dalserver.slap.SlapService {

    SlapDbFactory fact = null;
    boolean verbose = false;

    /**
     * Create a new local SLAP service instance.
     *
     * @param params	Input parameter set.
     */
    public SlapService(SlapParamSet params) {
        this(params, false);
    }

    /**
     * Create a new local SLAP service instance.
     *
     * @param params	Input parameter set.
     * @param verbose   if true, print the SQL query to standard error.
     */
    public SlapService(SlapParamSet params, boolean verbose) {
        super(params);
        this.verbose = verbose;

        try {
            // initialize the database interface
            fact = new SlapDbFactory(jdbcDriver, jdbcUrl, dbName, 
                                     dbUser, dbPassword);
        }
        catch (ClassNotFoundException ex) {
            throw new InternalError("Failed to load DB driver: " + 
                                    ex.getMessage());
        }
    }

    /**
     * Process a data query and generate a list of spectral lines
     * matching the query parameters.
     *
     * @param	params	  The fully processed SLAP parameter set representing
     *			  the request to be processed.
     *
     * @param	response  A dalserver request response object to which the
     *			  query response should be written.  Note this is
     *			  not a file, but an object containing the metadata
     *			  to be returned to the client.
     */
    @SuppressWarnings("unchecked")
    public void queryData(SlapParamSet params, RequestResponse response)
        throws DalServerException 
    {
	boolean debug = (params.getParam("DEBUG") != null);
	SlapKeywordFactory slap = null;
        try {
            slap = new dalserver.slap.SlapKeywordFactory(response);
            slap.loadKeywords(slap.confResStream("splat-keywords.xml", 
                                                 getClass()));
        }
        catch (IOException ex) {
            String msg = "Trouble configuring service";
            String dmsg = msg + ": " + ex.getMessage();
            System.err.println(dmsg);
            if (debug) msg = dmsg;
            throw new DalServerException(msg);
        }
        catch (KeywordConfig.FormatException ex) {
            String msg = "Trouble configuring service";
            String dmsg = msg + ": " + ex.getMessage();
            System.err.println(dmsg);
            if (debug) msg = dmsg;
            throw new DalServerException(msg);
        }

	RequestResponse r = response;
	String id, key;

	TableInfo dbNameInfo = new TableInfo("dbName", dbName);
	TableInfo tableNameInfo = new TableInfo("tableName", tableName);

	// Set global metadata.
	r.setDescription("Splatalogue SLAP Service");
	r.setType("results");

	// This indicates the query executed successfully.  If an exception
	// occurs the output we generate here will never be returned anyway,
	// so OK is always appropriate here.

	r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
	r.addInfo(key="dbName", dbNameInfo);
	r.addInfo(key="tableName", tableNameInfo);

        // Echo the query parameters as INFOs in the query response.
        for (Object o : params.entrySet()) {
	    Map.Entry<String,Param> keyVal = (Map.Entry<String,Param>)o;
            Param p = keyVal.getValue();
            if (!p.isSet() || (p.getLevel() == ParamLevel.SERVICE && !debug))
		continue;

	    String value = p.stringValue();
	    if (debug && p.getType().contains(ParamType.RANGELIST))
		value += " (" + p.toString() + ")";

            r.addInfo(id=p.getName(), new TableInfo(id, value));
        }

        // This implementation supports only SLAP.
	if (!serviceClass.equalsIgnoreCase("slap"))
	    throw new DalServerException("Service only supports SLAP");

	// Create the table metadata for a standard SLAP query response.
	// Only the fields for which valid values are to be returned should
	// be defined here.

	// For SLAP V1.0 we define (mostly) only the standard fields here.
	// Additional DAL-V2 dataset metadata can optionally be added, and
	// this metadata defined in the provided SLAP keyword dictionary.

	// *** The following is for SIAP and will need to be largel
	// *** replaced for SLAP.

        r.addField(slap.newField("title"));
        r.addField(slap.newField("catname"));
        r.addField(slap.newField("wavelength"));
        r.addField(slap.newField("frequency"));
        r.addField(slap.newField("molformula"));
        r.addField(slap.newField("moltype"));
        r.addField(slap.newField("recommended"));
        r.addField(slap.newField("QNs"));

        r.addGroup(slap.newGroup("Line"));
        // r.addGroup(slap.newGroup("TimeAxis"));

        // Execute the SLAP query.
        SlapDb db = null;
        try {
            db = fact.connect();
            if (verbose) db.verbose = true;
            db.query(params, response);
        } catch (DalServerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DalServerException(ex.getMessage());
        } finally {
            if (db != null) db.disconnect();
	}

	// Show the number of table rows in the response header.
	r.addInfo(key="TableRows",
	    new TableInfo(key, new Integer(r.size()).toString()));

	// We are done once the information content of the query response
	// is fully specified.  The servlet code will take care of serializing
	// the query response as a VOTable, and anything else required.
    }

    // -------- Testing -----------

    /**
     * Unit test to do a simple query.
     */
    public static void main(String[] args)
	throws DalServerException, InvalidDateException,
               IOException, FileNotFoundException 
    {
        String dburl = "jdbc:mysql://localhost:3306/";
        String dbname = "splat";
        String driver = "com.mysql.jdbc.Driver";
        String user = null;
        String pass = null;

        if (args.length > 0) dbname = args[0];
        if (args.length > 1) user = args[1];
        if (args.length > 2) pass = args[2];

	// Simulate a typical query.
	SlapParamSet params = new SlapParamSet();
        params.addParam(new Param("jdbcUrl", dburl, ParamLevel.SERVICE));
        params.addParam(new Param("dbName", dbname, ParamLevel.SERVICE));
        params.addParam(new Param("jdbcDriver", driver,ParamLevel.SERVICE));
        if (user != null) 
            params.addParam(new Param("dbUser", user, ParamLevel.SERVICE));
        if (pass != null) 
            params.addParam(new Param("dbPassword", pass, ParamLevel.SERVICE));
	params.setValue("WAVELENGTH", "0.00260075/0.00260080");
        params.setValue("CHEMICAL_ELEMENT", "CO");

        // Exercise the SlapService class.
        SlapService service = new SlapService(params, true);

	// Create an initial, empty request response object.
	RequestResponse r = new RequestResponse();

	// Set the request response context for SLAP.
	KeywordFactory slap = null;
        try {
            slap = new dalserver.slap.SlapKeywordFactory(r);
        }
        catch (IOException ex) {
            String dmsg = "Trouble configuring keywords: "+ex.getMessage();
            throw new DalServerException(dmsg);
        }
        catch (KeywordConfig.FormatException ex) {
            String dmsg = "Trouble configuring keywords: "+ex.getMessage();
            throw new DalServerException(dmsg);
        }   

	// Perform the query. 
	service.queryData(params, r);

	// Write out the VOTable to a file.
	OutputStream out = new FileOutputStream("_output.vot");
	r.writeVOTable(out);
	out.close();
    }
}
