package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.usvo.openid.Conf;
import org.usvo.openid.ui.TemplatePage;
import org.usvo.openid.ui.TemplateTags;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/** Serves OpenID Provider ID page as an XRDS document. */
public class ProviderIdServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(ProviderIdServlet.class);

    public ProviderIdServlet() { super(true); }

    protected void service(Session hibernateSession, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException 
    {
        // redirect https OpenIDs to http
//        if ("https".equalsIgnoreCase(request.getScheme())) {
//            String url = request.getRequestURL().toString();
//            url = url.replaceFirst("https", "http");
//            response.sendRedirect(url);
//        }

        log.debug("Request for OpenID: " + request.getRequestURL());

        response.setHeader("Content-Type", "application/xrds+xml");
        PrintWriter out = response.getWriter();

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<xrds:XRDS xmlns:xrds=\"xri://$xrds\" xmlns:openid=\"http://openid.net/xmlns/1.0\" xmlns=\"xri://$xrd*($v*2.0)\">");
        out.println("  <XRD>");
        out.println("  <Service priority=\"0\">");
        out.println("  <Type>http://specs.openid.net/auth/2.0/server</Type>");
        out.println("  <Type>http://openid.net/srv/ax/1.0</Type>");
        //out.println("  <URI>https://open.login.yahooapis.com/openid/op/auth</URI>");
        out.print("  <URI>");
        out.print(Conf.get().getBaseUrl() + "/provider");
        out.println("</URI>");
        out.println("  </Service>");
        out.println("  </XRD>");
        out.println("</xrds:XRDS>");

        out.flush();
    }
}
