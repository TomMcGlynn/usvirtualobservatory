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
package edu.jhu.pha.vospace.node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.TokenBuffer;

import edu.jhu.pha.vospace.QueueConnector;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.jobs.JobsProcessorServlet;

public class DataNode extends Node implements Cloneable {
	private static final Logger logger = Logger.getLogger(DataNode.class);
	private static final MappingJsonFactory f = new MappingJsonFactory();
	
	/**
	 * Construct a Node from the byte array
	 * @param req The byte array containing the Node
	 */
	public DataNode(byte[] bytes, String username, VospaceId id) {
		super(bytes, username, id);
	}


	public DataNode(VospaceId id, String username){
		super(id, username);
	}
	
	@Override
	public Object export(String format, Detail detail) {
		if(format.equals("json-dropbox") || format.equals("json-dropbox-object")){
			TokenBuffer g = new TokenBuffer(null);
			try {
	        	g.writeStartObject();

				g.writeStringField("size", readableFileSize(getNodeInfo().getSize()));
				g.writeNumberField("rev", getNodeInfo().getRevision());
				g.writeBooleanField("thumb_exists", false);
				g.writeNumberField("bytes", getNodeInfo().getSize());
				g.writeStringField("modified", dropboxDateFormat.format(getNodeInfo().getMtime()));
				g.writeStringField("path", getUri().getNodePath().getNodeOuterPath());
				g.writeBooleanField("is_dir", false);
				g.writeStringField("icon", "file");
				g.writeStringField("root", (getUri().getNodePath().isEnableAppContainer()?"sandbox":"dropbox"));
				g.writeStringField("mime_type", getNodeInfo().getContentType());
	        	
	        	g.writeEndObject();
		    	g.close(); // important: will force flushing of output, close underlying output stream
			} catch (JsonGenerationException e) {
				e.printStackTrace();
				throw new InternalServerErrorException("Error generationg JSON: "+e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				throw new InternalServerErrorException("Error generationg JSON: "+e.getMessage());
			}
			
			if(format.equals("json-dropbox")) {
				try {
			    	ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
					JsonGenerator g2 = f.createJsonGenerator(byteOut); //.useDefaultPrettyPrinter() - doesn't work with metadata header
					g.serialize(g2);
					g2.close();
					byteOut.close();
	
					return byteOut.toByteArray();
				} catch (IOException e1) { // shouldn't happen
					logger.error("Error creating JSON generator: "+e1.getMessage());
					throw new InternalServerErrorException("Error creating JSON generator: "+e1.getMessage());
				}

			} else {
				try {
					return g.asParser(f.getCodec()).readValueAsTree();
				} catch (JsonProcessingException e) {
					logger.error("Error generating JSON: "+e.getMessage());
					throw new InternalServerErrorException("Error generating JSON: "+e.getMessage());
				} catch (IOException e) {
					logger.error("Error generating JSON: "+e.getMessage());
					throw new InternalServerErrorException("Error generating JSON: "+e.getMessage());
				}
			}
		} else {
			return getXmlMetadata(detail).getBytes();
		}
	}

	/**
	 * Set the node content
	 * @param data The new node content
	 */
	public void setData(InputStream data) {
		if(!getMetastore().isStored(getUri()))
			throw new NotFoundException("NodeNotFound");
		logger.debug("Updating node "+getUri().toString());

		// put the node data into storage
		getStorage().putBytes(getUri().getNodePath(), data);
		
		// update node size from storage to metadata
		getStorage().updateNodeInfo(getUri().getNodePath(), getNodeInfo());
		
		getNodeInfo().setRevision(getNodeInfo().getRevision()+1);//increase revision version to store in DB
		
		getMetastore().storeInfo(getUri(), getNodeInfo());
		
		try {
			// Update root container size
			ContainerNode contNode = (ContainerNode)NodeFactory.getInstance().getNode(
					new VospaceId(new NodePath(getUri().getNodePath().getContainerName())), 
					this.getOwner());
			getStorage().updateNodeInfo(contNode.getUri().getNodePath(), contNode.getNodeInfo());
			getMetastore().storeInfo(contNode.getUri(), contNode.getNodeInfo());
			//logger.debug("Updated node "+contNode.getUri().toString()+" size to: "+contNode.getNodeInfo().getSize());
		} catch (URISyntaxException e) {
			logger.error("Updating root node size failed: "+e.getMessage());
		}
		
		QueueConnector.goAMQP("setData", new QueueConnector.AMQPWorker<Boolean>() {
			@Override
			public Boolean go(com.rabbitmq.client.Connection conn, com.rabbitmq.client.Channel channel) throws IOException {

				channel.exchangeDeclare(conf.getString("vospace.exchange.nodechanged"), "fanout", false);
				channel.exchangeDeclare(conf.getString("process.exchange.nodeprocess"), "fanout", true);

				Map<String,Object> nodeData = new HashMap<String,Object>();
				nodeData.put("uri",getUri().toString());
				nodeData.put("owner",getOwner());
    			nodeData.put("container", getUri().getNodePath().getParentPath().getNodeStoragePath());

    			byte[] jobSer = (new ObjectMapper()).writeValueAsBytes(nodeData);
    			channel.basicPublish(conf.getString("vospace.exchange.nodechanged"), "", null, jobSer);
				channel.basicPublish(conf.getString("process.exchange.nodeprocess"), "", null, jobSer);
		    	
		    	return true;
			}
		});

	}

	@Override
	public NodeType getType() {
		return NodeType.DATA_NODE;
	}

}
