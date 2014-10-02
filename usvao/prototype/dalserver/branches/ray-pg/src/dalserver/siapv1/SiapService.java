/*
 * SiapService.java
 * $ID*
 */

package dalserver.siapv1;

import dalserver.DalServerException;
import dalserver.DalOverflowException;
import dalserver.RequestResponse;
import dalserver.TableInfo;
import dalserver.Param;
import dalserver.ParamType;
import dalserver.ParamLevel;
import dalserver.InvalidDateException;
import dalserver.KeywordFactory;
import dalserver.conf.KeywordConfig;
import dalserver.siapv1.SiapParamSet;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A generic class which implements the operations for a DALServer SIAP
 * service.  A custom data service would subclass this, replacing the
 * <i>queryData</i> operation with a version which implements the same
 * operation for a specific archive and data collection (the custom code
 * should be placed in src/dataServices).  In most cases the remaining
 * SIAP operations (e.g., <i>getCapabilities</i>) should not need to be
 * subclassed by a data service, except to edit the metadata to be returned.
 * The remaining code required to implement a complete service (excluding
 * that required to generate the data product to be returned) is provided
 * by the {@link dalserver} code.
 *
 * <p>By default this generic SIAP implementation will function as an "echo"
 * service, implementing the SIA protocol but merely echoing back its input
 * parameters and providing a dataless query response including all SIAP
 * metadata.
 *
 * <p>This generic SIAP implementation also provides a limited capability
 * to serve archival image data, providing a DBMS table is provided containing
 * the basic SIAP metadata, supporting positional queries for archival images.
 * Whole images are returned from a storage directory referenced by URL
 * (file or http based).
 *
 * <p>The service implementation at this level should not be protocol
 * specific.  The intention is that the service operations at this level can
 * be used with any distributed computing platform.  Hence we can implement
 * an HTTP GET interface now, and add a SOAP or other interface later,
 * or some other interface which may not even be network based, all sharing
 * the same core service operations.</p>
 */
public class SiapService {
    /**
     * The service name, used to identify resources associated with this
     * service instance.
     */
    protected String serviceName = "siapv1";

    /**
     * The service class assumed by the caller, e.g., if the service
     * implementation supports multiple service protocols such as SCS
     * and TAP.
     */
    protected String serviceClass = "siapv1";

    /** The type of DBMS to be accessed, if any. */
    protected String dbType;

    /** The name of the database to be accessed. */
    protected String dbName;

    /** The name of the table to be accessed (for the SIAP metadata). */
    protected String tableName;

    /** The JDBC URL (endpoint) of the DBMS. */
    protected String jdbcUrl;

    /** The JDBC driver to be used for the connection. */
    protected String jdbcDriver;

    /** The user name to use to login to the DBMS. */
    private String dbUser;

    /** The user password to use to login to the DBMS. */
    private String dbPassword;

    /** Local directory where configuration files are stored. */
    protected String configDir;

    /** The description of the columns we will export in response */
    protected KeywordFactory kwf = null;

    /** Local directory where data files are stored. */
    protected String dataDirURL;

    /** File content (MIME) type of any returned datasets. */
    protected String contentType;


    /**
     * Create a new local SIAP service instance.
     *
     * @param params	Input parameter set.
     */
    public SiapService(SiapParamSet params, KeywordFactory kwfact) {
	if (kwfact != null) kwf = kwfact;
	if (params == null) {
	    this.serviceName = "siapv1";
	    this.serviceClass = "siapv1";
            this.dbType = null;
            this.dbName = null;
            this.tableName = null;
	    this.configDir = "/tmp";
	    this.dataDirURL = "/tmp";
	    this.contentType = "image/fits";
	} else {
	    try {
		this.serviceName = params.getValue("serviceName");
                this.serviceClass = params.getValue("serviceClass");
                this.dbType = params.getValue("dbType");
                this.dbName = params.getValue("dbName");
                this.tableName = params.getValue("tableName");
                this.jdbcUrl = params.getValue("jdbcUrl");
                this.jdbcDriver = params.getValue("jdbcDriver");
                this.dbUser = params.getValue("dbUser");
                this.dbPassword = params.getValue("dbPassword");
		this.configDir = params.getValue("configDir");
		this.dataDirURL = params.getValue("dataDirURL");
		this.contentType = params.getValue("contentType");
	    } catch (DalServerException ex) {
		// Keep defaults.
	    }
	}
    }

