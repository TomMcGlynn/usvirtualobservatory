package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A description of a general capability of the service--its behavioral characteristics and limitations--and how to use it.  The information in this object is only as recent as the call to VosiService#getCapabilities that generated this object.  To see if a service's capabilities have changed, call VosiService#getCapabilities again.  The following example explores the VOSI capabilities response of a service to determine if it has the TAP service capability.
 * <pre>
 * <code>
 *   for (Capability capability: capabilities) {
 *      System.out.println("This capability supports the following Standard: "+capability.getStandardId());
 *   }
 * </code>
 * </pre>
 * @see Capabilities
 */
public class Capability {
     private final static Logger logger = Logger.getLogger("edu.harvard.cfa.vo.tapclient.vosi.Capability");

    private List<Validation> validationLevelList;
    private String description;
    private List<Interface> interfaceList;
    private String standardId;

    /**
     * Constructs a Capability object.
     */
    protected Capability() {
	this.validationLevelList = new ArrayList<Validation>();
	this.description = null;
	this.interfaceList = new ArrayList<Interface>();
	this.standardId = null;
    }
    
    // Constructs a Capability object from the underlying binding.
    Capability(net.ivoa.xml.voResource.v10.Capability xcapability) {
	if (logger.isLoggable(Level.FINEST)) 
	    logger.log(Level.FINEST, "Capability");

	List<net.ivoa.xml.voResource.v10.Validation> xvalidationLevelList = xcapability.getValidationLevelList();
	if (xvalidationLevelList != null) {
	    validationLevelList = new ArrayList<Validation>(xvalidationLevelList.size());
	    for (net.ivoa.xml.voResource.v10.Validation xvalidationLevel:  xvalidationLevelList) {
		validationLevelList.add(new Validation(xvalidationLevel));
	    }
	} else {
	    validationLevelList = new ArrayList<Validation>();
	}
	this.description = xcapability.getDescription();
	List<net.ivoa.xml.voResource.v10.Interface> xinterfaceList = xcapability.getInterfaceList();
	if (xinterfaceList != null) {
	    interfaceList = new ArrayList<Interface>(xinterfaceList.size());
	    for (net.ivoa.xml.voResource.v10.Interface xinterface:  xinterfaceList) {
		interfaceList.add(new Interface(xinterface));
	    }
	} else {
	    interfaceList = new ArrayList<Interface>();
	}
	this.standardId = xcapability.getStandardID();
    }   

    /**
     * Returns a list of Validation objects which should indicate the quality of the capability description and whether its implementation is functionally with applicable standards
     * @return list of validation levels for this Capability object
     */
    public List<Validation> getValidations() { 
	return validationLevelList; 
    }

    /**
     * Returns a description of what this Capability provides as part of the overall service.
     * @return description of this Capability object
     */
    public String getDescription() { 
	return description; 
    }

    /**
     * Returns a list of Interface objects which describe how to call the service to access this capability.
     * @return list of interfaces for this Capability.
     */
    public List<Interface> getInterfaces() { return interfaceList; }

    /**
     * Returns a unique identifier for the VO standard that this Capability complies to.
     * @return the IVOA standard identifier for this capability.
     */
    public String getStandardId() { return standardId; }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String standardId = getStandardId();
	String description = getDescription();
	List<Validation> validations = getValidations();
	List<Interface> interfaces = getInterfaces();

	if (standardId != null)
	    output.println(indent+"Standard id: "+standardId);

	if (description != null)
	    output.println(indent+"Description : "+description);

	if (validations != null) 
	    for (Validation validation: validations) {
		output.println(indent+"Validation: ");
		validation.list(output, indent+"  ");
	    }

	if (interfaces != null) 
	    for (Interface iface: interfaces) {
		output.println(indent+"Interface: ");
		iface.list(output, indent);
	    }
    }
}
