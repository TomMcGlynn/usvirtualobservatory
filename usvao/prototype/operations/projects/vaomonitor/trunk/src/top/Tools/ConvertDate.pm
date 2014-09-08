#
#
#
#
#
package Tools::ConvertDate;

use Exporter ();
@ISA = qw(Exporter);

@EXPORT = qw(convert_date_to_epoch get_month_numbers);
use Time::Local;
use lib '/www/htdocs/vo/monitor/';
use CGIVO::VOMonutil;
use vars qw(%anchors);

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
1;
