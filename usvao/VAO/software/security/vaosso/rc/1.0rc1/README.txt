                  VAOSSO: Federated Login Services
                  --------------------------------

This product provides the deployable services for running the VAO
Single Sign-on services. These services are primarily provided as
browser-based services in a portal. Among the things it provides: 

 o  an OpenID-compliant Identity Provider authentication service
 o  a service for creating a VAO Login
 o  services for handling forgotten user names and passwords
 o  a portal for updating, as a user, one's profile and preferences
 o  services for retrieving short-lived X.509 certificates
    representing the user  

One can obtain the latest version from 
http://dev.usvao.org/vao/wiki/Products/vaosso.  

This package does not provide software to creating tools and portals
that use VAO Logins.  A separate package, call VAOLogin, contains this
support; visit http://dev.usvao.org/vao/wiki/Products/vaologin for
more information.  

Prerequisites
=============

This package has been tested on Fedora systems (v16) but it should
work on any Red-Hat derivative.  With some modification of the build
properties and the instructions herein, it should work with any Linux
distribution.  

It has the following prerequisite software, all of which can be
installed via rpm:
   o  httpd - web server with the following modules
      +  mod_ssl
      +  mod_proxy_ajp
   o  tomcat - servlet container application
   o  mysql v5.x or later - database
   o  globus v4.x or later - grid application toolkit, including
      +  myproxy
      +  gsi - grid security infrastructure
   o  openssl
   o  ant 1.8.2 or later - build framework
   o  java - Sun JDK v1.6 or later is recommended

Alternate web server, servlet container, and database may be possible
with appropriate changes made to the build properties.  

INSTALLING VAOSSO
=================

This package uses ant to build and install the package.  The "install"
target attempts to handle as much of the installation and
configuration as possible.  Root access is required to properly
install VAOSSO (but not to build it).  

Here are the steps:

1.  Obtain vaosso release tar-ball and unpack it.  The root directory it 
    creates upon unpacking is referred to as the distribution
    directory.  

    Make sure the directory is readable by other users.  Fedora 16
    will by default make user home directories unviewable by other
    users.  You can fix this with: 

          chmod a+rx ~

2.  Prepare the platform where vao will be installed (requires root access).

    2.1  Install Java:  either obtain Java JDK from java.com or
         install it with yum.  To install with yum:

          sudo yum install java-1.6.0-openjdk-devel

    2.2. Install globus:

         2.2.1.  configure yum to get Globus from globus.org (see 
     http://www.globus.org/toolkit/docs/5.2/5.2.0/admin/install/#q-bininst
                 for details):

          wget http://www.globus.org/ftppub/gt5/5.2/5.2.0/installers/repo/Globus-repo-config.fedora-16-1.noarch.rpm
          sudo rpm -i Globus-repo-config.fedora-16-1.noarch.rpm

         2.2.2.  install globus

          sudo yum groupinstall globus-gsi voms voms-client
          sudo yum install myproxy-server myproxy

         2.2.3   ensure proper permissions and ownership of /var/lib/myproxy.
                 Make sure that this directory is owned by the myproxy 
                 user and is un-readable by all other users.  

                 In Fedora 16, This will come up with incorrect permissions.
                 To correct it, run:
 
          sudo chmod 0700 /var/lib/myproxy

    2.3. Install remaining prerequesites with yum:

          sudo yum install httpd tomcat6 mysql mysql-server mysql-devel \
                   pam-devel ant ant-nodeps mod_ssl gcc gcc-c++ python-daemon

    2.4. Check and adjust SELinux settings.  This can be done either
         by turning off SELinux completely OR use the "setsebool"
         command to adjust a few parameters.  If you do not have
         this command, then SELinux is probably not in use.  

          sudo setsebool -P  httpd_can_network_connect 1
          sudo setsebool -P  httpd_can_network_connect_db 1

3.  Build and Install the vaosso package

    3.1. Change into the vaosso distribution directory.

    3.2. Edit the properties in build.properties for your
         installation, particularly those labelled "PLEASE CHANGE".  
         See documentation in build.properties and build-default.properties 
         for more information on property meanings and values.  

    3.3. Build the VAOSSO package using ant:

          ant build

    3.4. Install the VAOSSO package (requires root access):

          sudo ant install

         This creates the installation directory and installs the
         VAOSSO software tools and related data into it.  This
         includes the creation of the X.509 Certificate Authority
         and host certificate for the site.  

4.  Set up the user database (requires root access):

    For security purposes, we run the database on a special port.
    Thus we should make sure MySQL is running on the proper port that 
    we configured into build.properties (in Step 3.2).

    4.1. If mysqld is running, stop it:

          sudo service mysqld stop

    4.2. Insert the following line to /etc/my.cnf just below the 
         "[mysqld]" line, 

           port=<port>

         where <port> is the number you set for db.connection.port in
         the build.properties file.  Example:

           [mysqld]
           port=28365

    4.3. Restart mysqld:

          sudo service mysqld start

    4.4. Now initialize the database with ant:
           
          ant db

5.  Start up the system serices (requires root access).  MySQL is
    already running, of course, but we also want set up the services
    so that they start at boot-time.  

    5.1. This will ensure they start up at boot-time:

          sudo chkconfig mysqld on
          sudo chkconfig httpd on
          sudo chkconfig tomcat6 on
          sudo chkconfig myproxy-server-openidip on
          sudo chkconfig myproxy-server-geteec on

    5.2. This will actually start them now (note that mysqld is already running)

          sudo service httpd start
          sudo service tomcat6 start
          sudo service myproxy-server-openidip start
          sudo service myproxy-server-geteec start

6. Review the installation checklist:  

   [editing]

7. Deploy the VAOSSO web applications:

     sudo ant deploy

8. Visit the home page for the newly deployed VAOSSO server.  From
   there you can start creating accounts and testing its services.






