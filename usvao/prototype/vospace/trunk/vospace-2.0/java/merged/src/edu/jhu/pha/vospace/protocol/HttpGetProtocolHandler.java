package edu.jhu.pha.vospace.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.jobs.JobException;
import edu.jhu.pha.vospace.jobs.MyHttpConnectionPoolProvider;
import edu.jhu.pha.vospace.node.DataNode;
import edu.jhu.pha.vospace.node.NodeFactory;
import edu.jhu.pha.vospace.rest.JobDescription;
import edu.jhu.pha.vospace.storage.StorageManager;
import edu.jhu.pha.vospace.storage.StorageManagerFactory;

/**
 * This class handles the implementation details for the HTTP 1.1 GET protocol
 */
public class HttpGetProtocolHandler implements ProtocolHandler {

	private static final Logger logger = Logger.getLogger(HttpGetProtocolHandler.class);
	
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
    public void invoke(JobDescription job) throws IOException, JobException, URISyntaxException{
		String getFileUrl = job.getProtocols().get(SettingsServlet.getConfig().getString("transfers.protocol.httpget"));
		
		StorageManager backend = StorageManagerFactory.getInstance().getStorageManager(job.getUsername());

		HttpClient client = MyHttpConnectionPoolProvider.getHttpClient();
		
		HttpGet get = new HttpGet(getFileUrl);

		InputStream fileInp = null;
		
		try {
			HttpResponse response = client.execute(get);
			
			if(response.getStatusLine().getStatusCode() == 200) {
				fileInp = response.getEntity().getContent();
				
				//TODO make also for container
				DataNode targetNode = (DataNode)NodeFactory.getInstance().getNode(job.getTargetId(), job.getUsername());
				targetNode.setData(fileInp);
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
