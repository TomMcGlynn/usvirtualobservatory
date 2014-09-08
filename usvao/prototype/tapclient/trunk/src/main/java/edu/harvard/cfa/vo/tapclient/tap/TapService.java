package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import net.ivoa.xml.uws.v10.JobsDocument;
import net.ivoa.xml.uws.v10.ShortJobDescription;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;
import edu.harvard.cfa.vo.tapclient.vosi.Availability;
import edu.harvard.cfa.vo.tapclient.vosi.Capabilities;
import edu.harvard.cfa.vo.tapclient.vosi.Column;
import edu.harvard.cfa.vo.tapclient.vosi.ForeignKey;
import edu.harvard.cfa.vo.tapclient.vosi.Schema;
import edu.harvard.cfa.vo.tapclient.vosi.Table;
import edu.harvard.cfa.vo.tapclient.vosi.TableSet;
import edu.harvard.cfa.vo.tapclient.vosi.Vosi;

/**
 * An object for accessing the service level functions of a VO TAP service.  The functions include the VOSI availability, capabilites, and tables metadata as well as the TAP job list.
 *
 * <pre>
 *   // Create a TapService object.
 *   TapService service = new TapService(baseURL);
 *
 *   // Retrieve the VOSI tables for the service.
 *   TableSet tableset = service.getTableSet();
 *
 *   // Execute a metadata query at the TAP service synchronous endpoint.
 *   SyncJob syncJob = new SyncJob(tapService);
 *   syncJob.setParameter("FORMAT", "votable");
 *   syncJob.setParameter("LANG", "ADQL");
 *   syncJob.setParameter("QUERY", "SELECT * FROM master_source");
 *
 *   // Handle results
 *   InputStream = syncJob.run();
 *   ...
 * </pre>
 */
public class TapService implements Vosi {
    private final static Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.tap.TapService");

    private static final String VOSI_AVAILABILITY_ENDPOINT = "/availability";
    private static final String VOSI_CAPABILITIES_ENDPOINT = "/capabilities";
    private static final String VOSI_TABLES_ENDPOINT = "/tables";

    private String baseURL;

    private Availability availability;
    private Capabilities capabilities;
    private TableSet tableset;
    private TableSet metadataTableset;

    /**
     * Constructs a TapService object associated with the service located at the baseURL.
     * @param baseURL TAP service base URL, cannot be null.
     * @throws NullPointerException if baseURL is null.
     */
    public TapService(String baseURL) {
	this.baseURL = baseURL;
	this.availability = new Availability(baseURL+VOSI_AVAILABILITY_ENDPOINT);
	this.capabilities = new Capabilities(baseURL+VOSI_CAPABILITIES_ENDPOINT);
	this.tableset = new TableSet(baseURL+VOSI_TABLES_ENDPOINT);
 	this.metadataTableset = null;
   }

    /**
     * Returns a TableSet object constructed from a metadata query that represents the TAP tableset metadata of the service.  A set of metadata queries are mader to the service every time the getTablesetFromMetadata method is called.
     *
     * @return the tableset constructed from TAP_SCHEMA metadata queries.
     *
     * @throws HttpException if the service responses to the VOSI Tables request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Tables document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public TableSet getTableSetFromMetadata() throws HttpException, ResponseFormatException, IOException {
	if (metadataTableset == null) {
	    metadataTableset = (TableSet) new MetadataTableSet(this);
	    metadataTableset.update();
	} 
	
	return metadataTableset;
    }

    /**
     * Gets the base URL of this object.
     * @return base URL of this TapService object.
     */
    public String getBaseURL() {
	return baseURL;
    }

