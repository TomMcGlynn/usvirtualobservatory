#!/usr1/local/bin/perl
#
use strict;
use CGI;

my $cgi = CGI->new();

my $name = $cgi->param("ShortName");
if (!defined($name)) {
    $name= "query";
}
$name =~ s/[^a-zA-Z0-9._\-]/_/g;
my $sources = $cgi->param("sources");

print "Content-type: text/xml\n";
print "Content-disposition: attachment; filename=$name.xml\n";
print "\n";
print $sources
