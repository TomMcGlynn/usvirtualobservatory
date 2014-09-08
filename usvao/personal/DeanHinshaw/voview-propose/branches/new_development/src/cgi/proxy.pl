#!/usr/bin/perl 
#	don't use -w as will give error message if invalid URL

use strict;
use CGI qw(:standard);
use LWP::Simple qw(get);	
#	also has 'head' function which conflicts with CGI's 'head' function 
#	only need get, so only get get which fixes that problem.

my $base = url(-base=>1);
my $referer = referer();

print header(-type => "text/xml");

if( $referer =~ /$base/ ){

	my $URL = $ARGV[0];

	if ( $URL ) {
		print get $URL;
	}
}else{
	print "Proxy can only be called internally. base: ", $base, " referer: ", $referer, "\n";
}

exit;
