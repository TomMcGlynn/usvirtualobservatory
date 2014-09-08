<%@ page import="org.ietf.jgss.GSSCredential" %>
<%@ page import="org.gridforum.jgss.ExtendedGSSManager" %>
<%@ page import="org.gridforum.jgss.ExtendedGSSCredential" %>
<%@ page import="java.io.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>GSI Credential in Java</title></head>
  <body>
  <p>Loading credential from <%=request.getAttribute("X509_USER_PROXY")%>.</p>
  <dl>

    <dt>Logged in as</dt>
    <dd><%=request.getRemoteUser()%></dd>

  <%  try {
          ExtendedGSSCredential cred = loadCredential(request);
  %>

    <dt>Name</dt>
    <dd><%=cred.getName()%></dd>

    <dt>Remaining time</dt>
    <dd><%=describeSeconds(cred.getRemainingLifetime())%></dd>
  </dl>

  <%  } catch (Exception e) { %>

  <p>Exception loading credential: <b><%=e.getMessage()%></b></p>
  <pre>
  <% e.printStackTrace(new PrintWriter(out)); %>
  </pre>

  <%  } %>
  </body>
</html>
<%!
    // derived from Terry Fleury's MyProxyDelegator class
    ExtendedGSSCredential loadCredential(String filename) throws Exception {
        if (filename == null)
            throw new NullPointerException("filename is null");

        File credFile = new File(filename);
        if (!credFile.exists()) throw new FileNotFoundException
            ("Credential file \"" + filename + "\" does not exist.");

        byte[] credBytes = new byte[(int) credFile.length()];
        FileInputStream fileIn = new FileInputStream(credFile);
        //noinspection ResultOfMethodCallIgnored
        fileIn.read(credBytes);

        ExtendedGSSManager mgr = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        // byte[] buffer, int option, int lifetime, OID mechanism, int usage
        return (ExtendedGSSCredential) mgr.createCredential
                (credBytes, ExtendedGSSCredential.IMPEXP_OPAQUE,
                        GSSCredential.DEFAULT_LIFETIME, null,
                        ExtendedGSSCredential.INITIATE_AND_ACCEPT);
    }

    ExtendedGSSCredential loadCredential(HttpServletRequest request) throws Exception {
        String credPath = (String) request.getAttribute("X509_USER_PROXY");
        if (credPath == null) throw new Exception
            ("No credential specified -- X509_USER_PROXY is absent.");
        return loadCredential(credPath);
    }

    String describeSeconds(int seconds) {
	int h = seconds / 3600;
	int m = (seconds % 3600) / 60;
	int s = seconds % 60;
        NumberFormat f = NumberFormat.getIntegerInstance();
	f.setMinimumIntegerDigits(2);
	return "" + h + ":" + f.format(m) + ":" + f.format(s);
    }
%>