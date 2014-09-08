package org.nvo.service.validation;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

/**
 * A QueryConnection implementation that invokes a service via a 
 * simple Get request on a URL.
 */
public class HTTPGetQueryConnection implements HTTPQueryConnection {

    protected HttpURLConnection service = null;
    protected InputStream strm = null;
    protected IOException problem = null;
    protected int code = -1;
    protected String respMsg = null;
    protected Thread connthread = new ConnThread();

    /**
     * open a connection to a URL.  
     * @exception IllegalArgumentException  if the input is not an HTTP or 
     *              HTTPS URL.
     */
    public HTTPGetQueryConnection(URL serviceURL) {
        if (! serviceURL.getProtocol().equals("http") && 
            ! serviceURL.getProtocol().equals("https"))
            throw new IllegalArgumentException("Not an HTTP(S) URL: " + 
                                               serviceURL);
        try {
            service = (HttpURLConnection) serviceURL.openConnection();
            service.setUseCaches(false);
            service.setInstanceFollowRedirects(true);
            connthread.start();
        }
        catch (IOException ex) {
            problem = ex; 
        }
    }
    
    /**
     * return true if in input stream is ready to be returned by getStream().
     */
    public boolean isStreamReady() { return (strm != null); }

    /**
     * wait for the response stream to be ready.
     * @param millis   maximum time to wait
     * @return boolean  true if the stream is now ready or false if the timeout
     *                    expired before the stream became available.
     * @exception IOException            if an IOException occurs before the 
     *                                      timeout expires.
     * @exception InterruptedException   if the current thread is interrupted
     */
    public boolean waitUntilReady(long millis) 
         throws IOException, InterruptedException 
    {
        connthread.join(millis);
        if (problem != null) {
            IOException prob = new WrappedIOException(problem);
            problem = null;
            throw prob;
        }
        return isStreamReady();
    }

    /**
     * return the stream carrying the query response
     * @exception IOException   if an error occurs while opening a stream.
     * @exception HTTPServerException  if the server returns an error code.
     */
    public InputStream getStream() throws IOException, InterruptedException {

        // block until the stream is ready or we have been interrupted.
        if (connthread.isAlive()) {
            int nap = 100;
            while (connthread.isAlive()) {
                if (connthread.isInterrupted()) 
                    throw new InterruptedException("connection attempt aborted");

                try {
                    Thread.sleep(nap);
                }
                catch (InterruptedException ex) {
                    if (! connthread.isAlive()) return strm;
                    throw ex;
                }

                if (nap < 1000) {
                    nap *= 2;
                    if (nap > 1000) nap = 1000;
                }
            }
        }

        if (problem != null) {
            IOException prob = new WrappedIOException(problem);
            problem = null;
            throw prob;
        }
        if (code > 400) throw new HTTPServerException(code, respMsg);
        return service.getInputStream();
    }

    /**
     * attempt to shutdown the connection.  This would be invoked if 
     * getting the response is taking too long.
     */
    public void shutdown() throws IOException {
        connthread.interrupt();
        service.disconnect();
        if (strm != null) strm.close();
    }

    /**
     * return the HttpURLConnection object.  It will already have been
     * opened.
     */
    public HttpURLConnection getHttpURLConnection() { return service; }

    /**
     * return the HTTP response code
     */
    public int getResponseCode() {  return code; }

    /**
     * return the HTTP response code
     */
    public String getResponseMessage() {  return respMsg;  }

    class ConnThread extends Thread {
        public void run() {
            Thread.yield();
            try {
                service.connect();

                code = service.getResponseCode();
                respMsg = service.getResponseMessage();
                strm = service.getInputStream();
            }
            catch (IOException ex) {
                problem = ex;
            }
        }
    }

}
