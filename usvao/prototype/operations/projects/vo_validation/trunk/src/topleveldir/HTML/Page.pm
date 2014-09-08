#build pages for VO validation
#
#
#
package HTML::Page;

use lib "..";
use data::startup;
use CGI;
use SQL::Queries;
use Util::TableTools;
use Exporter ();
use DBI;
@ISA = qw(Exporter);

@EXPORT = qw (build_home_page build_centers_table build_container_for_center 
	      create_stats_table   build_small_stats);

#####################
sub build_small_stats
{
    
    my ($con)  = @_;
    my $cgi = $con->{'cgi'};
  
    my $table = getTodaysList($con);
    
    #table size
    $con->{'servicestable'}  = scalar(@$table);
    my $size                 = scalar(@$table);
   

    #basic stats
    $con->{'deleted'}        = add_up('yes','11',$table);  
    $con->{'skip'}           = add_up('skip','2', $table);
    $con->{'passed'}         = add_up('pass','2', $table);
    $con->{'failed'}         = add_up('fail','2',$table);
    $con->{'aborted'}        = add_up('abort','2',$table);
    $con->{'deprecated'}     = add_up('yes','12',$table);
    $con->{'validated'}      = $size-$con->{skip}-$con->{deleted};
    $con->{'notvalprior'}    = 0; #$notvalprior-$skip;

    
    my $activeservices       = $size-$con->{deleted}-$con->{deprecated};
  
    $con->{'activeservices'} = $activeservices;
   
    #percentages
    
    $con->{'p_passed'}       = substr((($con->{'passed'}) * 100)/$activeservices,0,4);
    $con->{'p_failed'}       = substr((($con->{'failed'})*100)/$activeservices,0,4);
    $con->{'p_aborted'}      = substr((($con->{'aborted'})*100)/$activeservices,0,4);
    $con->{'p_deleted'}      = substr((($con->{'deleted'})*100)/$activeservices,0,4);
    $con->{'p_deprecated'}   = substr((($con->{'deprecated'})*100)/$activeservices,0,4);
    $con->{'p_notvalprior'}  = substr((($con->{'notvalprior'})*100)/$activeservices,0,4); 
    $con->{'p_skip'}         = substr ((($con->{skip})*100)/$activeservices,0,4);


    build_statistics($cgi,$con);
    
    
    print $cgi->br,$cgi->br;

}
######################
sub build_home_page
{
    my ($container) = @_;
  
    my $cgi      = $container->{'cgi'};

    #get xsitype to turnoff from stats 
    my $types_to_skip  = $container->{ignore}; 
    my @a              = keys %$types_to_skip;
    my $turnoff        = join ("|",@a);
    $turnoff           = "&turnoff=$turnoff" if ($turnoff);;

    #show stats on validation
    build_statistics($cgi,$container);
    
    #query buttons
    add_querybutton($cgi,$turnoff);
    
    #user input query box
    add_searchbox($cgi);

    #get last validation time
    my $lastvaltime = get_lastvaltime($container->{'dbh'});
    my $nextvaltime = get_nextvaltime();
    my $number_to_test = get_number_to_test();
    
    
    


    print $cgi->br;
   
    print $cgi->table({-class => "tac", -align => "center", -width => "330", -border => '1'},
		      $cgi->Tr(
			       {-class => 'titleblue'},
			       $cgi->td(
					{-align => "left"},["Last Validation Occured:"]),
			       $cgi->td(
					{-align => "left"}, ($container->{'lastval'}, "ET")),
			       ),
		      $cgi->Tr(
			       {-class => 'titleblue'},   
			       $cgi->td({-align => "left"},["Next Scheduled Validation:"]),
			       $cgi->td({-align => "left"}, ($nextvaltime), "ET") ,
			       ),
		      $cgi->Tr(
			       {-class => 'titleblue'},   
			       $cgi->td({-align => "left"},["Services Tested Daily:"]),
			       $cgi->td({-align => "left"},  $number_to_test ) ,
			       ),
		    
		      $cgi->Tr(
			       { align => 'center', bgcolor => 'FFFFCC'},
			       [
				
				$cgi->td({-colspan => '2'},
					 $cgi->a(
						 {-href => "$::valcode?show=all$turnoff"},
						 $cgi->font({-color  => 'blue'}, "See All Services and Test Runs"))),
				]
			       ),		      
		      
		      );             
    
    print $cgi->br,$cgi->br;
}
############################
sub get_number_to_test
{
    my $number;
    open (File, "$::numbertoval")
	|| die "cannot open $::numbertoval";
    while(<File>)
    {
	my $line = $_;
	chomp $line;
	next if ($line =~ /^\#(.*)/);
	$number = $line;
	last;
    }
    close File || 
	die "cannot close $::numbertoval";
    return $number;
}
############################
sub get_nextvaltime
{
    my $currenttime  = time();    
    my $factor = 60*60*24;
   
    #add one day
    my $newtime = $currenttime+$factor;

    my ($sec, $min, $hour,$mday,$mon,$year,$wday,$yday,$isdt) = localtime($newtime);
    my $date = sprintf("%04d/%02d/%02d %02d:%02d:%02d", $year+1900, $mon+1, $mday, 06, 50, 00);
    return $date;
}
#########################
sub build_statistics
{
    my ($cgi,$container) = @_;
    		      
    
    print  $cgi->table(
		       {-border=> 1,-class => 'tac',-align => 'center', -width => "330"},
			  
		       $cgi->Tr(
				{ -class => "titleblue" },
				$cgi->td({-align => 'left'},("Total Number of Services")),
				$cgi->td({-colspan =>  '2'}, $container->{'servicestable'}),
				
				),
		       $cgi->Tr(
				{-class => "titleblue" },
				$cgi->td({-align => 'left'}, ("Total Number of Active Services Tested")),
				$cgi->td({-colspan => '2'}, $container->{'activeservices'}),      
				),
		       $cgi->Tr(
				{-class => "titleblue"},
				$cgi->td({-align => "left"},("In Validation Queue")),
				$cgi->td({-colspan => '2'}, $container->{'notvalprior'}),
				),		       		       
		       $cgi->Tr(
				{-class => "titleblue"},
				$cgi->td({-align => "left"},("Validated")),
				$cgi->td({-colspan => '2'}, $container->{'validated'}),
				),
		       $cgi->Tr( {-class => "titleblue"},
				 $cgi->td({-align => "left"}, ("Deleted")),
				 $cgi->td({-colspan => '2'}, [$container->{'deleted'} ]),			  
				 ), 

		       $cgi->Tr(),$cgi->Tr(),$cgi->Tr(),$cgi->Tr(),$cgi->Tr(),$cgi->Tr(),
		       $cgi->Tr({-class => "label"},
				$cgi->td("Status"),
				$cgi->td("Total"),
				$cgi->td("Percent"),
				),
		       
		        $cgi->Tr( {-class => "titleblue"},
				 $cgi->td({-align => "left"}, ["Deprecated",$container->{'deprecated'}]),
				 $cgi->td({-align => "left"}, [$container->{'p_deprecated'}]),			  
				 ), 
		       $cgi->Tr( {-class => "titleblue"},
				 $cgi->td({-align => "left"}, ["Skipped",$container->{skip}]),
				 $cgi->td({-align => "left"}, ["" ]),			  
				 ), 
		       $cgi->Tr({-class => "titleblue"},
				$cgi->td({-align => "left"}, ["Passed",$container->{'passed'}]),
				$cgi->td({-align => "left"}, [$container->{'p_passed'}]),
				),
		       $cgi->Tr({-class => "titlered"},
				$cgi->td({-align => "left"}, ["Failed",$container->{'failed'}]),
				$cgi->td({-align => "left"}, [$container->{'p_failed'} ]),
				),	
		       $cgi->Tr( {-class => "titlered"},
				 $cgi->td({-align => "left"}, ["Aborted",$container->{'aborted'}]),
				 $cgi->td({-align => "left"}, [$container->{'p_aborted'} ]),			  
				 ), 
		       
		       
		       ); 
}
###############################
sub add_querybutton
{

    my ($cgi,$turnoff) = @_;
  
    $turnoff =~ s/\&//g;
    my @a =  split(/\=/,$turnoff);
    $turnoff =  $a[1];
    print $cgi->start_form(-method => "get", -action => "$::valcode"); 
    print "<br>";
    print $cgi->table(
		      {-border=> 1,-class => 'tac',-align => 'center'},
		      $cgi->Tr(
			       $cgi->td( {-class => 'greenln'}, "",
				   $cgi->submit( -name  => 'show',-value =>"View Results by Center"),
				   $cgi->hidden(-name =>"turnoff", -default =>[$turnoff]),
				      
					 ),
			       ),
		      $cgi->Tr(
			       $cgi->td( {-class => 'greenln'}, "",
					$cgi->submit( -name  => 'show',-value =>'View Results by Type')),
			       ),
		      $cgi->Tr(
			       $cgi->td( {-class => 'greenln'}, "",
					 $cgi->submit( -name  => 'show',-value =>'View Last Set Tested')),
			       ),
		      $cgi->Tr(
			       $cgi->td( {-class => 'greenln'}, "",
					 $cgi->submit( -name  => 'show',-value =>'View Last Set Stats')),
			       ),
		      );
    print $cgi->end_form;
}
##############################
sub add_searchbox
{
    my ($cgi) = @_;
    print $cgi->table(
		      {-class => 'tac', -align => 'center', width => '300'},
		      $cgi->Tr(
			       $cgi->td( {-class => 'greenln'}, "Query by identifier or string. Any identifiers containing this string will be matched.",
					 )
			       ),
		      );
    print $cgi->start_form( -method  => "get", -action => "$::valcode?");
    
  
    
    print $cgi->table(
		      {-border=> 1,-class => 'tac',-align => 'center'},
		      $cgi->Tr(
			       $cgi->td( {-class => 'greenln'}, "",
					$cgi->textfield( -name  => "querystring")),
			       ),
		      );
    print $cgi->end_form;
}
1;
