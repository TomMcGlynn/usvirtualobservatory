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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import uws.UWSException;

import uws.job.manager.DefaultDestructionManager;
import uws.job.manager.DestructionManager;
import uws.job.serializer.UWSSerializer;

import uws.service.AbstractUWS;
import uws.service.UWSUrl;
import uws.service.UserIdentifier;

import edu.caltech.vao.vospace.meta.MetaStore;
import edu.caltech.vao.vospace.meta.MetaStoreFactory;

/**
 * <h3>General description</h3>
 * 
 * <p>An instance of this class lets listing UWS jobs (possible actions: get, add and remove).</p>
 * 
 * <p><i><u>Note:</u>This list implements the interface {@link Iterable} which lets you iterating more easily among the jobs.</i></p>
 * 
 * <h3>Jobs list by user</h3>
 * 
 * <p>So that avoiding any user to interact on jobs of anybody else, it is possible (and strongly encouraged)
 * to get the list of jobs only of the current user. For that you must use the function {@link #getJobs(String)} with a owner ID.</p>
 * 
 * <p>At each request sent to any instance of {@link AbstractUWS} the function {@link UserIdentifier#extractUserId(uws.service.UWSUrl, javax.servlet.http.HttpServletRequest)}
 * may extract the user ID. Thus the action GetJobList may know who is the current user. If the extracted owner ID is different from <i>null</i>
 * only the jobs of the current user will be returned.<br />
 * <b>However you need to find a way to identify for each UWS request the current user and then to override correctly the function
 * {@link UserIdentifier#extractUserId(uws.service.UWSUrl, javax.servlet.http.HttpServletRequest)}.</b></p>
 * 
 * <p><i><u>Note:</u> There are two special values for a owner ID:
 * 	<ul>
 * 		<li><b>NULL:</b> it is the only way to mean "all users"</li>
 * 		<li><b>{@link AbstractJob#ANONYMOUS_OWNER}:</b> the default value of an owner ID. It is set by default to a job if no owner is specified.</li>
 * 	</ul>
 * </i></p>
 * 
 * <h3>Searching in a jobs list</h3>
 * 
 * <ul>
 * 	<li><b>{@link #getJobs()} or {@link #iterator()}:</b> to get all the jobs of this jobs list.</li>
 * 	<li><b>{@link #getJob(String)}:</b> to get the job that has the given jobID.</li>
 * 	<li><b>{@link #searchJobs(String)}:</b> to search all the jobs that have the given runID.</li>
 * 	<li><b>{@link #getJobs(String)}: </b> to get the jobs of the specified user.</li>
 * </ul>
 * 
 * <h3>Get the users list</h3>
 * 
 * <p>If you are interested in (probably for some statistics or for an administrator) you can ask the list of users
 * who have at least one job in this jobs list ({@link #getUsers()}) and known how many they are ({@link #getNbUsers()}).</p>
 * 
 * <h3>Automatic job destruction</h3>
 * 
 * <p>
 * 	A job has a field - destructionTime - which indicates the date at which it must destroyed.
 * 	Remember that destroying a job consists in removing it from its jobs list and then clearing all its resources (result files, threads, ...).
 * 	This task is done by an "instance" of the interface {@link DestructionManager}. By default a jobs list has a default implementation of this interface: {@link DefaultDestructionManager}.
 * 	However when added in a UWS, the jobs list inherits the destruction manager of its UWS. Thus all jobs list of a UWS have the same destruction manager.
 * </p>
 * 
 * <p>
 * 	To use a custom destruction manager, you can use the method {@link #setDestructionManager(DestructionManager)}
 * 	if the jobs list is not managed by a UWS or {@link AbstractUWS#setDestructionManager(DestructionManager)} otherwise.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 * 
 * @see AbstractJob
 */
public class JobList<J extends AbstractJob> extends SerializableUWSObject implements Iterable<J> {
	private static final long serialVersionUID = 1L;
	
	/** <b>[Required]</b> Name of the jobs list. */
	private final String name;
	
	/** <b>[Required]</b> List of jobs. */
	protected final Map<String, J> jobsList;
	
	/** <b>[Required]</b> List of jobs per ownerId/userId. */
	protected final Map<String, Map<String, J>> ownerJobs;

	/** <b>[Optional]</b> The destruction manager to use to take into account the destructionTime field of contained jobs. */
	private DestructionManager destructionManager;
	
	/** <b>[Optional]</b> Useful only to get the URL of this job list. */
	@SuppressWarnings("unchecked")
	private AbstractUWS uws = null;

