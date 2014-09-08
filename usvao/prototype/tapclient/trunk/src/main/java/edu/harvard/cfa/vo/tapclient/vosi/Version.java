package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;

/**
 * The version of a capability element such as Language.
 */
public class Version {
    private String value;
    private String ivoId;

    Version(net.ivoa.xml.tap.v10.Version xversion) {
	if (xversion != null) {
	    value = xversion.getStringValue();
	    ivoId = xversion.getIvoId();
	}
    }

    /**
     * A version of the language supported by the server. 
     * @return the version value
     */
    public String getValue() {
	return value;
    }

    /**
     * An optional IVORN of the language. 
     * @return the IVO id associated with the version
     */
    public String getIvoId() {
	return ivoId;
    }

    /**
     * Write this Version to the PrintStream.
     * @param output the PrinteStream
     */
    public void list(PrintStream output) {
	list(output, "");
    }
    
    /**
     * Write this Version to the PrintStream.
     * @param output the PrinteStream
     * @param indent the indentation to prepend
     */
    public void list(PrintStream output, String indent) {
	if (getValue() != null)
	    output.println(indent+"value: "+getValue());
 	if (getIvoId() != null)
	    output.println(indent+"ivo-id: "+getIvoId());
    }
}
