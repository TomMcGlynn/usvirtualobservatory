<%-- Based on http://code.google.com/p/openid4java/wiki/QuickStart
        May need something like this, for Java to trust the server (prereq: find local JRE's cacerts & keytool):
        sudo keytool -importcert -file /etc/grid-security/certificates/e33418d1.0 \
            -keystore cacerts -storepass changeit --%><%@

        page import="java.io.*" %><%@ page import="java.util.*" %>
        <%@ page import="org.usvao.sso.client.VAOLogin" %>
        <%@ page import="java.security.Principal" %><%

    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String field1 = request.getParameter("field1");
    String field2 = request.getParameter("field2");

   String prinname = request.getUserPrincipal().toString();
   String identityUrl = "NOURL";
   List attribs = VAOLogin.getAttributes();


%>
<html>
<head>
    <title>Test App</title>
</head>
<body>
<h1>OpenID Sample to Test Spring Security</h1>
    <p>This is SECURE INFORMATION</p>
    <p>Field1 is (<%=field1%>) Field2 is (<%=field2%>) Principal (<%=prinname%>) identityUrl(<%=identityUrl%>) Attributes(<%=attribs%>)</p>
