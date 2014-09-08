package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A TAP TableAccess Capability
 */
public class TableAccess extends Capability {
    private List<DataModel> dataModelList;
    private List<Language> languageList;
    private List<OutputFormat> outputFormatList;
    private List<UploadMethod> uploadMethodList;
    private TimeLimits retentionPeriod;
    private TimeLimits executionDuration;
    private DataLimits outputLimit;
    private DataLimits uploadLimit;
    
    TableAccess(net.ivoa.xml.tap.v10.TableAccess xtableAccess) {
	super(xtableAccess);
	dataModelList = new ArrayList<DataModel>(); 
	languageList = new ArrayList<Language>(); 
	outputFormatList = new ArrayList<OutputFormat>(); 
	uploadMethodList = new ArrayList<UploadMethod>(); 
	if (xtableAccess != null) {
	    for (net.ivoa.xml.tap.v10.DataModelType xdataModel: xtableAccess.getDataModelList()) {
		dataModelList.add(new DataModel(xdataModel));
	    }
	    for (net.ivoa.xml.tap.v10.Language xlanguage: xtableAccess.getLanguageList()) {
		languageList.add(new Language(xlanguage));
	    }
	    for (net.ivoa.xml.tap.v10.OutputFormat xoutputFormat: xtableAccess.getOutputFormatList()) {
		outputFormatList.add(new OutputFormat(xoutputFormat));
	    }
	    for (net.ivoa.xml.tap.v10.UploadMethod xuploadMethod: xtableAccess.getUploadMethodList()) {
		uploadMethodList.add(new UploadMethod(xuploadMethod));
	    }
	    retentionPeriod = new TimeLimits(xtableAccess.getRetentionPeriod());
	    executionDuration =new TimeLimits(xtableAccess.getExecutionDuration());
	    outputLimit = new DataLimits(xtableAccess.getOutputLimit());
	    uploadLimit = new DataLimits(xtableAccess.getUploadLimit());
	}
    }

    /**
     * Return a list of  data models associated with the service.
     */    
    public List<DataModel> getDataModels() {
	return dataModelList;
    }

    /***
     * A list of query languages the service LANG parameter accepts.
     */
    public List<Language> getLanguages() {
	return languageList;
    }

    /**
     * A list of output formats the FORMAT parameter accepts.
     */
    public List<OutputFormat> getOutputFormats() {
	return outputFormatList;
    }

    /**
     * A list of upload methods the UPLOAD parameter accepts.
     */
    public List<UploadMethod> getUploadMethods() {
	return uploadMethodList;
    }

    /**
     * Limits on how long a job is kept by the service
     */
    public TimeLimits getRetentionPeriod() {
	return retentionPeriod;
    }

    /**
     * Limits on how long a job is allowed to run by the service
     */
    public TimeLimits getExecutionDuration() {
	return executionDuration;
    }

    /**
     * Row or byte limits on the results a service returns
     */
    public DataLimits getOutputLimit() {
	return outputLimit;
    }

    /**
     * Row or byte limits on the upload tables a user may supply.
     */
    public DataLimits getUploadLimit() {
	return uploadLimit;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	super.list(output, indent);

	List<DataModel> dataModels = getDataModels();
	List<Language> languages = getLanguages();
	List<OutputFormat> outputFormats = getOutputFormats();
	List<UploadMethod> uploadMethods = getUploadMethods();
	TimeLimits retentionPeriod = getRetentionPeriod();
	TimeLimits executionDuration = getExecutionDuration();
	DataLimits outputLimit = getOutputLimit();
	DataLimits uploadLimit = getUploadLimit();
	
	if (dataModels != null) {
	    output.println(indent+"Data models:");
	    for (DataModel dataModel: dataModels) {
		dataModel.list(output, indent+"  ");
	    }
	}

	if (languages != null) {
	    output.println(indent+"Languages:");
	    for (Language language: languages) {
		language.list(output, indent+"  ");
	    }
	}

	if (outputFormats != null) {
	    output.println(indent+"Output formats:");
	    for (OutputFormat outputFormat: outputFormats) {
		outputFormat.list(output, indent+"  ");
	    }
	}

	if (uploadMethods != null) {
	    output.println(indent+"Upload methods:");
	    for (UploadMethod uploadMethod: uploadMethods) {
		uploadMethod.list(output, indent+"  ");
	    }
	}

	if (retentionPeriod != null) {
	    output.println(indent+"Retention period:");
	    retentionPeriod.list(output, indent+"  ");
	}
	    
	if (executionDuration != null) {
	    output.println(indent+"Execution duration:");
	    executionDuration.list(output, indent+"  ");
	}
	    
	if (outputLimit != null) {
	    output.println(indent+"Output limit:");
	    outputLimit.list(output, indent+"  ");
	}
	    
	if (uploadLimit != null) {
	    output.println(indent+"Upload limit:");
	    uploadLimit.list(output, indent+"  ");
	}
    }
}	    
