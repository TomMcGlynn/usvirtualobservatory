package edu.harvard.cfa.vo.tapclient.util;

public interface TestServer extends MockHttpExchange {

    public String getAddress();

    public int getPort();

    public void start() throws Exception;

    public void stop() throws Exception;

}