	/** Job store */
	private final MetaStore jobStore;
	
/* ************ */
/* CONSTRUCTORS */
/* ************ */
	/**
	 * <p>Builds a jobs list with its name.</p>
	 * 
	 * @param jobListName				The jobs list name.
	 * 
	 * @throws UWSException				If the given name is <i>null</i> or empty.
	 */
	public JobList(String jobListName) throws UWSException {		
		if (jobListName == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a jobs list] The given jobs list name is NULL !");
		else{
			jobListName = jobListName.trim();
			if (jobListName.length() == 0)
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a jobs list] The given jobs list name is empty !");
		}

		name = jobListName;
		jobsList = new LinkedHashMap<String,J>();
		ownerJobs = new LinkedHashMap<String, Map<String, J>>();
		
		destructionManager = new DefaultDestructionManager();
		
		try {
		    jobStore = MetaStoreFactory.getInstance().getMetaStore();
		} catch (Exception e) {
		    throw new UWSException(e);
		}

	}
	
	/**
	 * <p>This method is called by its UWS just after a de-serialization of the whole UWS (this jobs list included).
	 * It re-initializes all the jobs of this jobs list.</p>
	 * 
	 * <p>If a job can not be re-initialized ({@link AbstractJob#sync()} returns <i>false</i>)
	 * it is destroyed and then removed from this jobs list.</p>
	 * 
	 * @see DestructionManager#refresh()
	 * @see AbstractJob#sync()
	 * @see #destroyJob(String)
	 */
	public final synchronized void sync(){
		LinkedList<String> jobToDestroy = new LinkedList<String>();
		
		if (destructionManager != null)
			destructionManager.refresh();
		
		// Synchronized all jobs:
		for(J job : this){
			if (!job.sync())
				jobToDestroy.add(job.getJobId());
		}
		
		// Delete all non-synchronized jobs:
		for(String jobId : jobToDestroy)
			destroyJob(jobId);
	}
	
