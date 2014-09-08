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

import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.JobList;

import uws.service.actions.AddJob;

/**
 * <p>Convenient implementation of {@link AbstractUWS}.</p>
 * 
 * <p>Contrary to {@link BasicUWS} this implementation of a UWS can manage several JobLists of a different kind of Job.
 * That means it is possible to manage a JobList of JobA and another JobList of JobB whereas {@link BasicUWS} could manage
 * several JobList only of JobA or of JobB.</p>
 * 
 * <p><b><u>IMPORTANT:</u> THE EXTENSIONS OF {@link AbstractJob} <u>MUST</u> CONTAIN A CONSTRUCTOR
 * WITH ONLY ONE PARAMETER OF TYPE MAP&lt;STRING,STRING&gt; !!!</b></p>
 * 
 * <u>Example:</u>
 * <pre>
 *	public class MyServlet extends HttpServlet {
 * 
 * 		private ExtendedUWS uws = null;
 * 
 * 		public void init(ServletConfig conf) throws ServletException {
 *			super.init(conf);
 *			try{
 *				<b>uws = new ExtendedUWS();
 *				uws.addJobList(new JobList&lt;JobA&gt;("JobListA"), JobA.class);
 *				uws.addJobList(new JobList&lt;JobB&gt;("JobListB"), JobB.class);</b>
 *			}catch(UWSException ex){
 *				ex.printStackTrace();
 *				throw new ServletException(ex);
 *			}
 * 		}
 * 
 * 		public void destroy(){
 *			uws.removeAllJobLists();
 *			super.destroy();
 * 		}
 *
 *		public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 *			try{
 *				uws.executeRequest(request, response);
 *			}catch(UWSException ex){
 *				res.sendError(uwsEx.getHttpErrorCode(), uwsEx.getMessage());
 *			}
 * 		}
 * 	}
 * </pre>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class ExtendedUWS extends AbstractUWS<JobList<AbstractJob>, AbstractJob> {
	private static final long serialVersionUID = 1L;
	
	/** Association between each JobList and the class object of the type of job that this JobList manages. */
	protected final Map<JobList<? extends AbstractJob>, Class<? extends AbstractJob>> assocJobListClass;

	/** Association between each JobList and the constructor to use when a job must be created and added into this JobList. */
	protected transient Map<JobList<? extends AbstractJob>, Constructor<? extends AbstractJob>> assocJobListConstructor;
	
	/** Current JobList. (ALWAYS <i>null</i> EXCEPT WHILE ADDING A JOB !!) */
	protected JobList<? extends AbstractJob> jlDestination = null;

