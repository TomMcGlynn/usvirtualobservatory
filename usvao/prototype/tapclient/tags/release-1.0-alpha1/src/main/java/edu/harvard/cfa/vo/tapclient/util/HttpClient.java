package edu.harvard.cfa.vo.tapclient.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


/**
 * A utility object which provides methods for RESTful interactions with a service.
 */
public class HttpClient {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.util.HttpClient");

    /**
     * Sends an HTTP GET request to the fully specified url <code>spec</code>.
     * @param spec the url.
     * @return the InputStream containing the server response.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException in case of a problem or if the connection was aborted.
     */
    public static InputStream get(String spec) throws HttpException, IOException {
	Map<String,String> emptyMap = Collections.emptyMap();
	return HttpClient.get(spec, emptyMap);
    }

    /**
     * Sends an HTTP GET request to the fully specified url <code>spec</code>.
     * @param spec the url.
     * @param parameters the query parameters.
     * @return the InputStream containing the server response.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException in case of a problem or if the connection was aborted.
     * @throws NullPointerException if the url is null.
     */
    public static InputStream get(String spec, Map parameters) throws HttpException, IOException {
	InputStream inputStream = null;

	if (spec == null)
	    throw new NullPointerException("url is null");
        
	DefaultHttpClient httpclient = new DefaultHttpClient();
	String query = "";
	if (parameters != null && !parameters.isEmpty()) {
	    List<NameValuePair> qparams = new ArrayList<NameValuePair>();
	    Set entrySet = parameters.entrySet();
	    Iterator iterator = entrySet.iterator();
	    while (iterator.hasNext()) {
		Map.Entry entry = (Map.Entry) iterator.next();
		qparams.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
	    }
	    query = "?"+URLEncodedUtils.format(qparams, "UTF-8");
	}

	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "GET "+spec+query);

	HttpGet httpget = new HttpGet(spec+query);
	httpget.setHeader("Accept", "*/*");

	HttpResponse httpresponse = httpclient.execute(httpget);
	inputStream = handleResponse(httpresponse);

