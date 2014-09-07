<%-- Based on http://code.google.com/p/openid4java/wiki/QuickStart
        May need something like this, for Java to trust the server (prereq: find local JRE's cacerts & keytool):
        sudo keytool -importcert -file /etc/grid-security/certificates/e33418d1.0 \
            -keystore cacerts -storepass changeit --%><%@

        page import="java.io.*" %><%@ page import="java.util.*" %>
        <%@ page import="java.security.cert.X509Certificate" %>
        <%@ page import="java.security.KeyStore" %>
        <%@ page import="java.net.URL" %>
        <%@ page import="javax.net.ssl.SSLSocketFactory" %>
        <%@ page import="javax.net.ssl.SSLContext" %>
        <%@ page import="javax.net.ssl.KeyManagerFactory" %>
        <%@ page import="javax.net.ssl.SSLSocket" %>
        <%@ page import="javax.net.ssl.HttpsURLConnection" %>
        <%@ page import="org.usvao.sso.client.VAOLogin" %><%

    response.setHeader("Cache-Control", "no-cache, no-store");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "-1");

    String status = "USER NOT LOGGED IN";
    List attribs = null;
    if (VAOLogin.isAuthenticated())
    {
        status = "User logged in as VAO User";
        attribs = VAOLogin.getAttributes();
    }

    String username = VAOLogin.getUsername();
    String name = VAOLogin.getName();
    String email = VAOLogin.getEmail();
    String phone = VAOLogin.getPhone();
    String institution = VAOLogin.getInstitution();
    String country = VAOLogin.getCountry();

    // This will have unencrypted private key and certificate
    String credential = VAOLogin.getCredential();

    if (credential != null) {
        FileWriter filew = new FileWriter("/tmp/cred");
        BufferedWriter bw = new BufferedWriter(filew);
        bw.write(credential);
        bw.close();
    }

    X509Certificate cert = VAOLogin.getCertificate();
    String subjectDN = null;
    if (cert != null)
        subjectDN = cert.getSubjectDN().toString();

    Properties props = VAOLogin.getAttributesAsProperties();

    if (credential != null) {
        // Passphrase we want protect the keystore with
        char[] passphrase = "changeit".toCharArray();

        KeyStore ks = VAOLogin.getPKCS12KeyStore(passphrase);

        // Recipe for using the above keystore for RESTful operations.
        if (ks != null) {
            ks.store(new FileOutputStream("/tmp/keystore.p12"), passphrase);

            SSLSocketFactory factory = null;
            try {
                SSLContext ctx;
                KeyManagerFactory kmf;

                // ctx = SSLContext.getInstance("Default");
                ctx = SSLContext.getInstance("TLS");
                // kmf = KeyManagerFactory.getInstance("SunX509");
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

                kmf.init(ks, passphrase);
                ctx.init(kmf.getKeyManagers(), null, null);

                factory = ctx.getSocketFactory();
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

            HttpsURLConnection conn = (HttpsURLConnection)new URL("https://nvoauth-stage.ncsa.illinois.edu/authn_client/secure.html").openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setSSLSocketFactory(factory);

            InputStream inputstream = conn.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            FileWriter filew = new FileWriter("/tmp/secure.html");
            BufferedWriter bw = new BufferedWriter(filew);
            String string = null;
            while ((string = bufferedreader.readLine()) != null) {
                System.out.println("Received " + string);
                bw.write(string, 0, string.length());
            }
            bw.close();
        }
    }

%>
<html>
<head>
    <title>Test App</title>
</head>
<body>
<h1>OpenID Sample to Test Spring Security</h1>

<%
    if (!VAOLogin.isAuthenticated())
    {
%>
<form action="j_spring_openid_security_check" id=”vaoOpenId” method="post" target="_top">
    <input id="openid_identifier" name="openid_identifier"
           type="hidden"
  value="https://testsso.ncsa.illinois.edu/openid/provider_id"/>
<input class="button" name="commit" value="Sign in" type="submit">
</form>
<%
    }
%>

    <p>You can access <a href="secure.jsp">secure</a> by clicking on the link</p>
    <p><%=status%></p>
    <p>User's attributes are: <%=attribs%></p>
    <p>User's username is: <%=username%></p>
    <p>User's name is: <%=name%></p>
    <p>User's email is: <%=email%></p>
    <p>User's phone is: <%=phone%></p>
    <p>User's institution is: <%=institution%></p>
    <p>User's country is: <%=country%></p>
    <p>User's credential is: <%=credential%></p>
    <p>User's SubjectDN is: <%=subjectDN%></p>
    <p>Properties are: <%=props%></p>
<form name="f" action="/testapp/secure.jsp" method="POST">
    <div id="user_name_login">
      <h2>Field1</h2>
      <input autocapitalize="off" autocorrect="off" id="field1" name="field1" type="text"/><br>

      <h2>Field2</h2>
      <input id="field2" name="field2"/><br>

      <input class="button" name="commit" value="Save" type="submit" />
</form>
</body>
</html>
