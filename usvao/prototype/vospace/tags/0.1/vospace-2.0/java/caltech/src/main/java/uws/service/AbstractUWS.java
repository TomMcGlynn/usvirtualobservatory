package uws.service;

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

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import uws.AcceptHeader;
import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.ExecutionPhase;
import uws.job.JobList;
import uws.job.SerializableUWSObject;

import uws.job.manager.DefaultDestructionManager;
import uws.job.manager.DestructionManager;
import uws.job.manager.DefaultExecutionManager;
import uws.job.manager.ExecutionManager;

import uws.job.serializer.JSONSerializer;
import uws.job.serializer.UWSSerializer;
import uws.job.serializer.XMLSerializer;

import uws.service.actions.AddJob;
import uws.service.actions.DestroyJob;
import uws.service.actions.GetJobParam;
import uws.service.actions.JobSummary;
import uws.service.actions.ListJobs;
import uws.service.actions.SetJobParam;
import uws.service.actions.ShowHomePage;
import uws.service.actions.UWSAction;

import uws.service.controller.DestructionTimeController;
import uws.service.controller.ExecutionDurationController;

/**
 * <h3>General description</h3>
 * 
 * <p>An abstract facility to implement the <b>U</b>niversal <b>W</b>orker <b>S</b>ervice pattern.</p>
 * 
 * <p>It can manage several jobs lists (create new, get and remove).</p>
 * 
 * <p>It also interprets {@link HttpServletRequest}, applies the action specified in its given URL and parameters
 * <i>(according to the <a href="http://www.ivoa.net/Documents/UWS/20100210">IVOA Proposed Recommendation of 2010-02-10</a>)</i>
 * and returns the corresponding response in a {@link HttpServletResponse}.</p>
 * 
 * <h3>The UWS URL interpreter</h3>
 * 
 * <p>Any subclass of {@link AbstractUWS} has one object called the UWS URL interpreter. It is stored in the field {@link #urlInterpreter}.
 * It lets interpreting the URL of any received request. Thus you can know on which jobs list, job and/or job attribute(s)
 * the request applies.</p>
 * 
 * <p>This interpreter must be initialized with the base URL/URI of this UWS. By using the default constructor (the one with no parameter),
 * the URL interpreter will be built at the first request (see {@link UWSUrl#UWSUrl(HttpServletRequest)}) and so the base URI is
 * extracted directly from the request).</p>
 * 
 * <p>You want to set another base URI or to use a custom URL interpreter, you have to set yourself the interpreter
 * by using the method {@link #setUrlInterpreter(UWSUrl)}.</p>
 * 
 * <h3>Create a job</h3>
 * 
 * <p>The most important abstract function of this class is {@link AbstractUWS#createJob(Map)}. It allows to create an instance
 * of the type of job which is managed by this UWS. The only parameter is a map of a job attributes. It is the same map that
 * take the functions {@link AbstractJob#AbstractJob(Map)} and {@link AbstractJob#addOrUpdateParameters(Map)}.</p>
 * 
 * <p>There are two convenient implementations of this abstract method in {@link BasicUWS} and {@link ExtendedUWS}. These two implementations
 * are based on the Java Reflection.</p>
 *  
 * <h3>UWS actions</h3>
 * 
 * <p>All the actions described in the IVOA recommendation are already managed. Each of these actions are defined in
 * an instance of {@link UWSAction}:</p>
 * <ul>
 * 	<li>{@link UWSAction#LIST_JOBS LIST_JOBS}: see the class {@link ListJobs}</li>
 * 	<li>{@link UWSAction#ADD_JOB ADD_JOB}: see the class {@link AddJob}</li>
 * 	<li>{@link UWSAction#DESTROY_JOB DESTROY_JOB}: see the class {@link DestroyJob}</li>
 * 	<li>{@link UWSAction#JOB_SUMMARY JOB_SUMMARY}: see the class {@link JobSummary}</li>
 * 	<li>{@link UWSAction#GET_JOB_PARAM GET_JOB_PARAM}: see the class {@link GetJobParam}</li>
 * 	<li>{@link UWSAction#SET_JOB_PARAM SET_JOB_PARAM}: see the class {@link SetJobParam}</li>
 * 	<li>{@link UWSAction#HOME_PAGE HOME_PAGE}: see the class {@link ShowHomePage}</li>
 * </ul>
 * 
 * <p><b>However you can add your own UWS actions !</b> To do that you just need to implement the abstract class {@link UWSAction}
 * and to call the method {@link #addUWSAction(UWSAction)} with an instance of this implementation.</p>
 * 
 * <p><b><u>IMPORTANT:</u> You must be careful when you override the function {@link UWSAction#match(UWSUrl, String, HttpServletRequest)}
 * so that your test is as precise as possible ! Indeed the order in which the actions of a UWS are evaluated is very important !<br />
 * <u>If you want to be sure your custom UWS action is always evaluated before any other UWS action you can use the function
 * {@link #addUWSAction(int, UWSAction)} with 0 as first parameter !</u></b></p>
 * 
 * <p><i><u>Note:</u> You can also replace an existing UWS action thanks to the method {@link #replaceUWSAction(UWSAction)} or
 * {@link #setUWSAction(int, UWSAction)} !</i></p>
 * 
 * <h3>User identification</h3>
 * 
 * <p>Some UWS actions need to know the current user so that they can adapt their response (i.e. LIST_JOBS must return the jobs of only
 * one user: the current one). Thus, before executing a UWS action (and even before choosing the good action in function of the request)
 * the function {@link UserIdentifier#extractUserId(UWSUrl, HttpServletRequest)} is called. Its goal
 * is to identify the current user in function of the received request.</p>
 * 
 * <p>
 * 	<i><u>Notes:</u>
 * 		<ul>
 * 			<li>If this function returns NULL, the UWS actions must be executed on all jobs, whatever is their owner !</li>
 * 			<li>{@link UserIdentifier} is an interface. So you must implement it and then set its extension to this UWS
 * 				by using {@link #setUserIdentifier(UserIdentifier)}.</li>
 *		</ul>
 * 	</i></p>
 * </p>
 * 
 * <h3>Queue management</h3>
 * 
 * <p>One of the goals of a UWS is to manage an execution queue for all managed jobs. This task is given to an instance
 * of {@link DefaultExecutionManager}, stored in the field {@link #executionManager}. Each time a job is created,
 * the UWS sets it the execution manager (see {@link AddJob}). Thus the {@link AbstractJob#start()} method will ask to the manager
 * whether it can execute now or whether it must be put in a {@link ExecutionPhase#QUEUED QUEUED} phase until enough resources are available for its execution.</p>
 * 
 * <p>By extending the class {@link DefaultExecutionManager} and by overriding {@link DefaultExecutionManager#isReadyForExecution(AbstractJob)}
 * you can change the condition which puts a job in the {@link ExecutionPhase#EXECUTING EXECUTING} or in the {@link ExecutionPhase#QUEUED QUEUED} phase. By default, a job is put
 * in the {@link ExecutionPhase#QUEUED QUEUED} phase if there are more running jobs than a given number.</p>
 * 
 * <p>With this manager it is also possible to list all running jobs in addition of all queued jobs, thanks to the methods:
 * {@link DefaultExecutionManager#getRunningJobs()}, {@link DefaultExecutionManager#getQueuedJobs()}, {@link DefaultExecutionManager#getNbRunningJobs()}
 * and {@link DefaultExecutionManager#getNbQueuedJobs()}.</p>
 * 
 * <h3>Serializers & MIME types</h3>
 * 
 * <p>According to the IVOA recommendation, the XML format is the default format in which each UWS resource must be returned. However it
 * is told that other formats can also be managed. To allow that, {@link AbstractUWS} manages a list of {@link UWSSerializer} and
 * lets define which is the default one to use. <i>By default, there are two serializers: {@link XMLSerializer} (the default choice)
 * and {@link JSONSerializer}.</i></p>
 * 
 * <p>One proposed way to choose automatically the format to use is to look at the Accept header of a HTTP-Request. This header field is
 * a list of MIME types (each one with a quality - a sort of priority). Thus each {@link UWSSerializer} is associated with a MIME type so
 * that {@link AbstractUWS} can choose automatically the preferred format and so, the serializer to use.</p>
 * 
 * <p><b><u>WARNING:</u> Only one {@link UWSSerializer} can be associated with a given MIME type in an {@link AbstractUWS} instance !
 * Thus, if you add a {@link UWSSerializer} to a UWS, and this UWS has already a serializer for the same MIME type,
 * it will be replaced by the added one.</b></p>
 * 
 * <p><i><u>Note:</u> A XML document can be linked to a XSLT style-sheet. By using the method {@link XMLSerializer#setXSLTPath(String)}
 * you can define the path/URL of the XLST to link to each UWS resource. <br />
 * <u>Since the {@link XMLSerializer} is the default format for a UWS resource you can also use the function
 * {@link AbstractUWS#setXsltURL(String)} !</u></i></p>
 * 
 * <h3>The UWS Home page</h3>
 * 
 * <p>As for a job or a jobs list, a UWS is also a UWS resource. That's why it can also be serialized !</p>
 * 
 * <p>However in some cases it could more interesting to substitute this resource by a home page of the whole UWS by using the function:
 * {@link #setHomePage(String)} or {@link #setHomePage(URL, boolean)}.
 * </p>
 * 
 * <p><i><u>Note:</u> To go back to the UWS serialization (that is to say to abort a call to {@link #setHomePage(String)}),
 * use the method {@link #setDefaultHomePage()} !</i></p>
 * 
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 * 
 * @see AbstractJob
 * @see BasicUWS
 * @see ExtendedUWS
 */
