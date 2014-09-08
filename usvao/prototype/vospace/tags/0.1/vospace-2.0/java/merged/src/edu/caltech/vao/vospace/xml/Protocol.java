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


import java.util.ArrayList;

public class Protocol  {

	private XMLObject protocol;

	/**
	 * Construct an empty Protocol
	 */
	public Protocol() {
		String blank = "<vos:protocol xmlns:vos=\"http://www.ivoa.net/xml/VOSpace/v2.0\" uri=\"\"></vos:protocol>";
		protocol = new XMLObject(blank.getBytes());
	}

	/**
	 * Construct a Protocol from the byte array
	 * @param bytes The byte array containing the Protocol
	 */
	public Protocol(byte[] bytes)  {
		protocol = new XMLObject(bytes);
	}

	/**
	 * Construct a Protocol from the string representation
	 * @param bytes The string containing the Protocol
	 */
	public Protocol(String bytes)  {
		protocol = new XMLObject(bytes.getBytes());
	}

	/**
	 * Get the endpoint of the protocol
	 * @return The endpoint of the protocol
	 */
	public String getEndpoint()  {
		return protocol.xpath("/vos:protocol/vos:endpoint")[0];
	}

	/**
	 * Get the URI of the protocol
	 * @return The URI of the protocol
	 */
	public String getURI()  {
		return protocol.xpath("/vos:protocol/@uri")[0];
	}

	/**
	 * Get the params of the protocol
	 * @return The params of the protocol
	 */
	public Param[] getParam()  {
		ArrayList<Param> params = new ArrayList<Param>();
		for (String param : protocol.item("/vos:protocol/vos:param")) {
			params.add(new Param(param));
		} 
		return params.toArray(new Param[0]);
	}

	/**
	 * Set the endpoint of the protocol
	 * @param endpoint The endpoint of the protocol
	 */
	public void setEndpoint(String endpoint)  {
		boolean hasEndpoint = protocol.has("/vos:protocol/vos:endpoint");
		if (!hasEndpoint)
			protocol.addChild("/vos:protocol", protocol.PREFIX == null ? "<endpoint></endpoint>" : "<" + protocol.PREFIX + ":endpoint></" + protocol.PREFIX + ":endpoint>");
		if (endpoint != null)
			protocol.replace("/vos:protocol/vos:endpoint", endpoint);
	}

	/**
	 * Set the URI of the protocol
	 * @return The URI of the protocol
	 */
	public void setURI(String uri)  {
		protocol.replace("/vos:protocol/@uri", uri);
	}

	/**
	 * Get a string representation of the protocol
	 * @return a string representation of the protocol
	 */
	public String toString() {
		return protocol.toString();
	}

}
