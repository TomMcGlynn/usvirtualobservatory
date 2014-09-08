#
#
#
#
#
package XML::Table;
use CGI;

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
    my ($self) =shift;
    my ($res,$status,$message, $desc,$id)  = @_;
    $self->{res}      = $res;
    
    $self->{status}   = $status;
    $self->{id}       = $id;
    $self->{desc}     = $desc;
    $self->{message}      = $message;
    return $self;
}
sub display
{
    my ($self) = shift;
    
    my $res =  qq{<?xml version="1.0" encoding="UTF-8"?>
		      <VAOMonitorTest>
		      <testQuery description = "$self->{desc}">
		      <test item = "$self->{id}">
		      </test>
		      </testQuery>
		      </VAOMonitorTest>};
    
    if ($self->{status} ne 'pass')
    {
	$res= qq{<?xml version="1.0" encoding="UTF-8"?>
		     <VAOMonitorTest>
		     <testQuery description = "$self->{desc}">
		     <test item = "$self->{id}" status = "$self->{status}">
	                $self->{message}
		     </test>
		     </testQuery>
		     </VAOMonitorTest>};
    };
    print $res;
    
}
1;

