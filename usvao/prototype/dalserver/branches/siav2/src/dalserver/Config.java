/*
 * Config.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * The Config class manages the DALServer Web-app local configuration,
 * including global framework configuration, and configuration of all
 * contained servlets.
 *
 * The DALServer Web-app is packaged as a single WAR file containing
 * generic framework code for all supported VO services.  A configured
 * instance of the Web-app may support any number of servlets.  Each
 * data service instance is a separate configured servlet with its
 * own distinct HTTP endpoint.  The DALServer Web-app is fully data-driven;
 * all configuration data and all served data content is stored externally.
 *
 * The Config class allows all server and servlet configuration to be
 * expressed concisely in simple text files, describing the configuration
 * of the framework at a particular site.  All configuration data is
 * stored separately from the DALServer Web-app, allowing a single framework
 * WAR file to be prepared and used unchanged regardless of the local 
 * configuration.  The DALServer WAR can be updated with no effect on the
 * local configuration.
 *
 * When the configuration is reloaded, the Config class reads the
 * configuration files stored in an external directory, and updates the
 * web.xml file used internally to define the Web-app runtime configuration.
 * All configuration information is cached in the computer generated web.xml
 * where it remains unchanged until the configuration is subsequently
 * reloaded.  The contents of web.xml are cached in memory in the running
 * Web-app hence no config file access is required other than when the
 * configuration is reloaded and processed to generate a new web.xml.
 *
 * Various techniques are possible to trigger a configuration reload.
 * The simplest and most direct is to POST a reload request to the running
 * DALServer Web-app.  Watching the server configuration directory for
 * any file mods or additions is also possible.
 *
 * @version	1.0, 8-Apr-2014
 * @author	Doug Tody
 */
public class Config extends HttpServlet {

    // Some built-in defaults; normally set in external configuration.
    private String configDir = "/opt/services/dalserver";
    private String dalServerConfig = "server.conf";

    // Private data.
    private final int BUFSIZE = 8192;
    private final int MAXLINE = 80;


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
        return ("DALServer framework auto-configuration servlet");
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
	ParamSet params = null;

	try {
	    Enumeration contextPars = context.getInitParameterNames();
	    Enumeration configPars = config.getInitParameterNames();
	    params = new ParamSet();

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

	    // Get the request parameters.
	    String[] omit = { "configKey" };
	    reqHandler = new RequestParams();
	    reqHandler.getRequestParams(request, params, omit);

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

	// Execute the service framework configuration reload.
	// This overwrites the Web-app web.xml file, which is automatically
	// reloaded by the servlet container (if enabled).

	ParamSet servletList = new ParamSet();
	int nservlets = 0;

	try {
	    nservlets = reload(params, response, servletList);
	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	}

	// Set up the output stream.
	response.setBufferSize(BUFSIZE);
	response.setContentType("text/plain");
	PrintWriter out = response.getWriter();

	// Briefly summarize the servlets created.
	for (Iterator ii = servletList.iterator();  ii.hasNext();  ) {
	    Map.Entry me = (Map.Entry) ii.next();
	    Param p = (Param) me.getValue();
	    String pname = p.getName();

	    if (pname.equals("WebApp")) {
		out.println(pname + ": " + p.stringValue());
		out.println();
	    } else {
		String line = pname + "\t";
		if (pname.length() < 8)
		    line += "\t";
		line += p.stringValue();
		out.println(line);
	    }
	}

	out.println();
	out.println("Successfully created " + nservlets + " servlets");
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(); out.println(dateFormat.format(date));
	out.flush();
    }