    /**
     * Process a data query and generate a list of candidate image
     * datasets matching the query parameters.  The datasets referenced
     * in the query response may be physical datasets, or virtual datasets
     * which will be generated on demand, only if subsequently requested
     * by the client.
     *
     * @param	params	  The fully processed SIAP parameter set representing
     *			  the request to be processed.
     *
     * @param	response  A dalserver request response object to which the
     *			  query response should be written.  Note this is
     *			  not a file, but an object containing the metadata
     *			  to be returned to the client.
     */
    @SuppressWarnings("unchecked")
    public void queryData(SiapParamSet params, RequestResponse response)
	    throws DalServerException, DalOverflowException {

	boolean debug = (params.getParam("DEBUG") != null);
	SiapKeywordFactory siap = new SiapKeywordFactory(response);
	RequestResponse r = response;
	String id, key;

	TableInfo dbNameInfo = new TableInfo("dbName", dbName);
	TableInfo tableNameInfo = new TableInfo("tableName", tableName);

	// If the "collection" parameter is not set go with the default
	// service configuration set in the servlet web.xml.  Otherwise
	// if collection=none set tableName=null to trigger the null echo
	// test service.  Otherwise query the named JDBC table.

	Param cp = params.getParam("Collection");
	if (cp != null && cp.isSet()) {
	    String pval = cp.stringValue().toLowerCase();
	    if (pval.equals("none"))
		tableName = null;
	    else
		tableName = pval;
	}

	// Set global metadata.
	r.setDescription("DALServer null echo/test SIAP service");
	r.setType("results");

	// This indicates the query executed successfully.  If an exception
	// occurs the output we generate here will never be returned anyway,
	// so OK is always appropriate here.

	r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
	r.echoParamInfos(params);

        // This implementation supports only SIAP.
	if (!serviceClass.equalsIgnoreCase("siapv1"))
	    throw new DalServerException("Service only supports SIAP");

	// Create the table metadata for a standard SIAP query response.
	// Only the fields for which valid values are to be returned should
	// be defined here.

	// For SIAP V1.0 we define (mostly) only the standard fields here.
	// Additional DAL-V2 dataset metadata can optionally be added, and
	// this metadata defined in the provided SIAP keyword dictionary.

	// Access Metadata
	r.addGroup(siap.newGroup("Access"));
	r.addField(siap.newField("AcRef"));
	r.addField(siap.newField("Format"));
	r.addField(siap.newField("DatasetSize"));

	// General Dataset Metadata
	r.addGroup(siap.newGroup("Dataset"));
	r.addParam(siap.newParam("DataModel", "SIAP-1.0"));
	r.addParam(siap.newParam("DatasetType", "Image"));
	r.addField(siap.newField("DateObs"));
	r.addField(siap.newField("RA"));
	r.addField(siap.newField("DEC"));
	r.addField(siap.newField("Naxes"));
	r.addField(siap.newField("Naxis"));
	r.addField(siap.newField("Scale"));

	// Dataset Identification Metadata
	r.addGroup(siap.newGroup("DataID"));
	r.addField(siap.newField("Title"));
	r.addField(siap.newField("Instrument"));

	// Image WCS metadata.
	r.addGroup(siap.newGroup("WCS"));
	r.addField(siap.newField("CoordRefFrame"));
	r.addField(siap.newField("CoordEquinox"));
	r.addField(siap.newField("CoordProjection"));
	r.addField(siap.newField("CoordRefPixel"));
	r.addField(siap.newField("CoordRefValue"));
	r.addField(siap.newField("CoordCDMatrix"));


	// If dbName and dbTable are defined we assume that the service
	// has been configured to query an actual image database.  Perform
	// the query and generate the query response metadata.

	if (dbName != null && tableName != null) {
	    SiapMySql mysql = null;
	    Exception error = null;

	    if (!dbType.equalsIgnoreCase("MySQL"))
		throw new DalServerException("Only MySQL supported currently");

            // Execute the SIAP query.
            try {
                mysql = new SiapMySql(jdbcDriver, kwf);
                mysql.connect(jdbcUrl, dbName, dbUser, dbPassword);
                mysql.query(params, response);

            } catch (DalOverflowException ex) {
		// Just quit normally if overflow occurs.
		error = null;
            } catch (Exception ex) {
                error = ex;
            } finally {
		if (mysql != null)
		    mysql.disconnect();
		if (error != null)
		    throw new DalServerException(error.getMessage());
	    }
	}

	// Show the number of table rows in the response header.
	r.addInfo(key="TableRows",
	    new TableInfo(key, new Integer(r.size()).toString()));

	// We are done once the information content of the query response
	// is fully specified.  The servlet code will take care of serializing
	// the query response as a VOTable, and anything else required.
    }