public abstract class AbstractUWS<JL extends JobList<J>, J extends AbstractJob> extends SerializableUWSObject implements Iterable<JL>, HttpSessionBindingListener {
	private static final long serialVersionUID = 1L;

	/** Name of this UWS. */
	protected String name = null;
	
	/** Description of this UWS. */
	protected String description = null;
	
	/** List of all managed jobs lists. <i>(it is a LinkedHashMap so that jobs lists are ordered by insertion)</i> */
	protected final Map<String, JL> mapJobLists;
	
	/** The "interpreter" of UWS URLs. */
	protected UWSUrl urlInterpreter = null;
	
	/** Indicates whether the UWS URL interpreter must be re-initialize after each de-serialization. */
	protected boolean reInitUrlInterpreter = false;
	
	/** A variable to know if this UWS has just been de-serialized (syncStatus = null) or not (syncStatus = something else). */
	private transient String syncStatus = "SYNCHRONIZED";
	
	/** List of available serializers. */
	protected final Map<String, UWSSerializer> serializers;
	
	/** The MIME type of the default serialization format. */
	protected String defaultSerializer = null;
	
	/** The serializer choosen during the last call of {@link #executeRequest(HttpServletRequest, HttpServletResponse)}. */
	protected UWSSerializer choosenSerializer = null;
	
	/** URL of the home page. (<i>NULL if there is no home page</i>) */
	protected String homePage = null;
	
	/** Indicates whether the home page must be a copy or a redirection to the given URL. */
	protected boolean homeRedirection = false;
	
	/** Lets controlling the execution duration of all managed jobs. */
	private ExecutionDurationController durationController = null;
	
	/** Lets controlling the destruction time of all managed jobs. */
	private DestructionTimeController destructionController = null;
	
	/** List of UWS actions (i.e. to list jobs, to get a job, to set a job parameter, etc...). */
	protected final Vector<UWSAction<JL, J>> uwsActions;
	
