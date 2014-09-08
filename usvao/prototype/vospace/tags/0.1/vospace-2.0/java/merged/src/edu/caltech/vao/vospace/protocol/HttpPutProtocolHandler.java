/**
 * HttpPutProtocolHandler.java
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
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.oauth.UserHelper;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.rest.JobDescription.STATE;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * This class handles the implementation details for the HTTP 1.1 PUT protocol
 */
public class HttpPutProtocolHandler implements ProtocolHandler {

	private HttpConnectionPoolProvider connProvider;
	private static final Logger logger = Logger.getLogger(HttpPutProtocolHandler.class);
	
	public HttpPutProtocolHandler(HttpConnectionPoolProvider connProvider) {
		this.connProvider = connProvider;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.protocol.ProtocolHandler#getUri()
	 */
	@Override
	public String getUri() {
    	return SettingsServlet.getConfig().getString("transfers.protocol.httpput");
	}

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.protocol.ProtocolHandler#invoke(edu.jhu.pha.vospace.rest.JobDescription)
	 */
	@Override
    public void invoke(JobDescription job) throws IOException {
		String putFileUrl = job.getProtocols().get(SettingsServlet.getConfig().getString("transfers.protocol.httpput"));
		
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(UserHelper.getDataStoreCredentials(job.getUsername()));

		HttpClient client = new DefaultHttpClient(connProvider.getClientConnManager());
		InputStream fileInp = backend.getBytes(job.getTarget());

		HttpPut put = new HttpPut(putFileUrl);
        put.setEntity(new InputStreamEntity(fileInp, Long.parseLong(backend.getNodeSize(job.getTarget()))));

        try {
        	HttpResponse response = client.execute(put);
        	response.getEntity().getContent().close();
        } catch(IOException ex) {
			put.abort();
        	ex.printStackTrace();
        	throw ex;
        } finally {
        	try {
				if(null != fileInp) fileInp.close();
			} catch (IOException e) {}
        }
	}
}
