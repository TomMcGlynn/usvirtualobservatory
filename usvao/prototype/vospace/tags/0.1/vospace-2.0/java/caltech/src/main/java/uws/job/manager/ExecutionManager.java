package uws.job.manager;

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

import java.io.Serializable;
import java.util.Iterator;

import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.ExecutionPhase;

/**
 * <p>Lets managing the execution of a set of jobs.</p>
 * 
 * <p>
 * 	It is used by a job to decide whether it can be executed or whether it must be put in a queue.
 * 	This decision is done by the function {@link #isReadyForExecution(AbstractJob)} when {@link #execute(AbstractJob)} is called.
 * </p>
 * 
 * <p>
 * 	Besides the job must notify its manager when it is asked to start ({@link #execute(AbstractJob)})
 *  and to end ({@link #remove(AbstractJob)}).
 * </p>
 * 
 * <p><i><u>Note:</u> {@link #update(AbstractJob)} is used only when the job is changing its execution manager while executing.</i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public interface ExecutionManager extends Serializable {
	/**
	 * <p>This method is called just after a de-serialization.
	 * It resets all: just after a de-serialization there is no running jobs so all jobs lists are cleared.</p>
	 */
	public void sync();
	
	/**
	 * Gets the list of running jobs.
	 * 
	 * @return	An iterator on the running jobs.
	 */
	public Iterator<AbstractJob> getRunningJobs();
	
	/**
	 * Gets the total number of running jobs.
	 * 
	 * @return	The number of running jobs.
	 */
	public int getNbRunningJobs();
	
	/**
	 * Gets the list of queued jobs.
	 * 
	 * @return	An iterator on the queued jobs.
	 */
	public Iterator<AbstractJob> getQueuedJobs();
	
	/**
	 * Gets the total number of queued jobs.
	 * 
	 * @return	The number of queued jobs.
	 */
	public int getNbQueuedJobs();
	
	/**
	 * Lets indicating that no more jobs must be put in the queue.
	 * Consequently all current queued jobs have to start immediately.
	 */
	public void setNoQueue();
	
	/**
	 * Indicates whether this execution manager has a queue or not.
	 * 
	 * @return	<i>true</i> if this manager has a queue, <i>false</i> otherwise.
	 */
	public boolean hasQueue();
	
	/**
	 * Refreshes the lists of running and queued jobs. 
	 * 
	 * @throws UWSException		If there is an error while refreshing this manager.
	 */
	public void refresh() throws UWSException;
	
	/**
	 * <p>Lets deciding whether the given job can start immediately or whether it must be put in the queue.</p>
	 * 
	 * @param jobToExecute	The job to execute.
	 * @return				The resulting execution phase of the given job.
	 * 
	 * @throws UWSException	If there is an error while changing the execution phase of the given job or if any other error occurs.
	 * 
	 * @see AbstractJob#start(boolean)
	 * @see AbstractJob#setPhase(ExecutionPhase)
	 */
	public ExecutionPhase execute(AbstractJob jobToExecute) throws UWSException;
	
	/**
	 * <p>Tells whether the given job can start immediately its execution or whether it must be put in the queue.</p>
	 * 
	 * @param jobToExecute	The job to execute.
	 * 
	 * @return				<i>true</i> if the job can start immediately, <i>false</i> otherwise.
	 */
	public boolean isReadyForExecution(AbstractJob jobToExecute);
	
	/**
	 * <p>Puts the given job into the appropriate list of jobs: if {@link ExecutionPhase#EXECUTING EXECUTING}, the job must be put in the list of running jobs,
	 * but if {@link ExecutionPhase#QUEUED QUEUED}, it must be put into the queue. In another case, nothing is done except refreshing the list of running jobs.</p>
	 * 
	 * @param job				The job to "add" to this manager.
	 * 
	 * @throws UWSException		If there is an error while refreshing the list of running jobs or if any other error occurs.
	 */
	public void update(AbstractJob job) throws UWSException;
	
	/**
	 * Removes the job from this manager whatever is its current execution phase.
	 * 
	 * @param jobToRemove		The job to remove.
	 * 
	 * @throws UWSException		If there is an error while refreshing the list of running jobs or if any other error occurs.
	 */
	public void remove(AbstractJob jobToRemove) throws UWSException;
}
