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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import uws.UWSException;
import uws.job.serializer.UWSSerializer;
import uws.job.serializer.XMLSerializer;

/**
 * <P>This class defines the methods that an object of the UWS pattern must implement to be written in any format (by default, XML ; see {@link XMLSerializer}).</P>
 * 
 * <P>The {@link SerializableUWSObject#serialize(UWSSerializer, String)} method must be implemented. It is the most important method of this class
 * because it returns a serialized representation of this UWS object.</P>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public abstract class SerializableUWSObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Serializes the whole object thanks to the given serializer.
	 * 
	 * @param serializer	The serializer to use.
	 * 
	 * @return				The serialized representation of this object.
	 * 
	 * @throws UWSException	If there is an error during the serialization.
	 * 
	 * @see #serialize(UWSSerializer, String)
	 */
	public String serialize(UWSSerializer serializer) throws UWSException {
		return serialize(serializer, null);
	}
	
	/**
	 * Serializes the whole object considering the given owner ID (supposed to be the ID of the current user)
	 * and thanks to the given serializer.
	 * 
	 * @param serializer		The serializer to use.
	 * @param ownerId			The ID of the current user.
	 * 
	 * @return					The serialized representation of this object.
	 * 
	 * @throws UWSException		If there is an error during the serialization.
	 */
	public abstract String serialize(UWSSerializer serializer, String ownerId) throws UWSException;
	
	/**
	 * Serializes the whole object in the given output stream and thanks to the given serializer.
	 * 
	 * @param output		The output stream in which this object must be serialized.
	 * @param serializer	The serializer to use.
	 * 
	 * @throws UWSException	If there is an error during the serialization.
	 * 
	 * @see #serialize(OutputStream, UWSSerializer, String)
	 */
	public void serialize(OutputStream output, UWSSerializer serializer) throws UWSException {
		serialize(output, serializer, null);
	}
	
	/**
	 * Serializes the while object in the given output stream,
	 * considering the given owner ID and thanks to the given serializer.
	 * 
	 * @param output		The ouput stream in which this object must be serialized.
	 * @param serializer	The serializer to use.
	 * @param ownerId		The ID of the current ID.
	 * 
	 * @throws UWSException	If the given ouput stream is <i>null</i>,
	 * 						or if there is an error during the serialization,
	 * 						or if there is an error while writing in the given stream.
	 * 
	 * @see #serialize(UWSSerializer, String)
	 */
	public void serialize(OutputStream output, UWSSerializer serializer, String ownerId) throws UWSException {
		if (output == null)
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Serialize] Impossible to serialize {"+toString()+"} because the given stream is null !");
		
		try{
			String serialization = serialize(serializer, ownerId);
			if (serialization != null) {
			    output.write(serialization.getBytes());
			    output.flush();
			} else
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, "[Serialize] Impossible to write the serialization of {"+toString()+"} because it is NULL !");
		}catch(IOException ex){
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex, "[Serialize] Impossible to serialize {"+toString()+"} !");
		}	
	}
}
