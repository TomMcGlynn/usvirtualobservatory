#
# Author:       Michael Preciado
#
# Description:  Validation Url building class for SIAP services. This class
#               builds the complete Validation URL.It is used to provide a 
#               real time validation test for a given service on the validation 
#               web interface. 
#
#  

package HTML::SIAP;
use Exporter ();
@ISA = qw(Exporter);
@ISA = ("HTML::Parent");
use HTML::Util;
#use URI::Escape;
#use HTML::Entities;

use warnings;

sub new 
{   
    my ($class) = shift;   
    my $objectref= {};    
    bless $objectref, $class;
    $objectref->initialize(@_);
    return $objectref;
}
sub getvalurl
{
    my $self       = shift;
    my $url         = $self->{url};
    my $randnum     = getrand();    
    my $base        = "http:" .  $self->{valURL} . "heasarc$randnum&endpoint=$url";
    my $datastring  = "&RA=" . $self->{ra} . "&DEC=" .  $self->{dec} . "&RASIZE=" . $self->{sr} . "&DECSIZE=" . $self->{sr} . "&format=xml&show=fail&op=Validate"; 
    $base           =   $base . $datastring;
    return $base;
}
1;
