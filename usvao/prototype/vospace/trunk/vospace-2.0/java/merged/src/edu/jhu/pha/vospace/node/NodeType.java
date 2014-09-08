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
package edu.jhu.pha.vospace.node;

/**
 * The possible types of node
 */
public enum NodeType {
    NODE ("Node", Node.class), 
    DATA_NODE ("DataNode", DataNode.class), 
    LINK_NODE ("LinkNode", LinkNode.class), 
    CONTAINER_NODE ("ContainerNode", ContainerNode.class), 
    UNSTRUCTURED_DATA_NODE ("UnstructuredDataNode", UnstructuredDataNode.class), 
    STRUCTURED_DATA_NODE ("StructuredDataNode", StructuredDataNode.class);
    
    private String typeName;
    private Class nodeClass;

    NodeType(String text, Class nodeClass) {
      this.typeName = text;
      this.nodeClass = nodeClass;
    }

    public String getTypeName() {
      return this.typeName;
    }
    
    public Class getNodeClass() {
    	return nodeClass;
    }

    public static NodeType fromString(String text) {
      if (text != null) {
        for (NodeType b : NodeType.values()) {
          if (text.equalsIgnoreCase(b.typeName)) {
            return b;
          }
        }
      }
      return null;
    }
}
