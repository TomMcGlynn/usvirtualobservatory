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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.JobList;
import uws.job.serializer.UWSSerializer;

import uws.service.AbstractUWS;
import uws.service.UWSUrl;

/**
 * <p>The "Get Job Parameter" action of a UWS.</p>
 * 
 * <p><i><u>Note:</u> The corresponding name is {@link UWSAction#GET_JOB_PARAM}.</i></p>
 * 
 * <p>This action returns the value of the specified job attribute.
 * If the attribute is basic (i.e. jobID, startTime, ...) the value is returned with the content type <i>text/plain</i>
 * whereas it is a complex type (i.e. results, parameters, ...) the value is the serialization of the job attribute itself.
 * The serializer is choosen in function of the HTTP Accept header.</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 01/2011
 */
public class GetJobParam<JL extends JobList<J>, J extends AbstractJob> extends UWSAction<JL, J> {
	private static final long serialVersionUID = 1L;

	public GetJobParam(AbstractUWS<JL, J> u){
		super(u);
	}

	/**
	 * @see UWSAction#GET_JOB_PARAM
	 * @see uws.service.actions.UWSAction#getName()
	 */
	@Override
	public String getName() {
		return GET_JOB_PARAM;
	}

	@Override
	public String getDescription() {
		return "Gets the value of a job attribute/parameter of the specified job. (URL: {baseUWS_URL}/{jobListName}/{job-id}/{job-attribute}, Method: HTTP-GET, No parameter)";
	}

	/**
	 * Checks whether:
	 * <ul>
	 * 	<li>a job list name is specified in the given UWS URL <i>(<u>note:</u> the existence of the jobs list is not checked)</i>,</li>
	 * 	<li>a job ID is given in the UWS URL <i>(<u>note:</u> the existence of the job is not checked)</i>,</li>
	 * 	<li>there is a job attribute,</li>
	 * 	<li>the HTTP method is HTTP-GET.</li>
	 * </ul>
	 * 
	 * @see uws.service.actions.UWSAction#match(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public boolean match(UWSUrl urlInterpreter, String userId, HttpServletRequest request) throws UWSException {
		return (urlInterpreter.hasJobList()
				&& urlInterpreter.hasJobList()
				&& urlInterpreter.hasAttribute()
				&& request.getMethod().equalsIgnoreCase("get"));
	}

	/**
	 * <p>Gets the specified job <i>(and throw an error if not found)</i>,
	 * chooses the serializer and write the serialization of the job attribute in the given response.</p>
	 * 
	 * <p><i><u>Note:</u> if the specified attribute is simple (i.e. jobID, runID, startTime, ...) it will not serialized ! The response will
	 * merely be the job attribute value (so, the content type will be: text/plain).</i></p>
	 * 
	 * @see #getJob(UWSUrl)
	 * @see AbstractUWS#getSerializer(String)
	 * @see AbstractJob#serialize(java.io.OutputStream, UWSSerializer)
	 * 
	 * @see uws.service.actions.UWSAction#apply(uws.service.UWSUrl, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public boolean apply(UWSUrl urlInterpreter, String userId, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
	// Get the job:
		J job = getJob(urlInterpreter);
		
	// Write the value/content of the selected attribute:
		UWSSerializer serializer = uws.getSerializer(request.getHeader("Accept"));
		String attribute = urlInterpreter.getAttributes()[0];
		if (attribute == null || attribute.trim().length() == 0 || (urlInterpreter.getAttributes().length <= 1 && (attribute.equalsIgnoreCase(AbstractJob.PARAM_ERROR_SUMMARY) || attribute.equalsIgnoreCase(AbstractJob.PARAM_RESULTS) || attribute.equalsIgnoreCase(AbstractJob.PARAM_PARAMETERS))))
			response.setContentType(serializer.getMimeType());
		else
			response.setContentType("text/plain");
		job.serialize(response.getOutputStream(), urlInterpreter.getAttributes(), serializer);
		
		return true;
	}
	
}
