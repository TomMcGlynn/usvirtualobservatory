package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Optional features of the query language grouped by feature type. 
 */
public class LanguageFeatures {
    private List<LanguageFeature> featureList;
    private String type;

    LanguageFeatures(net.ivoa.xml.tap.v10.LanguageFeatureList xfeatures) {
	featureList = new ArrayList<LanguageFeature>();
	if (xfeatures != null) {
	    for (net.ivoa.xml.tap.v10.LanguageFeature xfeature: xfeatures.getFeatureList()) {
		featureList.add(new LanguageFeature(xfeature));
	    }
	    type = xfeatures.getType();
	}
    }

    /**
     * A list of language features of a given type.
     */
    public List<LanguageFeature> getFeatures() {
	return featureList;
    }

    /**
     * The type of the features given here. 
     */
    public String getType() {
	return type;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	List<LanguageFeature> featureList = getFeatures();
	String type = getType();
	if (featureList != null) {
	    output.println(indent+"Language Features: ");
	    for (LanguageFeature feature: featureList) {
		feature.list(output, "  ");
	    }
	}

	if (type != null) {
	    output.println(indent+"Type: "+type);
	}
    }
}