	/** The action executed during the last call of {@link #executeRequest(HttpServletRequest, HttpServletResponse)}. */
	protected UWSAction<JL, J> executedAction = null;
	
	/** The object to use to extract the user ID from the received request. */
	protected UserIdentifier userIdentifier = null;
	
	/** Lets managing the automatic destruction of all managed jobs, whatever is their jobs list. */
	private DestructionManager destructionManager;

	/** Lets managing the execution queue and all the executing jobs. */
	private ExecutionManager executionManager;
	
	
/* ************ */
/* CONSTRUCTORS */
/* ************ */
	/**
	 * Builds a UWS (the base URI will be extracted at the first request directly from the request itself).
	 * 
	 * @see XMLSerializer
	 * @see JSONSerializer
	 * 
	 * @see ListJobs
	 * @see AddJob
	 * @see DestroyJob
	 * @see JobSummary
	 * @see GetJobParam
	 * @see SetJobParam
	 * @see ShowHomePage
	 */
	public AbstractUWS() {
		// Initialize the list of jobs:
		mapJobLists = new LinkedHashMap<String, JL>();
		
		// Initialize the list of available serializers:
		serializers = new HashMap<String, UWSSerializer>();
		addSerializer(new XMLSerializer());
		addSerializer(new JSONSerializer());
		
		// Initialize the list of UWS actions:
		uwsActions = new Vector<UWSAction<JL,J>>();
		
		// Load the default UWS actions:
		uwsActions.add(new ShowHomePage<JL, J>(this));
		uwsActions.add(new ListJobs<JL, J>(this));
		uwsActions.add(new AddJob<JL, J>(this));
		uwsActions.add(new DestroyJob<JL, J>(this));
		uwsActions.add(new JobSummary<JL, J>(this));
		uwsActions.add(new GetJobParam<JL, J>(this));
		uwsActions.add(new SetJobParam<JL, J>(this));
		
		// Set its destruction manager:
		destructionManager = new DefaultDestructionManager();
		
		// Set its execution manager:
		executionManager = new DefaultExecutionManager();
		
		// Set the executionDuration controller:
		durationController = new ExecutionDurationController();
		
		// Set the destructionTime controller:
		destructionController = new DestructionTimeController();
		
		reInitUrlInterpreter = true;
	}
	
	/**
	 * Builds a UWS with its base UWS URI.
	 * 
	 * @param baseURI		Base UWS URI.
	 * 
	 * @throws UWSException	If the given URI is <i>null</i> or empty.
	 * 
	 * @see UWSUrl#UWSUrl(String)
	 */
	protected AbstractUWS(String baseURI) throws UWSException {
		this();
		
		// Extract the name of the UWS:
		try{
			urlInterpreter = new UWSUrl(baseURI);
			name = urlInterpreter.getUWSName();
		}catch(UWSException ex){
			throw new UWSException(UWSException.BAD_REQUEST, ex, "[Create a UWS] Invalid base UWS URI !");
		}
	}
	
	/**
	 * Builds a UWS with the given UWS URL interpreter.
	 * 
	 * @param urlInterpreter	The UWS URL interpreter to use in this UWS.
	 */
	protected AbstractUWS(UWSUrl urlInterpreter) {
		this();
		setUrlInterpreter(urlInterpreter);
	}
	
	/**
	 * <p>This method is called just after a de-serialization of this UWS.
	 * It re-initializes all the jobs lists.</p>
	 * 
	 * @see JobList#sync()
	 */
	public final synchronized void sync(){
		if (reInitUrlInterpreter)
			urlInterpreter = null;
		
		if (syncStatus == null){
			destructionManager.refresh();
			executionManager.sync();
			
			for(JL jl : this)
				jl.sync();
		}	
		
		syncStatus = "SYNCHRONIZED";
	}
	
/* ***************** */
/* GETTERS & SETTERS */
/* ***************** */
	/**
	 * Gets the name of this UWS.
	 * 
	 * @return	Its name.
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this UWS.
	 * 
	 * @param name	Its new name.
	 */
	public final void setName(String name){
		this.name = name;
	}

	/**
	 * Gets the description of this UWS.
	 * 
	 * @return	Its description.
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this UWS.
	 * 
	 * @param description	Its new description.
	 */
	public final void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the base UWS URL.
	 * 
	 * @return	The base UWS URL.
	 * 
	 * @see UWSUrl#getBaseURI()
	 */
	public final String getBaseURI(){
		return (urlInterpreter == null) ? null : urlInterpreter.getBaseURI();
	}
	
	/**
	 * Gets the UWS URL interpreter of this UWS.
	 * 
	 * @return	Its UWS URL interpreter.
	 */
	public final UWSUrl getUrlInterpreter(){
		return urlInterpreter;
	}
	
	/**
	 * Sets the UWS URL interpreter to use in this UWS.
	 * 
	 * @param urlInterpreter	Its new UWS URL interpreter (may be <i>null</i>. In this case, it will be created from the next request ; see {@link #executeRequest(HttpServletRequest, HttpServletResponse)}).
	 */
	public final void setUrlInterpreter(UWSUrl urlInterpreter){
		this.urlInterpreter = urlInterpreter;
		reInitUrlInterpreter = false;
		if (name == null && urlInterpreter != null)
			name = urlInterpreter.getUWSName();
	}
	
	/**
	 * Gets the object which lets the UWS controlling the execution duration of all managed jobs.
	 * 
	 * @return	The used execution duration controller
	 */
	public final ExecutionDurationController getExecutionDurationController(){
		return durationController;
	}
	
