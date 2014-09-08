#!/usr/bin/perl 
#	don't use -w as will give error message if invalid URL

use strict;
use CGI qw(:standard);
use LWP::Simple qw(get);	
#	also has 'head' function which conflicts with CGI's 'head' function 
#	only need get, so only get get which fixes that problem.

print header(-type => "text/xml");

my $URL = $ARGV[0];

if ( $URL ) {
	print get $URL;
#	my $content;
#	if (defined ($content = get $URL)) {
#		print $content;
#	}
}

exit;