    /**
     * Retrieve an actual dataset.  The dataset to be returned is specified
     * by the PubDID parameter in the input request.  The value of PubDID is
     * an ivoa dataset identifier as returned in an earlier call to the
     * queryData operation.
     *
     * <p>While to the client the access reference is a simple URL, at the
     * level of the service (in this implementation at least) the access
     * reference URL resolves into an explicit getData service operation.
     * The interpretation of PubDID is entirely up to the service.  In
     * a simple case it provides a key which can be used to retrieve an 
     * archival dataset.  In another case, the service might generate a
     * unique PubDID on the fly for each virtual dataset described in the
     * query response (e.g., for a cutout or other virtual dataset), either
     * building sufficient information into the PubDID (and hence the URL)
     * to specify the dataset to be generated, or saving internally a 
     * persistent description of the virtual dataset, indexed by the PubDID.
     * The service can later generate the dataset on the fly if and when it
     * is subsequently requested by the client.
     *
     * @param	params	The fully processed SIAP parameter set representing
     *			the request to be processed.  Upon output the
     *			parameters "datasetContentType" and
     *			"datasetContentLength" are added to specify
     *			the content (MIME) type of the dataset to be returned,
     *			and the size of the data entity to be returned, if
     *			known.  Since data entities may be dynamically
     *			computed or may be dynamic streams, the content
     *			length is not always known in advance, in which
     *			case the value should be set to null.
     *
     * @param response  A request response object (not used in the SIAP
     *			implementation).
     *
     * @return		A getData operation may return data in either of two
     *			forms.  An InputStream may be returned which can be
     *			used to return the dataset as a byte stream; it is
     *			up to the caller to close the returned stream when
     *			the data has been read.  Alternatively, if a VOTable
     *			is to be returned, the VOTable content should be
     *			written to the request response object, and null
     *			should be returned as the InputStream.
     */
    public InputStream getData(SiapParamSet params, RequestResponse response)
	throws DalServerException {

	String fileType = null;
	long fileLength = -1;
	URLConnection conn;
	InputStream in;
	URL fileURL;

	// Get the dataset identifier.
	String pubDid = params.getValue("PubDID");
	if (pubDid == null)
	    throw new DalServerException("getData: missing PubDID value");

	// In our case here the PubDID is the publisher internal URL (not
	// necessarily externally accessible) of the file to be returned.

	try {
	    fileURL = new URL(pubDid);
	    conn = fileURL.openConnection();
	    fileType = conn.getContentType();
	    fileLength = conn.getContentLength();

	    if (fileType.equals("content/unknown")) {
		FileNameMap map = URLConnection.getFileNameMap();
		fileType = map.getContentTypeFor(pubDid);
	    }

	    // Don't trust the system idea of contentType if FITS file.
	    if (pubDid.toLowerCase().endsWith(".fits"))
		fileType = "image/fits";
	    else if (pubDid.toLowerCase().endsWith(".fit"))
		fileType = "image/fits";

	    if (fileLength <= 0) {
		String fileName = fileURL.getFile();
		if (fileName.length() > 0) {
		    File dataset = new File(fileName);
		    fileLength = dataset.length();
		}
	    }

	} catch (MalformedURLException ex) {
	    throw new DalServerException(ex.getMessage());
	} catch (IOException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Tell servlet what type of data stream we are returning.

	try {
	    String contentType = this.contentType;
	    if (contentType.equalsIgnoreCase("DYNAMIC"))
		contentType = fileType;
	    params.addParam(new Param("datasetContentType",
		EnumSet.of(ParamType.STRING), contentType,
		ParamLevel.SERVICE, false, "Content type of dataset"));

	    params.addParam(new Param("datasetContentLength",
	    	EnumSet.of(ParamType.STRING),
	    	(fileLength < 0) ? null : new Long(fileLength).toString(),
	    	ParamLevel.SERVICE, false, "Content length of dataset"));

	} catch (InvalidDateException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Return an InputStream to stream the dataset out.
	try {
	    in = conn.getInputStream();
	} catch (IOException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	return (in);
    }


    /**
     * Process a service metadata query, returning a description of the
     * service to the client as a structured XML document.  In the generic
     * form this operation returns an InputStream to an actual file in
     * the local file system on the server.  The file name is formed by
     * concatenating the serviceName with "Capabilities.xml".  The file
     * is assumed to be available in the directory specified when the
     * SiapService object was created, e.g., "path/myServiceCapabilities.xml".
     * More generally, the service capabilities do not have to be maintained
     * in a static file and could be dynamically generated (e.g., from a
     * database version), so long as a valid InputStream is returned.
     *
     * @param	params	The fully processed SIAP parameter set representing
     *			the request to be processed.
     *
     * @return		An InputStream which can be used to read the 
     *			formatted getCapabilities metadata document.
     *			It is up to the caller to close the returned stream
     *			when the data has been read.
     */
    public InputStream getCapabilities(SiapParamSet params)
	throws FileNotFoundException {

	File capabilities = new File (this.configDir,
	    this.serviceName + "Capabilities.xml");
	return ((InputStream) new FileInputStream(capabilities));
    }


    // -------- Testing -----------

    /**
     * Unit test to do a simple query.
     */
    public static void main(String[] args)
	throws DalServerException, InvalidDateException,
	IOException, FileNotFoundException {

	if (args.length == 0 || args[0].equals("query")) {
	    // Exercise the SiapService class.
	    SiapService service = new SiapService(null, null);

	    // Simulate a typical query.
	    SiapParamSet params = new SiapParamSet();
	    params.setValue("VERSION", "1.0");
	    params.setValue("REQUEST", "queryData");
	    params.setValue("FORMAT", "all");
	    params.setValue("POS", "180.0,1.0");
	    params.setValue("SIZE", "0.3333");	// 20 arcmin

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Set the request response context for SIAP.
	    SiapKeywordFactory siap = new SiapKeywordFactory(r);

	    // Perform the query. 
	    try {
		service.queryData(params, r);
	    } catch (DalOverflowException ex) {
		;
	    }

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();
	}
    }
}
