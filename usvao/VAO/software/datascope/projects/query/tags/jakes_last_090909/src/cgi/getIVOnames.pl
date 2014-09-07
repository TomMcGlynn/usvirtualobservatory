#!/usr1/local/bin/perl -w
use strict;
use CGI qw(:standard);
my $IVOID = param('IVOID');

# $IVOID is potentially tainted so should do some checking
#	could be "ivo://nasa.heasarc/abell; /bin/rm -rf *" !!!!
#	expecting something like ivo://nasa.heasarc/abell

print header;
if ( $IVOID =~ /^ivo:\/\/[\w\.]+\/\w+/ ) {
    my @output = `@VOCLIENT@/voregistry -r -f "Identifier,ShortName" $IVOID`;

    my $id  = uc($IVOID);
    my $len = length($id);

    $id =~ s/\./\//;

    for my $line (@output) {
        my $modLine = uc($line);
        $modLine =~ s/\./\//;
        if ( ($id eq $modLine) || 
	   (substr($modLine, 0, $len+1) eq ($id." ") )  ||
	   (substr($modLine, 0, $len+1) eq ($id."/") ) ) {
	    print $line;
        }
    }
}
