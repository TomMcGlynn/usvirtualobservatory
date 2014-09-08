/**
 * HttpGetProtocolHandler.java
 * Author: Matthew Graham (Caltech)
 * Version: Original (0.1) - 31 July 2006
 */

package edu.caltech.vao.vospace.protocol;

import edu.caltech.vao.vospace.storage.StorageManager;
import edu.caltech.vao.vospace.storage.StorageManagerFactory;
import edu.caltech.vao.vospace.xml.IdHelpers;
import edu.jhu.pha.vospace.jobs.HttpConnectionPoolProvider;
import edu.jhu.pha.vospace.jobs.JobException;
import edu.jhu.pha.vospace.jobs.JobsProcessor;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.oauth.UserHelper;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

import java.io.InputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * This class handles the implementation details for the HTTP 1.1 GET protocol
 */
public class HttpGetProtocolHandler implements ProtocolHandler {

	private HttpConnectionPoolProvider connProvider;
	private static final Logger logger = Logger.getLogger(HttpGetProtocolHandler.class);
	
	public HttpGetProtocolHandler(HttpConnectionPoolProvider connProvider) {
		this.connProvider = connProvider;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.protocol.ProtocolHandler#getUri()
	 */
	@Override
	public String getUri() {
    	return SettingsServlet.getConfig().getString("transfers.protocol.httpget");
    }

    /*
     * (non-Javadoc)
     * @see edu.caltech.vao.vospace.protocol.ProtocolHandler#invoke(edu.jhu.pha.vospace.rest.JobDescription)
     */
	@Override
    public void invoke(JobDescription job) throws IOException, JobException{
		String getFileUrl = job.getProtocols().get(SettingsServlet.getConfig().getString("transfers.protocol.httpget"));
		
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(job.getUsername()));

		HttpClient client = new DefaultHttpClient(connProvider.getClientConnManager());
		
		HttpGet get = new HttpGet(getFileUrl);

		InputStream fileInp = null;
		
		try {
			HttpResponse response = client.execute(get);
			
			if(response.getStatusLine().getStatusCode() == 200) {
				fileInp = response.getEntity().getContent();
				backend.putBytes(job.getTarget(), fileInp);
			} else {
				logger.error("Error processing job "+job.getId()+": "+response.getStatusLine().getStatusCode()+" "+response.getStatusLine().getReasonPhrase());
				throw new JobException("Error processing job "+job.getId()+": "+response.getStatusLine().getStatusCode()+" "+response.getStatusLine().getReasonPhrase());
			}
		} catch(IOException ex) {
			ex.printStackTrace();
			get.abort();
			throw ex;
		} finally {
			try {
				if(null != fileInp) fileInp.close();
			} catch (IOException e) {}
		}
    }

}
