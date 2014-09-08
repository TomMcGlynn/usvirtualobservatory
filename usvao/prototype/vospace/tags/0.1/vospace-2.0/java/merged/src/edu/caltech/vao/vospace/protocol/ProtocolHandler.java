/**
 * ProtocolHandler.java
 * Author: Matthew Graham (Caltech)
 * Version: Original (0.1) - 31 July 2006
 */

package edu.caltech.vao.vospace.protocol;

import edu.jhu.pha.vospace.rest.JobDescription;

/**
 * This interface represents the implementation details of a protocol
 * involved in a data transfer
 */
public interface ProtocolHandler {

    /**
     * Return the registered identifier for this protocol 
     * @return
     */
    public String getUri();

    /**
     * Invoke the protocol handler and transfer data
     * @param job
     * @return
     */
    public void invoke(JobDescription job) throws Exception; 
}
