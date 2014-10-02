/*
 * SsapService.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A generic class which implements the operations for a DALServer SSAP
 * service.  A real data service should subclass this, replacing the
 * <i>queryData</i> operation with a version which implements the same
 * operation for a specific archive and data collection.  In most cases
 * the remaining SSAP operations (e.g., <i>getCapabilities</i>) should not
 * need to be subclassed by a data service, except to edit the metadata
 * to be returned.  The remaining code required to implement a complete
 * service (excluding that required to generate the data product
 * to be returned) is provided by the {@link dalserver} code.
 *
 * <p>The service implementation at this level should not be protocol
 * specific.  The intention is that the service operations at this level can
 * be used with any distributed computing platform.  Hence we can implement
 * an HTTP GET interface now, and add a SOAP or other interface later,
 * or some other interface which may not even be network based, all sharing
 * the same core service operations.</p>
 */
public class SsapService {
    /**
     * The service name, used to identify resources associated with this
     * service instance.
     */
    private String serviceName = "ssap";

    /** Local directory where configuration files are stored. */
    private String configDir;

    /** Local directory where data files are stored. */
    private String dataDir;

    /** File content (MIME) type of any returned datasets. */
    private String contentType;


    /**
     * Create a new local SSAP service instance.
     *
     * @param params	Input parameter set.
     */
    public SsapService(SsapParamSet params) {
	if (params == null) {
	    this.serviceName = "ssap";
	    this.configDir = "/tmp";
	    this.dataDir = "/tmp";
	    this.contentType = "text/xml;x-votable";
	} else {
	    try {
		this.serviceName = params.getValue("serviceName");
		this.configDir = params.getValue("configDir");
		this.dataDir = params.getValue("dataDir");
		this.contentType = params.getValue("contentType");
	    } catch (DalServerException ex) {
		// Keep defaults.
	    }
	}
    }

