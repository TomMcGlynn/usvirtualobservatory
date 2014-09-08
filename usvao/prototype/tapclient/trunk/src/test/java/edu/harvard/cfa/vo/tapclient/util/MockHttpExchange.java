package edu.harvard.cfa.vo.tapclient.util;

import java.util.Map;

public interface MockHttpExchange {
    public String getRequestMethod();

    public void setRequestMethod(String newValue);

    public Map<String,String[]> getRequestHeaders();

    public Map<String,String[]> getRequestParameters();

    public int getResponseCode();

    public void setResponseCode(int newValue);

    public byte[] getResponseBody();

    public void setResponseBody(byte[] newValue);
}
