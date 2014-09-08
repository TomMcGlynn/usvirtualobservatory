#
#
#
#
package Util::Uptime;
use Exporter ();
@ISA = qw(Exporter);
@ISA = ("Util::TimeStampWrapper");



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
    my $con = $_[0];
    $self->{con}  = $con;
    
    my $q1            = "SQLVao::Queries::getLastDownTime";
    my $q2            = "SQLVao::Queries::getCurrentTime";
    my $q3            = "SQLVao::Queries::getCurrentHealthService";
    
    my $res            = &$q1($con);    
    my @a = @$res; 
    $self->{_lastdown} = $res;
    
    $self->{con}->{show}  = SQLVao::Queries::getServiceName($con);
    $self->{con}->{name} = $self->{con}->{show};     
    
    #get now time
    $self->{_now}      = &$q2($con);
 

    #get latest stats for service
    my $res3  = &$q3($con);   
    my @b     = @$res3;
    $self->{_latestdata} = $res3;
    
    $self->{name} =  $self->{con}->{name};
    
}
sub getName
{
    my $self  = shift;
    return $self->{name};
}
sub getColChange
{
    my $self = shift;
    return $self->{colchange};
}
sub getUpTime
{
    my $self       = shift;  
    my $latestdata = $self->{_latestdata};
    
    #last down time
    my $res        = $self->{_lastdown};
    my @array      = @$res; 
    my $tdown      = $array[0][2];
    
    
    #now time
    my $now   = $self->{_now};
    my @data  = @$now;
    my $tnow  = $data[0][0];    
   
    my $notup;
    my $uptime = [];
    my $bigarray;
    foreach my $n (@$latestdata)
    {
	my @array  = @$n;
	$notup = "fail" if ($array[5] ne "pass" );
    }
   
    
    if ($notup)
    {
	#service not working.	
	$self->{colchange} = 'down for at least';
	my $tup            =  SQLVao::Queries::getLastTimeUp($self->{con});	
	
	#corner case ;service never up
	$uptime   = ['never up', '',''];

	if ($tup)  
	{
	    #service was up at some point
	    my $sec       = $tnow-$tup;
	    $uptime->[0]  = int($sec/(24*60*60));
	    $uptime->[1]  = ($sec/(60*60))%24; 
	    $uptime->[2]  = ($sec/60)%60; 
	}
    }
    else
    {
       
	#service is up right now
	
	$self->{colchange} = 'time up';
	if (!$tdown)
	{   
            #corner case, service has always been up	    
	    $tdown    = SQLVao::Queries::getEarliestTime($self->{con});
	    $uptime   = ['always up','',''];       
	}
	else
	{
	   
	    #service currently up ...was down in the past,need uptime	    
	    my $sec       = $tnow-$tdown;
	    $uptime->[0]  = int($sec/(24*60*60));
	    $uptime->[1]  = ($sec/(60*60))%24; 
	    $uptime->[2]  = ($sec/60)%60;	    
	}	    
    }    
    $bigarray->[0]  = $uptime;
    return $bigarray;    
}
1;
