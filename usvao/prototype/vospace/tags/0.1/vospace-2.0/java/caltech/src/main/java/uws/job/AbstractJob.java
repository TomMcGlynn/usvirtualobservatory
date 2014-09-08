package uws.job;

/*
 * This file is part of UWSLibrary.
 * 
 * UWSLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * UWSLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with UWSLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2011 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.io.IOException;
import java.io.OutputStream;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import uws.UWSException;
import uws.UWSToolBox;

import uws.job.manager.ExecutionManager;

import uws.job.serializer.UWSSerializer;

/**
 * <h3>Brief description</h3>
 * 
 * <p>Abstract implementation of a job of the UWS pattern. Only the methods which deal with the job execution are abstract.
 * So if you want a job executes a task you must extend {@link AbstractJob} and override:</p>
 * <ul>
 * 	<li>{@link #jobWork()}: the task which must be executed
 * 		(<i><u>note:</u> it is your responsibility to write the result(s) at the end of the work</i>).</li>
 * </ul>
 * 
 * <h3>Some attributes comments</h3>
 * 
 * <ul>
 * 	<li>
 * 		The job attributes <i>startTime</i> and <i>endTime</i> are automatically managed by {@link AbstractJob}. You don't have to do anything !
 * 		However you can customize the used date/time format thanks to the function {@link #setDateFormat(DateFormat)}. The default date/time format is:
 * 		<i>yyyy-MM-dd'T'HH:mm:ss.SSSZ</i>
 * 	</li>
 * 	<br />
 * 	<li>Once set, the <i>destruction</i> and the <i>executionDuration</i> attributes are automatically managed. That is to say:
 * 		<ul>
 * 			<li><u>if the destruction time is reached:</u> the job stops and it is destroyed by its job list</li>
 * 			<li><u>if the execution duration is elapsed:</u> the job stops and the phase is put to {@link ExecutionPhase#ABORTED ABORTED}.</li>
 * 		</ul>
 * 	</li>
 * 	<br />
 * 	<li>
 * 		<u>The <i>ownerID</i> attribute is set at the job creation and can not be changed after</u> ! If no owner ID is given at the job creation,
 * 		its default value is {@link #ANONYMOUS_OWNER} (=anonymous).
 * 	</li>
 * 	<br />
 * 	<li>
 * 		If your job is executable, do not forget to set the <i>quote</i> parameter
 * 		ONLY by using the {@link #setQuote(long)} method (a negative value or {@link #QUOTE_NOT_KNOWN} value
 * 		indicates the quote is not known ; {@link #QUOTE_NOT_KNOWN} is the default value).
 * 	</li>
 * </ul>
 * 
 * <h3>More details</h3>
 * 
 * <ul>
 * 	<li>
 * 		<b>{@link #generateJobId()}:</b>
 * 					This function is called at the construction of any {@link AbstractJob}. It allows to generate a unique job ID.
 * 					By default: time (in milliseconds) + a upper-case letter (A, B, C, ....).
 * 					<u>If you want customizing the job ID of your jobs</u>, you need to override this function.
 * 	</li>
 * 	<br />
 * 	<li>
 * 		<b>{@link #loadAdditionalParams()}:</b>
 * 					All parameters that are not managed by default are automatically stored in the job attribute {@link #additionalParameters} (a map).
 * 					However if you want manage yourself some or all of these additional parameters (i.e. task parameters), you must override this method.
 * 					<i>(By default nothing is done.)</i>
 * 	</li>
 * 	<br />
 * 	<li>
 * 		<b>{@link #clearResources()}:</b> 
 * 					This method is called <u>only at the destruction of the job</u>.
 * 					By default, the job is stopped (if running) and thread resources are freed.
 * 					However, it is your responsibility to delete results files
 * 					if you want their deletion at the job destruction. To do that, you must override this method,
 * 					call the super method and finally do what you want (i.e. delete all results files).
 * 	</li>
 * 	<br />
 * 	<li>
 * 		<b>{@link #publishExecutionError(UWSException)}:</b>
 * 					This method is called <u>when any {@link UWSException} is thrown from the {@link #jobWork()} method</u>.
 * 					By default, the method {@link UWSToolBox#publishErrorSummary(AbstractJob, String, ErrorType)} is called.
 * 					<i>This function can be overrided.</i>
 * 	</li>
 * 	<br />
 * 	<li>
 * 		<b>{@link #setPhaseManager(JobPhase)}:</b>
 * 					Lets customizing the default behaviors of all the execution phases for any job instance.
 * 					For more details see {@link JobPhase}.
 * 	</li>
 * 	<br />
 * 	<li>
 * 		<b>{@link #addObserver(JobObserver)}:</b>
 * 					An instance of any kind of AbstractJob can be observed by objects which implements {@link JobObserver} (i.e. {@link uws.service.AbstractUWS}).
 * 					Observers are notified at any change of the execution phase.
 * 	</li>
 * </ul>
 * 
 * @author	Gr&eacute;gory Mantelet (CDS)
 * @version	02/2011
 */
public abstract class AbstractJob extends SerializableUWSObject {
	private static final long serialVersionUID = 1L;
	
/* ********* */
/* CONSTANTS */
/* ********* */
	/** Name of the parameter <i>ACTION</i>. */
	public static final String PARAM_ACTION = "ACTION";
	
	/** Name of the DELETE action. */
	public static final String ACTION_DELETE = "DELETE";
	
	/** Name of the parameter <i>runId</i>. */
	public static final String PARAM_RUN_ID = "runid";
	
	/** Name of the parameter <i>owner</i>. */
	public static final String PARAM_OWNER = "owner";
	
	/** Name of the parameter <i>phase</i>. */
	public static final String PARAM_PHASE = "phase";
	
	/** Value of the parameter <i>phase</i> which starts the job. */
	public static final String PHASE_RUN = "RUN";

	/** Value of the parameter <i>phase</i> which aborts the job. */
	public static final String PHASE_ABORT = "ABORT";
	
	/** Name of the parameter <i>quote</i>. */
	public static final String PARAM_QUOTE = "quote";
	
	/** Name of the parameter <i>executionDuration</i>. */
	public static final String PARAM_EXECUTION_DURATION = "executionduration";
	
	/** Name of the parameter <i>destructionTime</i>. */
	public static final String PARAM_DESTRUCTION_TIME = "destruction";
	
	/** Name of the parameter <i>errorSummary</i>. */
	public static final String PARAM_ERROR_SUMMARY = "error";
	
	/** Name of the parameter <i>otherParameters</i>. */
	public static final String PARAM_PARAMETERS = "parameters";
	
	/** Name of the parameter <i>results</i>. */
	public static final String PARAM_RESULTS = "results";
	
	/** Default value of {@link #owner} if no ID are given at the job creation. */
	public final static String ANONYMOUS_OWNER = "anonymous";
	
	/** Default date format pattern.  */
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	/** The quote value that indicates the quote of this job is not known. */
	public static final long QUOTE_NOT_KNOWN = -1;
	
	/** The duration that implies an unlimited execution duration. */
	public final static long UNLIMITED_DURATION = 0;
	
/* ********* */
/* VARIABLES */
/* ********* */
	/** The last generated job ID. <b>It MUST be used ONLY by the function {@link #generateJobId()} !</b> */
	protected static String lastId = null;

	/** The identifier of the job (it MUST be different from any other job).<BR />
	 * <i><u>Note:</u> It is assigned automatically at the job creation in any job constructor
	 * by the function {@link #generateJobId()}.
	 * To change the way this ID is generated or its format you must override this function.</i> */
	protected final String jobId;
	
	/** The identifier of the creator of this job.<BR />
	 * <i><u>Note:</u> This object will not exist for all invocations of the UWS conformant protocol,
	 * but only in cases where the access to the service is authenticated.</i> */
	protected final String owner;

	/** The name/label that the job creator uses to identify this job.<BR />
	 * <i><u>Note:</u> this is distinct from the Job Identifier that the UWS system itself
	 * assigns to each job ({@link #jobId}). <u>It may not be unique !</u></i> */
	protected String runId = null;
	
	/**
	 * <p>The current phase of the job.</p>
	 * <i><u>Remember:</u> A job is treated as a state machine thanks to this attribute.
	 * <ul>
	 * 	<li>A successful job will normally progress through the {@link ExecutionPhase#PENDING PENDING},
	 * 		{@link ExecutionPhase#QUEUED QUEUED}, {@link ExecutionPhase#EXECUTING EXECUTING}, {@link ExecutionPhase#COMPLETED COMPLETED}
	 * 		phases in that order.</li>
	 * 	<li>At any time before the {@link ExecutionPhase#COMPLETED COMPLETED} phase a job may either be {@link ExecutionPhase#ABORTED ABORTED}
	 * 		or may suffer an {@link ExecutionPhase#ERROR ERROR}.</li>
	 * 	<li>If the UWS reports an {@link ExecutionPhase#UNKNOWN UNKNOWN} phase, then all the client can do is re-query the phase until a known phase is reported.</li>
	 * 	<li>A UWS may place a job in a {@link ExecutionPhase#HELD HELD} phase on receipt of a PHASE=RUN request it for some reason the job cannot be immediately queued
	 * 	- in this case it is the responsibility of the client to request PHASE=RUN again at some later time.</li>
	 * </ul></i>
	 */
	private JobPhase phase;
	
