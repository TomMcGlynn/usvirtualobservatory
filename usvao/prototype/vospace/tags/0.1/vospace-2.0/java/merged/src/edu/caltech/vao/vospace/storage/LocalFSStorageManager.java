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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.xml.IdHelpers;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

/**
 * Backend storage manager for local filesystem
 */
public class LocalFSStorageManager implements StorageManager {

	private static Logger logger = Logger.getLogger(LocalFSStorageManager.class);
	private static Configuration conf = SettingsServlet.getConfig();
	private String rootLocationStr;
	private String username;
	
	/**
	 * Default constructor
	 */
	public LocalFSStorageManager(String credentials) {
		authenticate(credentials);
	}

	/**
	 * Authenticate the client to the current backend storage
	 * @param credentials The client's security credentials
	 */
	private void authenticate(String credentials) {
		this.rootLocationStr = conf.getString("storage.local.root");
		if(null == this.rootLocationStr || this.rootLocationStr.isEmpty())
			throw new RuntimeException("Configuration for local file store is not found.");

		File rootLocation = new File(this.rootLocationStr);
		
		if(!rootLocation.exists() && !rootLocation.mkdirs())
			throw new RuntimeException("Could not create the necessary file location.");

		try {
			ObjectMapper m = new ObjectMapper();
			JsonNode rootNode = m.readValue(credentials, JsonNode.class);
			this.username = rootNode.path("username").getTextValue();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Error reading credentials from db.");
		}
	}

	
	/**
	 * Create a container at the specified location in the current backend storage
	 * @param locationId The location of the container
	 */
	@Override
	public void createContainer(String locationId) {
		boolean success;
		try {
			success = idToFile(locationId).mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
		if (!success) throw new InternalServerErrorException("Container cannot be created");
	}

	/**
	 * Move the bytes from the specified old location to the specified new location 
	 * in the current backend storage
	 * @param oldLocationId The old location of the bytes
	 * @param newLocationId The new location of the bytes
	 */
	@Override
	public void moveBytes(String oldLocationId, String newLocationId) {
		logger.debug("Moving bytes from "+oldLocationId+" to "+newLocationId);
		try {
			File oldFile = idToFile(oldLocationId);
			if (oldFile.isFile()) {
				FileUtils.moveFile(oldFile, idToFile(newLocationId));
			} else {
				FileUtils.moveDirectory(oldFile, idToFile(newLocationId));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Copy the bytes from the specified old location to the specified new location
	 * in the current backend storage
	 * @param oldLocationId The old location of the bytes
	 * @param newLocationId The new location of the bytes
	 */
	@Override
	public void copyBytes(String oldLocationId, String newLocationId) {
		logger.debug("Copy: "+oldLocationId+" "+newLocationId);
		try {
			File oldFile = idToFile(oldLocationId);
			if (oldFile.isFile()) {
				FileUtils.copyFile(oldFile, idToFile(newLocationId));
			} else {
				FileUtils.copyDirectory(oldFile, idToFile(newLocationId));
			}
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Put the bytes from the specified input stream at the specified location in 
	 * the current backend storage
	 * @param locationId The location for the bytes
	 * @param stream The stream containing the bytes
	 */
	@Override
	public void putBytes(String locationId, InputStream stream) {
		logger.debug("Puting data to "+locationId);
		try {
			BufferedInputStream bis = new BufferedInputStream(stream);
			FileOutputStream fos = new FileOutputStream(idToFile(locationId)); 
			byte[] buffer = new byte[8192];
			int count = bis.read(buffer);
			while (count != -1 && count <= 8192) {
				fos.write(buffer, 0, count);
				count = bis.read(buffer);
			}
			if (count != -1) {
				fos.write(buffer, 0, count);
			}
			fos.close();
			bis.close();
		} catch (Exception e) {
			throw new InternalServerErrorException(e.toString());
		}
	}

	/**
	 * Get the bytes from the specified location in the current backend storage
	 * @param locationId The location of the bytes
	 * @return a stream containing the requested bytes
	 */
	@Override
	public InputStream getBytes(String locationId) {
		try {
			InputStream in = new FileInputStream(idToFile(locationId));
			return in;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Remove the bytes at the specified location in the current backend storage
	 * @param locationId The location of the bytes
	 */
	@Override
	public void removeBytes(String locationId) {
		try {
			idToFile(locationId).delete();
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	/**
	 * Retrieve when the bytes at the specified location in the current backend storage
	 * were last modified. A response of -1 indicates that the information is not
	 * available.
	 * @param locationId The location to check
	 * @return when the location was last modified
	 */
	@Override
	public long lastModified(String locationId) {
		try {
			long lastModified = idToFile(locationId).lastModified();
			return lastModified;
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	@Override
	public void createNode(String locationId) {
		try {
			idToFile(locationId).createNewFile();
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}

	@Override
	public Hashtable<String, NodeType> getNodesList(String locationId) {
		File listDir = idToFile(locationId);
		Hashtable<String, NodeType> nodes = new Hashtable<String, NodeType>();
		
		File[] dirFiles = listDir.listFiles();
		
		if(null != dirFiles && dirFiles.length > 0){
			for(File nextFile: dirFiles){
				if(nextFile.isDirectory())
					nodes.put(nextFile.getName(), NodeType.CONTAINER_NODE);
				else
					nodes.put(nextFile.getName(), NodeType.DATA_NODE);
			}
		}
		
		return nodes;
	}

	@Override
	public String getNodeSize(String locationId) {
		try {
			return idToFile(locationId).length()+"";
		} catch (Exception e) {
			throw new InternalServerErrorException(e.getMessage());
		}
	}
	
	public static String idToPath(String id) {
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
		logger.debug("Translated "+id+" to "+result);
		return result;
	}
	
	private File idToFile(String id) {
		return new File(rootLocationStr+(rootLocationStr.endsWith("/")?"":"/")+username+"/"+idToPath(id));
	}
}
