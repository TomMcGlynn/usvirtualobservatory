#
#
#
#
#
package XML::ParseTable;
use CGI;
use XML::SimpleObject::LibXML;

sub new
{
    my ($class) = shift;
    my $hash = {};   
    bless $hash,$class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my $self= shift;
    $self->{xml} = $_[0];
}
sub parse
{
    my $self = shift;
    my $xmlobj  = new XML::SimpleObject::LibXML(XML => $self->{xml});
    my $subtest = $xmlobj->child("VAOMonitorTest")->child("testQuery")->child("test");    
    return "fail" if ($subtest->attribute("status"));
} 
1;

