<%@ page import="org.nvo.sso.sample.reg.*" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="users.h" %>
<%
    String status = null;
    UserInfo user = null;
    String idString = request.getParameter(UserInfo.UNIQUE_ID);

    String username = request.getParameter("user");
    if (username == null || username.length() == 0)
        username = request.getRemoteUser();

    if (StringKit.isEmpty(idString)) status = "No user ID supplied.";
    else {
        try {
            long id = Long.parseLong(idString);
            user = users.get(id);
            if (user == null) status = "Unknown user ID: " + id + ".";
            else if (user.isRegistered()) status = "User #" + id + " was previously registered " + user.getRegistered() + ".";
            else {
                user.register(username);
                status = "Registration successful.  Username \"" + username + "\" correlated with ID " + id + " (" + user + ").";
            }
            if (user == null) status = "Unknown user ID " + id + ".";
        } catch(NumberFormatException e) {
            status = "Could not parse \"" + idString + "\" as an ID.";
        }
    }
%>
<html>
  <head>
      <title>NVO Sample Registration</title>
      <link href="samplereg.css" type="text/css" rel="stylesheet" />
  </head>
  <body>
  <%@ include file="notes-top.html"%>
  <h1>NVO Single Signon Sample Registration</h1>
  <h2>Step 4: Local Retrieval</h2>
  <% if (status != null) { %>
    <p style="background:#fe7;margin-left:2em;padding:0.5em"><%=status%></p>
  <% } %>
  <% if (request.getRemoteUser() != null) { %>
  <p>
    You are currently <a href="http://sso.us-vo.org">logged in to the NVO</a> as <%=request.getRemoteUser()%>.
    <a href="/protected/pc_logout_clearlogin" title="This is a local logout link.  You'll need to set a logout URL for your particular server in your apache mod_pubcookie config.">Log out</a>.
  </p>
  <% } %>
  <% if (user != null) { %>
      <blockquote>
          <table>
              <tr><td>Name</td><td><%=user.getName()%></td></tr>
              <tr><td>Email</td><td><%=user.getEmail()%></td></tr>
              <tr><td>Institution</td><td><%=user.getInst()%></td></tr>
              <tr><td>Phone</td><td><%=user.getPhone()%></td></tr>
              <tr><td>Favorite Color &nbsp; &nbsp;</td><td><%=user.getColor()%></td></tr>
              <tr><td>Created</td><td style="background:#fe7"><%=user.getCreated()%></td></tr>
              <tr><td>Registered</td><td style="background:#fe7"><%=user.getRegistered()%></td></tr>
              <tr><td>ID</td><td style="background:#fe7"><%=user.getUniqueId()%></td></tr>
          </table>
      </blockquote>
  <% } %>
  <%@ include file="user-list.h" %>
  <%@ include file="notes-bottom.html"%>
  </body>
</html>