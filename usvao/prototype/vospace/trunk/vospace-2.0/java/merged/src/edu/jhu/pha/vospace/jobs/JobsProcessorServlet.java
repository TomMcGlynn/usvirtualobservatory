/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package edu.jhu.pha.vospace.jobs;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.node.Node;

/**
 * The servlet runs a jobs processor according to the jobsprocessor.class setting
 * @author Dmitry Mishin, JHU
 *
 */
public class JobsProcessorServlet extends HttpServlet  {

	private static final long serialVersionUID = 8829039248294774005L;
	private static final Logger logger = Logger.getLogger(JobsProcessorServlet.class);
	static Configuration conf = SettingsServlet.getConfig();;
	private static JobsProcessor processor = null;

	static {
		try {
			Class jobsHandlerClass = Class.forName(conf.getString("jobsprocessor.class"));
			processor = (JobsProcessor)jobsHandlerClass.newInstance();
		} catch(ClassNotFoundException e){
			logger.error("Erorr initializing the JobsProcessorServlet: "+e.getMessage());
		} catch (InstantiationException e) {
			logger.error("Erorr initializing the JobsProcessorServlet: "+e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("Erorr initializing the JobsProcessorServlet: "+e.getMessage());
		}

	}

	@Override
	public void init() throws ServletException {
		String runJobsProc = getServletConfig().getInitParameter("processJobs");
		if(null == runJobsProc || Boolean.parseBoolean(runJobsProc)) {
			processor.start();
		}
	}
	
	@Override
	public void destroy() {
		processor.destroy();
	}

}
