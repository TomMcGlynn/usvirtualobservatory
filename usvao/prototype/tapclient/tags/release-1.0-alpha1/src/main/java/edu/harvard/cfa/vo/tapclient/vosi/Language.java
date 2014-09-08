package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about query languages supported by TAP services.  This includes language name, version, description, and supported features.
 */
public class Language {
    private String name;
    private List<Version> versionList;
    private String description;
    private List<LanguageFeatures> languageFeaturesList;
    
    Language(net.ivoa.xml.tap.v10.Language xlanguage) {
	versionList = new ArrayList<Version>();
	languageFeaturesList = new ArrayList<LanguageFeatures>();

	if (xlanguage != null) {
	    name = xlanguage.getName();
	    for (net.ivoa.xml.tap.v10.Version xversion: xlanguage.getVersionList()) {
		versionList.add(new Version(xversion));
	    }
	    description = xlanguage.getDescription();
	    for (net.ivoa.xml.tap.v10.LanguageFeatureList xlanguageFeatureList: xlanguage.getLanguageFeaturesList()) {
		languageFeaturesList.add(new LanguageFeatures(xlanguageFeatureList));
	    }
	} 
    }

    /**
     * The language name
     * @return the language name
     */    
    public String getName() {
	return name;
    }

    /**
     * Returns a list of versions supported by the service for this language.
     * @return the Version list
     */    
    public List<Version> getVersions() {
	return versionList;
    }

    /**
     * A language description
     * @return description
     */    
    public String getDescription() {
	return description;
    }

    /**
     * Returns a list of language features supported by the service for this language.
     * @return the languageFeatures list
     */    
    public List<LanguageFeatures> getLanguageFeatures() {
	return languageFeaturesList;
    }

    /**
     * Write this Languae to the PrintStream
     * @param output the PrintStream
     */
    public void list(PrintStream output) {
	list(output, "");
    }

    /**
     * Write this Languae to the PrintStream
     * @param output the PrintStream
     * @param indent the indentation to prepend
     */
    public void list(PrintStream output, String indent) {
	String name = getName();
	String description = getDescription();
	List<Version> versions = getVersions();
	List<LanguageFeatures> languageFeatures = getLanguageFeatures();

	if (name != null) {
	    output.println(indent+"Name: "+name);
	}
	if (description != null) {
	    output.println(indent+"Description: "+description);
	}
	if (versions != null) {
	    for (Version version: versions) {
		version.list(output, indent+"  ");
	    }
	}
	if (languageFeatures != null) {
	    for (LanguageFeatures languageFeature: languageFeatures) {
		languageFeature.list(output, indent+"  ");
	    }
	}
    }
}