	/** The used date formatter. */
	protected DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	
	/** 
	 * This time (in seconds) predicts when the job is likely to complete.<br />
	 * <b>It CAN NOT be changed after the job creation !<br />
	 * <i>By default if no ID is given, {@link #quote} is set to {@link #QUOTE_NOT_KNOWN} (= {@value #QUOTE_NOT_KNOWN}).</i></b>
	 */
	private long quote = QUOTE_NOT_KNOWN;
	
	/** The time at which the job execution started. */
	private Date startTime = null;
	
	/** The time at which the job execution ended. */
	private Date endTime = null;
	
	/**
	 * <p>This is the duration (in seconds) for which the job shall run.</p>
	 * <i><u>Notes:</u>
	 * <ul>
	 * 	<li>An execution duration of 0 ({@link #UNLIMITED_DURATION}) implies unlimited execution duration.</li>
	 * 	<li>When a job is created, the service sets the initial execution duration.</li>
	 * 	<li>When the execution duration has been exceeded the service should automatically abort the job,
	 * 	which has the same effect as when a manual "Abort" is requested.</li>
	 * </ul></i> */
	private long executionDuration = UNLIMITED_DURATION;
	
	/** Timer for the execution duration.
	 * Once the duration since the execution beginning elapsed the job execution is aborted ({@link #stop()}). */
	private transient Timer timExecDuration;
	
	/** <p>This represents the instant when the job shall be destroyed.</p>
	 * <i><u>Notes:</u> Destroying a job implies:
	 * <ul>
	 * 	<li>if the job is still executing, the execution is aborted</li>
	 * 	<li>any results from the job are destroyed and storage reclaimed</li>
	 * 	<li>the service forgets that the job existed.</li>
	 * </ul>
	 * <p>The Destruction time should be viewed as a measure of the amount of time
	 * that a service is prepared to allocated storage for a job - typically this will be a longer duration
	 * that the amount of CPU time that a service would allocate.</p></i> */
	private Date destructionTime = null;
	
	/** <p>This error summary gives a human-readable error message for the underlying job.</p>
	 * <i><u>Note:</u> This object is intended to be a detailed error message, and consequently, 
	 * might be a large piece of text such as a stack trace.</i> */
	protected ErrorSummary errorSummary = null;
	
	/** This is an enumeration of the other Job parameters (given in POST queries). */
	protected Map<String, String> additionalParameters;
	
	/** This is a list of all results of this job. */
	protected List<Result> results;
	
        /** Any jobInfo that might be associated with the job. */
        private String jobInfo = null;

	/** The thread to start for executing the job. */
	protected transient JobThread thread = null;
	
	/** The time to wait the end of the thread after an interruption. */
	protected long waitForStop = 1000;
	
	/** This object, if not null, decides whether this job can start immediately or must be put in a queue. */
	private ExecutionManager executionManager = null;
	
	/** The jobs list which is supposed to managed this job. */
	private JobList<AbstractJob> myJobList = null;
	
	/** Objects which want to be notified at each modification of the execution phase of this job. */
	private Vector<JobObserver> observers = new Vector<JobObserver>();
	
	/** A variable to know if this job has just been de-serialized (syncStatus = null) or not (syncStatus = something else). */
	private transient String syncStatus = "SYNCHRONIZED";
	
/* ************ */
/* CONSTRUCTORS */
/* ************ */
	/**
	 * <p>Builds a job with a map of all its parameters (UWS and additional parameters).</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link AbstractJob#PARAM_PHASE} (</i>phase<i>) is given with the value {@link AbstractJob#PHASE_RUN}
	 * the job execution starts immediately after the Job construction.</i></p>
	 * 
	 * @param lstParam	Map<String,String> of parameters
	 * 					{key: name of the parameter <i>(see public final static fields of {@link AbstractJob}, for instance {@link AbstractJob#PARAM_RUN_ID})</i>
	 * 					, value: value of this parameter}.
	 * 
	 * @throws UWSException	If a parameter is incorrect.
	 * 
	 * @see AbstractJob#AbstractJob(String, Map)
	 */
	public AbstractJob(final Map<String, String> lstParam) throws UWSException {
		this(null, lstParam);
	}
	
	/**
	 * <p>Builds a job with the ID of the owner of this job and a map of all its parameters (UWS and additional parameters).</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link AbstractJob#PARAM_PHASE} (</i>phase<i>) is given with the value {@link AbstractJob#PHASE_RUN}
	 * the job execution starts immediately after the job construction.</i></p>
	 * 
	 * @param ownerID	Job.ownerID ({@link AbstractJob#PARAM_RUN_ID}).
	 * @param lstParam	Map<String,String> of parameters
	 * 					{key: name of the parameter <i>(see public final static fields of {@link AbstractJob}, for instance {@link AbstractJob#PARAM_RUN_ID})</i>
	 * 					, value: value of this parameter}.
	 * 
	 * @throws UWSException	If a parameter is incorrect.
	 * 
	 * @see AbstractJob#AbstractJob(String, String, long, Date, Map)
	 */
	protected AbstractJob(String ownerID, final Map<String, String> lstParam) throws UWSException {
		this(null, (ownerID!=null)?ownerID:((lstParam.containsKey(PARAM_OWNER))?lstParam.get(PARAM_OWNER):null), 0, null, lstParam);
	}
	
	/**
	 * <p>Builds a job with a job name/label, an owner ID, an execution duration, a destruction time
	 * and a map of all its parameters (UWS and additional parameters).</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link AbstractJob#PARAM_PHASE} (</i>phase<i>) is given with the value {@link AbstractJob#PHASE_RUN}
	 * the job execution starts immediately after the job construction.</i></p>
	 * 
	 * @param jobName		Job.runID ({@link AbstractJob#PARAM_RUN_ID}).
	 * @param ownerId		Job.ownerID ({@link AbstractJob#PARAM_OWNER}).
	 * @param maxDuration	Job.executionduration ({@link AbstractJob#PARAM_EXECUTION_DURATION}).
	 * @param destructTime	Job.destructiontime ({@link AbstractJob#PARAM_DESTRUCTION_TIME}).
	 * @param lstParam		Map<String,String> of parameters
	 * 						{key: name of the parameter <i>(see public final static fields of {@link AbstractJob}, for instance {@link AbstractJob#PARAM_RUN_ID})</i>
	 * 						, value: value of this parameter}.
	 * 
	 * @throws UWSException	If a parameter is incorrect.
	 * 
	 * @see JobPhase#JobPhase(AbstractJob)
	 * @see AbstractJob#setDestructionTime(Date)
	 * @see AbstractJob#loadDefaultParams(Map)
	 * @see AbstractJob#generateJobId()
	 */
	protected AbstractJob(String jobName, String ownerId, long maxDuration, Date destructTime, final Map<String, String> lstParam) throws UWSException {
		runId = jobName;
		owner = (ownerId==null)?ANONYMOUS_OWNER:ownerId;
		
		phase = new JobPhase(this);
		
		executionDuration = (maxDuration<0)?UNLIMITED_DURATION:maxDuration;
		
		setDestructionTime(destructTime);

		additionalParameters = new HashMap<String,String>();
		results = new Vector<Result>();
		
		Map<String,String> others = loadDefaultParams(lstParam);
		if (others != null)
			additionalParameters.putAll(others);
		
		jobId = generateJobId();
	}
	
	/**
	 * <p>Builds a job with a map of all its parameters (UWS and additional parameters).</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link AbstractJob#PARAM_PHASE} (</i>phase<i>) is given with the value {@link AbstractJob#PHASE_RUN}
	 * the job execution starts immediately after the Job construction.</i></p>
	 * 
	 * @param lstParam	Map<String,String> of parameters
	 * 					{key: name of the parameter <i>(see public final static fields of {@link AbstractJob}, for instance {@link AbstractJob#PARAM_RUN_ID})</i>
	 * 					, value: value of this parameter}.
	 * 
	 * @throws UWSException	If a parameter is incorrect.
	 * 
	 * @see AbstractJob#AbstractJob(String, Map)
	 */
	public AbstractJob(final String document) throws UWSException {
		this(null, document);
	}
	
	/**
	 * <p>Builds a job with the ID of the owner of this job and a map of all its parameters (UWS and additional parameters).</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link AbstractJob#PARAM_PHASE} (</i>phase<i>) is given with the value {@link AbstractJob#PHASE_RUN}
	 * the job execution starts immediately after the job construction.</i></p>
	 * 
	 * @param ownerID	Job.ownerID ({@link AbstractJob#PARAM_RUN_ID}).
	 * @param lstParam	Map<String,String> of parameters
	 * 					{key: name of the parameter <i>(see public final static fields of {@link AbstractJob}, for instance {@link AbstractJob#PARAM_RUN_ID})</i>
	 * 					, value: value of this parameter}.
	 * 
	 * @throws UWSException	If a parameter is incorrect.
	 * 
	 * @see AbstractJob#AbstractJob(String, String, long, Date, Map)
	 */
	protected AbstractJob(String ownerID, final String document) throws UWSException {
		this(null, (ownerID!=null)?ownerID:null, 0, null, document);
	}
	
