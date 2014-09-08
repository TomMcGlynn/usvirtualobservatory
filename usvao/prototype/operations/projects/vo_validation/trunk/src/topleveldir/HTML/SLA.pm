#
# Author:       Michael Preciado
#
# Description:  Validation Url building class for SIAP services. This class
#               builds the complete Validation URL.It is used to provide a 
#               real time validation test for a given service on the validation 
#               web interface. 
#
#
#  

package HTML::SLA;
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
    my $url         = encode($r[4]);
    my $randnum     = getrand();    
    my $base        = "http://registry.euro-vo.org:8080/dalvalidate/SLAValidater?runid=heasarc$randnum&endpoint=$url";
    my $datastring  = "&RA=$r[8]" . "&DEC=$r[9]" . "&RASIZE=$r[10]" . "&DECSIZE=$r[10]" . "&format=xml&show=fail&op=Validate"; 
    $base           =   $base . $datastring;
    return $base;
} 
1;
