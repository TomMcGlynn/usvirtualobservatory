package edu.harvard.cfa.vo.tapclient.util;

import java.io.IOException;

/**
 * An exception used to indicate a service responded with an unexpected HTTP response code.
 */
public class HttpException extends IOException {
    private int statusCode;
    private String content;

    /**
     * Construct an HttpException object from the given HTTP status code.
     * The detail message is set to a default string for the given status code.
     * @param statusCode the HTTP status code, for example 404 for requests that are not found.
     */
    public HttpException(int statusCode) {
	super();
	this.statusCode = statusCode;
    }

    /**
     * Construct an HttpException object from the given HTTP status code.
     * The detail message is set to a default string for the given status code.
     * @param message the detail message string of this HttpException.  If null, it defaults to a string representation of the status code.
     */
    public HttpException(String message) {
	super(message);
    }

    /**
     * Construct an HttpException object from the given HTTP status code.
     * The detail message is set to a default string for the given status code.
     * @param message the detail message string of this HttpException.  If null, it defaults to a string representation of the status code.
     * @param statusCode the HTTP status code, for example 404 for requests that are not found.
     */
    public HttpException(String message, int statusCode) {
	super(message);
	this.statusCode = statusCode;
    }

    /**
     * Construct an HttpException object from the given HTTP status code.
     * The detail message is set to a default string for the given status code.
     * @param message the detail message string of this HttpException.  If null, it defaults to a string representation of the status code.
     * @param statusCode the HTTP status code, for example 404 for requests that are not found.
     * @param content the HTTP response body content
     */
    public HttpException(String message, int statusCode, String content) {
	super(message);
	this.statusCode = statusCode;
	this.content = content;
    }

    /**
     * Construct an HttpException object from the given HTTP status code.
     * The detail message is set to a default string for the given status code.
     * @param message the detail message string of this HttpException.  If null, it defaults to a string representation of the status code.
     * @param cause the cause of the exception.
     */
    public HttpException(String message, Throwable cause) {
	super(message, cause);
    }

   /**
    * Construct an HttpException object from the given HTTP status code.
    * The detail message is set to a default string for the given status code.
    * @param cause the cause of the exception.
    */
    public HttpException(Throwable cause) {
	super(cause);
    }

    /**
     * Get the HTTP Status Code.
     * @return the HTTP Status Code
     */
    public int getStatusCode() {
	return statusCode;
    }
    
    /**
     * Get the HTTP response content.
     * @return the HTTP response content
     */
    public String getContent() {
	return content;
    }

}