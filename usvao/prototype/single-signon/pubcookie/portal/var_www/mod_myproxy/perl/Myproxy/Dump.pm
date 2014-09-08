package Myproxy::Dump;

use strict;
use warnings;
  
use CGI::Cookie ();
use Apache2::Log ();
use Apache2::RequestRec ();
use APR::Table ();

use Apache2::Const -compile => qw(OK);

use constant X509_USER_PROXY => "X509_USER_PROXY";

sub handler {

    my $GLOBUS_LOCATION = "/usr/local/globus";

    my $r = shift; # ($package, $r) = $_;
    $r->content_type('text/html');

    my %cookies = CGI::Cookie->fetch($r);
    my $cookies = %cookies;

print qq(
<html>
<head><title>Logged In</title></head>
<body>
<h1>Logged in via Pubcookie</h1>
<div class="block" style="float:right;text-align:right">
  <a href="/protected/dump">/protected/dump</a><br>
  <a href="/protected/portal">/protected/portal</a><br>
  <a href="/protected/welcome">/protected/welcome</a><br>
  <a href="/protected/debug.php">/protected/debug.php</a><br>
  <a href="/debug/">/debug/</a>
</div>
<p><a href="pc_logout_clearlogin">logout</a></p>
);
#<p><a href="pc_logout_clearlogin">logout clearlogin</a> (via LocationMatch)</p>
#<p><i>[doesn't work]</i> <a href="logout/">logout</a> (via .htaccess)</p>
#<p><i>[doesn't work]</i> <a href="pc_logout_redirect">logout redirect</a> (via LocationMatch)</p>
#<p><a href="pc_placebo">placebo</a></p>

    my $user = $r->user();
    print("<h3>User: " 
	  . (defined($user) ? $user : "[undefined]") . "</h3>\n");

    my $x509;
    if (defined(%ENV)) {
	$x509 = $ENV{X509_USER_PROXY};
    }
    print("<h3>" . X509_USER_PROXY . ": " 
	  . (defined($x509) ? $x509 : "[undefined]") . "</h3>\n");

    print("<h3>Cookies:</h3>\n");
    if ($cookies ne '') {
	print("<table border=1>\n");
	print("<tr><th>Name</th><th>Path</th><th>Contents</th></tr>\n");
	foreach (keys %cookies) {
	    my $cookie = $cookies{$_};
	    my $name = $cookie->name();
	    my $value = $cookie->value();
	    my $path = $cookie->path();
	    print("<tr>\n"
		  ."  <td>$name</td>\n"
		  ."  <td>$path</td>\n"
		  ."  <td><font size=-2>$value</font></td>\n"
		  ."</tr>\n");
	    if ($name eq 'pubcookie_g') {
		print('<tr><td colspan=3 style="background:#eef">'
		      . "<h3>Found Granting Cookie</h3>\n");
		if ($user eq '') {
		    print("<p>No username found</p>\n");
		}
		print("</th></tr>\n");
	    }
	}
	print("</table>\n");
    } else {
	print("<p><i>No cookies found.</i></p>\n");
    }

    print("<h3>Environment:</h3>\n");
    if (%ENV ne '') {
	print("<table border=1>\n");
	foreach (keys %ENV) {
	    my $key = $_;
	    my $value = $ENV{$key};
	    print("<tr><td>$key</td>\n  <td>$value</td></tr>");
	}
	print("</table>\n");
    }

#    my $conn = $r->connection;
#    print("Connection = $conn\n");

    print("<h3>Subprocess Env:</h3>\n");
    print("<table border=1>\n");
    $r->subprocess_env->do(sub {
	print("<tr><td>$_[0]</td>\n  <td>$_[1]</td></tr>");
    });
    print("</table>\n");

    print("<h3>Headers:</h3>\n");
    print("<table border=1>\n");
    $r->headers_in->do(sub {
	print("<tr><td>$_[0]</td>\n  <td>$_[1]</td></tr>");
    });
    print("</table>\n");

print qq(
</body>
</html>
);

    return Apache2::Const::OK;
}
1;
