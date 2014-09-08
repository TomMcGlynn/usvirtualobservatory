 
package edu.caltech.vao.vospace;

import java.net.URISyntaxException;

public class VOSpaceResource {

    protected final VOSpaceManager manager;
    //    private final String PROPFILE = "/Users/mjg/Projects/vospace/vospace-2.0/java/vospace.properties";

    public VOSpaceResource() throws VOSpaceException {
	// Get property file
	try {
	    String propFile = this.getClass().getClassLoader().getResource("vospace.properties").toURI().getRawPath();
	    manager = VOSpaceManager.getInstance(propFile);
	} catch (URISyntaxException e) {
	    throw new VOSpaceException(VOSpaceException.INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }
}