/* *********** */
/* CONSTRUCTOR */
/* *********** */
	/**
	 * Builds an ExtendedUWS.
	 * 
	 * @see AbstractUWS#AbstractUWS()
	 * @see #replaceUWSAction(uws.service.actions.UWSAction)
	 * @see AddJobWithConstructor
	 */
	public ExtendedUWS() {
		super();
		
		// Initialize the association maps:
		assocJobListClass = new HashMap<JobList<? extends AbstractJob>, Class<? extends AbstractJob>>();
		assocJobListConstructor = new HashMap<JobList<? extends AbstractJob>, Constructor<? extends AbstractJob>>();
		
		// Replace the AddJob action so that at each creation of a job the right constructor can be used:
		replaceUWSAction(new AddJobWithConstructor(this));
	}

	/**
	 * Builds an ExtendedUWS with its base URI.
	 * 
	 * @param baseURI		The base UWS URI.
	 * 
	 * @throws UWSException	If there is an error when calling the constructor super(String).
	 * 
	 * @see AbstractUWS#AbstractUWS(String)
	 * @see #replaceUWSAction(uws.service.actions.UWSAction)
	 * @see AddJobWithConstructor
	 */
	public ExtendedUWS(String baseURI) throws UWSException {
		super(baseURI);
		
		// Initialize the association maps:
		assocJobListClass = new HashMap<JobList<? extends AbstractJob>, Class<? extends AbstractJob>>();
		assocJobListConstructor = new HashMap<JobList<? extends AbstractJob>, Constructor<? extends AbstractJob>>();
		
		// Replace the AddJob action so that at each creation of a job the right constructor can be used:
		replaceUWSAction(new AddJobWithConstructor(this));
	}

	/**
	 * Builds an ExtendedUWS with the UWS URL interpreter to use.
	 * 
	 * @param urlInterpreter	The UWS URL interpreter this UWS must use.
	 * 
	 * @see AbstractUWS#AbstractUWS(UWSUrl)
	 * @see #replaceUWSAction(uws.service.actions.UWSAction)
	 * @see AddJobWithConstructor
	 */
	public ExtendedUWS(UWSUrl urlInterpreter) {
		super(urlInterpreter);
		
		// Initialize the association maps:
		assocJobListClass = new HashMap<JobList<? extends AbstractJob>, Class<? extends AbstractJob>>();
		assocJobListConstructor = new HashMap<JobList<? extends AbstractJob>, Constructor<? extends AbstractJob>>();
		
		// Replace the AddJob action so that at each creation of a job the right constructor can be used:
		replaceUWSAction(new AddJobWithConstructor(this));
	}

	/**
	 * <p>Gets the constructor of the type of job to use.
	 * This constructor has only one parameter of type Map&lt;String,String&gt;.</p>
	 * 
	 * <p><i><u>Note:</u> If this UWS has just been de-serialized,
	 * the constructor is extracted one more time from the stored class object !</i></p>
	 * 
	 * @param jl			The job list which manages the kind of job whose the constructor must be extracted.
	 * 
	 * @return				The constructor of the type of job to use.
	 * 
	 * @throws UWSException	If it is impossible to extract the constructor with one parameter
	 * 						(Map&lt;String, String&gt;) from a stored class object.
	 * 
	 * @see #addConstructor(JobList)
	 */
	protected final Constructor<? extends AbstractJob> getConstructor(JobList<? extends AbstractJob> jl) throws UWSException {
		try{
		// Re-build the map of constructors from the map of class if NULL:
		if (assocJobListConstructor == null){
			assocJobListConstructor = new HashMap<JobList<? extends AbstractJob>, Constructor<? extends AbstractJob>>();
			for(Map.Entry<JobList<? extends AbstractJob>, Class<? extends AbstractJob>> entry : assocJobListClass.entrySet())
				addConstructor(entry.getKey());
		}else
			addConstructor(jl);
		
		// Get the corresponding constructor (or if not found, throw an error):
		Constructor<? extends AbstractJob> constructor = assocJobListConstructor.get(jl);
		if (constructor != null)
			return constructor;
		else
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] Impossible to get the constructor of the type of job to create for the job list \""+jl.getName()+"\" !");
		}catch(UWSException ex){
			ex.printStackTrace();
			throw ex;
		}
	}
	
	/**
	 * Searches in {@link #assocJobListClass} for the Class object of the given jobs list,
	 * gets the constructor with only one parameter of type Map&lt;String,String&gt; into this Class object,
	 * and finally saves in {@link #assocJobListConstructor} the association between the given jobs list and the found constructor.
	 * 
	 * @param jl			The jobs list with which a job constructor must be associated.
	 * 
	 * @throws UWSException	If no corresponding Class object can be found
	 * 						or if the found class is abstract
	 * 						or if there is no constructor with only one parameter of type Map&lt;String,String&gt;.
	 */
	protected final void addConstructor(JobList<? extends AbstractJob> jl) throws UWSException {
		Class<? extends AbstractJob> jobClass = assocJobListClass.get(jl);
		if (jobClass == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] Impossible to get a job constructor from a NULL Class object !");
		else if (Modifier.isAbstract(jobClass.getModifiers()))
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] The class \""+jobClass.getName()+"\" is abstract. It is impossible to build a BasicUWS object with an abstract class !");
		else{
			try{
				assocJobListConstructor.put(jl, jobClass.getConstructor(Map.class));
			} catch (Exception e) {
				e.printStackTrace();
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] Impossible to fetch the constructor with only one parameter of type HashMap<String, String> into the class \""+jobClass.getName()+"\" !");
			}
		}
	}

