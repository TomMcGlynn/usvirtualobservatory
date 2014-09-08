#
#
#
#
#
package Types::SimpleGrep;

@ISA = ("Types::SimpleURLResponse");
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
sub initialize
{
    my $self = shift;  
    $self->SUPER::initialize(@_); 
    $self->{desc} = 'string matching test';
    $self->setId("1.1");
}
sub test
{
    my ($self)       = shift;
    my $withxml      = shift;   
    my $params       = $self->{params};
    my $testname     = $self->{testname};
    my @array        = (split /;/,$params);
    my $matchstring  = shift @array;
    my $res          = $self->getsimpleresponse();
    $self->{res}     = $res;
    $self->{withxml} = '1' if ($withxml);
   
    #set defaults
    $self->setMessage("The $testname  test has failed. String in VOTable response was not matched");
    $self->setStatus('fail');
    
    my @content = $res->content;
    if ($matchstring ne '')
    {
	my @grepoutputs = grep ({/$matchstring/}  @content);
	if  (@grepoutputs)
	{
	    $self->setMessage("The $testname test has passed. String in VOTable response has been matched");
	    $self->setStatus('pass');
	}   
    }
   # $self->build_table();    
}
1;
