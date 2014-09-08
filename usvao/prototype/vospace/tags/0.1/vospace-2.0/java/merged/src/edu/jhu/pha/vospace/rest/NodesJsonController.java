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
@Path("/nodesjson/")
public class NodesJsonController {
	private static Logger logger = Logger.getLogger(NodesJsonController.class);
	private @Context HttpServletRequest request;
	private static Configuration conf = SettingsServlet.getConfig();;


	/**
	 * Method to get the tree of nodes in JSON format
	 * @return The nodes list in JSON format
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getNodesListJson(@QueryParam("path") String path) {
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials((String)request.getAttribute("username")));

		if(null == path)
			path = "";
		
		Hashtable<String, NodeType> nodes = backend.getNodesList(IdHelpers.pathToId(path));
		String[] nodeKeys = nodes.keySet().toArray(new String[]{});
		Arrays.sort(nodeKeys);
		
		while(path.startsWith("/"))
			path = path.substring(1);
		
		StringWriter writer = new StringWriter();
		try {
        	JsonFactory f = new JsonFactory();
        	JsonGenerator g = f.createJsonGenerator(writer);
        	 
        	g.writeStartObject();
        	g.writeStringField("label", "name");
        	g.writeStringField("identifier", "id");

        	g.writeArrayFieldStart("items");
        	
			for(String nodeKey: nodeKeys){
        		if(nodes.get(nodeKey).equals(NodeType.CONTAINER_NODE)){
    	        	g.writeStartObject();
    	        	g.writeStringField("type", nodes.get(nodeKey).toString());
    	        	g.writeStringField("id", path+"/"+nodeKey);
    	        	g.writeStringField("name", nodeKey);
    	        	g.writeArrayFieldStart("children");

    	        	g.writeEndArray();
    				g.writeEndObject();
    			} else {
    	        	g.writeStartObject();
    	        	g.writeStringField("type", nodes.get(nodeKey).toString());
    	        	g.writeStringField("id", path+"/"+nodeKey);
    	        	g.writeStringField("name", nodeKey);
    				g.writeEndObject();
    			}
			}        		

        	g.writeEndArray();
        	
        	g.writeEndObject();

        	g.close(); // important: will force flushing of output, close underlying output stream
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		}
		return writer.getBuffer().toString();
	}
    
}
