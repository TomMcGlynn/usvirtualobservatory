package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.openid4java.message.ParameterList;
import org.usvo.openid.Devel;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.serve.IdRequest;
import org.usvo.openid.serve.OpenIdKit;
import org.usvo.openid.ui.ErrorResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Receives OpenID login requests. */
public class SigninServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(SigninServlet.class);

    public SigninServlet() { super(true); }

    @Override
    protected void service(Session hibernateSession, HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException, ServletException, OpenIDException
    {
        //noinspection ConstantConditions,PointlessBooleanExpression
        if ("http".equalsIgnoreCase(request.getScheme()) && !Devel.DEVEL) {
            ErrorResponse.reportError("Login requested via http; must be via "+
                                      "https.", 500, request, response);
        }
        else {
            log.debug("Signin request: " + request.getRequestURL());
            // Devel.logParamsTrace(getServletConfig(), request);
            ParameterList params = OpenIdKit.getParams(request);
            if (params == null) {
                // an OpenID request appears not to be in progress
                String msg = "<p>Sorry, but your your login session is "+
                             "unavailable.  It may have expired, or this "+
                             "server may have been restarted recently.</p>"+
                             "<p>Please go back to the site requesting login "+
                             "and try again.</p>";
                ErrorResponse.reportError(msg, 500, request, response);
            }
            else {
                IdRequest idreq = IdServer.createIdRequest(getServletContext(),
                                                           request, response);
                idreq.handleOpenIDRequest();
            }
        }
    }
}
