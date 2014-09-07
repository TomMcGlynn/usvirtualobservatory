vaologin: a toolkit for support for VAO OpenID into a Portal

This toolkit provides libraries in multiple languages aid in plugging
in support for VAO OpenID logins. While any OpenID library may be used
to integrate VAO logins into a portal, this toolkit is meant to make
the process a bit easier, particularly when taking advantage of
special features of the VAO service.  In particular, one can use the
toolkit to transparently pull an X.509 certificate representing the
user which the portal can use to access other secured services on the
user's behalf.  

For more information about VAO Logins, please visit http://sso.usvao.org.

Current Capabilities
====================

Currently, this toolkit provides support for Java, Python, Javascript,
and simple CGI scripts.  In particular, the Python implementation
includes CGI scripts that wrap a set of static documents and CGI
scripts requiring the user to login before accessing them; this
solution requires not additional programming to deploy this support.
In a similar vein, we provide an example using the mod_auth_openid
Apache module.  

The Java support is most advanced for use with the Spring Security
framework, which is recommended if your portal is Java-based.  Several
examples are provided that demonstrate how to integrate suppport with
you local user database.  It has been tested with the Apache Web
Server and Apache Tomcat.  

If your portal is heavily Javascript-driven, where Javascript code
needs to access to protected resources, you can deploy one of our
loginstatus services (either as a Java servlet or a CGI script).  Your
code should access this service to determine if the user is
authenticated and if so, what their username is.  

Build dependencies
==================

Python and Command-line Tools
-----------------------------

This portion of the toolkit can be built just with Python.  It
requires Python 2.6 or 2.7 as well as two other python packages:

  python-openid (2.2.5 or later)
  pycurl (7.19.0 or later)

On most Linux systems, these are available as downloadable packages
(e.g. via yum, apt-get, etc.).   

If you do not have root access to install these prerequisites or your
web server runs in a restricted environment (via chroot), you may want
or need to install these prerequisites in user space.  To help with
this, a companion package, vaologin-support, is available from that
vaologin product page (http://dev.usvao.org/vaologin).  Through this
package, you can install as needed Python, the above python modules,
and the system libraries those modules require (zlib, openssl,
and curl).  

Java Tools
----------

If you downloaded this product as a distribution tar ball, you should
find that the Java portion is fully built, including the examples.
The Java examples can (and should) be configured and rebuilt; for this
you will need the Apache Ant build system (http://ant.apache.org/).
Servlets run within an servlet application engine; Apache Tomcat (v5.5
or later) is recommended, but other compliant engines should work.  

If you have retrieved this product from our source repository, you
will need (in addition to Ant) the following jars:

  bcprov-jdk14-123.jar
  commons-logging-1.1.1.jar
  log4j-1.2.15.jar
  servlet-api.jar
  spring-beans-3.0.6.RELEASE.jar
  spring-core-3.0.6.RELEASE.jar
  spring-security-core-3.1.0.RELEASE.jar
  spring-security-openid-3.1.0.RELEASE.jar
  spring-security-web-3.1.0.RELEASE.jar

  aopalliance-1.0.jar
  commons-codec-1.4.jar
  dom4j-1.6.1.jar
  guice-2.0.jar
  httpclient-4.0.jar
  httpcore-4.0.1.jar
  nekohtml-1.9.14.jar
  openid4java.jar
  openxri-client-1.0.1.jar
  openxri-syntax-1.0.1.jar
  spring-aop-3.0.6.RELEASE.jar
  spring-asm-3.0.6.RELEASE.jar
  spring-context-3.0.6.RELEASE.jar
  spring-context-support-3.0.6.RELEASE.jar
  spring-expression-3.0.6.RELEASE.jar
  spring-security-config-3.1.0.RELEASE.jar
  spring-web-3.0.6.RELEASE.jar
  xercesImpl-2.8.1.jar

Place these into the vaologin's lib/jars directory to build the
Java library and its example applications.

Building and Installing 
=======================

To use this toolkit, consult the appropriate how-to document found in
the doc subdirectory; these include instructions for building and
installing the vaologin software as well as for using it within your
web portal system.  

Choose a how-to document according to your type of web portal and how
you would like to integrate VAO Logins: 

  doc/VAOLoginForCGIPortals.txt -- If your web services are simply CGI
      scripts and programs, you can launch them via the VAOLogin
      portal wrapper script.  This solution is completely
      configuration driven, requiring no additional programming.

  doc/VAOLoginViaCommandLine.txt -- If your web services are simply
      CGI scripts, you can easily add VAO Login support by updating
      those scripts to call our command-line tool, vaoopenid.  

  doc/VAOLoginForPython.txt -- If your web service framework is
      python-based, vaologin provides module-level access to the user
      authentication process.  This document explains how to use the
      vaologin python API. 

  doc/VAOLoginForJava.txt -- If use a Java servlet or JSP framework,
      this document can get you started.  The how-tos leverage the 
      Spring Security Framework to manage authentication and
      sessions.  

In addition to these how-to documents, the examples directory includes
several example deployments of the vaologin toolkit.  Consult the
README documents there to see how to try out these examples.  


