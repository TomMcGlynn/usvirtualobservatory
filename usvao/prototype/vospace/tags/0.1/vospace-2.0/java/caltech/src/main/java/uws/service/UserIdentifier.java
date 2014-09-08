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

import java.io.Serializable;

import uws.service.UWSUrl;
import uws.service.actions.UWSAction;

import uws.UWSException;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>Lets defining how identifying a user thanks to a HTTP request.</p>
 * 
 * <p>
 *	This interface is mainly used by any subclass of {@link AbstractUWS} to extract the user ID of the author of a UWS action to apply.
 * 	It can be set by the function: {@link AbstractUWS#setUserIdentifier(UserIdentifier)}.
 * </p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 02/2011
 */
public interface UserIdentifier extends Serializable {
	
	/**
	 * <p>Extracts the ID of the user/owner of the current session.</p>
	 * 
	 * <p>This method is called just before choosing and applying a {@link UWSAction}.</p>
	 * 
	 * @param urlInterpreter	The interpreter of the request URL.
	 * @param request			The request.
	 * 
	 * @return					The ID of an owner/user
	 * 							or <i>null</i> to mean all users.
	 * 
	 * @throws UWSException		If any error occurs while extraction the user ID from the given parameters.
	 * 
	 * @see AbstractUWS#executeRequest(HttpServletRequest, HttpServletResponse)
	 */
	public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException;
	
}