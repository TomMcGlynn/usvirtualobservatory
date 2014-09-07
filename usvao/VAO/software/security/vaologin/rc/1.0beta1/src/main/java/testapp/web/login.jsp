<div id="login_dialog" class="login_dialog">
   <form name="f" action="/testapp/j_spring_security_check" method="POST">
    <div id="user_name_login">
      <h2>Username</h2>
      <input autocapitalize="off" autocorrect="off" id="username" name="j_username" type="text"><br>

      <h2>Password</h2>
      <input id="password" name="j_password" type="password"><br>

        <label><input class="auto" id="remember_me" name="remember_me" value="1" type="checkbox"> Remember me on this computer</label><br>

      <input class="button" name="commit" value="Sign in" type="submit">
     </form>
<h1>Or2, Log Into Your Account with OpenID</h1>
<p>
  Please use the form below to log into your account with OpenID.
</p>
<form action="j_spring_openid_security_check" method="post">
  <label for="openid_identifier">Login</label>:
  <input id="openid_identifier" name="openid_identifier" size="20"
maxlength="100" type="text"/>
  <img src="images/openid.png" alt="OpenID"/>
  <br />
  <input type="submit" value="Login"/>
</form>