    /**
     * Reload the current framework configuration.
     *
     * @param	params		Parameter set for HTTP reload request
     * @param	response	Response context
     * @param	servletList	Receives list of created servlets
     *
     * The root input configuration file is read, along with any files
     * it references, and a Web-app (web.xml format) file is generated
     * and written to the output stream.
     */
    public int reload (ParamSet params, HttpServletResponse response,
	ParamSet servletList) throws DalServerException, ServletException {

	String configDir = "/opt/services/dalserver";
	String configFile = "server.conf";
	String configKey = null;
	String webAppConfig = null;
	int nservlets = 0;
	boolean error;
	Param p;

	// Apply password protection if enabled.
	p = params.getParam("configKey");
	if (p != null && !p.stringValue().equals("none")) {
	    configKey = p.stringValue();

	    // If so, check for a valid password (param "key").
	    p = params.getParam("key");
	    if (p == null || !p.stringValue().equals(configKey))
		throw new DalServerException("missing or invalid password");
	}

	// Get the input configuration file location.
	p = params.getParam("configDir");
	if (p != null)
	    configDir = p.stringValue();
	p = params.getParam("configFile");
	if (p != null)
	    configFile = p.stringValue();

	// Get the file path for the output web.xml file.
	p = params.getParam("webAppConfig");
	if (p != null)
	    webAppConfig = p.stringValue();
	else
	    throw new DalServerException("webAppConfig not defined");

	try {
	    String configPath = configDir + "/" + configFile;
	    String tempfile = webAppConfig + ".temp";

	    // Write the new web.xml to a temporary file.
	    PrintWriter out = new PrintWriter(new FileWriter(tempfile));
	    nservlets = compile(out, configPath, servletList);

	    // If this succeeds, install the new web.xml.
	    File file = new File(tempfile);
	    File dest = new File(webAppConfig);
	    file.renameTo(dest);

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	} catch (IOException ex) {
	    error = this.errorResponse(params, response, ex);
	}

	return (nservlets);
    }


