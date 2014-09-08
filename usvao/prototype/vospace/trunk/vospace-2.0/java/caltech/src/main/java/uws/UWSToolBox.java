package uws;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;

import uws.job.AbstractJob;
import uws.job.ErrorSummary;
import uws.job.ErrorType;
import uws.job.ExecutionPhase;

import uws.service.AbstractUWS;
import uws.service.UWSUrl;

/**
 * Some useful functions for the managing of a UWS service.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public class UWSToolBox {

	/** <b>THIS CLASS CAN'T BE INSTANTIATED !</b> */
	private UWSToolBox() { ; }
	
	/**
	 * <p>Lets building the absolute URL of any resource available in the root server, from a relative URL.</p>
	 * <p>For instance, if the server URL is http://foo.org/uwstuto (and whatever is the current URL):</p>
	 * <ul>
	 * 	<li><i>if you want the URL of the html page "basic.html" of the root directory:</i> servletPath=<b>"/basic.html"</b> => returned URL=<b>http://foo.org/uwstuto/basic.html</b></li>
	 * 	<li><i>if you want the URL of the image "ivoa.png" contained into the directory "images":</i> servletPath=<b>"/images/ivoa.png"</b> => returned URL=<b>"http://foo.org/uwstuto/images/ivoa.png"</b></li>
	 * </ul> 
	 * 
	 * @param serverPath	The relative path to access a server resource.
	 * @param req			A request of the servlet.
	 * 
	 * @return				The absolute URL to access the desired server resource
	 * 						or <i>null</i> if one of the parameter is <i>null</i> or if a well-formed URL can not be built.
	 * 
	 * @see HttpServletRequest#getRequestURL()
	 * @see HttpServletRequest#getContextPath()
	 * @see URL#URL(String)
	 */
	public static final URL getServerResource(String serverPath, HttpServletRequest req){
		if (serverPath == null || req == null)
			return null;
		
		try {
			if (!serverPath.startsWith("/"))
				serverPath = "/"+serverPath;
			
			return new URL(req.getRequestURL().substring(0, req.getRequestURL().lastIndexOf(req.getContextPath())+req.getContextPath().length())+serverPath);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
/* ****************** */
/* PARAMETERS METHODS */
/* ****************** */
	/**
	 * <p>Builds a map of strings with all parameters of the given HTTP request.</p>
	 * 
	 * <p><i><u>Note:</u> Actually it converts the Map&lt;String, <b>String[]</b>&gt; returned by {@link HttpServletRequest#getParameterMap()}
	 * into a Map&lt;String, <b>String</b>&gt; (the key is put in lower case).</i></p>
	 * 
	 * @param req	The HTTP request which contains the parameters to extract.
	 * @return		The corresponding map of string.
	 */
	public static final Map<String,String> getParamsMap(HttpServletRequest req){
		HashMap<String,String> params = new HashMap<String,String>(req.getParameterMap().size());
		
		Enumeration<?> e = req.getParameterNames();
		while(e.hasMoreElements()){
			Object obj = e.nextElement();
			params.put(obj.toString().toLowerCase(), req.getParameter(obj.toString())); 
		}
		
		return params;
	}
	
	/**
	 * <p>Builds a map of strings with all parameters of the given HTTP request
	 * and adds the given owner ID if not already in the request parameters.</p>
	 * 
	 * <p><i><u>Note:</u> Actually it converts the Map&lt;String, <b>String[]</b>&gt; returned by {@link HttpServletRequest#getParameterMap()}
	 * into a Map&lt;String, <b>String</b>&gt;.</i></p>
	 * 
	 * @param req		The HTTP request which contains the parameters to extract.
	 * @param userId	The ID of the current user/owner.
	 * 
	 * @return			The corresponding map of string.
	 */
	public static final Map<String,String> getParamsMap(HttpServletRequest req, String userId){
		HashMap<String,String> params = (HashMap<String,String>)getParamsMap(req);
		
		if (!params.containsKey(AbstractJob.PARAM_OWNER))
			params.put(AbstractJob.PARAM_OWNER, userId);
		
		return params;
	}
	
	/**
	 * Converts map of UWS parameters into a string corresponding to the query part of a HTTP-GET URL (i.e. ?EXECUTIONDURATION=60&DESTRUCTION=2010-09-01T13:58:00:000-0200).
	 * 
	 * @param parameters	A Map of parameters.
	 * @return				The corresponding query part of an HTTP-GET URL (all keys have been set in upper case).
	 */
	public final static String getQueryPart(Map<String, String> parameters){
		if (parameters == null || parameters.isEmpty())
			return "";
		
		StringBuffer queryPart = new StringBuffer();
		for(Map.Entry<String,String> e:parameters.entrySet()){
			String key = e.getKey();
			String val = e.getValue();
			
			if (key != null) key = key.trim().toUpperCase();
			if (val != null) val = val.trim();
			
			if(key != null && key.length() > 0 && val != null && val.length() > 0){
				queryPart.append(e.getKey()+"="+val);
				queryPart.append("&");
			}
		}
		
		return queryPart.substring(0, queryPart.length()-1);
	}
	
	/**
	 * Converts the given query part of a HTTP-GET URL to a map of parameters.
	 * 
	 * @param queryPart		A query part of a HTTP-GET URL.
	 * @return				The corresponding map of parameters (all keys have been set in lower case).
	 */
	public final static Map<String, String> getParameters(String queryPart){
		HashMap<String,String> parameters = new HashMap<String,String>();
		
		if (queryPart != null){
			queryPart = queryPart.substring(queryPart.indexOf("?")+1).trim();
			if (queryPart.length() > 0){
				String[] keyValues = queryPart.split("&");
				for(String item : keyValues){
					String[] keyValue = item.split("=");
					if (keyValue.length == 2){
						keyValue[0] = keyValue[0].trim().toLowerCase();
						keyValue[1] = keyValue[1].trim();
						if (keyValue[0].length() > 0 && keyValue[1].length() > 0)
							parameters.put(keyValue[0].trim(), keyValue[1].trim());
					}
				}
			}
		}
		
		return parameters;
	}

	/**
	 * <p>Extracts the contents of the given HTTP request as a String.</p>
    	 * 
	 * @param req	The HTTP request which contains the document to extract.
	 * @return		The corresponding string.
	 */
    public static final String getDocument(HttpServletRequest req, String userId){

	    StringBuilder body = new StringBuilder();
	    BufferedReader reader = null;
	    try {
		reader = req.getReader();
		char[] buf = new char[4 * 1024]; // 4KB char buffer
		int len;
		while ((len = reader.read(buf)) != -1) {
		    body.append(buf, 0, len);
		}
	    } catch (IOException e) {
		e.printStackTrace(System.err);
	    } finally {
		try {
		    reader.close();
		} catch (IOException e) {
		    e.printStackTrace(System.err);
		}
	    }
	    return body.toString();
	}
	
/* ************************ */
/* SAVE/RESTORATION METHODS */
/* ************************ */
	/**
	 * Serializes <i>(Java Object Serialization)</i> the given UWS in the specified file.
	 * 
	 * @param uws			The UWS to serialize.
	 * @param restoreFile	The file in which the given UWS will be serialized.
	 * @param debug			<i>true</i> to print a debugging message before and after the serialization, <i>false</i> otherwise.
	 * 
	 * @return	<i>true</i> if the UWS has been successfully serialized in the specified file, <i>false</i> otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static final boolean saveUWS(AbstractUWS uws, File restoreFile, boolean debug){
		if (uws == null)
			return false;
		
		if (debug) System.out.print("### [UWS INFO] : DESTRUCTION => WRITING RESTORATION FILE FOR "+uws.getName()+"....");
		
		if (restoreFile != null){
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(new FileOutputStream(restoreFile));
				out.writeObject(uws);
				out.close();
				if (debug) System.out.println("OK ! ###");
				return true;
			}catch(IOException ex) {
				if (debug){
					System.out.println("ERROR ("+ex.getMessage()+") ! ###");
					ex.printStackTrace();
				}
			}
		}else
			if (debug) System.out.println("ERROR (no restoration file) ! ###");
		
		return false;
	}
	
	/**
	 * De-serializes <i>(Java Object de-Serialization)</i> a UWS from the specified file.
	 * 
	 * @param restoreFile			The file which has contains the serialization of a UWS.
	 * @param debug					<i>true</i> to print a debugging message before and after the de-serialization, <i>false</i> otherwise.
	 * 
	 * @return						The de-serialized UWS or <i>null</i> if there is no corresponding file.
	 * 
	 * @throws ServletException		If it is impossible to restore a UWS from the specified file.
	 */
	@SuppressWarnings("unchecked")
	public static final AbstractUWS restoreUWS(File restoreFile, boolean debug) throws ServletException {
		AbstractUWS uws = null;
		
		if (debug) System.out.print("### [UWS INFO] : INIT => RESTORATION...");
		
		try{
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(restoreFile));
			uws = (AbstractUWS)input.readObject();
			input.close();
			if (debug) System.out.println("OK ["+uws.getName()+" ; "+restoreFile.getAbsolutePath()+"] ! ###");
		} catch(FileNotFoundException ex){
			if (debug) System.out.println("Nothing to restore (no restoration file found) ! ###");
		} catch (ClassNotFoundException e) {
			throw new ServletException("Impossible to restore the UWS from \""+restoreFile.getAbsolutePath()+"\" because no instance of AbstractUWS has been found !", e);
		} catch(ClassCastException e){
			throw new ServletException("Impossible to restore the UWS from \""+restoreFile.getAbsolutePath()+"\" because the read object can not be cast into AbstractUWS !", e);
		} catch (IOException ex) {
			throw new ServletException("Impossible to restore the UWS from \""+restoreFile.getAbsolutePath()+"\" !", ex);
		}
		
		return uws;
	}

/* **************************** */
/* DIRECTORY MANAGEMENT METHODS */
/* **************************** */
	/**
	 * Empties the specified directory.
	 * 
	 * @param directoryPath	The path of the directory to empty.
	 */
	public static final void clearDirectory(String directoryPath){
		clearDirectory(new File(directoryPath));
	}
	
	/**
	 * <p>Empties the specified directory.</p>
	 * 
	 * <p><i><u>Note:</u> The directory is NOT deleted. Just its content is destroyed.</i></p>
	 * 
	 * @param directory	The directory which has to be emptied.
	 */
	public static final void clearDirectory(File directory){
		if (directory != null && directory.exists() && directory.isDirectory() && directory.canWrite()){
			File[] files = directory.listFiles();
			for(int i=0; i<files.length; i++)
				files[i].delete();
		}
	}

/* ************************ */
/* ERROR MANAGEMENT METHODS */
/* ************************ */
	/**
	 * Sets an error summary to the given job with the given message and the given error type
	 * and sets the phase member to {@link ExecutionPhase#ERROR ERROR}.
	 * 
	 * @param j		The job to update.
	 * @param msg	The message of the error summary.
	 * @param type	The type of the error.
	 * 
	 * @return		<i>true</i> if the job has been successfully updated, <i>false</i> otherwise.
	 * 
	 * @throws UWSException	If there is an error when changing the phase or setting the error summary.
	 * 
	 * @see AbstractJob#error(ErrorSummary)
	 */
	public static final boolean publishErrorSummary(AbstractJob j, String msg, ErrorType type) throws UWSException {
		if (j == null)
			return false;
		
		j.error(new ErrorSummary(msg, type));
		return true;
	}
	
	/**
	 * <p>Sets an error summary corresponding to the given Exception with the given error type.</p>
	 * <ol>
	 * 	<li>The message of the generated error summary is set to the message of the given exception.</li>
	 * 	<li>The phase is set to {@link ExecutionPhase#ERROR ERROR}.</li>
	 * 	<li>The stack trace of the given exception is written in the file whose the name
	 * 		and the parent directory are given in parameters.</li>
	 * </ol>
	 * 
	 * <p><i><u>Note:</u> Even if the error file can not be written, the error summary is set to job...
	 * but it would have no URI/URL for the details message !</i></p>
	 * 
	 * @param j					The job to update.
	 * @param ex				The exception which must be used to generate the error summary.
	 * @param type				The type of the error.
	 * @param errorFileUri		The URI/URL at which the content of the error file can be displayed.
	 * @param errorsDirectory	The parent directory of the generated error file.
	 * @param errorFileName		The name of the file which must contains the stack trace of the given exception.
	 * 
	 * @return					<i>true</i> if the job has been successfully updated, <i>false</i> otherwise.
	 * 
	 * @throws IOException		If there is an error during the error file writing.
	 * @throws UWSException		If there is an error when changing the phase or setting the error summary.
	 * 
	 * @see AbstractJob#error(ErrorSummary)
	 * @see UWSToolBox#writeErrorFile(Exception, String, String)
	 */
	public static final boolean publishErrorSummary(AbstractJob j, Exception ex, ErrorType type, String errorFileUri, String errorsDirectory, String errorFileName) throws IOException, UWSException {
		if (j == null || ex == null)
			return false;

		// Write the errorFile:
		boolean written = writeErrorFile(ex, errorsDirectory, errorFileName);

		// Build the error summary:
		try{
			j.error(new ErrorSummary(ex, type, written?errorFileUri:null));
			return true;
		}catch(UWSException ue){
			File f = new File(errorFileName);
			if (f.exists())
				f.delete();
			throw ue;
		}
	}
	
	/**
	 * Writes the stack trace of the given exception in the file whose the name and the parent directory are given in parameters.
	 * If the specified file already exists, nothing will be done. To overwrite the file use the {@link UWSToolBox#writeErrorFile(Exception, String, String, boolean)} function.
	 * 
	 * @param ex				The exception which has to be used to generate the error file.
	 * @param errorsDirectory	The directory in which the error file must be created.
	 * @param errorFileName		The name of the file to create.
	 * 
	 * @return					<i>true</i> if the file has been successfully created, <i>false</i> otherwise.
	 * 
	 * @throws IOException		If there is an error during the file creation.
	 * 
	 * @see UWSToolBox#writeErrorFile(Exception, String, String, boolean)
	 */
	public static final boolean writeErrorFile(Exception ex, String errorsDirectory, String errorFileName) throws IOException {
		return writeErrorFile(ex, errorsDirectory, errorFileName, false);
	}
	
	/**
	 * Writes the stack trace of the given exception in the file whose the name and the parent directory are given in parameters.
	 * If the specified file already exists, it will be overwritten if the parameter <i>overwrite</i> is equal to <i>true</i>, otherwise
	 * no file will not be changed <i>(default behavior of {@link UWSToolBox#writeErrorFile(Exception, String, String)})</i>.
	 * 
	 * @param ex				The exception which has to be used to generate the error file.
	 * @param errorsDirectory	The directory in which the error file must be created.
	 * @param errorFileName		The name of the file to create.
	 * @param overwrite			<i>true</i> to overwrite the file if it already exists, <i>false</i> otherwise.
	 * 
	 * @return					<i>true</i> if the file has been successfully created, <i>false</i> otherwise.
	 * 
	 * @throws IOException		If there is an error during the file creation.
	 */
	public static final boolean writeErrorFile(Exception ex, String errorsDirectory, String errorFileName, boolean overwrite) throws IOException{
		if (ex == null)
			return false;
		
		File dir = new File(errorsDirectory);
		if (dir.exists()){
			if (dir.isDirectory()){
				File errorFile = new File(errorsDirectory, errorFileName);
				if ((!errorFile.exists()) || (errorFile.exists() && errorFile.isFile() && overwrite)){
					PrintWriter pw = new PrintWriter(errorFile);
					ex.printStackTrace(pw);
					pw.close();
					return true;
				}
			}
		}
		return false;
	}
	
/* *********************** */
/* UWS_URL DISPLAY METHODS */
/* *********************** */
	/**
	 * Displays all the fields of the given UWSUrl.
	 * 
	 * @param url	The UWSUrl which has to be displayed.
	 * 
	 * @see #printURL(UWSUrl, java.io.OutputStream)
	 */
	public static final void printURL(UWSUrl url){
		try {
			printURL(url, System.out);
		} catch (IOException e) { ; }
	}
	
	/**
	 * Displays all the fields of the given UWSUrl in the given output stream.
	 * 
	 * @param url			The UWSUrl which has to be displayed.
	 * @param output		The stream in which the fields of the given UWSUrl has to be displayed.
	 * 
	 * @throws IOException	If there is an error while writing in the given stream.
	 */
	public static final void printURL(UWSUrl url, java.io.OutputStream output) throws IOException {
		String toPrint = "***** UWS_URL ("+url.getBaseURI()+") *****\nRequest URL: "+url.getRequestURL()+"\nRequest URI: "+url.getRequestURI()
						+"\nUWS URI: "+url.getUwsURI()+"\nJob List: "+url.getJobListName()+"\nJob ID: "+url.getJobId()+"\nAttributes ("+url.getAttributes().length+"):";
		
		for(String att : url.getAttributes())
			toPrint += " "+att;
		
		toPrint += "\n";
		
		output.write(toPrint.getBytes());
		output.flush();
	}

/* ****************** */
/* DEPRECATED METHODS */
/* ****************** */
	/**
	 * Gets all UWS namespaces declarations needed for an XML representation of a UWS object.
	 * 
	 * @return	The UWS namespaces: <br /> (i.e. <i>= "xmlns:uws=[...] xmlns:xlink=[...] xmlns:xs=[...] xmlns:xsi=[...]"</i>).
	 * 
	 * @deprecated	Replaced by a non-static function in {@link uws.job.serializer.XMLSerializer}: {@link uws.job.serializer.XMLSerializer#getUWSNamespace()}.
	 * 				It is totally discouraged to use this function.
	 * @see 		uws.job.serializer.XMLSerializer#getUWSNamespace()
	 */
	@Deprecated
	public static final String getUWSNamespace(){ return (new uws.job.serializer.XMLSerializer()).getUWSNamespace(); }

	/**
	 * <p>Sets an error summary corresponding to the given Exception with the given error type.</p>
	 * <ol>
	 * 	<li>The message of the generated error summary is set to the message of the given exception.</li>
	 * 	<li>The phase is set to {@link ExecutionPhase#ERROR ERROR}.</li>
	 * 	<li>The stack trace of the given exception is written in the file whose the name
	 * 		and the parent directory are given in parameters.</li>
	 * </ol>
	 * 
	 * <p><i><u>Note:</u> Even if the error file can not be written, the error summary is set to job...
	 * but it would have no URL for the details message !</i></p>
	 * 
	 * @param j					The job to update.
	 * @param ex				The exception which must be used to generate the error summary.
	 * @param type				The type of the error.
	 * @param errorFileUrl		The URL at which the content of the error file can be displayed.
	 * @param errorsDirectory	The parent directory of the generated error file.
	 * @param errorFileName		The name of the file which must contains the stack trace of the given exception.
	 * 
	 * @return					<i>true</i> if the job has been successfully updated, <i>false</i> otherwise.
	 * 
	 * @throws IOException		If there is an error during the error file writing.
	 * @throws UWSException		If there is an error when changing the phase or setting the error summary.
	 * 
	 * @see AbstractJob#error(ErrorSummary)
	 * @see UWSToolBox#writeErrorFile(Exception, String, String)
	 * 
	 * @deprecated Replaced by {@link #publishErrorSummary(AbstractJob, Exception, ErrorType, String, String, String)}
	 */
	@Deprecated
	public static final boolean publishErrorSummary(AbstractJob j, Exception ex, ErrorType type, URL errorFileUrl, String errorsDirectory, String errorFileName) throws IOException, UWSException {
		return publishErrorSummary(j, ex, type, errorFileUrl.toString(), errorsDirectory, errorFileName);
	}
}
