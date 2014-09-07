package org.usvao.sso.openid.portal;

import java.io.IOException;

/**
 * an exception indicating that an HTTP retrieval failed due to an 
 * unexpected response code returned by the server.
 */
public class HTTPErrorResponseException extends IOException {

    private int status = 0;
    private String response = null;

    /**
     * create the exception
     * @param code       the HTTP response code returned
     * @param resp       the response message returned with the code
     * @param message    the explanatory message for the error
     */
    public HTTPErrorResponseException(int code, String resp, String message) {
        super(message);
        status = code;
        response = resp;
    }

    /**
     * create the exception
     * @param code       the HTTP response code returned
     * @param resp       the response message returned with the code
     */
    public HTTPErrorResponseException(int code, String resp) {
        this(code, resp, defaultMessage(code, resp));
    }

    /**
     * create the exception
     * @param code       the HTTP response code returned
     */
    public HTTPErrorResponseException(int code) {
        this(code, null);
    }

    static String defaultMessage(int code, String resp) {
        if (code <= 0) 
            return "Unknown HTTP response code returned";
        StringBuilder sb = 
            new StringBuilder("Unexpected HTTP response code returned: ");
        sb.append(code);
        if (resp != null) 
            sb.append(" (").append(resp).append(')');
        return sb.toString();
    }

    /**
     * return the errant response code 
     */
    public int getResponseCode() { return status; }

    /**
     * return the response message returned with the response code
     */
    public String getResponseMessage() { return response; }
}
