package edu.harvard.cfa.vo.tapclient.tap;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException ;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import net.ivoa.xml.uws.v10.ErrorType;
import net.ivoa.xml.uws.v10.ExecutionPhase;
import net.ivoa.xml.uws.v10.JobDocument;
import net.ivoa.xml.uws.v10.Parameter;
import net.ivoa.xml.uws.v10.ParametersDocument.Parameters;
import net.ivoa.xml.uws.v10.ResultsDocument.Results;
import net.ivoa.xml.uws.v10.ResultReference;
import org.w3.x1999.xlink.TypeAttribute.Type;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;

/**
 * An object used for executing an asynchronous data query.
 *
 * <pre>
  	    AsyncJob asyncJob = new AsyncJob(service);

	    System.out.println("Set the parameters");
	    asyncJob.setParameter("QUERY", "SELECT TOP 10 * FROM TAP_SCHEMA.columns");
	    asyncJob.setParameter("LANG", "ADQL");
	    asyncJob.setParameter("FORMAT", "votable");
	    asyncJob.setParameter("MAXREC", "1000");

	    System.out.println("Run the job");
	    asyncJob.run();
	    
	    while (! asyncJob.isFinished()) {
		Thread.sleep(5000); 
		System.out.println("Update the job status");
		asyncJob.synchronize();
	    }

	    if (asyncJob.isCompleted()) {
		System.out.println("Job has completed");
		// List<Result> resultList = asyncJob.getResults();
		Result result = asyncJob.getResult();
		InputStream resultStream = null;
		try {
		    resultStream = result.openStream();
		    // ...
		} finally {
		    try { 
			resultStream.close();
		    } catch (Exception ignore) {
		    }
		}
	    } else if (asyncJob.isError()) {
		System.out.println("Job has an error");
		Error error = asyncJob.getError();
		System.out.println("The service has encountered a "+error.getType()+" error: "+error.getMessage());
		if (error.isDetailedErrorAvailable()) {
		    System.out.println("Additional details follow:");
		    InputStream errorStream = null;
		    try {
			errorStream = error.openStream();
			// ...
		    } finally {
			try {
			    errorStream.close();
			} catch (Exception ignore) {
			}
		    }
		} 
	    } else if (asyncJob.isAborted()) {
		System.out.println("The service has 'ABORTED' this job.");
	    } else if (asyncJob.isHeld()) {
		System.out.println("The service has 'HELD' this job.  Please run this job again at a later time.");
	    } 
 * </pre>
 */
public class AsyncJob extends Job {
    private static Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.tap.AsyncJob");

    public static final String ADQL_LANG = "ADQL";
    public static final String VOTABLE_FORMAT = "votable";

    private TapService service;
    private XMLInputFactory xmlInputFactory;
    private DatatypeFactory datatypeFactory;

    private AsyncJob job;

    private String jobId;
    private String ownerId;
    private String phase;
    private Calendar quote;
    private Calendar startTime;
    private Calendar endTime;
    private Integer executionDuration;
    private boolean isNewExecutionDuration;
    private Calendar destruction;
    private boolean isNewDestruction;
    private Map<String,String> newParameters;
    private Set<Result> results;
    private Error error;
    private Object jobInfo;


    /**
     * Creates an asynchronous job object associated with the TAP service at TapService.
     */
    public AsyncJob(TapService service) {
	this(service, null);
    }

    /**
     * Creates an asynchronous job object associated with the TAP service and job at service and jobId.
     */
    public AsyncJob(TapService service, String jobId) {
	super();
	this.service = service;
	this.jobId = jobId;
	this.newParameters = new HashMap<String,String>();
	this.results = new LinkedHashSet<Result>();

	// For csctap/ jobSummary->errorSummary->type.  Changes case.
	try {
	    this.xmlInputFactory = XMLInputFactory.newInstance();
	} catch (FactoryConfigurationError ex) {
	    if (logger.isLoggable(Level.WARNING)) 
		logger.log(Level.WARNING, "error configuring XML input factory.");
	}

	// For destruction -> XMLGregorianCalendar.normalize();
	try {
	    this.datatypeFactory = DatatypeFactory.newInstance();
	} catch (DatatypeConfigurationException ex) {
	    if (logger.isLoggable(Level.WARNING)) 
		logger.log(Level.WARNING, "error configuring XML datatype factory.");
	}
    }

