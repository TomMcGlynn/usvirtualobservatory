

/*
 * JhuSsapServlet.java
 * $ID*
 */

package demo;

import dalserver.*;

/**
 * HTTP servlet implementing a proxy SSAP service for the JHU spectrum
 * services.  This implementation is intended only for demonstration/test
 * purposes, e.g., developing and testing SSAP client applications.
 */
public class JhuSsapServlet extends SsapServlet {

    /**
     * Get a new instance of the JhuSsapService proxy service.
     *
     * @param params	Service parameter set.
     */
    public SsapService newSsapService(SsapParamSet params) {
	return ((SsapService) new JhuSsapService(params));
    }
}


