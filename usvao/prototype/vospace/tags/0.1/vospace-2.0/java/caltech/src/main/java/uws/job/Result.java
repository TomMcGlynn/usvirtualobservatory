package uws.job;

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

import uws.UWSException;

import uws.job.serializer.UWSSerializer;

/**
 * This class gives a short description (mainly an ID and a URL) of a job result.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 01/2011
 */
public class Result extends SerializableUWSObject {
	private static final long serialVersionUID = 1L;

	/** <b>[Required ; Default="result"]</b> Name or ID of this result. */
	protected String id = "result";

	/** <i>[Optional]</i> The readable URL which points toward the result file. */
	protected String href = null;
	
	/** <i>[Optional]</i> The type of result. */
	protected String type = null;
	
/* ************ */
/* CONSTRUCTORS */
/* ************ */
	/**
	 * Builds a result with its ID/name.
	 * 
	 * @param name	Name or ID of the result.
	 */
	public Result(String name){
		if (name != null)
			id = name;
	}
	
	/**
	 * Builds a result with the URL toward the file which contains the results.
	 * 
	 * @param resultUrl	Result file URL.
	 */
	public Result(java.net.URL resultUrl){
		href = resultUrl.toString();
	}
	
	/**
	 * Builds a result with an ID/name and the URL toward the file which contains the results.
	 * 
	 * @param name			Name or ID of the result.
	 * @param resultUrl		Result file URL.
	 * 
	 * @see Result#Result(String)
	 */
	public Result(String name, String resultUrl){
		this(name);
		href = resultUrl;
	}
	
	/**
	 * Builds a result with an ID/name, a result type and the URL toward the file which contains the results.
	 * 
	 * @param name			Name or ID or the result.
	 * @param resultType	Type of result.
	 * @param resultUrl		Result file URL.
	 * 
	 * @see Result#Result(String, String)
	 */
	public Result(String name, String resultType, String resultUrl){
		this(name, resultUrl);
		type = resultType;
	}	
	
/* ******* */
/* GETTERS */
/* ******* */
	/**
	 * Gets the id/name of this result.
	 * 
	 * @return	The result id or name.	
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Gets the URL of the result file.
	 * 
	 * @return	The result file URL.
	 */
	public final String getHref() {
		return href;
	}

	/**
	 * Gets the type of this result.
	 * 
	 * @return	The result type.
	 */
	public final String getType() {
		return type;
	}
	
/* ***************** */
/* INHERITED METHODS */
/* ***************** */
	@Override
	public String serialize(UWSSerializer serializer, String ownerId) throws UWSException {
		return serializer.getResult(this, true);
	}
	
	public String toString(){
		return "RESULT {id: "+id+"; type: \""+((type==null)?"?":type)+"\"; href: "+((href==null)?"none":href)+"}";
	}
	
/* ****************** */
/* DEPRECATED METHODS */
/* ****************** */
	/**
	 * Builds a result with an ID/name and the URL toward the file which contains the results.
	 * 
	 * @param name			Name or ID of the result.
	 * @param resultUrl		Result file URL.
	 * 
	 * @deprecated	Replaced by {@link #Result(String, String)}.
	 * @see 		#Result(String, String)
	 */
	@Deprecated
	public Result(String name, java.net.URL resultUrl){
		this(name, resultUrl.toString());
	}
	
	/**
	 * Builds a result with an ID/name, a result type and the URL toward the file which contains the results.
	 * 
	 * @param name			Name or ID or the result.
	 * @param resultType	Type of result.
	 * @param resultUrl		Result file URL.
	 * 
	 * @deprecated	Replaced by {@link #Result(String, String, String)}.
	 * @see 		#Result(String, String, String)
	 */
	@Deprecated
	public Result(String name, String resultType, java.net.URL resultUrl){
		this(name, resultType, resultUrl.toString());
	}
}
