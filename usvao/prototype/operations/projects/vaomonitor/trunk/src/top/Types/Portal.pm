#
#
#
#
#
package Types::Portal;
@ISA = ("Types::SimpleURLResponse");


use strict;

use warnings;

sub new 
{
    my ($class) = shift;
    my $hash = {};    
    bless $hash, $class;
    $hash->SUPER::initialize(@_);
    $hash->{desc} = "string matching test";
    return $hash;
}
sub showresponse
{
    my $self = shift;
    my $res = $self->getsimpleresponse();
    print $res;
}
sub test
{
    my $self         = shift;   
    my $withxml      = shift;
    my $url          = $self->{url};  
    my $matchstring  = $self->{params}; 
    $self->{withxml} = '1' if ($withxml);
    
    my $testname     = $self->{testname};
    
    
    $self->setId("2.1");
    $self->setStatus("fail");
    $self->setMessage("Service has failed the $testname test");

    my $res          = $self->getsimpleresponse();   
    my @a  = ($res);
    my @array = grep (/$matchstring/,@a);
    
    if ( @array)
    {
	 $self->setStatus("pass");
	 $self->setMessage("Service has passed the $testname test");
     }
    $self->build_table();
}
sub getsimpleresponse()
{
    my $self = shift;
    my $url = $self->{url};
    my $res;    
    $ENV{PATH} = "/usr/bin/ab";
    $res  = `/usr/bin/ab -n 3 $url`;
    return $res;
}
1;
