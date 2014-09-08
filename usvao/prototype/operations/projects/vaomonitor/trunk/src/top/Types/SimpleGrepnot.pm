#
#
#
#
#
package Types::SimpleGrepnot;

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
 
}
sub showresponse
{
    my $self = shift;
    $self->SUPER::showresponse;
}
sub test
{
    my ($self)      = shift;
    my $withxml     = shift;
    my $params      = $self->{params};  
    my @array       = (split /;/,$params);
    my $matchstring = shift @array;  
    my $res         = $self->SUPER::getsimpleresponse();
    $self->{res}    = $res;
    
    if (! $matchstring)
    {
	build_table($res,$withxml);
	exit();
    }
   
    my @content = $res->content;
    my @grepoutputs = grep (/$matchstring/,@content);  
    build_table($res,$withxml,\@grepoutputs);
   
}
sub build_table
{
    my ($res,$withxml,$grepoutputs) = @_;
    my $id = "1.2";
    my $table;
    my $message = 'pass';
    my $status = 'pass';
    my $desc  =  'string matching test';
   

    if ($grepoutputs)
    {
   	
	$message = 'The response contains an error string';
	$status  = 'fail';
    }
    $table = new HTML::Table($res, "html",$status, $message) if (! $withxml);
    $table = new XML::Table($res, $status,$message, $desc, $id)  if ($withxml);
    $table->display();
   
}
1;
