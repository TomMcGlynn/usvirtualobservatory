package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.serve.IdServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Provide an OpenID provider endpoint to service requests from Relying Parties.
 *  Most functionality is actually in {@link org.usvo.openid.serve.IdServer}. */
public class ProviderServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(ProviderServlet.class);

    public ProviderServlet() { super(true); }

    protected void service(Session hibernateSession, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException, OpenIDException {
        log.debug("Request for OpenID Provider: " + request.getRequestURL());
        // Devel.logParamsTrace(getServletConfig(), request);
        IdServer.getInstance(getServletContext()).handleRequest(request, response);
    }
}