    /**
     * Creates an asynchronous job object associated with the TAP service and job at service and jobId.
     */
    AsyncJob(TapService service, String jobId, String phase) {
	super();
	this.service = service;
	this.jobId = jobId;
	this.newParameters = new HashMap<String,String>();
	this.results = new LinkedHashSet<Result>();
	this.setPhase(phase);
		
	// For csctap/ jobSummary->errorSummary->type.  Changes case.
	try {
	    this.xmlInputFactory = XMLInputFactory.newInstance();
	} catch (FactoryConfigurationError ex) {
	    if (logger.isLoggable(Level.WARNING)) 
		logger.log(Level.WARNING, "error configuring XML input factory.");
	}

	// For destruction -> XMLGregorianCalendar.normalize();
	try {
	    this.datatypeFactory = DatatypeFactory.newInstance();
	} catch (DatatypeConfigurationException ex) {
	    if (logger.isLoggable(Level.WARNING)) 
		logger.log(Level.WARNING, "error configuring XML datatype factory.");
	}

	this.setPhase(phase);
    }

    /**
     * Sends a request to the TAP service to run this job.
     * @throws HttpException if the service response to the run request is an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into a UWS job summary  document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public void run() throws HttpException, ResponseFormatException, IOException {
	setPhase("RUN");
	synchronize();
    }

    /**
     * Sends a request to the TAP service to abort this job.
     * @throws HttpException if the service response to the abort request is an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into a UWS job summary document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public void abort() throws HttpException, ResponseFormatException, IOException {
	setPhase("ABORT");
	synchronize();
    }

    /**
     * Sends a request to the TAP service to delete this job and all of its resources.
     * @throws HttpException if the service response to the delete request is an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into a UWS job list document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public void delete() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = HttpClient.delete(service.getBaseURL()+"/async/"+URLEncoder.encode(getJobId(), "UTF-8"));
	inputStream.close();
    }

    /**
     * Returns the job id of this job as returned by the TAP service
     * @return job id provided by the TAP service
     */
    public String getJobId() {
	return jobId;
    }

    /**
     * Returns the run id of this job as returned by the TAP service
     * @return run id
     */
    public String getRunId() {
	return getParameters().get("RUNID");
    }

    /**
     * Returns the owner id of this job as returned by the TAP service
     * @return owner id provided by the TAP service
     */
    public String getOwnerId() {
	return ownerId;
    }

    /**
     * Returns the execution phase of this job as returned by the TAP service
     * @return execution phase
     */
    public String getPhase() {
	return phase;
    }

    /**
     * Returns true if the job has an execution phase that is completed, error, aborted, or held.
     * @return true if the job has an execution phase that is completed, error, aborted, or held, false otherwise
     */
    public boolean isFinished() {
	return (isCompleted() || isError() || isAborted() || isHeld());
    }

    /**
     * Returns true is the execution phase of the job is COMPLETED
     * @return true if the job has successfully completed, false otherwise
     */
    public boolean isCompleted() {
 	return "COMPLETED".equals(phase);
    }

    /**
     * Returns true is the execution phase of the job is ERROR
     * @return true if the job had an error, false otherwise.
     */
    public boolean isError() {
 	return "ERROR".equals(phase);
    }

    /**
     * Returns true is the execution phase of the job is ABORTED
     * @return true if the job was aborted, false otherwise.
     */
    public boolean isAborted() {
 	return "ABORTED".equals(phase);
    }

    /**
     * Returns true is the execution phase of the job is HELD.  If true, it may be possible to rerun this job.
     * @return true if the job was held by the service, false otherwise.
     */
    public boolean isHeld() {
 	return "HELD".equals(phase);
    }

    /**
     * Returns the quote of this job as returned by the TAP service
     * @return quote for job execution from the TAP service
     */
    public Calendar getQuote() {
	return quote;
    }

    /**
     * Returns the start time of this job as returned by the TAP service
     * @return start time of the job on the TAP service
     */
    public Calendar getStartTime() {
	return startTime;
    }

