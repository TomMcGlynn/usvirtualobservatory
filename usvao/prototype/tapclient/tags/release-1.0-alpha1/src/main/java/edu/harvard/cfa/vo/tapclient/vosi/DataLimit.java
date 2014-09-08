package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.math.BigInteger;

/**
 * Limits on data.
 */
public class DataLimit {
    private BigInteger value;
    private String unit;

    DataLimit(net.ivoa.xml.tap.v10.DataLimit xdataLimit) {
	if (xdataLimit != null) {
	    value = xdataLimit.getBigIntegerValue();
	    unit = xdataLimit.getUnit().toString();
	}
    }

    /**
     * Return the limit value
     * @return the data limit
     */
    public BigInteger getValue() {
	return value;
    }

    /**
     * Return the unit of the limit, e.g. 'rows' or 'bytes'
     * @return the data limit unit
     */
    public String getUnit() {
	return unit;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	BigInteger value = getValue();
	String unit = getUnit();
	if (value != null) 
	    output.println(indent+"Value: "+value);
	if (unit != null)
	    output.println(indent+"Unit: "+unit);
    }
}
