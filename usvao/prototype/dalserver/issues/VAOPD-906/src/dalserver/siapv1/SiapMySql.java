/*
 * SiapMySql.java
 * $ID*
 */

package dalserver.siapv1;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.RangeList;
import dalserver.TableInfo;
import dalserver.siapv1.SiapParamSet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * SiapMySql provides a simple image access (SIAP) capability for any
 * image collection which can be described via image metadata stored in
 * a MySQL database.
 *
 * @version	1.0, 27-Aug-2007
 * @author	Doug Tody
 */
public class SiapMySql {
    /** Connection to the remote DBMS. */
    private Connection conn;

    /** Constructor to generate a new MySql query object, providing
     * the functionality to query a remote MySQL-hosted catalog.
     */
    public SiapMySql(String jdbcDriver) throws DalServerException {
	try {
	    Class.forName(jdbcDriver).newInstance();
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
	    conn = DriverManager.getConnection(url+database,
		username, password);
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
     * Query the remote metadata table, writing results as a SIAP query
     * response to the output request response object.  The response object
     * can later be serialized and returned in various output formats.
     *
     * @param	params		The SIAP service input parameters.
     *
     * @param	response	The request response object.
     */
    public void query(SiapParamSet params, RequestResponse response)
	throws DalServerException {

	String tableName=null;
	boolean positional = true, allSky = false;
	String raColumn="ra", decColumn="decl";
	double ra=0, dec=0, ra_sr=0.1, dec_sr=0.1;
	Param p;

	// Get the name of the table to be queried.
	tableName = params.getValue("tableName");
	if (tableName == null)
	    throw new DalServerException("Table name not specified.");

        // SPATIAL Coverage.  If SIZE is omitted, find anything which includes
        // the specified position, otherwise find anything which overlaps.

        if ((p = params.getParam("POS")) != null && p.isSet()) {
            RangeList r = p.rangeListValue();
            ra = r.doubleValue(0);
            dec = r.doubleValue(1);

            if ((p = params.getParam("SIZE")) != null) {
		r = p.rangeListValue();
		ra_sr = r.doubleValue(0) / 2.0;
		try {
		    dec_sr = r.doubleValue(1) / 2.0;
		} catch (DalServerException ex) {
		    dec_sr = ra_sr;
		}
	    }
        } else
	    positional = false;

        // Check for SR=180 degrees (entire sky).
	allSky = (Math.abs(ra_sr - 180.0) < 0.000001);

	// If no search region specified the entire table will be returned.
	if (allSky)
	    positional = false;

        // What formats do we return?
        boolean retFITS=true, retGraphic=true;
        String formats = null;
        int nFormats = 2;

	formats = params.getValue("FORMAT");
	if (formats == null)
	    formats = "fits";

        if (formats != null) {
            formats = formats.toLowerCase();
            retFITS = retGraphic = false;
            nFormats = 0;

            if (formats.equals("all")) {
		retFITS = retGraphic = true;
                nFormats = 2;
            } else {
                if (formats.contains("fits")) {
                    retFITS = true;
                    nFormats++;
                }
                if (formats.contains("graphic")) {
                    retGraphic = true;
                    nFormats++; 
                }
            }
        }

	// Perform the data query and write rows to the output table.
	ResultSetMetaData md;
	ResultSet rs;
	Statement st;
	String key;

	try {
	    String query = "SELECT * FROM " + tableName;
	    if (positional) {
		double dec1 = Math.max(-90.0, Math.min(90.0, dec - dec_sr));
		double dec2 = Math.max(-90.0, Math.min(90.0, dec + dec_sr));

		query += (" WHERE " + decColumn + " BETWEEN " +
		    dec1 + " AND " + dec2);
	    }

	    // Execute the query.
	    response.addInfo(key="SQL_QUERY", new TableInfo(key, query));
	    st = conn.createStatement();
	    rs = st.executeQuery(query);
	    md = rs.getMetaData();

	    // Now check all the returned table rows to see if in ROI.
	    while (rs.next()) { 
	        double pos_ra=ra, pos_dec=dec;
		double scale1, scale2, ra_dist, dec_dist;
		double obj_ra, obj_dec, dx;
		long naxis1, naxis2;
		double ra1, ra2;

		if (positional) {
		    obj_ra = rs.getDouble(raColumn);
		    obj_dec = rs.getDouble(decColumn);
		    scale1 = rs.getDouble("scale1");
		    scale2 = rs.getDouble("scale2");
		    naxis1 = rs.getLong("naxis1");
		    naxis2 = rs.getLong("naxis2");

		    // For SIAP compute the SR separately for RA and DEC.
		    double ra_offset = ra_sr + (naxis1 * scale1) / 2.0;
		    double dec_offset = dec_sr + (naxis2 * scale2) / 2.0;

                    // Shift the RA coords to zero=180 to avoid wrap when
                    // computing the OBJ_RA to RA distance.

                    dx = pos_ra - 180.0;  pos_ra -= dx;
                    obj_ra -= dx;
                    if (obj_ra < 0)
                        obj_ra += 360.0;
                    if (obj_ra >= 360)
                        obj_ra -= 360.0;

                    ra_dist = Math.abs(obj_ra - pos_ra);
                    dec_dist = Math.abs(obj_dec - pos_dec);

		    if (Math.abs(obj_ra - pos_ra) > ra_offset)
			continue;
		    if (Math.abs(obj_dec - pos_dec) > dec_offset)
			continue;
		}

		// Output a record for each matched image format.
		String imageFormat = rs.getString("format").toLowerCase();
		String assocType = "MultiFormat";
		String assocId = null;
		int nAssoc = 0;

		if (retFITS) {
		    if (imageFormat.contains("fits")) {
			assocId = assocType + "." +
			     new Integer(nAssoc++).toString(); 
			response.addRow();
			this.setMetadata(params, rs, response, "image/fits");
		    }
		}

		if (retGraphic) {
		    String outFormat = null;
		    if (imageFormat.contains("gif")) {
			outFormat = "image/gif";
		    } else if (imageFormat.contains("jpg") ||
			    imageFormat.contains("jpeg")) {
			outFormat = "image/jpeg";
		    } else if (imageFormat.contains("png")) {
			outFormat = "image/png";
		    }

		    if (outFormat != null) {
			assocId = assocType + "." +
			     new Integer(nAssoc++).toString(); 
			response.addRow();
			this.setMetadata(params, rs, response, outFormat);
		    }
		}
	    }

	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}
    }

    /**
     * Set the content of one query response record.
     *
     * @param	params		SIAP parameter set
     * @param	rs		SQL query result set
     * @param	r		RequestResponse object
     * @param	format		MIME type of output image
     */
    private void setMetadata(SiapParamSet params, ResultSet rs,
	RequestResponse r, String format) throws DalServerException {

	// Access metadata.
	String datasetId = params.getValue("dataDirURL");
	if (!datasetId.endsWith("/"))
	    datasetId += "/";
	try {
	    datasetId += rs.getString("fpath");
	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	String runId = params.getValue("RunID");
	String serviceName = params.getValue("serviceName");
	String baseUrl = params.getValue("baseUrl");
	if (!baseUrl.endsWith("/"))
	    baseUrl += "/";

	try {
	    String acRef = baseUrl + serviceName + "?" +
		"REQUEST=getData" + "&" +
		"FORMAT=" + format + "&" +
		"PubDID=" + URLEncoder.encode(datasetId, "UTF-8");
	    if (runId != null)
		acRef += "&RunID=" + runId;
	    r.setValue("AcRef", acRef);

	} catch (UnsupportedEncodingException ex) {
	    throw new DalServerException("URL encoding failed");
	}

	r.setValue("Format", format);
	r.setValue("DatasetSize", getColumn(rs, "fsize"));
	String sval;

	// General dataset metadata.
	r.setValue("DateObs", getColumn(rs, "dateobs"));

	// The following are mandatory.
	try {
	    String s1 = rs.getString("ra");
	    String s2 = rs.getString("decl");

	    r.setValue("RA", rs.getString("ra"));
	    r.setValue("DEC", rs.getString("decl"));

	    sval = rs.getString("naxes");
	    int naxes = new Integer(sval);
	    r.setValue("Naxes", naxes);

	    sval = "";
	    for (int i=1;  i <= naxes;  i++) {
		if (i > 1) sval += " ";
		sval += rs.getString("naxis" + i);
	    }
	    r.setValue("Naxis", sval);

	    sval = "";
	    for (int i=1;  i <= naxes;  i++) {
		try {
		    if (i > 1) sval += " ";
		    sval += rs.getString("scale" + i);
		} catch (SQLException ex) {
		    sval = null;
		    break;
		}
	    }
	    r.setValue("Scale", sval);

	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Dataset identification metadata.
	r.setValue("Title", getColumn(rs, "title"));
	r.setValue("Instrument", getColumn(rs, "instr"));

	// WCS metadata.
	r.setValue("CoordRefFrame", getColumn(rs, "cframe"));
	r.setValue("CoordEquinox", getColumn(rs, "equinox"));
	r.setValue("CoordProjection", getColumn(rs, "proj"));
	r.setValue("CoordRefPixel", getColumn(rs, "crpix"));
	r.setValue("CoordRefValue", getColumn(rs, "crval"));
	r.setValue("CoordCDMatrix", getColumn(rs, "cd"));
    }

    /**
     * Get a SQL ResultSet value, returning null if it is not found.
     *
     * @param	rs		Result set.
     * @param	columnName	The name of the desired column.
     */
    private String getColumn(ResultSet rs, String columnName) {
	String sval;
	try {
	    sval = rs.getString(columnName);
	} catch (SQLException ex) {
	    sval = null;
	}

	return (sval);
    }


    /**
     * Test the SIAP MySQL interface.
     *
     * <pre>
     *   ingest [csv-file]	Turn a CSV version of the SIAP data model
     *				into a SiapData class which contains raw data
     *				defining the data model.
     *
     *   doc [type]		Generate an HTML version of the SIAP keyword
     * 				dictionary.
     *
     *   table [type]		Generate Java code to create the indicated
     *				keywords in a RequestResponse object.
     * </pre>
     */
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("ingest")) {
	    // Read a CSV version of the SIAP/Spectrum data models, and use
	    // this to generate code for a compiled SiapData class which
	    // encodes the raw keyword data.

	    String inFile = (args.length > 1) ?
		args[1] : "lib/messier.csv";
	    String outFile = (args.length > 2) ?
		args[2] : "src/dalserver/SiapMessierData.java";

	    BufferedReader in = null;
	    PrintWriter out = null;

	    try {
		in = new BufferedReader(new FileReader(inFile));
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot open file " + "["+inFile+"]");
	    }

	    try {
		out = new PrintWriter(outFile);
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot open file " + "["+outFile+"]");
		System.exit(1);
	    }

	    try {
		out.println("package dalserver;");
		out.println("/**");
		out.println(" * Raw data for Messier catalog" +
		    " (this class is autogenerated).");
		out.println(" * See {@link dalserver.SiapMessier}.");
		out.println(" */");

		out.println("public class SiapMessierData {");
		out.println("  /** Messier catalog. */");
		out.println("  public static final String[] data = {");
		for (String line;  (line = in.readLine()) != null;  ) {
		    out.println("  \"" + line + "\",");
		}
		out.println("  };");
		out.println("}");

		out.close();
		in.close();

	    } catch (IOException ex) {
		System.out.println(ex.getMessage());
	    }
	}
    }
}
