/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import java.io.Writer;
import java.io.IOException;
import java.util.Properties;

/**
 * an interface used for assembling the body of a message to be 
 * sent to a user.  
 */
public interface MessageComposer {

    /**
     * write the message to the given output stream.
     * @param info    a list of properties representing information to be 
     *                   incorporated into the message.  The expected 
     *                   properties and their use is implementation dependent. 
     * @param out     the output stream to write the message to.
     */
    public void compose(Properties info, Writer out) throws IOException;

}
