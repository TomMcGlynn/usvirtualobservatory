package edu.harvard.cfa.vo.tapclient.util;

import java.util.HashMap;
import java.util.Map;

public class DefaultMockHttpExchange implements MockHttpExchange {
    private String requestMethod;
    private Map<String,String[]> requestHeaders;
    private Map<String,String[]> requestParameters;
    private int responseCode;
    private byte[] responseBody;

    protected DefaultMockHttpExchange() {
	requestMethod = "UNINITIALIZED";
	requestHeaders = new HashMap<String,String[]>();
	requestParameters = new HashMap<String,String[]>();
	responseCode = 200;
	responseBody = new byte[0];
    }

    public String getRequestMethod() {
	return requestMethod;
    }

    public void setRequestMethod(String newValue) {
	requestMethod = newValue;
    }

    public Map<String,String[]> getRequestHeaders() {
	return requestHeaders;
    }

    public Map<String,String[]> getRequestParameters() {
	return requestParameters;
    }

    public int getResponseCode() {
	return responseCode;
    }

    public void setResponseCode(int newValue) {
	responseCode = newValue;
    }

    public byte[] getResponseBody() {
	return responseBody;
    }

    public void setResponseBody(byte[] newValue) {
	responseBody = newValue;
    }
}