	/**
	 * <p>Sets the object which lets the UWS controlling the execution duration of all managed jobs.</p>
	 * 
	 * <p><i><u>Note:</u> If the given controller is NULL, nothing is done.</i></p>
	 * 
	 * @param controller	Its new execution duration controller.
	 */
	public final void setExecutionDurationController(ExecutionDurationController controller){
		if (controller != null)
			durationController = controller;
	}
	
	/**
	 * Gets the object which lets the UWS controlling the destruction time of all managed jobs.
	 * 
	 * @return	The used destruction time controller.
	 */
	public final DestructionTimeController getDestructionTimeController(){
		return destructionController;
	}
	
	/**
	 * <p>Sets the object which lets the UWS controlling the destruction time of all managed jobs.</p>
	 * 
	 * <p><i><u>Note:</u> If the given controller is NULL, nothing is done.</i></p>
	 * 
	 * @param controller Its new destruction time controller.
	 */
	public final void setDestructionTimeController(DestructionTimeController controller){
		if (controller != null)
			destructionController = controller;
	}
	
	/**
	 * Gets the object which lets extracting the user ID from a received request.
	 * 
	 * @return	The used UserIdentifier (may be <i>null</i>).
	 */
	public final UserIdentifier getUserIdentifier(){
		return userIdentifier;
	}
	
	/**
	 * Sets the object which lets extracting the use ID from a received request.
	 * 
	 * @param identifier	The UserIdentifier to use (may be <i>null</i>).
	 */
	public final void setUserIdentifier(UserIdentifier identifier){
		userIdentifier = identifier;
	}
	
	/**
	 * Gets the destruction manager.
	 * 
	 * @return	Its destruction manager (may be <i>null</i>).
	 */
	public final DestructionManager getDestructionManager(){
		return destructionManager;
	}
	
	/**
	 * <p>Sets its destruction manager.</p>
	 * 
	 * <p><i><u>Note:</u> The given manager (even if NULL) is set for all managed jobs lists.</i></p>
	 * 
	 * @param newManager	Its new destruction manager.
	 * 
	 * @see JobList#setDestructionManager(DestructionManager)
	 */
	public final void setDestructionManager(DestructionManager newManager){
		destructionManager = newManager;
		for(JL jl : this)
			jl.setDestructionManager(destructionManager);
	}
	
	/**
	 * Gets the execution manager.
	 * 
	 * @return	Its execution manager (may be <i>null</i>).
	 */
	public final ExecutionManager getExecutionManager(){
		return executionManager;
	}
	
	/**
	 * <p>Sets its execution manager.</p>
	 * 
	 * <p><i><u>Note:</u> The execution manager is set for all managed jobs (whatever is their jobs list).</i></p>
	 * 
	 * @param newManager		Its new execution manager.
	 * 
	 * @throws UWSException		If the given manager is <i>null</i>
	 * 							or if there is an error while setting the new execution manager to a job.
	 * 
	 * @see AbstractJob#setExecutionManager(ExecutionManager)
	 */
	public final void setExecutionManager(ExecutionManager newManager) throws UWSException {
		if (newManager == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Set execution manager] Impossible to set a NULL execution manager !");
		
		executionManager = newManager;
		for(JL jl : this)
			for(J j : jl)
				j.setExecutionManager(executionManager);
	}
	
/* ******************** */
/* HOME PAGE MANAGEMENT */
/* ******************** */
	/**
	 * Gets the URL of the resource which must be used as home page of this UWS.
	 * 
	 * @return	The URL of the home page.
	 */
	public final String getHomePage(){
		return homePage;
	}
	
	/**
	 * Tells whether a redirection to the specified home page must be done or not.
	 * 
	 * @return	<i>true</i> if a redirection to the specified resource must be done
	 * 			or <i>false</i> to copy it.
	 */
	public final boolean isHomePageRedirection(){
		return homeRedirection;
	}
	
	/**
	 * Sets the URL of the resource which must be used as home page of this UWS.
	 * 
	 * @param homePageUrl	The URL of the home page (may be <i>null</i>).
	 * @param redirect		<i>true</i> if a redirection to the specified resource must be done
	 * 						or <i>false</i> to copy it.
	 */
	public final void setHomePage(URL homePageUrl, boolean redirect){
		homePage = homePageUrl.toString();
		homeRedirection = redirect;
	}
	
	/**
	 * <p>Sets the URI of the resource which must be used as home page of this UWS.</p>
	 * <i>A redirection will always be done on the specified resource.</i>
	 * 
	 * @param homePageURI	The URL of the home page.
	 */
	public final void setHomePage(String homePageURI){
		homePage = homePageURI;
		homeRedirection = true;
	}
	
	/**
	 * Indicates whether the current home page is the default one (the UWS serialization)
	 * or if it has been specified manually using {@link AbstractUWS#setHomePage(URL, boolean)}.
	 * 
	 * @return	<i>true</i> if it is the default home page, <i>false</i> otherwise.
	 */
	public final boolean isDefaultHomePage(){
		return homePage == null;
	}
	
	/**
	 * Forgets the home page specified by using {@link AbstractUWS#setHomePage(URL, boolean)} - if any -
	 * and go back to the default home page (XML format).
	 */
	public final void setDefaultHomePage(){
		homePage = null;
		homeRedirection = false;
	}
	
/* ********************** */
/* SERIALIZERS MANAGEMENT */
/* ********************** */
	/**
	 * Gets the MIME type of the serializer to use by default.
	 * 
	 * @return	The MIME type of the default serializer.
	 */
	public final String getDefaultSerializer(){
		return defaultSerializer;
	}
	
