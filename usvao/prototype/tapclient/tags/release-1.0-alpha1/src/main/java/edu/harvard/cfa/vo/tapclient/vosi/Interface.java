package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A description of how to call the service to access this capability.
 * @see Capability
 */
public class Interface {
    private List<AccessURL> accessURLList;
    private List<String> securityMethodList;
    private String version;
    private String role;

    /**
     * Constructor for Interface object.  For use by subclass.
     */
    protected Interface() {
	this.accessURLList = new ArrayList<AccessURL>();
	this.securityMethodList = new ArrayList<String>();
	this.version = null;
	this.role = null;
    }

    // Constructor for Interface object for underlying binding.
    Interface(net.ivoa.xml.voResource.v10.Interface xinterface) {
	List<net.ivoa.xml.voResource.v10.AccessURL> xaccessURLList = xinterface.getAccessURLList();
	if (xaccessURLList != null) {
	    accessURLList = new ArrayList<AccessURL>(xaccessURLList.size());
	    for (net.ivoa.xml.voResource.v10.AccessURL xaccessURL: xaccessURLList) {
		accessURLList.add(new AccessURL(xaccessURL));
	    }
	} else {
	    accessURLList = new ArrayList<AccessURL>();
	}

	List<net.ivoa.xml.voResource.v10.SecurityMethod> xsecurityMethodList = xinterface.getSecurityMethodList();
	if (xsecurityMethodList != null) {
	    securityMethodList = new ArrayList<String>(xsecurityMethodList.size());
	    for (net.ivoa.xml.voResource.v10.SecurityMethod xsecurityMethod: xsecurityMethodList) {
		securityMethodList.add(xsecurityMethod.getStandardID());
	    }
	} else {
	    securityMethodList = new ArrayList<String>();
	}

	this.version = xinterface.getVersion();
	this.role = xinterface.getRole();
    }

    /**
     * Returns a list of AccessURL objects that a client uses to access the 
     * service.
     *
     * @return a list of AccessURL objects.
     */
    public List<AccessURL> getAccessURLs() { 
	return accessURLList; 
    }

    /**
     * Returns a list of Strings containing unique identifiers which specify
     * the mechanism a client must employ to gain secure access to the 
     * service.
     *
     * @return a list of standard identifiers for security methods or null if not specified by the service.
     */
    public List<String> getSecurityMethodStandardIds() { 
	return securityMethodList; 
    }

    /**
     * Returns the version of a VO standard interface specification that this
     * interface complies with.
     *
     * @return the version of a standard interface specification that this interface complies with or null if not specified by the service.
     */
    public String getVersion() { 
	return version; 
    }

    /**
     * Returns a String identifying the role the interface plays in a
     * capability.  A value of "std" may indicate
     * that this interface refers to a standard interface defined by the 
     * standard referred to by the capability's standardID attribute.
     *
     * @return a tag name the identifies the role the interface plays in the 
     * particular capability or null if not specified by the service
     */
    public String getRole() { 
	return role; 
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String role = getRole();
	List<AccessURL> accessURLs = getAccessURLs();
	List<String> securityMethodStandardIds = getSecurityMethodStandardIds();
	String version = getVersion();

	if (role != null)
	    output.println(indent+"Role: "+role);

	if (version != null) 
	    output.println(indent+"Version: "+version);
 
	if (accessURLs != null) 
	    for (AccessURL accessURL: accessURLs) {
		output.println(indent+"Access URL: ");
		accessURL.list(output, indent+"  ");
	    }
	if (securityMethodStandardIds != null) 
	    for (String s: securityMethodStandardIds) {
		output.println(indent+"SecurityMethod: ");
		output.println(indent+"Standard ID: "+s);
	    }
    }
}
