package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;

/**
 * Data model supported by the TAP service.
 */
public class DataModel {
    private String value;
    private String ivoId;

    DataModel(net.ivoa.xml.tap.v10.DataModelType xdataModel) {
	if (xdataModel != null) {
	    value = xdataModel.getStringValue();
	    ivoId = xdataModel.getIvoId();
	}
    }

    /**
     * The data model
     */
    public String getValue() {
	return value;
    }

    /**
     * The IVO identifier for the data model.
     */
    public String getIvoId() {
	return ivoId;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String value = getValue();
	String ivoId = getIvoId();
	if (value != null) {
	    output.println(indent+"value: "+value);
	}

	if (ivoId != null) {
	    output.println(indent+"ivo-id: "+ivoId);
	}
    }
}
