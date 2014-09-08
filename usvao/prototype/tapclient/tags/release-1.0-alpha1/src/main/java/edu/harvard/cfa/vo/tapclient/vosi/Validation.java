package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.math.BigInteger;

/**
 * An evaluation of the quality of the capability description and whether its implementation is functionally with applicable standards.
 * @see Capability
 */
public class Validation {
    private BigInteger value;
    private String validatedBy;
    /**
     * Constructs a Validation object.  For use by subclasses
     */
    protected Validation() {
	this.value = null;
	this.validatedBy = null;
    }

    // Constructs a Validation object from the underlying binding.
    Validation(net.ivoa.xml.voResource.v10.Validation xvalidation) {
	this.value = xvalidation.getBigIntegerValue();
	this.validatedBy = xvalidation.getValidatedBy();
    }

    /**
     * Returns a number indicating the validation level.
     * @return a number indicating the validation level or null if not specified by the service.
     */
    public BigInteger getValue() { 
	return value; 
    }

    /**
     * Returns a String which may represent an IVOA ID referring to a registered organization or registry that determined the Validation value.
     * @return an IVOA ID referring to a registered organization that determined the Validation value or null if not specified by the service.
     */
    public String getValidatedBy() { 
	return validatedBy; 
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	String validatedBy = getValidatedBy();
	BigInteger value = getValue();

	if (validatedBy != null)
	    output.println(indent+"Validated by: "+validatedBy);

	if (value != null) 
	    output.println(indent+"Value: "+value);
    }
}