    /**
     * Compile the current framework configuration in web.xml format.
     *
     * @param	out		Output stream
     * @param	config		Root input configuration file
     * @param	servletList	Paramset describing the servlets
     *
     * The root input configuration file is read, along with any files
     * it references, and a Web-app (web.xml format) file is generated
     * and written to the output stream.  The number of servlets successfully
     * created is returned as the function value, and a description of
     * each servlet created is written to the servletList pset.
     */
    public int compile (PrintWriter out, String config, ParamSet servletList)
	throws DalServerException {

	ParamSet pset;
	int nservlets = 0;
	LinkedHashMap<String,ParamSet> psetList =
	    new LinkedHashMap<String,ParamSet>();
	String configDir;

	// Extract the configuration directory from the config file path.
	int off = config.lastIndexOf('/');
	if (off >= 0)
	    configDir = config.substring(0, off);
	else
	    configDir = "./";

	// Parse the root server config file.
	try {
	    if (parseIni(config, psetList) <= 0)
		throw new DalServerException("config file not found (" +
		    config + ")");
	} catch (FileNotFoundException ex) {
	    throw new DalServerException("config file not found (" +
		config + ")");
	}

	// Output the lead portion of the web.xml file.
	out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	out.println("<web-app>");
	int indent = 0;

	indent += 4;
	pset = psetList.get("web-app");
	putElement(out, "display-name", pset.getValue("display-name"), indent);
	try {
	    servletList.addParam("WebApp", pset.getValue("display-name"));
	} catch (InvalidDateException ex) {
	    ;
	}

	putElement(out, "description", pset.getValue("description"), indent);

	out.println();
	putText(out, "<!-- Context Initialization Parameters. -->", indent);
	out.println();

	// Output the Web-app context initialization parameters.
	pset = psetList.get("context-params");
	for (Iterator ii = pset.iterator();  ii.hasNext();  ) {
	    Map.Entry me = (Map.Entry) ii.next();
	    Param p = (Param) me.getValue();

	    putElement(out, null, "<context-param>", indent);
	    putElement(out, "param-name", p.getName(), indent + 2);
	    putElement(out, "param-value", p.stringValue(), indent + 2);
	    putElement(out, null, "</context-param>", indent);
	}

	out.println();
	putText(out, "<!-- Servlet Definitions. -->", indent);
	out.println();

	// Output the servlet definition section for each servlet.
	ParamSet servlets = psetList.get("servlets");
	for (Iterator ii = servlets.iterator();  ii.hasNext();  ) {
	    Map.Entry mapen = (Map.Entry) ii.next();
	    Param servlet = (Param) mapen.getValue();
	    boolean hasTableConfig = false;
	    int nparams = 0;

	    // Parse the servlet configuration.
	    String servconf = configDir + "/" + servlet.getName() + ".conf";
	    LinkedHashMap<String,ParamSet> servletPsets =
		new LinkedHashMap<String,ParamSet>();
	   
	    try {
		if (parseIni(servconf, servletPsets) <= 0) {
		    out.flush();
		    throw new DalServerException("config file not found (" +
			servconf + ")");
		}
	    } catch (FileNotFoundException ex) {
		out.flush();
		throw new DalServerException("config file not found (" +
		    servconf + ")");
	    }
	    
	    // Output the servlet definition.
	    putText(out, "<servlet>", indent);
	    indent += 2;

	    // Servlet top level attributes. 
	    pset = servletPsets.get("servlet");
	    Iterator ij;

	    for (ij = pset.iterator();  ij.hasNext();  ) {
		Map.Entry me = (Map.Entry) ij.next();
		Param p = (Param) me.getValue();
		putElement(out, p.getName(), p.stringValue(), indent);
	    }
	    out.println();
	    putText(out, "<!-- Servlet Parameters. -->", indent);
	    out.println();

	    // Servlet init (servlet-internal) parameters. 
	    pset = servletPsets.get("init-params");
	    for (ij = pset.iterator();  ij.hasNext();  ) {
		Map.Entry me = (Map.Entry) ij.next();
		Param p = (Param) me.getValue();
		nparams++;

		putElement(out, null, "<init-param>", indent);
		putElement(out, "param-name", p.getName(), indent + 2);
		putElement(out, "param-value", p.stringValue(), indent + 2);
		putElement(out, null, "</init-param>", indent);
	    }

	    // Servlet table configuration parameters.  These are passed
	    // to the servlet as another block of servlet init parameters.
	    // Not all servlets will have a table configuration section.

	    Param p = pset.getParam("tableConfig");
	    if (p != null && p.isSet()) {
		String tableConfig = p.stringValue();

		out.println();
		putText(out,
		    "<!-- Table Configuration Parameters. -->", indent);
		out.println();

		putTableConfig(out, servlet, tableConfig, configDir, indent);
		hasTableConfig = true;
	    }

	    indent -= 2;
	    putText(out, "</servlet>", indent);

	    String desc = nparams + " parameters";
	    if (hasTableConfig)
		desc += " tableconfig";
	    try {
		servletList.addParam(servlet.getName(), desc);
	    } catch (InvalidDateException ex) {
		;
	    }
	    nservlets++;

	    // Output the servlet to HTTP endpoint mapping element.
	    out.println();
	    putText(out, "<servlet-mapping>", indent);
	    indent += 2;

	    pset = servletPsets.get("servlet-mapping");
	    for (ij = pset.iterator();  ij.hasNext();  ) {
		Map.Entry me = (Map.Entry) ij.next();
		Param map = (Param) me.getValue();

		// Check for a missing leading "/" in the servlet mapping.
		// This would cause the Web-app to fail to reload.

		String pname = map.getName();
		String pvalue = map.stringValue();
		if (pname.equalsIgnoreCase("url-pattern")) {
		    if (!pvalue.startsWith("/")) {
			out.flush();
			throw new DalServerException(
			"malformed url-pattern (" + pvalue + ")");
		    }
		}

		putElement(out, pname, pvalue, indent);
	    }

	    indent -= 2;
	    putText(out, "</servlet-mapping>", indent);
	    out.println();
	}
    
	// Output the closing portion of the web.xml file.
	out.println("</web-app>");
	out.close();

	return (nservlets);
    }


