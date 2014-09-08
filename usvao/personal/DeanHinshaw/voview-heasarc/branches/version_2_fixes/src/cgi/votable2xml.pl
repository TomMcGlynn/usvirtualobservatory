#!/usr/bin/env perl
#
use strict;
use CGI qw(:standard);

$| = 1;

my $name = param("ShortName");
if (!defined($name)) {
    $name= "query";
}
$name =~ s/[^a-zA-Z0-9._\-]/_/g;
#my $sources = param("sources");

my $votable = ( param("sources") )
   ? param("sources") 
   : ( param("resources") )
      ? param("resources")
      : "Error.  sources or resources not given";

print "Content-type: text/xml\n";
print "Content-disposition: attachment; filename=$name.xml\n";
print "\n";

my $haveXML =  $votable =~ /\<\?xml/i;

if (!$haveXML) {
    print "<?xml version='1.0' encoding='UTF-8'?>\n";
}
print $votable;
