package Portal::Welcome_Constants;

use strict;
use warnings;
  
our $HR = qq|
<div class="divider"><hr noshade="noshade" /></div>
|;

our $PAGE_OPEN = qq|
<html>
<head>
<title>NVO User Logon Registration</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="https://sso.us-vo.org/usvo3.css" type="text/css" rel="stylesheet">
<link href="https://sso.us-vo.org/nvo.css" type="text/css" rel="stylesheet">
<link href="https://sso.us-vo.org/sso.css" type="text/css" rel="stylesheet">
</head>

<body leftmargin="0" topmargin="0">

<table width="800" border="0" align="center" cellpadding="0">
  <tr align="left" valign="top">
    <td width="220" style="vertical-align:middle">
      <div style="padding-bottom:.35em"><a class="nav" href="https://sso.us-vo.org/">Login to the NVO</a></div>
      <div style="padding-bottom:.35em"><a class="nav" href="https://nvologin1.ncsa.uiuc.edu/">Identity Portal</a></div>
      <div style="padding-bottom:.35em"><a class="nav" href="http://sso.us-vo.org/register/?returnURL=https://nvologin1.ncsa.uiuc.edu/protected/welcome&portalName=the+NVO+Identity+Portal">Create an account</a></div>
      <div><a class="nav" href="http://dev.usvao.org/vao/wiki/SSOforPortals/Overview">Documentation</a></div>
    </td>

    <td width="360"><div align="center">
      <div class="big"><img src="https://sso.us-vo.org/images/NVOwords_200pixels.jpg" alt="" width="198" height="108" /></div>
    </div></td>

    <td width="220" style="vertical-align:middle">
      <div align="right">
	<a class="nav" href="http://www.us-vo.org/what.cfm">what is the nvo</a><br />
	<a class="nav" href="http://www.us-vo.org/faq.cfm">faq</a><br />
	<a class="nav" href="http://blogs.us-vo.org/news/?p=3">the nvo book</a><br />
	<a class="nav" href="http://www.us-vo.org/behindthescenes/index.cfm">behind the scenes</a><br />
	<a class="nav" href="http://www.us-vo.org/pubs/index.cfm">documents</a>
      </div>
    </td>
  </tr>
  <tr><td colspan="3">
    <div class="divider"><hr noshade="noshade" /></div>

<h1 class="top" style="text-align:center">Welcome to the NVO Identity Portal</h1>
|;

our $PAGE_MIDDLE_LOGIN_A = qq|
<div class="announce">You are now logged onto the Virtual Observatory as
|;

our $PAGE_MIDDLE_LOGIN_B = qq|.<br>
Access to restricted services and data is enabled.</div>

<h2>Update your Identity Profile information</h2>

<p><i>This service is still under construction.</i></p>

<h2>Download a Certificate</h2>

<p>
You can create and download an NVO certificate that can be loaded
directly into applications that run directly on your
machine&#8212;such as a web browser.  This will be a so-called
"end-entity" certificate (i.e. not a proxy) and will be a packaged in
either <a href="http://en.wikipedia.org/wiki/PKCS">PKCS12</a> or <a
href="http://en.wikipedia.org/wiki/X.509#Certificate_filename_extensions">PEM</a>
format.
</p>

