#
#
#
#
#
package Types::SimpleURLResponse;


use LWP::UserAgent;
use URI;
use Types::SimpleGrep;
use XML::SimpleObject::LibXML;
use XML::LibXML;
use HTML::TextTable;
#use Types::SIAP;
#use Types::SSAP;
use strict;
use vars qw(%link_elements);
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
    my $ua = LWP::UserAgent->new();
    my $response = $ua->get($url);
    return $response;
}
sub initialize
{
    my $self = shift;
    $self->{url}      = $_[0]; 
    $self->{testname} = $_[1];
    $self->{params}   = $_[2];
    $self->{desc}     = "simple table download";    
}
sub showresponse
{
   my $self = shift;	
   my $res = $self->getsimpleresponse();
   my $html  =  $res->content;
   my $base = $res->base;
   
   print $html;  
}
sub expand_urls
{
    my ($e, $start,$url,$base) = @_;
    return 1 unless $start;
    my $attr = $link_elements{$e->tag};
    return 1 unless defined $attr;
    $url = $e->attr($attr);
    return 1 unless defined $url;
    $e->attr($attr, url($url, $base)->abs->as_string);
}
sub test
{
   my $self = shift;
   my $withxml = shift;
   $self->{withxml} = '1' if ($withxml);
 
   my $res  = $self->getsimpleresponse();
   
   my $testname = $self->{testname};
   
   my ($table,$err);
   $self->setId('1.0');
   $self->setStatus("fail");
   $self->setMessage("The $testname test has failed the simple IS UP test");
   $self->setRes($res->content);


   if ($res->is_success)
	{
	    $self->setStatus('pass');
	    $self->setMessage("The $testname test has passed the simple IS UP test");
	}	
   #$self->build_table();
   

}
sub setMessage
{
    my $self    = shift;
    my $message = shift;
    $self->{message} = $message;
}
sub getMessage
{
    my $self = shift;  
    return $self->{message};
}
sub setId
{
    my $self    = shift;
    my $id      = shift;
    $self->{id} = $id;
}
sub setStatus
{
    my $self   = shift;
    my $status = shift;
    $self->{status} = $status; 
}
sub setRes
{
    my $self = shift;
    my $res  = shift;
    $self->{res} = $res;
}
sub getRes
{
    my $self = shift;
    return $self->{res};   
}
sub getStatus
{
    my $self = shift;
    return $self->{status};
}
sub build_table
{
    my $self = shift;
    my $table; 
    $table = new HTML::TextTable($self->{res}, "html",$self->{status},$self->{message}) if (! exists ($self->{withxml}));  
    $table = new XML::Table($self->{res},$self->{status},$self->{message},$self->{desc},$self->{id})  if (exists ($self->{withxml}));
    $table->display();   
}
sub check_contents
{
     my ($self, $type, $contents,$testname) = @_; 
     if (($contents  =~ /^SIMPLE/) || (substr($contents,0,2) eq "\x1F\x8B") 
                                   || ($contents =~ /^II\*/) || ($type =~ /image\/jpeg/)  || ($contents eq 'pass'))
     {
        my $message = " The $testname test has passed. "
            . "The VOTable was retrieved and one of the urls "
            . "in the table was downloaded successfully";
        $self->setMessage($message);
        $self->setStatus('pass');
    }
    else
    {
        $self->setMessage("The $testname test failed. The linked data could not be downloaded");
        $self->setStatus('fail');
    }


}
1;




