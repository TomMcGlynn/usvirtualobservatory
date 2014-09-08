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
package edu.jhu.pha.vospace.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;

import com.rackspacecloud.client.cloudfiles.FilesAccountInfo;
import com.rackspacecloud.client.cloudfiles.FilesAuthorizationException;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesContainerInfo;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesInvalidNameException;
import com.rackspacecloud.client.cloudfiles.FilesNotFoundException;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;

import edu.jhu.pha.vospace.DbPoolServlet;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.DbPoolServlet.SqlWorker;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;
import edu.jhu.pha.vospace.node.NodeInfo;
import edu.jhu.pha.vospace.node.NodePath;
import edu.jhu.pha.vospace.oauth.UserHelper;

/**
 * Backend storage manager for OpenStack Swift system
 * @author Matthew Graham
 * @author Dmitry Mishin 
 * (Uses code from edu.jhu.pha.vospace.swiftapi.SwiftClient)
 */
public class SwiftStorageManager implements StorageManager {

	private FilesClient cli;
	private static final Configuration conf = SettingsServlet.getConfig();
	private static final Logger logger = Logger.getLogger(SwiftStorageManager.class);
	
	private SwiftJsonCredentials credentials;
	
	private final String username;
	
	private final int CONNECT_TIMEOUT = 25000;
	
	private static final String VO_URI = conf.getString("vospace.uri");
	
	private static final SimpleDateFormat swiftDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	
	//HTTP client
	private static PoolingClientConnectionManager cm = null;
	private static HttpClient httpClient = null;
	
