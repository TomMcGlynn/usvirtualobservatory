#
#
# Author:       Michael Preciado
#
# Description:  Class to build Registry Validation URLs. The url can be
#               accessed via the validation pages to run a test in real time.
#
#
#  

package HTML::RegistrySearch;
use Exporter ();
@ISA = qw(Exporter);
@ISA = ("HTML::Parent");
use HTML::Util;


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
    my $self   = shift;
    my $url     = $self->{url};
    my $base   =  "http:" .  $self->{valURL} . $url . "&format=xml";
    return $base;
} 
1;
