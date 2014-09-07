Using VAOLogin with the Spring Security Framework.

This directory contains three examples of integrating VAO Logins with
a Portal using the Spring Security Framework.  This is a conventient
framework for managing authentication and authorization within Java
servlet-based applications, and it includes explicit support for
OpenID.  We recommend using Spring Security if you keep track of user
information using some kind of user database.  You can integrate your
portal with the VAO Login Service using the simple examples described
in the the Spring Security (with help from the documentation at
http://sso.usvao.org/help/support.html); however, these examples show
how to plug in special support for VAO users.

The three examples show three different sample portals.  Each 
builds on the previous one, taking a little more advantage of add-ons
provided by this package. 

1. spring-anyOpenID
    o  allows authentication by any OpenID provider
    o  does not require users to register locally
    o  will recognize when the user has authenticated with the VAO login
         service 

2. spring-registration
    o  assumes user needs to register locally to use the portal
    o  shows how to integrate registration with VAO logins and your
       local user database.
    o  introduces the VAOLogin helper class that provides access to 
       user attribute data from the VAO login service.

3. spring-useratts
    o  assumes user needs to register locally to use the portal 
    o  shows how to access user attribute and authorizations from 
       your user database.
    o  introduces the PortalUser helper class for accessing user
       data. 

