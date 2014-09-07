package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.openid4java.message.ParameterList;
import org.usvo.openid.Devel;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.serve.OpenIdKit;
import org.usvo.openid.ui.ErrorResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Receives login requests. */
public class SigninServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(SigninServlet.class);

    public SigninServlet() { super(true); }

    @Override
    protected void service
            (Session hibernateSession, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, OpenIDException
    {
        //noinspection ConstantConditions,PointlessBooleanExpression
        if ("http".equalsIgnoreCase(request.getScheme()) && !Devel.DEVEL) {
            ErrorResponse.reportError("Login requested via http; must be via https.", 500,
                    request, response);
        }
        else {
            log.debug("Signin request: " + request.getRequestURL());
            // Devel.logParamsTrace(getServletConfig(), request);
            ParameterList params = OpenIdKit.getParams(request);
            if (params == null) ErrorResponse.reportError
                    ("<p>Sorry, but your your login session is unavailable. "
                            + "It may have expired, or this server may have been restarted recently.</p>"
                            + " <p>Please go back to the site requesting login and try again.</p>",
                            500, request, response);
            else
                IdServer.getInstance(getServletContext()).handleRequest(request, response);
        }
    }
}
