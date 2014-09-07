<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="VAOLogin.aspx.cs" Inherits="Publishing.VAOLogin" %>
<%@ Register Assembly="DotNetOpenAuth" Namespace="DotNetOpenAuth.OpenId.RelyingParty" TagPrefix="rp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <link type="text/css" rel="stylesheet" href="styles/layout.css" title="default" media="all"/>
    <title>VAO Publishing Login</title>
    
    <script type="text/javascript" src="scripts/InitializeJavaScript.js"></script>
    <script type="text/javascript" src="scripts/namespace.js"></script>
    <script type="text/javascript" src="scripts/login.js"></script>
    <script type="text/javascript" src="scripts/loginstatus.js"></script>

         <!-- Start this application. -->
    <script type="text/javascript">
        Ext.onReady(PublishingWizard.LoginStatusWizard.createAndRun, PublishingWizard.LoginStatusWizard, { mainDiv: 'isloggedin-div' });
        Ext.onReady(PublishingWizard.LoginWizard.createAndRun, PublishingWizard.LoginWizard, { mainDiv: 'wizard-div' });
    </script>
</head>
<body>

<div id="main-wrapper">
  <div align="right"></div>

<div id="logo"><a href="http://www.usvao.org/" title="Home" target="_blank"><img src="images/VAO_logo_100.png" alt="VAO" height="50" width="100"/></a></div>
<div id="logo"><a href="http://vao.stsci.edu/directory" title="Directory Search" target="_blank"><img src="images/Directory50.png" alt="Directory Search" height="50" width="50" /></a></div>
<div id="nav-wrapper">
  <div id="navigation">
  <div id="isloggedin-div"></div>
  </div>
  <div id="nav-right"></div>
</div>
<div class="clear"></div>

<div id="content-area"> 
            <div id="helpcontent"><h1>VAO Registry Publishing Interface - Login</h1><br />
            <p>Using the VAO registry publishing interface, you can publish new and modify existing VO resources, representing data services, catalogs, archive institutions,
            and many other astronomical data concepts. This system manages resources hosted at the VAO registry at
            Space Telescope Science Institute. Resources in this VAO registry are accessible by search tools and VO client software througout
            the IVOA. For more information, check the <a href="help.html" target="_blank">publishing help page</a> or the main <a href="http://usvao.org" target="_blank">VAO web page.</a> </p>
            <br />
</div>
</div>

    <form id="form1" runat="server">
    <div>
<asp:Button ID="loginButton" runat="server" Text="Sign in with VAO Login" OnClick="loginButton_Click" />
<asp:Button ID="registerButton" runat="server" Text="Register for a new VAO Login" OnClick="registerButton_Click" />
<br />
<asp:Label ID="loginFailedLabel" runat="server" EnableViewState="False" Text="Login failed"
        Visible="False" />
<asp:Label ID="loginCanceledLabel" runat="server" EnableViewState="False" Text="Login canceled"
        Visible="False" />
    </div>
    </form>

    <div style="width:98%;text-align:right;float:left;padding-top:5px;padding-right:5px;">
<a href="http://www.usvao.org/acknowledging-the-vao/"><span class="tiny">Acknowledging VAO</span></a><br/>
  ©<strong> 2013 VAO, LLC</strong></div>
</div>

</body>
</html>
