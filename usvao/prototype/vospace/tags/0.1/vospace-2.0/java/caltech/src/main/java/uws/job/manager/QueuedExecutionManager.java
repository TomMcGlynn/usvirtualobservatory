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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.ErrorType;
import uws.job.ExecutionPhase;

/**
 * <p>Implementation of the interface {@link ExecutionManager} which lets managing an execution queue in function of a maximum number of running jobs:
 * if there are more running jobs than a given number, the jobs to execute are put in the queue until a running job stops.
 * The order of queued jobs are preserved: it is implemented by a FIFO queue.</p>
 * 
 * <p>
 * 	<i><u>Note:</u>
 * 		By overriding {@link #isReadyForExecution(AbstractJob)} you can easily change the condition
 * 		which lets deciding when to put a job in the queue. By default: when there are more running jobs than {@link #nbMaxRunningJobs}.
 * 	</i>
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class QueuedExecutionManager implements ExecutionManager {
	private static final long serialVersionUID = 1L;
	
	/** List of running jobs. */
	protected transient Map<String, AbstractJob> runningJobs;
	
	/** List of queued jobs. */
	protected transient Vector<AbstractJob> queuedJobs;
	
	/** The maximum number of running jobs. */
	protected int nbMaxRunningJobs = NO_QUEUE;
	
	/** The value of {@link #nbMaxRunningJobs} which indicates that there is no queue. */
	public final static int NO_QUEUE = 0;

	
/* ************ */
/* CONSTRUCTORS */
/* ************ */
	/**
	 * Builds an execution manager without queue.
	 * 
	 * @see #sync()
	 */
	public QueuedExecutionManager(){
		sync();
	}
	
	/**
	 * Builds an execution manager with a queue. The number of executing jobs is limited by the given value (if positive and different from 0).
	 * 
	 * @param maxRunningJobs	The maximum number of running jobs (must be > 0 to have a queue).
	 * 
	 * @see #sync()
	 */
	public QueuedExecutionManager(int maxRunningJobs) {
		nbMaxRunningJobs = (maxRunningJobs <= NO_QUEUE)?NO_QUEUE:maxRunningJobs;
		sync();
	}
	
	public final synchronized void sync(){
		if (runningJobs == null)
			runningJobs = new LinkedHashMap<String, AbstractJob>();
		
		if (queuedJobs == null)
			queuedJobs = new Vector<AbstractJob>(0, 10);
	}

/* ***************** */
/* GETTERS & SETTERS */
/* ***************** */
	public synchronized final Iterator<AbstractJob> getRunningJobs(){
		if (runningJobs == null)
			sync();
		
		return runningJobs.values().iterator();
	}
	
	public synchronized final int getNbRunningJobs(){
		if (runningJobs == null)
			sync();
		
		return runningJobs.size();
	}
	
	public synchronized final Iterator<AbstractJob> getQueuedJobs(){
		if (runningJobs == null)
			sync();
		
		return queuedJobs.iterator();
	}
	
	public synchronized final int getNbQueuedJobs(){
		if (runningJobs == null)
			sync();
		
		return queuedJobs.size();
	}

	
	public void setNoQueue() {
		nbMaxRunningJobs = NO_QUEUE;
		try{
			refresh();
		}catch(UWSException ue){
			ue.printStackTrace();
		}
	}
	
	public boolean hasQueue(){
		return nbMaxRunningJobs > NO_QUEUE;
	}
	
	/**
	 * Gets the maximum number of running jobs.
	 * 
	 * @return	The maximum number of running jobs.
	 */
	public final int getMaxRunningJobs(){
		return nbMaxRunningJobs;
	}
	
	/**
	 * <p>Sets the maximum number of running jobs.</p>
	 * 
	 * <p><i><u>Note:</u> If the new maximum number of running jobs is increasing the list of running jobs is immediately updated
	 * BUT NOT IF it is decreasing (that is to say, running jobs will not be interrupted to be put in the queue, they continue to run) !</i></p>
	 * 
	 * @param maxRunningJobs	The new maximum number of running jobs ({@link #NO_QUEUE} or a negative value means no maximum number of running jobs: there will be no queue any more).
	 * 
	 * @throws UWSException		If there is an error while updating the list of running jobs (in other words if some queued jobs can not be executed).
	 * 
	 * @see #refresh()
	 */
	public void setMaxRunningJobs(int maxRunningJobs) throws UWSException {
		nbMaxRunningJobs = (maxRunningJobs <= NO_QUEUE)?NO_QUEUE:maxRunningJobs;
		refresh();
	}