	/**
	 * Sets the MIME of the serializer to use by default.
	 * 
	 * @param mimeType		The MIME type (only one).
	 * 
	 * @throws UWSException If there is no serializer with this MIME type available in this UWS.
	 */
	public final void setDefaultSerializer(String mimeType) throws UWSException {
		if (serializers.containsKey(mimeType))
			defaultSerializer = mimeType;
		else
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Set default serializer] No UWS Serializer with the MIME type \""+mimeType+"\" exists in this UWS ! Please add a serializer with this MIME type before setting the default serializer of this UWS !");
	}
	
	/**
	 * <p>Adds a serializer to this UWS</p>
	 * 
	 * <p><b><u>WARNING:</u> If there is already a serializer with the same MIME type in this UWS ,
	 * it will be replaced by the given one !</b></p>
	 * 
	 * @param serializer	The serializer to add.
	 * 
	 * @return				<i>true</i> if the serializer has been successfully added, <i>false</i> otherwise.
	 * 
	 * @see UWSSerializer#getMimeType()
	 */
	public final boolean addSerializer(UWSSerializer serializer){
		if (serializer != null){
			if (!serializers.containsKey(serializer.getMimeType())){
				serializers.put(serializer.getMimeType(), serializer);
				if (serializers.size() == 1)
					defaultSerializer = serializer.getMimeType();
			}
		}
		return false;
	}
	
	/**
	 * Tells whether this UWS has already a serializer with the given MIME type.
	 * 
	 * @param mimeType	A MIME type (only one).
	 * 
	 * @return			<i>true</i> if a serializer exists with the given MIME type, <i>false</i> otherwise.
	 */
	public final boolean hasSerializerFor(String mimeType){
		return serializers.containsKey(mimeType);
	}
	
	/**
	 * Gets the total number of serializers available in this UWS.
	 * 
	 * @return	The number of its serializers.
	 */
	public final int getNbSerializers(){
		return serializers.size();
	}
	
	/**
	 * Gets an iterator of the list of all serializers available in this UWS.
	 * 
	 * @return	An iterator on its serializers.
	 */
	public final Iterator<UWSSerializer> getSerializers(){
		return serializers.values().iterator();
	}
	
	/**
	 * <p>Gets the serializer whose the MIME type is the same as the given one.</p>
	 * 
	 * <p><i><u>Note:</u> If this UWS has no corresponding serializer, its default one will be returned !</i></p>
	 * 
	 * @param mimeTypes		The MIME type of the searched serializer (may be more than one MIME types
	 * 						- comma separated ; see the format of the Accept header of a HTTP-Request).
	 * 
	 * @return				The corresponding serializer
	 * 						or the default serializer of this UWS if no corresponding serializer has been found.
	 * 
	 * @throws UWSException	If there is no corresponding serializer AND if the default serializer of this UWS can not be found.
	 * 
	 * @see AcceptHeader#AcceptHeader(String)
	 * @see AcceptHeader#getOrderedMimeTypes()
	 */
	public final UWSSerializer getSerializer(String mimeTypes) throws UWSException {
		choosenSerializer = null;
		
		if (mimeTypes != null){
			// Parse the given MIME types list:
			AcceptHeader accept = new AcceptHeader(mimeTypes);
			ArrayList<String> lstMimeTypes = accept.getOrderedMimeTypes();
			
			// Try each of them and stop at the first which match with an existing serializer:
			for(int i=0; choosenSerializer == null && i<lstMimeTypes.size(); i++)
				choosenSerializer = serializers.get(lstMimeTypes.get(i));
		}
		
		// If no serializer has been found for each given mime type, return the default one:
		if (choosenSerializer == null){
			choosenSerializer = serializers.get(defaultSerializer);
			if (choosenSerializer == null)
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get Serializer] No UWS Serializer available neither for \""+mimeTypes+"\" (given MIME types) nor \""+defaultSerializer+"\" (default serializer MIME type) !");
		}
		
		return choosenSerializer;
	}
	
	/**
	 * Gets the serializer choosen during the last call of {@link #getSerializer(String)}.
	 * 
	 * @return	The last used serializer.
	 */
	public final UWSSerializer getChoosenSerializer(){
		return choosenSerializer;
	}
	
	/**
	 * Removes the serializer whose the MIME type is the same as the given one.
	 * 
	 * @param mimeType	MIME type of the serializer to remove.
	 * 
	 * @return			The removed serializer
	 * 					or <i>null</i> if no corresponding serializer has been found.
	 */
	public final UWSSerializer removeSerializer(String mimeType){
		return serializers.remove(mimeType);
	}
	
	/**
	 * Gets the URL of the XSLT style-sheet that the XML serializer of this UWS is using.
	 * 
	 * @return	The used XSLT URL.
	 */
	public final String getXsltURL(){
		XMLSerializer serializer = (XMLSerializer)serializers.get(UWSSerializer.MIME_TYPE_XML);
		if (serializer != null)
			return serializer.getXSLTPath();
		return null;
	}
	
	/**
	 * Sets the URL of the XSLT style-sheet that the XML serializer of this UWS must use.
	 * 
	 * @param xsltPath	The new XSLT URL.
	 * 
	 * @return			<i>true</i> if the given path/url has been successfully set, <i>false</i> otherwise.
	 */
	public final boolean setXsltURL(String xsltPath){
		XMLSerializer serializer = (XMLSerializer)serializers.get(UWSSerializer.MIME_TYPE_XML);
		if (serializer != null){
			serializer.setXSLTPath(xsltPath);
			return true;
		}
		return false;
	}
	
