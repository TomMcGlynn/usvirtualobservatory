/*******************************************************************************
 * Copyright (c) 2012, Johns Hopkins University
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
package edu.jhu.pha.vosync.exception;

import java.io.ByteArrayOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public abstract class DropboxException extends WebApplicationException {
	private static final long serialVersionUID = -8345838864718847104L;
	static final JsonFactory fact = new JsonFactory();
	private static final Logger logger = Logger.getLogger(DropboxException.class);

	public DropboxException(Response response) {
		super(response);
	}
	
	public DropboxException(Throwable cause) {
		super(cause);
	}
	
	public DropboxException(Throwable cause, Response response) {
		super(cause, response);
	}
	
	static byte[] constructBody(String message) {
    	ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    	try {
			JsonGenerator g = fact.createJsonGenerator(byteOut).useDefaultPrettyPrinter();
			
			g.writeStartObject();
			g.writeStringField("error", message);
			g.writeEndObject();
			
			g.close();
			byteOut.close();
    	} catch (Exception ex) {
    		logger.error("Error constructing DropboxException: "+ex.getMessage());
    	}
		return byteOut.toByteArray();
	}


}
