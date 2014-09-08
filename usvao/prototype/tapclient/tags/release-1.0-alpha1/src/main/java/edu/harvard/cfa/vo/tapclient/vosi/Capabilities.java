package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import edu.harvard.cfa.vo.tapclient.util.HttpClient;
import edu.harvard.cfa.vo.tapclient.util.HttpException;
import edu.harvard.cfa.vo.tapclient.util.ResponseFormatException;

/**
 * A collection of general capabilities of a service.  Capabilities, according to the VOSI Recommendation, provides a list of Capability objects which 
 * <ul>
 * <li>state that the service provides a particular, IVOA-standard function;</li>
 * <li>list the interfaces for invoking that function;</li>
 * <li>record any details of the implementation of the function that are not defined as default or constant in the standard for that function.</li>
 * </ul>
 */
public class Capabilities {
    private static final Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.vosi.Capabilities");

    private String fullURL;
    private List<Capability> capabilityList;

    /**
     * Constructs a Capabilities object for the given service.
     * @param fullURL the full URL associated with this Capabilities object.
     */
    public Capabilities(String fullURL) {
	this.fullURL = fullURL;
	this.capabilityList = new ArrayList<Capability>();
    }

    /**
     * Returns the <code>Capability</code> list of the service.  The list returned is current as of the last call to update.
     * @return a list of Capability objects.
     * @see #update
     * @see Capability
     */
     public List<Capability> getCapabilities() {
	 return Collections.unmodifiableList(capabilityList);
    }

    /**
     * Updates this Capabilities object with the latest response from the service.  A request is made to the service associated with this Capabilities object.
     * @throws HttpException if the service responses to the VOSI Capabilities request with an unexpected HTTP status.
     * @throws ResponseFormatException if an error occurs parsing the service response into an VOSI Capabilities document.
     * @throws IOException if an error occurs creating an input stream.
     */   
    public void update() throws HttpException, ResponseFormatException, IOException {
	InputStream inputStream = null;
	try {
	    inputStream = HttpClient.get(fullURL);
	    // Capabilities
	    XmlOptions capabilitiesOptions = new XmlOptions();
	    //   Namespace substitution 
	    Map<String,String> capabilitiesNamespaces = new HashMap<String,String>();
	    capabilitiesNamespaces.put("http://www.ivoa.net/xml/VOSICapabilities/v1.0", "");//"http://www.ivoa.net/xml/VOResource/v1.0");
	    capabilitiesOptions.setLoadSubstituteNamespaces(capabilitiesNamespaces);
	    //   Document element replacement
	    QName capabilitiesDocumentElement = new QName("http://www.ivoa.net/xml/VOSICapabilities/v1.0", "capabilities", "vosi");
	    capabilitiesOptions.setLoadReplaceDocumentElement(capabilitiesDocumentElement);
	    net.ivoa.xml.vosiCapabilities.v10.CapabilitiesDocument xdocument = net.ivoa.xml.vosiCapabilities.v10.CapabilitiesDocument.Factory.parse(inputStream, capabilitiesOptions);
	    java.util.ArrayList<org.apache.xmlbeans.XmlError> errors = new java.util.ArrayList<org.apache.xmlbeans.XmlError>();
	    xdocument.validate(capabilitiesOptions.setErrorListener(errors));
	    for (org.apache.xmlbeans.XmlError error: errors) {
		if (logger.isLoggable(Level.FINER)) {
		    logger.log(Level.FINER, error.toString());
		}
	    }

	    net.ivoa.xml.vosiCapabilities.v10.CapabilitiesDocument.Capabilities xcapabilities = xdocument.getCapabilities();
	    capabilityList.clear();
	    if (xcapabilities != null) {
		List<net.ivoa.xml.voResource.v10.Capability> xcapabilityList = xcapabilities.getCapabilityList();
		for (net.ivoa.xml.voResource.v10.Capability xcapability: xcapabilityList) {
		    if (xcapability instanceof net.ivoa.xml.tap.v10.TableAccess) {
			System.out.println("Yep!");
			capabilityList.add(new TableAccess((net.ivoa.xml.tap.v10.TableAccess) xcapability));
		    } else {
			System.out.println("Nope...");
			capabilityList.add(new Capability(xcapability));
		    }
		}
	    }
	} catch (XmlException ex) {
	    throw new ResponseFormatException("error parsing VOSI Capabilities response: "+ex.getMessage(), ex);
	} catch (HttpException ex) {
	    throw new HttpException("error getting VOSI Capabilities response: "+ex.getMessage(), ex);
	} catch (IOException ex) {
	    throw new IOException("error reading VOSI Capabilities response: "+ex.getMessage(), ex);
	}
    }

    public void list(PrintStream output) { 
	list(output, "");
    }

    public void list(PrintStream output, String indent) { 
	List<Capability> capabilityList = getCapabilities();

	if (capabilityList != null)
	    for (Capability capability: capabilityList) {
		output.println(indent+"Capability: ");
		capability.list(output, indent+"  ");
	    }
    }
}