    /**
     * Returns the end time of this job as returned by the TAP service
     * @return end time of the job on the TAP service
     */
    public Calendar getEndTime() {
	return endTime;
    }

    /**
     * Returns the execution duration of this job as returned by the TAP service
     * @return execution duration in seconds
     */
    public Integer getExecutionDuration() {
	return executionDuration;
    }

    /**
     * Returns the destruction of this job as returned by the TAP service
     * @return destruction timestamp
     */
    public Calendar getDestruction() {
	return destruction;
    }

    /**
     * Returns the parameters of this job as returned by the TAP service
     * @return parameter list
     */
    public Map<String,String> getParameters() {
	return Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns the results of this job as returned by the TAP service
     * @return result list
     */
    public Set<Result> getResults() {
	return Collections.unmodifiableSet(results);
    }

    /**
     * Returns the main result of this job
     * @return result 
     */
    public Result getResult() {
	if (results != null && ! results.isEmpty()) {
	    return new Result("results/result", null, service.getBaseURL()+"/async/"+getJobId()+"/results/result");
	} 

	return null;
    }

    /**
     * Returns the error summary of this job as returned by the TAP service
     * @return error summary if the job was run an there was an error, null otherwise
     */
    public Error getError() {
	return error;
    }
    
    /**
     * Returns the job info  of this job as returned by the TAP service
     * @return additional job info, service dependent.
     */
    public Object getJobInfo() {
	return jobInfo;
    }

    /**
     * Sets the execution phase of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the run id
     */
    public void setPhase(String newValue) {
	phase = newValue;
    }

    /**
     * Sets the execution duration of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the execution duration in seconds
     */
    public void setExecutionDuration(Integer newValue) {
	executionDuration = newValue;
	isNewExecutionDuration = true;
    }

    /**
     * Sets the destruction of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param newValue the destruction timestamp
     */
    public void setDestruction(Calendar newValue) {
	destruction = newValue;
	isNewDestruction = true;
    }

    /**
     * Sets a parameter of this job.  The value is sent to the TAP service when #run or #update is called.
     * @param id the parameter identifier
     * @param value the parameter value
     */
    public void setParameter(String id, String value) {
	String idToUpperCase = id.toUpperCase();
	parameters.put(idToUpperCase, value);
	newParameters.put(idToUpperCase, value);
    }

    // Sets the members of this object using the underlying binding to parse the InputStream.
    public void handleJobSummaryResponse(InputStream inputStream) throws IOException, XmlException {
	try {
	    JobDocument xdocument = parseJobDocument(inputStream);

	    net.ivoa.xml.uws.v10.JobSummary xjobSummary = xdocument.getJob();
	    
	    if (getJobId() != null && ! getJobId().equals(xjobSummary.getJobId())) {
		throw new IllegalStateException("server response does not match expected jobid: "+getJobId()+" != "+xjobSummary.getJobId());
	    } else {
		jobId = xjobSummary.getJobId();
	    }
	    
	    ownerId = null;
	    phase = null;
 	    quote = null;
	    startTime = null;
	    endTime = null;
	    executionDuration = null;
	    destruction = null;
	    parameters.clear();

	    String runId = xjobSummary.getRunId(); // Its a parameters, hold it til later...
	    ownerId = xjobSummary.getOwnerId();
	    ExecutionPhase xphase = xjobSummary.xgetPhase();
	    if (xphase != null)
		setPhase(xphase.getStringValue());
	    try {
		quote = xjobSummary.getQuote();
	    } catch (IllegalArgumentException ex) {
		if (logger.isLoggable(Level.INFO))
		    logger.log(Level.INFO, "quote value is not ISO 8601 compliant");

		XmlDateTime xdateTime = parseDateTime(xjobSummary.xgetQuote().toString());
		xjobSummary.xsetQuote(xdateTime);
		quote = xjobSummary.getQuote();
	    }
	    try {
		startTime = xjobSummary.getStartTime();
	    } catch (IllegalArgumentException ex) {
		if (logger.isLoggable(Level.INFO))
		    logger.log(Level.INFO, "startTime value is not ISO 8601 compliant");

		XmlDateTime xdateTime = parseDateTime(xjobSummary.xgetStartTime().toString());
		xjobSummary.xsetStartTime(xdateTime);
		startTime = xjobSummary.getStartTime();
	    }
	    try {
		endTime = xjobSummary.getEndTime();
	    } catch (IllegalArgumentException ex) {
		if (logger.isLoggable(Level.INFO))
		    logger.log(Level.INFO, "endTime value is not ISO 8601 compliant");

		XmlDateTime xdateTime = parseDateTime(xjobSummary.xgetEndTime().toString());
		xjobSummary.xsetEndTime(xdateTime);
		endTime = xjobSummary.getEndTime();
	    }
	    setExecutionDuration(Integer.valueOf(xjobSummary.getExecutionDuration()));
	    isNewExecutionDuration = false;
	    try {
		setDestruction(xjobSummary.getDestruction());
	    } catch (IllegalArgumentException ex) {
		if (logger.isLoggable(Level.INFO))
		    logger.log(Level.INFO, "destruction value is not ISO 8601 compliant");

		XmlDateTime xdateTime = parseDateTime(xjobSummary.xgetDestruction().toString());
		xjobSummary.xsetDestruction(xdateTime);
		setDestruction(xjobSummary.getDestruction());
	    }

	    isNewDestruction = false;
	    Parameters xparameters = xjobSummary.getParameters();
	    if (xparameters != null) {
		List<Parameter> xparameterList = xparameters.getParameterList();
		if (xparameterList != null) {
		    for (Parameter xparameter: xparameterList) {
			XmlCursor cursor = xparameter.newCursor();
			cursor.toFirstContentToken();
			String value = cursor.getChars();	
			cursor.dispose();
			setParameter(xparameter.getId(), value);
		    }
		}
		
		// Ok now check if the parameters have a runid, if not give them the runid from above.
		if (! parameters.containsKey("RUNID") && runId != null && ! runId.isEmpty()) {
		    setParameter("RUNID", runId);
		}
	    }
	    newParameters.clear();
	    this.inlineContent.clear();

	    Results xresults = xjobSummary.getResults();
	    Set<Result> newValues = new LinkedHashSet<Result>();
	    if (xresults != null) {
		List<ResultReference> xresultList = xresults.getResultList();
		if (xresultList != null) {
		    for (ResultReference xresult: xresultList) {
			Type type = xresult.xgetType();

			String xid = xresult.getId();
			String xtype = type != null ? type.getStringValue() : null;
			String xhref = xresult.getHref();
			if ("result".equals(xid) && (xhref == null || xhref.trim().isEmpty())) {
			    xhref = service.getBaseURL()+"/async/"+getJobId()+"/results/result";
			}

			newValues.add(new Result(xid, xtype, xhref));
		    }
		    
		    results.addAll(newValues);
		    results.retainAll(newValues);
		}
	    } else {
		results.clear();
	    }
	    
	    net.ivoa.xml.uws.v10.ErrorSummary xerrorSummary = xjobSummary.getErrorSummary();
	    if (xerrorSummary != null) {
		String errorMessage = xerrorSummary.getMessage();
		ErrorType xerrorType = xerrorSummary.xgetType();
		String errorType = (xerrorType != null ? xerrorType.getStringValue() : null);
		boolean errorDetailAvailable = xerrorSummary.getHasDetail();
		error = new Error(service, getJobId(), errorMessage, errorType, errorDetailAvailable);
	    }
	} catch (XMLStreamException ex) {
	    ex.printStackTrace();
	    throw new IOException(ex);
	} catch (XmlException ex) {
	    ex.printStackTrace();
	    throw ex;
	}
    }

    protected JobDocument parseJobDocument(InputStream inputStream) throws XMLStreamException, XmlException, IOException {
	XmlOptions jobOptions = new XmlOptions();

	// Namespace substitution
	Map<String,String> jobNamespaces = new HashMap<String,String>();
	jobNamespaces.put("http://www.ivoa.net/xml/UWS/v1.0rc3", "http://www.ivoa.net/xml/UWS/v1.0");
	jobOptions.setLoadSubstituteNamespaces(jobNamespaces);
	// Document element replacement
	//	    javax.xml.namespace.QName jobDocumentElement = new javax.xml.namespace.QName("http://www.ivoa.net/xml/UWS/v1.0", "job", "uws");
	//	    jobOptions.setLoadReplaceDocumentElement(jobDocumentElement);
	
	JobDocument xdocument = null;
	if (xmlInputFactory != null) {
	    XMLStreamReader xmlStreamReader = new StreamReaderDelegate(xmlInputFactory.createXMLStreamReader(inputStream)) {
		    public String getAttributeValue(int index) {
			String namespaceURI = getAttributeNamespace(index);
			String localName = getAttributeLocalName(index);
			return getAttributeValue(namespaceURI, localName);
		    }
		    public String getAttributeValue(String namespaceURI, String localName) {
			String value = super.getAttributeValue(namespaceURI, localName);
			return "type".equals(localName) ? value.toLowerCase() : value;
		    }
		};
	    
	    // Read past whitespace, comment, or processing instruction.
	    //   Having issues parsing xml-stylesheet processing instruction
	    xmlStreamReader.nextTag();
	    
	    xdocument = JobDocument.Factory.parse(xmlStreamReader, jobOptions);
	    
	} else {
	    xdocument = JobDocument.Factory.parse(inputStream, jobOptions);
	}
	return xdocument;
    }

    /**
     * Propagates parameter updates to the server and refreshes the state of this object with the server response.
     * @throws IOException if an error occurs with communicating with the server.
     */
    public void synchronize() throws HttpException, ResponseFormatException, IOException {
	if (job == null) {
	    job = new AsyncJob(service, getJobId());
	}

	try {
	    boolean isNewJobId = (jobId == null);
	    boolean isNewParameters = ! newParameters.isEmpty();
	    String phaseValue = phase; // hold the current value
	    
	    if (isNewJobId) {
		createJobOnServer();
	    } else if (isNewParameters) {
		updateParametersOnServer();
	    }
	    
	    if (isNewExecutionDuration) {
		updateExecutionDurationOnServer();
	    }
	    
	    if (isNewDestruction) {
		updateDestructionOnServer();
	    }
	    
	    if (phaseValue != null && ("RUN".equals(phaseValue) || "ABORT".equals(phaseValue))) {
		updateExecutionPhaseOnServer(phaseValue);
	    }
	    
	    if (! isNewJobId && ! isNewParameters && ! isNewExecutionDuration && ! isNewDestruction) {
		readJobFromServer();
	    }
	} catch (XmlException ex) {
	    throw new ResponseFormatException(ex);
	} finally {
	    populate(job);
	}
    }

    protected void createJobOnServer() throws HttpException, ResponseFormatException, IOException, XmlException {
	if (! newParameters.containsKey("REQUEST")) {
	    setParameter("REQUEST", "doQuery");
	}
	
	if (inlineContent.isEmpty()) {
	    job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async", newParameters));
	} else {
	    job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async", newParameters, this.inlineContent));
	}
    }

    protected void updateParametersOnServer() throws HttpException, ResponseFormatException, IOException, XmlException {
	if (inlineContent.isEmpty()) {
	    job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async/"+URLEncoder.encode(getJobId(), "UTF-8")+"/parameters", newParameters));
	} else {
	    job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async/"+URLEncoder.encode(getJobId(), "UTF-8")+"/parameters", newParameters, inlineContent));
	}
    }

    protected void updateExecutionDurationOnServer() throws HttpException, ResponseFormatException, IOException, XmlException {
	job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async/"+URLEncoder.encode(job.getJobId(), "UTF-8")+"/executionduration", Collections.singletonMap("EXECUTIONDURATION", getExecutionDuration().toString())));
    }

    protected void updateDestructionOnServer() throws HttpException, ResponseFormatException, IOException, XmlException {
	String destructionString = null;
	if (datatypeFactory != null) {
	    GregorianCalendar gregorianCalendar = new GregorianCalendar();
	    gregorianCalendar.setTimeInMillis(destruction.getTimeInMillis());
	    XMLGregorianCalendar xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar); 
	    destructionString = xmlGregorianCalendar.normalize().toString();
	} else {
	    destructionString = DatatypeConverter.printDateTime(getDestruction());
	}
	
	job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async/"+URLEncoder.encode(job.getJobId(), "UTF-8")+"/destruction", Collections.singletonMap("DESTRUCTION", destructionString)));
    }
	    