/* ********************* */
/* JOBS LISTS MANAGEMENT */
/* ********************* */
	/**
	 * An iterator on the jobs lists list.
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public final Iterator<JL> iterator(){
		return mapJobLists.values().iterator();
	}
	
	/**
	 * Lets retrieve a jobs list in function of its name.
	 * 
	 * @param name	Name of the jobs list to get.
	 * 
	 * @return		The corresponding jobs list
	 * 				or <i>null</i> if there is no jobs list with the given name in this UWS.
	 */
	public final JL getJobList(String name){
		return mapJobLists.get(name);
	}
	
	/**
	 * Gets the number of managed jobs lists.
	 * 
	 * @return	The number of jobs lists.
	 */
	public final int getNbJobList(){
		return mapJobLists.size();
	}
	
	/**
	 * Adds a jobs list to this UWS.
	 * 
	 * @param jl	The jobs list to add.
	 * 
	 * @return		<i>true</i> if the jobs list has been successfully added or if a jobs list with this name already exists,
	 * 				<i>false</i> if the given jobs list is <i>null</i>.
	 * 
	 * @see JobList#setUWS(AbstractUWS)
	 */
	public final boolean addJobList(JL jl){
		if (jl == null)
			return false;
		else if (mapJobLists.containsKey(jl.getName()))
			return false;
		
		mapJobLists.put(jl.getName(), jl);
		jl.setUWS(this);
		return true;
	}
	
	/**
	 * Removes the specified jobs list.
	 * 
	 * @param name	Name of the jobs list to remove.
	 * 
	 * @return		The removed jobs list
	 * 				or <i>null</i> if no jobs list with the given name has been found.
	 * 
	 * @see #removeJobList(JobList)
	 */
	public final JL removeJobList(String name){
		JL jl = mapJobLists.get(name);
		if (jl != null){
			if (removeJobList(jl))
				return jl;
		}
		return null;
	}
	
	/**
	 * Removes the given jobs list from this UWS.
	 * 
	 * @param jl	The jobs list to remove.
	 * 
	 * @return		<i>true</i> if the jobs list has been successfully removed, <i>false</i> otherwise.
	 * 
	 * @see JobList#removeAll()
	 * @see JobList#setUWS(AbstractUWS)
	 */
	public boolean removeJobList(JL jl){
		if (jl == null)
			return false;
		
		jl = mapJobLists.remove(jl.getName());
		if (jl != null){
			jl.removeAll();
			jl.setUWS(null);
		}
		return jl != null;
	}
	
	/**
	 * Destroys the specified jobs list.
	 * 
	 * @param name	Name of the jobs list to destroy.
	 * 
	 * @return	<i>true</i> if the given jobs list has been destroyed, <i>false</i> otherwise.
	 * 
	 * @see #destroyJobList(JobList)
	 */
	public final boolean destroyJobList(String name){
		return destroyJobList(mapJobLists.get(name));
	}
	
	/**
	 * Destroys the given jobs list.
	 * 
	 * @param jl	The jobs list to destroy.
	 * 
	 * @return	<i>true</i> if the given jobs list has been destroyed, <i>false</i> otherwise.
	 * 
	 * @see JobList#clear()
	 * @see JobList#setUWS(AbstractUWS)
	 */
	public boolean destroyJobList(JL jl){
		if (jl == null)
			return false;
		
		jl = mapJobLists.remove(jl.getName());
		if (jl != null){
			jl.clear();
			jl.setUWS(null);
		}
		return jl != null;
	}
	
	/**
	 * Removes all managed jobs lists.
	 * 
	 * @see #removeJobList(String)
	 */
	public final void removeAllJobLists(){
		ArrayList<String> jlNames = new ArrayList<String>(mapJobLists.keySet());
		for(String jlName : jlNames)
			removeJobList(jlName);
	}
	
	/**
	 * Destroys all managed jobs lists.
	 * 
	 * @see #destroyJobList(String)
	 */
	public final void destroyAllJobLists(){
		ArrayList<String> jlNames = new ArrayList<String>(mapJobLists.keySet());
		for(String jlName : jlNames)
			destroyJobList(jlName);
	}
	
