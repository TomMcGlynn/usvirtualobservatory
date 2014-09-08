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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import uws.UWSException;

import uws.job.AbstractJob;

/**
 * <p>
 * 	Let's controlling the destruction time of all jobs managed by a UWS. Thus it is possible to set a default and a maximum value.
 * 	Moreover you can indicate whether the destruction time of jobs can be modified by the user or not.
 * </p>
 * 
 * <p>
 * 	<i><u>Notes:</u>
 * 		<ul>
 * 			<li>By default, the destruction time can be modified by anyone without any limitation.
 * 				There is no default value (that means jobs may stay forever).</li>
 * 			<li>You can specify a destruction time (default or maximum value) in two ways:
 * 				by an exact date-time or by an interval of time from the initialization (expressed in the second, minutes, hours, days, months or years).</li>
 * 		</ul>
 * 	</i>
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class DestructionTimeController implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Represents a date/time field.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 02/2011
	 * 
	 * @see Calendar
	 */
	public static enum DateField {
		SECOND 	(Calendar.SECOND),
		MINUTE 	(Calendar.MINUTE),
		HOUR 	(Calendar.HOUR),
		DAY 	(Calendar.DAY_OF_MONTH),
		MONTH 	(Calendar.MONTH),
		YEAR 	(Calendar.YEAR);
		
		private final int index;
		private DateField(int fieldIndex){
			index = fieldIndex;
		}
		
		public final int getFieldIndex(){
			return index;
		}
	}
	
	/** Default value of an interval: a null interval. */
	public final static int NO_INTERVAL = 0;
	
	/** The default destruction time. */
	protected Date defaultTime = null;
	/** The date-time field on which the default interval applies. */
	protected DateField defaultIntervalField = null;
	/** The default interval from the initialization to the destruction of the concerned job. */
	protected int defaultInterval = NO_INTERVAL;
	
	/** The maximum destruction time. */
	protected Date maxTime = null;
	/** The date-time field on which the maximum interval applies. */
	protected DateField maxIntervalField = null;
	/** The maximum interval from the initialization to the destruction of the concerned job. */
	protected int maxInterval = NO_INTERVAL;
	
	/** Indicates whether the destruction time of jobs can be modified. */
	protected boolean allowModification = true;

	
