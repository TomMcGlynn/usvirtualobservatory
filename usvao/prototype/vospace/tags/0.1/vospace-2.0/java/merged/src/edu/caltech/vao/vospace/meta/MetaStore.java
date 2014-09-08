/**
 * MetaStore.java
 * Author: Matthew Graham (Caltech)
 * Version: Original (0.1) - 27 June 2006
 */

package edu.caltech.vao.vospace.meta;

import java.sql.SQLException;
import java.util.List;

import edu.caltech.vao.vospace.NodeType;
import edu.caltech.vao.vospace.xml.Node;

/**
 * This interface represents a metadata store for VOSpace 
 */
public interface MetaStore {


    /**
     * Check whether the object with the specified identifier is in the store
     * @param identifier
     * @return
     */
    public boolean isStored(String identifier, String owner);

    /**
     * Store the metadata for the specified identifier
     * @param identifier
     * @param type
     * @param owner
     * @param metadata
     */
    public void storeData(String identifier, NodeType type, String owner, Object metadata) ;

    public String checkData(String[] identifiers, int limit) ;

    /**
     * Remove the metadata for the specified identifier
     * @param identifier
     */
    public void removeData(String identifier, String owner);
 
    /**
     * Update the metadata for the specified identifier
     * @param identifier
     * @param metadata
     */
    public void updateData(String identifier, String owner, Object metadata) ;

    /**
     * Update the metadata for the specified identifier including updating the
     * identifier
     * @param identifier
     * @param newIdentifier
     * @param metadata
     */
    public void updateData(String identifier, String newIdentifier, String owner, Object metadata) ;

    /**
     * Get a token
     * @param identifiers
     * @return
     */
    public String getToken(String[] identifiers) ;

    /**
     * Get the type of the object with the specified identifier
     * @param identifier
     * @return
     */
    public NodeType getType(String identifier, String owner) ;

    /**
     * Check whether the specified property is known to the service
     * @param identifier
     * @return
     * @
     */
    public boolean isKnownProperty(String identifier) ;

    /**
     * Register the specified property
     * @param property
     * @param type
     * @param readOnly
     */
    public void registerProperty(String property, int type, boolean readOnly) ;

    /**
     * Update the specified property
     * @param property
     * @param type
     */
    public void updateProperty(String property, NodeType type) ;


    /**
     * Get the property type of the specified node
     * @param identifier
     * @return
     */
    public String getPropertyType(String identifier) ;
    
    /**
     * Get the node
     * @param identifier
     * @return
     */
    public Node getNode(String identifier, String owner) ;
    
    /**
     * Get the node children
     * @param identifier
     * @return
     */
    public List<String> getNodeChildren(String identifier, String owner) ;

    public List<String> getAllChildren(String identifier, String owner);

}
