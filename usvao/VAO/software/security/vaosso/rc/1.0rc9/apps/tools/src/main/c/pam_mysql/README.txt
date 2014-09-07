This is a patch to PAM_MySQL to support multiple passwords for a single user.
It was submitted as a patch to sourceforge on March 18 2011, but I don't think
PAM_MySQL is an active project.

    https://sourceforge.net/support/tracker.php?aid=3223965

The patch adds a parameter to PAM_MySQL, "allow_multiple_matches", which is
boolean ("true" or "yes"), default false.

We use it to retrieve grid credentials from MyProxy in OpenID, using a token
from a session cookie as a password.  Since a single user may have multiple
simultaneous sessions in different browsers, we have to check multiple rows.
This patch adds that capability to PAM_MySQL.  For a sample PAM_MySQL config
file, see ../openid/pam.

Also, the patch pam_mysql-salted1.patch has been added to handle salted
hashed passwords.
