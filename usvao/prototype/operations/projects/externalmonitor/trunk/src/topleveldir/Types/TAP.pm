#
#
#
package Types::TAP;

@ISA = ("Types::SimpleURLResponse");
#use strict;
use XML::LibXML;
use warnings;

sub new 
{
    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->initialize(@_);
    return $hash;
}
sub initialize
{
    my $self = shift;
    $self->SUPER::initialize(@_);
    $self->setId('9.1');
    $self->{desc} = 'TAP availability';
}
sub test
{
    my ($self)      = shift;
    my $withxml     = shift;
    my $params      = $self->{params};
    my $testname    = $self->{testname};   
    my $res         = $self->getsimpleresponse();   
    $self->{res}     = $res;
    $self->{withxml} = '1' if ($withxml);

    my $string;
    if ($res->is_success)
    {
	#remove element name prefixes
	my $content  = $res->content;
        $content =~ s/(<\w+)\:(.*?>)/<$2/gs;
        $content =~ s/(<\/\w+)\:(.*?>)/<\/$2/gs;
	
        my $parser   = new XML::LibXML;
	my $dom      = $parser->parse_string($content);
	my $xmlobj = new XML::SimpleObject::LibXML($dom);
       
      LINE: foreach my $tag  ($xmlobj->child("availability"))
        {
	  my @tds = $tag->children();
	  foreach my $td (@tds) {
	      my $name  = $td->name;
	      my $value = $td->value;
	      if (($name eq 'available') && ($value eq 'true')) {
		  $string = 1;
		  last LINE;
	      }
          }	  
        }
	
	if ($string)
	{		
	    my $message = "The $testname test has passed. The TAP service is available";	    
	    $self->setMessage($message);
	    $self->setStatus("pass");
	}
	else
	{
	    $self->setMessage("The $testname test has failed. The TAP service is not available");
	    $self->setStatus("fail");
	}
    }
    else
    { 
	$self->setMessage("The $testname test has failed. The TAP service is not available");
	$self->setStatus("fail");
    }    
    #$self->build_table();   
}
1;
