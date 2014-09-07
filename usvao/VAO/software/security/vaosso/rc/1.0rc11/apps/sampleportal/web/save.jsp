<%@ page import="org.nvo.sso.sample.reg.*" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:useBean id="user" class="org.nvo.sso.sample.reg.UserInfo" scope="page">
    <jsp:setProperty name="user" property="*" />
</jsp:useBean>
<%@ include file="users.h" %>
<% if (user.getName() != null) users.put(user.getUniqueId(), user); %>
<html>
  <head>
      <title>NVO Sample Registration</title>
      <link href="samplereg.css" type="text/css" rel="stylesheet" />
  </head>
  <body>
  <%@ include file="notes-top.html"%>
  <h1>NVO Single Signon Sample Registration</h1>
  <h2>Step 2: Local storage</h2>
  <% if (request.getRemoteUser() != null) { %>
    <p>
      Warning: logged in as <%=request.getRemoteUser()%>.  Registering an account while logged in will tie the account with the NVO username <b><%=request.getRemoteUser()%></b> instead of the new NVO username.
      <a href="/protected/pc_logout_clearlogin" title="This is a local logout link.  You'll need to set a logout URL for your particular server in your apache mod_pubcookie config.">Log out</a>.
    </p>
  <% } %>
  <blockquote>
      <table>
          <tr><td>Name</td><td><%=user.getName()%></td></tr>
          <tr><td>Email</td><td><%=user.getEmail()%></td></tr>
          <tr><td>Institution</td><td><%=user.getInst()%></td></tr>
          <tr><td>Phone</td><td><%=user.getPhone()%></td></tr>
          <tr><td>Favorite Color &nbsp; &nbsp;</td><td><%=user.getColor()%></td></tr>
          <tr><td>Created</td><td style="background:#fe7"><%=user.getCreated()%></td></tr>
          <tr><td>ID</td><td style="background:#fe7"><%=user.getUniqueId()%></td></tr>
      </table>
  </blockquote>
  <%@ include file="user-list.h" %>
  <%@ include file="notes-bottom.html"%>
  </body>
</html>