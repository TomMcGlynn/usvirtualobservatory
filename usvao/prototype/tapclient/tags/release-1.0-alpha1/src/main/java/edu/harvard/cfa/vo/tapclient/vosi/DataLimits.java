package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;

/**
 * Data limits for TAP service upload or maxrec parameters
 */
public class DataLimits {
    private DataLimit defaultLimit;
    private DataLimit hardLimit;

    DataLimits(net.ivoa.xml.tap.v10.DataLimits xdataLimits) {
	if (xdataLimits != null) {
	    defaultLimit = new DataLimit(xdataLimits.getDefault());
	    hardLimit = new DataLimit(xdataLimits.getHard());
	}
    }

    /**
     * Return the limit for newly-created jobs. 
     */
    public DataLimit getDefault() {
	return defaultLimit;
    }

    /**
     * Return the absolute limit.
     */
     public DataLimit getHard() {
	return hardLimit;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	DataLimit defaultLimit = getDefault();
	DataLimit hardLimit = getHard();

	if (defaultLimit != null) {
	    output.println(indent+"Default data limit: ");
	    defaultLimit.list(output, indent+"  ");
	}
	if (hardLimit != null) {
	    output.println(indent+"Hard data limit: ");
	    hardLimit.list(output, indent+"  ");
	}
    }
}
