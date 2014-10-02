/*
 * SsapServlet.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generic HTTP Servlet for the SSAP protocol (this is an actual working
 * service but it never finds any data).
 *
 * <p>This is a generic HTTP servlet which implements an SSAP service
 * using the service implementation provided by the generic
 * {@link dalserver.SsapService} class. It works, but all it does is echo
 * its input arguments and define and return an empty VOTable containing
 * all predefined SSAP table fields.  To turn this into a real data service,
 * one would 1) subclass {@link dalserver.SsapService} and modify it as
 * necessary to query a real data collection, and 2) subclass this class
 * (SsapServlet) and override the
 * {@link #newSsapService(SsapParamSet) newSsapService}
 * method to call the custom SsapService class provided
 * in step 1).  The modified Servlet class should then be runnable in any
 * compliant Java application server such as Apache Tomcat.
 */
public class SsapServlet extends HttpServlet {
    private static final long serialVersionUID = 1;

    /**
     * The DALserver engine version.
     */
    private String DalServerVersion = "0.3";

    /**
     * The name of the service or servlet instance.  This is used to
     * construct file names for service-specific configuration files.  
     * For example, if we have two different SSA service instances
     * these should have distinct service names.  Defined locally in
     * the servlet deployment descriptor (web.xml).  Optional.
     */
    protected String serviceName;

    /**
     * The default service interface version supported by the service
     * if no version number is explicitly negotiated.  This is usually
     * the highest standard interface version supported by the service.
     * Defined locally in the servlet deployment descriptor (web.xml).
     */
    protected String serviceVersion;

    /**
     * The directory on the local server machine used to store runtime
     * service configuration files, e.g., for the getCapabilities method.
     * Defined locally in the servlet deployment descriptor (web.xml).
     */
    protected String configDir;

    /**
     * A directory on the local server machine used to store precomputed
     * archival datasets to be returned by the built-in getData method.
     * Most services will require a more sophisticated means to return
     * data, but this technique is provided to provide a simple built-in
     * getData capability for testing.  Defined locally in the servlet
     * deployment descriptor (web.xml).  Optional.
     */
    protected String dataDir;

    /**
     * The content (MIME) type to be used for any datasets retrieved from
     * the dataDir.  If set to DYNAMIC, the service will attempt to
     * dynamically determine the content type of each file.
     */
    protected String contentType;


    // Private data.
    // ----------------
    private final int BUFSIZE = 8192;


    // ---------- Servlet Methods -------------------

