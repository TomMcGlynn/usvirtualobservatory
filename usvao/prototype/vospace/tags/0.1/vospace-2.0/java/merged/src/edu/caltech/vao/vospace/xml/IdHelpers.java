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
package edu.caltech.vao.vospace.xml;


import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.meta.MetaStore;
import edu.caltech.vao.vospace.meta.MetaStoreFactory;
import edu.caltech.vao.vospace.storage.StorageManager;
import edu.caltech.vao.vospace.storage.StorageManagerFactory;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.ConflictException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.oauth.UserHelper;

public class IdHelpers {
	private static final Pattern VOS_PATTERN = Pattern.compile("vos://[\\w\\d][\\w\\d\\-_\\.!~\\*'\\(\\)\\+=]{2,}(![\\w\\d\\-_\\.!~\\*'\\(\\)\\+=]+(/[\\w\\d\\-_\\.!~\\*'\\(\\)\\+=]+)*)+");

	private static Configuration conf = SettingsServlet.getConfig();
	private static final Logger logger = Logger.getLogger(IdHelpers.class);
	
    private final static int PROPERTIES_SPACE_ACCEPTS = 1;
    private final static int PROPERTIES_SPACE_PROVIDES = 2;
    private final static int PROPERTIES_SPACE_CONTAINS = 4;
    
	/**
	 * Check whether the specified identifier is valid
	 * @param id The identifier to check
	 * @return whether the identifier is valid or not
	 */
	public static boolean validId(String id) {
		Matcher m = VOS_PATTERN.matcher(id);
		return m.matches();
	}

	/**
	 * Check whether the specified identifier is valid
	 * @param id The identifier to check
	 * @param path The path to match the identifier with
	 * @return whether the identifier is valid or not
	 */
	public static boolean validId(String id, String path) {
		return validId(id) && (id.equals(pathToId(path)));
	}

	public static String pathToId(String path) {
		return conf.getString("vospace.uri")+"!vospace"+(path.isEmpty() || path.startsWith("/")?"":"/")+path;
	}
	
	/**
	 * Convert IVO identifier into VOSpace identifier
	 * @param ivoid The IVO identifier to convert
	 * @return The converted VOSpace identifier
	 */
	public static String getId(String ivoid) {
		return ivoid.replace("/", "!").replace("ivo:!!", "vos://");
	}

	/**
	 * Check whether the parent node of the specified identifier is valid:
	 *   - it exists
	 *   - it is a container
	 * @param id The identifier to check
	 * @return whether the parent node is valid or not
	 */
	public static boolean validParent(String id, String username) {
		MetaStore store = MetaStoreFactory.getInstance().getMetaStore();
		String parent = id.substring(0, id.lastIndexOf("/"));
		
		if(parent.endsWith("!vospace")) 
			return true;
		
		return store.getType(parent, username).equals(NodeType.CONTAINER_NODE);
	}
	
    /** 
     * Create the specified node
     * @param node The node to be created
     * @return The created node
     */
    public static Node create(Node node, String username, boolean overwrite) {
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();
		String uri = node.getUri();
		// Is identifier syntactically valid?
		if (!IdHelpers.validId(uri)) throw new BadRequestException("The requested URI is invalid."); 
		// Is the parent a valid container?
		if (!IdHelpers.validParent(uri, username)) throw new BadRequestException("The requested URI is invalid - bad parent."); 
		// Does node already exist?
		boolean exists = store.isStored(uri, username);
		logger.debug("The node "+uri+" exists? "+exists);
		if (exists && !overwrite) throw new ConflictException("A Node already exists with the requested URI."); 
		NodeType type = NodeType.NODE;
		// Is a service-generated name required?
		if (uri.endsWith(".auto")) {
			node.setUri(uri.substring(0, uri.length() - ".auto".length()) + UUID.randomUUID().toString());
			uri = node.getUri();
		}
		// Clear any <accepts>, <provides> and <capabilities> that the user might specify
		if (node instanceof DataNode) {
			type = NodeType.DATA_NODE;
			DataNode datanode = (DataNode) node;
			datanode.removeAccepts();
			datanode.removeProvides();
			datanode.removeCapabilities();
			// Set <accepts> for UnstructuredDataNode
			if (node instanceof UnstructuredDataNode) {
				type = NodeType.UNSTRUCTURED_DATA_NODE;
				datanode.addAccepts(conf.getString("core.view.any"));
			}
			// Set <accepts> for StructuredDataNode
			if (node instanceof StructuredDataNode) {
				type = NodeType.STRUCTURED_DATA_NODE;
				
				for (String view: (List<String>)conf.getList("space.accepts.image")) {
					datanode.addAccepts(view);
				}
				for (String view: (List<String>)conf.getList("space.accepts.table")) {
					datanode.addAccepts(view);
				}
				for (String view: (List<String>)conf.getList("space.provides.image")) {
					datanode.addProvides(view);
				}
				for (String view: (List<String>)conf.getList("space.provides.table")) {
					datanode.addProvides(view);
				}
			}
			// Set <accepts> for ContainerNode
			if (node instanceof ContainerNode) {
				type = NodeType.CONTAINER_NODE;
				for (String view: (List<String>)conf.getList("space.accepts.archive")) {
					datanode.addAccepts(view);
				}
				for (String view: (List<String>)conf.getList("space.provides.archive")) {
					datanode.addProvides(view);
				}
			}
			// Set capabilities
			for (String cap: (List<String>)conf.getList("space.capabilities")) {
				datanode.addCapabilities(cap);
			}
		}
		// Link node
		if (node instanceof LinkNode) type = NodeType.LINK_NODE;
		// Check properties
		if (node.hasProperties()) {
			for (String propUri: node.getProperties().keySet()) {
				if (!store.isKnownProperty(propUri))
				    store.registerProperty(propUri, PROPERTIES_SPACE_CONTAINS, false);
			}
		}
		// Store node
		if (exists) {
			store.updateData(uri, username, node.toString());
		} else {
			store.storeData(uri, type, username, node.toString());
		}
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(username));

		if (type.equals(NodeType.CONTAINER_NODE)) {
			backend.createContainer(node.getUri());
		} else {
			backend.createNode(node.getUri());
		}

		return node;

    }

	public static void delete(String identifier, String username) {
		MetaStore store = MetaStoreFactory.getInstance(conf).getMetaStore();
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(username));

		if (!IdHelpers.validId(identifier)) 
			throw new BadRequestException("The requested URI "+identifier+" is invalid."); 

	    if (!store.isStored(identifier, username)) 
	    	throw new NotFoundException("The specified node does not exist."); 

	    store.removeData(identifier, username);
	    backend.removeBytes(identifier);
	}
}
