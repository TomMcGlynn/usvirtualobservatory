package edu.harvard.cfa.vo.tapclient.tap;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;


/**
 * An object that can be used to get information about error conditions resulting from an asynchronous query.
 * @see AsyncJob
 */
public class Error {
    private TapService service;
    private String jobId;
    private String message;
    private String errorType;
    private boolean detailedErrorAvailable;
    
    /**
     * Construct a new Error for the TAP service and job identified by service and jobId.
     * 
     * @param service the TAP service
     * @param jobId job id for the job to which this Error pertains
     */
    Error(TapService service, String jobId, String message, String type, boolean detailedErrorAvailable) {
	this.service = service; 
	this.jobId = jobId;
	this.message = message;
	this.errorType = type;
	this.detailedErrorAvailable = detailedErrorAvailable;
    }

    /**
     * Returns the error message for this error summary
     * @return message of this error summary
     */
    public String getMessage() { 
	return message; 
    }

    /**
     * Returns the error type for this error summary
     * @return error type of this error summary
     */
    public String getType() { 
	return errorType; 
    }

    /**
     * Returns true if a detailed error is available on the TAP service
     * @return true if a detailed error is availabile, false otherwise
     */
    public boolean isDetailedErrorAvailable() { return detailedErrorAvailable; }

    /**
     * Returns an InputStream containing the TAP service response to a request
     * for the detailed error.  The result of this request is undefined if 
     * isDetailedErrorAvailable returns false.
     *
     * @return InputStream detailed error response from TAP service.
     *
     * @throws HttpException if the service responses with an unexpected HTTP status.
     * @throws IOException if an error occurs creating the InputStream
     * @throws NullPointerException if either service or jobId is null
     *
     * @see #isDetailedErrorAvailable
     */
    public InputStream openStream() throws HttpException, IOException {
	if (service == null || jobId == null) 
	    throw new NullPointerException();

	return HttpClient.get(service.getBaseURL()+"/async/"+jobId+"/error");
    }

    // populate this Error object with values from another Error object.
    void populate(Error error) {
	this.service = error.service;
	this.jobId = error.jobId;
	this.message = error.getMessage();
	this.errorType = error.getType();
	this.detailedErrorAvailable = error.isDetailedErrorAvailable();
    }

    public void list(PrintStream output) throws HttpException, IOException {
	InputStream inputStream = new BufferedInputStream(openStream());
	try {
	    int b = -1;
	    while ((b = inputStream.read()) != -1) {
		output.write(b);
	    }
	} finally {
	    inputStream.close();
	}
    }
}
