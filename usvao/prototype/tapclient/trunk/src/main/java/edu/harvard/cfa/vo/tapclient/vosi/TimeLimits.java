package edu.harvard.cfa.vo.tapclient.vosi;

import java.io.PrintStream;
import java.math.BigInteger;

/**
 * Time limits a service may impose.
 */
public class TimeLimits {
    private BigInteger defaultLimit;
    private BigInteger hardLimit;

    TimeLimits(net.ivoa.xml.tap.v10.TimeLimits xtimeLimits) {
	if (xtimeLimits != null) {
	    defaultLimit = xtimeLimits.getDefault();
	    hardLimit = xtimeLimits.getHard();
	}
    }

    /**
     * The default limit, in seconds, upon job creation.
     */    
    public BigInteger getDefault() {
	return defaultLimit;
    }

    /**
     *The absolute limit, in seconds.
     */
    public BigInteger getHard() {
	return hardLimit;
    }

    public void list(PrintStream output) {
	list(output, "");
    }

    public void list(PrintStream output, String indent) {
	BigInteger defaultLimit = getDefault();
	BigInteger hardLimit = getHard();

	if (defaultLimit != null) {
	    output.println(indent+"Default: "+defaultLimit);
	}

	if (hardLimit != null) {
	    output.println(indent+"Hard: "+hardLimit);
	}
    }
}
