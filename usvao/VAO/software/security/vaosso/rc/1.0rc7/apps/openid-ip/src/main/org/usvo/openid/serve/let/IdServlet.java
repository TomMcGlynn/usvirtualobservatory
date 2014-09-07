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
import java.util.*;

/** Serves OpenID pages -- that is, the pages identified by users' OpenID URLs. */
public class IdServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(IdServlet.class);

    public IdServlet() { super(true); }

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
        // Devel.logParamsTrace(getServletConfig(), request);
        String baseUrl = Conf.get().getBaseUrl();
        // mirror the scheme being used
        // (must be http for external RPs until we get a commercial cert)
        baseUrl = Conf.ensureScheme(request.getScheme(), baseUrl);

        String user = getUsername(request);
        
        String id = Conf.ensureScheme(request.getScheme(), Conf.get().getId(user));
        Map<String, String> map = new HashMap<String, String>();

        // put OpenID links in start of <head>
        String idLinks = "<link rel=\"openid2.provider\" href=\"" + baseUrl + "/provider\" />\n"
                + "<link rel=\"openid.server\" href=\"" + baseUrl + "/provider\" />";
        map.put(TemplateTags.TAG_HEAD_START, idLinks);

        map.put(TemplateTags.TAG_OPENID, id);
        map.put(TemplateTags.TAG_NAME, user);
        map.put(TemplateTags.TAG_OPENID_BASE, Conf.get().getIndexUrl());
        map.put(TemplateTags.TAG_TITLE, "OpenID for " + user);

        TemplatePage.display(request, response, TemplateTags.PAGE_ID, map);
    }

    public String getUsername(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null) path = "";
        return path.replaceAll("/", "");
    }
}
