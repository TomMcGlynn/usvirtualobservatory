package Portal::Welcome;

use strict;
use warnings;
  
use CGI::Cookie ();
use Apache2::Log ();
use Apache2::RequestRec ();
use APR::Table ();

use Apache2::Const -compile => qw(OK);
use constant X509_USER_PROXY => "X509_USER_PROXY";

use Portal::Welcome_Constants;

use POSIX ":sys_wait_h";

sub handler {

    my $GLOBUS_LOCATION = "/usr/local/nvo/globus";
    my $divider = " &nbsp;&nbsp; ";

    my $r = shift; # ($package, $r) = $_;
    $r->content_type('text/html');

    my %cookies = CGI::Cookie->fetch($r);
    my $cookies = %cookies;

    my $user = $r->user();

    my $x509;
    if (defined(%ENV)) {
	$x509 = $ENV{X509_USER_PROXY};
    }

    ##### Page Header #####
    print Portal::Welcome_Constants::getOpen();

    ##### Middle #####
    if (defined($user)) {
	print Portal::Welcome_Constants::getMiddleLoginA();
	print $user;

#print "<hr>Welcome.pm:<br>";
#for my $key (sort (keys %ENV)) {
#    print "$key = $ENV{$key}<br>";
#}
#$ENV{"indirect"} = "not direct";
#print "<hr>perl-test:<br>";
#open (perl_test_handle, "| /usr/local/nvo/globus/bin/perl-test >> /tmp/myproxy_out 2>> /tmp/myproxy_out") || print("can't open perl-test: $!");
#close (perl_test_handle) || print("can't close perl-test: $!");
#print "<hr>";
#print "[Welcome.pm] opened and closed perl-test.";

	print Portal::Welcome_Constants::getMiddleLoginB();

	print "<div class=\"username\">";
	print "Logged&nbsp;in&nbsp;as:&nbsp;<b>$user</b>";
	if (defined($x509)) {
	    my $info_cmd = $GLOBUS_LOCATION . "/bin/grid-proxy-info -debug -file \"$x509\" 2>&1";
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
	    }
	}
	else {
	    print $divider . "No credentials available";
	}
	print $divider . "<a href=\"pc_logout_clearlogin\">Logout</a>";
	print "</div>\n";

	print Portal::Welcome_Constants::getMiddleLoginC();

    } else { # no user present
	print Portal::Welcome_Constants::getMiddleNologin();
    }
    ##### End #####
    print Portal::Welcome_Constants::getClose();

    ##### Page Footer #####
    print Portal::Welcome_Constants::getFooter();

    return Apache2::Const::OK;
}
1;
