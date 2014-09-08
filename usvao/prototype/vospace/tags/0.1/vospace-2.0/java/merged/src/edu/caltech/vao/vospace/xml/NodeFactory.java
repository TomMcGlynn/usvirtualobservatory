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

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

/** 
 * A factory for creating nodes
 */
public class NodeFactory {

	private static NodeFactory ref;

	private NodeFactory() {}

	/*
	 * Get a NodeFactory
	 */
	public static NodeFactory getInstance() {
		if (ref == null) ref = new NodeFactory();
		return ref;
	}

	/*
	 * Get a node
	 */
	public Node getNode(InputStream in, int len){
		Node node = null;
		try {
			byte[] bytes = new byte[len];
			in.read(bytes, 0, len);
			String type = getType(bytes);
			node = (Node) Class.forName("edu.caltech.vao.vospace.xml." + type).getConstructor(byte[].class).newInstance(bytes);
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
		return node;
	}

	/*
	 * Get a node
	 */
	public Node getNode(HttpServletRequest req){
		Node node = null;
		try {
			InputStream in = req.getInputStream();
			int len = req.getContentLength();
			byte[] bytes = new byte[len];
			in.read(bytes, 0, len);
			String type = getType(bytes);
			node = (Node) Class.forName("edu.caltech.vao.vospace.xml." + type).getConstructor(byte[].class).newInstance(bytes);
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
		return node;
	}

	/*
	 * Get a node
	 */
	public Node getNode(byte[] bytes) {
		Node node = null;
		
		try {
			String type = getType(bytes);
			
			Class nodeClass = Class.forName("edu.caltech.vao.vospace.xml." + type);
			
			node = (Node) nodeClass.getConstructor(byte[].class).newInstance(bytes);
		} catch (ClassNotFoundException e) {
			throw new BadRequestException("Node type not supported.");
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
		return node;
	}

	private String getType(byte[] bytes) {
		String doc = new String(bytes).replace("'", "\"");
		int start = doc.indexOf("\"", doc.indexOf("xsi:type"));
		int end = doc.indexOf("\"", start + 1);
		String type = doc.substring(start + 1, end);
		return type.substring(type.indexOf(":") + 1);
	}

	/**
	 * Get a node of the default type for the service
	 * @return a Node of the default type
	 */
	public Node getDefaultNode() {
		String datanode = "<node xmlns=\"http://www.ivoa.net/xml/VOSpace/v2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"vos:DataNode\" uri=\"\" busy=\"false\"><properties></properties><accepts></accepts><provides></provides><capabilities></capabilities></node>";
		return getNode(datanode.getBytes());
	}

	public Node getDefaultContainer() {
		String contnode = "<node xmlns=\"http://www.ivoa.net/xml/VOSpace/v2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"vos:ContainerNode\" uri=\"\" busy=\"false\"><properties></properties><accepts></accepts><provides></provides><capabilities></capabilities></node>";
		return getNode(contnode.getBytes());
	}

}