/* ********************** */
/* UWS ACTIONS MANAGEMENT */
/* ********************** */
	/**
	 * <p>Lets adding the given action to this UWS.</p>
	 * 
	 * <p><b><u>WARNING:</u> The action will be added at the end of the actions list of this UWS. That means, it will be evaluated (call of
	 * the method {@link UWSAction#match(UWSUrl, String, HttpServletRequest)}) lastly !</b></p>
	 * 
	 * @param action	The UWS action to add.
	 * 
	 * @return			<i>true</i> if the given action has been successfully added, <i>false</i> otherwise.
	 */
	public final boolean addUWSAction(UWSAction<JL, J> action){
		if (!uwsActions.contains(action))
			return uwsActions.add(action);
		else
			return false;
	}
	
	/**
	 * <p>Lets inserting the given action at the given position in the actions list of this UWS.</p>
	 * 
	 * @param indAction							The index where the given action must be inserted.
	 * @param action							The action to add.
	 * 
	 * @return									<i>true</i> if the given action has been successfully added, <i>false</i> otherwise.
	 * 
	 * @throws ArrayIndexOutOfBoundsException	If the given index is incorrect (index < 0 || index >= uwsActions.size()).
	 */
	public final boolean addUWSAction(int indAction, UWSAction<JL, J> action) throws ArrayIndexOutOfBoundsException {
		if (!uwsActions.contains(action)){
			uwsActions.add(indAction, action);
			return true;
		}
		return false;
	}
	
	/**
	 * Replaces the specified action by the given action.
	 * 
	 * @param indAction							Index of the action to replace.
	 * @param action							The replacer.
	 * 
	 * @return									<i>true</i> if the replacement has been a success, <i>false</i> otherwise.
	 * 
	 * @throws ArrayIndexOutOfBoundsException	If the index is incorrect (index < 0 || index >= uwsActions.size()).
	 */
	public final boolean setUWSAction(int indAction, UWSAction<JL, J> action) throws ArrayIndexOutOfBoundsException {
		if (!uwsActions.contains(action)){
			uwsActions.set(indAction, action);
			return true;
		}
		return false;
	}
	
	/**
	 * Replaces the action which has the same name that the given action.
	 * 
	 * @param action	The replacer.
	 * 
	 * @return			The replaced action
	 * 					or <i>null</i> if the given action is <i>null</i> 
	 * 									or if there is no action with the same name (in this case, the given action is not added).
	 */
	public final UWSAction<JL, J> replaceUWSAction(UWSAction<JL, J> action){
		if (action == null)
			return null;
		else{
			for(int i=0; i<uwsActions.size(); i++){
				if (uwsActions.get(i).equals(action))
					return uwsActions.set(i, action);
			}
			return null;
		}
	}
	
	/**
	 * Gets the number of actions this UWS has.
	 * 
	 * @return	The number of its actions.
	 */
	public final int getNbUWSActions(){
		return uwsActions.size();
	}
	
	/**
	 * Gets the action of this UWS which has the same name as the given one.
	 * 
	 * @param actionName	The name of the searched action.
	 * 
	 * @return				The corresponding action
	 * 						or <i>null</i> if there is no corresponding action.
	 */
	public final UWSAction<JL,J> getUWSAction(String actionName){
		for(int i=0; i<uwsActions.size(); i++){
			if (uwsActions.get(i).getName().equals(actionName))
				return uwsActions.get(i);
		}
		return null;
	}
	
	/**
	 * Gets all actions of this UWS.
	 * 
	 * @return	An iterator on its actions.
	 */
	public final Iterator<UWSAction<JL, J>> getUWSActions(){
		return uwsActions.iterator();
	}
	
	/**
	 * Gets the UWS action executed during the last call of {@link #executeRequest(HttpServletRequest, HttpServletResponse)}.
	 * 
	 * @return	The last used UWS action.
	 */
	public final UWSAction<JL,J> getExecutedAction(){
		return executedAction;
	}
	
	/**
	 * Removes the specified action from this UWS.
	 * 
	 * @param indAction							The index of the UWS action to remove.
	 * 
	 * @return									The removed action.
	 * 
	 * @throws ArrayIndexOutOfBoundsException	If the given index is incorrect (index < 0 || index >= uwsActions.size()).
	 */
	public final UWSAction<JL,J> removeUWSAction(int indAction) throws ArrayIndexOutOfBoundsException {
		return uwsActions.remove(indAction);
	}
	
	/**
	 * Removes the action of this UWS which has the same name as the given one.
	 * 
	 * @param actionName	The name of the UWS to remove.
	 * @return				The removed action
	 * 						or <i>null</i> if there is no corresponding action.
	 */
	public final UWSAction<JL,J> removeUWSAction(String actionName){
		for(int i=0; i<uwsActions.size(); i++){
			if (uwsActions.get(i).getName().equals(actionName))
				return uwsActions.remove(i);
		}
		return null;
	}
	
/* **************** */
/* ABSTRACT METHODS */
/* **************** */
	/**
	 * Creates a job of the type <i>(extension of {@link AbstractJob})</i> which parameterized this implementation of AbstractUWS.
	 * 
	 * @param parameters	The map of parameters to give to the constructor of {@link AbstractJob}.
	 * 
	 * @return				The created job.
	 * 
	 * @throws UWSException	If any error occurs while creating the job.
	 * 
	 * @see AddJob#apply(UWSUrl, String, HttpServletRequest, HttpServletResponse)
	 */
	public abstract J createJob(final Map<String,String> parameters) throws UWSException;

	/**
	 * Creates a job of the type <i>(extension of {@link AbstractJob})</i> which parameterized this implementation of AbstractUWS.
	 * 
	 * @param document	The description to give to the constructor of {@link AbstractJob}.
	 * 
	 * @return				The created job.
	 * 
	 * @throws UWSException	If any error occurs while creating the job.
	 * 
	 * @see AddJob#apply(UWSUrl, String, HttpServletRequest, HttpServletResponse)
	 */
	public abstract J createJob(String document) throws UWSException;


