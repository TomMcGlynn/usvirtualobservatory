package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;

/**
 * An object used for executing an synchronous data query.
 *
 * <pre>
 	    SyncJob syncJob = new SyncJob(service);

	    // Set the parameters
	    syncJob.setParameter("QUERY", "SELECT TOP 10 * FROM TAP_SCHEMA.columns");
	    syncJob.setParameter("LANG", "ADQL");
	    syncJob.setParameter("FORMAT", "votable");
	    syncJob.setParameter("MAXREC", "1000");

	    // Run the job
	    InputStream resultStream = null;
	    try {
		resultStream = syncJob.run();
		// ...
	    } finally {
		try { 
		    resultStream.close();
		} catch (Exception ignore) {
		}
	    }
 * </pre>
 */
public class SyncJob extends Job {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.tap.SyncJob");

    private TapService service;
    private boolean finished;

    /**
     * Creates a SyncJob to execute a synchronous data query against the TAP service.  The TAP LANG and FORMAT parameters are set to
     * 'ADQL' and 'votable' by default.  These values may be overridden by a
     * call to #setParameter
     *
     * @param service the TAP service service
     * @throws IllegalArgumentException if service is null
     * @see #SyncJob(TapService,String,String,String)
     */
    public SyncJob(TapService service) {
	this(service, null, "ADQL", "votable");
    }

    /**
     * Creates a SyncJob object to execute a synchronous data query against the TAP service.
     * @param service the TAP service service
     * @param query the query
     * @param lang the requested query language
     * @param format the requested output results format
     * @throws IllegalArgumentException if service is null
     */
    public SyncJob(TapService service, String query, String lang, String format) {
	super();
	if (service == null)
	    throw new IllegalArgumentException("service is null");

	this.service = service;
	this.parameters.put("REQUEST", "doQuery");
	if (query != null)
	    this.parameters.put("QUERY", query);
	if (lang != null)
	    this.parameters.put("LANG", lang);
	if (format != null)
	    this.parameters.put("FORMAT", format);
	this.finished = false;
    }

    /**
     * Sets the named parameter's value.  The parameter will be posted with the synchronous data query to the TAP service when #run is called.
     * @param name parameter name to add
     * @param value parameter value to add
     * @throws IllegalStateException if #run has already been called on this SyncJob object.
     */
    public void setParameter(String name, String value) {
	if (finished) 
	    throw new IllegalStateException("job is finished");
	parameters.put(name, value);
    }

    /**
     * Sends a request to the TAP service to run this SyncJob objects's data query.  Note that the response could be either a result set in the requested format or an error document. 
     * @return InputStream the TAP service response to posting the synchronous
     * data query.
     * @throws HttpException if the service responses with an unexpected HTTP status.
     * @throws IOException if an error occurs creating the InputStream
     * @throws IllegalStateException if #run has already been called on this SyncJob object.
     */
    public InputStream run() throws HttpException, IOException {
	if (finished) 
	    throw new IllegalStateException("job is finished");
	finished = true;

	InputStream inputStream = null;
	try { 
	    Map<String,URI> runtimeInlineContent = new HashMap<String,URI>();

	    Set<Map.Entry<String,String>> entrySet = parameters.entrySet();
	    Iterator<Map.Entry<String,String>> entryIterator = entrySet.iterator();
	    while (entryIterator.hasNext()) {
		Map.Entry<String,String> entry = entryIterator.next();
		if ("UPLOAD".equalsIgnoreCase(entry.getKey())) {
		    String value = entry.getValue();
		    String[] pairs = value.split(";");
		    for (String pair: pairs) {
			String[] nameURI = pair.split(",");
			if (nameURI[1].startsWith("param:")) {
			    runtimeInlineContent.put(nameURI[0], inlineContent.get(nameURI[0]));
			}
		    }
		}
	    }

	    if (runtimeInlineContent.isEmpty()) {
		inputStream = HttpClient.post(service.getBaseURL()+"/sync", parameters);
	    } else {
		inputStream = HttpClient.post(service.getBaseURL()+"/sync", parameters, runtimeInlineContent);
	    }
	} catch (HttpException ex) {
	    throw new HttpException("error getting TAP server run response: "+ex.getMessage(), ex);
	} catch (IOException ex) {
	    throw new IOException("error reading TAP server run response: "+ex.getMessage(), ex);
	}
	return inputStream;
    }
}