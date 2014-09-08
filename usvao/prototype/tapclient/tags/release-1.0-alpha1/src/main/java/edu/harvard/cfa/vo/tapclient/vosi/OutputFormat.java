package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Output format supported by the TAP service.
 */
public class OutputFormat {
    private String mime;
    private List<String> aliasList;
    private String ivoId;
    
    OutputFormat(net.ivoa.xml.tap.v10.OutputFormat xoutputFormat) {
	aliasList = new ArrayList<String>();
	if (xoutputFormat != null) {
	    mime = xoutputFormat.getMime();
	    aliasList.addAll(xoutputFormat.getAliasList());
	    ivoId = xoutputFormat.getIvoId();
	}
    }

    /**
     * The MIME type of this format. 
     */
    public String getMime() {
	return mime;
    }

    /**
     * Other values of FORMAT ("shorthands") that make the service return documents with the MIME type. 
     */
    public List<String> getAliases() {
	return aliasList;
    }

    /**
     * The IVO identifier for the output format.
     */    
    public String getIvoId() {
	return ivoId;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String mime = getMime();
	List<String> aliasList = getAliases();
	String ivoId = getIvoId();
	
	if (mime != null) {
	    output.println(indent+"mime: "+mime);
	}

	if (aliasList != null) {
	    for (String alias: aliasList) {
		output.println(indent+"alias: "+alias);
	    }
	}

	if (ivoId != null) {
	    output.println(indent+"ivo-id: "+ivoId);
	}
    }
}

