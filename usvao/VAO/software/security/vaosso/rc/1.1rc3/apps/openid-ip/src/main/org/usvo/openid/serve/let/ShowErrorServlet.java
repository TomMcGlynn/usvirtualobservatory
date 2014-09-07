package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.ui.ErrorResponse;
import org.usvo.openid.ui.LoginUI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Receives login requests. */
public class ShowErrorServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(ShowErrorServlet.class);

    public ShowErrorServlet() { super(true); }

    @Override
    protected void service(Session hibernateSession, HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException, ServletException, OpenIDException
    {
        String msg = "<p>Not really.  This page exists to demonstrate the "+
                     "error page layout</p>\n\n<p>Pages should not normally " +
                     "link to this page</p>";
        ErrorResponse.reportError(msg, 500, request, response);
    }
}