/* ******************* */
/* GETTERS AND SETTERS */
/* ******************* */
	/**
	 * Gets the UWS which manages this jobs list.
	 * 
	 * @return	Its UWS <i>(may be NULL)</i>.
	 */
	@SuppressWarnings("unchecked")
	public final AbstractUWS getUWS(){
		return uws;
	}
	
	/**
	 * Sets the UWS which aims to manage this jobs list.
	 * 
	 * @param newUws			Its new UWS (may be <i>null</i>).
	 */
	@SuppressWarnings("unchecked")
	public final void setUWS(AbstractUWS newUws) {
		uws = newUws;
		setDestructionManager((uws==null)?null:uws.getDestructionManager());
	}
	
	/**
	 * <p>Gets the used destruction manager.</p>
	 * 
	 * <p><i><u>Note:</u> Remember that the destruction manager lets destroying automatically jobs only when their destructionTime has been reached.</i></p>
	 * 
	 * @return	Its destruction manager (may be <i>null</i>).
	 */
	public final DestructionManager getDestructionManager(){
		return destructionManager;
	}
	
	/**
	 * <p>
	 * 	Sets the destruction manager to use.
	 * 	All the jobs are removed from the former destruction manager and then added in the new one.
	 * </p>
	 * 
	 * <p><i><u>Note:</u> Remember that the destruction manager lets destroying automatically jobs only when their destructionTime has been reached.</i></p>
	 * 
	 * @param newManager	Its new destruction manager (may be <i>null</i>).
	 * 
	 * @see DestructionManager#remove(AbstractJob)
	 * @see DestructionManager#update(AbstractJob)
	 */
	public final void setDestructionManager(DestructionManager newManager){
		DestructionManager oldManager = destructionManager;
		destructionManager = newManager;
		if (oldManager != null || destructionManager != null){
			for(J job : this){
				if (oldManager != null)
					oldManager.remove(job);
				if (destructionManager != null)
					destructionManager.update(job);
			}
		}
	}
	
	/**
	 * Gets the UWS URL of this jobs list in function of its UWS.
	 * 
	 * @return	Its corresponding UWSUrl.
	 * 
	 * @see AbstractUWS#getUrlInterpreter()
	 * @see UWSUrl#listJobs(String)
	 */
	public UWSUrl getUrl() {
		if (uws == null || uws.getUrlInterpreter() == null)
			return null;
		else
			return uws.getUrlInterpreter().listJobs(getName());
	}
	
	/**
	 * Gets the name of this jobs list.
	 * 
	 * @return	JobList name.
	 */
	public final String getName(){
		return name;
	}
	
	/**
	 * Gets the job whose the ID is given in parameter.
	 * 
	 * @param jobID	The ID of the job to get.
	 * 
	 * @return		The requested job or <i>null</i> if there is no job with the given ID.
	 */
	public final J getJob(String jobID){
		return jobsList.get(jobID);
	}
	
	/**
	 * Gets an iterator on the whole jobs list.
	 * 
	 * @return 	All jobs of this list.
	 * 
	 * @see #iterator()
	 */
	public final Iterator<J> getJobs(){
		return iterator();
	}

	/**
	 * Gets an iterator on the jobs list of the specified user.
	 * 
	 * @param ownerId	The ID of the owner/user (may be <i>null</i>).
	 * 
	 * @return 			An iterator on all jobs which have been created by the specified owner/user
	 * 					or a NullIterator if the specified owner/user has no job
	 * 					or an iterator on all the jobs if <i>ownerId</i> is <i>null</i>.
	 */
	public final Iterator<J> getJobs(String ownerId) {
		if (ownerId == null)
			return iterator();
		else{
			if (ownerJobs.containsKey(ownerId))
				return ownerJobs.get(ownerId).values().iterator();
			else
				return new Iterator<J>(){
					public boolean hasNext() { return false; }
					public J next() { return null; }
					public void remove() {;}
				};
		}
	}

	/**
	 * Gets an iterator on the jobs list.
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public final Iterator<J> iterator() {
		return jobsList.values().iterator();
	}
	
	/**
	 * Gets the number of jobs into this list.
	 * 
	 * @return	Number of jobs.
	 */
	public final int getNbJobs(){
		return jobsList.size();
	}
	
	/**
	 * Gets the number of jobs owned by the specified user into this list.
	 * 
	 * @param ownerId The ID of the owner/user (may be <i>null</i>).
	 * 
	 * @return	Number of jobs that the specified owner/user has,
	 * 			or the number of all jobs if <i>ownerId</i> is <i>null</i>.
	 */
	public final int getNbJobs(String ownerId){
		if (ownerId == null)
			return getNbJobs();
		else{
			if (ownerJobs.containsKey(ownerId))
				return ownerJobs.get(ownerId).size();
			else
				return 0;
		}
	}
	
	/**
	 * Gets all users that own at least one job in this list.
	 * 
	 * @return	An iterator on owner IDs.
	 */
	public final Iterator<String> getUsers(){
		return ownerJobs.keySet().iterator();
	}
	
	/**
	 * Gets the number of all users that have at least one job in this list.
	 * 
	 * @return	The number of job owners.
	 */
	public final int getNbUsers(){
		return ownerJobs.size();
	}

