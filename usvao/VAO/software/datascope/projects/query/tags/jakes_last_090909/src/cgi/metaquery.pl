#!/usr1/local/bin/perl -w
use strict;
use CGI qw(:standard);
my $IVOID = param('IVOID');

# $IVOID is potentially tainted so should do some checking
#	could be "ivo://nasa.heasarc/abell; /bin/rm -rf *" !!!!
#	expecting something like ivo://nasa.heasarc/abell

my @output = `@VOCLIENT@/voregistry -m $IVOID`
	if ( $IVOID =~ /^ivo:\/\/[\w\.]+\/\w+/ );

print header;
print "<div>";
print "<table width='100%'><tr><td align=right><a href='' onclick='myModal.close();return false'>Click to close</a></td></tr></table>";
print "<pre style='font-size: 8pt;'>";
print @output;
print "</pre>";
print "</div>";
