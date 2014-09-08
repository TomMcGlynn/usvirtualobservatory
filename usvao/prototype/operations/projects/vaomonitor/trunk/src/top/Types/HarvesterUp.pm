#
#
#
#
#
package Types::HarvesterUp;
@ISA = ("Types::SimpleURLResponse");


use LWP::UserAgent;
use HTTP::Request;
use HTML::TreeBuilder;
use Time::Local;
#use strict;

#use warnings;

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
    my $url    = $self->{url};
    my $ua     = LWP::UserAgent->new();
    
    my $response = $ua->get($url);
    my $data = $response->content;
   
  
    my $status  = "fail"; 
    eval{
    my $tree = HTML::TreeBuilder->new_from_content($data);
    my $table = $tree->look_down('_tag','table','id','HarvesterTable');
    my @rows = $table->look_down('_tag','tr');
    my @array;
    foreach my $row (@rows)
    {
	my $name = ($row->look_down('_tag','td'))[0];
	if ($name->as_text =~ /(.*)heasarc(.*)/)
	{
	    
	    my $tstamp = ($row->look_down('_tag','td'))[3];
	    my $time = $tstamp->as_text;
	    push @array,$time;
	}
    }
    my $epochtime_1 = convert_to_epoch($array[0]);
    $status = verify_timewindow($epochtime_1);   
    };
    return $status;
}
sub verify_timewindow
{
    my ($epochtime_1) = @_;
    my $t = time;
    my $range = 24*60*60;
    if ($t-$epochtime_1 < $range)
    {
	return  "pass";
    }
}
sub convert_to_epoch
{

    my ($date) = @_;
    my @array  = (split  / /,$date);
    my @a      = (split /\//,$array[0]);
    my @b      = (split /\:/, $array[1]);
    $b[1]      =~ s/^0(\d)/$1/;
    $a[0]   = $a[0]-1;
    my $epoch = timegm("0",$b[1],$b[0],$a[1],$a[0],$a[2]);
    return $epoch;
}
sub initialize
{
    my $self = shift;
    $self->{url} = $_[0]; 
    $self->{params} = $_[1];    
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
   $self->{withxml} = $withxml if ($withxml);
   my $res  = $self->getsimpleresponse();


   my ($table,$err);
   $self->setId("1.0");
   $self->setStatus("fail");
   $self->setMessage("service is down");		
   my $desc = 'is up';
   
   if ($res eq 'pass')
   {
       $self->setStatus("pass");
       $self->setMessage("The Harvester page is up")
   }
 	
   $self->build_table();
   

}
1;