    /**
     * Process a data query and generate a list of candidate spectral
     * datasets matching the query parameters.  The datasets referenced
     * in the query response may be physical datasets, or virtual datasets
     * which will be generated on demand, only if subsequently requested
     * by the client.
     *
     * @param	params	  The fully processed SSAP parameter set representing
     *			  the request to be processed.
     *
     * @param	response  A dalserver request response object to which the
     *			  query response should be written.  Note this is
     *			  not a file, but an object containing the metadata
     *			  to be returned to the client.
     */
    @SuppressWarnings("unchecked")
    public void queryData(SsapParamSet params, RequestResponse response)
	    throws DalServerException {

	boolean debug = (params.getParam("DEBUG") != null);
	SsapKeywordFactory ssap = new SsapKeywordFactory(response);
	RequestResponse r = response;
	String id, key;

	// Set global metadata.
	r.setDescription("DALServer null echo/test SSAP service");
	r.setType("Results");

	// This indicates the query executed successfully.  If an exception
	// occurs the output we generate here will never be returned anyway,
	// so OK is always appropriate here.

	r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));

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
 
	// Create the table metadata for a standard SSAP query response.
	// In a real data table, only the fields for which valid values are
	// to be returned should be defined here.  Any field which has a
	// constant value for all table rows should be defined as a PARAM;
	// some fields which are normally PARAMs are defined as such in the
	// default table constructor below.  Any additional Spectrum data
	// model fields may also be returned here as well.  If the service
	// wants to include some custom metadata, those fields should also
	// be defined here.
	//
	// Notes: Multiple association groups are possible in the same table,
	// if a single dataset is a member of more than one association.
	// To implement this, one would assign a unique GROUP id to each
	// association, instead of the default ID "Association" shown below.
	// This can be done by specifying the unique GROUP ID string as an
	// additional argument to the newGroup, newParam, or newField methods.
	// If no logical associations are defined, no Association groups
	// should be created.
	//
	// TOKEN should only be generated if a large query response is to be
	// returned in multiple pages.

	// Query Metadata
	r.addGroup(ssap.newGroup("Query"));
	r.addField(ssap.newField("Score"));
	r.addParam(ssap.newParam("Token", "UNSET"));

	// Association Metadata
	r.addGroup(ssap.newGroup("Association"));
	r.addParam(ssap.newParam("AssocType", "UNSET"));
	r.addField(ssap.newField("AssocID"));
	r.addField(ssap.newField("AssocKey"));

	// Access Metadata
	r.addGroup(ssap.newGroup("Access"));
	r.addField(ssap.newField("AcRef"));
	r.addField(ssap.newField("Format"));
	r.addField(ssap.newField("DatasetSize"));

	// General Dataset Metadata
	r.addGroup(ssap.newGroup("Dataset"));
	r.addParam(ssap.newParam("DataModel", "Spectrum-1.0"));
	r.addParam(ssap.newParam("DatasetType", "Spectrum"));
	r.addField(ssap.newField("DataLength"));
	r.addField(ssap.newField("TimeSI"));
	r.addField(ssap.newField("SpectralSI"));
	r.addField(ssap.newField("FluxSI"));
	r.addField(ssap.newField("SpectralAxis"));
	r.addField(ssap.newField("FluxAxis"));

	// Dataset Identification Metadata
	r.addGroup(ssap.newGroup("DataID"));
	r.addField(ssap.newField("Title"));
	r.addField(ssap.newField("Creator"));
	r.addField(ssap.newField("Collection"));
	r.addField(ssap.newField("DatasetID"));
	r.addField(ssap.newField("CreatorDID"));
	r.addField(ssap.newField("CreatorDate"));
	r.addField(ssap.newField("CreatorVersion"));
	r.addField(ssap.newField("Instrument"));
	r.addField(ssap.newField("Bandpass"));
	r.addField(ssap.newField("DataSource"));
	r.addField(ssap.newField("CreationType"));
	r.addField(ssap.newField("CreatorLogo"));
	r.addField(ssap.newField("Contributor"));

	// Curation Metadata
	r.addGroup(ssap.newGroup("Curation"));
	r.addField(ssap.newField("Publisher"));
	r.addField(ssap.newField("PublisherID"));
	r.addField(ssap.newField("PublisherDID"));
	r.addField(ssap.newField("PublisherDate"));
	r.addField(ssap.newField("PublisherVersion"));
	r.addField(ssap.newField("Rights"));
	r.addField(ssap.newField("Reference"));
	r.addField(ssap.newField("ContactName"));
	r.addField(ssap.newField("ContactEmail"));

	// Target Metadata
	r.addGroup(ssap.newGroup("Target"));
	r.addField(ssap.newField("TargetDescription"));
	r.addField(ssap.newField("TargetName"));
	r.addField(ssap.newField("TargetClass"));
	r.addField(ssap.newField("TargetPos"));
	r.addField(ssap.newField("SpectralClass"));
	r.addField(ssap.newField("Redshift"));
	r.addField(ssap.newField("VarAmpl"));

	// Derived Metadata
	r.addGroup(ssap.newGroup("Derived"));
	r.addField(ssap.newField("DerivedSNR"));
	r.addField(ssap.newField("DerivedVarAmpl"));
	r.addField(ssap.newField("DerivedRedshift"));
	r.addField(ssap.newField("RedshiftStatError"));
	r.addField(ssap.newField("RedshiftConfidence"));

	// Coordinate System Metadata
	r.addGroup(ssap.newGroup("CoordSys"));
	r.addParam(ssap.newParam("SpaceFrameName", "UNSET"));
	r.addParam(ssap.newParam("SpaceFrameUcd", "UNSET"));
	r.addParam(ssap.newParam("SpaceFrameRefPos", "UNSET"));
	r.addParam(ssap.newParam("SpaceFrameEquinox", "UNSET"));
	r.addParam(ssap.newParam("TimeFrameName", "UNSET"));
	r.addParam(ssap.newParam("TimeFrameUcd", "UNSET"));
	r.addParam(ssap.newParam("TimeFrameZero", "UNSET"));
	r.addParam(ssap.newParam("TimeFrameRefPos", "UNSET"));
	r.addParam(ssap.newParam("SpectralFrameName", "UNSET"));
	r.addParam(ssap.newParam("SpectralFrameUcd", "UNSET"));
	r.addParam(ssap.newParam("SpectralFrameRefPos", "UNSET"));
	r.addParam(ssap.newParam("SpectralFrameRedshift", "UNSET"));
	r.addParam(ssap.newParam("RedshiftFrameName", "UNSET"));
	r.addParam(ssap.newParam("RedshiftFrameRefPos", "UNSET"));
	r.addParam(ssap.newParam("DopplerDefinition", "UNSET"));

	// Spatial Axis Characterization
	r.addGroup(ssap.newGroup("Char.SpatialAxis"));
	r.addField(ssap.newField("SpatialAxisName"));
	r.addField(ssap.newField("SpatialAxisUcd"));
	r.addField(ssap.newField("SpatialAxisUnit"));
	r.addField(ssap.newField("SpatialLocation"));
	r.addField(ssap.newField("SpatialExtent"));
	r.addField(ssap.newField("SpatialArea"));
	r.addField(ssap.newField("SpatialSupportExtent"));
	r.addField(ssap.newField("SpatialSampleExtent"));
	r.addField(ssap.newField("SpatialFillFactor"));
	r.addField(ssap.newField("SpatialStatError"));
	r.addField(ssap.newField("SpatialSysError"));
	r.addField(ssap.newField("SpatialCalibration"));
	r.addField(ssap.newField("SpatialResolution"));

	// Spectral Axis Characterization
	r.addGroup(ssap.newGroup("Char.SpectralAxis"));
	r.addField(ssap.newField("SpectralAxisName"));
	r.addField(ssap.newField("SpectralAxisUcd"));
	r.addField(ssap.newField("SpectralAxisUnit"));
	r.addField(ssap.newField("SpectralLocation"));
	r.addField(ssap.newField("SpectralExtent"));
	r.addField(ssap.newField("SpectralStart"));
	r.addField(ssap.newField("SpectralStop"));
	r.addField(ssap.newField("SpectralSupportExtent"));
	r.addField(ssap.newField("SpectralSampleExtent"));
	r.addField(ssap.newField("SpectralFillFactor"));
	r.addField(ssap.newField("SpectralBinSize"));
	r.addField(ssap.newField("SpectralStatError"));
	r.addField(ssap.newField("SpectralSysError"));
	r.addField(ssap.newField("SpectralCalibration"));
	r.addField(ssap.newField("SpectralResolution"));
	r.addField(ssap.newField("SpectralResPower"));

	// Time Axis Characterization
	r.addGroup(ssap.newGroup("Char.TimeAxis"));
	r.addField(ssap.newField("TimeAxisName"));
	r.addField(ssap.newField("TimeAxisUcd"));
	r.addField(ssap.newField("TimeAxisUnit"));
	r.addField(ssap.newField("TimeLocation"));
	r.addField(ssap.newField("TimeExtent"));
	r.addField(ssap.newField("TimeStart"));
	r.addField(ssap.newField("TimeStop"));
	r.addField(ssap.newField("TimeSupportExtent"));
	r.addField(ssap.newField("TimeSampleExtent"));
	r.addField(ssap.newField("TimeFillFactor"));
	r.addField(ssap.newField("TimeBinSize"));
	r.addField(ssap.newField("TimeStatError"));
	r.addField(ssap.newField("TimeSysError"));
	r.addField(ssap.newField("TimeCalibration"));
	r.addField(ssap.newField("TimeResolution"));

	// Flux Axis Characterization
	r.addGroup(ssap.newGroup("Char.FluxAxis"));
	r.addField(ssap.newField("FluxAxisName"));
	r.addField(ssap.newField("FluxAxisUcd"));
	r.addField(ssap.newField("FluxAxisUnit"));
	r.addField(ssap.newField("FluxStatError"));
	r.addField(ssap.newField("FluxSysError"));
	r.addField(ssap.newField("FluxCalibration"));

	// Write the table data.  Since this is a dummy service we have
	// no data (a valid limiting case of any data service), hence we
	// return no table rows.

	// foreach row {
	//     r.addRow();
	//     r.setValue("Score", 0.3);
	//     r.setValue("AcRef", "http://myvo.org/ssap?foo=bar");
	//     r.setValue("Format", "text/xml;x-votable");
	//     r.setValue("Size", "250000");
	//        (etc.)
	// }

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
     * <p>Since our template implementation here is generic and does not
     * access any real data, we simulate data access by using PubDID to pass
     * the name of a sample data file to be found in the "dataDir" directory
     * specified in the service configuration.
     *
     * @param	params	The fully processed SSAP parameter set representing
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
     * @param response  A request response object, which may be used if
     *			the response is a VOTable.
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
    public InputStream getData(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	// Get the dataset identifier.
	String pubDid = params.getValue("PubDID");
	if (pubDid == null)
	    throw new DalServerException("getData: missing PubDID value");

	// In our case here, everything after the "#" is a filename.
	int offset = pubDid.indexOf("#");
	if (offset < 0)
	    throw new DalServerException(
		"filename missing in PubDID " + "["+pubDid+"]");

	// Get the name of the file to be retrieved.
	String fileName = pubDid.substring(offset + 1);
	File dataset = new File (this.dataDir, fileName);
	InputStream in = null;

	// Tell servlet what type of data stream we are returning.
	try {
	    String contentType = this.contentType;
	    if (contentType.equals("DYNAMIC")) {
		FileNameMap map = URLConnection.getFileNameMap();
		contentType = map.getContentTypeFor(dataset.getPath());
	    } 
	    params.addParam(new Param("datasetContentType",
		EnumSet.of(ParamType.STRING), contentType,
		ParamLevel.SERVICE, false, "Content type of dataset"));
	    params.addParam(new Param("datasetContentLength",
		EnumSet.of(ParamType.STRING),
		new Long(dataset.length()).toString(),
		ParamLevel.SERVICE, false, "Content length of dataset"));

	} catch (InvalidDateException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Return an InputStream to read the dataset.
	try {
	    in = (InputStream) new FileInputStream(dataset);
	} catch (FileNotFoundException ex) {
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
     * SsapService object was created, e.g., "path/myServiceCapabilities.xml".
     * More generally, the service capabilities do not have to be maintained
     * in a static file and could be dynamically generated (e.g., from a
     * database version), so long as a valid InputStream is returned.
     *
     * @param	params	The fully processed SSAP parameter set representing
     *			the request to be processed.
     *
     * @return		An InputStream which can be used to read the 
     *			formatted getCapabilities metadata document.
     *			It is up to the caller to close the returned stream
     *			when the data has been read.
     */
    public InputStream getCapabilities(SsapParamSet params)
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
	    // Exercise the SsapService class.
	    SsapService service = new SsapService(null);

	    // Simulate a typical query.
	    SsapParamSet params = new SsapParamSet();
	    params.setValue("VERSION", "1.0");
	    params.setValue("REQUEST", "queryData");
	    params.setValue("FORMAT", "all");
	    params.setValue("POS", "180.0,1.0;ICRS");
	    params.setValue("SIZE", "0.3333");	// 20 arcmin
	    params.setValue("Collection", "ivo://jhu/sdss/dr5");
	    params.setValue("TOP", "20");

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Set the request response context for SSAP.
	    SsapKeywordFactory ssap = new SsapKeywordFactory(r);

	    // Perform the query. 
	    service.queryData(params, r);

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();
	}
    }
}
