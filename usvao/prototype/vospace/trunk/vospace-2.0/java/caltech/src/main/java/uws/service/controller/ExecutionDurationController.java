package uws.service.controller;

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

import java.util.Map;

import uws.UWSException;

import uws.job.AbstractJob;

/**
 * <p>
 * 	Lets controlling the execution duration of all jobs managed by a UWS. Thus it is possible to set a default and a maximum value.
 * 	Moreover you can indicate whether the execution duration of jobs can be modified by the user or not.
 * </p>
 * 
 * <p>
 * 	<i><u>Note:</u>
 * 		By default, the execution duration can be modified by anyone without any limitation.
 * 		The default value is {@link AbstractJob#UNLIMITED_DURATION}.
 * 	</i>
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class ExecutionDurationController implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** The default duration. */
	protected long defaultDuration = AbstractJob.UNLIMITED_DURATION;
	
	/** The maximum duration. */
	protected long maxDuration = AbstractJob.UNLIMITED_DURATION;
	
	/** Indicates whether the execution duration of jobs can be modified. */
	protected boolean allowModification = true;
	
	
/* ***************** */
/* GETTERS & SETTERS */
/* ***************** */
	/**
	 * Gets the default execution duration.
	 * 
	 * @return The default execution duration <i>(0 or less mean an unlimited duration)</i>.
	 */
	public final long getDefaultExecutionDuration() {
		return defaultDuration;
	}

	/**
	 * Sets the default execution duration.
	 * 
	 * @param defaultExecutionDuration The new default execution duration <i>({@link AbstractJob#UNLIMITED_DURATION}, 0 or a negative value mean an unlimited duration)</i>.
	 */
	public final boolean setDefaultExecutionDuration(long defaultExecutionDuration) {
		defaultExecutionDuration = (defaultExecutionDuration <= 0)?AbstractJob.UNLIMITED_DURATION:defaultExecutionDuration;
		
		if (defaultExecutionDuration != AbstractJob.UNLIMITED_DURATION && maxDuration != AbstractJob.UNLIMITED_DURATION && defaultExecutionDuration > maxDuration)
			return false;
		else
			defaultDuration = defaultExecutionDuration;
		
		return true;
	}

	/**
	 * Gets the maximum execution duration.
	 * 
	 * @return The maximum execution duration <i>(0 or less mean an unlimited duration)</i>.
	 */
	public final long getMaxExecutionDuration() {
		return maxDuration;
	}

	/**
	 * Sets the maximum execution duration.
	 * 
	 * @param maxExecutionDuration The maximum execution duration <i>({@link AbstractJob#UNLIMITED_DURATION}, 0 or a negative value mean an unlimited duration)</i>.
	 */
	public final void setMaxExecutionDuration(long maxExecutionDuration) {
		maxDuration = (maxExecutionDuration <= 0)?AbstractJob.UNLIMITED_DURATION:maxExecutionDuration;
		if (defaultDuration != AbstractJob.UNLIMITED_DURATION && maxDuration != AbstractJob.UNLIMITED_DURATION && defaultDuration > maxDuration)
			defaultDuration = maxDuration;
	}

	/**
	 * Tells whether the execution duration of any managed job can be modified.
	 * 
	 * @return <i>true</i> if the execution duration can be modified, <i>false</i> otherwise.
	 */
	public final boolean allowModification() {
		return allowModification;
	}

	/**
	 * Lets indicating whether the execution duration of any managed job can be modified.
	 * 
	 * @param allowModification <i>true</i> if the execution duration can be modified, <i>false</i> otherwise.
	 */
	public final void allowModification(boolean allowModification) {
		this.allowModification = allowModification;
	}
	
