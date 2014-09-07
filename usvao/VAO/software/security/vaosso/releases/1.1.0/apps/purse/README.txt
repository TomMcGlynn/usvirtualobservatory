purse:  the Purse user registration web app
-------------------------------------------

This application area contains the purse web app; a build produces
the openid.war web app file.  

Normally this application is build along with the rest of the vaosso
application suite via the ant build file in the vaosso directory
(../..); however, one can re-build just the purse app by running ant
in this directory.  Be sure, however, to first build the entire vaosso
application to ensure purse is properly configured.  

Ant tasks you may want to do in this directory include: 

  ant build      -- (re-)build everything (e.g. the war file)
  ant war        -- (re-)build the openid.war file
  ant jar        -- (re-)build the openid-ip.jar containing the 
                    application code