    /**
     * Parse the table configuration file for a servlet (optional) and
     * append the content to the servlet definition as a block of init
     * parameters.
     *
     * @param	out		Output text stream
     * @param	servlet		Servlet ID parameter
     * @param	tableConfig	Table configuration file
     * @param	configDir	Configuration directory
     * @param	indent		Base level indent for output
     *
     * Table configuration parameters are named "table.standard.<param>"
     * or "table.custom.<param>" in the servlet init parameter block.
     * This allows the table configuration to be cached in memory when
     * the Web-app is loaded, and passed in via the existing servlet
     * init parameter mechanism.
     */
    private void putTableConfig (PrintWriter out, Param servlet,
	String tableConfig, String configDir, int indent)
	throws DalServerException {

	// Parse the table configuration.
	String config = configDir + "/" + servlet.getName() + ".tab";
	LinkedHashMap<String,ParamSet> tablePsets =
	    new LinkedHashMap<String,ParamSet>();

	try {
	    if (parseIni(config, tablePsets) <= 0) {
		out.flush();
		throw new DalServerException("config file not found (" +
		    config + ")");
	    }
	} catch (FileNotFoundException ex) {
	    out.flush();
	    throw new DalServerException("config file not found (" +
		config + ")");
	}
	
	// Table top level attributes. 
	ParamSet pset = tablePsets.get("table");
	String section, pname;
	Iterator ii;
	Param p;

	for (ii = pset.iterator();  ii.hasNext();  ) {
	    Map.Entry me = (Map.Entry) ii.next();
	    p = (Param) me.getValue();
	    putElement(out, p.getName(), p.stringValue(), indent);
	}

	// Standard table parameters, if any. 
	section = "standard";
	pset = tablePsets.get(section);

	for (ii = pset.iterator();  ii.hasNext();  ) {
	    Map.Entry me = (Map.Entry) ii.next();
	    p = (Param) me.getValue();

	    putElement(out, null, "<init-param>", indent);
	    pname = "table." + section + "." + p.getName();
	    putElement(out, "param-name", pname, indent + 2);
	    putElement(out, "param-value", p.stringValue(), indent + 2);
	    putElement(out, null, "</init-param>", indent);
	}

	// Custom table parameters, if any. 
	section = "custom";
	pset = tablePsets.get(section);

	if (pset != null) {
	    for (ii = pset.iterator();  ii.hasNext();  ) {
		Map.Entry me = (Map.Entry) ii.next();
		p = (Param) me.getValue();

		putElement(out, null, "<init-param>", indent);
		pname = "table." + section + "." + p.getName();
		putElement(out, "param-name", pname, indent + 2);
		putElement(out, "param-value", p.stringValue(), indent + 2);
		putElement(out, null, "</init-param>", indent);
	    }
	}
    }


    /** 
     * Private method to print an XML element.
     *
     * @param	out		Output stream.
     * @param	element		XML Element name.
     * @param	value		Element value text.
     * @param	indent		Number of spaces to indent.
     */
    private void putElement (PrintWriter out,
	String element, String value, int indent) {

	StringBuilder line = new StringBuilder();

	// Indent the line.
	for (int i=0;  i < indent;  i++)
	    line.append(' ');

	// Format the XML element.
	if (element != null)
	    line.append("<" + element + ">");

	if (element != null && value.length() > MAXLINE) {
	    out.println(line.toString());
	    line.setLength(0);
	    for (int i=0;  i < (indent+2);  i++)
		line.append(' ');
	    line.append(value);
	    out.println(line.toString());
	    line.setLength(0);
	    for (int i=0;  i < indent;  i++)
		line.append(' ');
	} else
	    line.append(value);

	if (element != null)
	    line.append("</" + element + ">");

	// Output the line.
	out.println(line.toString());
    }

    /**
     * Output some text with the current indent.
     */
    private void putText(PrintWriter out, String text, int indent) {
	putElement(out, null, text, indent);
    }