/* *************** */
/* CONTROL METHODS */
/* *************** */
	/**
	 * Controls the execution duration given in the map
	 * or initializes it with the default value if not present in the map.
	 * 
	 * @param params			The map of all parameters to use for the creation of a job.
	 * 
	 * @throws UWSException		If the execution duration given in the map is not valid.
	 * 
	 * @see #control(AbstractJob, Map)
	 */
	public void init(Map<String, String> params) throws UWSException {
		control(null, params);
		
		if (!params.containsKey(AbstractJob.PARAM_EXECUTION_DURATION))
			params.put(AbstractJob.PARAM_EXECUTION_DURATION, Long.toString(defaultDuration));
	}
	
	/**
	 * Extracts the execution duration from the given map and controls its value.
	 * 
	 * @param job				The job whose the execution duration will be set.
	 * @param params			The map which contains the new execution duration of the given job.
	 * 
	 * @throws UWSException		If the new execution duration is not a long number
	 * 							or if the execution duration of the given job can not be modified
	 * 							or if the given value exceeds the maximum value.
	 * 
	 * @see #control(AbstractJob, long)
	 */
	public void control(AbstractJob job, Map<String,String> params) throws UWSException {
		if (params.containsKey(AbstractJob.PARAM_EXECUTION_DURATION)){
			String execDuration = params.get(AbstractJob.PARAM_EXECUTION_DURATION);
			try{
				control(job, Long.parseLong(execDuration));
			}catch(NumberFormatException nfe){
				throw new UWSException(UWSException.BAD_REQUEST, "[Set executionDuration] The given execution duration ("+execDuration+") is not valid: it must be a long (numeric) value !");
			}catch(UWSException ue){
				throw new UWSException(ue.getHttpErrorCode(), "[Set executionDuration] "+ue.getMessage(), ue.getUWSErrorType());
			}
		}
	}
	
	/**
	 * Controls the given execution duration.
	 * 
	 * @param jobToControl		The job whose the execution duration will be updated by the given value.
	 * @param durationToCheck	The new execution duration of the given value.
	 * 
	 * @throws UWSException		If the current phase of the job doesn't allow any modification
	 * 							or if this controller forbids the modification of the execution duration
	 * 							or if the new execution duration exceeds the maximum value.
	 */
	public void control(AbstractJob jobToControl, long durationToCheck) throws UWSException {
		if (jobToControl != null && !jobToControl.getPhaseManager().isJobUpdatable())
			throw new UWSException(UWSException.BAD_REQUEST, "Impossible to update any field (executionDuration included) of the job "+jobToControl.getJobId()+" because its phase is "+jobToControl.getPhase()+" !");
		
		if (!allowModification)
			throw new UWSException(UWSException.BAD_REQUEST, "The UWS forbids the modification of the execution duration !");
		
		if (maxDuration > AbstractJob.UNLIMITED_DURATION && durationToCheck > maxDuration)
			throw new UWSException(UWSException.BAD_REQUEST, "The UWS limits the execution duration to "+maxDuration+" seconds !");
	}
	
	/**
	 * Sets the execution duration of the given job with the default value.
	 * 
	 * @param job				The job whose the execution duration must be set.
	 * 
	 * @throws UWSException		If it is impossible to set the execution duration of the given job (for instance, if the job is running or is finished).
	 * 
	 * @see #setExecutionDuration(AbstractJob, long)
	 */
	public void setDefaultExecutionDuration(AbstractJob job) throws UWSException {
		setExecutionDuration(job, defaultDuration);
	}
	
	/**
	 * Controls the given value and sets the execution duration of the given job with this value.
	 * 
	 * @param job			The job whose the execution duration must be set.
	 * @param duration		The new execution duration of the given job.
	 * 
	 * @throws UWSException	If the current phase of the job doesn't allow any modification
	 *						or if this controller forbids the modification of the execution duration
	 *						or if the new execution duration exceeds the maximum value.
	 *
	 * @see #control(AbstractJob, long)
	 * @see AbstractJob#setExecutionDuration(long)
	 */
	public void setExecutionDuration(AbstractJob job, long duration) throws UWSException {
		control(job, duration);
		job.setExecutionDuration(duration);
	}
	
}
