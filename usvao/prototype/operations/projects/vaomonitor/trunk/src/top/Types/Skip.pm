#
#
#
#
#
package Types::Skip;
@ISA = ("Types::SimpleURLResponse");

use LWP::UserAgent;
use Types::SimpleGrep;
use strict;

use warnings;

sub new 
{

    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->initialize(@_);
    return $hash;

}
sub getsimpleresponse
{
    my ($self) = shift; 
    my $url = $self->{url};   
    return "skip";
}
sub initialize
{
    my $self = shift;
    $self->SUPER::initialize(@_); 
    $self->{desc} = 'test being skipped';    
}
sub showresponse
{
   my $self = shift;	
   my $res = $self->getsimpleresponse();
   print $res;
}
sub test
{
   my $self = shift;
   my $withxml = shift;
   $self->{withxml} = '1' if ($withxml);
   my $res  = $self->getsimpleresponse();
 
   #messages
   $self->setId("99.0");
   $self->setMessage("this test is being skipped");
   $self->setStatus("skip");     
   $self->build_table();

}
1;




