package edu.harvard.cfa.vo.tapclient.tap;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * This object provides an implementation of common aspects of the AsyncJob and SyncJob.
 *
 * Two implementations are provided, {@link AsyncJob} and {@link SyncJob}, which excercise the TAP asynchronous and synchronous data query interfaces respectively.
 */
public abstract class Job {
    protected Map<String,String> parameters;
    protected Map<String,URI> inlineContent;

    /**
     * Creates a Job object.  For use by subclasses.
     */
    protected Job() {
	this.parameters = new HashMap<String,String>();
	this.inlineContent = new HashMap<String,URI>();
    }

    /**
     * Sets the named parameter equal to the value.
     * @param name of the parameter 
     * @param value of the parameter
     */
    public abstract void setParameter(String name, String value);

    /**
     * Sets the named parameter equal to the value.
     * @param name of the parameter 
     * @param value of the parameter
     */
    public void setParameter(String name, int value) {
	setParameter(name, Integer.toString(value));
    }

    /**
     * Sets the format of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the format
     */
    public void setFormat(String newValue) {
	setParameter("FORMAT", newValue);
    }

    /**
     * Sets the lang of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the lang
     */
    public void setLang(String newValue) {
	setParameter("LANG", newValue);
    }

    /**
     * Sets the maximum number of records of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the maximum number of records
     */
    public void setMaxRec(int newValue) {
	setParameter("MAXREC", newValue);
    }

    /**
     * Sets the query of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the query
     */
    public void setQuery(String newValue) {
	setParameter("QUERY", newValue);
    }

    /**
     * Sets the run id of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the run id
     */
    public void setRunId(String newValue) {
	setParameter("RUNID", newValue);
    }

    /**
     * Sets the TAP UPLOAD parameter.  This method allows a single table to be specified for upload.  This method assumes the URI will be resolved by the TAP service.
     <pre>
     job = new SyncJob(service);
     job.setParameter("QUERY", "select count(*) from service_schema.service_table t1, TAP_UPLOAD.usrtbl t2 where contains(point('ICRS-GEO', t1.ra, t1.dec), circle('ICRS-GEO', t2.ra, t2.dec, t2.radius)) = 1");
      // TAP service will call the VO cone search service.
      URI uri = new URI("http://cda.cfa.harvard.edu/cscvo/coneSearch?RA=188.97&DEC=26.36&SR=1.0");
    job.setUpload("usrtbl", uri);
     job.run();
     */
    public void setUpload(String tableName, URI uri) {
	if ("param".equals(uri.getScheme())) {
	    throw new IllegalArgumentException();
	}
	
	String value = tableName+","+uri.toString();
	inlineContent.clear();
	
	setParameter("UPLOAD", value);
    }

    /**
     * Adds to the TAP UPLOAD parameter.  This method allows multiple tables to be specified for upload.  This method assumes the URI will be resolved by the TAP service.
     */
    public void addUpload(String tableName, URI uri) {
	if ("param".equals(uri.getScheme())) {
	    throw new IllegalArgumentException();
	}

	String value = tableName+","+uri.toString();
	if (parameters.containsKey("UPLOAD")) {
	    value += ";"+parameters.get("UPLOAD");
	} 
	
	setParameter("UPLOAD", value);
    }

    /**
     * Sets the TAP UPLOAD parameter.  This method allows a single table to be uploaded.  This method assumes the URI will be resolved by this object and sent as inline content in the query request.
     <pre>
     job = new SyncJob(service);
     job.setParameter("QUERY", "select count(*) from service_schema.service_table t1, TAP_UPLOAD.usrtbl t2 where contains(point('ICRS-GEO', t1.ra, t1.dec), circle('ICRS-GEO', t2.ra, t2.dec, t2.radius)) = 1");
     // User's local data to send inline with query request
     File file = new File("myvotable.xml");
     job.setInlineUploadParameter("usrtbl", file.toURI());
     job.run();
     </pre>

     */
    public void setInlineUpload(String tableName, URI uri)  {
	if ("param".equals(uri.getScheme())) {
	    throw new IllegalArgumentException();
	}

	String value = tableName+",param:"+tableName;
	inlineContent.clear();

	setParameter("UPLOAD", value);
	inlineContent.put(tableName, uri);
    }

    /**
     * Adds to the TAP UPLOAD parameter.  This method allows multiple tables to be uploaded.  This method assumes the URI will be resolved by this object and sent as inline content in the query request.
     */
    public void addInlineUpload(String tableName, URI uri)  {
	if ("param".equals(uri.getScheme())) {
	    throw new IllegalArgumentException();
	}

	String value = tableName+",param:"+tableName;
	if (parameters.containsKey("UPLOAD")) {
	    value += ";"+parameters.get("UPLOAD");
	} 

	setParameter("UPLOAD", value);
	inlineContent.put(tableName, uri);
    }
}
