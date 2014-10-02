/*
 * JhuProxyServlet.java
 * $ID*
 */

package dataServices;

import dalserver.*;

/**
 * HTTP servlet implementing a proxy SSAP service for the JHU spectrum
 * services.  This implementation is intended only for demonstration/test
 * purposes, e.g., developing and testing SSAP client applications.
 */
public class JhuProxyServlet extends SsapServlet {

    /**
     * Get a new instance of the JhuProxyService proxy service.
     *
     * @param params	Service parameter set.
     */
    public SsapService newProxyService(SsapParamSet params) {
	return ((SsapService) new JhuProxyService(params));
    }
}
