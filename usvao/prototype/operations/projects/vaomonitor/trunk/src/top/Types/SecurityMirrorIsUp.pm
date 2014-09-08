#
#
#
#
#
package Types::SecurityMirrorIsUp;
@ISA  = ("Types::SimpleURLResponse");
use HTML::Form;


use LWP::UserAgent;
use Types::SimpleGrep;
use HTTP::Request;
use Types::SimpleGrepnot;

use warnings;

sub new 
{
    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
}
sub getsimpleresponse
{
    my ($self) = shift; 
    my $url = $self->{url};
    my $ua = LWP::UserAgent->new();
    #push @{ $ua->requests_redirectable }, 'POST';
    
    my $response = $ua->get($url);
 
    my  @forms = HTML::Form->parse($response);
    my $form = shift @forms;
    #my @inputs = $form->inputs;
    #my $value;
   
    my $req =   $form->click();
    my $r  = $ua->request($req);   
    return $r;
}
sub init
{
    my $self = shift;
    $self->SUPER::initialize(@_); 
    $self->{desc}   = 'is up';
}
sub showresponse
{
   my $self = shift;	
   my $res = $self->getsimpleresponse();
   print $res->content;
}
sub test
{
   my $self = shift;
   my $withxml = shift;
   $self->{withxml} = '1' if ($withxml);
   my $res  = $self->getsimpleresponse();

   	
   $self->setId('1.0');
   $self->setStatus("fail");
   $self->setMessage('service is down');
   
   if ($res->is_success)
   {     
       $self->setStatus('pass');
       $self->setMessage('NVO Security Mirror is up');
   }
   $self->build_table();
}
1;




