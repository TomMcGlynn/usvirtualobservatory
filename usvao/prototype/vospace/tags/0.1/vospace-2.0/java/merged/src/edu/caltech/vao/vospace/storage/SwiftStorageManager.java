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
package edu.caltech.vao.vospace.storage;

import com.rackspacecloud.client.cloudfiles.FilesAuthorizationException;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesContainer;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesInvalidNameException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.xml.IdHelpers;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.PathSeparator;
import edu.jhu.pha.vospace.api.PathSeparator.NodePath;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Backend storage manager for OpenStack Swift system
 * @author Matthew Graham
 * @author Dmitry Mishin 
 * (Uses code from edu.jhu.pha.vospace.swiftapi.SwiftClient)
 */
public class SwiftStorageManager implements StorageManager {

	private FilesClient cli = null;
	private static Configuration conf = SettingsServlet.getConfig();
	private static Logger logger = Logger.getLogger(SwiftStorageManager.class);

	/**
	 * Default constructor
	 */
	public SwiftStorageManager(String credentials) {
		authenticate(credentials);
	}

	/**
	 * Authenticate the client to the current backend storage
	 * @param endpoint The storage URL
	 * @param credentials The client's security credentials
	 */
	private void authenticate(String credentials) {
		String endPoint = conf.getString("storage.url");
		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode rootNode = m.readValue(credentials, JsonNode.class);
			String username = rootNode.path("username").getTextValue();
			String apikey = rootNode.path("apikey").getTextValue();
			
			cli = new FilesClient(username,apikey,endPoint);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e, "Error reading credentials from db.");
		}
	}

	@Override
	public String getNodeSize(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
        
        try {
        	return getClient().getObjectMetaData(npath.getContainerName(), npath.getNodePath()).getContentLength();
		} catch (FilesNotFoundException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} catch (FilesAuthorizationException ex) {
			ex.printStackTrace();
			throw new PermissionDeniedException(ex);
		} catch (FilesInvalidNameException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} catch (HttpException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		}
	}
	
	/**
	 * Create a container at the specified location in the current backend storage
	 * @param locationId The location of the container
	 */
	public void createContainer(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
		try {
			if(null != npath.getNodePath()) { // creating a node inside a first level container
				if(!getClient().containerExists(npath.getContainerName()))
					throw new NotFoundException("Container "+npath.getContainerName()+" not found.");
				
				if(!getClient().listObjects(npath.getContainerName(), npath.getNodePath()).isEmpty())
					throw new BadRequestException("Node "+npath.getNodePath()+" already exists.");

				logger.debug("Creating full path "+npath.getContainerName()+", "+npath.getNodePath());
				getClient().createFullPath(npath.getContainerName(), npath.getNodePath());
			} else { // creating first level container (bucket)
				if(!getClient().containerExists(npath.getContainerName())){
					logger.debug("Creating container "+npath.getContainerName());
					getClient().createContainer(npath.getContainerName());
				}
			}

		} catch (FilesException ex) {
			throw new InternalServerErrorException(ex);
		} catch (IOException ex) {
			throw new InternalServerErrorException(ex);
		} catch (HttpException ex) {
			throw new InternalServerErrorException(ex);
		}
	}

	@Override
	public void createNode(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
		try {
			if(null != npath.getNodePath()) { // creating a node inside a first level container
				if(!getClient().containerExists(npath.getContainerName()))
					throw new NotFoundException("Container "+npath.getContainerName()+" not found.");
				
				if(!getClient().listObjects(npath.getContainerName(), npath.getNodePath()).isEmpty())
					throw new BadRequestException("Node "+npath.getNodePath()+" already exists.");

				getClient().storeObject(npath.getContainerName(), new byte[]{}, "application/file", npath.getNodePath(), new Hashtable());
			} else { // creating first level container (bucket)
				throw new BadRequestException("Can not create a data node in the root of the storage.");
			}

		} catch (FilesException ex) {
			throw new InternalServerErrorException(ex);
		} catch (IOException ex) {
			throw new InternalServerErrorException(ex);
		} catch (HttpException ex) {
			throw new InternalServerErrorException(ex);
		}
	}

	public Hashtable<String, NodeType> getNodesList(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
		
		Hashtable<String, NodeType> nodes = new Hashtable<String, NodeType>();
		
		try {
        	if(null == npath.getContainerName() || npath.getContainerName().isEmpty()) {
				for(Iterator<FilesContainer> it = getClient().listContainers().iterator(); it.hasNext();){
		        	FilesContainer filesContainer = it.next();
		        	nodes.put(filesContainer.getName(), NodeType.CONTAINER_NODE);
				}
        	} else {
        		if(null == npath.getNodePath())
        			npath.setNodePath("");
        		ArrayList<FilesObject> list = (ArrayList<FilesObject>)getClient().listObjects(npath.getContainerName(), npath.getNodePath(),'/');

				for(Iterator<FilesObject> it = list.iterator(); it.hasNext();){
					FilesObject fileObj = it.next();
        			String nodeName = fileObj.getName().substring((null == npath.getNodePath() || npath.getNodePath().isEmpty())?0:npath.getNodePath().length()+1);

	        		if(fileObj.isDirectory()){
			        	nodes.put(nodeName, NodeType.CONTAINER_NODE);
	    			} else {
			        	nodes.put(nodeName, NodeType.DATA_NODE);
	    			}
				}        		
        	}
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		}
		return nodes;
	}
	/**
	 * Move the bytes from the specified old location to the specified new location 
	 * in the current backend storage
	 * @param oldLocationId The old location of the bytes
	 * @param newLocationId The new location of the bytes
	 */
	public void moveBytes(String oldLocationId, String newLocationId) {
		logger.debug("Move: "+oldLocationId+" "+newLocationId);
		NodePath npathOld = PathSeparator.splitPath(idToPath(oldLocationId), false);
		NodePath npathNew = PathSeparator.splitPath(idToPath(newLocationId), false);
		try {
			getClient().copyObject(npathOld.getContainerName(), npathOld.getNodePath(), npathNew.getContainerName(), npathNew.getNodePath());
			getClient().deleteObject(npathOld.getContainerName(), npathOld.getNodePath());
		} catch (FilesException e) {
			logger.error("Error from server: "+e.getHttpStatusCode()+" "+e.getHttpStatusMessage());
			throw new InternalServerErrorException(e.getHttpStatusCode()+" "+e.getHttpStatusMessage());
		} catch (IOException e) {
			throw new InternalServerErrorException(e.getMessage());
		} catch (HttpException e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Copy the bytes from the specified old location to the specified new location
	 * in the current backend storage
	 * @param oldLocationId The old location of the bytes
	 * @param newLocationId The new location of the bytes
	 */
	public void copyBytes(String oldLocationId, String newLocationId) {
		NodePath npathOld = PathSeparator.splitPath(idToPath(oldLocationId), false);
		NodePath npathNew = PathSeparator.splitPath(idToPath(newLocationId), false);
		try {
			getClient().copyObject(npathOld.getContainerName(), npathOld.getNodePath(), npathNew.getContainerName(), npathNew.getNodePath());
		} catch (FilesException e) {
			logger.error("Error from server: "+e.getHttpStatusCode()+" "+e.getHttpStatusMessage());
			throw new InternalServerErrorException(e.getHttpStatusCode()+" "+e.getHttpStatusMessage());
		} catch (IOException e) {
			throw new InternalServerErrorException(e.getMessage());
		} catch (HttpException e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Put the bytes from the specified input stream at the specified location in 
	 * the current backend storage
	 * @param location The location for the bytes
	 * @param stream The stream containing the bytes
	 */
	public void putBytes(String locationId, InputStream stream) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);

		try {
			getClient().storeStreamedObject(npath.getContainerName(), stream, "application/file", npath.getNodePath(), new Hashtable<String, String>());
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Get the bytes from the specified location in the current backend storage
	 * @param locationId The location of the bytes
	 * @return a stream containing the requested bytes
	 */
	public InputStream getBytes(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
		try {
			return getClient().getObjectAsStream(npath.getContainerName(), npath.getNodePath());
		} catch (FilesAuthorizationException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesInvalidNameException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesNotFoundException e) {
			throw new NotFoundException("Node Not Found");
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Remove the bytes at the specified location in the current backend storage
	 * @param locationId The location of the bytes
	 */
	public void removeBytes(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
		try {
			if (null == npath.getNodePath()) {
				List<FilesObject> contContent = getClient().listObjects(npath.getContainerName());
				for(FilesObject obj: contContent) {
					getClient().deleteObject(npath.getContainerName(), obj.getName());
				}
				getClient().deleteContainer(npath.getContainerName());
			} else {
				getClient().deleteObject(npath.getContainerName(), npath.getNodePath());
			}
		} catch (FilesNotFoundException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesException e) {
			throw new InternalServerErrorException(e);
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Retrieve when the bytes at the specified location in the current backend storage
	 * were last modified. A response of -1 indicates that the information is not
	 * available.
	 * @param locationId The location to check
	 * @return when the location was last modified
	 */
	public long lastModified(String locationId) {
		NodePath npath = PathSeparator.splitPath(idToPath(locationId), false);
		try {
			String lastModified = getClient().getObjectMetaData(npath.getContainerName(), npath.getNodePath()).getLastModified();
			/**@TODO convert! */
			return -1;
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * @return OpenStack connector
	 */
	private FilesClient getClient() {
		if (cli.isLoggedin()) {
			return cli;
		}
		if (null != cli.getUserName() && null != cli.getPassword()) {
			try {
				cli.login();
				return cli;
			} catch (HttpException e) {
				throw new InternalServerErrorException(e);
			} catch (IOException e) {
				throw new InternalServerErrorException(e);
			}
		} else {
			throw new InternalServerErrorException("You should be logged in. Please initialise with login and password first."+cli.getUserName() +" "+ cli.getPassword());
		}
	}
	
	private static String idToPath(String id) {
		String result = null;

		if(!IdHelpers.validId(id)){
			logger.error("The URI "+id+" is invalid.");
			throw new BadRequestException("The URI "+id+" is invalid.");
		}

		if(id.endsWith(".auto")) {
			id = id.substring(0, id.length() - ".auto".length()) + UUID.randomUUID().toString();
		}

		if(id.indexOf("!vospace")>0){
			result = id.substring(id.indexOf("!vospace")+"!vospace".length());
		}
		//logger.debug("Translated "+id+" to "+result);
		return result;
	}
}