	/**
	 * <p>Builds a job with a job name/label, an owner ID, an execution duration, a destruction time
	 * and a map of all its parameters (UWS and additional parameters).</p>
	 * 
	 * <p><i><u>Note:</u> if the parameter {@link AbstractJob#PARAM_PHASE} (</i>phase<i>) is given with the value {@link AbstractJob#PHASE_RUN}
	 * the job execution starts immediately after the job construction.</i></p>
	 * 
	 * @param jobName		Job.runID ({@link AbstractJob#PARAM_RUN_ID}).
	 * @param ownerId		Job.ownerID ({@link AbstractJob#PARAM_OWNER}).
	 * @param maxDuration	Job.executionduration ({@link AbstractJob#PARAM_EXECUTION_DURATION}).
	 * @param destructTime	Job.destructiontime ({@link AbstractJob#PARAM_DESTRUCTION_TIME}).
	 * @param lstParam		Map<String,String> of parameters
	 * 						{key: name of the parameter <i>(see public final static fields of {@link AbstractJob}, for instance {@link AbstractJob#PARAM_RUN_ID})</i>
	 * 						, value: value of this parameter}.
	 * 
	 * @throws UWSException	If a parameter is incorrect.
	 * 
	 * @see JobPhase#JobPhase(AbstractJob)
	 * @see AbstractJob#setDestructionTime(Date)
	 * @see AbstractJob#loadDefaultParams(Map)
	 * @see AbstractJob#generateJobId()
	 */
	protected AbstractJob(String jobName, String ownerId, long maxDuration, Date destructTime, final String document) throws UWSException {
		runId = jobName;
		owner = (ownerId==null)?ANONYMOUS_OWNER:ownerId;
		
		phase = new JobPhase(this);
		
		executionDuration = (maxDuration<0)?UNLIMITED_DURATION:maxDuration;
		
		setDestructionTime(destructTime);

		additionalParameters = new HashMap<String,String>();
		results = new Vector<Result>();
		
		jobInfo = document;

		jobId = generateJobId();
	}
	




	/**
	 * <p><b>MANUAL CONSTRUCTOR - <u>DISCOURAGED</u></b></p>
	 * 
	 * <p>Constructs a Job with <b>all</b> its parameters.
	 * It is the only constructor which lets setting/managing <b>manually</b> all the parameters/attributes of a Job.
	 * <u>No automatic process will be done as for the other constructors.</u></p>
	 * 
	 * <p><b><u>WARNINGS:</u>
	 * 	<ul><li>Whatever the given phase is, nothing will be done automatically !
	 * It is your responsibility to really start the job if needed, put the different results and error summaries.</li>
	 * 	<li>The parameters given in the map will be all saved as additional parameters and without any interpretation
	 * (no call to {@link AbstractJob#loadAdditionalParams() loadAdditionalParams()}).</li></b></p>
	 * 
	 * @param jobId				Job.jobID (The identifier of this job. It must be <b>unique</b>).
	 * @param jobName			Job.runID ({@link AbstractJob#PARAM_RUN_ID}).
	 * @param ownerId			Job.ownerID ({@link AbstractJob#PARAM_OWNER})
	 * @param phase				Job.phase (The current execution status of this job).
	 * @param startTime			Job.startTime (The time at which the job execution starts).
	 * @param endTime			Job.endTime (The time at which the job execution ends).
	 * @param maxDuration		Job.executionduration ({@link AbstractJob#PARAM_EXECUTION_DURATION}).
	 * @param destructTime		Job.destructiontime ({@link AbstractJob#PARAM_DESTRUCTION_TIME}).
	 * @param additionalParams	Map<String,String> of all additional parameters. 
	 * 
	 * @throws UWSException		If the given jobId is <i>null</i>.
	 */
	protected AbstractJob(String jobId, String jobName, String ownerId, ExecutionPhase phase, Date startTime, Date endTime,
						long maxDuration, Date destructTime, List<Result> results, ErrorSummary errors, final Map<String,String> additionalParams) throws UWSException {
		if (jobId == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create an AbstractJob] Impossible to build a Job with a NULL ID !");
		
		this.jobId = jobId;
		this.runId = jobName;
		this.owner = (ownerId==null)?ANONYMOUS_OWNER:ownerId;
		this.startTime = startTime;
		this.endTime = endTime;
		
		this.phase = new JobPhase(this);
		if (phase != null)
			setPhase(phase, true);
		
		this.executionDuration = maxDuration;
		
		setDestructionTime(destructTime);
		
		this.additionalParameters = new HashMap<String,String>();
		if (additionalParams != null)
			this.additionalParameters.putAll(additionalParams);

		this.results = new Vector<Result>();
		if (results != null)
			this.results.addAll(results);
		
		errorSummary = errors;
	}
	
	/**
	 * <p>This method is called by its job list just after the de-serialization of the whole UWS (this job included).
	 * It re-initializes the job in function of this last execution phase:</p>
	 * <ul>
	 * 	<li><b>{@link ExecutionPhase#EXECUTING EXECUTING} or {@link ExecutionPhase#QUEUED QUEUED}:</b> all results are deleted, the start/end time are reset,
	 * 																							all the timers are restarted (execution duration and destruction time)
	 * 																							and the execution phase is reset to {@link ExecutionPhase#PENDING PENDING}.</li>
	 * 	<li><b>Otherwise:</b> only the destruction timer is restarted if needed.</li>
	 * </ul>
	 * 
	 * <p><i><u>Note:</u> the variable {@link #syncStatus} is used to know whether the AbstractJob has been restored
	 * by a de-serialization. In this case it is equals to NULL.
	 * Otherwise it has another value (!= NULL ; by default: "SYNCHRONIZED").<br />
	 * <b>Thus, this method will do something ONLY IF the variable {@link #syncStatus} is NULL !</b></i></p>
	 * 
	 * @return	<i>true</i> if synchronized, <i>false</i> if the job must not exist any more in its jobList (so it MUST be destroyed)
	 * 			because of an error or if the destruction time is already reached.
	 */
	public final synchronized boolean sync(){
		if (syncStatus == null){
			// Re-Initialize the job if it was executing or queued:
			if (phase.getPhase() == ExecutionPhase.EXECUTING || phase.getPhase() == ExecutionPhase.QUEUED){
				// Re-Initialize the execution phase:
				try{
					setPhase(ExecutionPhase.PENDING, true);
				}catch(UWSException ex){
					ex.printStackTrace();
					return false;
				}
				
				// Clear resources (particularly if there is some files to delete):
				try{
					clearResources();
				}catch(Exception ex){
					ex.printStackTrace();
					return false;
				}
				
				// Clear errors, results and start+end times:
				errorSummary = null;
				results.clear();
				startTime = null;
				endTime = null;
			}
		}
		
		syncStatus = "SYNCHRONIZED";
		
		return true;
	}
	
	/**
	 * <p>This function lets generating a unique ID.</p>
	 * 
	 * <p><i><b>By default:</b> System.currentTimeMillis()+UpperCharacter (UpperCharacter: one upper-case character: A, B, C, ....)</i></p>
	 * 
	 * @return	A unique job identifier.
	 */
	protected String generateJobId(){
		String generatedId = System.currentTimeMillis()+"A";
		if (lastId != null){
			while(lastId.equals(generatedId))
				generatedId = generatedId.substring(0, generatedId.length()-1)+(char)(generatedId.charAt(generatedId.length()-1)+1);
		}
		lastId = generatedId;
		return generatedId;
	}
	