/* **************************** */
/* EXECUTION MANAGEMENT METHODS */
/* **************************** */
	/**
	 * <p>Removes the first queued job(s) from the queue and executes it (them)
	 * <b>ONLY IF</b> it (they) can be executed (see {@link #isReadyForExecution(AbstractJob)}).</p>
	 * 
	 * <p><i><u>Note:</u> Nothing is done if there is no queue.</i></p>
	 * 
	 * @throws UWSException	If there is an error during the phase transition of one or more jobs.
	 * 
	 * @see #sync()
	 * @see #hasQueue()
	 * @see #isReadyForExecution(AbstractJob)
	 * @see AbstractJob#start(boolean)
	 */
	public synchronized final void refresh() throws UWSException {
		if (runningJobs == null)
			sync();
		
		// Return immediately if no queue:
		if (!hasQueue())
			return;
		
		String allMsg = null;	// the concatenation of all errors which may occur
		
		// Start the first job of the queue while it can be executed:
		while (!queuedJobs.isEmpty() && isReadyForExecution(queuedJobs.firstElement())){
			AbstractJob job = queuedJobs.remove(0);
			try{
				if (job != null){
					job.start(false);
					runningJobs.put(job.getJobId(), job);
				}
			}catch(UWSException ue){
				allMsg = ((allMsg == null)?"ERRORS THAT OCCURED WHILE REFRESHING THE EXECUTION MANAGER:":allMsg)+"\n\t- "+ue.getMessage();
			}
		}
		
		// Throw one error for all jobs that can not have been executed:
		if (allMsg != null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, allMsg, ErrorType.TRANSIENT);
	}
	
	/**
	 * <p>By default a job is executed if there are not yet more than {@link #getMaxRunningJobs()} running jobs.</p>
	 * 
	 * @param jobToExecute	The job to execute.
	 * @return				The resulting execution phase of the given job ({@link ExecutionPhase#EXECUTING EXECUTING} or {@link ExecutionPhase#QUEUED QUEUED} or <i>null</i> if the given job is <i>null</i>).
	 * 
	 * @throws UWSException	If there is an error while changing the execution phase of the given job or if the job is already finished.
	 * 
	 * @see #refresh()
	 * @see AbstractJob#isRunning()
	 * @see #hasQueue()
	 * @see #isReadyForExecution(AbstractJob)
	 * @see AbstractJob#start(boolean)
	 * @see AbstractJob#setPhase(ExecutionPhase)
	 */
	public synchronized final ExecutionPhase execute(AbstractJob jobToExecute) throws UWSException {
		if (jobToExecute == null)
			return null;

		// Refresh the list of running jobs before all:
		try{
			refresh();
		}catch(UWSException ue){
			ue.printStackTrace();
		}
		
		// If the job is already running, ensure it is in the list of running jobs:
		if (jobToExecute.isRunning())
			runningJobs.put(jobToExecute.getJobId(), jobToExecute);
		
		// If the job is already finished, ensure it is not any more in both list of jobs:
		else if (jobToExecute.isFinished()){
			runningJobs.remove(jobToExecute);
			queuedJobs.remove(jobToExecute);
		
		// If the jobs is in the QUEUED phase, ensure it is into the queue, and if not, refresh the running jobs list:
		}else if (hasQueue() && jobToExecute.getPhase() == ExecutionPhase.QUEUED){
			if (!queuedJobs.contains(jobToExecute)){
				queuedJobs.add(jobToExecute);
				refresh();
			}
			
		// If the job can be executed, start it:
		}else if (!hasQueue() || isReadyForExecution(jobToExecute)){
			jobToExecute.start(false);
			runningJobs.put(jobToExecute.getJobId(), jobToExecute);
			queuedJobs.remove(jobToExecute);
			
		// Otherwise, the job is put in the queue:
		}else{
			jobToExecute.setPhase(ExecutionPhase.QUEUED);
			if (!queuedJobs.contains(jobToExecute))
				queuedJobs.add(jobToExecute);
		}
		
		return jobToExecute.getPhase();
	}
	
	public final boolean isReadyForExecution(AbstractJob jobToExecute) {
		if (runningJobs == null)
			sync();
		
		if (jobToExecute == null || jobToExecute.isFinished())
			return false;
		
		if (!hasQueue())
			return true;
		else
			return runningJobs.size() < nbMaxRunningJobs;
	}
	
	public final synchronized void update(AbstractJob job) throws UWSException {
		if (runningJobs == null)
			sync();
		
		if (job == null || job.isFinished())
			return;
		
		if (job.isRunning())
			runningJobs.put(job.getJobId(), job);
		
		else if (hasQueue() && job.getPhase() == ExecutionPhase.QUEUED){
			if (!queuedJobs.contains(job))
				queuedJobs.add(job);
		}
		
		refresh();
	}
	
	public final synchronized void remove(AbstractJob jobToRemove) throws UWSException {
		if (runningJobs == null)
			sync();
		
		if (jobToRemove == null)
			return;
		
		runningJobs.remove(jobToRemove.getJobId());
		queuedJobs.remove(jobToRemove);
		
		refresh();
	}
}