<form action="/protected/geteec" method="POST">
<blockquote>
   <table width="100%" cellspacing="0" cellpadding="0" border="0">
     <tr>
        <td>Lifetime: </td>
        <td><select name="lifehours">
              <option selected value="8760">1 year</option>
              <option value="720">1 month</option>
              <option value="24">1 day</option>
              <option value="12">12 hours</option>
              <option value="3">3 hours</option>
            </selected> </td>
        <td>&nbsp;&nbsp;</td>
        <td><font color="green" size="-1"><em>The time before 
            the certificate expires</em></font></td>
     </tr>
     <tr>
        <td nowrap="1">Package Key: &nbsp;&nbsp; </td>
        <td><input type="password" name="pkcskey" size="12"></td>
        <td>&nbsp;&nbsp;</td>
        <td><font color="green" size="-1"><em>A password for 
            sealing the credential file that will be downloaded.  You will need 
            this when you load the certificate into your application.
            </em></font></td>
     </tr>
     <tr>
	<td nowrap="1">Format:</td>
        <td>
	  <label accesskey="k"><input type="radio" name="format" value="PKCS12" checked title="PKCS12"/> PKCS12</label>
	</td>
        <td>&nbsp;&nbsp;</td>
        <td>
	  <font color="green" size="-1"><em>Preferred by browsers and Java.</em></font>
	</td>
     </tr>
     <tr>
	<td></td>
        <td>
	  <label accesskey="m"><input type="radio" name="format" value="PEM" title="PEM"/> PEM</label>
	</td>
        <td>&nbsp;&nbsp;</td>
        <td>
	  <font color="green" size="-1"><em>Commonly used by grid computing tools.</em></font>
	</td>
     </tr>
     <tr>
        <td></td>
        <td colspan="3">
	  <input type="hidden" name="format" value="pkcs12"/>
	  <input type="submit" value="Download Certificate"/>
	</td>
     </tr>
   </table>
</blockquote>
</form>
|;

our $PAGE_MIDDLE_LOGIN_C = qq|
<p>For more information about this site, see the <a href="/">Identity Portal home page</a>.</p>
|;

our $PAGE_MIDDLE_NOLOGIN = qq|
<h3 style="text-align:center">Enabling secure access to Virtual Observatory (VO) services</h3>

<p>
<img style="float:right" src="/images/VOSecurity.gif" />
If you've <a href="http://us-vo.org">used the VO</a>, then you know
that much of the VO can be accessed anonymously through public
services and web sites with out any special logins; however, some
services require more security.  This portal allows you 
to create and manage your own <!--<a href="/aboutNVOLogons.html">VO
Logon</a>--><b>VO Logon</b>&#8212;a single unique identity which you can use to log onto any
VO-compliant portal.
</p>

<p>Through this portal, you can...</p>

<ul>
<!--  <li> <a href="#why">Learn why you might create a VO Logon</a> </li> -->
<!--  <li> <a href="aboutNVOLogons.html">learn how VO Logons work and what
       you can do with them.</a> </li> -->
  <li><a href="http://sso.us-vo.org/purse/register.jsp?voproj=NVO&returnURL=https://nvologin1.ncsa.uiuc.edu/protected/welcome&portalName=The+NVO+Identity+Portal&rg=Submit">Create your own VO Logon</a> </li>
  <li>Manage your NVO profile data <i>(under construction)</i></li>
  <li><a href="https://sso.us-vo.org/purse/password.jsp">Recall a forgotten username or password</a></li>
  <li><a href="http://nvologin1.ncsa.uiuc.edu/protected/welcome">Download proxy credentials</a> for use with local applications</li>
  <li><a href="mailto:bbb\@illinois.edu">Request a server certificate</a> (for service developers; see also <a href="http://dev.usvao.org/vao/wiki/SSOforPortals/TrustRoots">acquiring trust roots</a>)</li>
</ul>

<table cellspacing="12">
   <tr>
     <td width="50%"><table border="3" cellspacing="3" cellpadding="3"><tr>
                     <td bgcolor="#96f3ff">
<h3>
<a href="http://sso.us-vo.org/purse/register.jsp?voproj=NVO&returnURL=https://nvologin1.ncsa.uiuc.edu/protected/welcome&portalName=The+NVO+Identity+Portal&rg=Submit">Register and Create a Logon</a></h3>

Fill out a small registration form and create a username and
password.
        </td></tr></table>
     </td>
     <td><table border="3" cellspacing="3" cellpadding="3"><tr>
                     <td bgcolor="#96f3ff">
<h3><a href="/protected/welcome">Login to the NVO Identity Portal</a></h3>

Use this portal to manage your identity information <i>(under construction)</i>.
     </td></tr></table></td>
   </tr>
</table>

<h3>Why Get a VO Logon?</h3>

