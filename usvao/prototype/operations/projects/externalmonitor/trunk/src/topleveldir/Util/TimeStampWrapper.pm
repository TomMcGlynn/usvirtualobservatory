#
#
#
#
package Util::TimeStampWrapper;
use Util::Uptime;



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
    my $dbh = $_[0];
   
    my $q             = "SQLVo::Queries::getLastValTime";
   
    my $response = &$q($dbh);
    $self->{_lastvaltime} = $response;   
    

    
}
sub getLastValTime
{
    my $self = shift;
    return $self->{_lastvaltime};

}
sub getNextValTime
{
    my $self = shift;
    my $date = '';
    if ($self->{_lastvaltime})
    {   
	my ($ymd, $hms) = (split " ", $self->{_lastvaltime});
        
	my ($h, $m,$s) = (split ":",$hms);
	
     
	my ($cursec, $curmin, $curhour,$curmday,$curmon,$curyear,$wday,$yday,$isdt) = localtime(time);
	
	if (($curmin >= 30) && ($cursec >= 0 ))
	{
	    $curhour++;
	}
	$date = sprintf("%04d/%02d/%02d %02d:%02d:%02d", $curyear+1900, $curmon+1, $curmday, $curhour, 30, 00);
    }
    return $date;
}
1;
