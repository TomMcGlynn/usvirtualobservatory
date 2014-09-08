#
#
#
#
#
package Tools::TimeZone;

use CGI::Cookie;

use Exporter;
@ISA= qw (Exporter);
@EXPORT = qw(get_zonename add_zone_adjustment  convert_date_to_epoch_A convert_num_to_month  
	  simple_date convert_date_to_epoch get_month_numbers get_current_gmtime);
use Time::Local;
use strict;


sub get_zonename
{
    my ($julyzone, $janzone,$currentoffset) = @_;
    my %zones = ( "5"  => "EST",
                  "6"  => "CST",
                 "7"  => "MST",
                 "8"  => "PST",           
                  );

    my $name;
    if ($julyzone <   $janzone) 
    {
        #print "You are in a zone that uses daylight savings time:";
        $name =  $zones{$janzone};
        
        if ($julyzone  == $currentoffset)
        {
            #print "You are in daylight savings right now:";
            $name =~ s/(\w)S(\w)/$1D$2/;
            #print "You are using $name<br>";
        }   
    }
    else 
    {
        #print "You are in a zone that does not use daylight savings time";
        $name = $zones{$janzone};
    }
    return "$name";
}
sub get_current_gmtime
{
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) =  gmtime(time);
    my @mons = qw( Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec );

    $mon  +=1;
    $mon = sprintf("%02d", $mon);
    $mday = sprintf("%02d", $mday);
    $hour = sprintf("%02d",$hour);
    $year += 1900;
    return "$year-$mon-$mday $hour:00";

}

##########################################
# add zone adjustment
##########################################
sub add_zone_adjustment
{
    my ($container, $string,$janzone, $julyzone, $usercurrentoffset) = @_;
    my $months               = get_month_numbers();
    my $expires              = $container->{'expires'};   
    my $zonechosen           = $container->{'zone'};

    if ($zonechosen ne "none")
    {
        $zonechosen              = get_zonechosen($zonechosen);
        $usercurrentoffset = $zonechosen;
    }
    my ($y,$m,$d,$h,$mnum,$min,$sec);

    if   ($string eq "eff")
    {       
        $h       = $container->{'hourA'};
        $d       = $container->{'dayA'}; 
        $y       = $container->{'yearA'};
        $m       = $container->{'monthA'};
	#print "Eff  : $h, $d, $y, $m<br>";
    }
    else 
    {
        $h       = $container->{'expires'}->{'hourb'};
        $d       = $container->{'expires'}->{'dayb'};
        $y       = $container->{'expires'}->{'yearb'};
        $m       = $container->{'expires'}->{'monthb'};
        #print "ExP: $h,$d, $y,$m<br>"; 
    }
    
    my $monupper = ucfirst($m);
    my $zonef    = $usercurrentoffset/24;
    my  $mnumA       =  $$months{$monupper}; 
    my $jd    = DateTime::ymdhms2mjd($y, $mnumA, $d, $h,"0","0");
    my $newjd = $jd+$zonef;
   
    ($y,$m,$d, $h, $min,$sec) = DateTime::mjd2ymdhms($newjd);     
    $m =  convert_num_to_month($m,$months);
    return $y,$m,$d,$h;
}
########################################
# convert number to a month name
#######################################
sub convert_num_to_month
{
    my ($m, $months) = @_;
    foreach my $entry (keys %$months)
    { 
        my $value = $$months{$entry}; 
        if ($value eq  $m) 
        {
            $m = lc($entry);
	}
    }
    return $m;
}
#########################################
# zone chosen hash
#########################################
sub get_zonechosen
{

    my ($zonechosen) = @_;
    my %zones = ( 'EST' =>  '5',
                    'CST' =>  '6',
                    'MST' =>  '7',
                    'PST' =>  '8',
	         'EDT' =>  '4',
                    'CDT' =>  '5',
                    'MDT' =>  '6',
                    'PDT' =>  '7',
                    );
  
    my $zone   = $zones{$zonechosen};
    return $zone;
}

######################################
# sub convert date to epoch seconds
######################################
sub convert_date_to_epoch
{  
    my ($year, $month, $day,$hour) = @_;
    $month                   = trim($month);
    $day                     = trim($day);
    $year                    = trim($year);
    $hour                    = trim($hour);
    $month                   = ucfirst($month);
    my $month_nums           = get_month_numbers();
    $year                    = $year-1900;
    $month                   = $$month_nums{$month};
    $month                   = $month-1;  
    my $time                 = timegm(0,0,$hour,$day,$month, $year);
    return $time;
}
sub convert_date_to_epoch_A
{
    my ($year, $month, $day,$hour) = @_;
  $month                   = trim($month);
    $day                     = trim($day);
    $year                    = trim($year);
    $hour                    = trim($hour);
    #$month                   = ucfirst($month);
    my $month_nums           = get_month_numbers();
    $year                    = $year-1900;
    $month                   =~ s/^0+//;
    $month                   = $month-1;
    my $time                 = timegm(0,0,$hour,$day,$month, $year);
    return $time;
}
#####################################
# initialize dates
#####################################
sub get_month_numbers
{
    my  %mon_num = ('Jan' => "1",
                    'Feb' => "2",
                    'Mar' => "3",
                    'Apr' => "4",
                    'May' => "5",
                    'Jun' => "6",
                    'Jul' => "7",
                    'Aug' => "8",
                    'Sep' => "9",
                    'Oct' => "10",
                    'Nov' => "11",
                    'Dec' => "12",
                    );
    return \%mon_num;
}
###################################
# trim whitespace
###################################
sub trim
{
    my ($string) = @_;
    #print "SSPRPRPRR: $string<br>";
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
}
###########################################
#
###########################################
sub simple_date
{
    my ($data) = @_;
    my ($date,$hour) = (split / /,$data);
    $hour =~ s/T//g;
    $hour =~ s/((\d){2})(.*)/$1/;
    my ($y,$m,$d) = (split /\-/,$date);  
    return $y, $m,$d,$hour;
}
1;
