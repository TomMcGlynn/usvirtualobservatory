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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import java.util.Map;

import uws.UWSException;

import uws.job.AbstractJob;
import uws.job.JobList;

/**
 * <p>Convenient implementation of {@link AbstractUWS}.</p>
 * 
 * <p>You do not have to extend this implementation to create a job. The function {@link BasicUWS#createJob(Map)} uses
 * the {@link Constructor} found in the {@link Class} given at the instantiation of this UWS to create a new job.
 * Thus whatever is the type of the extension of {@link AbstractJob} this method will always call its constructor
 * which contains a Map<String,String> as only parameter.</p>
 * 
 * <p><b><u>IMPORTANT:</u> THE EXTENSION OF {@link AbstractJob} <u>MUST</u> CONTAIN A CONSTRUCTOR
 * WITH ONLY ONE PARAMETER OF TYPE MAP&lt;STRING,STRING&gt; !!!</b></p>
 * 
 * <u>Example:</u>
 * <pre>
 *	public class MyServlet extends HttpServlet {
 * 
 * 		private BasicUWS&lt;JobImpl&gt; uws = null;
 * 
 * 		public void init(ServletConfig conf) throws ServletException {
 * 			super.init(conf);
 *			try{
 *				uws = new BasicUWS&lt;JobImpl&gt;(<b>JobImpl.class</b>);
 *				<b>uws.addJobList(new JobList&lt;JobImpl&gt;("myJobList"));</b>
 *			}catch(UWSException ex){
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
public class BasicUWS<J extends AbstractJob> extends AbstractUWS<JobList<J>, J> {
	private static final long serialVersionUID = 1L;
	
	/** The class object of the type of job to manage. */
	protected final Class<J> jobClass;
	
	/** The constructor to use when the method {@link BasicUWS#createJob(Map)} is called. */
	protected transient Constructor<J> constructor;
	
/* *********** */
/* CONSTRUCTOR */
/* *********** */
	/**
	 * Builds a BasicUWS with the class object of the type of job to manage.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * 
	 * @throws UWSException		<ul><li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li></ul>
	 * 
	 * @see AbstractUWS#AbstractUWS()
	 * @see #getConstructor()
	 */
	public BasicUWS(Class<J> cl) throws UWSException {
		super();
		jobClass = cl;
		getConstructor(true);
	}

	/**
	 * Builds a BasicUWS with the class object of the type of job to manage and the base UWS URI.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * @param baseURI			The base UWS URI.
	 * 
	 * @throws UWSException		<ul><li>If there is an error when calling the constructor super(String).</li>
	 * 							<li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li></ul>
	 * 
	 * @see AbstractUWS#AbstractUWS(String)
	 * @see #getConstructor()
	 */
	public BasicUWS(Class<J> cl, String baseURI) throws UWSException {
		super(baseURI);
		jobClass = cl;
		getConstructor(true);
	}

	/**
	 * Builds a BasicUWS with the class object of the type of job to manage and the base UWS URI.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * @param baseURI			The base UWS URI.
	 * @param paramMap                      Are the arguments for the job passed in parameters
	 * 
	 * @throws UWSException		<ul><li>If there is an error when calling the constructor super(String).</li>
	 * 							<li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li></ul>
	 * 
	 * @see AbstractUWS#AbstractUWS(String)
	 * @see #getConstructor()
	 */
	public BasicUWS(Class<J> cl, String baseURI, boolean paramMap) throws UWSException {
	    	super(baseURI);
		jobClass = cl;
		getConstructor(paramMap);
	}

	/**
	 * Builds a BasicUWS with the class object of the type of job to manage and the UWS URL interpreter to use.
	 * 
	 * @param cl				The class object of the type of job to manage.
	 * @param urlInterpreter	The UWS URL interpreter to use in this UWS.
	 * 
	 * @throws UWSException		<ul><li>If the given class is abstract.</li>
	 * 							<li>If the constructor with one parameter of type Map< String, String > doesn't exist for the given class object.</li>
	 * 							<li>If any other error occurs during the fetching of the good constructor.</li></ul>
	 * 
	 * @see AbstractUWS#AbstractUWS(UWSUrl)
	 * @see #getConstructor()
	 */
	public BasicUWS(Class<J> cl, UWSUrl urlInterpreter) throws UWSException {
		super(urlInterpreter);
		jobClass = cl;
		getConstructor(true);
	}

	/**
	 * <p>Gets the constructor of the type of job to use.
	 * This constructor has only one parameter of type Map&lt;String,String&gt;.</p>
	 * 
	 * <p><i><u>Note:</u> If this UWS has just been de-serialized,
	 * the constructor is extracted one more time from the stored class object !</i></p>
	 * 
	 * @param paramMap                      Are the arguments for the job passed in parameters
	 *
	 * @return				The constructor of the type of job to use.
	 * 
	 * @throws UWSException	If it is impossible to extract the constructor with one parameter
	 * 						(Map&lt;String, String&gt;) from the stored class object.
	 */
	protected final Constructor<J> getConstructor(boolean paramMap) throws UWSException {
		if (constructor == null){
			if (jobClass == null)
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] Impossible to build a BasicUWS with a NULL Class object !");
			else if (Modifier.isAbstract(jobClass.getModifiers()))
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] The class \""+jobClass.getName()+"\" is abstract. It is impossible to build a BasicUWS object with an abstract class !");
	
			try {
			        constructor = paramMap ? jobClass.getConstructor(Map.class) : jobClass.getConstructor(String.class);
			} catch (Exception e) {
				e.printStackTrace();
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Get job constructor] Impossible to fetch the constructor with only one parameter of type HashMap<String, String> into the class \""+jobClass.getName()+"\" !");
			}
		}
		return constructor;
	}
	
/* **************** */
/* INHERITED METHOD */
/* **************** */
	/** 
	 * @see AbstractUWS#createJob(java.util.Map)
	 * @see #getConstructor()
	 * @see Constructor#newInstance(Object...)
	 */
	@Override
	public J createJob(final Map<String,String> parameters) throws UWSException {
		try {
			try{
				return getConstructor(true).newInstance(parameters);
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
	 * @see #getConstructor()
	 * @see Constructor#newInstance(Object...)
	 */
	@Override
	public J createJob(final String document) throws UWSException {
		try {
			try{
				return getConstructor(false).newInstance(document);
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
	

	
}
