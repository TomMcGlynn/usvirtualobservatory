#!/usr1/local/bin/perl -w
use strict;
use CGI qw(:standard);
my $IVOID = param('IVOID');

# $IVOID is potentially tainted so should do some checking
#	could be "ivo://nasa.heasarc/abell; /bin/rm -rf *" !!!!
#	expecting something like ivo://nasa.heasarc/abell

my @output = `@VOCLIENT@/voregistry -I $IVOID`
#my @output = `/www/server/vo/080225/voregistry -I $IVOID`
	if ( $IVOID =~ /^ivo:\/\/[\w\.]+\/\w+/ );
#push(@output,$IVOID) unless ( @output ) ;

print header;
print @output;
