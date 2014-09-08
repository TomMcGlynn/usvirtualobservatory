#
#
# Author:       Michael Preciado
#
# Description:  Validation Url building class for Cone services. This class
#               builds the complete Validation URL.It is used to provide a 
#               real time validation test for a given service on the 
#               validation web interface. 
#
#
#  

package HTML::TestWrapper;
use Exporter ();
@ISA = qw(Exporter);
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
sub initialize
{
   my $self  = shift;
   $self->{testurl} =  $_[0];  
 
}
sub test
{
    my $child       = shift;
    my $res         = $child->{res};
    my @r           =  @$res;
    my $url         = encode($child->{base});
    my $randnum     = getrand();    
    my $base        = "http://nvo.ncsa.uiuc.edu/dalvalidate/ConeSearchValidater?runid=heasarc$randnum&endpoint=$url";
    my $datastring  = "&RA=$r[7]" . "&DEC=$r[8]" . "&SR=$r[9]" . "&format=xml&show=fail&op=Validate"; 
    $base =   $base . $datastring;
    return $base;
}
sub dumptodb
{



}
1;