	/**
	 * <p>Loads the given parameters: all known parameters (with write access) are updated
	 * whereas others are returned in a new map in which all keys are in lower case.</p>
	 * 
	 * <p><b><u>Important:</u> The phase parameter is NEVER managed here and is ALWAYS added immediately in the additional parameters attribute !</b></p>
	 * 
	 * <p><i><u>Note:</u> UWS parameters with write access are:
	 * <ul>
	 * 	<li>{@link AbstractJob#PARAM_RUN_ID RUN_ID}</li>
	 * 	<li>{@link AbstractJob#PARAM_EXECUTION_DURATION EXECUTION_DURATION}</li>
	 * 	<li>{@link AbstractJob#PARAM_DESTRUCTION_TIME DESTRUCTION_TIME}</li>
	 * 	<li>{@link AbstractJob#PARAM_PHASE PHASE} if equals to {@link AbstractJob#PHASE_RUN} or {@link AbstractJob#PHASE_ABORT}</li>
	 * </ul></i></p>
	 * 
	 * <p><i><u>Note:</u> To check more DEFAULT parameters you just have to:
	 * <ol>
	 * 	<li>override the function {@link AbstractJob#loadDefaultParams(Map)}</li>
	 * 	<li>call super.loadParams(Map)</li>
	 * 	<li>add your own checking (do not forget to update the returned map and to return it).</li>
	 * </ol></i></p>
	 * 
	 * @param lstParam	The list of parameters to load <i>(UWS - included PHASE - and additional parameters)</i>.
	 * 
	 * @return			<ul>
	 * 						<li>a new map with all the parameters that have not been loaded <i>(additional parameters and/or not known UWS parameter and/or the PHASE parameter)</i></li> 
	 * 						<li>or an empty map</li>
	 * 						<li>or <i>null</i> if the job is executing or is ended <i>(actually: all phase except PENDING)</i></li>
	 * 					</ul>
	 * 
	 * @throws UWSException	If a given UWS parameter is not correct.
	 */
	protected Map<String, String> loadDefaultParams(final Map<String, String> lstParam) throws UWSException {		
		if (lstParam == null)
			return new HashMap<String,String>();

	// Build a new map in which all keys are in lower case:
		HashMap<String,String> params = new HashMap<String,String>();
		for(String key : lstParam.keySet())
			params.put(key.toLowerCase(), lstParam.get(key));
		
	// If NOT UPDATABLE return immediately just the phase parameter if any, otherwise return null:
		if (!phase.isJobUpdatable()){
			if (params.containsKey(PARAM_PHASE))
				additionalParameters.put(PARAM_PHASE, params.get(PARAM_PHASE));
			return null;
		}
		
	// Remove all UWS parameters with no write access:
		// OWNER:
		if (params.containsKey(PARAM_OWNER))
			params.remove(PARAM_OWNER);
		
		// PARAMETERS:
		if (params.containsKey(PARAM_PARAMETERS))
			params.remove(PARAM_PARAMETERS);
		
		// RESULTS:
		if (params.containsKey(PARAM_RESULTS))
			params.remove(PARAM_RESULTS);
		
		// ERROR SUMMARY:
		if (params.containsKey(PARAM_ERROR_SUMMARY))
			params.remove(PARAM_ERROR_SUMMARY);
		
		// QUOTE:
		if (params.containsKey(PARAM_QUOTE))
			params.remove(PARAM_QUOTE);
		
	// Manage all UWS parameters with write access:
		// RUN ID:
		if (params.containsKey(PARAM_RUN_ID)){
			setRunId(params.get(PARAM_RUN_ID));
			params.remove(PARAM_RUN_ID);
		}
		
		// EXECUTION DURATION:
		if (params.containsKey(PARAM_EXECUTION_DURATION)){
			try{
				setExecutionDuration(Long.parseLong(params.get(PARAM_EXECUTION_DURATION)));
				params.remove(PARAM_EXECUTION_DURATION);
			}catch(NumberFormatException ex){
				setExecutionDuration(0);
				throw new UWSException("[Load default params] The parameter \""+PARAM_EXECUTION_DURATION+"\" must be a long integer value.");
			}
		}
		
		// DESTRUCTION TIME:
		if (params.containsKey(PARAM_DESTRUCTION_TIME)){
			try {
				String time = params.get(PARAM_DESTRUCTION_TIME);
				if (time != null && time.trim().length() > 0)
					setDestructionTime(dateFormat.parse(time));
				params.remove(PARAM_DESTRUCTION_TIME);
			} catch (ParseException e) {
				throw new UWSException("[Load default params] The destruction time format is incorrect.");
			}
		}
		
		return params;
	}
	
	/**
	 * <p>Method called when updating one or several parameters using the functions {@link #addOrUpdateParameter(String, String)} and
	 * {@link #addOrUpdateParameters(Map)} or at the job creation.</p>
	 * 
	 * <p><b>It is useful if you need to check or to process all or a part of the additional parameters stored in {@link #additionalParameters}.</b></p>
	 * 
	 * <p><i><b>By default</b> this function does nothing and always return </i>true<i>.</i></p>
	 * 
	 * @return					<i>true</i> if all required additional parameters have been successfully updated, <i>false</i> otherwise.
	 * 
	 * @throws UWSException		If an error occurred during the updating of one parameter.
	 * 
	 * @see #addOrUpdateParameter(String, String)
	 * @see #addOrUpdateParameters(Map)
	 */
	protected boolean loadAdditionalParams() throws UWSException {
		return true;
	}
	
	/**
	 * <p>Looks for an additional parameters which corresponds to the Execution Phase. If it exists and:</p>
	 * <ul>
	 * 	<li> is equals to {@link AbstractJob#PHASE_RUN RUN} => remove it from the attribute {@link #additionalParameters} and start the job.</li>
	 * 	<li> is equals to {@link AbstractJob#PHASE_ABORT ABORT} => remove it from the attribute {@link #additionalParameters} and abort the job.</li>
	 * 	<li> is another value => the attribute stays in the attribute {@link #additionalParameters} and nothing is done.</li>
	 * </ul>
	 * 
	 * @throws UWSException	If it is impossible to change the Execution Phase.
	 * 
	 * @see #start()
	 * @see #abort()
	 */
	protected synchronized void applyPhaseParam() throws UWSException {
		if (getAdditionalParameterValue(PARAM_PHASE) != null){
			String newPhase = additionalParameters.get(PARAM_PHASE);
			if (newPhase.equalsIgnoreCase(PHASE_RUN)){
				additionalParameters.remove(PARAM_PHASE);
				start();
			}else if (newPhase.equalsIgnoreCase(PHASE_ABORT)){
				additionalParameters.remove(PARAM_PHASE);
				abort();
			}
		}
	}
	
/* ***************** */
/* GETTERS & SETTERS */
/* ***************** */
	/**
	 * Gets the phase in which this job is now.
	 * 
	 * @return The current phase of this job.
	 * 
	 * @see JobPhase#getPhase()
	 */
	public final ExecutionPhase getPhase() {
		return phase.getPhase();
	}

	/**
	 * <p>Sets the current phase of this job.</p>
	 * 
	 * <p><b><u>IMPORTANT:</u></b>
	 * <ul><li>The order of all phases must be respected:<i> BY DEFAULT</i> <BR /> {@link ExecutionPhase#PENDING PENDING} ---> {@link ExecutionPhase#QUEUED QUEUED} ---> {@link ExecutionPhase#EXECUTING EXECUTING} ---> {@link ExecutionPhase#COMPLETED COMPLETED}.</li>
	 * 	<li>The only way to go to the {@link ExecutionPhase#EXECUTING EXECUTING} phase is by sending a POST query with the value {@link AbstractJob#PHASE_RUN RUN} for the parameter {@link AbstractJob#PARAM_PHASE PHASE}.</li>
	 * 	<li>The only way to go to the {@link ExecutionPhase#ABORTED ABORTED} phase is by sending a POST query with the value {@link AbstractJob#PHASE_ABORT ABORT} for the parameter {@link AbstractJob#PARAM_PHASE PHASE}.</li>
	 * 	<li>The start time and the end time are set automatically when the phase is set to {@link ExecutionPhase#EXECUTING EXECUTING} and {@link ExecutionPhase#COMPLETED COMPLETED}, {@link ExecutionPhase#ABORTED ABORTED} or {@link ExecutionPhase#ERROR ERROR}</li>
	 *</ul></p>
	 *  
	 * @param p					The phase to set for this job.
	 * 
	 * @throws UWSException 	If the given phase does not respect the job's phases order.
	 * 
	 * @see #setPhase(ExecutionPhase, boolean)
	 */
	public final void setPhase(ExecutionPhase p) throws UWSException {
		setPhase(p, false);
	}
	
	/**
	 * <p>Sets the current phase of this job, respecting or not the imposed order.</p>
	 * 
	 * <p><b><u>IMPORTANT:</u></b>
	 * <ul><li><b><u>If the parameter <i>force</i> is <i>false</i></u></b>, the order of all phases must be respected:<BR /> {@link ExecutionPhase#PENDING PENDING} ---> {@link ExecutionPhase#QUEUED QUEUED} ---> {@link ExecutionPhase#EXECUTING EXECUTING} ---> {@link ExecutionPhase#COMPLETED COMPLETED}.</li>
	 * 	<li>The only way to go to the {@link ExecutionPhase#EXECUTING EXECUTING} phase is by sending a POST query with the value {@link AbstractJob#PHASE_RUN RUN} for the parameter {@link AbstractJob#PARAM_PHASE PARAM_PHASE}.</li>
	 * 	<li>The only way to go to the {@link ExecutionPhase#ABORTED ABORTED} phase is by sending a POST query with the value {@link AbstractJob#PHASE_ABORT ABORT} for the parameter {@link AbstractJob#PARAM_PHASE PARAM_PHASE}.</li>
	 * 	<li>The start time and the end time are set automatically when the phase is set to {@link ExecutionPhase#EXECUTING EXECUTING} and {@link ExecutionPhase#COMPLETED COMPLETED}, {@link ExecutionPhase#ABORTED ABORTED} or {@link ExecutionPhase#ERROR ERROR}</li>
	 *</ul></p>
	 *  
	 * @param p		 The phase to set for this job.
	 * @param force	<i>true</i> to impose the given execution phase, <i>false</i> to take into account the order of all phases.
	 * 
	 * @throws UWSException If the given phase does not respect the job's phases order.
	 * 
	 * @see JobPhase#setPhase(ExecutionPhase, boolean)
	 * @see JobPhase#isFinished()
	 * @see ExecutionManager#remove(AbstractJob)
	 * @see #notifyObservers(ExecutionPhase)
	 */
	public final void setPhase(ExecutionPhase p, boolean force) throws UWSException {
		ExecutionPhase oldPhase = phase.getPhase();
		phase.setPhase(p, force);
		
		// Notify the execution manager:
		if (phase.isFinished())
			executionManager.remove(this);

		// Notify all the observers:
		notifyObservers(oldPhase);
	}
	