        return inputStream;
    }

    /**
     * Sends an HTTP POST request to the fully specified url <code>spec</code>.
     * @param spec the url.
     * @return the InputStream containing the server response.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException in case of a problem or if the connection was aborted.
     */
    public static InputStream post(String spec) throws HttpException, IOException {
	Map<String,String> emptyMap = Collections.emptyMap();
	return HttpClient.post(spec, emptyMap);
    }

    /**
     * Sends an HTTP POST request to the fully specified url <code>spec</code>.
     * @param spec the url.
     * @param parameters the query parameters.
     * @return the InputStream containing the server response.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException in case of a problem or if the connection was aborted.
     */
    public static InputStream post(String spec, Map parameters) throws HttpException, IOException {
	InputStream inputStream = null;
	String query = "";
	DefaultHttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost(spec);
	httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
	httppost.setHeader("Accept", "*/*");
	if (parameters != null && !parameters.isEmpty()) {
	    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	    Set entrySet = parameters.entrySet();
	    Iterator iterator = entrySet.iterator();
	    while (iterator.hasNext()) {
		Map.Entry entry = (Map.Entry) iterator.next();
		formparams.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
	    }
	    query = "?"+URLEncodedUtils.format(formparams, "UTF-8");
	    UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
	    
	    httppost.setEntity(formEntity);
	}
	
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "POST "+spec+query);

	inputStream = handleResponse(httpclient.execute(httppost));
	
        return inputStream;
    }

    /**
     * Sends an HTTP POST request to the fully specified url <code>spec</code>.
     * @param spec the url.
     * @param parameters the query parameters.
     * @return the InputStream containing the server response.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException in case of a problem or if the connection was aborted.
     */
    public static InputStream post(String spec, Map<String,String> parameters, Map<String,URI> inlineContent) throws HttpException, IOException {
	InputStream inputStream = null;
	String query = "";
	DefaultHttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost(spec);
	httppost.setHeader("Accept", "*/*");
	if (parameters != null && !parameters.isEmpty()) {
	    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	    MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	    Set entrySet = parameters.entrySet();
	    Iterator iterator = entrySet.iterator();
	    while (iterator.hasNext()) {
		Map.Entry entry = (Map.Entry) iterator.next();
		entity.addPart(entry.getKey().toString(), new StringBody(entry.getValue().toString(), "text/plain", Charset.forName("UTF-8")));
		formparams.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
	    }
	    
	    Set<Map.Entry<String,URI>> inlineEntrySet = inlineContent.entrySet();
	    Iterator<Map.Entry<String,URI>> inlineIterator = inlineEntrySet.iterator();
	    while (inlineIterator.hasNext()) {
		Map.Entry<String,URI> entry = inlineIterator.next();
		URI uri = entry.getValue();

		entity.addPart(entry.getKey(), new InputStreamBody(uri.toURL().openStream(), entry.getKey()));
		formparams.add(new BasicNameValuePair(entry.getKey().toString(), "<inline content>"));

	    }

	    query = "?"+URLEncodedUtils.format(formparams, "UTF-8");

	    httppost.setEntity(entity);
	}

	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "POST  "+spec+query);

	inputStream = handleResponse(httpclient.execute(httppost));
	
        return inputStream;
    }

    /**
     * Sends an HTTP DELETE request to the fully specified url <code>spec</code>.
     * @param spec the url.
     * @return the InputStream containing the server response.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException in case of a problem or if the connection was aborted.
     */
    public static InputStream delete(String spec) throws HttpException, IOException {
	InputStream inputStream = null;
	DefaultHttpClient httpclient = new DefaultHttpClient();
	HttpDelete httpdelete = new HttpDelete(spec);
	
	if (logger.isLoggable(Level.INFO)) 
	    logger.log(Level.INFO, "DELETE "+spec);
	
	inputStream = handleResponse(httpclient.execute(httpdelete));
	
        return inputStream;
    }
    
    /**
     * @return the InputStream containing the server respsonse.
     * @throws HttpException if the HTTP Status Code is not 200(OK).
     * @throws IOException if the error occurs creating the InputStream.
     */
    protected static InputStream handleResponse(HttpResponse response) throws HttpException, IOException {
	if (response != null) {
	    StatusLine statusLine = response.getStatusLine();
	    if (statusLine != null) {
		int statusCode = statusLine.getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {	
		    if (logger.isLoggable(Level.WARNING)) 
			logger.log(Level.WARNING, "error handling response: "+statusLine.getReasonPhrase());
		    
		    String content = null;
		    HttpEntity entity = response.getEntity();
		    if (entity != null) {
			InputStream inputStream = entity.getContent();
			try {
			    content = asString(inputStream);
			} finally {
			    if (inputStream != null) {
				try {
				    inputStream.close();
				} catch (IOException ex) {
				    if (logger.isLoggable(Level.WARNING)) 
					logger.log(Level.WARNING, "error closing http response content");
				    
				}
			    }
			}
		    }
		    
		    throw new HttpException(statusLine.getReasonPhrase(), statusCode, content);
		}
	    }
	    HttpEntity entity = response.getEntity();
	    InputStream inputStream = entity.getContent();
	    return inputStream;
	}

	return null;
    }

    /**
     * Concatenates the entire InputStream into a String.
     * @param inputStream the InputStream to read into a String.
     * @return a String containing the entire InputStream.
     * @throws IOException if an error occurs handling the InputStream.
     */
    public static String asString(InputStream inputStream) throws IOException {
	if (inputStream != null) {
	    InputStreamReader inStreamReader = new InputStreamReader(inputStream);
	    StringBuffer text = new StringBuffer();
	    int buffersize = 2048;
	    char readBuffer[] = new char[buffersize];
	    int dataRead = 0;
	    
	    while ((dataRead = inStreamReader.read(readBuffer, 0, buffersize)) > -1) {        text.append(readBuffer, 0, dataRead);
	    }
	    return text.toString();
	}
	return null;
    }

    /*
    public class Tee extends java.io.FilterInputStream {
	private OutputStream outputStream;
	public Tee(InputStream inputStream, OutputStream outputStream) {
	    super(inputStream);
	    this.outputStream = outputStream;
	}

	public int read() throws IOException {
	    int nread = super.read();
	    if (nread != -1) 
		outputStream.write(nread);
	    return nread;
	}

	public int read(byte[] b) throws IOException {
	    int nread = super.read(b);
	    if (nread != -1)
		outputStream.write(b, 0, nread);
	    return nread;
	}

	public int read(byte[] b, int off, int len) throws IOException {
	    int nread = super.read(b, off, len);
	    if (nread != -1) 
		outputStream.write(b, off, len < nread ? len : nread);
	    return nread;
	}
    }
    */
}