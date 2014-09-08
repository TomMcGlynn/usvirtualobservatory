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
}
sub test
{
    my ($self)      = shift;
    my $withxml     = shift;
    $self->{withxml}  = '1' if ($withxml);
    my $params      = $self->{params}; 
    my @array       = (split /;/,$params);
    my $matchstring = shift @array;  
    my $res         = $self->getsimpleresponse();
    $self->{res}    = $res;
    
    my $testname = $self->{testname};	 
     
    $self->setId("1.1");
    $self->setMessage("The $testname test has failed");
    $self->setStatus("fail");
    
    
    my @content = $res->content;
    my @grepoutputs = grep (/$matchstring/,@content);  

    if (@grepoutputs)
    {
	$self->setMessage("The $testname test has passed");
	$self->setStatus("pass");
    }    
    $self->build_table();
    
}
1;
