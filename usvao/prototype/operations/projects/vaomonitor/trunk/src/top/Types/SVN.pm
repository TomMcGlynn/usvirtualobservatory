#
#
#
#
#
package Types::SVN;

@ISA = ("Types::SimpleURLResponse");


#use strict;


use warnings;

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
    my $self = shift;
    $self->SUPER::initialize(@_);
    $self->{desc} = 'svn string matching test';
}
sub test
{
    my ($self)        = shift;
    my $withxml       = shift;
    my $params        = $self->{params};  
    #my @array        = (split /;/,$params);
    $self->{withxml}  = '1' if ($withxml);
    my $matchstring   = shift @array;  
    $self->setId("2.1");
    
    #set  default messages
    $self->setMessage('The response does not contain the string to be matched');
    $self->setStatus('fail');
    
    my $res          = $self->getsimpleresponse();     
    my @content      = $res->content();
    my @grepoutputs  = grep (/Last Changed/,@content);
    if (  @grepoutputs)
    {
	$self->setMessage("The response contains the string to be matched");
	$self->setStatus('pass');
    }
    $self->build_table();
}
1;
