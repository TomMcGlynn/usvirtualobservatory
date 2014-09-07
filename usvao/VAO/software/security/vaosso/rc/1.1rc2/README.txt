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

         Also, if you run mysql on a non-standard port, you will need to
         tell SELinux to allow that port:

          sudo semanage port -a -t mysqld_port_t -p tcp 28365

         where 28365 is the port number.

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
          sudo chkconfig myproxy-server-bycookie on
          sudo chkconfig myproxy-server-bypw on

    5.2. This will actually start them now (note that mysqld is already running)

          sudo service httpd start
          sudo service tomcat6 start
          sudo service myproxy-server-bycookie start
          sudo service myproxy-server-bypw start

    5.3. Adjust your firewall to ensure that your web server can be
         accessed from the outside.  With Fedora 16, one can easily 
         update the firewall with a configuration GUI:

          sudo system-config-firewall &

         The GUI will show a list of network services that you can
         allow access to by clicking their adjacent checkboxes.  (SSH
         is probably already checked.)  Ensure that both "Secure WWW
         (HTTPS)" and "WWW (HTTP)" are checked; click "Apply" after
         making your changes.  

6. Review the installation checklist:  

   o  Is the MySQL server running?  
   o  Is the Apache server running (httpd)? 
   o  Is Tomcat running?  
   o  Is myproxy running?  

7. Deploy the VAOSSO web applications:

     sudo ant deploy

8. Visit the home page for the newly deployed VAOSSO server.  If you
   are using the site certificates that 

From there you can start creating accounts and testing its services.

9. Further customize:

   The web pages that come with VAOSSO out of the box are customized
   for the VAO.  You will want to change the look for your own
   community.  Generally, after making customizations, you will need
   to run 

   Here are some places to look:

   9.1. Change the look of the web page footer and headers.  

        These are found in the web/templates directory.  Here's what
        the files are for:

           styles.include:  a common place to put html/head content,
               including references to stylesheets and javascript
               files.
           body.open.include:  the visible content that appears at the
               top of every web page
           body.close.include:  the visible content that appears at the
               bottom of every web page

        These files will reference other static files, like images,
        stylesheets, and javascript files.  These should be put into
        web/html/images or web/html/styles directory.  Alternatively, 
        you can customize its location by installing explicitly into
        the web server manually in the place where you want it.  

   9.2. Change the home page: 

        This is found as web/html/docs/home.html.  For now, this is a
        static file, so you will need to replicate the header and
        footer bits from 9.1. as desired. 

   9.3. Change the layout of the registration pages

        This is should not be necessary, but it's possible to change
        them.  You can find JSP pages for them under
        apps/purse/web/jsp.  

        Note that the body.open.include and body.close.include files
        will be overwritten by the versions that appear in
        web/templates which you edited in 9.1. 

   9.4. Change the content of the automated emails to new registrants.

        You can find the templates for these under
        apps/purse/web/templates.  Some of these files are artifacts
        from the original Purse distribution from the Globus team and
        are not used in this implementation.  The key ones are:

         tokenMailTemplate:  the mail containing the confirmation
            token which the registrant must send back to the server.  
         caAcceptTemplate:   the mail that announces acceptance of the 
            confirmation token and welcoming the new user.  

   9.5. Change the layout of the login and preferences pages:  

        You can find the templates for these under
        apps/openid-ip/web/templates.  As with 9.3, body.open.include 
        and body.close.include will be overwritten by the versions in 
        web/templates.  

   9.6. Install trusted (e.g. commercial) site certificates.

        


