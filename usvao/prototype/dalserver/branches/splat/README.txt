Splatalogue SLAP service
R. Plante
----------------------------------

This an implementation of a SLAP service that access a Splatalogue
Spectral Line database (www.splatalogue.net).  It is built on top of
the DALServer package (http://trac.us-vo.org/project/nvo/wiki/DALServer).
More information about this Splatalogue implementation can be found at 
http://trac.us-vo.org/project/nvo/wiki/SplataSLAP.


Prerequisites
-------------

This package is designed to run as Java Web Service in a web service
container; thus, you will need, at a minimum:

  o  a Java Virtual Machine, compliant with Java v1.5 or later.
  o  a Java Web Service container that supports the Java Servlet API
       (v2.4) and capable of accepting Java WAR files. 

We recommend the following toolkits that provide these requirements:

  o  Sun Java JDK, v1.5.  Download from 
     http://java.sun.com/javase/downloads/index_jdk5.jsp

  o  Apache Tomcat 5.5.  Download from 
     http://tomcat.apache.org/download-55.cgi; documentation is at 
     http://tomcat.apache.org/tomcat-5.5-doc/index.html.

     (Note that this package has not yet been tested with Tomcat 6.x.)

These are sufficient for configuring and installing a released version
of Splatalogue SLAP service.  If you checked this package out from the
SVN code repository, you will need additional packages:

  o  Apache Ant, v1.7.1 or later. (http://ant.apache.org).
     This is used to build the source code and package the JAR and WAR
     files. 

  o  DALServer v0.4 (http://trac.us-vo.org/project/nvo/wiki/DALServer)

  o  MySQL JDBC Connector, v3.1.10 or later.  
     http://dev.mysql.com/downloads/connector/j/.  

Installation
------------

If you have downloaded a released version, then the code is already
built; all that is necessary to do to run is to configure this package
and deploy it.  If you checked out this package from the NVO code
repository or you otherwise would like to rebuild it, consult the
section below on "Building".  

If you are installing a released version, then installation has four
major steps: 

1)  Ensure proper installation of the Splatalogue MySQL database,
    Java, and the Java Servlet engine.  

2)  Edit the conf/web.xml file that connects the web service to the
    Splatalogue MySQL database. 

3)  Create the service WAR file.

4)  Copy the WAR file into the Servlet Engine's service deployment
    directory.  

5)  Test the installation

From here, these instructions will assume the use of the Sun Java JDK,
a MySQL database, and a Tomcat Server.  The Tomcat Server can be set
up to work with an Apache web server, which is typical.  For details
on setting the JDK, the database, and Tomcat, see ....

Step 2: Edit the conf/web.xml file
----------------------------------

The conf/web.xml contains parameter definitions that control how the 
Splatalogue service operates.  Normally only a few parameter values
require editing.   These parameters are found in the section marked
"CONFIGURATION".  

The format of this file is XML.  The name and value of a parameter is
given by lines of this form:

   <param-name>NAME</param-name>
   <param-value>VALUE</param-value> 

Each parameteter has a description (given within the <description>
tag) along with additional help above it.  The parameters one
typically changes are the following:

baseUrl:   The base URL that users will use to access the SLAP web
           service.  At a minimum, replace "MYSERVER" with the full 
           hostname of the machine where the service will be running.  
           The default value assumes that the servlet engine (Tomcat)
           has been integrated into a web server such that all
           servlets are accessible via http://MYSERVER/ws.  (See
           ...)

jdbcUrl:   The JDBC URL for the database that contains the 
           splatalogue data.  When the MySQL database is on the same
           machine as where the service is running, then the default
           should work fine.  If it is running on another machine, 
           change "localhost" to the full hostname of the machine
           where the database is running.  Be sure that that machine's
           firewall allows the service machine to connect on the 3306
           port.  

dbName     The name of the database that contains the
           splatalogue data.  Assuming the recommended setup for the 
           database (see ...),
           the default value is fine.

dbUser     The database user name to use to access the database.
           Change DBUSER to the proper user name (see
           ...).

dbPassoword  The database password to use to connect to the database.
           Change DBPASSWORD to the password necessary for the
           database user (see ...).

Step 3:  Create the service WAR file
------------------------------------

A war file is a fully packaged web servlet that is ready to be
installed into a servlet engine.  A simple script, makewar.sh, can be
used to create this file, loaded with the updated configuration file.
Before you can run this script, you need access to your Java
installation.  At a minimum, you need to have the "jar" command in
your command search path; otherwise, you need to set the environment
variable JAVA_HOME to directory that contains Java (and its bin
directory).  

Assuming Java is setup, you can create the file by typing:

   ./makewar.sh 

You should see it print the following:

   Using configuration file, conf/web.xml
   War file created: dist/splata-slap.war


Step 4:  Install the WAR file
-----------------------------

To install the service into the Tomcat engine, copy the WAR file into
the engine's "webapps" directory.  In particular, if you have properly
installed and started the Tomcat server, then you have had to define
the CATALINA_HOME environment variable.  To copy the file file, type:

   cp dist/splata-slap.war $CATALINA_HOME/webapps

If the Tomcat server is running (or when it starts up), it will notice
the new war file, intialize the service, and make it available at the
configured URL.  

Step 5:  Testing the service
----------------------------

Assuming that the service is running on current server, try accessing
the following URLs to test out the service:

http://localhost/ws:  
   With a default Tomcat setup, this would bring up the Tomcat home
   page.  Seeing this page tells you that the server is running and
   working properly.  

http://localhost/ws/splata-slap/slap:
   This is not a legal SLAP query, but it will tell you if your
   service is running.  You should see s short XML file returned that
   includes the error message, "no operation specified".  

http://localhost/ws/splata-slap/slap?REQUEST=queryData&WAVELENGTH=0.00260075/0.00260080
   This simple query should return a small handful of records in
   VOTable format.  

Building
--------

To rebuild all the source code, you need to have the Ant package
installed (see above) and the "ant" command available to your command
search path.  

If you have obtained this package from the SVN repository, then it
will not include the external Java packages, in the form of jar files,
that this package requires.  You can obtain copies from 
http://trac.us-vo.org/project/nvo/browser/dal/splataslap/externaljars.
Alternatively, you obtain these jar files from the packages' home web
sites if, say, you wish to obtain more recent versions.  The necessary
from are:

   o  DALServer (http://trac.us-vo.org/project/nvo/wiki/DALServer):
      ivoa-dal.jar

   o  SAVOT VOTable Library 
      (http://cdsweb.u-strasbg.fr/cdsdevcorner/savot.html):
      cds-savot.jar

   o  iNamik Text Table Formatter (http://trac.inamik.com/trac/jtable_format):
      texttable.jar

   o  MySQL Connector Library (http://www.mysql.com/downloads/connector/j/):
      mysql-connector-java-3.1.10-bin.jar

   o  kXML Parser (http://kxml.sourceforge.net/):
      kxml2-2.3.0.jar

These should be placed in the "lib" directory.

With ant installed and the jar files in place, one can rebuild the war
file by first editing the conf/web.xml file (see "Step 2" above) and
then typing:

   ant

If you update the configuration (or any source code), you can type
"ant" again to re-create the war file.


