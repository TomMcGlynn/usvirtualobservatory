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


public class DataNode extends Node {

	/**
	 * Construct a Node from the byte array
	 * @param req The byte array containing the Node
	 */
	public DataNode(byte[] bytes) {
		super(bytes);
	}

	/**
	 * Validate the structure of the document
	 */
	protected boolean validStructure() {
		// Check to see whether accepts, provides and capabilities defined
		return true;
	}

	/**
	 * Remove the <accepts> element
	 */
	public void removeAccepts() {
		node.remove("/vos:node/vos:accepts");
	}

	/**
	 * Remove the <provides> element
	 */
	public void removeProvides() {
		node.remove("/vos:node/vos:provides");
	}

	/**
	 * Remove the busy attribute
	 */
	public void removeBusy() {
		node.remove("/vos:node/@busy");
	}


	/**
	 * Add a <view> with the specified value to the <accepts> element creating the latter
	 * if it does not exist.
	 * @param value The value of the <view> element
	 */
	public void addAccepts(String value) {
		boolean hasAccepts = node.has("/vos:node/vos:accepts");
		if (!hasAccepts)
			node.add("/vos:node/vos:properties", node.PREFIX == null ? "<accepts></accepts>" : "<" + node.PREFIX + ":accepts></" + node.PREFIX + ":accepts>");
		if (value != null)
			node.addChild("/vos:node/vos:accepts", node.PREFIX == null ? "<view uri=\"" + value + "\"/>" : "<" + node.PREFIX + ":view uri=\"" + value + "\"/>");
	}

	/**
	 * Add a <view> with the specified value to the <provides> element creating the latter
	 * if it does not exist.
	 * @param value The value of the <view> element
	 */
	public void addProvides(String value) {
		boolean hasProvides = node.has("/vos:node/vos:provides");
		if (!hasProvides)
			node.add("/vos:node/vos:accepts", node.PREFIX == null ? "<provides></provides>" : "<" + node.PREFIX + ":provides></" + node.PREFIX + ":provides>");
		if (value != null)
			node.addChild("/vos:node/vos:provides", node.PREFIX == null ? "<view uri=\"" + value + "\"/>" : "<" + node.PREFIX + ":view uri=\"" + value + "\"/>");
	}

	/**
	 * Set the busy attribute
	 * @param value The value of the busy attribute
	 */
	public void setBusy(boolean value) {
		node.replace("/vos:node/@busy", String.valueOf(value));
	}

}
