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

/** Serves an XRDS document for a given username. */
public class XRDSIdServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(XRDSIdServlet.class);

    public XRDSIdServlet() { super(true); }

    protected void service(Session hibernateSession, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException 
    {
        // redirect https OpenIDs to http
//        if ("https".equalsIgnoreCase(request.getScheme())) {
//            String url = request.getRequestURL().toString();
//            url = url.replaceFirst("https", "http");
//            response.sendRedirect(url);
//        }

        log.debug("XRDS Request: " + request.getRequestURL());
        String username = request.getPathInfo();
        boolean isProviderId = 
            username == null || username.equals("/provider_id");

        response.setHeader("Content-Type", "application/xrds+xml");
        PrintWriter out = response.getWriter();

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<xrds:XRDS xmlns:xrds=\"xri://$xrds\" xmlns:openid=\"http://openid.net/xmlns/1.0\" xmlns=\"xri://$xrd*($v*2.0)\">");
        out.println("  <XRD>");
        out.println("   <Service priority=\"0\">");

        out.print("    ");
        if (isProviderId) {
            // defaults to the provider_id description
            out.println("<Type>http://specs.openid.net/auth/2.0/server</Type>");
        }
        else {
            out.println("<Type>http://specs.openid.net/auth/2.0/signon</Type>");
        }
        out.println("    <Type>http://openid.net/srv/ax/1.0</Type>");

        out.print("    <URI>");
        out.print(Conf.get().getBaseUrl() + "/provider");
        out.println("</URI>");

        if (! isProviderId) {
            out.print("    ");
            out.print("<LocalID>");
            out.print(Conf.get().getBaseUrl());
            out.print("/id");
            out.print(username);
            out.println("</LocalID>");
        }

        out.println("   </Service>");
        out.println("  </XRD>");
        out.println("</xrds:XRDS>");

        out.flush();
    }
}
