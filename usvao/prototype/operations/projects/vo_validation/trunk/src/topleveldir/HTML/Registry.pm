#
#
# Author:       Michael Preciado
#
# Description:  Validation Url building class for Registries. This class
#               builds the complete Validation URL.It is used to provide a 
#               real time validation test for a given service on the 
#               validation web interface. 
#
#
#  

package HTML::Registry;
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
    my $child       = shift;
    my $res         = $child->{res};
    my @r           =  @$res;
    my $url          = $child->{base};
    my $base         = "http://rofr.ivoa.net/regvalidate/HarvestValidater?endpoint=$url&format=xml";
    return $base;
} 
1;
