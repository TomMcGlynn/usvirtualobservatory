package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;

/**
 * Optional feature of the query language.
 */
public class LanguageFeature {
    private String form;
    private String description;

    LanguageFeature(net.ivoa.xml.tap.v10.LanguageFeature xfeature) {
	form = xfeature != null ? xfeature.getForm() : null;
	description = xfeature != null ? xfeature.getDescription() : null;
    }

    /**
     * Formal notation for the language feature. 
     */
    public String getForm() {
	return form;
    }

    /**
     * Human-readable freeform documentation for the language feature. 
     */
    public String getDescription() {
	return description;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String form = getForm();
	String description = getDescription();
	if (form != null) {
	    output.println(indent+"Form: "+form);
	}
	if (description != null) {
	    output.println(description+"Description: "+description);
	}
    }
}