/* ***************** */
/* GETTERS & SETTERS */
/* ***************** */
	/**
	 * Gets the default destruction time: either computed with an interval of time or obtained directly by a default destruction time.
	 * 
	 * @return The default destruction time (<i>null</i> means that jobs may stay forever).
	 */
	public final Date getDefaultDestructionTime() {
		if (defaultInterval > NO_INTERVAL){
			Calendar date = Calendar.getInstance();
			try{
				date.add(defaultIntervalField.getFieldIndex(), defaultInterval);
				return date.getTime();
			}catch(ArrayIndexOutOfBoundsException ex){
				return null;
			}
		}else
			return defaultTime;
	}

	/**
	 * <p>Sets the default destruction time.</p>
	 * 
	 * <p>
	 * 	<i><u>Note:</u>
	 * 		If there was a default interval, it is reset and so the given destruction time will be used by {@link #getDefaultDestructionTime()}.
	 * 	</i>
	 * </p>
	 * 
	 * @param defaultDestructionTime The default destruction time to set (<i>null</i> means jobs may stay forever).
	 */
	public final void setDefaultDestructionTime(Date defaultDestructionTime) {
		this.defaultTime = defaultDestructionTime;
		defaultInterval = NO_INTERVAL;
		defaultIntervalField = null;
	}

	/**
	 * <p>Gets the default interval value.</p>
	 * 
	 * <p><i><u>Note:</u> To get the corresponding unit, use {@link #getDefaultIntervalField()} and {@link DateField#name()}.</i></p>
	 * 
	 * @return The default destruction interval.
	 */
	public final int getDefaultDestructionInterval() {
		return defaultInterval;
	}

	/**
	 * Gets the date-time field of the default interval.
	 * 
	 * @return The default interval field.
	 */
	public final DateField getDefaultIntervalField() {
		return defaultIntervalField;
	}

	/**
	 * <p>Sets the default interval <b>in minutes</b> from the initialization to the destruction of the concerned job.</p>
	 * 
	 * <p>
	 * 	<i><u>Note:</u>
	 * 		If there was a default destruction time, it is reset and so the given interval will be used by {@link #getDefaultDestructionTime()}.
	 * 	</i>
	 * </p>
	 * 
	 * @param defaultDestructionInterval The default destruction interval ({@link #NO_INTERVAL}, 0 or a negative value mean the job may stay forever).
	 * 
	 * @see #setDefaultDestructionInterval(int, DateField)
	 */
	public final void setDefaultDestructionInterval(int defaultDestructionInterval) {
		setDefaultDestructionInterval(defaultDestructionInterval, DateField.MINUTE);
	}

	/**
	 * <p>Sets the default interval (in the given unit) from the initialization to the destruction of the concerned job.</p>
	 * 
	 * <p>
	 * 	<i><u>Note:</u>
	 * 		If there was a default destruction time, it is reset and so the given interval will be used by {@link #getDefaultDestructionTime()}.
	 * 	</i>
	 * </p>
	 * 
	 * @param defaultDestructionInterval	The default destruction interval ({@link #NO_INTERVAL}, 0 or a negative value mean the job may stay forever).
	 * @param timeField						The unit of the interval (<i>null</i> means the job may stay forever).
	 */
	public final void setDefaultDestructionInterval(int defaultDestructionInterval, DateField timeField) {
		if (defaultDestructionInterval <= 0 || timeField == null){
			defaultIntervalField = null;
			defaultInterval = NO_INTERVAL;
		}else{
			defaultIntervalField = timeField;
			defaultInterval = defaultDestructionInterval;
		}
		defaultTime = null;
	}

	/**
	 * Gets the maximum destruction time: either computed with an interval of time or obtained directly by a maximum destruction time.
	 * 
	 * @return The maximum destruction time (<i>null</i> means that jobs may stay forever).
	 */
	public final Date getMaxDestructionTime() {
		if (maxInterval > NO_INTERVAL){
			Calendar date = Calendar.getInstance();
			try{
				date.add(maxIntervalField.getFieldIndex(), maxInterval);
				return date.getTime();
			}catch(ArrayIndexOutOfBoundsException ex){
				return null;
			}
		}else
			return maxTime;
	}

	/**
	 * <p>Sets the maximum destruction time.</p>
	 * 
	 * <p>
	 * 	<i><u>Note:</u>
	 * 		If there was a maximum interval, it is reset and so the given destruction time will be used by {@link #getMaxDestructionTime()}.
	 * 	</i>
	 * </p>
	 * 
	 * @param maxDestructionTime The maximum destruction time to set (<i>null</i> means jobs may stay forever).
	 */
	public final void setMaxDestructionTime(Date maxDestructionTime) {
		this.maxTime = maxDestructionTime;
		maxInterval = NO_INTERVAL;
		maxIntervalField = null;
	}

	/**
	 * <p>Gets the maximum interval value.</p>
	 * 
	 * <p><i><u>Note:</u> To get the corresponding unit, use {@link #getMaxIntervalField()} and {@link DateField#name()}.</i></p>
	 * 
	 * @return The maximum destruction interval.
	 */
	public final int getMaxDestructionInterval() {
		return maxInterval;
	}

	/**
	 * Gets the date-time field of the maximum interval.
	 * 
	 * @return The maximum interval field.
	 */
	public final DateField getMaxIntervalField() {
		return maxIntervalField;
	}

	/**
	 * <p>Sets the maximum interval <b>in minutes</b> from the initialization to the destruction of the concerned job.</p>
	 * 
	 * <p>
	 * 	<i><u>Note:</u>
	 * 		If there was a maximum destruction time, it is reset and so the given interval will be used by {@link #getMaxDestructionTime()}.
	 * 	</i>
	 * </p>
	 * 
	 * @param maxDestructionInterval The maximum destruction interval ({@link #NO_INTERVAL}, 0 or a negative value mean the job may stay forever).
	 * 
	 * @see #setMaxDestructionInterval(int, DateField)
	 */
	public final void setMaxDestructionInterval(int maxDestructionInterval) {
		setMaxDestructionInterval(maxDestructionInterval, DateField.MINUTE);
	}

	/**
	 * <p>Sets the maximum interval (in the given unit) from the initialization to the destruction of the concerned job.</p>
	 * 
	 * <p>
	 * 	<i><u>Note:</u>
	 * 		If there was a maximum destruction time, it is reset and so the given interval will be used by {@link #getMaxDestructionTime()}.
	 * 	</i>
	 * </p>
	 * 
	 * @param maxDestructionInterval		The maximum destruction interval ({@link #NO_INTERVAL}, 0 or a negative value mean the job may stay forever).
	 * @param timeField						The unit of the interval (<i>null</i> means the job may stay forever).
	 */
	public final void setMaxDestructionInterval(int maxDestructionInterval, DateField timeField) {
		this.maxInterval = maxDestructionInterval;
		maxIntervalField = timeField;
		maxTime = null;
	}

	/**
	 * Tells whether the destruction time of any managed job can be modified.
	 * 
	 * @return <i>true</i> if the destruction time can be modified, <i>false</i> otherwise.
	 */
	public final boolean allowModification() {
		return allowModification;
	}

	/**
	 * Lets indicating whether the destruction time of any managed job can be modified.
	 * 
	 * @param allowModification <i>true</i> if the destruction time can be modified, <i>false</i> otherwise.
	 */
	public final void allowModification(boolean allowModification) {
		this.allowModification = allowModification;
	}
	