    protected void updateExecutionPhaseOnServer(String phaseValue) throws HttpException, ResponseFormatException, IOException, XmlException {
	job.handleJobSummaryResponse(HttpClient.post(service.getBaseURL()+"/async/"+URLEncoder.encode(job.getJobId(), "UTF-8")+"/phase", Collections.singletonMap("PHASE", phaseValue)));
    }   

    protected void readJobFromServer() throws HttpException, ResponseFormatException, IOException, XmlException {
	Map<String,String> emptyMap = Collections.emptyMap();
	job.handleJobSummaryResponse(HttpClient.get(service.getBaseURL()+"/async/"+URLEncoder.encode(job.getJobId(), "UTF-8"), emptyMap)); 
    }

    // Populates this object with the values for another AsyncJob object.
    void populate(AsyncJob job) {
	if (getJobId() == null)
	    jobId = job.getJobId();
	
	ownerId = job.getOwnerId();
	setPhase(job.getPhase());
	quote  = job.getQuote();
	startTime = job.getStartTime();
	endTime = job.getEndTime();
	setExecutionDuration(job.getExecutionDuration());
	isNewExecutionDuration = job.isNewExecutionDuration;
	setDestruction(job.getDestruction());
	isNewDestruction = job.isNewDestruction;
	parameters.clear();
	parameters.putAll(job.getParameters());
	newParameters.clear();
	newParameters.putAll(job.newParameters);
	results.addAll(job.getResults());
	results.retainAll(job.getResults());
	if (error == null) {
	    if (job.error != null) {
		error = new Error(service, job.getJobId(), job.error.getMessage(), job.error.getType(), job.error.isDetailedErrorAvailable());
	    } 
	} else {
	    if (job.error == null) {
		error.populate(new Error(service, job.getJobId(), null, null, false));
	    } else {
		error.populate(job.error);
	    }
	}
    }