	/**
	 * <p>Gets the phase manager of this job.</p>
	 * 
	 * <p><i><u>Note:</u> The phase manager manages all the transitions between all the execution phases.</i></p>
	 * 
	 * @return	Its phase manager.
	 */
	public final JobPhase getPhaseManager(){
		return phase;
	}
	
	/**
	 * <p>Sets the phase manager of this job.</p>
	 * 
	 * <p><i><u>Note:</u> The phase manager manages all the transitions between all the execution phases.</i></p>
	 * 
	 * @param jobPhase	Its new phase manager (if <i>null</i> this function does nothing).
	 */
	public final void setPhaseManager(JobPhase jobPhase){
		if (jobPhase != null)
			phase = jobPhase;
	}
	
	/**
	 * Gets the used date-time formatter. 
	 * 
	 * @return	The current date format.
	 */
	public final DateFormat getDateFormat(){
		return dateFormat;
	}
	
	/**
	 * Sets the date-time formatter to use.
	 * 
	 * @param newDateFormat	The date format which must be used from now (if <i>null</i> this function does nothing).
	 */
	public final void setDateFormat(DateFormat newDateFormat){
		if (newDateFormat != null)
			dateFormat = newDateFormat;
	}

	/**
	 * Gets the time at which the job execution has started.
	 * 
	 * @return The start time of the execution of this job.
	 */
	public final Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Sets the time at which the job execution has started.
	 * 
	 * @param newDateTime	The start time of the execution of this job.
	 */
	protected final void setStartTime(Date newDateTime){
		startTime = newDateTime;
	}

	/**
	 * Gets the time at which the job execution has finished.
	 * 
	 * @return The end time of the execution of this job.
	 */
	public final Date getEndTime() {
		return endTime;
	}
	
	/**
	 * Sets the time at which the job execution has finished.
	 * 
	 * @param newDateTime	The end time of the execution of this job.
	 */
	protected final void setEndTime(Date newDateTime){
		endTime = newDateTime;
		if (timExecDuration != null)
			timExecDuration.cancel();
	}

	/**
	 * Gets the duration (in seconds) for which this job shall run.
	 * 
	 * @return The execution duration of this job.
	 */
	public final long getExecutionDuration() {
		return executionDuration;
	}

	/**
	 * <p>Sets the duration (in seconds) for which this job shall run ONLY IF the job can updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).</p>
	 * 
	 * <p><i><u>Note:</u> A duration of 0 (or less) implies unlimited execution duration.</i></p>
	 * 
	 * @param executionDuration The execution duration of this job.
	 */
	public final void setExecutionDuration(long executionDuration) {
		if (phase.isJobUpdatable())
			this.executionDuration = (executionDuration<0)?UNLIMITED_DURATION:executionDuration;
	}

	/**
	 * Gets the instant when the job shall be destroyed.
	 * 
	 * @return The destruction time of this job.
	 */
	public final Date getDestructionTime() {
		return destructionTime;
	}

	/**
	 * <p>
	 * 	Sets the instant when the job shall be destroyed ONLY IF the job can updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).
	 * 	If known the jobs list is notify of this destruction time update.
	 * </p>
	 * 
	 * @param destructionTime The destruction time of this job.
	 * 
	 * @see JobPhase#isJobUpdatable()
	 * @see JobList#updateDestruction(AbstractJob)
	 */
	public final void setDestructionTime(Date destructionTime) {
		if (phase.isJobUpdatable() && (destructionTime == null || destructionTime.after(new Date()))){
			this.destructionTime = destructionTime;
			if (myJobList != null)
				myJobList.updateDestruction(this);
		}
	}

	/**
	 * Gets the error that occurs during the execution of this job.
	 * 
	 * @return A summary of the error.
	 */
	public final ErrorSummary getErrorSummary() {
		return errorSummary;
	}

	/**
	 * <p>Sets the error that occurs during the execution of this job.</p>
	 * 
	 * <p><b><u>IMPORTANT:</u> This function will have no effect if the job is finished, that is to say if the current phase is
	 * {@link ExecutionPhase#ABORTED ABORTED}, {@link ExecutionPhase#ERROR ERROR} or {@link ExecutionPhase#COMPLETED COMPLETED}.</i>.</b></p>
	 * 
	 * @param errorSummary	A summary of the error.
	 * 
	 * @throws UWSException	If the job execution is finished that is to say if the phase is ABORTED, ERROR or COMPLETED.
	 * 
	 * @see #isFinished()
	 */
	public final void setErrorSummary(ErrorSummary errorSummary) throws UWSException {
		if (!isFinished())
			this.errorSummary = errorSummary;
		else
			throw new UWSException(UWSException.BAD_REQUEST, "[Set an error] Impossible to set an error summary after the job execution (now, phase = "+phase+") !");
	}

	/**
	 * Gets the job info of this job.
	 * 
	 * @return The jobInfo
	 */
	public final String getJobInfo() {
		return jobInfo;
	}

	/**
	 * Gets the ID of this job (this ID <b>MUST</b> be unique).
	 * 
	 * @return The job ID (unique).
	 */
	public final String getJobId() {
		return jobId;
	}

	/**
	 * <p>Gets the RunID of this job given by the UWS user (presumed to be the owner of this job).
	 * This ID isn't the one used to access to this job thanks to the jobs list: it is more likely a label/name than an ID => it is not unique.</p>
	 * 
	 * <p><b><u>Warning:</u> This ID may be used by other jobs BUT their job id (cf {@link AbstractJob#getJobId()}) must be different.</b></p>
	 * 
	 * @return The Run ID (a kind of job name/label).
	 */
	public final String getRunId() {
		return runId;
	}
	
	/**
	 * <p>Sets the RunID of this job ONLY IF the job can updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).</p>
	 * 
	 * @param name	Its name/label.
	 * 
	 * @see JobPhase#isJobUpdatable()
	 */
	public final void setRunId(String name){
		if (phase.isJobUpdatable())
			runId = name;
	}

	/**
	 * Gets the ID of the owner of this job.
	 * 
	 * @return The owner ID.
	 */
	public final String getOwner() {
		return owner;
	}

	/**
	 * Get the quote attribute of this job.
	 *
	 * @return The estimated duration of the job execution (in seconds).
	 */
	public final long getQuote() {
		return quote;
	}
	
	/**
	 * <p>Sets the quote attribute of this job ONLY IF the job can updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).</p>
	 * 
	 * @param nbSeconds	The estimated duration of the job execution (in seconds).
	 * 
	 * @see JobPhase#isJobUpdatable()
	 */
	public final void setQuote(long nbSeconds){
		if (phase.isJobUpdatable())
			quote = nbSeconds;
	}

	/**
	 * Gets the list of parameters' name.
	 * 
	 * @return	The additional parameters of this job.
	 */
	public final Set<String> getAdditionalParameters(){
		return additionalParameters.keySet();
	}
	
	/**
	 * Gets the number of additional parameters.
	 * 
	 * @return	Number of additional parameters.
	 */
	public final int getNbAdditionalParameters(){
		return additionalParameters.size();
	}
	
	/**
	 * Gets the value of the specified additional parameter.
	 * 
	 * @param paramName	The name of the parameter whose the value is wanted.
	 * @return			The value of the specified parameter or <i>null</i> if it doesn't exist.
	 */
	public final String getAdditionalParameterValue(String paramName){
		return additionalParameters.get(paramName);
	}
	
	/**
	 * Adds or updates the specified parameter with the given value ONLY IF the job can be updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).
	 * 
	 * @param paramName		The name of the parameter to add or to update.
	 * @param paramValue	The (new) value of the specified parameter.
	 * 
	 * @return				<ul><li><i>true</i> if the parameter has been successfully added/updated,</li>
	 * 						<li><i>false</i> otherwise <i>(particularly if paramName=null or paramName="" or paramValue=null)</i>.</li></ul>
	 * 
	 * @throws UWSException	If a parameter value is incorrect.
	 * 
	 * @see JobPhase#isJobUpdatable()
	 * @see AbstractJob#addOrUpdateParameters(Map)
	 */
	public final boolean addOrUpdateParameter(String paramName, String paramValue) throws UWSException{
		if (phase.isJobUpdatable()){
			HashMap<String,String> param = new HashMap<String,String>();
			param.put(paramName, paramValue);
			return addOrUpdateParameters(param);
		}else
			return false;
	}
	
