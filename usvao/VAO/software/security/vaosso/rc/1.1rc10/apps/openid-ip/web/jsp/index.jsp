<%@ page import="org.usvo.openid.*" %><%@
        page import="java.util.*" %><%@
        page import="org.usvo.openid.serve.IdRequest" %><%@
        page import="org.usvo.openid.serve.IdServer" %><%@
        page import="org.usvo.openid.serve.AuthnAttempt" %><%@
        page import="org.usvo.openid.ui.TemplatePage" %>
<%@ page import="org.usvo.openid.ui.TemplateTags" %><%@
    page import="org.usvo.openid.ui.LoginUI" %><%

    IdRequest idreq = IdServer.createIdRequest(getServletContext(),
                                              request, response);
    // this provides backword compatibility for "?logout=true"
    if ("true".equalsIgnoreCase(request.getParameter("logout"))) {
        idreq.logout(request.getParameter("returnURL"));
    }
    else {
        AuthnAttempt authn = idreq.authenticate();
        if (authn.isSuccessful()) {

            Map<String, String> map = new HashMap<String, String>();
            map.put(TemplateTags.TAG_TITLE, "Welcome");
            String username = authn.getUsername();
            //String username = request.getParameter("username");
            map.put(TemplateTags.TAG_NAME, username);
            map.put(TemplateTags.TAG_OPENID_BASE, Conf.get().getIdBase());

            String feedback = "";
            if (username != null) {
                String openid = Conf.get().getId(username);
                feedback += "<div style=\"background:#ddf; margin-top:0.7em; padding:0.4em 1em 0.4em 1em; -moz-border-radius:0.3em; width:auto\">\n";
                feedback += "<p>Your VAO OpenID is <a href=\"" + openid + "\">" + openid + "</a>.</p>\n";
                // TODO: http-encode openid
                feedback += "<p>Test it by <a href=\"http://openidenabled.com/resources/openid-test/diagnose-server/start?openid_url="
                        + openid + "\">logging in via a third party</a>.</p>\n";
                feedback += "</div>\n";
            }
            map.put(TemplateTags.TAG_FEEDBACK, feedback);

            TemplatePage.display(request, response, TemplateTags.PAGE_INDEX, map);
        } else {
            String path = request.getContextPath() + request.getServletPath();
            // log.trace("Login form path = " + path);
            LoginUI ui = LoginUI.forLocal(request, response, authn, path,
                                          "VAO Login Portal");
            ui.display();
        }
    }
   
%>
