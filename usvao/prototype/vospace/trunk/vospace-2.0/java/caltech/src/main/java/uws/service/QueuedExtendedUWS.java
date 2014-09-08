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

import uws.UWSException;

import uws.job.manager.ExecutionManager;
import uws.job.manager.QueuedExecutionManager;

/**
 * <p>A {@link ExtendedUWS} which is able to manage an execution queue.
 * In this implementation the size of the queue is limited by a number given at the initialization.
 * Thus a job can run in this UWS only if there are less running jobs than the given number.</p>
 * 
 * <p><i><u>Note:</u> The limitation of the queue size is only one possible queue management way !
 * If you want to test other conditions before running a job, you must change the used execution manager by using the method
 * {@link AbstractUWS#setExecutionManager(ExecutionManager)} with the appropriate implementation
 * of the interface {@link ExecutionManager}.</i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 * 
 * @see ExtendedUWS
 * @see QueuedExecutionManager
 */
public class QueuedExtendedUWS extends ExtendedUWS {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a QueuedExtendedUWS.
	 * 
	 * @param nbMaxRunningJobs	The maximum number of jobs that can run in the same time (that is to say: the size of the execution queue).
	 * 
	 * @throws UWSException		If there is an error when calling the constructor super()
	 * 							or if the given maximum number of running jobs is negative or null.
	 * 
	 * @see ExtendedUWS#ExtendedUWS()
	 * @see QueuedExecutionManager#QueuedExecutionManager(int)
	 * @see #setExecutionManager(uws.job.manager.ExecutionManager)
	 */
	public QueuedExtendedUWS(int nbMaxRunningJobs) throws UWSException {
		super();
		setExecutionManager(new QueuedExecutionManager(nbMaxRunningJobs));
	}

	/**
	 * Builds a QueuedExtendedUWS with its base URI.
	 * 
	 * @param nbMaxRunningJobs	The maximum number of jobs that can run in the same time (that is to say: the size of the execution queue).
	 * @param baseURI			The base UWS URI.
	 * 
	 * @throws UWSException		If there is an error when calling the constructor super(String)
	 * 							or if the given maximum number of running jobs is negative or null.
	 * 
	 * @see ExtendedUWS#ExtendedUWS(String)
	 * @see QueuedExecutionManager#QueuedExecutionManager(int)
	 * @see #setExecutionManager(uws.job.manager.ExecutionManager)
	 */
	public QueuedExtendedUWS(int nbMaxRunningJobs, String baseURI) throws UWSException {
		super(baseURI);
		setExecutionManager(new QueuedExecutionManager(nbMaxRunningJobs));
	}

	/**
	 * Builds a QueuedExtendedUWS with the UWS URL interpreter to use.
	 * 
	 * @param nbMaxRunningJobs	The maximum number of jobs that can run in the same time (that is to say: the size of the execution queue).
	 * @param urlInterpreter	The UWS URL interpreter this UWS must use.
	 * 
	 * @throws UWSException		If there is an error when calling the constructor super(UWSUrl)
	 * 							of if the given maximum number of running jobs is negative or null.
	 * 
	 * @see ExtendedUWS#ExtendedUWS(UWSUrl)
	 * @see QueuedExecutionManager#QueuedExecutionManager(int)
	 * @see #setExecutionManager(uws.job.manager.ExecutionManager)
	 */
	public QueuedExtendedUWS(int nbMaxRunningJobs, UWSUrl urlInterpreter) throws UWSException {
		super(urlInterpreter);
		setExecutionManager(new QueuedExecutionManager(nbMaxRunningJobs));
	}
	
	/**
	 * <p>Gets the maximum number of jobs that can run in the same time.</p>
	 * 
	 * <p><i><u>Note:</u> This method does nothing if the used execution manager is not subclass of {@link QueuedExecutionManager}.</i></p>
	 * 
	 * @return	The maximum number of running jobs or a negative value if the used execution manager is not a subclass of {@link QueuedExecutionManager}.
	 * 
	 * @see QueuedExecutionManager#getMaxRunningJobs()
	 */
	public final int getMaxRunningJobs() {
		if (getExecutionManager() instanceof QueuedExecutionManager)
			return ((QueuedExecutionManager)getExecutionManager()).getMaxRunningJobs();
		else
			return -1;
	}

	/**
	 * <p>Sets the maximum number of jobs that can run in the same time.</p>
	 * 
	 * <p><i><u>Note:</u> This method does nothing if the used execution manager is not subclass of {@link QueuedExecutionManager}.</i></p>
	 * 
	 * @param maxRunningJobs	The new maximum number of running jobs (must be > 0 to have a queue).
	 * 
	 * @throws UWSException		If there is an error while updating the list of running jobs (in other words if some queued jobs can not be executed).
	 * 
	 * @see QueuedExecutionManager#setMaxRunningJobs(int)
	 */
	public final void setMaxRunningJobs(int maxRunningJobs) throws UWSException {
		if (getExecutionManager() instanceof QueuedExecutionManager)
			((QueuedExecutionManager)getExecutionManager()).setMaxRunningJobs(maxRunningJobs);
	}

}
