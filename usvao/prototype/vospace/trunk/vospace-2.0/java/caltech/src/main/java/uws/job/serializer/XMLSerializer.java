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
 * Lets serializing any UWS resource in XML.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 01/2011
 */
public class XMLSerializer extends UWSSerializer {
	private static final long serialVersionUID = 1L;

	/** Tab to add just before each next XML node. */
	protected String tabPrefix = "";
	
	/** The path of the XSLT style-sheet. */
	protected String xsltPath = null;
	
	
	/**
	 * Builds a XML serializer.
	 */
	public XMLSerializer(){ ; }
	
	/**
	 * Builds a XML serializer with a XSLT link.
	 * 
	 * @param xsltPath	Path of a XSLT style-sheet.
	 */
	public XMLSerializer(final String xsltPath){
		this.xsltPath = xsltPath;
	}
	
	/**
	 * Gets the path/URL of the XSLT style-sheet to use.
	 * 
	 * @return	XSLT path/url.
	 */
	public final String getXSLTPath(){
		return xsltPath;
	}
	
	/**
	 * Sets the path/URL of the XSLT style-sheet to use.
	 * 
	 * @param path	The new XSLT path/URL.
	 */
	public final void setXSLTPath(final String path){
		xsltPath = path;
	}
	
	/**
	 * <p>Gets the XML file header (xml version, encoding and the xslt style-sheet link if any).</p>
	 * <p>It is always called by the implementation of the UWSSerializer functions
	 * if their boolean parameter (<i>root</i>) is <i>true</i>.</p>
	 * 
	 * @return	The XML file header.
	 */
	public String getHeader(){
		String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		if (xsltPath != null && xsltPath.trim().length() > 0)
			xmlHeader += "<?xml-stylesheet type=\"text/xsl\" href=\""+xsltPath+"\"?>\n";
		return xmlHeader;
	}
	
	/**
	 * Gets all UWS namespaces declarations needed for an XML representation of a UWS object.
	 * 
	 * @return	The UWS namespaces: <br /> (i.e. <i>= "xmlns:uws=[...] xmlns:xlink=[...] xmlns:xs=[...] xmlns:xsi=[...]"</i>).
	 */
	public String getUWSNamespace(){
		return "xmlns:uws=\"http://www.ivoa.net/xml/UWS/v1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
	}
	
	/**
	 * Gets the node attributes which declare the UWS namespace.
	 * 
	 * @param root	<i>false</i> if the attribute to serialize will be included
	 * 				in a top level serialization (for a job attribute: job), <i>true</i> otherwise.
	 * 
	 * @return		"" if <i>root</i> is <i>false</i>, " "+UWSNamespace otherwise.
	 * 
	 * @see #getUWSNamespace()
	 */
	protected final String getUWSNamespace(boolean root){
		if (root)
			return " "+getUWSNamespace();
		else
			return "";
	}

	@Override
	public final String getMimeType(){
		return MIME_TYPE_XML;
	}

	@Override
	public String getUWS(final AbstractUWS<? extends JobList<? extends AbstractJob>, ? extends AbstractJob> uws, final String userId) {
		String name = uws.getName(), description = uws.getDescription();
		
		String xml = getHeader()+"<uws"+getUWSNamespace(true)+((name!=null)?(" name=\""+name+"\""):"")+">\n";
		if (description != null)
			xml += "\t<description>\n"+description+"\n\t</description>\n";
		
		xml += "\t<jobLists>\n";
		for(JobList<? extends AbstractJob> jobList : uws){
			UWSUrl jlUrl = jobList.getUrl();
			String url = (jlUrl==null)?null:jlUrl.getRequestURL();
			xml += "\t\t<jobListRef name=\""+jobList.getName()+"\" href=\""+((url == null)?"":url)+"\" />\n";
		}
		xml += "\t</jobLists>\n";
		
		xml += "</uws>\n";
		
		return xml;
	}