    public void list(PrintStream output) {
	String jobId = getJobId();
	String ownerId = getOwnerId();
	String phase = getPhase();
	Calendar quote = getQuote();
	Calendar startTime = getStartTime();
	Calendar endTime = getEndTime();
	Integer executionDuration = getExecutionDuration();
	Calendar destruction = getDestruction();
	Map<String,String> parameters = getParameters();
	Set<Result> results = getResults();
	Error error = getError();
	
	if (jobId != null) {
	    output.println("Job id: "+jobId);
	}

	if (ownerId != null) {
	    output.println("Owner id: "+ownerId);
	}

	if (phase != null) {
	    output.println("Execution phase: "+phase);
	}

	if (quote != null) {
	    output.println("Quote: "+quote);
	}

	if (startTime != null) {
	    output.println("Start time: "+startTime);
	}

	if (endTime != null) {
	    output.println("End time: "+endTime);
	}

	if (executionDuration != null) {
	    output.println("Execution duration: "+executionDuration);
	}

	if (destruction != null) {
	    output.println("Destruction: "+destruction);
	}

	Set<Map.Entry<String,String>> entrySet = parameters.entrySet();
	Iterator<Map.Entry<String,String>> parametersIterator = entrySet.iterator();
	while (parametersIterator.hasNext()) {
	    Map.Entry<String,String> parameter = parametersIterator.next();
	    output.println("Parameter: "+parameter.getKey()+" = "+parameter.getValue());
	}
	
	Iterator<Result> resultsIterator = results.iterator();
	while (resultsIterator.hasNext()) {
	    Result result = resultsIterator.next();
	    output.println("Result: "+result.getId()+" = "+result.getHref());
	}

	if (error != null) {
	    if (error.getMessage() != null) 
		output.println("Error message: "+error.getMessage());
	    if (error.getType() != null) 
		output.println("Error type: "+error.getType());
	}
    }


    private XmlDateTime parseDateTime(String value) throws XmlException {
	XmlString xstring = XmlString.Factory.parse(value);
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
	ParsePosition parsePosition = new ParsePosition(0);
	Date date = dateFormat.parse(xstring.getStringValue(), parsePosition);
		
	XmlDateTime xdateTime = XmlDateTime.Factory.newInstance();
	xdateTime.setDateValue(date);
	return xdateTime;
    }
}