/* *************** */
/* CONTROL METHODS */
/* *************** */
	/**
	 * Controls the destruction time given in the map
	 * or initializes it with the default value if not present in the map.
	 * 
	 * @param params			The map of all parameters to use for the creation of a job.
	 * 
	 * @throws UWSException		If the destruction time given in the map is not valid.
	 * 
	 * @see #control(AbstractJob, Map)
	 */
	public void init(Map<String, String> params) throws UWSException {
		control(null, params);
		
		if (!params.containsKey(AbstractJob.PARAM_DESTRUCTION_TIME)){
			Date date = getDefaultDestructionTime();
			params.put(AbstractJob.PARAM_DESTRUCTION_TIME, (date==null)?null:getDateFormat(null).format(date));
		}
	}
	
	/**
	 * Extracts the destruction time from the given map and controls its value.
	 * 
	 * @param job				The job whose the destruction time will be set.
	 * @param params			The map which contains the new destruction time of the given job.
	 * 
	 * @throws UWSException		If the new destruction time does not respect the date format used by the given job or the default date format
	 * 							or if the destruction time of the given job can not be modified
	 * 							or if the given value exceeds the maximum value.
	 * 
	 * @see #control(AbstractJob, Date)
	 */
	public void control(AbstractJob job, Map<String,String> params) throws UWSException {
		if (params.containsKey(AbstractJob.PARAM_DESTRUCTION_TIME)){
			String destruction = params.get(AbstractJob.PARAM_DESTRUCTION_TIME);
			try{
				Date destDate = getDateFormat(job).parse(destruction);
				control(job, destDate);
			} catch (ParseException e) {
				throw new UWSException("[Set destruction] The destruction time format is incorrect.");
			}catch(UWSException ue){
				throw new UWSException(ue.getHttpErrorCode(), "[Set destruction] "+ue.getMessage(), ue.getUWSErrorType());
			}
		}
	}
	
	/**
	 * Controls the given destruction time.
	 * 
	 * @param jobToControl		The job whose the destruction time will be updated by the given value.
	 * @param dateToCheck		The new destruction time of the given value.
	 * 
	 * @throws UWSException		If the current phase of the job doesn't allow any modification
	 * 							or if this controller forbids the modification of the destruction time
	 * 							or if the new destruction time exceeds the maximum value.
	 */
	public void control(AbstractJob jobToControl, Date dateToCheck) throws UWSException {
		if (jobToControl != null && !jobToControl.getPhaseManager().isJobUpdatable())
			throw new UWSException(UWSException.BAD_REQUEST, "Impossible to update any field (destruction included) of the job "+jobToControl.getJobId()+" because its phase is "+jobToControl.getPhase()+" !");
		
		if (!allowModification)
			throw new UWSException(UWSException.BAD_REQUEST, "The UWS forbids the modification of the destruction time !");
		
		Date maxDate = getMaxDestructionTime();
		if (maxDate != null && dateToCheck.after(maxDate))
			throw new UWSException(UWSException.BAD_REQUEST, "The UWS limits "+((defaultInterval > NO_INTERVAL)?("the destruction interval (since now) to "+maxInterval+" "+maxIntervalField.name().toLowerCase()+"s"):("the destruction time to "+maxDate))+" !");
	}
	
	/**
	 * Sets the destruction time of the given job with the default value.
	 * 
	 * @param job				The job whose the destruction time must be set.
	 * 
	 * @throws UWSException		If it is impossible to set the destruction time of the given job (for instance, if the job is running or is finished).
	 * 
	 * @see #setDestructionTime(AbstractJob, Date)
	 */
	public void setDefaultDestructionTime(AbstractJob job) throws UWSException {
		setDestructionTime(job, getDefaultDestructionTime());
	}
	
	/**
	 * Controls the given value and sets the destruction time of the given job with this value.
	 * 
	 * @param job			The job whose the destruction time must be set.
	 * @param date			The new destruction time of the given job.
	 * 
	 * @throws UWSException	If the current phase of the job doesn't allow any modification
	 *						or if this controller forbids the modification of the destruction time
	 *						or if the new destruction time exceeds the maximum value.
	 *
	 * @see #control(AbstractJob, Date)
	 * @see AbstractJob#setDestructionTime(Date)
	 */
	public void setDestructionTime(AbstractJob job, Date date) throws UWSException {
		control(job, date);
		job.setDestructionTime(date);
	}
	
	/**
	 * Gets the date format used currently by the given job if not <i>null</i> or gets the default date format of a job (see {@link AbstractJob#DEFAULT_DATE_FORMAT}).
	 * 
	 * @param job	The job whose the date format must be extracted.
	 * 
	 * @return	A valid date format understood by the given job (if the given job is <i>null</i> the date format is the default one and so, it is surely understood at the job creation).
	 * 
	 * @see AbstractJob#getDateFormat()
	 * @see AbstractJob#DEFAULT_DATE_FORMAT
	 */
	public static final DateFormat getDateFormat(AbstractJob job){
		if (job == null || job.getDateFormat() == null)
			return new SimpleDateFormat(AbstractJob.DEFAULT_DATE_FORMAT);
		else
			return job.getDateFormat();
	}
	
}
