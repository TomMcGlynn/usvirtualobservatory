/*
 * SiapMySql.java
 * $ID*
 */

package dalserver.siapv2;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.DateParser;
import dalserver.RangeList;
import dalserver.TableInfo;
import dalserver.siapv2.SiapParamSet;
import dalserver.siapv2.SiapKeywordFactory;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * SiapMySql provides a simple image access (SIAP Version 2) capability
 * for any image collection which can be described via image metadata
 * stored in a MySQL database.
 *
 * @version	2.0, 28-Aug-2013
 * @author	Doug Tody
 *
 * This class is growing rather large; much of the functionality herein
 * is generic and should be moved to a general query class, driving 
 * much smaller DBMS-specific query classes.
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
     * Add fields to the request response table corresponding to the
     * defined fields of the SIAV2 metadata DBMS table for the current
     * service.  In principle the SIAV2 metadata table could contain
     * dozens or hundreds of data model attributes; an actual instance of
     * the metadata table will in general be much narrower.  The DM fields
     * which are defined in a table instance will always include a
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

	SiapKeywordFactory siap = new SiapKeywordFactory();
	RequestResponse r = response;

	// For the prototype version of this code we hard-wire the DM
	// fields to be output, and omit support for custom
	// provider-defined metadata fields. The generalized version of this
	// needs to query the DBMS table for the service instance to
	// determine what standard ImageDM or custom metadata is defined
	// for the service instance.  The fields shown here, some of which
	// are commented out, are from the pre-generated lib/siapv2-table.txt,
	// which includes all mandatory and recommended DM QR fields.

	// Query Metadata
	// r.addGroup(siap.newGroup("Query"));
	// r.addField(siap.newField("Score"));
	// r.addParam(siap.newParam("Token", "UNSET"));

	// Association Metadata
	//
	// For SIA we omit the Multiformat association for now, since
	// initially we support only FITS as an output image format.

	// r.addGroup(siap.newGroup("Association"));
	// r.addParam(siap.newParam("AssocType", "UNSET"));
	// r.addField(siap.newField("AssocID"));
	// r.addField(siap.newField("AssocKey"));

	// Access Metadata
	r.addGroup(siap.newGroup("Access"));
	r.addField(siap.newField("AcRef"));
	r.addField(siap.newField("Format"));
	r.addField(siap.newField("EstSize"));

	// General Dataset Metadata
	r.addGroup(siap.newGroup("Dataset"));
	r.addParam(siap.newParam("DataModel", "Image-2.0"));
	r.addParam(siap.newParam("DataModelPrefix", "im"));
	r.addParam(siap.newParam("DatasetType", "Image"));
	// r.addField(siap.newField("DatasetSubtype"));
	r.addField(siap.newField("DatasetCalibLevel"));
	r.addField(siap.newField("DataLength"));

	// Image-specific Dataset metadata
	r.addGroup(siap.newGroup("Dataset.Image"));
	r.addField(siap.newField("Nsubarrays"));
	r.addField(siap.newField("Naxes"));
	r.addField(siap.newField("Naxis"));
	r.addField(siap.newField("Pixtype"));
	r.addField(siap.newField("WCSAxes"));
	r.addField(siap.newField("DataRef"));

	// Dataset Identification Metadata
	r.addGroup(siap.newGroup("DataID"));
	r.addField(siap.newField("Title"));
	r.addField(siap.newField("Creator"));
	r.addField(siap.newField("Collection"));
	r.addField(siap.newField("CreationType"));

	// Provenance Metadata
	// r.addGroup(siap.newGroup("Provenance"));
	// r.addField(siap.newField("Instrument"));
	// r.addField(siap.newField("Bandpass"));

	// Curation Metadata
	r.addGroup(siap.newGroup("Curation"));
	r.addField(siap.newField("PublisherDID"));

	// Target Metadata
	// r.addGroup(siap.newGroup("Target"));
	// Derived Metadata
	// r.addGroup(siap.newGroup("Derived"));

	// Coordinate System Metadata
	//
	// Most of this can be omitted since the query response
	// fixes or defines defaults for the coordinate frames
	// used in the response.

	// r.addGroup(siap.newGroup("CoordSys"));
	// Spatial frame metadata
	// r.addGroup(siap.newGroup("CoordSys.SpaceFrame"));
	// Time frame metadata
	// r.addGroup(siap.newGroup("CoordSys.TimeFrame"));
	// Spectral frame metadata
	// r.addGroup(siap.newGroup("CoordSys.SpectralFrame"));
	// Redshift frame metadata
	// r.addGroup(siap.newGroup("CoordSys.RedshiftFrame"));
	// Flux (observable) frame metadata
	// r.addGroup(siap.newGroup("CoordSys.FluxFrame"));

	// Spatial Axis Characterization
	r.addGroup(siap.newGroup("Char.SpatialAxis"));
	r.addField(siap.newField("SpatialLocation"));
	// r.addField(siap.newField("SpatialExtent"));
	r.addField(siap.newField("SpatialLoLimit"));
	r.addField(siap.newField("SpatialHiLimit"));
	// r.addField(siap.newField("SpatialFillFactor"));
	r.addField(siap.newField("SpatialCalibration"));
	r.addField(siap.newField("SpatialResolution"));

	// Spectral Axis Characterization
	r.addGroup(siap.newGroup("Char.SpectralAxis"));
	// r.addField(siap.newField("SpectralAxisUCD"));
	// r.addField(siap.newField("SpectralAxisUnit"));
	r.addField(siap.newField("SpectralStart"));
	r.addField(siap.newField("SpectralStop"));
	// r.addField(siap.newField("SpectralFillFactor"));
	// r.addField(siap.newField("SpectralCalibration"));
	r.addField(siap.newField("SpectralResolution"));
	r.addField(siap.newField("SpectralResPower"));

	// Time Axis Characterization
	r.addGroup(siap.newGroup("Char.TimeAxis"));
	r.addField(siap.newField("TimeStart"));
	r.addField(siap.newField("TimeStop"));
	// r.addField(siap.newField("TimeFillFactor"));
	// r.addField(siap.newField("TimeCalibration"));
	// r.addField(siap.newField("TimeResolution"));

	// Flux Axis Characterization
	r.addGroup(siap.newGroup("Char.FluxAxis"));
	r.addField(siap.newField("FluxAxisUCD"));
	r.addField(siap.newField("FluxAxisUnit"));
	// r.addField(siap.newField("FluxCalibration"));

	// Polarization Axis Characterization
	r.addGroup(siap.newGroup("Char.PolAxis"));
	r.addField(siap.newField("PolAxisUCD"));
	r.addField(siap.newField("PolAxisEnum"));
    }


    /**
     * Query the metadata for the dataset identified by the given
     * PubDID, and return the named metadata attribute.
     *
     * @param	publisherDid	The PubDID of the dataset.
     */
    public String queryDataset(String publisherDid, String attribute)
	throws DalServerException {

	String tableName, sval, id;
	int taboff, idoff;

	// Get the tableName and dataset ID.
	taboff = publisherDid.lastIndexOf("#") + 1;
	idoff = publisherDid.lastIndexOf(":") + 1;
	tableName = publisherDid.substring(taboff, idoff - 1);
	id = publisherDid.substring(idoff);

	// Compose the MySQL query.
	String query = "SELECT " + attribute + " FROM " + tableName +
	    " WHERE (id = " + id + ")";

	// Perform the data query and write rows to the output table.
	try {
	    // Execute the query.
	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery(query);

	    // Walk through the resultset and output each row.
	    if (rs.next())
		return (getColumn(rs, attribute));

	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	return (null);
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

	String tableName="siav2model";
	String sval; Param p;
	int maxrec = response.maxrec();

	// Get the name of the SIAV2 DBMS table to be queried.
	sval = params.getValue("tableName");
	if (sval != null)
	    tableName = sval;


        // SPATIAL Coverage.
	// ------------------
	// If SIZE is omitted, find anything which includes
        // the specified position, otherwise find anything which overlaps.

	boolean spatial_constraint = true, allSky = false;
	String s1Column="spatiallocation1", s2Column="spatiallocation2";
	double ra=0, dec=0, ra_sr=0.1, dec_sr=0.1;

        if ((p = params.getParam("POS")) != null && p.isSet()) {
            RangeList r = p.rangeListValue();
            ra = r.doubleValue(0);
            dec = r.doubleValue(1);

            if ((p = params.getParam("SIZE")) != null && p.isSet()) {
		r = p.rangeListValue();
		ra_sr = r.doubleValue(0) / 2.0;
		try {
		    dec_sr = r.doubleValue(1) / 2.0;
		} catch (DalServerException ex) {
		    dec_sr = ra_sr;
		}
	    }
        } else
	    spatial_constraint = false;

        // Check for SR=180 degrees (entire sky).
	allSky = (Math.abs(ra_sr - 180.0) < 0.000001);

	// If no search region specified the spatial constraint is 
	// disabled.
	if (allSky)
	    spatial_constraint = false;


        // SPECTRAL Coverage.
	// ------------------

	boolean spectral_constraint = true;
	String e1Column="spectralstart", e2Column="spectralstop";
	double e1val=0, e2val=0;

        if ((p = params.getParam("BAND")) != null && p.isSet()) {
            RangeList r = p.rangeListValue();
            e1val = r.getRange(0).doubleValue1();
            e2val = r.getRange(0).doubleValue2();
        } else
	    spectral_constraint = false;


        // TIME Coverage.
	// ------------------

	boolean time_constraint = true;
	String t1Column="timestart", t2Column="timestop";
	double t1val=0, t2val=0;

        if ((p = params.getParam("TIME")) != null && p.isSet()) {
            RangeList r = p.rangeListValue();
	    java.util.Date d1val, d2val;
	    DateParser dp = new DateParser();

            d1val = r.getRange(0).dateValue1();
            d2val = r.getRange(0).dateValue2();
	    t1val = dp.getMJD(d1val);
	    t2val = dp.getMJD(d2val);
        } else
	    time_constraint = false;


        // POLARIZATION Coverage.
	// -----------------------

	boolean pol_constraint = true, polAny = false;
	String polColumn="polaxisenum";
	String pol1=null, pol2=null, pol3=null, pol4=null;

        if ((p = params.getParam("POL")) != null && p.isSet()) {
	    sval = p.stringValue();
	    if (sval != null && sval.equalsIgnoreCase("any")) {
		polAny = true;
	    } else if (sval != null && sval.equalsIgnoreCase("stokes")) {
		pol1 = "I";
		pol2 = "Q";
		pol3 = "U";
		pol4 = "V";
	    } else if (sval != null) {
		RangeList r = p.rangeListValue();
		pol1 = (r.length() > 0) ?  r.stringValue(0) : null;
		pol2 = (r.length() > 1) ?  r.stringValue(3) : null;
		pol3 = (r.length() > 2) ?  r.stringValue(2) : null;
		pol4 = (r.length() > 3) ?  r.stringValue(3) : null;
	    } else
		pol_constraint = false;
        } else
	    pol_constraint = false;



        // OUTPUT FORMATS.  What formats do we return?
	//---------------------
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

	// Most of the optional physical constraint parameters are not yet
	// implemented, e.g., SPECRES, SPATRES, TIMERES, etc.

	// Most of the simpler optional query parameters are handled directly
	// below as we compose the MySQL query.  


	// Compose the MySQL query.
	//-------------------------------------
	String query = "SELECT * FROM " + tableName + " WHERE ";
	boolean additional_term = false;

	// Apply the spatial constraint if we have one.
	if (spatial_constraint) {
	    if (additional_term)
		query += (" AND ");

	    // This needs to be converted to a 2D radial test for SIAV2.
	    // We use the old CAR/box test for now.  Note this need
	    // not be exact, as more precise refinement is done in the
	    // second pass below (this old looks to have a problem at
	    // the 0/360 boundary for RA, but lets' ignore this for the
	    // moment until the code is redone.)
 
	    double ra1 = Math.max(0.0, Math.min(360.0, ra - ra_sr));
	    double ra2 = Math.max(0.0, Math.min(360.0, ra + ra_sr));
	    double dec1 = Math.max(-90.0, Math.min(90.0, dec - dec_sr));
	    double dec2 = Math.max(-90.0, Math.min(90.0, dec + dec_sr));

	    // This needs to be generalized to allow for a spatial position
	    // specified as NULL, e.g, for theory data.

	    query += ("(" +
		s1Column + " BETWEEN " + ra1 + " AND " + ra2 + " AND " +
		s2Column + " BETWEEN " + dec1 + " AND " + dec2 + ")");

	    additional_term = true;
	}

	// If we have a BAND term, apply the constraint, or if the metadata
	// is null, ignore the constraint (i.e., constraint term evaluates
	// to TRUE).  [TO DO - add support for multiple ranges]

	if (spectral_constraint) {
	    if (additional_term)
		query += (" AND ");

	    query += ("IFNULL(" + e1Column + " <= " + e2val + ", true)");
	    query += (" AND ");
	    query += ("IFNULL(" + e2Column + " >= " + e1val + ", true)");

	    additional_term = true;
	}

	// If we have a TIME term, apply the constraint, or if the metadata
	// is null, ignore the constraint.
	// [TO DO - add support for multiple ranges]

	if (time_constraint) {
	    if (additional_term)
		query += (" AND ");

	    query += ("IFNULL(" + t1Column + " <= " + t2val + ", true)");
	    query += (" AND ");
	    query += ("IFNULL(" + t2Column + " >= " + t1val + ", true)");

	    additional_term = true;
	}

	if (pol_constraint) {
	    // The polarization metadata needs to be refined before we
	    // can fully implement tests for the individual polarization
	    // states.  Just test for any form of polarization for now.

	    if (polAny) {
		if (additional_term)
		    query += (" AND ");

		query += ("(" + polColumn + " IS NOT NULL)");
		additional_term = true;
	    }
	}

	// Maximum number of output records (0 for a metadata only response).
        if ((p = params.getParam("MAXREC")) != null && p.isSet()) {
	    maxrec = p.intValue();
	    response.setMaxrec(maxrec);
	}

	// Publisher Dataset Identifier.
        if ((p = params.getParam("PubDID")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");

	    // Extract the ID of the dataset within the index table.
	    sval = p.stringValue();
	    int offset = sval.lastIndexOf(":");
	    String datasetID = sval.substring(offset+1);
	    query += ("(id = " + datasetID + ")");

	    additional_term = true;
	}

	// Find only data from the specified collection or collections.
        if ((p = params.getParam("Collection")) != null && p.isSet()) {
	    sval = p.stringValue();

	    // The reserved value "all" searches all collections.
	    if (!sval.equalsIgnoreCase("all")) {
		String collections[] = sval.split(",");
		boolean firstone = true;

		if (collections.length > 0) {
		    if (additional_term)
			query += (" AND ");
		    query += ("(");

		    for (String collection : collections) {
			if (!firstone)
			    query += (" || ");
			query += ("(collection like '%" + collection + "%')");
			firstone = false;
		    }

		    query += (")");
		    additional_term = true;
		}
	    }
	}

	// Dataset has an astrometric calibration (i.e., WCS).
        if ((p = params.getParam("AstCalib")) != null && p.isSet()) {
	    sval = p.stringValue();
	    if (sval.equalsIgnoreCase("relative") ||
	        sval.equalsIgnoreCase("absolute")) {

		if (additional_term)
		    query += (" AND ");
		query += ("(wcsaxes1 IS NOT NULL)");
		additional_term = true;
	    }
	}

	// Test if dataset is a 2D image or a cube (nD where N >= 3).
        if ((p = params.getParam("type")) != null && p.isSet()) {
	    sval = p.stringValue();
	    boolean image = sval.equalsIgnoreCase("image");
	    boolean cube = sval.equalsIgnoreCase("cube");

	    if (additional_term)
		query += (" AND ");

	    if (image)
		query += ("(naxes = 2)");
	    else if (cube)
		query += ("(naxes >= 3)");

	    additional_term = true;
	}

	// Ensure a valid SQL query if no constraints were defined.
	if (!additional_term)
	    query = "SELECT * FROM " + tableName;


	// Perform the data query and write rows to the output table.
	//-------------------------------------------------------------
	ResultSetMetaData md;
	ResultSet rs;
	Statement st;
	String key;

	try {
	    // Execute the query.
	    String null_query = "SELECT * FROM " + tableName + " WHERE (id = 0)";

	    response.addInfo(key="QUERY", new TableInfo(key, query));
	    st = conn.createStatement();
	    rs = st.executeQuery((maxrec > 0) ? query : null_query);
	    md = rs.getMetaData();

	    // Walk through the resultset and output each row.
	    while (rs.next()) { 
	        double pos_ra=ra, pos_dec=dec;
		double scale1, scale2, ra_dist, dec_dist;
		double obj_ra, obj_dec, dx;
		long naxis1, naxis2;
		double ra1, ra2;

		// Refine the spatial ROI intersect test.  The initial
		// SQL spatial query is crude but fast, and may find images
		// that do not satisfy the spatial constraint.  We do a
		// more rigorous test here as a second pass.

		if (spatial_constraint) {
		    obj_ra = rs.getDouble(s1Column);
		    obj_dec = rs.getDouble(s2Column);

		    // This assumes the spatial axes are 1 and 2; should be
		    // generalized.
		    scale1 = rs.getDouble("pixelresolution1");
		    scale2 = rs.getDouble("pixelresolution2");
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
	String sval = null;

	// Access metadata.
	String authorityID = params.getValue("authorityID");
	String tableName = params.getValue("tableName");
	String datasetID = getColumn(rs, "id");
	if (authorityID == null || datasetID == null)
	    throw new DalServerException("missing authorityID or datasetID");

	// Format the PublisherDID for this dataset.  The format is
	// <IVO-authority>#<internal-id>, where for <internal-id>
	// we use the tablename of the index table, plus the unique
	// dataset ID within the table.  The identity or location of
	// the dataset within the archive is not exposed externally.

	String publisherDID = authorityID;
	if (!publisherDID.endsWith("#"))
	    publisherDID += "#";
	publisherDID += tableName + ":" + datasetID;

	String runId = params.getValue("RunID");
	String serviceName = params.getValue("serviceName");
	String baseUrl = params.getValue("baseUrl");
	if (serviceName == null || baseUrl == null)
	    throw new DalServerException("missing serviceName or baseUrl parameter");

	if (!baseUrl.endsWith("/"))
	    baseUrl += "/";

	try {
	    String acRef = baseUrl + serviceName + "/sync" + "?" +
		"REQUEST=accessData" + "&" +
		"FORMAT=" + format + "&" +
		"PubDID=" + URLEncoder.encode(publisherDID, "UTF-8");
	    if (runId != null)
		acRef += "&RunID=" + runId;
	    r.setValue("AcRef", acRef);

	} catch (UnsupportedEncodingException ex) {
	    throw new DalServerException("Encoding of access reference failed");
	}

	r.setValue("Format", format);
	r.setValue("EstSize", getColumn(rs, "estsize"));

	// General dataset metadata.
	r.setValue("DatasetCalibLevel", getColumn(rs, "datasetcaliblevel"));
	r.setValue("DataLength", getColumn(rs, "datalength"));

	// Image-specific dataset metadata.
	r.setValue("Nsubarrays", getColumn(rs, "nsubarrays"));

	// Compute Naxes and Naxis from the axis lengths.
	String WCSAxes = "";
	String naxis = "";
	int naxes = 0;
	int axlen = 0;

	// Compute image axes descriptive metadata.
	for (int i=1;  i <= 4;  i++) {
	    try {
		axlen = rs.getInt("naxis" + i);
	    } catch (SQLException ex) {
	        axlen = 0;
	    }
	    if (axlen > 1) {
		naxes++;
		if (i > 1)
		    naxis += " ";
		naxis += axlen;
	    }
	    if (axlen > 0) {
		if (i > 1)
		    WCSAxes += " ";
		WCSAxes += getColumn(rs, "wcsaxes" + i);
	    }
	}
	r.setValue("Naxes", naxes);
	r.setValue("Naxis", naxis);
	r.setValue("Pixtype", getColumn(rs, "pixtype"));
	r.setValue("WCSAxes", WCSAxes);

	// Dataset identification metadata.
	r.setValue("Title", getColumn(rs, "title"));
	r.setValue("Creator", getColumn(rs, "creator"));
	r.setValue("Collection", getColumn(rs, "collection"));
	r.setValue("CreationType", getColumn(rs, "creationtype"));

	// Provenance metadata.
	// (Skipped for the moment).

	// Curation metadata.
	r.setValue("PublisherDID", publisherDID);

	// Spatial axis characterization.
	r.setValue("SpatialLocation",
	    getColumn(rs, "spatiallocation1") + " " +
	    getColumn(rs, "spatiallocation2"));
	r.setValue("SpatialLoLimit",
	    getColumn(rs, "spatiallolimit1") + " " +
	    getColumn(rs, "spatiallolimit2"));
	r.setValue("SpatialHiLimit",
	    getColumn(rs, "spatialhilimit1") + " " +
	    getColumn(rs, "spatialhilimit2"));
	r.setValue("SpatialCalibration",
	    (getColumn(rs, "wcsaxes1") != null) ? "absolute" : "none");
	r.setValue("SpatialResolution", getColumn(rs, "spatialresolution1"));

	// Spectral axis characterization.
	r.setValue("SpectralStart", getColumn(rs, "spectralstart"));
	r.setValue("SpectralStop", getColumn(rs, "spectralstop"));
	r.setValue("SpectralResolution", getColumn(rs, "spectralresolution"));
	r.setValue("SpectralResPower", getColumn(rs, "spectralrespower"));

	// Time axis characterization.
	r.setValue("TimeStart", getColumn(rs, "timestart"));
	r.setValue("TimeStop", getColumn(rs, "timestop"));

	// Flux (observable) axis characterization.
	r.setValue("FluxAxisUCD", getColumn(rs, "fluxaxisucd"));
	r.setValue("FluxAxisUnit", getColumn(rs, "fluxaxisunit"));

	// Polarization axis characterization.
	r.setValue("PolAxisUCD", getColumn(rs, "polaxisucd"));
	r.setValue("PolAxisEnum", getColumn(rs, "polaxisenum"));
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
