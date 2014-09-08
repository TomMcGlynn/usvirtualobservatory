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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;

import edu.caltech.vao.vospace.NodeType;

/**
 * Interface for communicating with backend storage
 */
public interface StorageManager {


    /**
     * Create a container at the specified location in the current backend storage
     * @param locationId The location of the container
     */
    public void createContainer(String locationId);

    /**
     * Create a container at the specified location in the current backend storage
     * @param locationId The location of the container
     */
    public void createNode(String locationId);

    /**
     * Move the bytes from the specified old location to the specified new location 
     * in the current backend storage
     * @param oldLocationId The old location of the bytes
     * @param newLocationId The new location of the bytes
     */
    public void moveBytes(String oldLocationId, String newLocationId);

    /**
     * Copy the bytes from the specified old location to the specified new location
     * in the current backend storage
     * @param oldLocationId The old location of the bytes
     * @param newLocationId The new location of the bytes
     */
    public void copyBytes(String oldLocationId, String newLocationId);

    /**
     * Put the bytes from the specified input stream at the specified location in 
     * the current backend storage
     * @param locationId The location for the bytes
     * @param stream The stream containing the bytes
     */
    public void putBytes(String locationId, InputStream stream);

    /**
     * Get the bytes from the specified location in the current backend storage
     * @param locationId The location of the bytes
     * @return a stream containing the requested bytes
     */
    public InputStream getBytes(String locationId);

    /**
     * Remove the bytes at the specified location in the current backend storage
     * @param locationId The location of the bytes
     */
    public void removeBytes(String locationId);

    /**
     * Retrieve when the bytes at the specified location in the current backend storage
     * were last modified. A response of -1 indicates that the information is not
     * available.
     * @param locationId The location to check
     * @return when the location was last modified
     */
    public long lastModified(String locationId);
    
    public String getNodeSize(String locationId);

	Hashtable<String, NodeType> getNodesList(String locationId);

}
