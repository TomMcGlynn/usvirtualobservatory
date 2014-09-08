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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;
import uws.UWSToolBox;

import uws.job.AbstractJob;
import uws.job.JobList;


import uws.service.AbstractUWS;
import uws.service.UWSUrl;
import uws.service.controller.DestructionTimeController;
import uws.service.controller.ExecutionDurationController;

/**
 * <p>The "Add Job" action of a UWS.</p>
 * 
 * <p><i><u>Note:</u> The corresponding name is {@link UWSAction#ADD_JOB}.</i></p>
 * 
 * <p>This action creates a new job and adds it to the specified jobs list.
 * The response of this action is a redirection to the new job resource (that is to say: a redirection to the job summary of the new job).</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class AddJob<JL extends JobList<J>, J extends AbstractJob> extends UWSAction<JL, J> {
	private static final long serialVersionUID = 1L;

	public AddJob(AbstractUWS<JL,J> u){
		super(u);
	}
	
	/**
	 * @see UWSAction#ADD_JOB
	 * @see uws.service.actions.UWSAction#getName()
	 */
	@Override
	public String getName() {
		return ADD_JOB;
	}

	@Override
	public String getDescription() {
		return "Lets adding to the specified jobs list a job whose the parameters are given. (URL: {baseUWS_URL}/{jobListName}, Method: HTTP-POST, Parameters: job parameters)";
	}

	/**
	 * Checks whether:
	 * <ul>
	 * 	<li>a job list name is specified in the given UWS URL <i>(<u>note:</u> by default, the existence of the jobs list is not checked)</i>,</li>
	 * 	<li>the UWS URL does not make a reference to a job (so: no job ID),</li>
	 * 	<li>the HTTP method is HTTP-POST.</li>
	 * </ul>
	 * @see uws.service.actions.UWSAction#match(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean match(UWSUrl urlInterpreter, String userId, HttpServletRequest request) throws UWSException {
		return (urlInterpreter.hasJobList()
				&& !urlInterpreter.hasJob()
				&& request.getMethod().equalsIgnoreCase("post"));
	}

	/**
	 * Gets the specified jobs list <i>(throw an error if not found)</i>,
	 * creates a new job, adds it to the jobs list and makes a redirection to the summary of this new job.
	 * 
	 * @see #getJobsList(UWSUrl)
	 * @see UWSToolBox#getParamsMap(HttpServletRequest, java.lang.String)
	 * @see ExecutionDurationController#init(Map)
	 * @see DestructionTimeController#init(Map)
	 * @see AbstractUWS#createJob(java.util.Map)
	 * @see AbstractUWS#setExecutionManager(uws.job.manager.ExecutionManager)
	 * @see JobList#addNewJob(AbstractJob)
	 * @see AbstractUWS#redirect(String, HttpServletRequest, HttpServletResponse)
	 * 
	 * @see uws.service.actions.UWSAction#apply(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean apply(UWSUrl urlInterpreter, String userId, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
	// Get the jobs list:
		JL jobsList = getJobsList(urlInterpreter);
	
	// Get the request parameters:
		Map<String,String> params = UWSToolBox.getParamsMap(request, userId);
	
	// Get the request document
		String document = null;
		if (params.size() == 1) {
		    document = UWSToolBox.getDocument(request, userId);
		}
	// Control the execution duration:
		uws.getExecutionDurationController().init(params);
		
	// Control the destruction time:
		uws.getDestructionTimeController().init(params);
		
	// Create the job:
		J newJob = (document == null) ? uws.createJob(params) : uws.createJob(document);
		
	// Set its default execution duration:
		newJob.setExecutionManager(uws.getExecutionManager());
		
	// Add it to the jobs list:
		if (jobsList.addNewJob(newJob) != null){
			
		// Make a redirection to the added job:
			uws.redirect(urlInterpreter.jobSummary(jobsList.getName(), newJob.getJobId()).getRequestURL(), request, response);
			
			return true;
		}else
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "["+getName()+"] Unable to add the new job to the jobs list. (ID of the new job = \""+newJob.getJobId()+"\" ; ID already used = "+(jobsList.getJob(newJob.getJobId())!=null)+")");
	}

}
