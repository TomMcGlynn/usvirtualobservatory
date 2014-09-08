#
#
#
#
#
package Types::USVO;

@ISA= ("Types::SimpleURLResponse");


use strict;
use HTML::TreeBuilder;
use HTML::Element;
use HTML::Form;
use HTTP::Request::Common;
sub new 
{

    my ($class) = shift;
    my $hash = {};    
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
    
}
sub init
{
    my ($self) = shift;
    $self->{url} = $_[0];
    $self->{desc} = 'link test';
    return $self;
}
sub test
{
    my ($self)    = shift;  
    my $withxml   = shift;
    my $res       = $self->getsimpleresponse();
    $self->{res}  = $res;
    $self->{withxml} = '1' if ($withxml);
    my @content      = $res->content;
    my @grepoutputs  = grep (/Convert to position in the sky/,@content);
 
    $self->setId("1.4");    
    $self->setMessage("The links test has failed. The links did not point to the expected pages.");
    $self->setStatus('fail');

    if (@grepoutputs) 
    {
        $self->setMessage('The links test has passed. The links pointed to the expected pages');
        $self->setStatus('pass');
    }
    
    $self->build_table();
    
           
}
sub showresponse
{
   my $self = shift;    
   my $res = $self->getsimpleresponse();
   print $res->content;
}
sub getsimpleresponse
{
    my ($self) = shift; 
    my $url    = $self->{url};
    my $ua     = LWP::UserAgent->new();
    my $response = $ua->get($url);
   # print $response->content; exit();
    my $tree = HTML::TreeBuilder->new_from_content($response->content);
    my $table  = $tree->look_down(
				  '_tag','table',
				  sub {
				         $_[0]->attr('width') eq '600'
					     
			              });
    
    my $ln = $table->extract_links('a','href');
    my $ahref;
   
    foreach my $n (@$ln)
    {
	my @a   = @$n;
        
	my $url = shift @a;
        $ahref  = $url if  ($url eq 'help/index.html');
    }
    
    my $response2 = $ua->get("$url/$ahref");
   

    $tree->delete;
    my $tree1 =  HTML::TreeBuilder->new_from_content($response2->content);
    
    my $table1 = $tree1->look_down('_tag','table',sub {
	                                                $_[0]->attr('width') eq  '438'
						      });
			       
    my $ln1 = $table1->extract_links('a','href');
   
    foreach my $n (@$ln1)
    {
	my @a   = @$n;
	my $url = shift @a;
	$ahref  = $url if  ($url eq 'commontasks.html');
    }
  
    $tree1->delete;
   
    my $response3 = $ua->get("$url/help/$ahref");
    if ($response3->is_success)
    {
	return $response3;
    }
    
}
1;