/* ******* */
/* METHODS */
/* ******* */
	/**
	 * Adds a JobList with the constructor of the type of job to use.
	 * 
	 * @param <J>	The type of job that the given JobList manages.
	 * @param jl	The JobList to add.
	 * @param cl	The class of job <i>J</i> which are managed in the given JobList. 
	 * 
	 * @return		<i>true</i> if the JobList has been added successfully, <i>false</i> otherwise.
	 * 
	 * @throws UWSException	If the given class object corresponds to an abstract class
	 * 						or if the given class does not contain a constructor with only one parameter of type Map&lt;String,String&gt;.
	 */
	@SuppressWarnings("unchecked")
	public <J extends AbstractJob> boolean addJobList(JobList<J> jl, Class<J> cl) throws UWSException {
		boolean result = addJobList((JobList<AbstractJob>)jl);
		
		if (Modifier.isAbstract(cl.getModifiers()))
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Add job list] The class \""+cl.getName()+"\" is abstract. It is impossible to build a BasicUWS object with an abstract class !");
		
		if (result){
			// Add an association between the job list and the class object of the type of managed jobs:
			assocJobListClass.put(jl, cl);
			
			// Gets the constructor of the type of managed jobs:
			getConstructor(jl);
		}
		
		return result;
	}
	
/* ***************** */
/* INHERITED METHODS */
/* ***************** */
	@Override
	public boolean removeJobList(JobList<AbstractJob> jl) {
		boolean result = super.removeJobList(jl);
		
		if (result){
			assocJobListClass.remove(jl);
			if (assocJobListConstructor != null)
				assocJobListConstructor.remove(jl);
		}
		
		return result;
	}
	
	@Override
	public boolean destroyJobList(JobList<AbstractJob> jl) {
		boolean result = super.destroyJobList(jl);
		
		if (result){
			assocJobListClass.remove(jl);
			if (assocJobListConstructor != null)
				assocJobListConstructor.remove(jl);
		}
		
		return result;
	}

	/** 
	 * @see AbstractUWS#createJob(java.util.Map)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 */
	@Override
	public AbstractJob createJob(final Map<String, String> parameters) throws UWSException {
		// Gets the constructor corresponding to the JobList in which the Job to create will be added:
		Constructor<? extends AbstractJob> constructor = null;
		constructor = getConstructor(jlDestination);
		
		// Create the job thanks to the retrieved constructor:
		try {
			try{
				return constructor.newInstance(parameters);
			}catch(InvocationTargetException e){
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a job] "+e.getCause()+": "+e.getMessage());
			}
		} catch (Exception e) {
			if (e instanceof UWSException)
				throw (UWSException)e;
			else{
				e.printStackTrace();
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a job] Error while using the constructor \""+constructor.toString()+"\": "+e.getMessage());
			}
		}
	}
	
	/** 
	 * @see AbstractUWS#createJob(String)
	 * @see java.lang.reflect.Constructor#newInstance(Object...)
	 */
	@Override
	public AbstractJob createJob(final String document) throws UWSException {
		// Gets the constructor corresponding to the JobList in which the Job to create will be added:
		Constructor<? extends AbstractJob> constructor = null;
		constructor = getConstructor(jlDestination);
		
		// Create the job thanks to the retrieved constructor:
		try {
			try{
				return constructor.newInstance(document);
			}catch(InvocationTargetException e){
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a job] "+e.getCause()+": "+e.getMessage());
			}
		} catch (Exception e) {
			if (e instanceof UWSException)
				throw (UWSException)e;
			else{
				e.printStackTrace();
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Create a job] Error while using the constructor \""+constructor.toString()+"\": "+e.getMessage());
			}
		}
	}

	/**
	 * Action AddJob which initializes the attribute {@link ExtendedUWS#jlDestination} with the job list in which a job must be added.
	 * Then the original function {@link AddJob#apply(UWSUrl, String, HttpServletRequest, HttpServletResponse)} is called.
	 * Finally the attribute {@link ExtendedUWS#jlDestination} is reset to <i>null</i>.
	 * 
	 * @author Gr&eacute;gory Mantelet (CDS)
	 * @version 12/2010
	 */
	protected class AddJobWithConstructor extends AddJob<JobList<AbstractJob>, AbstractJob>{
		private static final long serialVersionUID = 1L;

		public AddJobWithConstructor(AbstractUWS<JobList<AbstractJob>, AbstractJob> u) {
			super(u);
		}

		@Override
		public boolean apply(UWSUrl urlInterpreter, String userId, HttpServletRequest request, HttpServletResponse response) throws UWSException, IOException {
			boolean resultApplied = false;
			((ExtendedUWS)uws).jlDestination = getJobsList(urlInterpreter);
			resultApplied = super.apply(urlInterpreter, userId, request, response);
			((ExtendedUWS)uws).jlDestination = null;
			return resultApplied;
		}
		
	}

}