	/**
	 * <p>Adds or updates the given parameters ONLY IF the job can be updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).</p>
	 * 
	 * <p>Whatever is the result of {@link #loadDefaultParams(Map)} the method {@link #applyPhaseParam()} is called so that if there is an additional parameter {@link #PARAM_PHASE} with the value:
	 * <ul>
	 * 	<li>{@link AbstractJob#PHASE_RUN RUN} then the job is starting and the phase goes to {@link ExecutionPhase#EXECUTING EXECUTING}.</li>
	 * 	<li>{@link AbstractJob#PHASE_ABORT ABORT} then the job is aborting.</li>
	 * 	<li>otherwise the parameter {@link AbstractJob#PARAM_PHASE PARAM_PHASE} remains in the {@link AbstractJob#additionalParameters additionalParameters} list.</li>
	 * </ul></p>
	 * 
	 * @param params		A list of parameters to add/update.
	 * @return				<ul><li><i>true</i> if all the given parameters have been successfully added/updated,</li>
	 * 						<li><i>false</i> if some parameters have not been managed.</li></ul>
	 * 
	 * @throws UWSException	If a parameter value is incorrect.
	 * 
	 * @see #loadDefaultParams(Map)
	 * @see JobPhase#isJobUpdatable()
	 * @see #loadAdditionalParams()
	 * @see #applyPhaseParam()
	 */
	public boolean addOrUpdateParameters(Map<String,String> params) throws UWSException {
		boolean additionnalLoaded = false;
		
		// Load the default Job parameters:
		Map<String,String> remaining = loadDefaultParams(params);
		
		if (remaining != null){
			if (phase.isJobUpdatable()){
				// Save all remaining parameters:
				additionalParameters.putAll(remaining);
				
				// Load the additional parameters:
				additionnalLoaded = loadAdditionalParams();
			}
		}

		// Apply the retrieved phase:
		applyPhaseParam();
		
		return remaining == null || additionnalLoaded;
	}
	
	/**
	 * Removes the specified additional parameter ONLY IF the job can be updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).
	 * 
	 * @param paramName	The name of the parameter to remove.
	 * 
	 * @return	<i>true</i> if the parameter has been successfully removed, <i>false</i> otherwise.
	 * 
	 * @see JobPhase#isJobUpdatable()
	 */
	public final boolean removeAdditionalParameter(String paramName){
		if (!phase.isJobUpdatable() || paramName == null)
			return false;
		else
			return additionalParameters.remove(paramName.toLowerCase()) != null;
	}
	
	/**
	 * Clears the whole list of additional parameters ONLY IF the job can be updated (considering its current execution phase, see {@link JobPhase#isJobUpdatable()}).
	 * 
	 * @see JobPhase#isJobUpdatable()
	 */
	public final void removeAllAdditionalParameter(){
		if (phase.isJobUpdatable())
			additionalParameters.clear();
	}
	
	/**
	 * Gets the results list of this job.
	 * 
	 * @return An iterator on the results list.
	 */
	public final Iterator<Result> getResults() {
		return results.iterator();
	}
	
	/**
	 * Gets the total number of results.
	 * 
	 * @return	The number of results.
	 */
	public final int getNbResults(){
		return results.size();
	}
	
	/**
	 * <p>Adds the given result in the results list of this job.</p>
	 * 
	 * <p><b><u>IMPORTANT:</u> This function will throw an error if the job is finished.</b></p>
	 * 
	 * @param res			The result to add (<b>not null</b>).
	 * 
	 * @return				<i>true</i> if the result has been successfully added, <i>false</i> else.
	 * 
	 * @throws UWSException	If the job execution is finished that is to say if the phase is ABORTED, ERROR or COMPLETED.
	 * 
	 * @see #isFinished()
	 */
	public boolean addResult(Result res) throws UWSException {
		if (res == null)
			return false;
		else if (!isFinished())
			return results.add(res);
		else
			throw new UWSException(UWSException.BAD_REQUEST, "[Add a result] Impossible to add a result after the job execution (now, phase = "+phase+") !");
	}
	
	/**
	 * Gets the execution manager of this job, if any.
	 * 
	 * @return	Its execution manager (may be <i>null</i>).
	 */
	public final ExecutionManager getExecutionManager(){
		return executionManager;
	}
	
	/**
	 * Sets the execution manager of this job.
	 * 
	 * @param newManager		Its new execution manager (may be <i>null</i> ; in this case the job will always start immediately => no {@link ExecutionPhase#QUEUED QUEUED} phase).
	 * 
	 * @throws UWSException		If there is an error when removing this job from the former execution manager
	 * 							or when updating the new execution manager.
	 * 
	 * @see ExecutionManager#remove(AbstractJob)
	 * @see ExecutionManager#update(AbstractJob)
	 */
	public final void setExecutionManager(ExecutionManager newManager) throws UWSException {
		ExecutionManager oldManager = executionManager;		
		executionManager = newManager;

		if (oldManager != null)
			oldManager.remove(this);
		if (executionManager != null)
			executionManager.update(this);
	}
	
	/**
	 * Gets its jobs list, if known.
	 * 
	 * @return	Its jobs list (may be <i>null</i>).
	 */
	public final JobList<? extends AbstractJob> getJobList(){
		return myJobList;
	}
	
	/**
	 * <p>Sets its jobs list.</p>
	 * 
	 * <p>
	 * 		<i><u>Note:</u>
	 * 			If the given jobs list does not know this job, it will be added immediately (see {@link JobList#addNewJob(AbstractJob)}).
	 * 			Obviously, before all, we ensure that the job is removed from its former jobs list.
	 *		</i>
	 * </p>
	 * 
	 * @param jobList			Its new jobs list (may be <i>null</i>).
	 * 
	 * @throws UWSException		If an error occurs when adding this job in the given jobs list.
	 * 
	 * @see JobList#removeJob(String)
	 * @see JobList#getJob(String)
	 * @see JobList#addNewJob(AbstractJob)
	 */
	@SuppressWarnings("unchecked")
	public final void setJobList(JobList<? extends AbstractJob> jobList) throws UWSException {
		if (jobList != null && myJobList != null && jobList.equals(myJobList))
			return;
		
		if (myJobList != null && myJobList.getJob(getJobId()) != null)
			myJobList.removeJob(getJobId());
		
		myJobList = (JobList<AbstractJob>) jobList;
		
		try{
			if (myJobList != null && myJobList.getJob(getJobId()) == null)
				myJobList.addNewJob(this);
		}catch(UWSException ue){
			myJobList = null;
			throw ue;
		}
	}
	
/* ******************** */
/* EXECUTION MANAGEMENT */
/* ******************** */
	/**
	 * Gets the time to wait for the end of the thread after an interruption.
	 * 
	 * @return	The time to wait for the end of the thread  (a negative or null value means no wait for the end of the thread).
	 */
	public final long getTimeToWaitForEnd(){
		return waitForStop;
	}
	
	/**
	 * Sets the time to wait for the end of the thread after an interruption.
	 * 
	 * @param timeToWait	The new time to wait for the end of the thread (a negative or null value means no wait for the end of the thread).
	 */
	public final synchronized void setTimeToWaitForEnd(long timeToWait){
		waitForStop = timeToWait;
	}
	
	/**
	 * <p>Starts the job by using the execution manager if any.</p>
	 * 
	 * @throws UWSException
	 */
	public synchronized final void start() throws UWSException {
		start(executionManager != null);
	}
	
	/**
	 * <p>Starts the job.</p>
	 * 
	 * <p><i><u>Note:</u> This function does nothing if the job is already running !</i></p>
	 * 
	 * @param useManager	<i>true</i> to let the execution manager deciding whether the job starts immediately or whether it must be put in a queue until enough resources are available, <i>false</i> to start the execution immediately.
	 * 
	 * @throws UWSException	If there is an error while changing the execution phase or when starting the corresponding thread.
	 * 
	 * @see #isRunning()
	 * @see ExecutionManager#execute(AbstractJob)
	 * @see #setPhase(ExecutionPhase)
	 * @see #isFinished()
	 * @see #startTime
	 */
	public synchronized void start(boolean useManager) throws UWSException {
	// If already running do nothing:
		if (isRunning())
			return;
		
	// If asked propagate this request to the execution manager:
		else if (useManager && executionManager != null){
			executionManager.execute(this);
			
	// Otherwise start directly the execution:
		}else{
			// Try to change the phase:
			try{
				setPhase(ExecutionPhase.EXECUTING);
			}catch(UWSException ue){
				if (isFinished())
					throw new UWSException(UWSException.BAD_REQUEST, "[Start a job] The job \""+jobId+"\" has already been executed. It has finished with the phase \""+getPhase()+"\" !", ErrorType.TRANSIENT);
				else
					throw ue;
			}
			
			// Create and run its corresponding thread:
			thread = new JobThread(this);
			thread.start();
		
			// Set the start time:
			setStartTime(new Date());
			
			// Start the timer for the execution duration:
			if (executionDuration != UNLIMITED_DURATION && executionDuration > 0){
				timExecDuration = new Timer();
				timExecDuration.schedule(new DurationTimerTask(this), executionDuration*1000);
			}
		}
	}
	
	/**
	 * <p>Tells whether the job is still running.</p>
	 * 
	 * <p><i><u>Note:</u> This function tests the execution phase (see {@link JobPhase#isExecuting()}) AND the status of the thread (see {@link #isStopped()}).</i></p>
	 * 
	 * @return	<i>true</i> if the job is still running, <i>false</i> otherwise.
	 * 
	 * @see JobPhase#isExecuting()
	 * @see #isStopped()
	 */
	public final boolean isRunning(){
		return phase.isExecuting() && !isStopped();
	}
	
