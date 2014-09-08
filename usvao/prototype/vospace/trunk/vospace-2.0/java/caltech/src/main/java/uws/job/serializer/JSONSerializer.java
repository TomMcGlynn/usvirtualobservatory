package uws.job.serializer;

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

import uws.UWSException;
import uws.job.AbstractJob;
import uws.job.ErrorSummary;
import uws.job.JobList;
import uws.job.Result;
import uws.service.AbstractUWS;
import uws.service.UWSUrl;

/**
 * Lets serializing any UWS resource in JSON.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class JSONSerializer extends UWSSerializer {
	private static final long serialVersionUID = 1L;
	
	/** Tab to use just before a JSON item. */
	protected String tabPrefix = "";
	
	
	@Override
	public final String getMimeType() {
		return MIME_TYPE_JSON;
	}

	@Override
	public String getUWS(final AbstractUWS<? extends JobList<? extends AbstractJob>, ? extends AbstractJob> uws, final String userId) throws UWSException {
		String json = "{\n\t\"name\": \""+uws.getName()+"\",";
		if (uws.getDescription() != null)
			json += "\n\t\"description\": \""+uws.getDescription()+"\",";
		
		json += "\n\t\"jobLists\": [";
		Iterator<? extends JobList<? extends AbstractJob>> it = uws.iterator();
		while(it.hasNext()){
			JobList<? extends AbstractJob> jobList = it.next();
			UWSUrl jlUrl = jobList.getUrl();
			String url = (jlUrl==null)?null:jlUrl.getRequestURL();
			json += "\n\t\t{ \"name\": \""+jobList.getName()+"\", \"href\": "+((url == null)?"":url)+"\" }"+(it.hasNext()?",":"");
		}
		
		return json+"\n\t]\n}";
	}

	@Override
	public String getJobList(final JobList<? extends AbstractJob> jobsList, final String ownerId, final boolean root) throws UWSException {
		UWSUrl jobsListUrl = jobsList.getUrl();
		
		String json = "{\n\t\"name\": \""+jobsList.getName()+"\",\n\t\"jobs\": [";
		Iterator<? extends AbstractJob> it = jobsList.getJobs(ownerId);
		while(it.hasNext()){
			AbstractJob j = it.next();
			String url = null;
			if (jobsListUrl != null){
				jobsListUrl.setJobId(j.getJobId());
				url = jobsListUrl.getRequestURL();
			}
			json += "\n\t\t{ \"id\": \""+j.getJobId()+"\", \"href\": \""+((url==null)?"":url)+"\", "+getPhase(j, false)+" }"+(it.hasNext()?",":"");
		}
		json += "\n\t]\n}";
		
		return json;
	}

	@Override
	public String getJob(final AbstractJob job, final boolean root) throws UWSException {
		String json = "{ "
				+"\n\t"+getJobID(job, false)
				+",\n\t"+getRunID(job, false)
				+",\n\t"+getOwnerID(job, false)
				+",\n\t"+getPhase(job, false)
				+",\n\t"+getQuote(job, false)
				+",\n\t"+getStartTime(job, false)
				+",\n\t"+getEndTime(job, false)
				+",\n\t"+getExecutionDuration(job, false)
				+",\n\t"+getDestructionTime(job, false);
		
		tabPrefix += "\t";
		
		// parameters:
		json += ",\n"+getAdditionalParameters(job, false);
		
		// results:
		json += ",\n"+getResults(job, false);
		
		// errorSummary:
		if (job.getErrorSummary() != null)
			json += ",\n"+getErrorSummary(job.getErrorSummary(), false);
		
		tabPrefix = "";
		return json+"\n}";
	}

	@Override
	public String getJobID(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"jobId\": \""+job.getJobId()+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getRunID(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"runId\": \""+((job.getRunId() == null)?"":job.getRunId())+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getOwnerID(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"owner\": \""+((job.getOwner() == null)?"":job.getOwner())+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getPhase(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"phase\": \""+job.getPhase()+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getQuote(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"quote\": \""+job.getQuote()+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getExecutionDuration(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"executionDuration\": \""+job.getExecutionDuration()+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getDestructionTime(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"destruction\": \""+((job.getDestructionTime() != null)?job.getDateFormat().format(job.getDestructionTime()):"")+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getStartTime(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"startTime\": \""+((job.getStartTime() != null)?job.getDateFormat().format(job.getStartTime()):"")+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getEndTime(final AbstractJob job, final boolean root) throws UWSException {
		String json = "\"endTime\": \""+((job.getEndTime() != null)?job.getDateFormat().format(job.getEndTime()):"")+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getErrorSummary(final ErrorSummary error, final boolean root) throws UWSException {
		String json = "\"error\": {";
		
		if (error == null)
			json += "}";
		else{
			json = "\n\t"+tabPrefix
					+"\"type\": \""+error.getType()+"\",\n\t"+tabPrefix
					+"\"hasDetail\": \""+error.hasDetail()+"\",\n\t"+tabPrefix
					+"\"message\": \""+error.getMessage().replaceAll("\"", "'")+"\"\n"+tabPrefix+"}";
		}
		return tabPrefix+(root?("{ "+json+" }"):json);
	}

        @Override
	public String getJobInfo(final String jobInfo, final boolean root) throws UWSException {
	    return "";
	}

	@Override
	public String getAdditionalParameters(final AbstractJob job, final boolean root) throws UWSException {
		String json = null;
		for(String paramName : job.getAdditionalParameters()){
			if (json == null) json = "\n\t"+tabPrefix;
			else			  json += "\t,";
			json += getAdditionalParameter(paramName, job.getAdditionalParameterValue(paramName), false) +"\n"+tabPrefix;
		}
		return tabPrefix+(root?"{ ":"")+"\"parameters\": ["+(json==null?"":json)+"]"+(root?("\n"+tabPrefix+"}"):"");
	}

	@Override
	public String getAdditionalParameter(final String paramName, final String paramValue, final boolean root) throws UWSException {
		String json = "\""+paramName+"\": \""+paramValue+"\"";
		return root?("{ "+json+" }"):json;
	}

	@Override
	public String getResults(final AbstractJob job, final boolean root) throws UWSException {
		String json = null, oldTabPrefix = tabPrefix;
		Iterator<Result> it = job.getResults();
		while(it.hasNext()){
			if (json == null) json = "\n\t"+tabPrefix;
			else			  json += "\t,";
			json += getResult(it.next(), false) +"\n"+tabPrefix;
		}
		tabPrefix = oldTabPrefix;
		return tabPrefix+(root?"{ ":"")+"\"results\": ["+((json == null)?"":json)+"]"+(root?("\n"+tabPrefix+"}"):"");
	}

	@Override
	public String getResult(final Result result, final boolean root) throws UWSException {
		String json = "{"+" \"id\": \""+result.getId()+"\", \"type\": \""+result.getType()+"\", \"href\": \""+result.getHref()+"\" }";
		return root?("{ "+json+" }"):json;
	}

}
