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

import uws.job.AbstractJob;

import uws.job.manager.ExecutionManager;
import uws.job.manager.QueuedExecutionManager;

/**
 * <p>A {@link BasicUWS} which is able to manage an execution queue.
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
 * @see BasicUWS
 * @see QueuedExecutionManager
 */
public class QueuedBasicUWS<J extends AbstractJob> extends BasicUWS<J> {
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a QueuedBasicUWS.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * @param nbMaxRunningJobs	The maximum number of jobs that can run in the same time (that is to say: the size of the execution queue).
	 * 
	 * @throws UWSException		<ul><li>If there is an error when calling the constructor super(Class).</li>
	 * 							<li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li>
	 * 							<li>If the given maximum number of running jobs is negative or null.</li></ul>
	 * 
	 * @see BasicUWS#BasicUWS(Class)
	 * @see QueuedExecutionManager#QueuedExecutionManager(int)
	 * @see #setExecutionManager(uws.job.manager.ExecutionManager)
	 */
	public QueuedBasicUWS(Class<J> cl, int nbMaxRunningJobs) throws UWSException {
		super(cl);
		setExecutionManager(new QueuedExecutionManager(nbMaxRunningJobs));
	}

	/**
	 * Builds a QueuedBasicUWS.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * @param nbMaxRunningJobs	The maximum number of jobs that can run in the same time (that is to say: the size of the execution queue).
	 * @param baseURI			The base UWS URI.
	 * 
	 * @throws UWSException		<ul><li>If there is an error when calling the constructor super(Class, String).</li>
	 * 							<li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li>
	 * 							<li>If the given maximum number of running jobs is negative or null.</li></ul>
	 * 
	 * @see BasicUWS#BasicUWS(Class, String)
	 * @see QueuedExecutionManager#QueuedExecutionManager(int)
	 * @see #setExecutionManager(uws.job.manager.ExecutionManager)
	 */
	public QueuedBasicUWS(Class<J> cl, int nbMaxRunningJobs, String baseURI) throws UWSException {
		super(cl, baseURI);
		setExecutionManager(new QueuedExecutionManager(nbMaxRunningJobs));
	}

	/**
	 * Builds a QueuedBasicUWS.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * @param nbMaxRunningJobs	The maximum number of jobs that can run in the same time (that is to say: the size of the execution queue).
	 * @param urlInterpreter	The UWS URL interpreter to use in this UWS.
	 * 
	 * @throws UWSException		<ul><li>If there is an error when calling the constructor super(Class, UWSUrl).</li>
	 * 							<li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li>
	 * 							<li>If the given maximum number of running jobs is negative or null.</li></ul>
	 * 
	 * @see BasicUWS#BasicUWS(Class, UWSUrl)
	 * @see QueuedExecutionManager#QueuedExecutionManager(int)
	 * @see #setExecutionManager(uws.job.manager.ExecutionManager)
	 */
	public QueuedBasicUWS(Class<J> cl, int nbMaxRunningJobs, UWSUrl urlInterpreter) throws UWSException {
		super(cl, urlInterpreter);
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
