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

import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

public class Node {

	protected XMLObject node;
	private static Logger logger = Logger.getLogger(Node.class);

	/**
	 * Construct a Node from the byte array
	 * @param req The byte array containing the Node
	 */
	public Node(byte[] bytes) {
		node = new XMLObject(bytes);
	}

	/**
	 * Get the uri of the node
	 * @return The uri of the node
	 */
	public String getUri() {
		return node.xpath("/vos:node/@uri")[0];
	}

	/**
	 * Get the location of the node based on the node uri
	 * @return The location of the node
	 */
	/*public String getLocation() {
		String uri = getUri();
		String path = uri.substring(uri.lastIndexOf("!"));
        path = "/" + path.substring(path.indexOf("/") + 1);
        logger.debug("Returning "+path+" location from uri "+uri);
        return path;
	}*/
	
	/**
	 * Get the type of the node
	 * @return The type of the node
	 */
	public String getType() {
		return node.xpath("/vos:node/@xsi:type")[0];
	}

	/**
	 * Set the uri of the node
	 * @param uri The new uri of the node
	 */
	public void setUri(String uri) {
		node.replace("/vos:node/@uri", uri);
	}


	/**
	 * Check whether the node has any properties set
	 * @return whether the node has any properties set
	 */
	public boolean hasProperties() {
		try {
			return node.has("/vos:node/vos:properties/vos:property");
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Get the properties set on the node
	 * @return any properties the node has set on it
	 */
	public HashMap<String, String> getProperties() {
		try {
			HashMap<String, String> properties = new HashMap<String, String>();
			String[] propUris = node.xpath("/vos:node/vos:properties/vos:property/@uri");
			for (String uri: propUris) {
				String value = node.xpath("/vos:node/vos:properties/vos:property[@uri = '" + uri + "']")[0];
				properties.put(uri, value);
			}
			return properties;
		} catch (Exception e) {
			throw new InternalServerErrorException(e);
		}
	}

	/**
	 * Remove the <properties> element
	 */
	public void removeProperties() {
		node.remove("/vos:node/vos:properties");
	}

	/**
	 * Remove the <capabilities> element
	 */
	public void removeCapabilities() {
		node.remove("/vos:node/vos:capabilities");
	}

	/**
	 * Add a <capability> with the specified value to the <capabilities> element creating the latter
	 * if it does not exist.
	 * @param value The value of the <capability> element
	 */
	public void addCapabilities(String value) {
		boolean hasCapabilities = node.has("/vos:node/vos:capabilities");
		if (!hasCapabilities)
			node.add("/vos:node/vos:provides", node.PREFIX == null ? "<capabilities></capabilities>" : "<" + node.PREFIX + ":capabilities></" + node.PREFIX + ":capabilities>");
		if (value != null)
			node.addChild("/vos:node/vos:capabilities", node.PREFIX == null ? "<capability uri=\"" + value + "\"/>" : "<" + node.PREFIX + ":capability uri=\"" + value + "\"/>");
	}

	/**
	 * Get a string representation of the node
	 * @return a string representation of the node
	 */
	public String toString() {
		return node.toString();
	}

}