	public static String generateRandomCredentials(final String username) {
        return DbPoolServlet.goSql("Generate random credentials",
        		"select username, apikey from storage_users_pool where user_id IS NULL limit 1;",
                new SqlWorker<String>() {
                    @Override
                    public String go(Connection conn, PreparedStatement stmt) throws SQLException {
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
            				String user = rs.getString("username");
            				String password = rs.getString("apikey");
            				PreparedStatement prep = conn.prepareStatement("update storage_users_pool SET user_id = (select user_id from user_identities where identity = ?) where username = ?");
            				prep.setString(1, username);
            				prep.setString(2, user);
            				prep.execute();

            				logger.debug(username+" "+user);
            				
            				StringWriter writer = new StringWriter();
            		    	JsonFactory f = new JsonFactory();
            		    	try {
            		        	JsonGenerator g = f.createJsonGenerator(writer);
            		        	 
            		        	g.writeStartObject();
            		        	g.writeStringField("username", user);
            		        	g.writeStringField("apikey", password);
            		        	g.writeEndObject();

            		        	g.close(); // important: will force flushing of output, close underlying output stream

            		        	return writer.getBuffer().toString();
            		    	} catch(JsonGenerationException ex) {
            					throw new InternalServerErrorException("Error generating user storage credentials. "+ex.getMessage());
            		    	} catch (IOException e) {
            					throw new InternalServerErrorException("Error generating user storage credentials. "+e.getMessage());
            				}
                        
                        } else
            				throw new PermissionDeniedException("The user does not exist.");
                    }
                }
        );
		
	}

	public static HttpClient getHttpClient() {
		if(null == cm) {
		
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(
			         new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(
			         new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
	
			cm = new PoolingClientConnectionManager(schemeRegistry);
			// Increase max total connection to 200
			cm.setMaxTotal(200);
			// Increase default max connection per route to 20
			cm.setDefaultMaxPerRoute(50);
		}
		 
		if(null == httpClient)
			httpClient = new DefaultHttpClient(cm);
		
		return httpClient;
	}
	
	/**
	 * Default constructor
	 */
	public SwiftStorageManager(String username) {
		this.username = username;
		this.credentials = UserHelper.getDataStoreCredentials(username);

		try {
			cli = new FilesClient(getHttpClient(), credentials.getUsername(),credentials.getApikey(),conf.getString("storage.url"), null, CONNECT_TIMEOUT);
			
			if(null != credentials.getStorageurl() && null != credentials.getAuthtoken()){
				cli.login(credentials.getAuthtoken(), credentials.getStorageurl(), null);
			} else {
				cli.login();
				credentials.setStorageurl(cli.getStorageURL());
				credentials.setAuthtoken(cli.getAuthToken());
				logger.debug("Added token to DB");
				UserHelper.setDataStoreCredentials(username, credentials.toString());
			}
				
		} catch (JsonParseException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException(e, "Error reading credentials from db.");
		} catch (HttpException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		}

	
	}

	/**
	 * Copy the bytes from the specified old location to the specified new location
	 * in the current backend storage
	 * @param oldLocationId The old location of the bytes
	 * @param newLocationId The new location of the bytes
	 */
	@Override
	public void copyBytes(NodePath oldNodePath, NodePath newNodePath) {
		try {
			getClient().copyObject(oldNodePath.getContainerName(), oldNodePath.getNodeRelativeStoragePath(), newNodePath.getContainerName(), newNodePath.getNodeRelativeStoragePath());
		} catch (FilesInvalidNameException e) {
			throw new NotFoundException("Node Not Found");
		} catch (HttpException e) {
			throw new BadRequestException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e.getMessage());
		}
		updateCredentials();
	}
	
	/**
	 * Create a container at the specified location in the current backend storage
	 * @param locationId The location of the container
	 */
	@Override
	public void createContainer(NodePath npath) {
		try {
			if(!npath.getNodeRelativeStoragePath().isEmpty()) { // creating a node inside a first level container
				/*if(!getClient().containerExists(npath.getContainerName()))
					throw new NotFoundException("Container "+npath.getContainerName()+" not found.");
				
				if(!getClient().listObjects(npath.getContainerName(), npath.getNodeRelativeStoragePath()).isEmpty())
					throw new BadRequestException("Node "+npath.getNodeRelativeStoragePath()+" already exists.");

				logger.debug("Creating full path "+npath.getContainerName()+", "+npath.getNodeRelativeStoragePath());
				getClient().createFullPath(npath.getContainerName(), npath.getNodeRelativeStoragePath());*/
			} else { // creating first level container (bucket)
				if(!npath.getContainerName().isEmpty() /*is empty when creating the root node for new user */ && 
						!getClient().containerExists(npath.getContainerName())){
					logger.debug("Creating container "+npath.getContainerName());
					getClient().createContainer(npath.getContainerName());
				}
			}
    		updateCredentials();

		} catch (FilesException ex) {
			throw new InternalServerErrorException(ex);
		} catch (IOException ex) {
			throw new InternalServerErrorException(ex);
		} catch (HttpException ex) {
			throw new InternalServerErrorException(ex);
		}
	}

	/**
	 * Get the bytes from the specified location in the current backend storage
	 * @param locationId The location of the bytes
	 * @return a stream containing the requested bytes
	 */
	@Override
	public InputStream getBytes(NodePath npath) {
		try {
			InputStream inp = getClient().getObjectAsStream(npath.getContainerName(), npath.getNodeRelativeStoragePath());
			updateCredentials();
			return inp;
		} catch (FilesAuthorizationException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesInvalidNameException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesNotFoundException e) {
			/* Return empty stream */
			return new ByteArrayInputStream(new byte[]{});
			//throw new NotFoundException("Node Not Found");
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
	}

	@Override
	public long getBytesUsed() {
		try {
			long bytesUsed = getClient().getAccountInfo().getBytesUsed();
			updateCredentials();
			return bytesUsed;
		} catch (FilesAuthorizationException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * @return OpenStack connector
	 */
	private FilesClient getClient() {
		return cli;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.jhu.pha.vospace.storage.StorageManager#getStorageUrl()
	 */
	@Override
	public String getStorageUrl() {
			return getClient().getStorageURL();
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.jhu.pha.vospace.storage.StorageManager#getNodeSyncAddress(java.lang.String)
	 */
	@Override
	public String getNodeSyncAddress(String container) {
		try {
			return getClient().getContainerInfo(container).getSyncTo();
		} catch (FilesAuthorizationException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Move the bytes from the specified old location to the specified new location 
	 * in the current backend storage
	 * @param oldLocationId The old location of the bytes
	 * @param newLocationId The new location of the bytes
	 */
	@Override
	public void moveBytes(NodePath oldNodePath, NodePath newNodePath) {

		try {
			getClient().copyObject(oldNodePath.getContainerName(), oldNodePath.getNodeRelativeStoragePath(), newNodePath.getContainerName(), newNodePath.getNodeRelativeStoragePath());
			getClient().deleteObject(oldNodePath.getContainerName(), oldNodePath.getNodeRelativeStoragePath());
		} catch (FilesInvalidNameException e) {
			throw new NotFoundException("Node Not Found");
		} catch (HttpException e) {
			throw new BadRequestException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e.getMessage());
		}
		updateCredentials();
	}

	/**
	 * Put the bytes from the specified input stream at the specified location in 
	 * the current backend storage
	 * @param location The location for the bytes
	 * @param stream The stream containing the bytes
	 * @param size The stream size
	 */
	@Override
	public void putBytes(NodePath npath, InputStream stream) {
		try {
			getClient().storeStreamedObject(npath.getContainerName(), stream, "application/file", npath.getNodeRelativeStoragePath(), new Hashtable<String, String>());
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
		updateCredentials();
	}

	/**
	 * Remove the bytes at the specified location in the current backend storage
	 * @param locationId The location of the bytes
	 */
	@Override
	public void remove(NodePath npath) {
		try {
			if (npath.getNodeRelativeStoragePath().isEmpty()) {
				List<FilesObject> contContent = getClient().listObjects(npath.getContainerName(), 100);
				while(!contContent.isEmpty()) {
					for(FilesObject obj: contContent) {
						try { getClient().deleteObject(npath.getContainerName(), obj.getName()); } catch (Exception e) {}
					}
					contContent = getClient().listObjects(npath.getContainerName(), 100);
				}
				try { getClient().deleteContainer(npath.getContainerName()); } catch (Exception e) {}
			} else {
				List<FilesObject> contContent = getClient().listObjects(npath.getContainerName(), npath.getNodeRelativeStoragePath(), 100);
				while(!contContent.isEmpty()) {
					for(FilesObject obj: contContent) {
						try { getClient().deleteObject(npath.getContainerName(), obj.getName()); } catch (Exception e) {}
					}
					contContent = getClient().listObjects(npath.getContainerName(), npath.getNodeRelativeStoragePath(), 100);
				}
				try {getClient().deleteObject(npath.getContainerName(), npath.getNodeRelativeStoragePath()); } catch (Exception e) {}
			}
		} catch(FilesNotFoundException e) {
			//ignore
		} catch (FilesException e) {
			throw new InternalServerErrorException(e);
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		}
		updateCredentials();
	}
	

	@Override
	public void setNodeSyncTo(String container, String syncTo, String syncKey) {
		try {
			getClient().setSyncTo(container, syncTo, syncKey);
		} catch (FilesAuthorizationException e) {
			throw new InternalServerErrorException(e);
		} catch (FilesException e) {
			throw new InternalServerErrorException(e);
		} catch (IOException e) {
			throw new InternalServerErrorException(e);
		} catch (HttpException e) {
			throw new InternalServerErrorException(e);
		}
	}

	private void updateCredentials() {
		if(!(cli.getAuthToken().equals(credentials.getAuthtoken()) && cli.getStorageURL().equals(credentials.getStorageurl()))) {
			credentials.setStorageurl(cli.getStorageURL());
			credentials.setAuthtoken(cli.getAuthToken());
			logger.debug("Added token to DB");
			UserHelper.setDataStoreCredentials(username, credentials.toString());
		}
	}

	@Override
	public void updateNodeInfo(NodePath npath, NodeInfo nodeInfo) {
		try {
			if(npath.isRoot(false)) { // root node
		    	FilesAccountInfo accountInfo = getClient().getAccountInfo();
				nodeInfo.setSize(accountInfo.getBytesUsed());
				nodeInfo.setContentType("application/directory");
	    	} else if(npath.getNodeStoragePathArray().length == 1) { // container info
				if(!getClient().containerExists(npath.getContainerName()))
					return;
				
				FilesContainerInfo contInfo = getClient().getContainerInfo(npath.getContainerName());
				nodeInfo.setSize(contInfo.getTotalSize());
				nodeInfo.setContentType("application/directory");
	    	} else { // info for a node inside a container
	    		try {
			    	FilesObjectMetaData nodeMeta = getClient().getObjectMetaData(npath.getContainerName(), npath.getNodeRelativeStoragePath());
			    	nodeInfo.setSize(Long.parseLong(nodeMeta.getContentLength()));
			    	nodeInfo.setContentType(nodeMeta.getMimeType());
	    		} catch(FilesNotFoundException e) {
			    	nodeInfo.setSize(0);
			    	nodeInfo.setContentType("application/file");
			    	logger.debug("Info for non-existent object");
	    		}
	    	}
		} catch(FilesException ex) {
			logger.error("Not found file "+npath.getNodeStoragePath());
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		} catch (HttpException ex) {
			ex.printStackTrace();
			throw new InternalServerErrorException(ex);
		}
		updateCredentials();
	}
	
}