    /**
     * Parse a config file in INI Format.
     *
     * @param	config		Input config file in INI Format
     * @param	psetList	List of named psets for output
     *
     * The input config file is parsed, and the content returned in the form
     * of zero or more ParamSet objects, one ParamSet per INI context, that
     * are added to the provided pset hashmap.  The number of psets output
     * is returned.
     */
    public int parseIni (String config,
	LinkedHashMap<String,ParamSet> psetList)
	throws FileNotFoundException, DalServerException {

	ParamSet pset = null;
	int ncontexts = 0;

	// Open the config file.
	BufferedReader br = new BufferedReader(new FileReader(config));

	try {
	    // Get the entire text file as a String.
	    StringBuilder sb = new StringBuilder();
	    boolean skipWhitespace = false;
	    String line, token;

	    try {
		for (line=br.readLine();  line != null;  line=br.readLine()) {
		    // Join lines if newline is escaped.
		    if (line.endsWith("\\")) {
			if (skipWhitespace) {
			    sb.append(" ");
			    line = line.trim();
			}
			sb.append(line.substring(0, line.length()-1));
			skipWhitespace = true;
		    } else {
			if (skipWhitespace) {
			    sb.append(" ");
			    sb.append(line.trim());
			    sb.append(" | ");
			    skipWhitespace = false;
			} else {
			    sb.append(line);
			    sb.append(" | ");
			}
		    }
		}
	    } catch (IOException ex) {
		;
	    }

	    // Get the final, long line of text.
	    String text = sb.toString();

	    // Access the string as a sequence of tokens.
	    // TODO: Rewrite this to use StreamTokenizer.

	    StringTokenizer in = new StringTokenizer(text);
	    StringTokenizer pushTok=null;
	    StringBuilder pvalue=null;
	    String pname=null;

	    try {
		while (pushTok != null || in.hasMoreTokens()) {
		    // Retrieve a pushed tokenizer.
		    if (!in.hasMoreTokens() && pushTok != null) {
			in = pushTok;
			pushTok = null;
		    }

		    token = in.nextToken(" \t");

		    // Skip a blank line.
		    if (token.equals("|"))
			continue;

		    // Skip a comment line.
		    if (token.startsWith("#")) {
			while (in.hasMoreTokens()) {
			    token = in.nextToken(" \t");
			    if (token.equals("|"))
				break;
			}
			if (in.hasMoreTokens())
			    continue;
			else
			    break;
		    }

		    // Start a new context.
		    if (token.startsWith("[")) {
			String contextName =
			    token.substring(1, token.indexOf(']'));
			pset = new ParamSet();
			psetList.put(contextName, pset);
			ncontexts++;
			continue;
		    }

		    // Add a parameter to the current context.
		    boolean eqseen=false, skipToEol=false;
		    pvalue = new StringBuilder();
		    pname = token;

		    token = in.nextToken(" \t");
		    if (eqseen = token.equals("="))
			token = in.nextToken(" \t");

		    // The param value is either "param = value", all on one
		    // line, or "param =" followed by end of line, in which
		    // case the value is the entire next line or lines, until
		    // a blank line is encountered.  Continuation lines must
		    // begin with a TAB character.
		   
		    if (eqseen && token.equals("|")) {
			// Either a missing or multi-line pvalue.
		
			// Skip forward unless TAB is seen (token=" ").
			token = in.nextToken("|");

			// if (!token.equals(" ")) {
			if (!Character.isWhitespace(token.charAt(0))) {
			    // Handle a null-valued param.
			    try {
				if (pset != null)
				    pset.addParam(pname, "");
				pname = null; pvalue = null;
			    } catch (InvalidDateException ex) {
				;
			    }

			    // Process the line just read in.
			    pushTok = in;
			    in = new StringTokenizer(token);
			    continue;
			} else
			    pvalue.append(token.trim() + " ");

			// Append any following lines to the pvalue.
			while (in.hasMoreTokens()) {
			    token = in.nextToken("|");
			    if (token.equals("  "))
				break;
			    pvalue.append(token.trim() + " ");
			}

		    } else if (token.startsWith("#")) {
			// Param with no value, followed by comment.
			while (in.hasMoreTokens()) {
			    token = in.nextToken(" \t");
			    if (token.equals("|"))
				break;
			}
		    } else if (token.equals("|")) {
			// Param has no value.
			;
		    } else {
			// Param = value.
			if (!skipToEol)
			    pvalue.append(token + " ");

			while (in.hasMoreTokens()) {
			    token = in.nextToken(" \t");
			    if (token.equals("|"))
				break;
			    else if (token.equals("#"))
				skipToEol = true;
			    if (!skipToEol)
				pvalue.append(token + " ");
			}
		    }

		    // Add the parameter to the context pset.
		    try {
			if (pset != null) {
			    String pval = pvalue.toString().trim();
			    pset.addParam(pname, pval);
			}
			pname = null; pvalue = null;
		    } catch (InvalidDateException ex) {
			;
		    }
		}
	    } catch (NoSuchElementException ex) {
		if (pset != null && pname != null && pvalue != null) {
		    try {
			String pval = pvalue.toString().trim();
			pset.addParam(pname, pval);
			pname = null; pvalue = null;
		    } catch (InvalidDateException idex) {
			;
		    }
		}
	    }

	} finally {
	    try {
		br.close();
	    } catch (IOException ex) {
		;
	    }
	}

	return (ncontexts);
    }

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
    private boolean errorResponse(ParamSet params,
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