	/**
	 * <p>Tells whether the job is already finished (completed, aborted, error, ...).</p>
	 * 
	 * <p><i><u>Note:</u> This function test the execution phase (see {@link JobPhase#isFinished()}) AND the status of the thread (see {@link #isStopped()})</i></p>
	 * 
	 * @return	<i>true</i> if the job is finished, <i>false</i> otherwise.
	 * 
	 * @see JobPhase#isFinished()
	 * @see #isStopped()
	 */
	public final boolean isFinished(){
		return phase.isFinished() && isStopped();
	}
	
	/**
	 * <p>Stops immediately the job, sets its phase to {@link ExecutionPhase#ABORTED ABORTED} and sets its end time.</p>
	 * 
	 * <p><b><u>IMPORTANT:</u> If the thread does not stop immediately the phase and the end time are not modified. However it can be done by calling one more time {@link #abort()}.
	 * Besides you should check that you test regularly the interrupted flag of the thread in {@link #jobWork()} !</b></p>
	 * 
	 * @throws UWSException	If there is an error while changing the execution phase.
	 * 
	 * @see #stop()
	 * @see #isStopped()
	 * @see #setPhase(ExecutionPhase)
	 * @see #setEndTime(Date)
	 */
	public synchronized void abort() throws UWSException {
		// Interrupt the corresponding thread:
		stop();
		
		if (isStopped()){
			if (!phase.isFinished()){
				// Try to change the phase:
				setPhase(ExecutionPhase.ABORTED);
				
				// Set the end time:
				setEndTime(new Date());
			}else if (thread == null || (thread != null && !thread.isAlive()))
				throw new UWSException(UWSException.BAD_REQUEST, "[Abort a job] The job \""+jobId+"\" has already been executed. It has finished with the phase \""+getPhase()+"\" !", ErrorType.TRANSIENT);
		}
	}
	
	/**
	 * <p>Stops immediately the job, sets its error summary, sets its phase to {@link ExecutionPhase#ERROR} and sets its end time.</p>
	 * 
	 * <p><b><u>IMPORTANT:</u> If the thread does not stop immediately the phase, the error summary and the end time are not modified.
	 * However it can be done by calling one more time {@link #error(ErrorSummary)}.
	 * Besides you should check that you test regularly the interrupted flag of the thread in {@link #jobWork()} !</b></p>
	 * 
	 * @param error			The error that has interrupted this job.
	 * 
	 * @throws UWSException	If there is an error while setting the error summary or while changing the phase.
	 * 
	 * @see #stop()
	 * @see #isStopped()
	 * @see JobPhase#isFinished()
	 * @see #setErrorSummary(ErrorSummary)
	 * @see #setPhase(ExecutionPhase)
	 * @see #setEndTime(Date)
	 */
	public synchronized void error(ErrorSummary error) throws UWSException {
		// Interrupt the corresponding thread:
		stop();
			
		if (isStopped()){
			if (!phase.isFinished()){
				// Set the error summary:
				setErrorSummary(error);
				
				// Try to change phase:
				setPhase(ExecutionPhase.ERROR);
				
				// Set the end time:
				setEndTime(new Date());
			}else if (thread != null && !thread.isAlive())
				throw new UWSException(UWSException.BAD_REQUEST, "[Stop a job with error] The job \""+jobId+"\" has already been executed. It has finished with the phase \""+getPhase()+"\" !", ErrorType.TRANSIENT);
		}
	}
	
	/**
	 * <p>Publishes the given exception as an error summary.
	 * Doing that also stops the job, sets the phase to {@link ExecutionPhase#ERROR} and sets the end time.</p>
	 * 
	 * <p><i><u>Note:</u> This function only calls {@link UWSToolBox#publishErrorSummary(AbstractJob, String, ErrorType)}.
	 * An exception is thrown only if the publication has failed.</i></p>
	 * 
	 * @param ue				The exception that has interrupted this job.
	 * 
	 * @throws UWSException		If there is an error while publishing the given exception.
	 * 
	 * @see UWSToolBox#publishErrorSummary(AbstractJob, String, ErrorType)
	 */
	public synchronized void error(UWSException ue) throws UWSException {
		boolean published = UWSToolBox.publishErrorSummary(this, (ue.getCause() != null)?ue.getCause().getMessage():ue.getMessage(), ue.getUWSErrorType());
		if (!published)
			throw new UWSException("[Set an error] Impossible to set the given UWS exception to the job "+jobId+" !");
	}
	
	/**
	 * Stops the thread that executes the work of this job.
	 */
	protected synchronized void stop(){
		if (!isStopped()){
			// Interrupts the thread:
			thread.interrupt();
			
			// Wait a little for its end:
			if (waitForStop > 0){
				try{
					thread.join(waitForStop);
				}catch(InterruptedException ie){;}
			}
		}
	}
	
	/**
	 * Tells whether the thread is different from <i>null</i>, is not alive, is interrupted or is finished (see {@link JobThread#isFinished()}).
	 * 
	 * @return	<i>true</i> if the thread is not still running, <i>false</i> otherwise.
	 */
	protected synchronized final boolean isStopped(){
		return thread == null || !thread.isAlive() || thread.isInterrupted() || thread.isFinished();
	}
	
	/**
	 * <p>Does the job work <i>(i.e. making a long computation or executing a query on a Database)</i>.</p>
	 * 
	 * <p><b><u>Important:</u>
	 * <ul>
	 * 	<li>This method does the job work but it MUST also fill the associated job with the execution results and/or errors.</li>
	 * 	<li>Do not forget to check the interrupted flag of the thread ({@link Thread#isInterrupted()}) and then to send an {@link InterruptedException}.
	 * 		Otherwise the {@link AbstractJob#stop()} method will have no effect, as for {@link #abort()} and {@link #error(ErrorSummary)}.</li>
	 * </ul></b></p>
	 * 
	 * <p><i><u>Notes</u>:
	 * <ul>
	 * 	<li>The "setPhase(COMPLETED)" and the "endTime=new Date()" are automatically applied just after the call to jobWork.</li>
	 * 	<li>If an {@link UWSException} is thrown the {@link JobThread} will automatically publish the exception in this job
	 * 		thanks to the {@link AbstractJob#error(UWSException)} method or the {@link #setErrorSummary(ErrorSummary)} method,
	 * 		and so it will set its phase to {@link ExecutionPhase#ERROR}.</li>
	 * 	<li>If an {@link InterruptedException} is thrown the {@link JobThread} will automatically set the phase to {@link ExecutionPhase#ABORTED}</li>
	 * </ul></i></p>
	 * 
	 * @throws UWSException			If there is any kind of error which must be propagated.
	 * @throws InterruptedException	If the thread has been interrupted or if any method throws this exception.
	 */
	protected abstract void jobWork() throws UWSException, InterruptedException;
	
	/**
	 * <p>Stops the job if running, removes the job from the execution manager, stops the timer for the execution duration
	 * and may clear all files or any other resources associated to this job.</p>
	 * 
	 * <p><i>By default the job is aborted, only the {@link AbstractJob#thread} attribute is set to null and the timers are stopped; no other operations (i.e. clear result files and error files) is done.</i></p>
	 */
	public void clearResources(){
		if (isRunning()){
			try {
				abort();
			} catch (UWSException e) {
				stop();
			}
		}
		
		try{
			if (executionManager != null)
				executionManager.remove(this);
		}catch(UWSException ue){
			System.err.println("### [Clear Resources] ERROR While clearing resources of "+jobId+": ###");
			ue.printStackTrace();
		}
		
		if (timExecDuration != null)
			timExecDuration.cancel();
		thread = null;
		// Clear all results file !
		// Clear the error file !
	}
	
	/**
	 * Merely clears resources: it only calls {@link #clearResources()}.
	 * 
	 * @see #clearResources()
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		try{
			clearResources();
		}finally{
			super.finalize();
		}
	}
	
/* ******************* */
/* OBSERVER MANAGEMENT */
/* ******************* */
	/**
	 * Lets adding an observer of this job. The observer will be notified each time the execution phase changes.
	 * 
	 * @param observer	A new observer of this job.
	 * 
	 * @return			<i>true</i> if the given object has been successfully added as observer of this job, <i>false</i> otherwise.
	 */
	public final boolean addObserver(JobObserver observer){
		if (observer != null && !observers.contains(observer)){
			observers.add(observer);
			return true;
		}else
			return false;
	}
	
	/**
	 * Gets the total number of observers this job has.
	 * 
	 * @return	Number of its observers.
	 */
	public final int getNbObservers(){
		return observers.size();
	}
	
	/**
	 * Gets the observers of this job.
	 * 
	 * @return	An iterator on the list of its observers.
	 */
	public final Iterator<JobObserver> getObservers(){
		return observers.iterator();
	}
	
	/**
	 * Lets removing the given object from the list of observers of this job.
	 * 
	 * @param observer	The object which must not be considered as observer of this job.
	 * 
	 * @return			<i>true</i> if the given object is not any more an observer of this job, <i>false</i> otherwise.
	 */
	public final boolean removeObserver(JobObserver observer){
		return observers.remove(observer);
	}
	
