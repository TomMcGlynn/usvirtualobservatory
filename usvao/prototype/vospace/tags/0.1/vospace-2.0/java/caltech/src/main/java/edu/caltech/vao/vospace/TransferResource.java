
package edu.caltech.vao.vospace;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import uws.UWSException;
import uws.job.JobList;
import uws.service.BasicUWS;
import uws.service.UWSUrl;

@Path("transfers")
public class TransferResource extends VOSpaceResource {

    private BasicUWS<TransferJob> uws = null;

    public TransferResource() throws VOSpaceException {
	super();
    }

    /**
     * Retrieve a UWS to use
     *
     * @param req the HTTP Request
     * @return the BasicUWS associated with the request
     */
    private BasicUWS<TransferJob> getUWS(HttpServletRequest req) throws UWSException {
	
	    // Get the current session (or create a new one):
	    HttpSession session = req.getSession(true);
	
	    /* 
	     * If it is a new session, set its maximum inactive time interval ;
	     *  so that all managed jobs lists can be removed and so all jobs stopped
	     * (if running) and their resources freed once the given time interval elapsed
	     */
	    if (session.isNew())
		session.setMaxInactiveInterval(3600);
			
	    // Fetch the UWS from the current session:
	    BasicUWS<TransferJob> uws = (BasicUWS<TransferJob>)session.getAttribute("BasicUWS");

	    // Initialize our UWS:
	    if (uws == null){

		// Get the base UWS URL:
		String baseUWSUrl = new UWSUrl(req).getBaseURI();
				
		// Create the Universal Worker Service:
		uws = new BasicUWS<TransferJob>(TransferJob.class, baseUWSUrl, false);
		uws.setDescription("This UWS aims to manage one (or more) JobList(s) of Transfers." + "Transfer is a kind of Job dealing with a data transfer within a VOSpace");

		// Create the job list
		uws.addJobList(new JobList<TransferJob>("transfers"));

		// Add this UWS to the current session:
		session.setAttribute("BasicUWS", uws);
	    }
	    return uws;
	
    }

    private void executeRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	try {
	    uws = getUWS(req);
	    boolean done = uws.executeRequest(req, resp);
	} catch (UWSException e) {
	    // Display properly the caught UWSException:
	    resp.sendError(e.getHttpErrorCode(), e.getMessage());		
	}	
    }


    /**
     * This method retrieves the specified transfer.
     * 
     * @return the transfer JAXB object
     */
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public void getTransfer(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws IOException {
	executeRequest(req, resp);
    }

    /**
     * This method retrieves the specified transfer.
     * 
     * @param jobid The identifier for the transfer job to return.
     * @return the transfer JAXB object
     */
    @Path("{jobid}")
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public void getTransfer(@Context HttpServletRequest req, @Context HttpServletResponse resp, @PathParam("jobid") String id) throws IOException {
	executeRequest(req, resp);
    }

    /**
     * This method retrieves the specified transfer.
     * 
     * @param jobid The identifier for the transfer job to return.
     * @return the transfer JAXB object
     */
    @Path("{jobid}/results/details")
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public void getResultsDetails(@Context HttpServletRequest req, @Context HttpServletResponse resp, @PathParam("jobid") String id) throws IOException {
	executeRequest(req, resp);
    }

    /**
     * This method launches a transfer job.
     *
     * @param transfer the transfer object to launch
     * @return a Response instance indicating that the transfer job was created
     */
    @POST
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public void postTransfer(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws IOException {
	executeRequest(req, resp);
    }

    /**
     * This method launches a transfer job.
     *
     * @param transfer the transfer object to launch
     * @return a Response instance indicating that the transfer job was created
     */
    @Path("{jobid}/phase")
    @POST
    public void postTransfer(@Context HttpServletRequest req, @Context HttpServletResponse resp, @PathParam("jobid") String id) throws IOException {
	executeRequest(req, resp);
    }
}
