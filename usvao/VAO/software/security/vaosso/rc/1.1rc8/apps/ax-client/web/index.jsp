<%-- Based on http://code.google.com/p/openid4java/wiki/QuickStart
        May need something like this, for Java to trust the server (prereq: find local JRE's cacerts & keytool):
        sudo keytool -importcert -file /etc/grid-security/certificates/e33418d1.0 \
            -keystore cacerts -storepass changeit --%><%@

        page import="org.openid4java.consumer.ConsumerManager" %><%@
        page import="org.openid4java.discovery.DiscoveryException" %><%@
        page import="org.openid4java.discovery.DiscoveryInformation" %><%@
        page import="org.openid4java.message.AuthRequest" %><%@
        page import="org.openid4java.message.ax.FetchRequest" %><%@
        page import="java.io.*" %><%@ page import="java.util.*" %><%

    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    Map<String, Throwable> errors = new LinkedHashMap<String, Throwable>();
    ConsumerManager manager = (ConsumerManager) session.getAttribute("manager");
    synchronized(this) {
        if (manager == null) {
            manager = new ConsumerManager();
            session.setAttribute("manager", manager);
        }
    }
    boolean submitted = "true".equals(request.getParameter("submitted"));
    String openId = request.getParameter("openid");

    // discover endpoint, but only if we are asked to and haven't already discovered it
    List discoveries = null;
    DiscoveryInformation discovered;
    if (submitted) discovered = (DiscoveryInformation) session.getAttribute("discovered");
    else {
        discovered = null;
        session.removeAttribute("discovered");
    }
    if (submitted && discovered == null) {
        if (openId != null && openId.trim().length() > 0) {
            try {
                System.out.println("Before discovery: associations: " + manager.getAssociations());
                discoveries = manager.discover(openId);
                discovered = manager.associate(discoveries);
                System.out.println("After discovery: associations: " + manager.getAssociations());;
                session.setAttribute("discovered", discovered);
            } catch (DiscoveryException e) { errors.put("discovering", e); }
        }
    }
    AuthRequest authReq;

    // initiate authentication, if we're ready to
    if (discovered != null) {
        String returnUrl = request.getRequestURL().toString();
        int trim = returnUrl.indexOf("index.jsp");
        if (trim < 0) trim = returnUrl.indexOf("?");
        if (trim >= 0) returnUrl = returnUrl.substring(0, trim);
        returnUrl += "return.jsp";

        try {

            // add some attribute requests
            FetchRequest fetch = FetchRequest.createFetchRequest();
            String emailRequest = request.getParameter("email"),
                    phoneRequest = request.getParameter("phone"),
                    usernameRequest = request.getParameter("username"),
                    nameRequest = request.getParameter("name"),
                    credRequest = request.getParameter("credential");
            if (nameRequest != null && nameRequest.length() > 0)
                fetch.addAttribute("name", "http://axschema.org/namePerson",
                        "required".equals(nameRequest));
            if (usernameRequest != null && usernameRequest.length() > 0)
                fetch.addAttribute("username", "http://axschema.org/namePerson/friendly",
                        "required".equals(usernameRequest));
            if (emailRequest != null && emailRequest.length() > 0) {
                fetch.addAttribute("email", "http://axschema.org/contact/email",
                        "required".equals(emailRequest));
                fetch.setCount("Email", 3); // want up to three email addresses;
            }
            if (phoneRequest != null && phoneRequest.length() > 0)
                fetch.addAttribute("phone", "http://axschema.org/contact/phone/default",
                        "required".equals(phoneRequest));
            if (credRequest != null && credRequest.length() > 0)
                fetch.addAttribute("credential", "http://sso.usvao.org/schema/credential/x509",
                        "required".equals(credRequest));

            authReq = manager.authenticate(discovered, returnUrl);
            if (fetch.getAttributes().size() > 0)
                authReq.addExtension(fetch);

            response.sendRedirect(authReq.getDestinationUrl(true));
        } catch (Exception e) { errors.put("authenticating", e); }
    }
%>
<html>
<head>
    <title>OpenId Sample Attribute Exchange</title>
</head>
<body>
<h1>OpenID Sample Attribute Exchange</h1>

<form action="index.jsp">
    <label>
        OpenID:
        <input type="text" name="openid" style="width:30em"/>
    </label>
    <input type="hidden" name="submitted" value="true"/>
    <input type="submit" value="Go"/>
    <p>Request attributes:</p>
    <blockquote>
        <label>Name
            <select name="name">
                <option value="" selected>not requested</option>
                <option value="requested">requested</option>
                <option value="required">required</option>
            </select>
        </label><br>
        <label>Email
            <select name="email">
                <option value="" selected>not requested</option>
                <option value="requested">requested</option>
                <option value="required">required</option>
            </select>
        </label><br>
        <label>Phone
            <select name="phone">
                <option value="" selected>not requested</option>
                <option value="requested">requested</option>
                <option value="required">required</option>
            </select>
        </label><br>
        <label>Credential
            <select name="credential">
                <option value="" selected>not requested</option>
                <option value="requested">requested</option>
                <option value="required">required</option>
            </select>
        </label><br>
        <!--
        <label>Username
            <select name="username">
                <option value="" selected>not requested</option>
                <option value="requested">requested</option>
                <option value="required">required</option>
            </select>
        </label><br>
        -->
        <input type="submit" value="Go"/>
    </blockquote>
</form>

<% if (discoveries != null) { %>
    <h2>Discoveries:</h2>
    <ul>
        <% for (Object discovery : discoveries) { %>
            <li><%=discovery%></li>
        <% } %>
    </ul>
<% } %>

<h2>Associations:</h2>
<blockquote><pre>
<%=manager.getAssociations()%>
</pre></blockquote>

<% if (discovered != null) { %>
<h2>Discovered:</h2>
<blockquote><pre>
<%=discovered%>
</pre></blockquote>
<% } %>

<%@include file="foot.jsp" %>
