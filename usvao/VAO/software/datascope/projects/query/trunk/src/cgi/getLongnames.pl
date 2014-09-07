#!/usr1/local/bin/perl -w
use strict;
use CGI qw(:standard);
my $IVOID = param('IVOID');
my $VOCLIENT = '@VOCLIENT@';

# $IVOID is potentially tainted so should do some checking
#	could be "ivo://nasa.heasarc/abell; /bin/rm -rf *" !!!!
#	expecting something like ivo://nasa.heasarc/abell

my @output = `$VOCLIENT/vodirectory -I $IVOID`
	if ( $IVOID =~ /^ivo:\/\/[\w\.]+\/\w+/ );
#push(@output,$IVOID) unless ( @output ) ;

print header;
print @output;