/* ********************** */
/* UWS MANAGEMENT METHODS */
/* ********************** */
	/**
	 * <p>Executes the given request according to the <a href="http://www.ivoa.net/Documents/UWS/20100210/">IVOA Proposed Recommendation of 2010-02-10</a>.
	 * The result is returned in the given response.</p>
	 * 
	 * <p>Here is the followed algorithm:</p>
	 * <ol>
	 * 	<li>Load the request in the UWS URL interpreter (see {@link UWSUrl#load(HttpServletRequest)})</li>
	 * 	<li>Extract the user ID (see {@link UserIdentifier#extractUserId(UWSUrl, HttpServletRequest)})</li>
	 * 	<li>Iterate - in order - on all available actions and apply the first which matches.
	 * 		(see {@link UWSAction#match(UWSUrl, String, HttpServletRequest)} and {@link UWSAction#apply(UWSUrl, String, HttpServletRequest, HttpServletResponse)})</li>
	 * </ol>
	 * 
	 * @param request		The UWS request.
	 * @param response		The response of this request which will be edited by the found UWS actions.
	 * 
	 * @return				<i>true</i> if the request has been executed successfully, <i>false</i> otherwise.
	 * 
	 * @throws UWSException	If no action matches or if any error has occurred while applying the found action.
	 * @throws IOException	If it is impossible to write in the given {@link HttpServletResponse}.
	 * 
	 * @see UWSUrl#UWSUrl(HttpServletRequest)
	 * @see UWSUrl#load(HttpServletRequest)
	 * @see UserIdentifier#extractUserId(UWSUrl, HttpServletRequest)
	 * @see UWSAction#match(UWSUrl, String, HttpServletRequest)
	 * @see UWSAction#apply(UWSUrl, String, HttpServletRequest, HttpServletResponse)
	 */
	public boolean executeRequest(HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
		if (request == null || response == null)
			return false;

		if (syncStatus == null)
			sync();
		
		boolean actionApplied = false;
		UWSAction<JL,J> action = null;
		
		try{
		// Update the UWS URL interpreter:
			if (urlInterpreter == null){
				boolean reInitNeeded = reInitUrlInterpreter;
				setUrlInterpreter(new UWSUrl(request));
				reInitUrlInterpreter = reInitNeeded;
			}
			urlInterpreter.load(request);
			
		// Extract the user ID:
			String userId = (userIdentifier == null)?null:userIdentifier.extractUserId(urlInterpreter, request);
			
		// Apply the appropriate UWS action:
			for(int i=0; action == null && i<uwsActions.size(); i++){
				if (uwsActions.get(i).match(urlInterpreter, userId, request)){
					action = uwsActions.get(i);
					choosenSerializer = null;
					actionApplied = action.apply(urlInterpreter, userId, request, response);
				}
			}
			
		// If no corresponding action has been found, throw an error:
			if (action == null)
				throw new UWSException(UWSException.NOT_IMPLEMENTED, "[Execute UWS request] This UWS action is not implemented by this UWS service !");
		
		}catch(UWSException ex){
			if (ex.getHttpErrorCode() == UWSException.SEE_OTHER)
				actionApplied = true;
			sendError(ex, request, response);
		}catch(Exception ex){
			sendError(ex, request, response);
		}finally{
			executedAction = action;
		}
		
		return actionApplied;
	}

	/**
	 * <p>Sends a redirection (with the HTTP status code 303) to the given URL/URI into the given response.</p>
	 * 
	 * @param url		The redirection URL/URI.
	 * @param request	The {@link HttpServletRequest} which may be used to make a redirection.
	 * @param response	The {@link HttpServletResponse} which must contain all information to make a redirection.
	 * 
	 * @throws IOException	If there is an error during the redirection.
	 * @throws UWSException	If there is any other error. 
	 */
	public void redirect(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, UWSException {
	    response.setStatus(HttpServletResponse.SC_SEE_OTHER);
	    response.setHeader("Location", url);
	    response.flushBuffer();
	    // response.sendRedirect(url);
	}
	
	/**
	 * <p>
	 * 	Fills the response with the given error. The HTTP status code is set in function of the error code of the given UWSException.
	 * 	If the error code is {@link UWSException#SEE_OTHER} this method calls {@link #redirect(String, HttpServletRequest, HttpServletResponse)}.
	 * 	Otherwise the function {@link HttpServletResponse#sendError(int, String)} is called.
	 * </p>
	 * 
	 * @param error			The error to send/display.
	 * @param request		The request which has caused the given error <i>(not used by default)</i>.
	 * @param response		The response in which the error must be published.
	 * 
	 * @throws IOException	If there is an error when calling {@link #redirect(String, HttpServletRequest, HttpServletResponse)} or {@link HttpServletResponse#sendError(int, String)}.
	 * @throws UWSException	If there is an error when calling {@link #redirect(String, HttpServletRequest, HttpServletResponse)}.
	 * 
	 * @see #redirect(String, HttpServletRequest, HttpServletResponse)
	 * @see HttpServletResponse#sendError(int, String)
	 */
	public void sendError(UWSException error, HttpServletRequest request, HttpServletResponse response) throws IOException, UWSException {
		if (error.getHttpErrorCode() == UWSException.SEE_OTHER)
			redirect(error.getMessage(), request, response);
		else
			response.sendError(error.getHttpErrorCode(), error.getMessage());
	}
	
	/**
	 * <p>
	 * 	Fills the response with the given error.
	 * 	The stack trace of the error is printed on the standard output and then the function
	 * 	{@link HttpServletResponse#sendError(int, String)} is called with the HTTP status code is {@link UWSException#INTERNAL_SERVER_ERROR}
	 * 	and the message of the given exception.
	 * </p>
	 * 
	 * 
	 * @param error			The error to send/display.
	 * @param request		The request which has caused the given error <i>(not used by default)</i>.
	 * @param response		The response in which the error must be published.
	 * 
	 * @throws IOException	If there is an error when calling {@link HttpServletResponse#sendError(int, String)}.
	 * @throws UWSException	
	 * 
	 * @see HttpServletResponse#sendError(int, String)
	 */
	public void sendError(Exception error, HttpServletRequest request, HttpServletResponse response) throws IOException, UWSException {
		error.printStackTrace();
		response.sendError(UWSException.INTERNAL_SERVER_ERROR, error.getMessage());
	}
	
	@Override
	public String serialize(UWSSerializer serializer, String ownerId) throws UWSException {
		return serializer.getUWS(this, ownerId);
	}
	
/* ***************************** */
/* HTTP_SESSION_BINDING_LISTENER */
/* ***************************** */
	/**
	 * Called by an {@link HttpSession} while adding an instance of {@link AbstractUWS} in its attributes list.
	 * 
	 * @see javax.servlet.http.HttpSessionBindingListener#valueBound(javax.servlet.http.HttpSessionBindingEvent)
	 */
	public void valueBound(HttpSessionBindingEvent e) {
		System.out.println("### [UWS INFO] : ADD THE UWS \""+getName()+"\" IN THE SESSION N°"+e.getSession().getId()+" ! ###");
	}

	/**
	 * Called by an {@link HttpSession} while removing an instance of {@link AbstractUWS} from its attributes list.
	 * By default, all managed jobs lists are destroyed and so all running jobs are stopped and destroyed.
	 * 
	 * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(javax.servlet.http.HttpSessionBindingEvent)
	 * @see AbstractUWS#removeAllJobLists()
	 */
	public void valueUnbound(HttpSessionBindingEvent e) {
		System.out.println("### [UWS INFO] : SESSION N°"+e.getSession().getId()+" EXPIRED => REMOVING THE UWS \""+getName()+"\" => DESTROYING ALL JOBS LISTS ! ###");
		destroyAllJobLists();
	}
	
}
