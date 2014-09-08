package Nsa::Welcome;

use strict;
use warnings;
  
use CGI::Cookie ();
use Apache2::Log ();
use Apache2::RequestRec ();
use APR::Table ();

use Apache2::Const -compile => qw(OK);
use constant X509_USER_PROXY => "X509_USER_PROXY";

use Nsa::Welcome_Constants;

use POSIX ":sys_wait_h";

sub handler {

    my $GLOBUS_LOCATION = "/usr/local/globus";

    my $r = shift; # ($package, $r) = $_;
    $r->content_type('text/html');

    my %cookies = CGI::Cookie->fetch($r);
    my $cookies = %cookies;

    my $user = $r->user();

    my $x509;
    if (defined(%ENV)) {
	$x509 = $ENV{X509_USER_PROXY};
    }

    print Nsa::Welcome_Constants::getOpen();
    my $divider = " &nbsp;&nbsp;&nbsp; ";
    if (defined($user)) {
	print "<div id=\"username\">";
	print "Logged&nbsp;in&nbsp;as:&nbsp;<b>$user</b>";
#	print $divider . "<a href=\"pc_logout_clearlogin\">Logout</a>";
	if (defined($x509)) {
#	    print $divider . "Credential file = $x509";

	    my $info_cmd = $GLOBUS_LOCATION . "/bin/grid-proxy-info -debug -file \"$x509\" 2>&1";
#	    my $info_cmd = "ls /usr/local";
	    my $info = `$info_cmd`;
	    my @info_lines = split('\n', $info);
	    foreach (@info_lines) {
		if (m/^subject/) {
		    my @subj = split(' : ');
		    my $dn = $subj[1];
		    my $i = index($dn, $user);
		    my $dn_trimmed = $dn;
		    if ($i > 0) {
			$dn_trimmed = substr($dn, 0, $i + length($user));
		    }
		    print $divider . "Grid&nbsp;Identity:&nbsp;<b>$dn_trimmed</b>";
		}
		if (m/^timeleft/) {
		    my @timeleft = split(' : ');
		    print $divider . "Credential&nbsp;will&nbsp;expire&nbsp;in:&nbsp;<b>$timeleft[1]</b>";
		}
#		print "<br>$_";
	    }
	}
	else {
	    print $divider . "No credentials available";
	}
	print $divider . "<a href=\"pc_logout_clearlogin\">Logout</a>";
	print "</div>\n";
    } else { # no user present
	print "<div id=\"username\">";
	print 'Not logged in; <a href="/protected/welcome">click here</a> to log in.';
	print "</div>\n";
    }
    print Nsa::Welcome_Constants::getHead();
    if (defined($user)) {
	print Nsa::Welcome_Constants::getLoggedIn();
    } else {
	print Nsa::Welcome_Constants::getLoginSidebar();
    }
    print Nsa::Welcome_Constants::getRest();
    if (defined($user)) {
	print "<center><em><I><font color=\"green\">" 
	    . "Proprietary access enabled" 
	    . "</font></I></em></center>\n";
    }
    else {
	print "<center><em><I><font color=\"green\">"
	    . "Public data access"
	    . "</font></I></em></center>\n";
    }

    print Nsa::Welcome_Constants::getClose();
    return Apache2::Const::OK;
}
1;
