<%
    String appDesc = (String)request.getAttribute("CONS_DESC");
    String requestToken = (String)request.getAttribute("TOKEN");
    String callback = (String)request.getAttribute("CALLBACK");
    Object error = request.getAttribute("ERROR");
    if (error == null)
        // another way to pass in an error message for the user to see
        // note that OpenidClientServlet uses this
        error = request.getParameter("ERROR");
    if(callback == null)
        callback = "";
    if (requestToken != null) {
        Cookie requestCookie = new Cookie("oauth_request", requestToken);
        requestCookie.setComment("OAuth request token");
        requestCookie.setMaxAge(-1); // temporary cookie -- goes away when browser is closed
        response.addCookie(requestCookie);
    }
%><%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Your Friendly OAuth Provider</title>
        <style type="text/css"><!--
            .login-link { color:#223388; text-decoration:underline }
            td { vertical-align:middle; }
        --></style>
    </head>
    <body>
	<table width="100%" cellpadding="10">
	  <tr>
	      <td bgcolor="#7799DD"><h1>Your Friendly OAuth Provider</h1></td>
	  </tr>
	</table>
	<br/>
        
    <h3>"<%=appDesc%>" is trying to access your information.</h3>

    <% if (error != null) { %>
        <b><font color="red"><%=(error)%></font></b>
    <% } %>

    <form name="authZForm" action="authorize" method="POST">
		<table border="0">
			<tr>
				<td>Username:</td>
	        	<td><input type="text" name="userId" value="" size="20" /></td>
			</tr>
			<tr>
				<td>Password:</td>
	        	<td><input type="text" name="password" value="" size="20" /></td>
	        </tr>
			<tr>
				<th colspan="2">
			        <input type="hidden" name="oauth_token" value="<%= requestToken %>"/>
			        <input type="hidden" name="oauth_callback" value="<%= callback %>"/>
			        <input type="submit" name="Authorize" value="Authorize"/>
				</th>
	        </tr>
        </table>
    </form>

    <%-- use a table to get vertical centering of the text and image --%>
    <table cellspacing="0" cellpadding="0" border="0" align="center"><tr>
        <td>
            <a href="openid?provider=vao&action=initiate" class="login-link">Log in using your</a>
        </td>
        <td>
            <a href="openid?provider=vao&action=initiate" class="login-link">
                <img src="images/vao_small.jpg" style="border:none" title="Log in using your VAO ID.">
            </a>
        </td>
        <td>
            <a href="openid?provider=vao&action=initiate" class="login-link">identity.</a>
        </td>
    </tr></table>

    </body>
</html>
