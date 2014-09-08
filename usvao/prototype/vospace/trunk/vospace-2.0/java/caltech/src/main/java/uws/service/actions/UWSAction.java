package uws.service.actions;

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
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.JobList;

import uws.service.AbstractUWS;
import uws.service.UWSUrl;

/**
 * <p>Action of a UWS (i.e. "List Jobs", "Get Job", etc...). An instance of a UWSAction can be added to a given UWS thanks to the method
 * {@link AbstractUWS#addUWSAction(UWSAction)}.</p>
 * 
 * <p><b><u>WARNING:</u> The action of a UWS have, each one, a different name. So be careful about the name of your UWS action !
 * By default the name of a UWS action is the full java name of the class !</b></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 01/2011
 * 
 * @see AbstractUWS
 */
public abstract class UWSAction<JL extends JobList<J>, J extends AbstractJob> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** Name of the UWS action {@link ListJobs}. */
	public final static String LIST_JOBS = "List Jobs";
	/** Name of the UWS action {@link AddJob}. */
	public final static String ADD_JOB = "Add Job";
	/** Name of the UWS action {@link DestroyJob}. */
	public final static String DESTROY_JOB = "Destroy Job";
	/** Name of the UWS action {@link JobSummary}. */
	public final static String JOB_SUMMARY = "Get Job";
	/** Name of the UWS action {@link GetJobParam}. */
	public final static String GET_JOB_PARAM = "Get Job Parameter";
	/** Name of the UWS action {@link SetJobParam}. */
	public final static String SET_JOB_PARAM = "Set Job Parameter";
	/** Name of the UWS action {@link ShowHomePage}. */
	public final static String HOME_PAGE = "Show UWS Home Page";
	
	/** The UWS on which this action must be applied. */
	protected final AbstractUWS<JL,J> uws;
	
/* *********** */
/* CONSTRUCTOR */
/* *********** */
	/**
	 * Builds a UWSAction.
	 * 
	 * @param u	The UWS which contains this action.
	 */
	protected UWSAction(AbstractUWS<JL, J> u){
		uws = u;
	}

/* ***************** */
/* GETTERS & SETTERS */
/* ***************** */
	/**
	 * Gets the UWS which contains this action.
	 * 
	 * @return	Its UWS.
	 */
	public final AbstractUWS<JL,J> getUWS(){
		return uws;
	}
	
	/**
	 * <p>Gets the name of this UWS action. <b>MUST BE UNIQUE !</b></p>
	 * 
	 * <p><i><u>Note:</u> By default the name of the class is returned ({@link Class#getName()}).</i></p>
	 * 
	 * @return	Its name.
	 */
	public String getName(){
		return getClass().getName();
	}
	
	/**
	 * <p>Gets the description of this UWS action.</p>
	 * 
	 * <p><i><u>Note:</u> By default an empty string is returned.</i></p>
	 * 
	 * @return	Its description.
	 */
	public String getDescription(){
		return "";
	} 
	
