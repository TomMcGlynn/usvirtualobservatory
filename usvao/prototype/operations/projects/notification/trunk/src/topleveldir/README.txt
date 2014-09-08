# This directory contains the code for the VO notification system. The directory contains:
#
#- 6 perl scripts
#- The Tools,Connect, SQL, Table subdirectories with all the classes needed to run the scripts
#- The css,js directories with css files and js files
#- The images directory
#
#This version of the notification system uses single sign-on to log users into our
#site. This code requires the Python vaoopenid package available at:
#http://dev.usvao.org/vao/wiki/Products/vaologin 

Overview (how to use):

1) The primary script is displaynote.pl.  The user can view the default notices or
expired notices from this page. The user may also click on the "login" button
to enter the site.

2)Logging in:
After the user clicks on "login", he/she is redirected to sso.usvao.org to enter their
login information. Once this information is entered, the user will be redirected to our
page. 

3)Entering a notice:
Once the user is logged in,he/she may click on "Add a notice" and enter text via
the cgi fill out form. After clicking on submit, the user will be returned to
'displaynote.pl' where he/she may view the notice.

4) Deleting a notice:
The user may delete a notice via the "delete" link provided next to their note. Users
may only delete their own notices.

5)Logging out:
The user may log out via the "logout" button provided. Once logged out, the user will
be returned to displaynote.pl


Scripts:

-displaynote.pl - primary, top level script which shows the notices and provides
access to our site.
-login.pl - script that logs the user into our site. This should not be accessed
directly
-deletenote.pl - the script that is invoked when a user clicks on the "delete"
link next to their note.
-logout.pl - the script that logs a user out. This should not be accessed directly
but instead via the "logout" button.
-addnote.pl - the script that adds the notice to our database. This should not be
accessed directly. If accessed directly, will display an error.
- composenote.pl - the script that provides the cgi fill out form once the user
is logged into our site. If invoked directly and the user is not logged in, the
user will be redirected to displaynote.pl


Modules:
-Connect directory: contains module to connect to db
-SQL directory: contains module with SQL queries
-Table directory: contains modules that invoke gpg, encoding/decoding routines, as well
as routines for building the cgi form, mailing users,displaying notices,etc.


Dependencies:
-curl,libcurl,curl-config, pycurl
-python,python-openid
-vaologin
-gpg,.gnupg,uudecode,uuencode


Extra Dependencies (needed in restricted environments)
-libcrypto.so.0.9.7
-libcurl.so.4
-libidn.so.11
-libssl.so.0.9.7
-libusb-0.1.so.4

***
For more information on installing vaologin and other
libraries, see:
http://dev.usvao.org/vao/browser/VAO/software/security/vaologin/rel/1.0/doc/VAOLoginViaCommandLine.txt



