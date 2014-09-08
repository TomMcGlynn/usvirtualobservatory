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
package edu.jhu.pha.vospace.meta;

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.jhu.pha.vospace.node.Node;
import edu.jhu.pha.vospace.node.Node.PropertyType;
import edu.jhu.pha.vospace.node.NodeInfo;
import edu.jhu.pha.vospace.node.NodeType;
import edu.jhu.pha.vospace.node.VospaceId;

/**
 * This interface represents a metadata store for VOSpace 
 */
public interface MetaStore {

    /**
     * Get the node bytes
     * @param identifier
     * @return Node bytes
     */
	public byte[] getNodeBytes(VospaceId identifier);
 
    /**
     * Get the node children
     * @param uri
     * @param searchDeep
     * @param includeDeleted
     * @return
     */
    public NodesList getNodeChildren(VospaceId uri, boolean searchDeep, boolean includeDeleted, int start, int count) ;

    public NodeInfo getNodeInfo(VospaceId uri);

    /**
     * Get the type of the object with the specified identifier
     * @param identifier
     * @return
     */
    public NodeType getType(VospaceId identifier) ;

    //public void incrementRevision(VospaceId uri);


    /**
     * Check whether the object with the specified identifier is in the store
     * @param identifier
     * @return
     */
    public boolean isStored(VospaceId identifier);
    
    public void markRemoved(VospaceId uri);

	/**
     * Remove the metadata for the specified identifier
     * @param identifier
     */
    public void remove(VospaceId identifier);

	public List<VospaceId> search(VospaceId identifier, String searchPattern, int fileLimit, boolean includeDeleted);

    /**
     * Store the metadata for the specified identifier
     * @param identifier
     * @param type
     * @param metadata
     */
    public void storeData(VospaceId identifier, NodeType type) ;

	public void storeInfo(VospaceId identifier, NodeInfo info);

	/**
     * Update the metadata for the specified identifier including updating the
     * identifier
     * @param identifier
     * @param newIdentifier
     * @param metadata
     */
    public void updateData(VospaceId identifier, VospaceId newIdentifier) ;

	/**
     * Update the specified properties
     * @param properties
     */
    public void updateUserProperties(VospaceId identifier, Map<String, String> properties);

	/**
     * Get share ID for node
     */
	String createShare(VospaceId identifier, String groupId, boolean write_perm);

	public Map<String, String> getProperties(VospaceId identifier, PropertyType properties);

}