/* ************ */
/* TOOL METHODS */
/* ************ */
	/**
	 * Extracts the name of the jobs list from the given UWS URL
	 * and gets the jobs list from the UWS.
	 * 
	 * @param urlInterpreter	The UWS URL which contains the name of the jobs list to get.
	 * 
	 * @return					The corresponding jobs list.
	 * 
	 * @throws UWSException		If there is no jobs list name in the given UWS URL
	 * 							or if no corresponding jobs list can be found in the UWS.
	 * 
	 * @see UWSUrl#getJobListName()
	 * @see AbstractUWS#getJobList(String)
	 */
	protected final JL getJobsList(UWSUrl urlInterpreter) throws UWSException {
		String jlName = urlInterpreter.getJobListName();
		JL jobsList = null;
		
		if (jlName != null){
			jobsList = uws.getJobList(jlName);
			if (jobsList == null)
				throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The job list \""+jlName+"\" does not exist ! (note: the jobs list names are case sensitive)");
		}else
			throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The jobs list name is missing !");
		
		return jobsList;
	}
	
	/**
	 * Extracts the job ID from the given UWS URL
	 * and gets the corresponding job from the UWS.
	 * 
	 * @param urlInterpreter	The UWS URL which contains the ID of the job to get.
	 * 
	 * @return					The corresponding job.
	 * 
	 * @throws UWSException		If no jobs list name or/and job ID can be found in the given UWS URL
	 * 							or if there are no corresponding jobs list and/or job in the UWS.
	 * 
	 * @see UWSUrl#getJobId()
	 * @see #getJobsList(UWSUrl)
	 * @see JobList#getJob(String)
	 */
	protected final J getJob(UWSUrl urlInterpreter) throws UWSException {
		String jobId = urlInterpreter.getJobId();
		J job = null;
		
		if (jobId != null){
			JL jobsList = getJobsList(urlInterpreter);
			job = jobsList.getJob(jobId);
			if (job == null)
				throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The job \""+jobId+"\" does not exist in the jobs list \""+jobsList.getName()+"\" ! (note: the job IDs are case sensitive)");
		}else
			throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The job ID is missing !");
		
		return job;
	}
	
	/**
	 * Extracts the job ID from the given UWS URL
	 * and gets the corresponding job from the given jobs list.
	 * 
	 * @param urlInterpreter	The UWS URL which contains the ID of the job to get.
	 * @param jobsList			The jobs list which is supposed to contain the job to get.
	 * 
	 * @return					The corresponding job.
	 * 
	 * @throws UWSException		If no job ID can be found in the given UWS URL
	 * 							or if there are no corresponding job in the UWS.
	 * 
	 * @see UWSUrl#getJobId()
	 * @see JobList#getJob(String)
	 */
	protected final J getJob(UWSUrl urlInterpreter, JL jobsList) throws UWSException {
		String jobId = urlInterpreter.getJobId();
		J job = null;
		
		if (jobId != null){
			if (jobsList == null)
				throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The jobs list name is missing !");
			job = jobsList.getJob(jobId);
			if (job == null)
				throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The job \""+jobId+"\" does not exist in the jobs list \""+jobsList.getName()+"\" ! (note: the job IDs are case sensitive)");
		}else
			throw new UWSException(UWSException.BAD_REQUEST, "["+getName()+"] The job ID is missing !");
		
		return job;
	}
	
/* ************** */
/* ACTION METHODS */
/* ************** */
	/**
	 * Indicates whether the given request corresponds to this UWS action.
	 * 
	 * @param urlInterpreter	The UWS URL of the given request.
	 * @param userId			The user who has sent the given request.
	 * @param request			The received request.
	 * 
	 * @return					<i>true</i> if the given request corresponds to this UWS action, <i>false</i> otherwise.
	 * 
	 * @throws UWSException		If any error occurs during the tests.
	 */
	public abstract boolean match(UWSUrl urlInterpreter, String userId, HttpServletRequest request) throws UWSException;
	
	/**
	 * <p>Applies this UWS action in function of the given request
	 * and writes the result in the given response.</p>
	 * 
	 * <p><i><u>Note:</u> You can use the functions {@link #getJobsList(UWSUrl)}, {@link #getJob(UWSUrl)} and {@link #getJob(UWSUrl, JobList)} to
	 * get more easily the jobs list and/or the job from the given UWS URL !</i></p>
	 * 
	 * @param urlInterpreter	The UWS URL of the given request.
	 * @param userId			The user who has sent the given request.
	 * @param request			The received request.
	 * @param response			The response of the given request (MUST BE UPDATED).
	 * 
	 * @return					<i>true</i> if the actions has been successfully applied, <i>false</i> otherwise.
	 * 
	 * @throws UWSException		If any error occurs during the action application.
	 * @throws IOException		If there is an error while the result is written in the given response.
	 */
	public abstract boolean apply(UWSUrl urlInterpreter, String userId, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException;
	
/* ************* */
/* MISCELLANEOUS */
/* ************* */
	@Override
	@SuppressWarnings("unchecked")
	public final boolean equals(Object obj){
		if (obj instanceof UWSAction)
			return getName().equals(((UWSAction)obj).getName());
		else
			return super.equals(obj);
	}

	@Override
	public String toString() {
		return getName();
	}
	
}
