package edu.harvard.cfa.vo.tapclient.util;

import java.io.IOException;

/**
 * An exception used to indicate an unexpected format for a service response.
 */
public class ResponseFormatException extends IOException {
    public ResponseFormatException() {
	super();
    }

    public ResponseFormatException(String message) {
	super(message);
    }

    public ResponseFormatException(String message, Throwable cause) {
	super(message, cause);
    }

    public ResponseFormatException(Throwable cause) {
	super(cause);
    }
}