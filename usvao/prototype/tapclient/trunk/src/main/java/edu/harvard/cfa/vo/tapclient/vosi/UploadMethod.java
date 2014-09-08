package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;

/**
 * The upload method supported by a service.
 */
public class UploadMethod {	
    private String ivoId;
    
    UploadMethod(net.ivoa.xml.tap.v10.UploadMethod xuploadMethod) {
	ivoId = xuploadMethod.getIvoId();
    }
    
    /**
     * The IVORN of the upload method. 
     * @return the IVO id associated with the upload method.
     */
    public String getIvoId() {
	return ivoId;
    }

    /**
     * Write this UploadMethod to the PrintStream
     * @param output the PrintStream
     */
    public void list(PrintStream output) {
	list(output, "");
    }


    /**
     * Write this UploadMethod to the PrintStream
     * @param output the PrintStream
     * @param indent the indentation to prepend
     */
    public void list(PrintStream output, String indent) {
	if (getIvoId() != null)
	    output.println(indent+"ivo-id: "+getIvoId());
    }
}
