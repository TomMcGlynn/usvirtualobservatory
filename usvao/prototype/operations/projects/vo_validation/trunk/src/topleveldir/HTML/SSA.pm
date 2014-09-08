#
# Author:       Michael Preciado
#
# Description:  Validation Url building class for SSA services. This class
#               builds the complete Validation URL.It is used to provide a 
#               real time validation test for a given service on the validation 
#               web interface. 
#
#
#  

package HTML::SSA;
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
    my $self       = shift;
    my $url         = $self->{url};
    
    my $base        = "http:" . $self->{valURL} . "&POS=" . $self->{ra} . "," . $self->{dec} . "&SIZE=" . $self->{sr} . "&TIME=&BAND=&FORMAT=ALL&spec=" 
                     . "Simple+Spectral+Access+1.03&addparams=&service=" 
                     . "http://voparis-validator.obspm.fr/xml/111.xml?&serviceURL=$url"
                     . "&format=XML"; 
    return $base;
}
1;