/* ********************** */
/* JOB MANAGEMENT METHODS */
/* ********************** */
	/**
	 * Gets all job whose the runID is equals (not case sensitive) to the given runID.
	 * 
	 * @param runID	The runID of jobs to search.
	 * 
	 * @return		All the corresponding jobs.
	 */
	public final ArrayList<J> searchJobs(String runID){
		ArrayList<J> foundJobs = new ArrayList<J>();
		runID = (runID != null)?runID.trim():runID;
		
		if (runID != null && runID.length() > 0){
			for(J job : this)
				if (job.getRunId().equalsIgnoreCase(runID))
					foundJobs.add(job);
		}
		
		return foundJobs;
	}
	
	/**
	 * <p>Add the given job to the list except if a job with the same jobID already exists.</p>
	 * 
	 * @param j	The job to add.
	 * 
	 * @return	The JobID if the job has been successfully added, <i>null</i> otherwise.
	 * 
	 * @see AbstractJob#loadAdditionalParams()
	 * @see AbstractJob#setJobList(JobList)
	 * @see DestructionManager#update(AbstractJob)
	 * @see AbstractJob#applyPhaseParam()
	 */
	public String addNewJob(J j) throws UWSException {
		if (j == null || jobsList.containsKey(j.getJobId())){
			return null;
		}else{
			// Load the additional parameters:
			j.loadAdditionalParams();
			// Add the job to the jobs list:
			jobsList.put(j.getJobId(), j);
			try {
			    UWSSerializer serializer = uws.getSerializer("application/xml");
			    jobStore.addJob(j.getJobId(), j.serialize(serializer, j.getOwner()));
			} catch (Exception e) {
			    throw new UWSException(e);
			}
			if (j.getOwner() != null){
				// Index also this job in function of its owner:
				if (!ownerJobs.containsKey(j.getOwner()))
					ownerJobs.put(j.getOwner(), new LinkedHashMap<String, J>());
				ownerJobs.get(j.getOwner()).put(j.getJobId(), j);
			}
			// Set its job list:
			j.setJobList(this);
			// Add it to the destruction manager:
			destructionManager.update(j);
			// Execute the job if asked in the additional parameters:
			j.applyPhaseParam();
			return j.getJobId();
		}
	}
	
	/**
	 * <p>Lets notifying the destruction manager of a possible modification of the destructionTime of the given job.</p>
	 * 
	 * <p><i><u>Note:</u> This method does nothing if this jobs list has no destruction manager, if the given job is NULL or if this jobs list does not know the given job.</i></p>
	 * 
	 * @param job	The job whose the destructionTime may have been modified.
	 * 
	 * @see DestructionManager#update(AbstractJob)
	 */
	public final void updateDestruction(J job){
		if (destructionManager != null && job != null && job.getJobList() != null && job.getJobList().equals(this))
			destructionManager.update(job);
	}
	
	/**
	 * <p>Lets removing (NOT DESTROYING) the specified job from this jobs list.</p>
	 * 
	 * @param jobId		The ID of the job to remove.
	 * 
	 * @return			The removed job or <i>null</i> if there is no job with the given jobID.
	 * 
	 * @see AbstractJob#setJobList(JobList)
	 * @see DestructionManager#remove(AbstractJob)
	 */
	public J removeJob(String jobId){
		// Remove the specified job:
		J removedJob = (jobId==null)?null:jobsList.remove(jobId);
		
		if (removedJob != null){
			// Delete completely their association:
			try{
				removedJob.setJobList(null);
			}catch(UWSException ue){ ; }
			
			// Clear its owner index:
			if (ownerJobs.containsKey(removedJob.getOwner())){
				ownerJobs.get(removedJob.getOwner()).remove(jobId);
				if (ownerJobs.get(removedJob.getOwner()).isEmpty())
					ownerJobs.remove(removedJob.getOwner());
			}
			
			// Remove it from the destruction manager:
			if (destructionManager != null)
				destructionManager.remove(removedJob);
			return removedJob;
		}else
			return null;
	}
	
	/**
	 * Removes the job from the list and deletes all its attached resources ({@link AbstractJob#clearResources()}.
	 * 
	 * @param jobId	The ID of the job to destroy.
	 * 
	 * @return		<i>true</i> if it has been successfully destroyed, <i>false</i> otherwise.
	 * 
	 * @see #removeJob(String)
	 * @see AbstractJob#clearResources()
	 */
	public boolean destroyJob(String jobId){
		// Remove the job:
		J destroyedJob = removeJob(jobId);
		
		if (destroyedJob != null){
			// Clear associated resources:
			destroyedJob.clearResources();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Removes all jobs of this list.
	 * 
	 * @see #removeJob(String)
	 */
	public void removeAll(){
		ArrayList<String> jobIDs = new ArrayList<String>(jobsList.keySet());
		for(String id : jobIDs)
			removeJob(id);
	}
	
	/**
	 * Destroys all jobs of this list.
	 * 
	 * @see #destroyJob(String)
	 */
	public void clear(){
		ArrayList<String> jobIDs = new ArrayList<String>(jobsList.keySet());
		for(String id : jobIDs)
			destroyJob(id);
	}
	
	/**
	 * Destroys all jobs owned by the specified user.
	 * 
	 * @param ownerId The ID of the owner/user.
	 * 
	 * @see #clear()
	 * @see #destroyJob(String)
	 */
	public void clear(String ownerId){
		if (ownerId == null)
			clear();
		else{
			if (ownerJobs.containsKey(ownerId)){
				ArrayList<String> jobIDs = new ArrayList<String>(ownerJobs.get(ownerId).keySet());
				for(String id : jobIDs)
					destroyJob(id);
				ownerJobs.remove(ownerId);
			}
		}
	}
	
/* ***************** */
/* INHERITED METHODS */
/* ***************** */
	@Override
	public String serialize(UWSSerializer serializer, String ownerId) throws UWSException {
		return serializer.getJobList(this, ownerId, true);
	}

	@Override
	public String toString(){
		return "JOB_LIST {name: \""+getName()+"\"; nbJobs: "+jobsList.size()+"}";
	}
	
}

