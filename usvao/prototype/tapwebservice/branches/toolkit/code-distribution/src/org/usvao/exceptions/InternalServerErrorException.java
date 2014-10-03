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
package org.usvao.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Exception for arising the InternalServerError HTTP error response
 *@author deoyani nandrekar-heinis
 */
public class InternalServerErrorException extends WebApplicationException {
	private static final long serialVersionUID = -8895468353386243446L;
	public InternalServerErrorException(String message) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).type(MediaType.TEXT_XML).build());                
	}
	public InternalServerErrorException(Throwable cause) {
		super(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(cause.getMessage()).type(MediaType.TEXT_XML).build());
	}
	public InternalServerErrorException(Throwable cause, String message) {
		super(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).type(MediaType.TEXT_XML).build());
	}
//        public InternalServerErrorException(String message) {
//		super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).type("text/plain").build());
//	}
//	public InternalServerErrorException(Throwable cause) {
//		super(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(cause.getMessage()).type("text/plain").build());
//	}
//	public InternalServerErrorException(Throwable cause, String message) {
//		super(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(message).type("text/plain").build());
//	}
}