	/**
	 * Lets removing all observers of this job.
	 */
	public final void removeAllObservers(){
		observers.clear();
	}
	
	/**
	 * Notifies all the observer of this job that its phase has changed.
	 * 
	 * @param oldPhase		The former phase of this job.
	 * @throws UWSException	If at least one observer can not have been updated.
	 */
	public final void notifyObservers(ExecutionPhase oldPhase) throws UWSException{
		int i=0;
		JobObserver observer = null;
		String errors = null;

		while(i<observers.size()){
			// Gets the observer:
			if (i==0 && observer == null)
				observer = observers.get(i);
			else if (observer.equals(observers.get(i))){
				i++;
				if (i<observers.size())
					observer = observers.get(i);
				else
					return;
			}
			// Update this observer:
			try{
				observer.update(this, oldPhase, getPhase());
			}catch(UWSException ex){
				if (errors == null) errors = "\t* "+ex.getMessage();
				else				errors += "\n\t* "+ex.getMessage();
			}
		}
		
		if (errors != null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Notify job observers] Some observers of \""+jobId+"\" can not have been updated:\n"+errors);
	}

/* **************** */
/* ERROR MANAGEMENT */
/* **************** */
	/**
	 * <p>Gets the error (if any) which has occurred during the job execution.</p>
	 * 
	 * <p><i><u>Note:</u> In the case an error summary can not have been published, the job has no error summary.
	 * However the last {@link UWSException} caught during the execution of a {@link JobThread} is saved and is available thanks to {@link JobThread#getError()}.
	 * In that case, the {@link AbstractJob#getWorkError() getWorkError()} method can be used to get back the occurred error.</i></p>
	 * 
	 * @return	The error which interrupts the thread or <i>null</i> if there was no error or if the job is still running.
	 */
	public final UWSException getWorkError(){
		return (thread==null && !thread.isAlive())?null:thread.getError();
	}
	
/* ************* */
/* SERIALIZATION */
/* ************* */
	@Override
	public String serialize(UWSSerializer serializer, String ownerId) throws UWSException {
		return serializer.getJob(this, true);
	}
	
	/**
	 * Serializes the specified attribute of this job by using the given serializer.
	 * 
	 * @param attributes		All the given attributes (may be <i>null</i> or empty).
	 * @param serializer		The serializer to use.
	 * 
	 * @return					The serialized job attribute (or the whole job if <i>attributes</i> is an empty array or is <i>null</i>).
	 * 
	 * @throws UWSException		If there is an error during the serialization.
	 * 
	 * @see UWSSerializer#getJob(AbstractJob, String[], boolean)
	 */
	public String serialize(String[] attributes, UWSSerializer serializer) throws UWSException {
		return serializer.getJob(this, attributes, true);
	}
	
	/**
	 * Serializes the specified attribute of this job in the given output stream by using the given serializer.
	 * 
	 * @param output			The output stream in which the job attribute must be serialized.
	 * @param attributes		The name of the attribute to serialize (if <i>null</i>, the whole job will be serialized).
	 * @param serializer		The serializer to use.
	 * 
	 * @throws UWSException		If there is an error during the serialization.
	 * 
	 * @see #serialize(String[], UWSSerializer)
	 */
	public void serialize(OutputStream output, String[] attributes, UWSSerializer serializer) throws UWSException {
		String attributeName = null;
		if (attributes==null || attributes.length <= 0)
			attributeName = "the job "+toString();
		else
			attributeName = "the given attribute \""+attributeName+"\" of {"+toString()+"}";
		
		if (output == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Serialize] Impossible to serialize "+attributeName+" because the given stream is null !");
		
		try{
			String serialization = serialize(attributes, serializer);
			if (serialization != null) {
				output.write(serialization.getBytes());
				output.flush();
			} else
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Serialize] Impossible to write the serialization of "+attributeName+" because it is NULL !");
		}catch(IOException ex){
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex, "[Serialize] Impossible to serialize "+attributeName+" !");
		}
	}
	
	@Override
	public String toString(){
		return "JOB {jobId: "+jobId+"; phase: "+phase+"; runId: "+runId+"; ownerId: "+owner+"; executionDuration: "+executionDuration+"; destructionTime: "+destructionTime+"; quote: "+quote+"; NbResults: "+results.size()+"; "+((errorSummary!=null)?errorSummary.toString():"No error")+" }";
	}

	/**
	 * <p>2 instances of AbstractJob are equals ONLY IF their ID are equals.</p>
	 * 
	 * <p><i><u>Note:</u> If the given object is not an AbstractJob, super.equals(..) is called.</i></p>
	 *  
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object anotherJob) {
		if (anotherJob instanceof AbstractJob)
			return jobId.equals(((AbstractJob)anotherJob).jobId);
		else
			return super.equals(anotherJob);
	}
	
	/**
	 * Task that the executionDuration timer of a job must trigger: only calls {@link AbstractJob#abort()}.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 02/2011
	 */
	protected static class DurationTimerTask extends TimerTask {
		private static final long serialVersionUID = 1L;
		
		protected final AbstractJob job;
		
		public DurationTimerTask(AbstractJob j){
			job = j;
		}
		
		public void run() {
			try {
				job.abort();
			} catch (UWSException e) {
				e.printStackTrace();
			}
		}

	}
	
/* ****************** */
/* DEPRECATED METHODS */
/* ****************** */
	/**
	 * Gets the list of parameters' name.
	 * 
	 * @return	The additional parameters of this job.
	 * 
	 * @deprecated Replaced by {@link #getAdditionalParameters()}
	 */
	@Deprecated
	public final Set<String> getOtherParameters(){
		return getAdditionalParameters();
	}
	
	/**
	 * Gets the number of additional parameters.
	 * 
	 * @return	Number of additional parameters.
	 * 
	 * @deprecated Replaced by {@link #getNbAdditionalParameters()}.
	 */
	@Deprecated
	public final int getNbOtherParameters(){
		return getNbAdditionalParameters();
	}
	
	/**
	 * Gets the value of the specified additional parameter.
	 * 
	 * @param paramName	The name of the parameter whose the value is wanted.
	 * @return			The value of the specified parameter or <i>null</i> if it doesn't exist.
	 * 
	 * @deprecated Replaced by {@link #getAdditionalParameterValue(String)}.
	 */
	@Deprecated
	public final String getOtherParameterValue(String paramName){
		return getAdditionalParameterValue(paramName);
	}
	
	/**
	 * Removes the specified additional parameter.
	 * 
	 * @param paramName	The name of the parameter to remove.
	 * 
	 * @return	<i>true</i> if the parameter has been successfully removed, <i>false</i> otherwise.
	 * 
	 * @deprecated Replaced by {@link #removeAdditionalParameter(String)}.
	 */
	@Deprecated
	public final boolean removeOtherParameter(String paramName){
		return removeAdditionalParameter(paramName);
	}
	
	/**
	 * Clears the whole list of additional parameters.
	 * 
	 * @deprecated Replaced by {@link #removeAllAdditionalParameter()}.
	 */
	@Deprecated
	public final void removeAllOtherParameter(){
		removeAllAdditionalParameter();
	}
	
	/**
	 * <p>Checks whether there is an additional parameters which corresponds to the Execution Phase. If it exists and:</p>
	 * <ul>
	 * 	<li> is equals to RUN => remove it from the attribute otherParameters and start the job.</li>
	 * 	<li> is equals to ABORT => remove it from the attribute otherParameters and abort the job.</li>
	 * 	<li> is another value => the attribute stays in the attribute otherParameters and nothing is done.</li>
	 * </ul>
	 * 
	 * @throws UWSException	If it is impossible to change the Execution Phase.
	 * 
	 * @deprecated {@link #applyPhaseParam()}.
	 */
	@Deprecated
	protected final void checkPhaseParam() throws UWSException {
		applyPhaseParam();
	}
	
	/**
	 * <p>Sets the phase to {@link ExecutionPhase#ERROR ERROR} and sets the error summary thanks to the given {@link UWSException}.</p>
	 * 
	 * <p><i><u>Note:</u>
	 * By default this method ONLY set the phase to {@link ExecutionPhase#ERROR ERROR} and write the message of the given exception in a new error summary.
	 * To change this behavior </i>(for instance writing an error file)<i> you need to override this function
	 * </i>(for instance you can call the function {@link UWSToolBox#publishErrorSummary(AbstractJob, Exception, ErrorType, java.net.URL, String, String)})</p>
	 * 
	 * @param ue	The {@link UWSException} to use to publish the error summary.
	 * 
	 * @return		<i>true</i> if the error has been successfully published, <i>false</i> otherwise.
	 * 
	 * @throws UWSException	It there is an error while setting the phase or setting the summary or writing the error file (if any).
	 * 
	 * @see UWSToolBox#publishErrorSummary(AbstractJob, String, ErrorType)
	 * 
	 * @deprecated Replaced by {@link #error(UWSException)}.
	 */
	@Deprecated
	protected final boolean publishExecutionError(UWSException ue) throws UWSException {
		try{
			error(ue);
			return true;
		}catch(UWSException ex){
			return false;
		}
	}
}