    /** Servlet startup and initialization. */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
        // Add any servlet initialization here.
    }

    /** Servlet shutdown. */
    public void destroy() {
        // Add any servlet shutdown here.
    }

    /** Return a brief description of the service.  */
    public String getServletInfo() {
        return ("Implements the Simple Spectral Access protocol" +
	    " version=" + this.serviceVersion);
    }

    /**
     * Handle a GET or POST request.  Includes all operations for the
     * given service.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

	// Internal data.
	ServletContext context = getServletContext();
	ServletConfig config = getServletConfig();
	String operation = null;
	boolean error = false;

        HttpSession session = request.getSession(true);
        ResourceBundle messages =
            (ResourceBundle) session.getAttribute("messages");

	// Construct the service parameter set.  This is a single ParamSet
	// containing all context, config, and request parameters.  Any
	// locally defined context/config parameters are automatically
	// passed through.

	RequestParams reqHandler = null;
	SsapParamSet params = null;

	try {
	    Enumeration contextPars = context.getInitParameterNames();
	    Enumeration configPars = config.getInitParameterNames();
	    params = new SsapParamSet();

	    // Get the servlet context parameters.
	    while (contextPars.hasMoreElements()) {
		String name = (String) contextPars.nextElement();
		String value = (String) context.getInitParameter(name);

		Param p = params.getParam(name);
		if (p == null) {
		    params.addParam(p = new Param(name, value));
		    p.setLevel(ParamLevel.SERVICE);
		} else
		    p.setValue(value);
	    }

	    // Get the servlet config parameters.  If already defined,
	    // these values will override any context parameter values.

	    while (configPars.hasMoreElements()) {
		String name = (String) configPars.nextElement();
		String value = (String) config.getInitParameter(name);

		Param p = params.getParam(name);
		if (p == null) {
		    params.addParam(p = new Param(name, value));
		    p.setLevel(ParamLevel.SERVICE);
		} else
		    p.setValue(value);
	    }

	    // Set service config/context parameter defaults.
	    String pname = null;
	    serviceName = params.getValue(pname = "serviceName");
	    if (serviceName == null)
		params.setValue(pname, serviceName = "ssap");

	    serviceVersion = params.getValue(pname = "serviceVersion");
	    if (serviceVersion == null)
		params.setValue(pname, serviceVersion = "1.0");

	    configDir = params.getValue(pname = "configDir");
	    if (configDir == null)
		params.setValue(pname, configDir = "/tmp");

	    dataDir = params.getValue(pname = "dataDir");
	    if (dataDir == null)
		params.setValue(pname, dataDir = "/tmp");

	    contentType = params.getValue(pname = "contentType");
	    if (contentType == null)
		params.setValue(pname, contentType = "text/xml;x-votable");

	    // Identify the service elements and versions.
	    Param p = new Param("ServiceEngine", serviceName +
		": SSAP version " + serviceVersion +
		" DALServer version " + DalServerVersion);
	    p.setLevel(ParamLevel.EXTENSION);
	    params.addParam(p);

	    // Get the request parameters.
	    reqHandler = new RequestParams();
	    reqHandler.getRequestParams(request, params);

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	} catch (InvalidDateException ex) {
	    error = this.errorResponse(params, response, ex);

	} finally {
	    reqHandler = null;
	    if (error) {
		params = null;
		return;
	    }
	}

	// Handle VERSION and REQUEST.
	try {
	    // Verify the service version matches, if specified.
	    Param p = params.getParam("VERSION");
	    String clientVersion = p.stringValue();
	    if (clientVersion != null)
		if (!clientVersion.equalsIgnoreCase(serviceVersion))
		    throw new DalServerException( "protocol version mismatch");

	    // Get the service operation to be performed.
	    operation = params.getParam("REQUEST").stringValue();
	    if (operation == null)
		throw new DalServerException("no operation specified");

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	} finally {
	    if (error) {
		params = null;
		return;
	    }
	}

	/*
	 * ------------------------------------------------------------
	 * Get the service implementation.  Override the newSsapService
	 * method below to implement a new data service.  The rest of
	 * this code should not normally have to be modified.
	 * ------------------------------------------------------------
	 */
        SsapService service = newSsapService(params);


	// -------- QUERYDATA operation. --------

        if (operation.equalsIgnoreCase("queryData")) {
	    RequestResponse requestResponse = null;
	    ServletOutputStream out = null;

	    try {
		// Execute the queryData operation.
		requestResponse = new RequestResponse();
		service.queryData(params, requestResponse);

		// Set up the output stream.
		response.setContentType("text/xml;x-votable");
		response.setBufferSize(BUFSIZE);
		out = response.getOutputStream();

		// Write the output VOTable.
		requestResponse.writeVOTable((OutputStream)out);

	    } catch (DalServerException ex) {
		error = this.errorResponse(params, response, ex);

	    } finally {
		if (out != null) out.close();
		requestResponse = null;
	    }


	// -------- GETDATA operation. --------

        } else if (operation.equalsIgnoreCase("getData")) {
	    RequestResponse requestResponse = new RequestResponse();
	    InputStream inStream = null;
	    String contentType = null;
	    String contentLength = null;

	    try {
		// Call the service's getData method.
		inStream = service.getData(params, requestResponse);

		// Get the dataset content type.
		contentType = params.getValue("datasetContentType");
		if (contentType == null)
		    contentType = "text";
		contentType = contentType.toLowerCase();

		// Get the dataset content length (null if unknown).
		contentLength = params.getValue("datasetContentLength");


	    } catch (DalServerException ex) {
		if (this.errorResponse(params, response, ex))
		    return;
	    }

	    // Set up the output stream and return the dataset.
	    // For dynamically generated or streaming data, the content
	    // length is unknown and should be omitted.

	    response.setBufferSize(BUFSIZE);
	    response.setContentType(contentType);
	    if (contentLength != null)
		response.setContentLength(
		    new Integer(contentLength).intValue());

	    if (contentType.contains("csv")) {
		// Output a CSV version of the request response model.
		ServletOutputStream out = null;

		try {
		    // Set up the output stream.
		    out = response.getOutputStream();

		    // Write the output VOTable.
		    requestResponse.writeCsv((OutputStream)out);

		} catch (DalServerException ex) {
		    error = this.errorResponse(params, response, ex);

		} finally {
		    if (out != null) out.close();
		    requestResponse = null;
		}

	    } else if (contentType.startsWith("text/plain")) {
		// Output a Text version of the request response model.
		ServletOutputStream out = null;

		try {
		    // Set up the output stream.
		    out = response.getOutputStream();

		    // Write the output VOTable.
		    requestResponse.writeText((OutputStream)out);

		} catch (DalServerException ex) {
		    error = this.errorResponse(params, response, ex);

		} finally {
		    if (out != null) out.close();
		    requestResponse = null;
		}

	    } else if (contentType.contains("votable")) {
		// Output a VOTable version of the request response model.
		ServletOutputStream out = null;

		try {
		    // Set up the output stream.
		    out = response.getOutputStream();

		    // Write the output VOTable.
		    requestResponse.writeVOTable((OutputStream)out);

		} catch (DalServerException ex) {
		    error = this.errorResponse(params, response, ex);

		} finally {
		    if (out != null) out.close();
		    requestResponse = null;
		}

	    } else if (inStream != null && contentType.startsWith("text")) {
		// Write a text-formatted data stream (e.g., text/xml).

		BufferedReader in = new BufferedReader(
		    new InputStreamReader(inStream));
		PrintWriter out = response.getWriter();

		for (String line;  (line = in.readLine()) != null;  )
		    out.println(line);

		out.close(); in.close();
		inStream.close();

	    } else if (inStream != null) {
		// Write a binary-formatted data stream.

		ServletOutputStream out = response.getOutputStream();
		byte[] b = new byte[BUFSIZE];
		int count;

		while ((count = inStream.read(b, 0, BUFSIZE)) > 0)
		    out.write(b, 0, count);

		out.close();
		inStream.close();
	    }


	// -------- GETCAPABILITIES operation. --------

        } else if (operation.equalsIgnoreCase("getCapabilities")) {
	    InputStream inStream = service.getCapabilities(params);
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(inStream));

	    // Set up the output stream.
	    response.setContentType("text/xml");
	    response.setBufferSize(BUFSIZE);
	    PrintWriter out = response.getWriter();

	    // Return the document to the client.
	    for (String line;  (line = in.readLine()) != null;  )
		out.println(line);

	    out.close(); in.close();
	    inStream.close();


        } else {
	    DalServerException ex = new DalServerException(
		"unrecognized operation " + "["+operation+"]");
	    error = this.errorResponse(params, response, ex);
	}
    }


    // ---------- Generic Service Implementation -------------------

    /**
     * Get a new SsapService instance.  By default the generic dataless
     * {@link dalserver.SsapService} class is used.  To build a real data
     * service, subclass SsapServlet and replace the newSsapService method
     * with one which calls a custom replacement for the builtin generic
     * SsapService class.
     *
     * <p>This version includes the dataDir and dataType parameters, used to
     *implement a simple mechanism (see {@link dalserver.SsapService#getData}
     *for returning static precomputed archival files from local storage on
     *the server.
     *
     * @param	params	The input and service parameters.
     */
    public SsapService newSsapService(SsapParamSet params) {
	return (new SsapService(params));
    }


    // ---------- Private Methods -------------------

    /**
     * Handle an exception, returning an error response to the client.
     * This version return a VOTable.  If any further errors occur while
     * returning the error response, a servlet-level error is returned
     * instead.
     *
     * @param	params		The input service parameter set.
     *
     * @param	response	Servlet response channel.  This will be
     *				reset to ensure that the output stream is
     *				correctly setup for the error response.
     *
     * @param	ex		The exception which triggered the error
     *				response.
     */
    @SuppressWarnings("unchecked")
    private boolean errorResponse(SsapParamSet params,
	HttpServletResponse response, Exception ex)
	throws ServletException {

	boolean error = true;
	ServletOutputStream out = null;
	RequestResponse r = null;
	TableInfo info = null;

	try {
	    // Set up a response object with QUERY_STATUS=ERROR. */
	    r = new RequestResponse();
	    r.setType("results");

	    String id, key = "QUERY_STATUS";
	    info = new TableInfo(key, "ERROR");
	    if (ex.getMessage() != null)
		info.setContent(ex.getMessage());
	    r.addInfo(key, info);
	    r.echoParamInfos(params);

	    // Set up the output stream.
	    response.resetBuffer();
	    response.setContentType("text/xml;x-votable");
	    response.setBufferSize(BUFSIZE);
	    out = response.getOutputStream();

	    // Write the output VOTable.
	    r.writeVOTable((OutputStream)out);

	} catch (Exception ex1) {
	    throw new ServletException(ex1);

	} finally {
	    if (out != null)
		try {
		    out.close();
		} catch (IOException ex2) {
		    throw new ServletException(ex2);
		}
	    if (r != null)
		r = null;
	    if (info != null)
		info = null;
	}

	return (error);
    }
}
