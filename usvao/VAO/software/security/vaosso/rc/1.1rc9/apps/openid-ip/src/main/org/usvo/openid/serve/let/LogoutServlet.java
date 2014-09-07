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

/** Receives logout requests. */
public class LogoutServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(LogoutServlet.class);

    public LogoutServlet() { super(true); }

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
            log.debug("Logout request: " + request.getRequestURL());
            IdRequest idreq = IdServer.createIdRequest(getServletContext(),
                                                       request, response);
            idreq.logout(request.getParameter("returnURL"));
        }
    }
}