	@Override
	public String getJobList(final JobList<? extends AbstractJob> jobsList, final String ownerId, final boolean root) throws UWSException {
		String name = jobsList.getName();
		UWSUrl jobsListUrl = jobsList.getUrl();
		
		String xml = getHeader()+"<uws:jobList"+getUWSNamespace(true)+((name==null || name.length() == 0)?"":(" name=\""+name+"\""))+">";
		Iterator<? extends AbstractJob> it = jobsList.getJobs(ownerId);
		while(it.hasNext()){
			AbstractJob j = it.next();
			String url = null;
			if (jobsListUrl != null){
				jobsListUrl.setJobId(j.getJobId());
				url = jobsListUrl.getRequestURL();
			}
			xml += "\n\t<uws:jobRef id=\""+j.getJobId()+"\" xlink:href=\""+((url==null)?"":url)+"\">\n\t\t"+getPhase(j, false)+"\n\t</uws:jobRef>";
		}
		xml += "\n</uws:jobList>";
		
		return xml;
	}

	@Override
	public String getJob(final AbstractJob job, final boolean root) {
		String xml = root?getHeader():"";
		
		// general information:
		xml += "<uws:job"+getUWSNamespace(root)+">"
				+"\n\t"+getJobID(job, false)
				+"\n\t"+getRunID(job, false)
				+"\n\t"+getOwnerID(job, false)
				+"\n\t"+getPhase(job, false)
				+"\n\t"+getQuote(job, false)
				+"\n\t"+getStartTime(job, false)
				+"\n\t"+getEndTime(job, false)
				+"\n\t"+getExecutionDuration(job, false)
				+"\n\t"+getDestructionTime(job, false);

		tabPrefix = "\t";
		
		// parameters:
		xml += "\n"+getAdditionalParameters(job, false);
		
		// results:
		xml += "\n"+getResults(job, false);
		
		// errorSummary:
		xml += "\n"+getErrorSummary(job.getErrorSummary(), false);
		
		// jobInfo:
		xml += "\n"+getJobInfo(job.getJobInfo(), false);

		tabPrefix = "";
		return xml + "\n</uws:job>";
	}

	@Override
	public String getJobID(final AbstractJob job, final boolean root) {
		return (root?getHeader():"")+"<uws:jobId"+getUWSNamespace(root)+">"+job.getJobId()+"</uws:jobId>";
	}

	@Override
	public String getRunID(final AbstractJob job, final boolean root) {
		String name = job.getRunId();
		if (name == null)
			name = "";
		
		String xml = (name.length()==0)?"<uws:runId"+getUWSNamespace(root)+" xsi:nil=\"true\" />":("<uws:runId"+getUWSNamespace(root)+">"+name+"</uws:runId>");
		return (root?getHeader():"")+xml;
	}

	@Override
	public String getOwnerID(final AbstractJob job, final boolean root) {
		String owner = job.getOwner();
		if (owner == null)
			owner = "";
		
		String xml = (owner.length()==0)?"<uws:ownerId"+getUWSNamespace(root)+" xsi:nil=\"true\" />":("<uws:ownerId"+getUWSNamespace(root)+">"+owner+"</uws:ownerId>");
		return (root?getHeader():"")+xml;
	}

	@Override
	public String getPhase(final AbstractJob job, final boolean root) {
		return (root?getHeader():"")+"<uws:phase"+getUWSNamespace(root)+">"+job.getPhase()+"</uws:phase>";
	}

	@Override
	public String getQuote(final AbstractJob job, final boolean root) {
		String quote = (job.getQuote()<=0)?"":(job.getQuote()+"");
		String xml = quote.length()==0?"<uws:quote"+getUWSNamespace(root)+" xsi:nil=\"true\" />":("<uws:quote"+getUWSNamespace(root)+">"+quote+"</uws:quote>");
		return (root?getHeader():"")+xml;
	}

	@Override
	public String getStartTime(final AbstractJob job, final boolean root) {
		String time = "";
		if (job.getStartTime() != null)
			time = job.getDateFormat().format(job.getStartTime());
		
		String xml = (time.length()==0)?("<uws:startTime"+getUWSNamespace(root)+" xsi:nil=\"true\" />"):("<uws:startTime"+getUWSNamespace(root)+">"+time+"</uws:startTime>");
		return (root?getHeader():"")+xml;
	}