    /**
     * Returns an Availability object that represents the VOSI availability of
     * the service.  A new VOSI availability request is made to the 
     * service every time the getAvailability method is called.
     *
     * @return the VOSI availability from the service.
     *
     * @throws HttpException if the service responses to the VOSI Availability request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Availability document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public Availability getAvailability() throws HttpException, ResponseFormatException, IOException {
	if (availability != null)
	    availability.update();
        return availability;
    }

    /**
     * Returns a list of Capability objects that represents the VOSI capabilities
     * of the service.  A new VOSI capabilities request is made to the 
     * service every time the getCapabilities method is called.
     * 
     * @return a list of Capabilty objects representing the VOSI capabilities of
     * the service.
     *
     * @throws HttpException if the service responses to the VOSI Capabilities request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Capabilities document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public Capabilities getCapabilities() throws HttpException, ResponseFormatException, IOException {
	if (capabilities != null)
	    capabilities.update();
        return capabilities;
    }

    /**
     * Returns a list of Schema objects that represents the VOSI table set metadata of the service.  A new VOSI tables request is made to the service every
     * time the getTableset method is called.
     *
     * @return the VOSI tables from the service.
     *
     * @throws HttpException if the service responses to the VOSI Tables request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Tables document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public TableSet getTableSet() throws HttpException, ResponseFormatException, IOException {
	if (tableset != null)
	    tableset.update();
	return tableset;
    }

    /**
     * Returns a List of AsyncJob objects.  
     * @return list of asynchronous jobs associated with this TAP service
     * @throws HttpException if the service responses to the metadata query with an unexpected HTTP status.  It should be noted that some services may respond to a job list request with an HTTP 403 Forbidden code which will manifest in an HttpException.
     * @throws ResponseFormatException if an error occurs parsing the service response into a metadata query result document.
     * @throws IOException  if an error occurs creating an input stream.
     */
    public List<AsyncJob> getJobs() throws HttpException, ResponseFormatException, IOException { 
	List<AsyncJob> jobList = new ArrayList<AsyncJob>();

	InputStream inputStream = null;
	try {
	    Map<String,String> emptyMap = Collections.emptyMap();
	    inputStream = HttpClient.get(getBaseURL()+"/async", emptyMap);

	    XmlOptions xmlOptions = new XmlOptions();
	    QName jobsDocumentElement = new QName("http://www.ivoa.net/xml/UWS/v1.0", "jobs", "uws");
	    xmlOptions.setLoadReplaceDocumentElement(jobsDocumentElement);
	    JobsDocument xdocument = JobsDocument.Factory.parse(inputStream, xmlOptions);
	    JobsDocument.Jobs xjobs = xdocument.getJobs();
	    
	    if (xjobs != null) {
		List<ShortJobDescription> xjobrefList = xjobs.getJobrefList();
		
		for (ShortJobDescription xjobref: xjobrefList) {
		    xjobref.getPhase().toString();
		    jobList.add(new AsyncJob(this, xjobref.getId(), xjobref.getPhase().toString()));
		}
	    }
	} catch (HttpException ex) {
	    if (logger.isLoggable(Level.SEVERE))
		logger.log(Level.SEVERE, "error getting UWS Jobs response: "+ex.getMessage());
	    throw new HttpException("error getting UWS Jobs response: "+ex.getMessage(), ex);
	} catch (IOException ex) {
	    if (logger.isLoggable(Level.SEVERE))
		logger.log(Level.SEVERE, "error reading UWS Jobs response: "+ex.getMessage());
	    throw new IOException("error reading UWS Jobs response: "+ex.getMessage(), ex);
	} catch (XmlException ex) {
	    if (logger.isLoggable(Level.SEVERE))
		logger.log(Level.SEVERE, "error parsing UWS Jobs response: "+ex.getMessage());
	    throw new ResponseFormatException("error parsing UWS Jobs response: "+ex.getMessage(), ex);
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (Exception ignore) {
		    if (logger.isLoggable(Level.SEVERE)) 
			logger.log(Level.SEVERE, "error closing input stream", ignore);
		}
	    }
	}

	return jobList;
    }

    static void printTableset(TableSet tableset) {
	for (Schema schema: tableset.getSchemas()) {
	    System.out.println(schema+"\t[Schema]");
	    
	    for (Table table: schema.getTables()) {
		System.out.println("\t"+table+"\t[Table]");
		
		for (Column column: table.getIndexedColumns()) {
		    System.out.println("\t\t"+column+"\t[Indexed column]");
		}
		
		for (ForeignKey foreignKey: table.getForeignKeys()) {
		    System.out.println("\t\t"+foreignKey+"\t[Foreign key]");
		} 
		System.out.println("");
	    }
	    System.out.println("");
	}
    }
}
