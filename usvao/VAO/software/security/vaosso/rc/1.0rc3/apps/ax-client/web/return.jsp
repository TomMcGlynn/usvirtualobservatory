<%@ page import="org.openid4java.consumer.ConsumerManager" %>
<%@ page import="org.openid4java.consumer.VerificationResult" %>
<%@ page import="org.openid4java.discovery.DiscoveryInformation" %>
<%@ page import="org.openid4java.message.Message" %>
<%@ page import="org.openid4java.message.MessageExtension" %>
<%@ page import="org.openid4java.message.ParameterList" %>
<%@ page import="org.openid4java.message.ax.AxMessage" %>
<%@ page import="org.openid4java.message.ax.FetchResponse" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<html>
<head>
    <title>OpenId Sample Attribute Exchange</title>
    <link href="ax.css" type="text/css" rel="stylesheet">
</head>
<body>

<%
    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    Map<String, Throwable> errors = new LinkedHashMap<String, Throwable>();
    ConsumerManager manager = (ConsumerManager) session.getAttribute("manager");
    if (manager == null)
        errors.put("ConsumerManager is null.", null);
    ParameterList openidResp = null;
    DiscoveryInformation discovered = null;
    VerificationResult verification = null;
    try {
        openidResp = new ParameterList(request.getParameterMap());
        discovered = (DiscoveryInformation) session.getAttribute("discovered");
        session.removeAttribute("discovered");
        if (manager != null)
            verification = manager.verify(request.getRequestURL().toString(), openidResp, discovered);
    } catch(Exception e) { errors.put("verifying", e); }
    String q = request.getQueryString();
    String[] qq = q == null ? new String[0] : q.split("&");
%>

<% if (verification != null) { %>
    <h2>Verification:</h2>
    <blockquote>
        Status:<%=verification.getStatusMsg()%><br>
        Authenticated as: <%=verification.getVerifiedId()%>
    </blockquote>
    <%
    Message authResponse = verification.getAuthResponse();
    if (authResponse != null) {
    %>
        <%
        if (authResponse.hasExtension(AxMessage.OPENID_NS_AX)) {
            MessageExtension ext = authResponse.getExtension(AxMessage.OPENID_NS_AX);
            if (ext != null && ext instanceof FetchResponse) {
                FetchResponse fetchResp = (FetchResponse) ext;
            %>
                <h3>Extension:</h3>
                <blockquote><table>
                    <% for (Object key : fetchResp.getAttributes().keySet()) {
                        List<String> val = fetchResp.getAttributeValues((String) key);
                        for (int i = 0; i < val.size(); i++) {
                            String s = val.get(i);
                            if (s.startsWith("http"))
                                val.set(i, "<a href=\"" + s + "\">" + s + "</a>");
                        }
                        %>
                        <tr><td><%=key%></td><td><%=val%></td></tr>
                    <% } %>
                </table></blockquote>
            <% } %>
        <% } %>
        <h3>Response Message:</h3>
        <blockquote>
        <pre>
        <%=authResponse%>
        </pre>
        </blockquote>
    <% } %>
<% } %>

<% if (openidResp != null) { %>
<h2>OpenID Response:</h2>
<blockquote><pre>
<%=openidResp%>
</pre></blockquote>
<% } %>

<% if (manager != null) { %>
<h2>Associations:</h2>
<blockquote><pre>
<%=manager.getAssociations()%>
</pre></blockquote>
<% } %>

<% if (discovered != null) { %>
<h2>Discovered:</h2>
<blockquote><pre>
<%=discovered%>
</pre></blockquote>
<% } %>

<h2>Query String:</h2>
<blockquote><table>
    <% for (String pair : qq) { %>
        <tr>
        <% String[] pp = pair.split("=");
        if (pp != null) for (String p : pp) { %>
            <td><%=URLDecoder.decode(p, "UTF-8")%></td>
        <% } %>
        </tr>
    <% } %>
</table></blockquote>

<%@include file="foot.jsp" %>