	@Override
	public String getEndTime(final AbstractJob job, final boolean root) {
		String time = "";
		if (job.getEndTime() != null)
			time = job.getDateFormat().format(job.getEndTime());
		
		String xml = (time.length()==0)?("<uws:endTime"+getUWSNamespace(root)+" xsi:nil=\"true\" />"):("<uws:endTime"+getUWSNamespace(root)+">"+time+"</uws:endTime>");
		return (root?getHeader():"")+xml;
	}

	@Override
	public String getDestructionTime(final AbstractJob job, final boolean root) {
		String time = "";
		if (job.getDestructionTime() != null)
			time = job.getDateFormat().format(job.getDestructionTime());
		
		String xml = (time.length()==0)?("<uws:destruction"+getUWSNamespace(root)+" xsi:nil=\"true\" />"):("<uws:destruction"+getUWSNamespace(root)+">"+time+"</uws:destruction>");
		return (root?getHeader():"")+xml;
	}

	@Override
	public String getExecutionDuration(final AbstractJob job, final boolean root) {
		return (root?getHeader():"")+"<uws:executionDuration"+getUWSNamespace(root)+">"+job.getExecutionDuration()+"</uws:executionDuration>";
	}

	@Override
	public String getErrorSummary(final ErrorSummary error, final boolean root) {
		if (error != null){
			String xml = (root?getHeader():"")+tabPrefix+"<uws:errorSummary"+getUWSNamespace(root)+" type=\""+error.getType()+"\""+" hasDetail=\""+error.hasDetail()+"\">";
			xml += "\n\t"+tabPrefix+"<uws:message>"+error.getMessage()+"</uws:message>";
			xml += "\n"+tabPrefix+"</uws:errorSummary>";
			return xml;
		}else
			return (root?getHeader():"")+tabPrefix+"<uws:errorSummary"+getUWSNamespace(root)+" xsi:nil=\"true\" />";
	}

        @Override
	public String getJobInfo(final String jobInfo, final boolean root) {
	    if (jobInfo != null) {
		String xml = (root?getHeader():"")+tabPrefix+"<uws:jobInfo"+getUWSNamespace(root)+">";
		xml += "\n\t"+tabPrefix+jobInfo;
		xml += "\n"+tabPrefix+"</uws:jobInfo>";
		return xml;
	    } else {
		return "";
	    }
	}

	@Override
	public String getAdditionalParameters(final AbstractJob job, final boolean root) {
		String xml = (root?getHeader():"")+tabPrefix+"<uws:parameters"+getUWSNamespace(root)+">";
		for(String paramName : job.getAdditionalParameters())
			xml += "\n\t"+tabPrefix+getAdditionalParameter(paramName, job.getAdditionalParameterValue(paramName), false);
		xml += "\n"+tabPrefix+"</uws:parameters>";
		return xml;
	}

	@Override
	public String getAdditionalParameter(final String paramName, final String paramValue, final boolean root) {
		if (paramName != null && paramValue != null){
			if (root)
				return paramValue;
			else
				return "<uws:parameter"+getUWSNamespace(root)+" id=\""+paramName+"\"><![CDATA["+paramValue+"]]></uws:parameter>";
		}else
			return "";
	}

	@Override
	public String getResults(final AbstractJob job, final boolean root) {
		String xml = (root?getHeader():"")+tabPrefix+"<uws:results"+getUWSNamespace(root)+">";
		Iterator<Result> it = job.getResults();
		while(it.hasNext())
			xml += "\n\t"+tabPrefix+getResult(it.next(), false);
		xml += "\n"+tabPrefix+"</uws:results>";
		return xml;
	}

	@Override
	public String getResult(final Result result, final boolean root) {
		return (root?getHeader():"")+"<uws:result"+getUWSNamespace(root)+" id=\""+result.getId()+"\""+
				((result.getType()==null)?"":(" xlink:type=\""+result.getType()+"\""))+
				((result.getHref()==null)?"":(" xlink:href=\""+result.getHref()+"\""))+
				" />";
	}

}
