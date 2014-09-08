#
#
#
#
package Util::LoadTypes;

use Exporter ();
@ISA = qw(Exporter);
@EXPORT = qw (load_types);

use strict;
use warnings;

sub load_types
{
    my ($path) = @_;
    open (Types, "$path")
	|| die "cannot open $path\n";

    my $hash = {};
    while(<Types>)
    {
	chomp $_;
	my $line = $_;
	my ($string, $type,$out,$query ) = (split /\|/,$line);
	$hash->{$string}->{'type'} = $type;
	$hash->{$string}->{'out'} = $out;
        $hash->{$string}->{'query'} = $query;
    }
    close Types ||die "cannot close $path\n";
    return $hash;
}
1;
