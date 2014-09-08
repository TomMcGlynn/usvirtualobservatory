package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.IOException;

import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;

/**
 * An interface for the common, basic functions of a VO service.  These 
 * functions include service availability, capabilities, and tableset metadata.
 */
public interface Vosi {
    public Availability getAvailability() throws HttpException, ResponseFormatException, IOException;

    /**
     * Returns a list of Capability objects that represents the VOSI capabilities
     * of the service.  A new VOSI capabilities request is made to the 
     * service every time the getCapabilities method is called.
     * 
     * @return a list of Capabilty objects representing the VOSI capabilities of
     * the service.
     *
     * @throws HttpException if the service responses to the VOSI Capabilities request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Capabilities document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public Capabilities getCapabilities() throws HttpException, ResponseFormatException, IOException;

    /**
     * Returns a list of Schema objects that represents the VOSI table set metadata of the service.  A new VOSI tables request is made to the service every
     * time the getTableSet method is called.
     *
     * @return the VOSI tables from the service.
     *
     * @throws HttpException if the service responses to the VOSI Tables request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Tables document.
     * @throws IOException if an error occurs creating an input stream.
     */
    public TableSet getTableSet() throws HttpException, ResponseFormatException, IOException;
}
