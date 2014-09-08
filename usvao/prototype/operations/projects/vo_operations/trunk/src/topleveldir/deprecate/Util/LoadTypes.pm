#
#
#
#
package Util::LoadTypes;

use Exporter ();
@ISA = qw(Exporter);
@EXPORT = qw (load_types load_downloadtypes);

use strict;
use warnings;


sub load_types
{
    my ($homedir) = @_;
   
    open (Types, "$homedir/data/types")
	|| die "cannot open types\n";

    my $hash = {};
    while(<Types>)
    {
	chomp $_;
	my $line = $_;
	my ($type, $string,$id) = (split /\|/,$line);
	$hash->{$string}->{'type'} = $type;
	$hash->{$string}->{'id'} = $id;
	print "$hash->{$string}->{'type'}\n";
    }
    close Types ||die "cannot close types\n";

    return $hash;
}

sub load_downloadtypes
{
    my ($homedir) = @_;

   
    open (Types, "$homedir/data/downloadtypes")
        || die "cannot open downloadtypes\n";
    
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
    close Types ||die "cannot close downloadtypes\n";

    return $hash;
}
1;
