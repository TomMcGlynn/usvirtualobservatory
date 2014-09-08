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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.meta.MetaStore;
import edu.caltech.vao.vospace.meta.MetaStoreFactory;
import edu.caltech.vao.vospace.storage.StorageManager;
import edu.caltech.vao.vospace.storage.StorageManagerFactory;
import edu.caltech.vao.vospace.xml.ContainerNode;
import edu.caltech.vao.vospace.xml.DataNode;
import edu.caltech.vao.vospace.xml.IdHelpers;
import edu.caltech.vao.vospace.xml.LinkNode;
import edu.caltech.vao.vospace.xml.Node;
import edu.caltech.vao.vospace.xml.NodeFactory;
import edu.caltech.vao.vospace.xml.StructuredDataNode;
import edu.caltech.vao.vospace.xml.UnstructuredDataNode;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.ConflictException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.oauth.UserHelper;

/**
 * Provides the REST service for /nodes/ path: the functions for manipulating the nodes content and metadata
 * @author Dmitry Mishin
 */
@Path("/nodes/")
public class NodesController {
	private static Logger logger = Logger.getLogger(NodesController.class);
	private @Context HttpServletRequest request;
	private static Configuration conf = SettingsServlet.getConfig();

	@GET
	@Produces(MediaType.TEXT_XML)
	public Response getNodeXml(@QueryParam("uri") String uri, @QueryParam("detail") String detail) {
		return getNodeXml("", uri, detail);
	}
	
	/**
	 * Method to return the node description in VOSpace XML format
	 * @return The node description in VOSpace XML format
	 */
	@GET @Path("{path:.+}")
	@Produces(MediaType.TEXT_XML)
	public Response getNodeXml(@PathParam("path") String fullPath, @QueryParam("uri") String uri, @QueryParam("detail") String detail) {
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();

		String identifier = IdHelpers.pathToId(fullPath);
		logger.debug("Get node: "+identifier);

		// Is identifier syntactically valid?
		if (!IdHelpers.validId(identifier)) throw new BadRequestException("The requested URI "+identifier+" is invalid."); 

		Node node = null;
		if(identifier.endsWith("!vospace") || identifier.endsWith("!vospace/")){
			node = NodeFactory.getInstance().getDefaultContainer();
			node.setUri(identifier);
		} else {
			node = store.getNode(identifier, (String)request.getAttribute("username"));
		}
		
		if(null != node) {
			detail = (detail == null) ? "max" : detail;
			if (!detail.equals("max")) {
				if (node instanceof DataNode) {
					DataNode datanode = (DataNode) node;
					datanode.removeAccepts();
					datanode.removeProvides();
					datanode.removeBusy();
				}
				node.removeCapabilities();
				if (detail.equals("min")) {
					node.removeProperties();
				}
			} else {
				if (node instanceof ContainerNode) {
					ContainerNode container = (ContainerNode) node;
					
					List<String> children = store.getNodeChildren(identifier, (String)request.getAttribute("username"));
					
					for(Iterator<String> it = children.iterator(); it.hasNext();) {
					    String child = it.next();
				    	container.addNode(child);
				    	
					}
				}
			}
		} else {
			throw new NotFoundException("Not Found");
		}
			
		return Response.ok(node.toString()).build();
	}


	/**
	 * Create new node
	 * @param fullPath the path to the new node
	 * @param headers HTTP headers of the request
	 * @param node XML node template
	 * @return The XML node description
	 */
	@PUT @Path("{path:.+}")
	public Response createNode(@PathParam("path") String fullPath, @Context HttpHeaders headers, byte[] nodeBytes) {
		Node node = NodeFactory.getInstance().getNode(nodeBytes);
		logger.debug("Creating node "+fullPath);
		Node resultNode = IdHelpers.create(node, (String)request.getAttribute("username"), false);
		logger.debug("Created node "+fullPath);
		return Response.ok(resultNode.toString()).status(201).build();
    }

	/**
	 * Delete a node
	 * @param fullPath The path of the node
	 * @param headers request headers
	 * @return HTTP result
	 */
	@DELETE @Path("{path:.+}")
    public Response deleteNode(@PathParam("path") String fullPath) {
		IdHelpers.delete(IdHelpers.pathToId(fullPath), (String)request.getAttribute("username"));
		return Response.ok().build();
    }

	/**
	 * setNode implementation
	 * @param fullPath the node path
	 * @param headers
	 * @param node Node representation to merge with
	 * @return The new node representation
	 */
	@POST @Path("{path:.+}")
    public Response setNode(@PathParam("path") String fullPath, @Context HttpHeaders headers, byte[] nodeBytes) {
		Node node = NodeFactory.getInstance().getNode(nodeBytes);
		return Response.ok(IdHelpers.create(node, (String)request.getAttribute("username"), true).toString()).build();
    }

    
}
