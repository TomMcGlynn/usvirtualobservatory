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
package edu.jhu.pha.vospace.rest;

import java.util.List;
import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import com.generationjava.io.xml.SimpleXmlWriter;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

/**
 * Provides the REST service for / path: the functions for manipulating the observatory capabilities and metadata
 * @author Dmitry Mishin
 */
@Path("/")
public class MetadataController {
	private static Logger logger = Logger.getLogger(MetadataController.class);
	private static Configuration conf = SettingsServlet.getConfig();;
	
	/**
	 * Returns the observatory supported protocols
	 * @return The supported protocols in VOSpace xml format
	 */
	@GET @Path("/protocols")
	@Produces(MediaType.APPLICATION_XML)
	public String getProtocols() {
		StringWriter writ = new StringWriter();
		SimpleXmlWriter xw = new SimpleXmlWriter(writ);
		try {
			xw.writeEntity("protocols");
			
			xw.writeEntity("accepts");
			for(String protocol: (List<String>)conf.getList("transfers.protocols.accepts")){
				xw.writeEntity("protocol").writeAttribute("uri", protocol).endEntity();
			}
			xw.endEntity();
			
			xw.writeEntity("provides");
			for(String protocol: (List<String>)conf.getList("transfers.protocols.provides")){
				xw.writeEntity("protocol").writeAttribute("uri", protocol).endEntity();
			}
			xw.endEntity();
			
			xw.endEntity();
			xw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}
		return writ.toString();
	}

	/**
	 * Returns the observatory supported views
	 * @return The supported views in VOSpace xml format
	 */
	@GET @Path("/views")
	@Produces(MediaType.APPLICATION_XML)
	public String getViews() {
		StringWriter writ = new StringWriter();
		SimpleXmlWriter xw = new SimpleXmlWriter(writ);
		try {
			//org.iso_relax.verifier.Verifier verifier = org.iso_relax.verifier.VerifierFactory.newInstance("http://www.w3.org/2001/XMLSchema").newVerifier(new URL("http://dimm.wdcb.ru/pub/tmp/scheme.xsd").openStream());
			//JarvWriter xw = new JarvWriter(new SimpleXmlWriter(writ), verifier.getVerifierHandler());

			xw.writeEntity("views");
			
			xw.writeEntity("accepts");
			for(String view: (List<String>)conf.getList("core.views.accepts")){
				xw.writeEntity("view").writeAttribute("uri", view).endEntity();
			}
			xw.endEntity();
			
			xw.writeEntity("provides");
			for(String view: (List<String>)conf.getList("core.views.provides")){
				xw.writeEntity("view").writeAttribute("uri", view).endEntity();
			}
			xw.endEntity();
			
			xw.endEntity();
			xw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}
		return writ.toString();
	}
	
	/**
	 * Returns the observatory supported properties
	 * @return The supported properties in VOSpace xml format
	 */
	@GET @Path("/properties")
	@Produces(MediaType.APPLICATION_XML)
	public String getProperties() {
		StringWriter writ = new StringWriter();
		SimpleXmlWriter xw = new SimpleXmlWriter(writ);
		try {
			xw.writeEntity("properties");
			
			xw.writeEntity("accepts");
			for(String prop: (List<String>)conf.getList("core.properties.accepts")){
				xw.writeEntity("property").writeAttribute("uri", prop).endEntity();
			}
			xw.endEntity();
			
			xw.writeEntity("provides");
			for(String prop: (List<String>)conf.getList("core.properties.provides")){
				xw.writeEntity("property").writeAttribute("uri", prop).endEntity();
			}
			xw.endEntity();

			xw.writeEntity("contains");
			for(String prop: (List<String>)conf.getList("core.properties.contains")){
				xw.writeEntity("property").writeAttribute("uri", prop).endEntity();
			}
			xw.endEntity();

			xw.endEntity();
			xw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e);
		}
		return writ.toString();
	}
}
