<%@ page import="org.nvo.sso.sample.reg.*" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="users.h" %>
<html>
  <head>
      <title>NVO Sample Registration</title>
      <link href="samplereg.css" type="text/css" rel="stylesheet" />
  </head>
  <body>
  <%@ include file="notes-top.html"%>
  <h1>NVO Single Signon Sample Registration</h1>
  <h2>Step 1: Local registration</h2>
  <blockquote>
      <form action="save.jsp">
          <table>
              <tr><td>Name</td><td><input type="text" name="<%=UserInfo.NAME%>" /></td></tr>
              <tr><td>Email</td><td><input type="text" name="<%=UserInfo.EMAIL%>" /></td></tr>
              <tr><td>Institution</td><td><input type="text" name="<%=UserInfo.INST%>" /></td></tr>
              <tr><td>Phone</td><td><input type="text" name="<%=UserInfo.PHONE%>" /></td></tr>
              <tr><td>Favorite Color</td><td><input type="text" name="<%=UserInfo.COLOR%>" /></td></tr>
              <tr><td colspan="2" style="text-align:right"><input type="submit" value="Register"></td></tr>
          </table>
      </form>
  </blockquote>
  <%@ include file="user-list.h" %>
  <%@ include file="notes-bottom.html"%>
  </body>
</html>