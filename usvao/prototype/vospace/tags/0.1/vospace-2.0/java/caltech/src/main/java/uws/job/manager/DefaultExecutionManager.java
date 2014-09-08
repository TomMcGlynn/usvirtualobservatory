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

import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.ErrorType;
import uws.job.ExecutionPhase;

/**
 * <p>Default implementation of the ExecutionManager interface.</p>
 * 
 * <p>This manager does not have a queue. That is to say that all jobs are always immediately starting.
 * Consequently this manager is just user to gather all running jobs.</p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class DefaultExecutionManager implements ExecutionManager {
	private static final long serialVersionUID = 1L;
	
	/** List of running jobs. */
	protected transient Map<String, AbstractJob> runningJobs;

	
/* ************ */
/* CONSTRUCTORS */
/* ************ */
	/**
	 * Builds an execution manager without queue.
	 * 
	 * @see #sync()
	 */
	public DefaultExecutionManager(){
		sync();
	}
	
	public synchronized void sync(){
		if (runningJobs == null)
			runningJobs = new LinkedHashMap<String, AbstractJob>();
	}
	
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
	
	/**
	 * Always returns a Null Iterator (iterator whose next() returns <i>null</i> and hasNext() returns <i>false</i>).
	 * 
	 * @see uws.job.manager.ExecutionManager#getQueuedJobs()
	 */
	public synchronized Iterator<AbstractJob> getQueuedJobs(){
		return new Iterator<AbstractJob>() {
			public boolean hasNext() { return false; }

			public AbstractJob next() { return null; }
			
			public void remove() { ; }
		};
	}
	
	/**
	 * Always returns 0.
	 * 
	 * @see uws.job.manager.ExecutionManager#getNbQueuedJobs()
	 */
	public synchronized int getNbQueuedJobs(){
		return 0;
	}
	
	public void setNoQueue() { ; }
	
	public boolean hasQueue(){ return false; }

	public synchronized void refresh() throws UWSException {
		if (runningJobs == null)
			sync();
	}
	
	public synchronized ExecutionPhase execute(AbstractJob jobToExecute) throws UWSException {
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
		
		// If the job is already finished, ensure it is not any more in the list of running jobs:
		else if (jobToExecute.isFinished()){
			runningJobs.remove(jobToExecute);
			throw new UWSException(UWSException.BAD_REQUEST, "[Start a job] The job \""+jobToExecute.getJobId()+"\" has already been executed. It has finished with the phase \""+jobToExecute.getPhase()+"\" !", ErrorType.TRANSIENT);
			
		// Otherwise start it:
		} else {
			jobToExecute.start(false);
			runningJobs.put(jobToExecute.getJobId(), jobToExecute);
		}
		
		return jobToExecute.getPhase();
	}
	
	public boolean isReadyForExecution(AbstractJob jobToExecute) {
		if (runningJobs == null)
			sync();
		
		return jobToExecute != null && !jobToExecute.isFinished();
	}
	
	public synchronized void update(AbstractJob job) throws UWSException {
		if (runningJobs == null)
			sync();
		
		if (job == null || job.isFinished())
			return;
		
		if (job.isRunning())
			runningJobs.put(job.getJobId(), job);
		
		refresh();
	}
	
	public synchronized void remove(AbstractJob jobToRemove) throws UWSException {
		if (runningJobs == null)
			sync();
		
		if (jobToRemove == null)
			return;
		
		runningJobs.remove(jobToRemove.getJobId());
		
		refresh();
	}
}
