/*
 * SlapServlet.java
 * $ID*
 */

package net.splatalogue.slap;

import dalserver.slap.SlapParamSet;
import dalserver.slap.SlapService;

public class SlapServlet extends dalserver.slap.SlapServlet {

    /**
     * Get a new SlapService instance.  By default the generic dataless
     * {@link dalserver.SlapService} class is used.  To build a real data
     * service, subclass SlapServlet and replace the newSlapService method
     * with one which calls a custom replacement for the builtin generic
     * SlapService class.
     *
     * <p>This version includes the dataDir and dataType parameters, used to
     *implement a simple mechanism (see {@link dalserver.SlapService#getData}
     *for returning static precomputed archival files from local storage on
     *the server.
     *
     * @param	params	The input and service parameters.
     */
    public SlapService newSlapService(SlapParamSet params) {
	return (new net.splatalogue.slap.SlapService(params));
    }

}