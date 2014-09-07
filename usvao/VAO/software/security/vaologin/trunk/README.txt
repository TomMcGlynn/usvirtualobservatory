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

Current Capabilities:
=====================

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
loginstatus services.  Your code should access this service to
determine if the user is authenticated and if so, what their username
is.  

Build dependencies:
===================

Java Tools:

If you downloaded this product as a distribution tar ball, you should
find that the Java portion is fully built, including the examples.
The Java examples can (and should) be configured and rebuilt; for this
you will need the Apache Ant build system.  

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

Python and Command-line Tools:

This portion of the toolkit can be built just with Python.  It
requires two other python packages:

  python-openid (2.2.5 or later)
  pycurl (7.19.0 or later)

On most Linux systems, these are available as downloadable packages
(e.g. via yum, apt-get, etc.).   

Build Instructions:
===================

As noted above, if you received this product, the Python and Java
toolkits will already be built.  However, to build the included
toolkits from source: 

  1.  Install Ant on your platform
  2.  Install the above mentioned jar files
  3.  Type "ant build"

The Python and Java components can be built separately via the
build-python and build-java build targets.  The former will put
compiled python code into lib/python and stand-alone scripts into
bin.  The latter will create lib/vaologin-1.0.jar.   

The individual examples under the examples directory have build
scripts as well; see their README files for details.  

