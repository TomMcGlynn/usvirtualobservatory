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
 * <p>The "Set Job Parameter" action of a UWS.</p>
 * 
 * <p><i><u>Note:</u> The corresponding name is {@link UWSAction#SET_JOB_PARAM}.</i></p>
 * 
 * <p>This action sets the value of the specified job attribute.
 * The response of this action is a redirection to the job summary.</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class SetJobParam<JL extends JobList<J>, J extends AbstractJob> extends UWSAction<JL, J> {
	private static final long serialVersionUID = 1L;

	public SetJobParam(AbstractUWS<JL, J> u){
		super(u);
	}

	/**
	 * @see UWSAction#SET_JOB_PARAM
	 * @see uws.service.actions.UWSAction#getName()
	 */
	@Override
	public String getName() {
		return SET_JOB_PARAM;
	}

	@Override
	public String getDescription() {
		return "Sets the value of a job attribute/parameter of the specified job. (URL: {baseUWS_URL}/{jobListName}/{job-id}/{job-attribute}, Method: HTTP-POST or HTTP-PUT, Parameter: {JOB-ATTRIBUTE}={attribute-value})";
	}

	/**
	 * Checks whether:
	 * <ul>
	 * 	<li>a job list name is specified in the given UWS URL <i>(<u>note:</u> by default, the existence of the jobs list is not checked)</i>,</li>
	 * 	<li>a job ID is given in the UWS URL <i>(<u>note:</u> by default, the existence of the job is not checked)</i>,</li>
	 * 	<li>if the HTTP method is HTTP-POST: there is exactly one attribute <b>and</b> at least one parameter</li>
	 * 	<li>if the HTTP method is HTTP-PUT: there are at least two attributes ({@link AbstractJob#PARAM_PARAMETERS}/{parameter_name}) <b>and</b> there are at least two parameters</li>
	 * </ul>
	 * 
	 * @see uws.service.actions.UWSAction#match(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean match(UWSUrl urlInterpreter, String userId, HttpServletRequest request) throws UWSException {
		return (urlInterpreter.hasJobList()
				&& urlInterpreter.hasJob()
				&& (	
						(request.getMethod().equalsIgnoreCase("post")
						&& (!urlInterpreter.hasAttribute() || urlInterpreter.getAttributes().length == 1)
							&& request.getParameterMap().size() > 0)
					||
						(request.getMethod().equalsIgnoreCase("put")
						&& urlInterpreter.getAttributes().length >= 2
						&& urlInterpreter.getAttributes()[0].equalsIgnoreCase(AbstractJob.PARAM_PARAMETERS)
						&& request.getParameter(urlInterpreter.getAttributes()[1]) != null)
					)
				);
	}

	/**
	 * <p>Gets the specified job <i>(and throw an error if not found)</i>,
	 * changes the value of the specified job attribute
	 * and makes a redirection to the job summary.</p>
	 * 
	 * @see #getJob(UWSUrl)
	 * @see UWSToolBox#getParamsMap(HttpServletRequest)
	 * @see ExecutionDurationController#init(Map)
	 * @see DestructionTimeController#init(Map)
	 * @see AbstractJob#addOrUpdateParameters(java.util.Map)
	 * @see AbstractUWS#redirect(String, HttpServletRequest, HttpServletResponse)
	 * 
	 * @see uws.service.actions.UWSAction#apply(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean apply(UWSUrl urlInterpreter, String userId, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
	// Get the job:
		J job = getJob(urlInterpreter);
		
		Map<String, String> params = UWSToolBox.getParamsMap(request);
		
	// Control the execution duration:
		uws.getExecutionDurationController().control(job, params);
		
	// Control the destruction time:
		uws.getDestructionTimeController().control(job, params);
		
	// Update the job parameters:
		boolean updated = job.addOrUpdateParameters(params);

	// Make a redirection to the job:
		uws.redirect(urlInterpreter.jobSummary(urlInterpreter.getJobListName(), job.getJobId()).getRequestURL(), request, response);
		
		return updated;
	}
	
}