While many VO services are publicly accessible, new services are
emerging that require users to login with a user name and password.
These will include:

<ul>
  <li> <strong><i>Access to proprietary data:</i></strong>  A VO Logon
       can allow an observatory to restrict access to data project PIs
       and their teams during the data's proprietary period.  </li>

  <li> <strong><i>Remote storage:</i></strong>  <i>VOSpace</i>
       services can provide a place to store your results of VO
       data discovery and analysis; a VO Logon can restrict access
       to data stored there to you and your collaborators.  </li>

  <li> <strong><i>High-performance processing and
       services:</i></strong>  New computational services will take
       advantage of high-end resources that require higher levels of
       protection and balancing of usage.  A VO Logon can establish
       the necessary trust relationship between service providers and
       users and help ensure that the services are used for their
       intended purpose.  </li>
</ul>

<p>
A major aim of VO Logon is to enable <i>single sign-on</i>&#8212;that is,
it is not necessary to acquire a username and password for every
separately managed site you visit that provides VO services.  More
important than that, single sign-on means that you log on to the VO
through a portal, you have access to VO-compliant data and services
across the VO, regardless of who manages them.  
</p>
|;

our $PAGE_CLOSE = qq|
<p>
Here are some portals currently supporting VO Logons:
<ul>
  <li> <a href="http://nesssi.cacr.caltech.edu/">Caltech NESSSI
       portal</a> for computing on the NSF Teragrid.  </li>

  <li> <a href="http://archive.noao.edu/nsa/">the NOAO Science Archive</a></li>
  <li> <a href="http://archive.noao.edu/nsa/">the NOAO NVO portal</a></li>
</ul>
</p>

<!--
<p>
For more information on how VO Logons work, click
<a href="aboutNVOLogons.html">here</a>.  
</p>
-->
<div class="divider"><hr noshade="noshade" /></div>
</div>
<div id="footer">
|;

our $PAGE_FOOTER = qq|
  </td></tr>

  <tr><td colspan="3">
    <table width="100%" border="0" cellspacing="0" style="margin:0px">
      <tr><td align="middle" width="64">
          <a href="http://www.nsf.gov/"><img align="left"
             src="https://sso.us-vo.org/images/nsflogo_64x.gif" width="64" height="64" border="0">
          </a></td>
          <td width="80" class="tiny">
          Supported by the <br><a href="http://www.nsf.gov/">National
          Science Foundation</a> </td>
      </td>
      <td>&nbsp;</td>
      <td width="120" class="tiny" align="right">
          With contributions from the
          <a href="http://www.ncsa.uiuc.edu">National Center for Supercomputing 
          Applications</a>
      </td>
      <td>
          <img src="https://sso.us-vo.org/images/ncsalogo-foot.gif" height="50" />
      </td>
      <td class="tiny">and</td>
      <td>
          <a href="http://www.globus.org"><img src="https://sso.us-vo.org/images/globusalliance-foot.gif"
             border="0"/></a>
      </td>
      <td>&nbsp;</td>
      <td width="90" class="tiny" align="right">
          Member of the <a href="http://www.ivoa.net">International
          Virtual Observatory Alliance</a> 
      </td>
      <td align="middle" class="tiny" width="80">    
          <a href="http://www.ivoa.net/"><img height="44" align="right"
             src="https://sso.us-vo.org/images/IVOAlogo.gif" width="80" border="0"></a></td>
      </tr>
    </table>
  </td></tr>
</table>
</body>
</html>
|;

sub getOpen          { return $PAGE_OPEN; }
sub getMiddleNologin { return $PAGE_MIDDLE_NOLOGIN; }
sub getMiddleLoginA  { return $PAGE_MIDDLE_LOGIN_A; }
sub getMiddleLoginB  { return $PAGE_MIDDLE_LOGIN_B; }
sub getMiddleLoginC  { return $PAGE_MIDDLE_LOGIN_C; }
sub getClose         { return $PAGE_CLOSE; }
sub getFooter        { return $PAGE_FOOTER; }
sub getHR            { return $HR; }

1;
