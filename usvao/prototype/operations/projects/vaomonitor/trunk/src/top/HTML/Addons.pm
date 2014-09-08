#
#
#
#
package HTML::Addons;
use Util::TimeStampWrapper;
use strict;

sub add_timestamp_box
{
    my ($cgi,$dbh) = @_;
    
    my $tsw = new Util::TimeStampWrapper($dbh);
    my $lasttime = $tsw->getLastValTime();
    my $nexttime = $tsw->getNextValTime();
    print $cgi->table({-class => "tac", -align => "center", -width => "360", -border => '1'},
                      $cgi->Tr(
                               {-class => 'titleblue'},
                               $cgi->td(
                                        {-align => "left"},["Last Monitor Check Occurred:"]),
                               $cgi->td(
                                        {-align => "left"}, ("$lasttime ET")),
                               ),
                      $cgi->Tr(
                               {-class => 'titleblue'},   
                               $cgi->td({-align => "left"},["Next Scheduled Monitor Check:"]),
                               $cgi->td({-align => "left"}, (" $nexttime ET") ,
                               ),
                      $cgi->Tr(
                               {-class => 'titleblue'},   
                               $cgi->td({-align => "left"},["Services Tested Daily:"]),
                               $cgi->td({-align => "left"},  "ALL"    ) ,
                               ),
                    
                      $cgi->Tr(
                               { align => 'center', bgcolor => 'FFFFCC'},
                               [
                                
                                $cgi->td({-colspan => '2'},
                                         $cgi->a(
                                                 {-href => "$::valcode?show=all"},
                                                 $cgi->font({-color  => 'blue'}, "See All Services and Test Runs"))),
                                ]
                               ),                     
			       
			       ),          
		      );



		      
}
1;
