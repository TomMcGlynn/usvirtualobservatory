openid-ip:  the OpenID Identity Provider web app
------------------------------------------------

This application area contains the openid-ip web app; a build produces
the openid.war web app file.  

Normally this application is build along with the rest of the vaosso
application suite via the ant build file in the vaosso directory
(../..); however, one can re-build just the openid-ip app by running ant
in this directory.  Be sure, however, to first build the entire vaosso
application to ensure openid-ip is properly configured.  

Ant tasks you may want to do in this directory include: 

  ant build      -- (re-)build everything (e.g. the war file)
  ant war        -- (re-)build the war file
  ant jar        -- (re-)build the jar containing the application code
  ant test       -- run the unit tests

Note that unit tests currently only provide coverage for the IdRequest
class (the core of the application business logic) and Conf.  A few
older ad-hoc tests also exist, and can be executed via "ant test-adhoc"; 
these require an operating and configured MySQL user database.  

