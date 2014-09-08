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

public class Transfer {

    private XMLObject transfer;

    /**
     * Construct a Transfer from the byte array
     * @param bytes The byte array containing the Transfer
     */
    public Transfer(byte[] bytes)  {
	transfer = new XMLObject(bytes);
    }

    public Transfer(String bytes)  {
	this(bytes.getBytes());
    }
    

    /**
     * Get the target of the transfer
     * @return The target of the transfer
     */
    public String getTarget()  {
	return transfer.xpath("/vos:transfer/vos:target")[0];
    }

    /**
     * Get the direction of the transfer
     * @return The direction of the transfer
     */
    public String getDirection()  {
	return transfer.xpath("/vos:transfer/vos:direction")[0];
    }

    /**
     * Get the view of the transfer
     * @return The view of the transfer
     */
    public View getView()  {
	return new View(transfer.item("/vos:transfer/vos:view")[0]);
    }

    /**
     * Get the protocols of the transfer
     * @return The protocols of the transfer
     */
    public Protocol[] getProtocol()  {
	ArrayList<Protocol> protocols = new ArrayList<Protocol>();
	for (String protocol : transfer.item("/vos:transfer/vos:protocol")) {
	    protocols.add(new Protocol(protocol));
	}
	return protocols.toArray(new Protocol[0]);
    }

    /**
     * Is keepBytes set in the transfer?
     * @return The value of the keepBytes attribute
     */
    public boolean isKeepBytes()  {
	 String isKeepBytes = transfer.xpath("/vos:transfer/vos:keepBytes")[0];
	 return Boolean.valueOf(isKeepBytes).booleanValue();
    }

    /**
     * Add the protocols of the transfer
     */
    public void addProtocol(Protocol protocol)  {
	boolean hasView = transfer.has("/vos:transfer/vos:view");
	if (!hasView) {
	    transfer.add("/vos:transfer", protocol.toString());
	} else {
	    transfer.add("/vos:transfer/vos:view", protocol.toString());
	}
    }

    /**
     * Remove the protocols of the transfer
     */
    public void deleteProtocols()  {
	transfer.remove("/vos:transfer/vos:protocol");
    }

    /**
     * Get a string representation of the transfer
     * @return a string representation of the transfer
     */
    public String toString() {
	return transfer.toString();
    }


}
